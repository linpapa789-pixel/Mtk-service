#pragma once

#include <Arduino.h>
#include <WiFi.h>
#include <ArduinoJson.h>
#include "esp_wifi.h"
#include "lwip/sockets.h"

static const char *TAG_NET = "NETWORK_BRIDGE";

#define AP_SSID_DEFAULT "MTK_SERVICE_BRIDGE_S3"
#define AP_PASS_DEFAULT "12345678"
#define TCP_PORT 8080

typedef void (*CommandCallback)(const char* cmd, JsonDocument& root);

class NetworkBridge {
public:
    static void init(const char* ssid = AP_SSID_DEFAULT, const char* pass = AP_PASS_DEFAULT) {
        WiFi.mode(WIFI_AP);
        WiFi.softAP(ssid, pass);
        WiFi.setSleep(false); // Disable power saving for low-latency streaming

        IPAddress ip = WiFi.softAPIP();
        ESP_LOGI(TAG_NET, "SoftAP Started! SSID: %s, IP: %s, TCP Port: %d", ssid, ip.toString().c_str(), TCP_PORT);

        // Start FreeRTOS TCP Server Task
        xTaskCreatePinnedToCore(tcpServerTask, "tcp_bridge_task", 8192, NULL, 5, NULL, 1);
    }

    static void setCommandCallback(CommandCallback cb) {
        s_cmdCallback = cb;
    }

    static void broadcastTelemetry(int cmdType, const char* msg, uint32_t progressPct, uint32_t freePsram, uint32_t freeHeap) {
        if (s_clientSock < 0) return;

        JsonDocument doc;
        doc["type"] = cmdType;
        doc["msg"] = msg;
        doc["pct"] = progressPct;
        doc["psram_free"] = freePsram;
        doc["heap_free"] = freeHeap;
        doc["timestamp"] = millis();

        char jsonBuf[512];
        serializeJson(doc, jsonBuf);
        strcat(jsonBuf, "\n");

        send(s_clientSock, jsonBuf, strlen(jsonBuf), 0);
    }

    static void broadcastRawHex(const uint8_t* data, size_t len) {
        if (s_clientSock < 0 || len == 0) return;
        send(s_clientSock, (const char*)data, len, 0);
    }

    static bool isClientConnected() {
        return s_clientSock >= 0;
    }

private:
    static int s_clientSock;
    static CommandCallback s_cmdCallback;

    static void tcpServerTask(void *pvParameters) {
        int serverSock = socket(AF_INET, SOCK_STREAM, IPPROTO_IP);
        if (serverSock < 0) {
            ESP_LOGE(TAG_NET, "Unable to create socket: errno %d", errno);
            vTaskDelete(NULL);
            return;
        }

        int opt = 1;
        setsockopt(serverSock, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));

        struct sockaddr_in serverAddr = {};
        serverAddr.sin_family = AF_INET;
        serverAddr.sin_addr.s_addr = htonl(INADDR_ANY);
        serverAddr.sin_port = htons(TCP_PORT);

        if (bind(serverSock, (struct sockaddr *)&serverAddr, sizeof(serverAddr)) != 0) {
            ESP_LOGE(TAG_NET, "Socket bind failed: errno %d", errno);
            close(serverSock);
            vTaskDelete(NULL);
            return;
        }

        listen(serverSock, 2);
        ESP_LOGI(TAG_NET, "Socket server listening on port %d...", TCP_PORT);

        while (true) {
            struct sockaddr_in clientAddr;
            socklen_t addrLen = sizeof(clientAddr);
            s_clientSock = accept(serverSock, (struct sockaddr *)&clientAddr, &addrLen);
            if (s_clientSock < 0) {
                vTaskDelay(pdMS_TO_TICKS(100));
                continue;
            }

            char clientIp[INET_ADDRSTRLEN];
            inet_ntop(AF_INET, &clientAddr.sin_addr, clientIp, sizeof(clientIp));
            ESP_LOGI(TAG_NET, "Android App Connected from %s", clientIp);

            broadcastTelemetry(0, "ESP32-S3 N16R8 Coprocessor Bridge Connected & Armed", 0, ESP.getFreePsram(), ESP.getFreeHeap());

            char rxBuffer[1024];
            while (true) {
                int received = recv(s_clientSock, rxBuffer, sizeof(rxBuffer) - 1, 0);
                if (received <= 0) {
                    ESP_LOGW(TAG_NET, "Android Client disconnected");
                    break;
                }
                rxBuffer[received] = '\0';

                // Process Incoming Command from Android
                if (s_cmdCallback != NULL) {
                    JsonDocument cmdDoc;
                    DeserializationError err = deserializeJson(cmdDoc, rxBuffer);
                    if (!err) {
                        const char* cmd = cmdDoc["cmd"] | "UNKNOWN";
                        s_cmdCallback(cmd, cmdDoc);
                    } else {
                        // Plain string command support
                        JsonDocument fallbackDoc;
                        fallbackDoc["raw"] = rxBuffer;
                        s_cmdCallback(rxBuffer, fallbackDoc);
                    }
                }
            }

            close(s_clientSock);
            s_clientSock = -1;
        }
    }
};

int NetworkBridge::s_clientSock = -1;
CommandCallback NetworkBridge::s_cmdCallback = NULL;
