package com.example.data

object Esp32FirmwareTemplate {

    const val FIRMWARE_VERSION = "v2.5.0-N16R8"
    const val TARGET_HARDWARE = "ESP32-S3 DevKitC-1 N16R8 (16MB Flash, 8MB Octal PSRAM)"

    const val BURMESE_SUMMARY = """
🌟 ESP32-S3 N16R8 အတွက် အထူးထုတ်လုပ်ထားသော Firmware နှင့် Android APK ချိတ်ဆက်မှုစနစ်:
1. 8MB Octal PSRAM + 16MB Flash အပြည့်အဝအသုံးပြုပြီး 512KB High-Speed DMA Buffer ဖြင့် ဒေတာစီးဆင်းမှု မြန်ဆန်စေခြင်း။
2. GPIO 4 မှ Microsecond / Nanosecond Glitch Pulse ထုတ်လွှတ်ပြီး MediaTek BROM / Preloader SLA / DAA ကို Bypass လုပ်နိုင်ခြင်း။
3. WiFi SoftAP ("MTK_SERVICE_BRIDGE_S3") နှင့် USB CDC Serial Pass-through စနစ်နှစ်မျိုးလုံး အထောက်အပံ့ပေးထားခြင်း။
4. GitHub Actions CI/CD (.github/workflows/build.yml) ဖြင့် Android APK နှင့် ESP32-S3 Firmware နှစ်မျိုးလုံးကို အလိုအလျောက် Compile လုပ်ပေးခြင်း။
"""

    const val DUAL_PORT_GUIDE = """
=== ESP32-S3 DUAL TYPE-C PORTS GUIDE (COM vs USB) ===

1. "COM" Port (USB-to-UART / CP2102 / CH343):
   • ဘယ်အတွက်သုံးလဲ: Firmware Flash လုပ်ရန် (APK Flasher / PC esptool.py) နှင့် Serial Debug Monitor ကြည့်ရန်။
   • ချိတ်ဆက်နည်း: Android ဖုန်း (သို့မဟုတ် PC) သို့ Type-C OTG ကြိုးဖြင့် ချိတ်ပါ။

2. "USB" Port (Native USB-OTG / GPIO 19 D- & GPIO 20 D+):
   • ဘယ်အတွက်သုံးလဲ: Target Phone ကို USB Host ဖြင့် ချိတ်ဆက်ရန် (သို့မဟုတ်) High-Speed 12Mbps Native CDC Pass-through အတွက်။
   • ချိတ်ဆက်နည်း: Target ဖုန်း၏ Type-C/MicroUSB ကြိုးဖြင့် ချိတ်ဆက်ပါ။
"""

    const val WIRING_GUIDE = """
=== ESP32-S3 N16R8 HARDWARE PINOUT & DUAL TYPE-C GUIDE ===

[1] TYPE-C PORTS USAGE:
• Port 1: "COM" (UART)  -----> Android Master Phone / PC (For Flashing Firmware & Serial Logs)
• Port 2: "USB" (OTG)   -----> Target Phone (Native USB Host / CDC Pass-through)

[2] HARDWARE GPIO PINOUT:
• GPIO 19 (UART1 RX / USB D-) -----> Target Phone USB D- (White Wire)
• GPIO 20 (UART1 TX / USB D+) -----> Target Phone USB D+ (Green Wire)
• GPIO 4  (Glitch Pulse Out)  -----> Target Motherboard CLK / VBUS MOSFET Gate
• GPIO 48 (WS2812 RGB LED)    -----> On-board Status Visualizer
• GPIO 8  (I2C SDA)           -----> SSD1306 OLED SDA
• GPIO 9  (I2C SCL)           -----> SSD1306 OLED SCL
• GND (Common Ground)         -----> Target Phone GND + 5V Adapter GND (MANDATORY)
• EXT 5V 2A~3A Adapter        -----> Target Phone VBUS (Red Wire) - Do NOT use 3.3V!
"""

    const val PLATFORMIO_INI = """; =================================================================
; MTK Service Coprocessor & BROM Bridge
; Hardware: ESP32-S3-DevKitC-1-N16R8 (16MB Flash, 8MB Octal PSRAM)
; =================================================================

[platformio]
default_envs = esp32s3_n16r8
src_dir = src

[env:esp32s3_n16r8]
platform = espressif32 @ 6.6.0
board = esp32-s3-devkitc-1
framework = arduino

; CPU & Memory Architecture for N16R8
board_build.mcu = esp32s3
board_build.f_cpu = 240000000L
board_build.f_flash = 80000000L
board_build.flash_mode = qio
board_build.flash_size = 16MB
board_build.partitions = partitions_16mb.csv
board_build.arduino.memory_type = qio_opi

build_flags = 
    -DCORE_DEBUG_LEVEL=3
    -DBOARD_HAS_PSRAM
    -DCONFIG_SPIRAM_MODE_OCT=1
    -DCONFIG_SPIRAM_SPEED_80M=1
    -DCONFIG_SPIRAM_USE_MALLOC=1
    -DARDUINO_USB_CDC_ON_BOOT=1
    -DARDUINO_USB_MODE=1
    -DMTK_FIRMWARE_VERSION=\"2.5.0-N16R8\"
    -DGLITCH_GPIO_PIN=4
    -DSTATUS_RGB_PIN=48

lib_deps =
    bblanchon/ArduinoJson @ ^7.0.4
    adafruit/Adafruit SSD1306 @ ^2.5.10
    adafruit/Adafruit GFX Library @ ^1.11.9
    fastled/FastLED @ ^3.6.0
    links2004/WebSockets @ ^2.4.1

monitor_speed = 115200
upload_speed = 921600
"""

