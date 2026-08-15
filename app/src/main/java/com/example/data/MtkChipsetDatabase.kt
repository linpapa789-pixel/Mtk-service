package com.example.data

import com.example.model.DiagnosticIssue
import com.example.model.MtkChipset
import com.example.model.PartitionEntry

object MtkChipsetDatabase {

    val supportedChipsets: List<MtkChipset> = listOf(
        MtkChipset(
            name = "MT6765 / Helio G25 / P35",
            hwCodeHex = "0x0766",
            hwSubCodeHex = "0x8A00",
            architecture = "ARM64 (Cortex-A53)",
            popularPhones = "Xiaomi Redmi 9A, 9C, Oppo A15, A16, Vivo Y15s, Y1s, Samsung A03s",
            bromKeyCombo = "Power OFF -> Hold Vol(+) and Vol(-) -> Connect USB Cable",
            testPointGuide = "Short TP (CLK pin near eMMC shielding) to GND with tweezers while inserting cable",
            defaultNvramStart = "0x1800000",
            defaultNvramLength = "0x500000",
            defaultNvdataStart = "0x1D00000",
            defaultNvdataLength = "0x2000000",
            defaultFrpStart = "0x5580000",
            defaultFrpLength = "0x100000",
            daRequired = "MTK_AllInOne_DA_v6765.bin",
            slaType = "Kamakiri / Amonet Exploit supported"
        ),
        MtkChipset(
            name = "MT6768 / Helio G85 / G80",
            hwCodeHex = "0x0707",
            hwSubCodeHex = "0x8A00",
            architecture = "ARM64 (Cortex-A75 + A55)",
            popularPhones = "Xiaomi Redmi 9, Note 9, Realme 6i, Narzo 20, Poco M2, Vivo Y20G",
            bromKeyCombo = "Power OFF -> Hold Vol(+) and Vol(-) -> Connect USB Cable",
            testPointGuide = "Locate KCOL0 / TP pad next to PMIC MT6358 and connect to shield GND",
            defaultNvramStart = "0x1A00000",
            defaultNvramLength = "0x500000",
            defaultNvdataStart = "0x1F00000",
            defaultNvdataLength = "0x4000000",
            defaultFrpStart = "0x6A00000",
            defaultFrpLength = "0x100000",
            daRequired = "MTK_DA_v6768_HighSecurity.bin",
            slaType = "Preloader handshake + Watchdog payload"
        ),
        MtkChipset(
            name = "MT6785 / Helio G90T / G95",
            hwCodeHex = "0x0788",
            hwSubCodeHex = "0x8A00",
            architecture = "ARM64 (Cortex-A76 + A55)",
            popularPhones = "Redmi Note 8 Pro, Realme 6, Realme 7, Realme 8, Infinix Note 10 Pro",
            bromKeyCombo = "Power OFF -> Hold Vol(+) + Vol(-) or Vol(-) only -> Connect USB Cable",
            testPointGuide = "CLK Testpoint located under motherboard camera bracket -> Short to GND",
            defaultNvramStart = "0x2800000",
            defaultNvramLength = "0x500000",
            defaultNvdataStart = "0x2D00000",
            defaultNvdataLength = "0x4000000",
            defaultFrpStart = "0x8B00000",
            defaultFrpLength = "0x100000",
            daRequired = "DA_PL_MT6785_CustomAuth.bin",
            slaType = "SLA/DAA bypass via Brom Crypto Buffer Overflow"
        ),
        MtkChipset(
            name = "MT6833 / Dimensity 700 5G",
            hwCodeHex = "0x0886",
            hwSubCodeHex = "0x8A00",
            architecture = "ARM64 (Cortex-A76 5G)",
            popularPhones = "Redmi Note 10 5G, Poco M3 Pro 5G, Realme 8 5G, Samsung A22 5G",
            bromKeyCombo = "Power OFF -> Hold Vol(+) and Vol(-) with Battery Connected",
            testPointGuide = "V6 Protocol Test Point - Connect D+ / TP pad to ground before inserting VBUS",
            defaultNvramStart = "0x3400000",
            defaultNvramLength = "0x1000000",
            defaultNvdataStart = "0x4400000",
            defaultNvdataLength = "0x4000000",
            defaultFrpStart = "0x9800000",
            defaultFrpLength = "0x100000",
            daRequired = "MTK_DA_V6_MT6833_UFS.bin",
            slaType = "V6 Protocol / Preloader Mode required"
        ),
        MtkChipset(
            name = "MT6877 / Dimensity 900 / 920 5G",
            hwCodeHex = "0x0986",
            hwSubCodeHex = "0x8A00",
            architecture = "ARM64 (Cortex-A78 5G)",
            popularPhones = "Xiaomi Redmi Note 11 Pro+ 5G, Realme 9 Pro+, Vivo V23 5G, Infinix Zero 5G",
            bromKeyCombo = "Power OFF -> Hold Vol(-) -> Connect High-Current USB Cable",
            testPointGuide = "UFS Storage TP pad on mainboard backside -> Short to GND",
            defaultNvramStart = "0x3800000",
            defaultNvramLength = "0x1000000",
            defaultNvdataStart = "0x4800000",
            defaultNvdataLength = "0x4000000",
            defaultFrpStart = "0xA000000",
            defaultFrpLength = "0x100000",
            daRequired = "MTK_DA_V6_MT6877.bin",
            slaType = "V6 High-Speed Protocol"
        ),
        MtkChipset(
            name = "MT6580 / Legacy 32-bit",
            hwCodeHex = "0x0580",
            hwSubCodeHex = "0xCA00",
            architecture = "ARMv7 (Cortex-A7 32-bit)",
            popularPhones = "Older Huawei, Tecno, Itel, Lava, Symphony, Generic MTK Feature Phones",
            bromKeyCombo = "Power OFF -> Remove battery & insert -> Hold Vol(-) or Vol(+) -> Connect USB",
            testPointGuide = "Direct BROM auto-detection without testpoint. No security keys required.",
            defaultNvramStart = "0x380000",
            defaultNvramLength = "0x500000",
            defaultNvdataStart = "0x880000",
            defaultNvdataLength = "0x2000000",
            defaultFrpStart = "0x2880000",
            defaultFrpLength = "0x100000",
            daRequired = "MTK_AllInOne_DA_v3320.bin",
            slaType = "None (Unsecured BROM)"
        ),
        MtkChipset(
            name = "MT6739 / Entry Level 4G",
            hwCodeHex = "0x0321",
            hwSubCodeHex = "0x8A00",
            architecture = "ARM64 (Cortex-A53)",
            popularPhones = "Samsung A01 Core, A02, Nokia 1 Plus, Tecno Spark 4 Air",
            bromKeyCombo = "Power OFF -> Hold Vol(+) + Vol(-) -> Connect USB",
            testPointGuide = "Short TP point near SIM tray to GND",
            defaultNvramStart = "0x1200000",
            defaultNvramLength = "0x500000",
            defaultNvdataStart = "0x1700000",
            defaultNvdataLength = "0x2000000",
            defaultFrpStart = "0x4500000",
            defaultFrpLength = "0x100000",
            daRequired = "MTK_AllInOne_DA.bin",
            slaType = "Kamakiri payload enabled"
        ),
        MtkChipset(
            name = "MT6771 / Helio P60 / P70",
            hwCodeHex = "0x0717",
            hwSubCodeHex = "0x8A00",
            architecture = "ARM64 (Cortex-A73 + A53)",
            popularPhones = "Oppo F7, F9, Realme 1, Realme 3, Vivo V15, Vivo V11i",
            bromKeyCombo = "Power OFF -> Hold Vol(+) and Vol(-) -> Connect USB Cable",
            testPointGuide = "TP testpoint adjacent to battery connector FPC -> Short to ground",
            defaultNvramStart = "0x1C00000",
            defaultNvramLength = "0x500000",
            defaultNvdataStart = "0x2100000",
            defaultNvdataLength = "0x2000000",
            defaultFrpStart = "0x5D00000",
            defaultFrpLength = "0x100000",
            daRequired = "DA_PL_MT6771_Custom.bin",
            slaType = "SLA/DAA bypass payload supported"
        )
    )

