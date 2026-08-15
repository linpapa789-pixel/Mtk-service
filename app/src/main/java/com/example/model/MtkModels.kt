package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogLevel {
    INFO,
    SUCCESS,
    WARN,
    ERROR,
    PACKET,
    COMMAND
}

data class TerminalLog(
    val id: Long = System.nanoTime(),
    val timestamp: String = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date()),
    val level: LogLevel,
    val message: String,
    val hexDump: String? = null
)

enum class ServiceState {
    IDLE,
    ARMED_WAITING,
    DEVICE_DETECTED,
    HANDSHAKING,
    SLA_BYPASSING,
    DA_LOADING,
    GPT_READING,
    STREAMING_BACKUP,
    ERASING_PARTITION,
    FLASHING_IMAGE,
    DIAGNOSTIC_TESTING,
    REBOOTING_DEVICE,
    COMPLETED,
    FAILED
}

enum class BridgeConnectionMode {
    ESP32_WIFI_TCP,
    ESP32_USB_SERIAL,
    ESP32_BLE,
    DIRECT_USB_OTG
}

enum class MtkOperationCategory(val displayName: String, val iconName: String) {
    ALL("All Operations", "All"),
    SECURITY_LOCKS("Security & Locks", "Lock"),
    IMEI_BASEBAND("IMEI & Baseband", "Signal"),
    FLASH_PARTITION("Flash & Partitions", "Storage"),
    HARDWARE_HEALTH("Hardware Health", "Health"),
    FASTBOOT_ADB("Fastboot & ADB", "Usb")
}