    const val PARTITIONS_CSV = """# Name,   Type, SubType, Offset,  Size, Flags
nvs,      data, nvs,     0x9000,  0x5000,
otadata,  data, ota,     0xe000,  0x2000,
app0,     app,  ota_0,   0x10000, 0x480000,
app1,     app,  ota_1,   0x490000,0x480000,
spiffs,   data, spiffs,  0x910000,0x6E0000,
coredump, data, coredump,0xFF0000,0x10000,
"""

    const val GITHUB_WORKFLOW_YML = """name: Build Android APK and ESP32-S3 N16R8 Firmware

on:
  push:
    branches: [ "main", "master" ]
    tags: [ "v*" ]
  pull_request:
    branches: [ "main", "master" ]
  workflow_dispatch:

permissions:
  contents: write

jobs:
  build-android-apk:
    name: 📱 Build Android APK
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle
      - run: chmod +x gradlew || true
      - run: cp .env.example .env || true
      - run: ./gradlew assembleDebug --stacktrace --no-daemon
      - uses: actions/upload-artifact@v4
        with:
          name: MTK-ESP32-Workshop-APKs
          path: app/build/outputs/apk/**/*.apk

  build-esp32s3-firmware:
    name: ⚡ Build ESP32-S3 N16R8 Firmware
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-python@v5
        with:
          python-version: '3.10'
      - run: pip install platformio esptool
      - working-directory: ./firmware
        run: pio run -e esp32s3_n16r8
      - working-directory: ./firmware
        run: |
          mkdir -p build_output
          cp .pio/build/esp32s3_n16r8/bootloader.bin build_output/
          cp .pio/build/esp32s3_n16r8/partitions.bin build_output/
          cp .pio/build/esp32s3_n16r8/firmware.bin build_output/
          python -c "with open('build_output/boot_app0.bin', 'wb') as f: f.write(b'\xff' * 0x2000)"
          esptool.py --chip esp32s3 merge_bin \
            -o build_output/esp32s3_n16r8_firmware_merged.bin \
            --flash_mode qio --flash_freq 80m --flash_size 16MB \
            0x0000 build_output/bootloader.bin \
            0x8000 build_output/partitions.bin \
            0xe000 build_output/boot_app0.bin \
            0x10000 build_output/firmware.bin
      - uses: actions/upload-artifact@v4
        with:
          name: ESP32-S3-N16R8-Firmware-Binaries
          path: firmware/build_output/*.bin
"""

    const val ESP32_C_CODE = """/*
 * ==============================================================================
 * MTK BROM Service & Glitch Coprocessor Firmware
 * Target: ESP32-S3-DevKitC-1-N16R8 (16MB Flash, 8MB Octal PSRAM)
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
#define PIN_GLITCH       4
#define PIN_STATUS_RGB   48
#define PIN_I2C_SDA      8
#define PIN_I2C_SCL      9
#define PIN_UART_RX      19
#define PIN_UART_TX      20

CRGB leds[1];
Adafruit_SSD1306 display(128, 64, &Wire, -1);
bool oledAvailable = false;
uint8_t* psramStreamBuffer = nullptr;

void updateOledDisplay() {
    if (!oledAvailable) return;
    display.clearDisplay();
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);
    display.setCursor(0, 0);
    display.println("MTK WORKSHOP S3");
    display.drawLine(0, 10, 127, 10, SSD1306_WHITE);
    display.setCursor(0, 14);
    display.printf("PSRAM: %dMB / 8MB\n", (int)(ESP.getFreePsram() / (1024 * 1024)));
    display.setCursor(0, 24);
    display.printf("Mode: %s\n", NetworkBridge::isClientConnected() ? "LINKED (WiFi)" : "ARMED/IDLE");
    display.setCursor(0, 34);
    display.printf("BROM: %s\n", MtkBromBridge::getDeviceInfo().chipsetName);
    display.setCursor(0, 48);
    display.printf("Ver: %s", FIRMWARE_VERSION);
    display.display();
}

void setup() {
    Serial.begin(115200);
    if (psramInit()) {
        psramStreamBuffer = (uint8_t*)ps_malloc(512 * 1024);
    }
    FastLED.addLeds<WS2812, PIN_STATUS_RGB, GRB>(leds, 1);
    leds[0] = CRGB::Green;
    FastLED.show();

    Wire.begin(PIN_I2C_SDA, PIN_I2C_SCL);
    if (display.begin(SSD1306_SWITCHCAPVCC, 0x3C)) {
        oledAvailable = true;
        updateOledDisplay();
    }

    GlitchEngine::init(GPIO_NUM_4);
    MtkBromBridge::init(PIN_UART_RX, PIN_UART_TX, 115200);
    NetworkBridge::init("MTK_SERVICE_BRIDGE_S3", "12345678");
}

void loop() {
    vTaskDelay(pdMS_TO_TICKS(50));
}
"""
}
