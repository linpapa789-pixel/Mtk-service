# ESP32-S3 N16R8 Hardware Coprocessor & Firmware for MTK Workshop Pro

## 🇲🇲 မြန်မာဘာသာ ရှင်းလင်းချက် (Burmese Guide)

ဤ Firmware သည် **ESP32-S3 N16R8 (16MB Flash, 8MB Octal PSRAM)** ဘုတ်များအတွက် အထူးပြုလုပ်ထားသော MediaTek BROM / Preloader Exploit & High-Speed Passthrough Bridge ဖြစ်ပါသည်။

### အဓိက စွမ်းဆောင်ရည်များ (Key Features)
1. **Octal 8MB PSRAM & 16MB Flash အပြည့်အဝ ထောက်ပံ့ခြင်း**: ကြီးမားသော Dump / Flash image များကို 512KB DMA buffer ဖြင့် ကြားခံထားရှိပြီး အရှိန်မြင့်စွာ ဖတ်/ရေးနိုင်ခြင်း။
2. **MediaTek BROM SLA/DAA Hardware Glitch Engine**: GPIO 4 မှ တိကျသော Nanosecond / Microsecond Glitch Pulse ထုတ်လွှတ်ပြီး Preloader Dead / BROM Lock များကို Bypass လုပ်နိုင်ခြင်း။
3. **Dual Connection Mode**:
   - **Direct USB-OTG**: Android ဖုန်းသို့ Type-C ကြိုးဖြင့် တိုက်ရိုက်ချိတ်ဆက်ခြင်း။
   - **WiFi SoftAP**: `MTK_SERVICE_BRIDGE_S3` (Password: `12345678`, IP: `192.168.4.1:8080`) သို့ ကြိုးမဲ့ ချိတ်ဆက်ပြီး BROM / Fastboot commands များကို အမိန့်ပေးစေခိုင်းခြင်း။
4. **OLED Display (SSD1306) & WS2812 RGB LED**: လက်ရှိ HW Code, RAM/Flash ပမာဏ, Glitch အခြေအနေနှင့် ချိတ်ဆက်မှု အခြေအနေများကို အချိန်နဲ့တပြေးညီ ပြသပေးခြင်း။

---

### ESP32-S3 Dual Type-C Ports ရှင်းလင်းချက် (COM vs USB)

ESP32-S3 DevKitC-1 ဘုတ်ပေါ်တွင် Type-C ပေါက် (၂) ခု ပါဝင်ပါသည်:

1. **"COM" Port (USB-to-UART / CP2102 / CH343)**:
   - **လုပ်ဆောင်ချက်**: ESP32 သို့ Firmware Flash ရန် (Android APK Flasher / PC esptool.py) နှင့် Serial Logs ကြည့်ရှုရန်။
   - **ချိတ်ဆက်ပုံ**: Android Master ဖုန်း (သို့မဟုတ် PC) သို့ Type-C OTG ကြိုးဖြင့် ချိတ်ပါ။

2. **"USB" Port (Native USB-OTG / GPIO 19 D- & GPIO 20 D+)**:
   - **လုပ်ဆောင်ချက်**: Target Phone ကို USB Host ဖြင့် ချိတ်ဆက်ရန် သို့မဟုတ် 12Mbps Native CDC Pass-through အသုံးပြုရန်။
   - **ချိတ်ဆက်ပုံ**: ပြုပြင်မည့် Target Phone သို့ Type-C / MicroUSB ကြိုးဖြင့် ချိတ်ပါ။

---

### Hardware Pinout & Wiring Schematic

```text
=======================================================================
ESP32-S3-DevKitC-1 (N16R8)       <=====>      TARGET PHONE / MOTHERBOARD
=======================================================================
GPIO 19 (UART RX / USB D-)       ----->       Target Phone USB D- (White Wire)
GPIO 20 (UART TX / USB D+)       ----->       Target Phone USB D+ (Green Wire)
GPIO 4  (Glitch Pulse Out)       ----->       CLK Testpoint or VBUS Switch MOSFET
GPIO 48 (WS2812 RGB Status LED)  ----->       On-board RGB Indicator
GPIO 8  (I2C SDA)                ----->       OLED SDA (Pin 3)
GPIO 9  (I2C SCL)                ----->       OLED SCL (Pin 4)
GND (Common Ground)              <=====>      Target Phone GND + 5V Adapter GND
5V External Power Adapter        ----->       Target Phone VBUS (Red Wire)
=======================================================================
⚠️ သတိပြုရန်: Target Phone ကို ESP32 ၏ 3.3V pin မှ ပါဝါမကျွေးရပါ။ External 5V 2A~3A adapter သို့မဟုတ် Battery ဖြင့် ပါဝါပေးပါ။ GND အားလုံးကို အတူတူ ပူးဆက်ပေးပါ။
```

---

## 🚀 GitHub Actions CI/CD Build Setup

GitHub သို့ push တင်လိုက်ရုံဖြင့် `.github/workflows/build.yml` အလိုအလျောက် အလုပ်လုပ်ပြီး:
1. **Android APK** (`app-release.apk`, `app-debug.apk`)
2. **ESP32-S3 N16R8 Firmware** (`esp32s3_n16r8_firmware_merged.bin` at offset `0x00000000`)
နှစ်မျိုးလုံးကို Build လုပ်ပေးပြီး **Artifacts** နှင့် **GitHub Releases** တွင် ဒေါင်းလုဒ်ရယူနိုင်ပါသည်။

### Manual Local Build (PlatformIO):
```bash
cd firmware
pio run -e esp32s3_n16r8
```

### 1-Click ESP32 Flashing via esptool.py:
```bash
esptool.py --chip esp32s3 -p /dev/ttyACM0 -b 921600 write_flash 0x00000 build_output/esp32s3_n16r8_firmware_merged.bin
```
(သို့မဟုတ် Android APK ထဲရှိ **ESP32-S3 Flasher** မျက်နှာပြင်မှ 1-Click Flash ပြုလုပ်နိုင်ပါသည်)