enum class MtkOperation(
    val title: String,
    val description: String,
    val isDestructive: Boolean,
    val category: MtkOperationCategory
) {
    // 🔓 Security & Locks
    UNLOCK_BOOTLOADER_SECCFG(
        title = "🔓 1-Click Instant Bootloader Unlock",
        description = "Patches seccfg partition directly via BROM (Bypasses 168h Mi Account wait)",
        isDestructive = true,
        category = MtkOperationCategory.SECURITY_LOCKS
    ),
    LOCK_BOOTLOADER_SECCFG(
        title = "🔒 1-Click Relock Bootloader",
        description = "Restores factory locked flags in seccfg to pass Play Integrity / SafetyNet",
        isDestructive = true,
        category = MtkOperationCategory.SECURITY_LOCKS
    ),
    MI_ACCOUNT_RESET(
        title = "☁️ Xiaomi Mi Account / Cloud Reset",
        description = "Wipes find-device token in persist & frp partitions to prevent cloud relock",
        isDestructive = true,
        category = MtkOperationCategory.SECURITY_LOCKS
    ),
    PERMANENT_DEMO_REMOVE(
        title = "🏬 Permanent Demo Mode Removal",
        description = "Clears retail live demo lock flags for Xiaomi, Oppo, Vivo, Realme",
        isDestructive = true,
        category = MtkOperationCategory.SECURITY_LOCKS
    ),
    ERASE_FRP(
        title = "🔑 1-Click Erase FRP (Google Lock)",
        description = "Wipes FRP & persistent memory blocks to clear Google Account verification",
        isDestructive = true,
        category = MtkOperationCategory.SECURITY_LOCKS
    ),
    ERASE_USERDATA(
        title = "🧹 1-Click Erase Userdata (Screen Lock)",
        description = "Cleans userdata partition blocks to wipe PIN, Password, Pattern lock",
        isDestructive = true,
        category = MtkOperationCategory.SECURITY_LOCKS
    ),
    RPMB_READ_KEYS(
        title = "🔐 Read RPMB Keys & Secure Boot State",
        description = "Extracts RPMB authentication key hash and OEM secure boot status",
        isDestructive = false,
        category = MtkOperationCategory.SECURITY_LOCKS
    ),

    // 📶 IMEI & Baseband Suite
    BACKUP_NVRAM(
        title = "🛡️ Full NVRAM / NVDATA / NVCFG Backup",
        description = "Safely dumps IMEI, Baseband, MAC, and RF calibration tables to storage",
        isDestructive = false,
        category = MtkOperationCategory.IMEI_BASEBAND
    ),
    RESTORE_NVRAM(
        title = "📥 Restore NVRAM / NVDATA from File",
        description = "Restores calibrated RF and Baseband parameters to flash memory",
        isDestructive = true,
        category = MtkOperationCategory.IMEI_BASEBAND
    ),
    REPAIR_BASEBAND_UNKNOWN(
        title = "📶 Fix Baseband Unknown / Null Radio",
        description = "Reconstructs corrupted modem nvdata structures and restores RF power table",
        isDestructive = true,
        category = MtkOperationCategory.IMEI_BASEBAND
    ),
    WRITE_DUAL_IMEI(
        title = "✏️ Dual IMEI Hex Injector & Repair",
        description = "Generates valid Luhn checksum and writes IMEI 1 & IMEI 2 to nvram blocks",
        isDestructive = true,
        category = MtkOperationCategory.IMEI_BASEBAND
    ),
    SIM_NETWORK_UNLOCK(
        title = "🌐 SIM Carrier / Regional Unlock",
        description = "Patches modem lock policy to allow any SIM carrier worldwide",
        isDestructive = true,
        category = MtkOperationCategory.IMEI_BASEBAND
    ),

    // 💾 Flash & Partition
    READ_GPT(
        title = "📑 Read Partition Table (GPT)",
        description = "Scans and resolves full memory partition offsets and linear boundaries",
        isDestructive = false,
        category = MtkOperationCategory.FLASH_PARTITION
    ),
    FLASH_SINGLE_PARTITION(
        title = "⚡ Flash Single Partition (.img)",
        description = "Directly flashes boot.img, recovery.img, vbmeta.img, super.img via BROM",
        isDestructive = true,
        category = MtkOperationCategory.FLASH_PARTITION
    ),
    PATCH_VBMETA_DMVERITY(
        title = "🛡️ Patch VBMeta (Disable DM-Verity)",
        description = "Disables AVB verification flags to fix 'Red State' and custom ROM bootloops",
        isDestructive = true,
        category = MtkOperationCategory.FLASH_PARTITION
    ),
    DUMP_FULL_RAW_EMMC(
        title = "💾 1:1 Full eMMC / UFS RAW Dump",
        description = "Clones entire physical storage blocks into a raw uncompressed image binary",
        isDestructive = false,
        category = MtkOperationCategory.FLASH_PARTITION
    ),
    GENERATE_SCATTER_FROM_GPT(
        title = "📑 Auto-Generate Scatter.txt from Device",
        description = "Reads on-device GPT and produces a standard SP Flash Tool scatter file",
        isDestructive = false,
        category = MtkOperationCategory.FLASH_PARTITION
    ),

    // 🩺 Hardware Diagnostics & Health
    ESP32_FORCE_BROM_GLITCH(
        title = "⚡ ESP32 Hardware Glitch (Force BROM)",
        description = "Sends microsecond VBUS/CLK pulse to recover dead preloader / hard-brick phones",
        isDestructive = false,
        category = MtkOperationCategory.HARDWARE_HEALTH
    ),
    EMMC_HEALTH_CHECK(
        title = "🩺 eMMC / UFS Health & Life-Time Check",
        description = "Reads EXT_CSD registers to analyze storage lifespan, bad blocks, and Pre-EOL",
        isDestructive = false,
        category = MtkOperationCategory.HARDWARE_HEALTH
    ),
    DRAM_EMI_STRESS_TEST(
        title = "🧠 RAM / DRAM Calibration & EMI Test",
        description = "Executes memory bus stress patterns to diagnose hardware freezing / reboot issues",
        isDestructive = false,
        category = MtkOperationCategory.HARDWARE_HEALTH
    ),

    // ⚡ Fastboot & ADB
    FASTBOOT_WIPE(
        title = "⚡ Fastboot 1-Click Userdata Wipe",
        description = "Direct OTG fastboot -w command to format userdata",
        isDestructive = true,
        category = MtkOperationCategory.FASTBOOT_ADB
    ),
    FASTBOOT_FRP(
        title = "🔓 Fastboot Erase FRP",
        description = "Direct OTG fastboot erase frp command",
        isDestructive = true,
        category = MtkOperationCategory.FASTBOOT_ADB
    ),
    FASTBOOT_UNLOCK(
        title = "🔓 Fastboot OEM Unlock",
        description = "Sends fastboot flashing unlock / oem unlock command",
        isDestructive = true,
        category = MtkOperationCategory.FASTBOOT_ADB
    ),
    ADB_REBOOT_BROM(
        title = "🔄 ADB Reboot to BROM / EDL",
        description = "Sends adb reboot edl / reboot-bootloader via USB",
        isDestructive = false,
        category = MtkOperationCategory.FASTBOOT_ADB
    ),
    ADB_DEVICE_INFO(
        title = "📱 ADB Read Full Device Info",
        description = "Reads getprop ro.product.model, soc, build, security patch level",
        isDestructive = false,
        category = MtkOperationCategory.FASTBOOT_ADB
    )
}

