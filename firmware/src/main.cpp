/*
 * ==============================================================================
 * MTK BROM Service & Glitch Coprocessor Firmware
 * Target: ESP32-S3-DevKitC-1-N16R8 (16MB Flash, 8MB Octal PSRAM)
 * Designed for Android MTK Workshop Pro APK & Hardware Service Suite
 * ==============================================================================
 */

#include <Arduino.h>
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include <FastLED.h>
#include "GlitchEngine.h"
#include "MtkBromBridge.h"
#include "NetworkBridge.h"

#define FIRMWARE_VERSION "v2.5-N16R8"
#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64
#define OLED_RESET -1
#define SCREEN_ADDRESS 0x3C

// Hardware Pin Definitions for ESP32-S3 DevKitC-1
#define PIN_GLITCH       4
#define PIN_STATUS_RGB   48
#define PIN_I2C_SDA      8
#define PIN_I2C_SCL      9
#define PIN_UART_RX      19
#define PIN_UART_TX      20

// RGB LED (WS2812)
#define NUM_LEDS 1
CRGB leds[NUM_LEDS];

// OLED Display (SSD1306)
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);
bool oledAvailable = false;

// 8MB Octal PSRAM Buffer Allocation (512KB Streaming DMA buffer)
#define STREAM_BUFFER_SIZE (512 * 1024)
uint8_t* psramStreamBuffer = nullptr;

// Status Flags
String lastLogMessage = "System Booting...";
uint32_t activeBaud = 115200;
uint32_t glitchDelayUs = 350;
uint32_t glitchWidthNs = 500;

void updateOledDisplay() {
    if (!oledAvailable) return;

    display.clearDisplay();
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);

    // Header
    display.setCursor(0, 0);
    display.println("MTK WORKSHOP S3");
    display.drawLine(0, 10, 127, 10, SSD1306_WHITE);

    // Hardware Specs
    display.setCursor(0, 14);
    display.printf("PSRAM: %dMB / 8MB\n", (int)(ESP.getFreePsram() / (1024 * 1024)));

    display.setCursor(0, 24);
    display.printf("Mode: %s\n", NetworkBridge::isClientConnected() ? "LINKED (WiFi/USB)" : "ARMED / IDLE");

    display.setCursor(0, 34);
    display.printf("BROM: %s\n", MtkBromBridge::getDeviceInfo().chipsetName);

    display.setCursor(0, 44);
    display.printf("Glitch: %luns | %luus\n", glitchWidthNs, glitchDelayUs);

    display.setCursor(0, 54);
    display.printf("Ver: %s (N16R8)", FIRMWARE_VERSION);

    display.display();
}

void setStatusColor(CRGB color) {
    leds[0] = color;
    FastLED.show();
}

void handleAndroidCommand(const char* cmd, JsonDocument& doc) {
    lastLogMessage = String(cmd);
    Serial.printf("[CMD_RECV] %s\n", cmd);

    if (strcmp(cmd, "CMD_ARM_BROM") == 0) {
        setStatusColor(CRGB::Blue);
        MtkBromBridge::armHandshake();
        NetworkBridge::broadcastTelemetry(1, "BROM Armed! Waiting for phone plug-in (Hold Vol+ / Vol-)...", 10, ESP.getFreePsram(), ESP.getFreeHeap());
        updateOledDisplay();
    } 
    else if (strcmp(cmd, "CMD_TRIGGER_GLITCH") == 0) {
        setStatusColor(CRGB::Purple);
        uint32_t delayUs = doc["delay_us"] | glitchDelayUs;
        uint32_t widthNs = doc["width_ns"] | glitchWidthNs;
        
        GlitchEngine::triggerGlitch(delayUs, widthNs);
        
        NetworkBridge::broadcastTelemetry(2, "⚡ Hardware Glitch Pulse Fired on GPIO 4!", 25, ESP.getFreePsram(), ESP.getFreeHeap());
        vTaskDelay(pdMS_TO_TICKS(50));
        
        // Follow up with BROM Handshake
        bool ok = MtkBromBridge::performBromHandshake(3000);
        if (ok) {
            setStatusColor(CRGB::Green);
            NetworkBridge::broadcastTelemetry(3, "✅ BROM Handshake OK! Secure Boot SLA/DAA Bypassed!", 50, ESP.getFreePsram(), ESP.getFreeHeap());
        } else {
            setStatusColor(CRGB::Red);
            NetworkBridge::broadcastTelemetry(4, "⚠️ Handshake Timeout, adjust glitch delay in settings.", 0, ESP.getFreePsram(), ESP.getFreeHeap());
        }
        updateOledDisplay();
    }
    else if (strcmp(cmd, "CMD_SET_BAUD") == 0) {
        uint32_t baud = doc["baud"] | 921600;
        activeBaud = baud;
        MtkBromBridge::setBaudRate(baud);
        NetworkBridge::broadcastTelemetry(0, "UART Baud Rate updated.", 0, ESP.getFreePsram(), ESP.getFreeHeap());
        updateOledDisplay();
    }
    else if (strcmp(cmd, "CMD_SET_GLITCH_PARAMS") == 0) {
        glitchDelayUs = doc["delay_us"] | glitchDelayUs;
        glitchWidthNs = doc["width_ns"] | glitchWidthNs;
        NetworkBridge::broadcastTelemetry(0, "Glitch timings calibrated.", 0, ESP.getFreePsram(), ESP.getFreeHeap());
        updateOledDisplay();
    }
    else if (strcmp(cmd, "CMD_PING") == 0) {
        NetworkBridge::broadcastTelemetry(0, "PONG: ESP32-S3 N16R8 Coprocessor Active", 100, ESP.getFreePsram(), ESP.getFreeHeap());
    }
}

