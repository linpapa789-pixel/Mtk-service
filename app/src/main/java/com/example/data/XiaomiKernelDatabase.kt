package com.example.data

data class XiaomiDeviceEntry(
    val branch: String,
    val devices: String,
    val androidVersion: String,
    val baseTag: String,
    val link: String,
    val soc: String,
    val isMtk: Boolean,
    val testPointGuide: String,
    val nvramStartHex: String = "0x1800000",
    val frpStartHex: String = "0x5580000"
)

object XiaomiKernelDatabase {

    val deviceList: List<XiaomiDeviceEntry> = listOf(
        // MediaTek Devices
        XiaomiDeviceEntry(
            branch = "dandelion-q-oss",
            devices = "Redmi 9A, Redmi 9C, POCO C3",
            androidVersion = "Android Q (10)",
            baseTag = "MTK alps-mp-q0.mp1",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/dandelion-q-oss",
            soc = "MT6765 (Helio G25 / G35)",
            isMtk = true,
            testPointGuide = "Short TP (CLK pin near eMMC shield) to GND with tweezers while inserting USB cable.",
            nvramStartHex = "0x1800000",
            frpStartHex = "0x5580000"
        ),
        XiaomiDeviceEntry(
            branch = "begonia-r-oss",
            devices = "Redmi Note 8 Pro",
            androidVersion = "Android R (11)",
            baseTag = "MTK alps-mp-r0.mp1",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/begonia-r-oss",
            soc = "MT6785 (Helio G90T)",
            isMtk = true,
            testPointGuide = "CLK Test Point located directly under motherboard camera bracket -> Short to GND.",
            nvramStartHex = "0x2800000",
            frpStartHex = "0x8B00000"
        ),
        XiaomiDeviceEntry(
            branch = "cactus-p-oss",
            devices = "Redmi 6A, Redmi 6",
            androidVersion = "Android P (9)",
            baseTag = "MTK alps-mp-p0.mp1",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/cactus-p-oss",
            soc = "MT6761 / MT6762 (Helio A22 / P22)",
            isMtk = true,
            testPointGuide = "Short KCOL0 test point pad next to battery connector to GND.",
            nvramStartHex = "0x1600000",
            frpStartHex = "0x4C00000"
        ),
        XiaomiDeviceEntry(
            branch = "earth-s-oss",
            devices = "Redmi 12C, POCO C55",
            androidVersion = "Android S (12)",
            baseTag = "alps-mp-s0.mp1.tc8sp2-cs1-V1.3",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/earth-s-oss",
            soc = "MT6769 (Helio G85)",
            isMtk = true,
            testPointGuide = "Short TP pad adjacent to PMIC MT6358 to motherboard shield GND.",
            nvramStartHex = "0x1A00000",
            frpStartHex = "0x6A00000"
        ),
        XiaomiDeviceEntry(
            branch = "fire-t-oss",
            devices = "Redmi 12 (4G)",
            androidVersion = "Android T (13)",
            baseTag = "alps-mp-s0.mp1.tc8sp2-cs1-xm.V1.0.13_P53",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/fire-t-oss",
            soc = "MT6769 (Helio G88)",
            isMtk = true,
            testPointGuide = "Hold Vol(+) and Vol(-) while plugging USB, or short CLK pin on back of PCB.",
            nvramStartHex = "0x1A00000",
            frpStartHex = "0x6A00000"
        ),
        XiaomiDeviceEntry(
            branch = "fleur-s-oss",
            devices = "Redmi Note 11S, POCO M4 Pro (4G)",
            androidVersion = "Android S (12)",
            baseTag = "alps-mp-r0.mp8.tc8sp3-mt6781-V1.5.1",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/fleur-s-oss",
            soc = "MT6781 (Helio G96)",
            isMtk = true,
            testPointGuide = "Test Point located near fingerprint ribbon cable connector on motherboard.",
            nvramStartHex = "0x2C00000",
            frpStartHex = "0x8E00000"
        ),
        XiaomiDeviceEntry(
            branch = "camellian-t-oss",
            devices = "POCO M3 Pro 5G, Redmi Note 10 5G, Redmi Note 10T",
            androidVersion = "Android T (13)",
            baseTag = "alps-mp-s0.mp1.tc8sp-mt6785",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/camellian-t-oss",
            soc = "MT6833 (Dimensity 700 5G)",
            isMtk = true,
            testPointGuide = "V6 Protocol Test Point - Connect D+ / TP pad to ground before inserting VBUS cable.",
            nvramStartHex = "0x3400000",
            frpStartHex = "0x9800000"
        ),
        XiaomiDeviceEntry(
            branch = "chopin-r-oss",
            devices = "Redmi Note 10 Pro (China / 5G)",
            androidVersion = "Android R (11)",
            baseTag = "MTK alps-mp-r0.mp1",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/chopin-r-oss",
            soc = "MT6891 (Dimensity 1100 5G)",
            isMtk = true,
            testPointGuide = "Short TP pad near camera bracket while connecting USB.",
            nvramStartHex = "0x3600000",
            frpStartHex = "0xA000000"
        ),
        XiaomiDeviceEntry(
            branch = "rubens-s-oss",
            devices = "Redmi K50",
            androidVersion = "Android S (12)",
            baseTag = "alps-release-s0.mp1.tc8sp2-mt6983",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/rubens-s-oss",
            soc = "MT6983 (Dimensity 8100 5G)",
            isMtk = true,
            testPointGuide = "High-Speed UFS TP pad located near memory shielding.",
            nvramStartHex = "0x4000000",
            frpStartHex = "0xB000000"
        ),
        XiaomiDeviceEntry(
            branch = "xaga-s-oss",
            devices = "Redmi Note 11T Pro, POCO X4 GT",
            androidVersion = "Android S (12)",
            baseTag = "alps-release-s0.mp1.tc8sp2-mt6983",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/xaga-s-oss",
            soc = "MT6983 (Dimensity 8100)",
            isMtk = true,
            testPointGuide = "Hold Vol(-) and plug high-speed Type-C cable, or use BROM TP.",
            nvramStartHex = "0x4000000",
            frpStartHex = "0xB000000"
        ),
        XiaomiDeviceEntry(
            branch = "bsp-plato-s-oss",
            devices = "Xiaomi 12T",
            androidVersion = "Android S (12)",
            baseTag = "t-alps-release-s0.mp1.tc8sp2-mt6983-V1.0.1",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/bsp-plato-s-oss",
            soc = "MT6983 (Dimensity 8100-Ultra)",
            isMtk = true,
            testPointGuide = "Short TP pad to ground or use Auth Bypass DA.",
            nvramStartHex = "0x4000000",
            frpStartHex = "0xB000000"
        ),
        XiaomiDeviceEntry(
            branch = "corot-s-oss",
            devices = "Redmi K60 Ultra, Xiaomi 13T Pro",
            androidVersion = "Android T (13)",
            baseTag = "t-alps-release-t0.mp1.tc8sp2-V1.14",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/corot-t-oss",
            soc = "MT6989 (Dimensity 9200+ 5G)",
            isMtk = true,
            testPointGuide = "V6 DA Stage 2 required for High-Security Preloader Handshake.",
            nvramStartHex = "0x4500000",
            frpStartHex = "0xC000000"
        ),
        XiaomiDeviceEntry(
            branch = "zircon-t-oss",
            devices = "Redmi Note 13 Pro+ (5G)",
            androidVersion = "Android T (13)",
            baseTag = "t-alps-release-t0.mp1.tc8sp2-V1.14",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/zircon-t-oss",
            soc = "MT6886 (Dimensity 7200-Ultra)",
            isMtk = true,
            testPointGuide = "Hold Vol(-) and Vol(+) with charged battery, or short TP CLK line.",
            nvramStartHex = "0x3800000",
            frpStartHex = "0xA500000"
        ),
        XiaomiDeviceEntry(
            branch = "gold-s-oss",
            devices = "Redmi Note 13 5G",
            androidVersion = "Android T (13)",
            baseTag = "t-alps-release-s0.mp1.tc8sp-cs2-V1",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/gold-s-oss",
            soc = "MT6833P (Dimensity 6080 5G)",
            isMtk = true,
            testPointGuide = "Short TP pad to GND while inserting Type-C cable.",
            nvramStartHex = "0x3400000",
            frpStartHex = "0x9800000"
        ),
        XiaomiDeviceEntry(
            branch = "blue-u-oss",
            devices = "POCO C61, Redmi A3",
            androidVersion = "Android U (14)",
            baseTag = "alps-mp-s0.mp1.tc8sp-cs3-V1_xiaomi",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/blue-u-oss",
            soc = "MT6765X (Helio G36)",
            isMtk = true,
            testPointGuide = "Short CLK TP on top mother board section to shield ground.",
            nvramStartHex = "0x1800000",
            frpStartHex = "0x5580000"
        ),
        XiaomiDeviceEntry(
            branch = "gale-s-oss",
            devices = "Redmi 13C, POCO C65",
            androidVersion = "Android T (13)",
            baseTag = "t-alps-release-s0.mp1.tc8sp-cs2-V1.31",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/gale-s-oss",
            soc = "MT6769Z (Helio G85)",
            isMtk = true,
            testPointGuide = "Hold Vol(-) and Vol(+) buttons simultaneously and connect USB.",
            nvramStartHex = "0x1A00000",
            frpStartHex = "0x6A00000"
        ),
        XiaomiDeviceEntry(
            branch = "rock-u-oss",
            devices = "POCO M5, Redmi 11 Prime (4G)",
            androidVersion = "Android U (14)",
            baseTag = "alps-mp-s0.mp1.tc8sp2-cs1-xm.V1.0.13",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/rock-u-oss",
            soc = "MT6789 (Helio G99)",
            isMtk = true,
            testPointGuide = "Short TP pinout next to battery connector to shield GND.",
            nvramStartHex = "0x2C00000",
            frpStartHex = "0x8E00000"
        ),
        XiaomiDeviceEntry(
            branch = "agate-u-oss",
            devices = "Xiaomi 11T",
            androidVersion = "Android U (14)",
            baseTag = "alps-mp-s0.mp1.tc8sp-cs2-V1.14",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/agate-u-oss",
            soc = "MT6893 (Dimensity 1200-Ultra)",
            isMtk = true,
            testPointGuide = "Short CLK TP under mainboard heatsink shield to GND.",
            nvramStartHex = "0x3600000",
            frpStartHex = "0xA000000"
        ),
        XiaomiDeviceEntry(
            branch = "air-t-oss",
            devices = "Redmi 13R 5G, Redmi 13C 5G",
            androidVersion = "Android T (13)",
            baseTag = "alps-mp-t0.mp1.tc8sp2-V1.32",
            link = "https://github.com/MiCode/MTK_kernel_modules/tree/air-t-oss",
            soc = "MT6835 (Dimensity 6100+ 5G)",
            isMtk = true,
            testPointGuide = "Hold Vol(-) and plug Type-C cable into ESP32 Host.",
            nvramStartHex = "0x3400000",
            frpStartHex = "0x9800000"
        ),
        XiaomiDeviceEntry(
            branch = "ares-r-oss",
            devices = "Redmi K40 Gaming, POCO F3 GT",
            androidVersion = "Android R (11)",
            baseTag = "MTK alps-mp-r0.mp1",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/ares-r-oss",
            soc = "MT6893 (Dimensity 1200)",
            isMtk = true,
            testPointGuide = "Short TP pad near shoulder trigger ribbons to ground.",
            nvramStartHex = "0x3600000",
            frpStartHex = "0xA000000"
        ),
        XiaomiDeviceEntry(
            branch = "cannon-r-oss",
            devices = "Redmi Note 9 5G, Redmi 9T",
            androidVersion = "Android R (11)",
            baseTag = "MTK alps-mp-r0.mp1",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/cannon-r-oss",
            soc = "MT6853 (Dimensity 800U)",
            isMtk = true,
            testPointGuide = "Short TP testpoint near PMIC to GND.",
            nvramStartHex = "0x3200000",
            frpStartHex = "0x9200000"
        ),
        XiaomiDeviceEntry(
            branch = "lancelot-q-oss",
            devices = "Redmi 9, Redmi 10X 4G, POCO M2",
            androidVersion = "Android Q (10)",
            baseTag = "MTK alps-mp-q0.mp1",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/lancelot-q-oss",
            soc = "MT6769V (Helio G80)",
            isMtk = true,
            testPointGuide = "Short KCOL0 test point pad next to PMIC MT6358 to ground.",
            nvramStartHex = "0x1A00000",
            frpStartHex = "0x6A00000"
        ),
        XiaomiDeviceEntry(
            branch = "merlin-r-oss",
            devices = "Redmi Note 9 (4G), Redmi 10X 4G",
            androidVersion = "Android R (11)",
            baseTag = "MTK alps-mp-r0.mp1",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/merlin-r-oss",
            soc = "MT6769Z (Helio G85)",
            isMtk = true,
            testPointGuide = "Hold Vol(+) + Vol(-) or short TP CLK line to ground.",
            nvramStartHex = "0x1A00000",
            frpStartHex = "0x6A00000"
        ),
        XiaomiDeviceEntry(
            branch = "pissarro-r-oss",
            devices = "Redmi Note 11 Pro+ 5G, Xiaomi 11i 5G",
            androidVersion = "Android R (11)",
            baseTag = "MTK alps-mp-r0.mp1",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/pissarro-r-oss",
            soc = "MT6877 (Dimensity 920 5G)",
            isMtk = true,
            testPointGuide = "V6 Protocol Test Point - Connect D+ / TP pad to ground before cable plug.",
            nvramStartHex = "0x3800000",
            frpStartHex = "0xA000000"
        ),
        XiaomiDeviceEntry(
            branch = "rosemary-r-oss",
            devices = "Redmi Note 10S, POCO M5s",
            androidVersion = "Android R (11)",
            baseTag = "MTK alps-mp-r0.mp1",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/rosemary-r-oss",
            soc = "MT6785V (Helio G95)",
            isMtk = true,
            testPointGuide = "Short TP pad near camera module to GND.",
            nvramStartHex = "0x2800000",
            frpStartHex = "0x8B00000"
        ),
        XiaomiDeviceEntry(
            branch = "selene-r-oss",
            devices = "Redmi 10, Redmi 10 Prime",
            androidVersion = "Android R (11)",
            baseTag = "MTK alps-mp-r0.mp1",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/selene-r-oss",
            soc = "MT6769H (Helio G88)",
            isMtk = true,
            testPointGuide = "Hold Vol(+) and Vol(-) while connecting cable.",
            nvramStartHex = "0x1A00000",
            frpStartHex = "0x6A00000"
        ),
        XiaomiDeviceEntry(
            branch = "pearl-s-oss",
            devices = "Redmi Note 12T Pro",
            androidVersion = "Android S (12)",
            baseTag = "alps-mp-s0.mp1.tc8sp2-mt6983",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/pearl-s-oss",
            soc = "MT6895 (Dimensity 8200-Ultra)",
            isMtk = true,
            testPointGuide = "UFS High-Speed BROM Test Point required for preloader bypass.",
            nvramStartHex = "0x4000000",
            frpStartHex = "0xB000000"
        ),
        XiaomiDeviceEntry(
            branch = "bsp-duchamp-u-oss",
            devices = "Redmi K70E, POCO X6 Pro 5G",
            androidVersion = "Android U (14)",
            baseTag = "t-alps-release-u0.mp1.tc8sp1-V1",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/bsp-duchamp-u-oss",
            soc = "MT6897 (Dimensity 8300-Ultra)",
            isMtk = true,
            testPointGuide = "V6 High-Speed Protocol - DA V6 with SLA Crypto Signature.",
            nvramStartHex = "0x4200000",
            frpStartHex = "0xBA00000"
        ),
        XiaomiDeviceEntry(
            branch = "bsp-rothko-u-oss",
            devices = "Redmi K70 Ultra, Xiaomi 14T Pro",
            androidVersion = "Android U (14)",
            baseTag = "TAG:alps-mp-u0.mp1.tc8sp3-V1",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/bsp-rothko-u-oss",
            soc = "MT6989 (Dimensity 9300+ 5G)",
            isMtk = true,
            testPointGuide = "Direct High-Security BROM DA with ESP32 Host.",
            nvramStartHex = "0x4800000",
            frpStartHex = "0xC500000"
        ),
        XiaomiDeviceEntry(
            branch = "bsp-degas-u-oss",
            devices = "Xiaomi 14T",
            androidVersion = "Android U (14)",
            baseTag = "alps-mp-u0.mp1.tc8sp3-V1",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/bsp-degas-u-oss",
            soc = "MT6897 (Dimensity 8300-Ultra)",
            isMtk = true,
            testPointGuide = "Short TP near battery FPC to GND.",
            nvramStartHex = "0x4200000",
            frpStartHex = "0xBA00000"
        ),
        XiaomiDeviceEntry(
            branch = "malachite-u-oss",
            devices = "Redmi Note 14 Pro (China)",
            androidVersion = "Android U (14)",
            baseTag = "t-alps-release-u0.mp1.tc8sp3-V1",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/malachite-u-oss",
            soc = "MT6878 (Dimensity 7300-Ultra)",
            isMtk = true,
            testPointGuide = "V6 Protocol Test Point - Connect D+ to GND.",
            nvramStartHex = "0x3800000",
            frpStartHex = "0xA000000"
        ),
        XiaomiDeviceEntry(
            branch = "beryl-u-oss",
            devices = "POCO M7 Pro 5G, Redmi Note 14 5G",
            androidVersion = "Android U (14)",
            baseTag = "t-alps-release-s0.mp1.tc8sp-cs3-V1.67",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/beryl-u-oss",
            soc = "MT6835 (Dimensity 7025-Ultra)",
            isMtk = true,
            testPointGuide = "Hold Vol(-) and Vol(+) while plugging Type-C cable.",
            nvramStartHex = "0x3400000",
            frpStartHex = "0x9800000"
        ),

        // Snapdragon / Qualcomm Devices (Supported via Fastboot / ADB / EDL Suite)
        XiaomiDeviceEntry(
            branch = "alioth-r-oss",
            devices = "Mi 10S, Redmi K40, POCO F3",
            androidVersion = "Android R (11)",
            baseTag = "LA.UM.9.12.r1-08000-SMxx50.0",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/alioth-r-oss",
            soc = "Qualcomm Snapdragon 870 (SM8250-AC)",
            isMtk = false,
            testPointGuide = "Qualcomm EDL 9008 Test Point located next to wireless charging coil pads."
        ),
        XiaomiDeviceEntry(
            branch = "apollo-q-oss",
            devices = "Redmi K30S Ultra, Mi 10T, Mi 10T Pro",
            androidVersion = "Android Q (10)",
            baseTag = "LA.UM.8.12.r1-10600-sm8250.0",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/apollo-q-oss",
            soc = "Qualcomm Snapdragon 865 (SM8250)",
            isMtk = false,
            testPointGuide = "EDL 9008 TP pads under camera bracket."
        ),
        XiaomiDeviceEntry(
            branch = "curtana-q-oss",
            devices = "Redmi Note 9 Pro, Redmi Note 9 Pro Max",
            androidVersion = "Android Q (10)",
            baseTag = "LA.UM.8.9.r1-07100-SM6xx.0",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/curtana-q-oss",
            soc = "Qualcomm Snapdragon 720G (SM7125)",
            isMtk = false,
            testPointGuide = "Short 2 gold TP dots near motherboard edge to ground for EDL 9008."
        ),
        XiaomiDeviceEntry(
            branch = "ginkgo-q-oss",
            devices = "Redmi Note 8, Redmi Note 8T",
            androidVersion = "Android Q (10)",
            baseTag = "LA.UM.8.11.r1-02400-NICOBAR.0",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/ginkgo-q-oss",
            soc = "Qualcomm Snapdragon 665 (SM6125)",
            isMtk = false,
            testPointGuide = "EDL 9008 Test Point: Short two test pads near battery connector."
        ),
        XiaomiDeviceEntry(
            branch = "sweet-r-oss",
            devices = "Redmi Note 10 Pro (Global)",
            androidVersion = "Android R (11)",
            baseTag = "LA.UM.9.1.r1-06700-SMxxx0.0-1",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/sweet-r-oss",
            soc = "Qualcomm Snapdragon 732G (SM7150)",
            isMtk = false,
            testPointGuide = "Short EDL test pads next to rear camera lens to ground."
        ),
        XiaomiDeviceEntry(
            branch = "vayu-r-oss",
            devices = "POCO X3 Pro",
            androidVersion = "Android R (11)",
            baseTag = "LA.UM.9.1.r1-07000-SMxxx0.0",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/vayu-r-oss",
            soc = "Qualcomm Snapdragon 860 (SM8150-AC)",
            isMtk = false,
            testPointGuide = "EDL Test Point pads located below upper microphone flex."
        ),
        XiaomiDeviceEntry(
            branch = "marble-s-oss",
            devices = "Redmi Note 12 Turbo, POCO F5",
            androidVersion = "Android S (12)",
            baseTag = "LA.VENDOR.1.0.r1-19000-r2.0",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/marble-s-oss",
            soc = "Qualcomm Snapdragon 7+ Gen 2 (SM7475)",
            isMtk = false,
            testPointGuide = "EDL 9008 Test Point pads near UFS chip."
        ),
        XiaomiDeviceEntry(
            branch = "shennong-u-oss",
            devices = "Xiaomi 14, Xiaomi 14 Pro",
            androidVersion = "Android U (14)",
            baseTag = "KERNEL.PLATFORM.3.0.r1-03200",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/shennong-u-oss",
            soc = "Qualcomm Snapdragon 8 Gen 3 (SM8650)",
            isMtk = false,
            testPointGuide = "Fastboot Flash / EDL 9008 Auth Mode."
        ),
        XiaomiDeviceEntry(
            branch = "dada-v-oss",
            devices = "Xiaomi 15, Xiaomi 15 Pro",
            androidVersion = "Android V (15)",
            baseTag = "qcom-LA.VENDOR.15.4.0.r1",
            link = "https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/dada-v-oss",
            soc = "Qualcomm Snapdragon 8 Elite (SM8750)",
            isMtk = false,
            testPointGuide = "Fastboot / EDL Protocol."
        )
    )

    fun searchDevices(query: String, mtkOnly: Boolean = false): List<XiaomiDeviceEntry> {
        val q = query.trim().lowercase()
        return deviceList.filter { entry ->
            val matchesQuery = q.isEmpty() ||
                    entry.devices.lowercase().contains(q) ||
                    entry.branch.lowercase().contains(q) ||
                    entry.soc.lowercase().contains(q) ||
                    entry.androidVersion.lowercase().contains(q)
            val matchesMtk = !mtkOnly || entry.isMtk
            matchesQuery && matchesMtk
        }
    }
}