data class MtkChipset(
    val name: String,
    val hwCodeHex: String,
    val hwSubCodeHex: String,
    val architecture: String,
    val popularPhones: String,
    val bromKeyCombo: String,
    val testPointGuide: String,
    val defaultNvramStart: String,
    val defaultNvramLength: String,
    val defaultNvdataStart: String,
    val defaultNvdataLength: String,
    val defaultFrpStart: String,
    val defaultFrpLength: String,
    val daRequired: String,
    val slaType: String
)

data class EmmcHealthReport(
    val deviceLifeTimeEstTypA: String = "0x01 (0% - 10% device life time used)",
    val deviceLifeTimeEstTypB: String = "0x01 (0% - 10% device life time used)",
    val preEolInfo: String = "0x01 (Normal / Healthy)",
    val badBlocksCount: Int = 0,
    val healthPercentage: Int = 98,
    val statusSummary: String = "Storage chip in excellent condition"
)

data class DeviceInfo(
    val isConnected: Boolean = false,
    val socModel: String = "No Device Connected",
    val hwCode: String = "----",
    val hwSubCode: String = "----",
    val hwVer: String = "----",
    val swVer: String = "----",
    val targetName: String = "Waiting for connection...",
    val securityState: String = "Unknown",
    val storageType: String = "eMMC 5.1 / UFS",
    val emmcCid: String = "----",
    val vbusVoltage: String = "0.0 V",
    val batteryState: String = "Unknown",
    val connectionMode: String = "Direct OTG / ESP32",
    val healthReport: EmmcHealthReport = EmmcHealthReport()
)

data class PartitionEntry(
    val index: Int,
    val name: String,
    val linearStartHex: String,
    val boundaryLengthHex: String,
    val sizeBytes: Long,
    val isProtected: Boolean = false,
    val isSelected: Boolean = false
) {
    val formattedSize: String
        get() {
            return when {
                sizeBytes >= 1024 * 1024 * 1024 -> String.format(Locale.US, "%.2f GB", sizeBytes / (1024.0 * 1024.0 * 1024.0))
                sizeBytes >= 1024 * 1024 -> String.format(Locale.US, "%.2f MB", sizeBytes / (1024.0 * 1024.0))
                sizeBytes >= 1024 -> String.format(Locale.US, "%.2f KB", sizeBytes / 1024.0)
                else -> "$sizeBytes B"
            }
        }
}

data class DiagnosticIssue(
    val errorCode: String,
    val title: String,
    val description: String,
    val cause: String,
    val solution: String,
    val recommendedDa: String
)

data class DaPreloaderConfig(
    val selectedDa: String = "MTK_AllInOne_DA.bin (Standard V5/V6 Universal)",
    val customDaPath: String = "",
    val selectedPreloader: String = "Auto-detect from SoC / Target Device",
    val customPreloaderPath: String = "",
    val selectedAuth: String = "Auto Glitch Hardware Bypass (No SLA/DAA Auth Required)",
    val customAuthPath: String = "",
    val enableHighSpeedDramInit: Boolean = true,
    val bypassSlaDaaSecurity: Boolean = true
) {
    companion object {
        val DA_PRESETS = listOf(
            "MTK_AllInOne_DA.bin (Standard V5/V6 Universal)",
            "DA_PL.bin (High-Speed Preloader DA)",
            "DA_SWSEC_6765_6768.bin (Xiaomi/Oppo/Vivo Security Bypass)",
            "DA_6833_6877_Dimensity.bin (Dimensity 700/810/900/1200)",
            "DA_6761_6762_P22_A22.bin (Helio A22/P22/P35)",
            "Custom DA File (.bin)"
        )

        val PRELOADER_PRESETS = listOf(
            "Auto-detect from SoC / Target Device",
            "preloader_k62v1_64.bin (MT6765 / MT6762)",
            "preloader_k68v1_64.bin (MT6768 / MT6769 / Helio G80/G85/G88)",
            "preloader_k6833v1_64.bin (Dimensity 700 / 810 5G)",
            "preloader_k6877v1_64.bin (Dimensity 900 / 920 5G)",
            "preloader_k6983v1_64.bin (Dimensity 9000 Flagship)",
            "Custom Preloader File (.bin)"
        )

        val AUTH_PRESETS = listOf(
            "Auto Glitch Hardware Bypass (No SLA/DAA Auth Required)",
            "auth_sv5.auth (Standard SV5 Signed Token)",
            "oppo_realme_auth.auth (Oppo/Realme V3 Auth)",
            "xiaomi_auth_v4.auth (Xiaomi Signed Token)",
            "Custom Auth File (.auth)"
        )
    }
}