void setup() {
    // 1. Native USB CDC & Hardware UART
    Serial.begin(115200);
    delay(500);
    Serial.println("\n=============================================");
    Serial.println(" MTK SERVICE WORKSHOP PRO COPROCESSOR ");
    Serial.println(" Hardware: ESP32-S3-DevKitC-1-N16R8");
    Serial.printf(" Firmware: %s\n", FIRMWARE_VERSION);
    Serial.println("=============================================");

    // 2. Validate Octal PSRAM (8MB) & 16MB Flash
    log_i("ESP32-S3 Chip Model: %s (Rev %d, Cores: %d)", ESP.getChipModel(), ESP.getChipRevision(), ESP.getChipCores());
    log_i("Flash Size: %d MB (Mode: QIO @ %d MHz)", ESP.getFlashChipSize() / (1024 * 1024), ESP.getFlashChipSpeed() / 1000000);
    
    if (psramInit()) {
        log_i("✅ 8MB Octal PSRAM Detected! Total: %d bytes, Free: %d bytes", ESP.getPsramSize(), ESP.getFreePsram());
        psramStreamBuffer = (uint8_t*)ps_malloc(STREAM_BUFFER_SIZE);
        if (psramStreamBuffer != nullptr) {
            log_i("✅ 512KB High-Speed DMA Stream Buffer allocated in PSRAM");
        }
    } else {
        log_w("⚠️ PSRAM not detected, using internal SRAM fallback.");
    }

    // 3. WS2812 RGB Status LED
    FastLED.addLeds<WS2812, PIN_STATUS_RGB, GRB>(leds, NUM_LEDS);
    FastLED.setBrightness(40);
    setStatusColor(CRGB::Yellow);

    // 4. I2C SSD1306 OLED Display
    Wire.begin(PIN_I2C_SDA, PIN_I2C_SCL);
    if (display.begin(SSD1306_SWITCHCAPVCC, SCREEN_ADDRESS)) {
        oledAvailable = true;
        display.clearDisplay();
        display.setTextSize(1);
        display.setTextColor(SSD1306_WHITE);
        display.setCursor(0, 10);
        display.println("MTK WORKSHOP S3");
        display.setCursor(0, 25);
        display.println("N16R8 INITIALIZED");
        display.display();
    }

    // 5. Initialize Hardware Glitch Engine & MTK BROM Bridge
    GlitchEngine::init(GPIO_NUM_4);
    MtkBromBridge::init(PIN_UART_RX, PIN_UART_TX, 115200);

    // 6. Network Bridge (WiFi SoftAP + TCP Socket Server)
    NetworkBridge::setCommandCallback(handleAndroidCommand);
    NetworkBridge::init("MTK_SERVICE_BRIDGE_S3", "12345678");

    setStatusColor(CRGB::Green);
    updateOledDisplay();
    Serial.println("🟢 All Coprocessor Subsystems Armed & Ready.");
}

void loop() {
    // Background Periodic Telemetry & Keepalive
    static uint32_t lastTick = 0;
    if (millis() - lastTick > 2000) {
        lastTick = millis();
        updateOledDisplay();
    }

    // Handle USB CDC Pass-through if connected via USB-OTG directly to phone
    if (Serial.available()) {
        char ch = Serial.read();
        // Forward serial commands to BROM UART if needed
    }

    vTaskDelay(pdMS_TO_TICKS(20));
}