    fun findChipsetByHwCode(hwCode: String): MtkChipset? {
        val clean = hwCode.trim().lowercase()
        return supportedChipsets.firstOrNull {
            it.hwCodeHex.lowercase() == clean || it.name.lowercase().contains(clean)
        }
    }

    val sampleScatterPartitions: List<PartitionEntry> = listOf(
        PartitionEntry(0, "preloader", "0x00000000", "0x00040000", 262144, isProtected = true),
        PartitionEntry(1, "pgpt", "0x00000000", "0x00080000", 524288, isProtected = true),
        PartitionEntry(2, "boot_para", "0x00080000", "0x00100000", 1048576, isProtected = false),
        PartitionEntry(3, "nvcfg", "0x00180000", "0x00200000", 2097152, isProtected = true),
        PartitionEntry(4, "nvram", "0x01800000", "0x00500000", 5242880, isProtected = true),
        PartitionEntry(5, "nvdata", "0x01D00000", "0x02000000", 33554432, isProtected = true),
        PartitionEntry(6, "proinfo", "0x03D00000", "0x00300000", 3145728, isProtected = false),
        PartitionEntry(7, "frp", "0x05580000", "0x00100000", 1048576, isProtected = false),
        PartitionEntry(8, "para", "0x05680000", "0x00080000", 524288, isProtected = false),
        PartitionEntry(9, "misc", "0x05700000", "0x00080000", 524288, isProtected = false),
        PartitionEntry(10, "boot", "0x05780000", "0x02000000", 33554432, isProtected = false),
        PartitionEntry(11, "vbmeta", "0x07780000", "0x00800000", 8388608, isProtected = false),
        PartitionEntry(12, "super", "0x08000000", "0x180000000", 6442450944L, isProtected = false),
        PartitionEntry(13, "userdata", "0x188000000", "0x600000000", 25769803776L, isProtected = false)
    )

