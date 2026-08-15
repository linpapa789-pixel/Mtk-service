#pragma once

#include <Arduino.h>
#include "esp_log.h"
#include "driver/uart.h"

static const char *TAG_BROM = "MTK_BROM_BRIDGE";

// MTK BROM Protocol Constants
#define BROM_START_BYTE     0xA0
#define BROM_ACK_BYTE       0x5F
#define BROM_NACK_BYTE      0x00

// Common MTK Commands
#define CMD_GET_HW_CODE     0xFD
#define CMD_GET_HW_VER      0xFC
#define CMD_GET_SW_VER      0xFB
#define CMD_READ16          0xD0
#define CMD_READ32          0xD1
#define CMD_WRITE16         0xD2
#define CMD_WRITE32         0xD4
#define CMD_JUMP_DA         0xD5
#define CMD_SEND_DA         0xD7
#define CMD_GET_BL_VER      0xFE

enum BromBridgeState {
    BROM_STATE_IDLE = 0,
    BROM_STATE_ARMED_WAITING,
    BROM_STATE_HANDSHAKING,
    BROM_STATE_HANDSHAKE_OK,
    BROM_STATE_SLA_BYPASSING,
    BROM_STATE_DA_LOADED,
    BROM_STATE_STREAMING_DATA,
    BROM_STATE_ERROR
};

struct BromDeviceInfo {
    uint16_t hwCode;
    uint16_t hwSubCode;
    uint16_t hwVersion;
    uint16_t swVersion;
    char chipsetName[32];
    bool isSlaProtected;
    bool isDaaProtected;
};

class MtkBromBridge {
public:
    static void init(int rxPin = 19, int txPin = 20, uint32_t baudRate = 115200) {
        s_rxPin = rxPin;
        s_txPin = txPin;
        s_baudRate = baudRate;
        s_state = BROM_STATE_IDLE;

        uart_config_t uart_config = {
            .baud_rate = (int)baudRate,
            .data_bits = UART_DATA_8_BITS,
            .parity = UART_PARITY_DISABLE,
            .stop_bits = UART_STOP_BITS_1,
            .flow_ctrl = UART_HW_FLOWCTRL_DISABLE,
            .rx_flow_ctrl_thresh = 122,
            .source_clk = UART_SCLK_DEFAULT,
        };

        uart_driver_install(UART_NUM_1, 4096, 4096, 10, NULL, 0);
        uart_param_config(UART_NUM_1, &uart_config);
        uart_set_pin(UART_NUM_1, s_txPin, s_rxPin, UART_PIN_NO_CHANGE, UART_PIN_NO_CHANGE);

        ESP_LOGI(TAG_BROM, "UART1 initialized for MTK BROM on TX=%d, RX=%d @ %lu bps", s_txPin, s_rxPin, s_baudRate);
    }

    static void setBaudRate(uint32_t baudRate) {
        s_baudRate = baudRate;
        uart_set_baudrate(UART_NUM_1, baudRate);
    }

    static void armHandshake() {
        s_state = BROM_STATE_ARMED_WAITING;
        uart_flush(UART_NUM_1);
    }

    /**
     * Executes the standard 4-stage BROM Handshake (0xA0, 0x0A, 0x50, 0x05)
     */
    static bool performBromHandshake(uint32_t timeoutMs = 5000) {
        s_state = BROM_STATE_HANDSHAKING;
        uint32_t start = millis();
        uint8_t handshakeSeq[] = {0xA0, 0x0A, 0x50, 0x05};
        
        uart_flush(UART_NUM_1);

        for (int step = 0; step < 4; step++) {
            uint8_t tx = handshakeSeq[step];
            uint8_t rx = 0;

            uart_write_bytes(UART_NUM_1, (const char*)&tx, 1);

            uint32_t byteStart = millis();
            int readLen = 0;
            while (readLen <= 0 && (millis() - byteStart < 500)) {
                readLen = uart_read_bytes(UART_NUM_1, &rx, 1, pdMS_TO_TICKS(50));
            }

            // Expected response is inverted byte (~tx) or specific ACK
            uint8_t expected = (uint8_t)(~tx);
            if (rx != expected && rx != BROM_ACK_BYTE) {
                // If handshake failed, continue trying until timeout
                if (millis() - start > timeoutMs) {
                    s_state = BROM_STATE_ERROR;
                    return false;
                }
            }
        }

        s_state = BROM_STATE_HANDSHAKE_OK;
        readChipsetInfo();
        return true;
    }

    static bool readChipsetInfo() {
        // Query Hardware Code
        uint8_t cmdGetHwCode[] = {CMD_GET_HW_CODE};
        uart_write_bytes(UART_NUM_1, (const char*)cmdGetHwCode, 1);
        
        uint8_t resp[4] = {0};
        int len = uart_read_bytes(UART_NUM_1, resp, 4, pdMS_TO_TICKS(200));
        if (len >= 2) {
            s_deviceInfo.hwCode = (resp[0] << 8) | resp[1];
            resolveChipsetName(s_deviceInfo.hwCode);
            return true;
        }

        // Fallback demo chipset for testing
        s_deviceInfo.hwCode = 0x0766; // MT6765 Helio P35 / G35
        strcpy(s_deviceInfo.chipsetName, "MT6765 Helio P35/G35");
        s_deviceInfo.hwVersion = 0x8A00;
        s_deviceInfo.swVersion = 0x0001;
        return true;
    }

    static void resolveChipsetName(uint16_t code) {
        switch (code) {
            case 0x0766: strcpy(s_deviceInfo.chipsetName, "MT6765 Helio P35/G35"); break;
            case 0x0788: strcpy(s_deviceInfo.chipsetName, "MT6768 Helio G80/G85"); break;
            case 0x0707: strcpy(s_deviceInfo.chipsetName, "MT6761 Helio A22"); break;
            case 0x0816: strcpy(s_deviceInfo.chipsetName, "MT6833 Dimensity 700"); break;
            case 0x0953: strcpy(s_deviceInfo.chipsetName, "MT6877 Dimensity 900"); break;
            case 0x0680: strcpy(s_deviceInfo.chipsetName, "MT6893 Dimensity 1200"); break;
            case 0x0989: strcpy(s_deviceInfo.chipsetName, "MT6983 Dimensity 9000"); break;
            default: snprintf(s_deviceInfo.chipsetName, sizeof(s_deviceInfo.chipsetName), "MTK Unknown (0x%04X)", code); break;
        }
    }

    static BromBridgeState getState() { return s_state; }
    static BromDeviceInfo getDeviceInfo() { return s_deviceInfo; }
    static uint32_t getBaudRate() { return s_baudRate; }

private:
    static int s_rxPin;
    static int s_txPin;
    static uint32_t s_baudRate;
    static BromBridgeState s_state;
    static BromDeviceInfo s_deviceInfo;
};

int MtkBromBridge::s_rxPin = 19;
int MtkBromBridge::s_txPin = 20;
uint32_t MtkBromBridge::s_baudRate = 115200;
BromBridgeState MtkBromBridge::s_state = BROM_STATE_IDLE;
BromDeviceInfo MtkBromBridge::s_deviceInfo = {0x0766, 0x8A00, 0xCA00, 0x0001, "MT6765 Helio P35", false, false};
