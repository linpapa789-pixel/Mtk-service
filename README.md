# MTK & ESP32 Workshop Pro

**MTK & ESP32 Workshop Pro** is a mobile phone software servicing toolkit and ESP32-S3 hardware coprocessor bridge for MediaTek (MTK) BROM/Preloader exploits, Bootloader Unlocking, NVRAM/IMEI repair, and partition flashing.

---

## 🚀 Features

- **MediaTek BROM Service Suite**:
  - 1-Click Instant Bootloader Unlock (`seccfg` patch)
  - Xiaomi Mi Account & Cloud Lock Reset
  - FRP / Google Account Wipe
  - Userdata Wipe / Screen Lock removal
  - NVRAM / NVDATA Backup, Restore, and Dual-IMEI Repair
  - eMMC / UFS Life-Time & Health Inspection (`EXT_CSD`)
  - Custom Download Agent (DA) & Preloader Selector

- **ESP32-S3 N16R8 Coprocessor Bridge (`firmware/`)**:
  - 16MB Flash (QIO 80MHz) & 8MB Octal PSRAM (OPI)
  - Microsecond / Nanosecond Hardware Glitch Pulse Generator (GPIO 4) for SLA/DAA Security Bypass
  - Dual Bridge: WiFi SoftAP (`192.168.4.1:8080`) & Native USB-OTG CDC
  - Live SSD1306 OLED Telemetry & WS2812 RGB Status LED

- **Automated CI/CD Workflow (`.github/workflows/build.yml`)**:
  - Automatically compiles Android APK (`release` and `debug`)
  - Automatically compiles ESP32-S3 Firmware via PlatformIO
  - Merges binary into `esp32s3_n16r8_firmware_merged.bin` at offset `0x00000000`

---

## 🛠️ How to Build

### 1. Android APK
```bash
./gradlew assembleRelease
```

### 2. ESP32-S3 Firmware
```bash
cd firmware
pio run
```

---

## 📄 License
Open source under Apache License 2.0.