    val commonDiagnosticIssues: List<DiagnosticIssue> = listOf(
        DiagnosticIssue(
            errorCode = "ERROR 4032 (S_FT_ENABLE_DRAM_FAIL)",
            title = "DRAM Initialization Failure",
            description = "Download Agent (DA) successfully entered BROM but could not configure DRAM memory controller.",
            cause = "1. Incorrect DA file selected for this exact hardware variation.\n2. Mismatched eMMC/UFS/LPDDR RAM parameters.\n3. Hardware fault in DRAM IC or loose solder ball.",
            solution = "Select a Custom DA with the exact RAM timing profile (e.g. Micron / Samsung / SK Hynix profile) or check battery voltage > 3.8V.",
            recommendedDa = "MTK_AllInOne_DA_v6765_HighSecurity.bin"
        ),
        DiagnosticIssue(
            errorCode = "ERROR 0x1011 (STATUS_BROM_CMD_FAIL)",
            title = "BROM Security SLA/DAA Rejected",
            description = "Target MediaTek Boot ROM rejected the unsigned DA download command.",
            cause = "Secure Boot is enabled by the OEM (Xiaomi, Oppo, Realme, Vivo) and DAA signature check failed.",
            solution = "Arm the Kamakiri/Amonet payload injector in the ESP32-S3 bridge before plugging in the cable to disable DAA registers.",
            recommendedDa = "Use Auth-Bypass with DA_PL.bin"
        ),
        DiagnosticIssue(
            errorCode = "ERROR 0x7000 (DA_HASH_MISMATCH)",
            title = "Download Agent Hash Verification Failed",
            description = "DA stage 1 checksum sent over USB did not match expected Boot ROM hash table.",
            cause = "USB packet corruption or wrong DA version for SoC generation.",
            solution = "Use chunked 16KB/64KB transfers with CRC32 verification and ensure a powered OTG cable.",
            recommendedDa = "DA_SW_SEC_v6.bin"
        ),
        DiagnosticIssue(
            errorCode = "ERROR PORT_TIMEOUT (0x0005)",
            title = "Preloader Disconnected Before Handshake",
            description = "The target device exited VCOM preloader port in 2-3 seconds and entered battery charging state.",
            cause = "Vol(+) and Vol(-) buttons were released too early, or USB Host listener was not armed prior to cable insertion.",
            solution = "Always press 'START WAITING FOR DEVICE' first in the tool, hold Vol buttons firmly, then insert USB cable.",
            recommendedDa = "Generic MTK BROM Handler"
        )
    )
}
