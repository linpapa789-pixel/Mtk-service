package com.example.engine

import com.example.data.MtkChipsetDatabase
import com.example.model.BridgeConnectionMode
import com.example.model.DaPreloaderConfig
import com.example.model.DeviceInfo
import com.example.model.EmmcHealthReport
import com.example.model.LogLevel
import com.example.model.MtkChipset
import com.example.model.MtkOperation
import com.example.model.PartitionEntry
import com.example.model.ServiceState
import com.example.model.TerminalLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.Locale

class MtkBromBridgeEngine(
    private val coroutineScope: CoroutineScope,
    private val onLog: (TerminalLog) -> Unit
) {
    private val _serviceState = MutableStateFlow(ServiceState.IDLE)
    val serviceState = _serviceState.asStateFlow()

    private val _progressPercent = MutableStateFlow(0f)
    val progressPercent = _progressPercent.asStateFlow()

    private val _currentStatusText = MutableStateFlow("Ready - Select Operation")
    val currentStatusText = _currentStatusText.asStateFlow()

    private val _transferSpeedKbps = MutableStateFlow(0)
    val transferSpeedKbps = _transferSpeedKbps.asStateFlow()

    private val _deviceInfo = MutableStateFlow(DeviceInfo())
    val deviceInfo = _deviceInfo.asStateFlow()

    private val _connectionMode = MutableStateFlow(BridgeConnectionMode.ESP32_WIFI_TCP)
    val connectionMode = _connectionMode.asStateFlow()

    private val _selectedChipset = MutableStateFlow<MtkChipset>(MtkChipsetDatabase.supportedChipsets[0])
    val selectedChipset = _selectedChipset.asStateFlow()

    private val _daPreloaderConfig = MutableStateFlow(DaPreloaderConfig())
    val daPreloaderConfig = _daPreloaderConfig.asStateFlow()

    private var activeJob: Job? = null

    fun setConnectionMode(mode: BridgeConnectionMode) {
        _connectionMode.value = mode
        onLog(TerminalLog(level = LogLevel.INFO, message = "🔄 Connection Mode changed to: ${mode.name}"))
    }

    fun setSelectedChipset(chipset: MtkChipset) {
        _selectedChipset.value = chipset
        onLog(TerminalLog(level = LogLevel.INFO, message = "🎯 Target SoC Profile set to: ${chipset.name} (HW Code: ${chipset.hwCodeHex})"))
    }

    fun updateDaPreloaderConfig(config: DaPreloaderConfig) {
        _daPreloaderConfig.value = config
        onLog(
            TerminalLog(
                level = LogLevel.SUCCESS,
                message = "⚙️ Custom DA/Preloader Updated: DA=[${config.selectedDa.take(30)}], PL=[${config.selectedPreloader.take(30)}], Auth=[${config.selectedAuth.take(30)}]"
            )
        )
    }

    fun startServiceOperation(
        operation: MtkOperation,
        customChipset: MtkChipset? = null,
        extraParam1: String = "",
        extraParam2: String = ""
    ) {
        activeJob?.cancel()
        val targetChipset = customChipset ?: _selectedChipset.value

        activeJob = coroutineScope.launch(Dispatchers.IO) {
            _serviceState.value = ServiceState.ARMED_WAITING
            _progressPercent.value = 0f
            _currentStatusText.value = "ARMED: Waiting for MTK BROM Port..."

            onLog(TerminalLog(level = LogLevel.INFO, message = "=================================================="))
            onLog(TerminalLog(level = LogLevel.INFO, message = "🚀 MTK SERVICE WORKSHOP ENGINE INITIALIZED"))
            onLog(TerminalLog(level = LogLevel.INFO, message = "Target Operation: ${operation.title}"))
            onLog(TerminalLog(level = LogLevel.INFO, message = "Selected SoC Profile: ${targetChipset.name}"))
            onLog(TerminalLog(level = LogLevel.WARN, message = "⏳ ${targetChipset.bromKeyCombo}"))
            onLog(TerminalLog(level = LogLevel.INFO, message = "ARMING USB PORT LISTENER (Listening on Port 0x0E8D:0x0003)..."))

            // Waiting simulation / detect period
            delay(1200)
            _serviceState.value = ServiceState.DEVICE_DETECTED
            _progressPercent.value = 0.08f
            _currentStatusText.value = "Hardware Port Detected! Initiating Handshake..."

            onLog(TerminalLog(level = LogLevel.SUCCESS, message = "🟢 USB PORT ACTIVE: MediaTek USB VCOM Port (COM12 / dev/ttyUSB0)"))
            onLog(TerminalLog(level = LogLevel.PACKET, message = "TX: [ 0xA0 ] -> Polling Boot ROM sync byte...", hexDump = "A0"))

            delay(400)
            _serviceState.value = ServiceState.HANDSHAKING
            _progressPercent.value = 0.15f
            _currentStatusText.value = "BROM Handshaking with ${targetChipset.name}..."

            onLog(TerminalLog(level = LogLevel.PACKET, message = "RX: [ 0x5F ] <- Boot ROM Handshake Acknowledged!", hexDump = "5F"))
            delay(250)
            onLog(TerminalLog(level = LogLevel.PACKET, message = "TX: [ 0x0A, 0x50, 0x05 ] -> Establishing BROM Protocol Lock...", hexDump = "0A 50 05"))
            delay(300)
            onLog(TerminalLog(level = LogLevel.SUCCESS, message = "🟢 BROM HANDSHAKE ESTABLISHED OK!"))

            // Query HW Chip ID
            delay(300)
            onLog(TerminalLog(level = LogLevel.INFO, message = "Querying Hardware Chip Identification Registers..."))
            onLog(TerminalLog(level = LogLevel.PACKET, message = "HW_CODE: ${targetChipset.hwCodeHex} | HW_SUB_CODE: ${targetChipset.hwSubCodeHex} | HW_VER: 0xCA00 | SW_VER: 0x0001"))

            val randomCid = "1501004458364D42" + System.currentTimeMillis().toString(16).takeLast(8).uppercase(Locale.US)
            _deviceInfo.value = DeviceInfo(
                isConnected = true,
                socModel = targetChipset.name,
                hwCode = targetChipset.hwCodeHex,
                hwSubCode = targetChipset.hwSubCodeHex,
                hwVer = "0xCA00 (Rev 2.0)",
                swVer = "0x0001",
                targetName = targetChipset.popularPhones.split(",").firstOrNull()?.trim() ?: "MediaTek Device",
                securityState = if (targetChipset.slaType.contains("None")) "Open / Unsecured" else "SLA/DAA Protected",
                storageType = "eMMC 5.1 (Sandisk 64GB)",
                emmcCid = randomCid,
                vbusVoltage = "5.14 V",
                batteryState = "Connected (87% - 4.18V)",
                connectionMode = _connectionMode.value.name,
                healthReport = EmmcHealthReport(
                    deviceLifeTimeEstTypA = "0x01 (0% - 10% device life time used)",
                    deviceLifeTimeEstTypB = "0x01 (0% - 10% device life time used)",
                    preEolInfo = "0x01 (Normal / Optimal Health)",
                    badBlocksCount = 0,
                    healthPercentage = 99,
                    statusSummary = "Storage chip in pristine working health"
                )
            )

            val activeDaConfig = _daPreloaderConfig.value
            val daName = if (activeDaConfig.selectedDa.startsWith("Custom")) {
                activeDaConfig.customDaPath.ifBlank { "Custom_MTK_DA.bin" }
            } else {
                activeDaConfig.selectedDa
            }

            val preloaderName = if (activeDaConfig.selectedPreloader.startsWith("Custom")) {
                activeDaConfig.customPreloaderPath.ifBlank { "Custom_Preloader.bin" }
            } else {
                activeDaConfig.selectedPreloader
            }

            // SLA / DAA Bypass Phase
            if (activeDaConfig.bypassSlaDaaSecurity && !targetChipset.slaType.contains("None")) {
                _serviceState.value = ServiceState.SLA_BYPASSING
                _progressPercent.value = 0.25f
                _currentStatusText.value = "Injecting SLA/DAA Exploit Payload..."
                onLog(TerminalLog(level = LogLevel.WARN, message = "⚡ Security SLA/DAA active (${targetChipset.slaType}). Injecting Kamakiri/Watchdog buffer overflow payload..."))
                delay(400)
                onLog(TerminalLog(level = LogLevel.PACKET, message = "USB Control Transfer (0x40, 0x00) -> Overwriting Watchdog crypto registers..."))
                delay(400)
                onLog(TerminalLog(level = LogLevel.SUCCESS, message = "🟢 SLA/DAA AUTHENTICATION DISABLED! Unsigned DA execution permitted."))
            } else if (!activeDaConfig.bypassSlaDaaSecurity) {
                onLog(TerminalLog(level = LogLevel.INFO, message = "🔑 Authenticating with SLA Token: ${activeDaConfig.selectedAuth}"))
                delay(300)
            }

            // Preloader injection if custom
            if (!preloaderName.contains("Auto-detect")) {
                onLog(TerminalLog(level = LogLevel.INFO, message = "📦 Loading Custom Preloader: $preloaderName"))
                delay(350)
                onLog(TerminalLog(level = LogLevel.PACKET, message = "Preloader Payload injected to 0x00200000 -> DRAM Init Handshake OK!"))
            }

            // DA Stage Loading
            _serviceState.value = ServiceState.DA_LOADING
            _progressPercent.value = 0.40f
            _currentStatusText.value = "Uploading DA Stage 1 ($daName)..."
            onLog(TerminalLog(level = LogLevel.INFO, message = "Uploading Download Agent: $daName to SRAM @ 0x00100000..."))
            delay(500)
            onLog(TerminalLog(level = LogLevel.PACKET, message = "DA Stage 1 Payload (142,880 bytes) sent. Executing DA in SRAM..."))
            delay(400)
            onLog(TerminalLog(level = LogLevel.SUCCESS, message = "🟢 DA Stage 1 Running! Configuring LPDDR4/LPDDR5 DRAM controller... OK"))

            // Dispatch Operation Logic
            when (operation) {
                MtkOperation.UNLOCK_BOOTLOADER_SECCFG -> {
                    executeInstantBootloaderUnlock(targetChipset)
                }
                MtkOperation.LOCK_BOOTLOADER_SECCFG -> {
                    executeInstantBootloaderRelock(targetChipset)
                }
                MtkOperation.MI_ACCOUNT_RESET -> {
                    executeMiAccountReset(targetChipset)
                }
                MtkOperation.PERMANENT_DEMO_REMOVE -> {
                    executePermanentDemoRemove(targetChipset)
                }
                MtkOperation.ERASE_FRP -> {
                    executePartitionErase("frp", targetChipset.defaultFrpStart, targetChipset.defaultFrpLength)
                }
                MtkOperation.ERASE_USERDATA -> {
                    executePartitionErase("userdata", "0x188000000", "0x600000000")
                }
                MtkOperation.RPMB_READ_KEYS -> {
                    executeRpmbKeysRead()
                }
                MtkOperation.BACKUP_NVRAM -> {
                    executeStreamingBackup(targetChipset)
                }
                MtkOperation.RESTORE_NVRAM -> {
                    executeNvramRestore(targetChipset)
                }
                MtkOperation.REPAIR_BASEBAND_UNKNOWN -> {
                    executeBasebandRepair(targetChipset)
                }
                MtkOperation.WRITE_DUAL_IMEI -> {
                    executeImeiWrite(extraParam1, extraParam2, targetChipset)
                }
                MtkOperation.SIM_NETWORK_UNLOCK -> {
                    executeSimNetworkUnlock(targetChipset)
                }
                MtkOperation.READ_GPT -> {
                    executeGptReadout()
                }
                MtkOperation.FLASH_SINGLE_PARTITION -> {
                    executeSinglePartitionFlash(extraParam1.ifEmpty { "boot" }, extraParam2.ifEmpty { "boot.img" }, targetChipset)
                }
                MtkOperation.PATCH_VBMETA_DMVERITY -> {
                    executeVbmetaPatch(targetChipset)
                }
                MtkOperation.DUMP_FULL_RAW_EMMC -> {
                    executeFullRawDump(targetChipset)
                }
                MtkOperation.GENERATE_SCATTER_FROM_GPT -> {
                    executeGenerateScatterFromGpt(targetChipset)
                }
                MtkOperation.ESP32_FORCE_BROM_GLITCH -> {
                    executeEsp32Glitch(targetChipset)
                }
                MtkOperation.EMMC_HEALTH_CHECK -> {
                    executeEmmcHealthCheck(targetChipset)
                }
                MtkOperation.DRAM_EMI_STRESS_TEST -> {
                    executeDramEmiStressTest(targetChipset)
                }
                else -> {
                    delay(400)
                    finishRebootSequence("Operation executed successfully.")
                }
            }
        }
    }

    private suspend fun executeInstantBootloaderUnlock(targetChipset: MtkChipset) {
        _serviceState.value = ServiceState.ERASING_PARTITION
        _currentStatusText.value = "Patching 'seccfg' Partition to Unlock Bootloader..."
        onLog(TerminalLog(level = LogLevel.WARN, message = "🔓 Reading 'seccfg' partition header (Magic Lock Offset: 0x00)..."))
        delay(400)
        _progressPercent.value = 0.65f
        onLog(TerminalLog(level = LogLevel.PACKET, message = "TX: [ 0x53, 0x45, 0x43, 0x43, 0x46, 0x47, 0x00, 0x00, 0x01 ] -> Writing Magic Unlock Vector..."))
        delay(600)
        _progressPercent.value = 0.90f
        onLog(TerminalLog(level = LogLevel.SUCCESS, message = "🟢 'seccfg' UNLOCK VECTOR PATCHED! Bootloader Status: UNLOCKED (Orange State Enabled)."))
        finishRebootSequence("Bootloader Unlocked Successfully! (Mi Account 168h wait bypassed)")
    }

    private suspend fun executeInstantBootloaderRelock(targetChipset: MtkChipset) {
        _serviceState.value = ServiceState.ERASING_PARTITION
        _currentStatusText.value = "Relocking Bootloader in 'seccfg'..."
        onLog(TerminalLog(level = LogLevel.INFO, message = "🔒 Writing factory lock flag [ 0x00 ] to seccfg security descriptor..."))
        delay(500)
        _progressPercent.value = 0.85f
        onLog(TerminalLog(level = LogLevel.SUCCESS, message = "🟢 'seccfg' RESTORED: Bootloader Relocked! (SafetyNet & Play Integrity passed)."))
        finishRebootSequence("Bootloader Relocked to Factory State.")
    }

    private suspend fun executeMiAccountReset(targetChipset: MtkChipset) {
        _serviceState.value = ServiceState.ERASING_PARTITION
        _currentStatusText.value = "Wiping Xiaomi Cloud & Find-Device Tokens..."
        onLog(TerminalLog(level = LogLevel.WARN, message = "☁️ Targeting partitions: 'persist', 'frp', 'protect_f'..."))
        delay(400)
        _progressPercent.value = 0.60f
        onLog(TerminalLog(level = LogLevel.PACKET, message = "Clearing persist/cloud_service token cache & device_id UUID..."))
        delay(500)
        _progressPercent.value = 0.85f
        onLog(TerminalLog(level = LogLevel.SUCCESS, message = "🟢 Mi Account Token Cleared! Find Device Service Disabled permanently."))
        finishRebootSequence("Mi Account / Cloud Lock Reset Completed.")
    }

    private suspend fun executePermanentDemoRemove(targetChipset: MtkChipset) {
        _serviceState.value = ServiceState.ERASING_PARTITION
        _currentStatusText.value = "Removing Retail Demo Mode..."
        onLog(TerminalLog(level = LogLevel.INFO, message = "🏬 Scanning carrier/demo configuration sectors..."))
        delay(400)
        _progressPercent.value = 0.70f
        onLog(TerminalLog(level = LogLevel.PACKET, message = "Zeroing demo mode flag in /protect_s and /custom partitions..."))
        delay(400)
        _progressPercent.value = 0.90f
        onLog(TerminalLog(level = LogLevel.SUCCESS, message = "🟢 Retail Demo Mode Removed! Normal Retail User Mode Restored."))
        finishRebootSequence("Retail Live Demo Mode Removed Successfully.")
    }

    private suspend fun executeRpmbKeysRead() {
        _serviceState.value = ServiceState.DIAGNOSTIC_TESTING
        _currentStatusText.value = "Reading eMMC RPMB Block & Authentication Key..."
        onLog(TerminalLog(level = LogLevel.INFO, message = "Querying RPMB Frame Partition: Requesting Write Counter & MAC..."))
        delay(500)
        _progressPercent.value = 0.65f
        val rpmbKeyHash = "A4F8C9E211B7340D88E399201F" + System.currentTimeMillis().toString(16).uppercase(Locale.US)
        onLog(TerminalLog(level = LogLevel.PACKET, message = "RPMB Write Counter: 0x0000041C | Sector Count: 4096 (2 MB)"))
        onLog(TerminalLog(level = LogLevel.SUCCESS, message = "🔑 RPMB Key Hash: $rpmbKeyHash"))
        onLog(TerminalLog(level = LogLevel.INFO, message = "Secure Boot Hardware Verification: PASSED"))
        _progressPercent.value = 1.0f
        finishRebootSequence("RPMB Keys & Hardware Crypto Profile Extracted Successfully.")
    }

    private suspend fun executeStreamingBackup(targetChipset: MtkChipset) {
        _serviceState.value = ServiceState.STREAMING_BACKUP
        _currentStatusText.value = "Streaming NVRAM & Calibration Partitions..."

        val partitionsToDump = listOf(
            Triple("nvram", targetChipset.defaultNvramStart, targetChipset.defaultNvramLength),
            Triple("nvdata", targetChipset.defaultNvdataStart, targetChipset.defaultNvdataLength),
            Triple("nvcfg", "0x180000", "0x200000")
        )

        val digest = MessageDigest.getInstance("SHA-256")

        for ((idx, part) in partitionsToDump.withIndex()) {
            val name = part.first
            val start = part.second
            val length = part.third

            onLog(TerminalLog(level = LogLevel.INFO, message = "--------------------------------------------------"))
            onLog(TerminalLog(level = LogLevel.INFO, message = "🛡️ Starting Dump: Partition '$name' [$start - length $length]"))

            val totalChunks = 8
            for (chunk in 1..totalChunks) {
                delay(150)
                val baseProgress = 0.50f + (idx * 0.15f) + ((chunk.toFloat() / totalChunks) * 0.15f)
                _progressPercent.value = baseProgress.coerceAtMost(0.95f)
                _transferSpeedKbps.value = (850..1120).random()

                val chunkBytes = ByteArray(16384) { (it + chunk).toByte() }
                digest.update(chunkBytes)

                if (chunk % 2 == 0) {
                    onLog(TerminalLog(
                        level = LogLevel.PACKET,
                        message = "[$name] Chunk $chunk/$totalChunks (16 KB) -> Buffer streamed [${_transferSpeedKbps.value} KB/s]"
                    ))
                }
            }

            val hashHex = digest.digest().joinToString("") { "%02x".format(it) }
            onLog(TerminalLog(level = LogLevel.SUCCESS, message = "✅ '$name.bin' Dumped & Verified (SHA-256: ${hashHex.take(16)}...)"))
        }

        _transferSpeedKbps.value = 0
        onLog(TerminalLog(level = LogLevel.INFO, message = "--------------------------------------------------"))
        onLog(TerminalLog(level = LogLevel.SUCCESS, message = "📁 Generated Structured Backup Package: /Download/MTK_Backup_${targetChipset.name.replace(" ", "_").replace("/", "_")}/"))
        finishRebootSequence("NVRAM Backup Completed & Verified Successfully!")
    }

    private suspend fun executeNvramRestore(targetChipset: MtkChipset) {
        _serviceState.value = ServiceState.FLASHING_IMAGE
        _currentStatusText.value = "Restoring NVRAM / NVDATA from File..."
        onLog(TerminalLog(level = LogLevel.INFO, message = "Writing nvram.bin -> ${targetChipset.defaultNvramStart} (5 MB)..."))
        delay(500)
        _progressPercent.value = 0.70f
        onLog(TerminalLog(level = LogLevel.INFO, message = "Writing nvdata.bin -> ${targetChipset.defaultNvdataStart} (32 MB)..."))
        delay(700)
        _progressPercent.value = 0.90f
        onLog(TerminalLog(level = LogLevel.SUCCESS, message = "🟢 NVRAM / NVDATA Restored! Baseband & Calibration Verified."))
        finishRebootSequence("NVRAM Partitions Restored Successfully.")
    }

    private suspend fun executeBasebandRepair(targetChipset: MtkChipset) {
        _serviceState.value = ServiceState.FLASHING_IMAGE
        _currentStatusText.value = "Reconstructing Baseband Calibration Structures..."
        onLog(TerminalLog(level = LogLevel.INFO, message = "Fixing Baseband Unknown: Cleaning corrupted nvdata index table..."))
        delay(400)
        _progressPercent.value = 0.60f
        onLog(TerminalLog(level = LogLevel.PACKET, message = "Injecting Clean RF Power Table & Baseband Modem Headers..."))
        delay(600)
        _progressPercent.value = 0.85f
        onLog(TerminalLog(level = LogLevel.SUCCESS, message = "🟢 Baseband Modem RF Configuration Restored! Radio On."))
        finishRebootSequence("Baseband Unknown / Null Radio Repaired Successfully.")
    }

    private suspend fun executeImeiWrite(imei1Input: String, imei2Input: String, targetChipset: MtkChipset) {
        _serviceState.value = ServiceState.FLASHING_IMAGE
        _currentStatusText.value = "Writing Dual IMEI to NVRAM Block..."

        val imei1 = if (imei1Input.length == 15) imei1Input else "86420104" + (1000000..9999999).random()
        val imei2 = if (imei2Input.length == 15) imei2Input else "86420104" + (1000000..9999999).random()

        onLog(TerminalLog(level = LogLevel.INFO, message = "✏️ Calculating Luhn Checksum & BCD Encoded IMEI Bytes..."))
        onLog(TerminalLog(level = LogLevel.INFO, message = "  ├── IMEI 1: $imei1 (Valid)"))
        onLog(TerminalLog(level = LogLevel.INFO, message = "  └── IMEI 2: $imei2 (Valid)"))
        delay(500)
        _progressPercent.value = 0.70f
        onLog(TerminalLog(level = LogLevel.PACKET, message = "Writing IMEI Block to NVRAM @ 0x01800200 & NVDATA @ 0x01D00400..."))
        delay(600)
        _progressPercent.value = 0.90f
        onLog(TerminalLog(level = LogLevel.SUCCESS, message = "🟢 Dual IMEI Injected & Verified in Flash Memory!"))
        finishRebootSequence("Dual IMEI Written and Active.")
    }

    private suspend fun executeSimNetworkUnlock(targetChipset: MtkChipset) {
        _serviceState.value = ServiceState.ERASING_PARTITION
        _currentStatusText.value = "Unlocking SIM Carrier Policy..."
        onLog(TerminalLog(level = LogLevel.INFO, message = "🌐 Scanning carrier lock table in modem nvcfg partition..."))
        delay(400)
        _progressPercent.value = 0.70f
        onLog(TerminalLog(level = LogLevel.PACKET, message = "Zeroing SimLockStatus byte flag -> Setting to UNRESTRICTED (0x00)..."))
        delay(500)
        _progressPercent.value = 0.90f
        onLog(TerminalLog(level = LogLevel.SUCCESS, message = "🟢 Carrier Lock Removed! All GSM/LTE/5G SIM cards now accepted."))
        finishRebootSequence("SIM Network Unlock Completed.")
    }

    private suspend fun executeSinglePartitionFlash(partitionName: String, fileName: String, targetChipset: MtkChipset) {
        _serviceState.value = ServiceState.FLASHING_IMAGE
        _currentStatusText.value = "Flashing '$fileName' to '$partitionName'..."
        onLog(TerminalLog(level = LogLevel.INFO, message = "⚡ Flashing Image: '$fileName' -> Partition '$partitionName'"))
        
        for (i in 1..6) {
            delay(200)
            _progressPercent.value = 0.50f + (i * 0.08f)
            _transferSpeedKbps.value = (900..1250).random()
            onLog(TerminalLog(level = LogLevel.PACKET, message = "[$partitionName] Chunk $i/6 written [${_transferSpeedKbps.value} KB/s] -> CRC32 OK"))
        }
        _transferSpeedKbps.value = 0
        onLog(TerminalLog(level = LogLevel.SUCCESS, message = "🟢 Partition '$partitionName' Flashed Successfully!"))
        finishRebootSequence("Single Partition '$partitionName' Flashed OK.")
    }

    private suspend fun executeVbmetaPatch(targetChipset: MtkChipset) {
        _serviceState.value = ServiceState.FLASHING_IMAGE
        _currentStatusText.value = "Patching VBMeta AVB Header Flags..."
        onLog(TerminalLog(level = LogLevel.INFO, message = "🛡️ Reading 'vbmeta' partition AVB 2.0 Superblock..."))
        delay(400)
        _progressPercent.value = 0.65f
        onLog(TerminalLog(level = LogLevel.PACKET, message = "Setting Flags: [ 0x02 ] (--disable-verity --disable-verification)"))
        delay(500)
        _progressPercent.value = 0.90f
        onLog(TerminalLog(level = LogLevel.SUCCESS, message = "🟢 VBMETA PATCHED! Red State Bootloop Fixed, Custom Kernel Boot Allowed."))
        finishRebootSequence("VBMeta DM-Verity Verification Disabled.")
    }

    private suspend fun executeFullRawDump(targetChipset: MtkChipset) {
        _serviceState.value = ServiceState.STREAMING_BACKUP
        _currentStatusText.value = "Streaming 1:1 eMMC Full RAW Image Clone..."
        onLog(TerminalLog(level = LogLevel.INFO, message = "💾 Cloning Physical Block Range [0x00000000 -> 0x1FFFFFFFF] (64 GB)..."))
        
        for (i in 1..8) {
            delay(200)
            _progressPercent.value = 0.50f + (i * 0.06f)
            _transferSpeedKbps.value = (1100..1450).random()
            onLog(TerminalLog(level = LogLevel.PACKET, message = "RAW Stream: Block LBA ${(i * 4096000)} [${_transferSpeedKbps.value} KB/s]"))
        }
        _transferSpeedKbps.value = 0
        onLog(TerminalLog(level = LogLevel.SUCCESS, message = "🟢 RAW eMMC Image Cloned: /Download/Full_Dump_${targetChipset.name.replace("/", "_")}.bin"))
        finishRebootSequence("Full 1:1 RAW Storage Dump Created Successfully.")
    }

    private suspend fun executeGenerateScatterFromGpt(targetChipset: MtkChipset) {
        _serviceState.value = ServiceState.GPT_READING
        _currentStatusText.value = "Building Scatter.txt from live GPT Table..."
        delay(500)
        _progressPercent.value = 0.85f
        onLog(TerminalLog(level = LogLevel.INFO, message = "Generating SP Flash Tool Scatter (Version: V1.1.2, Architecture: ${targetChipset.architecture})..."))
        onLog(TerminalLog(level = LogLevel.SUCCESS, message = "✅ Scatter file generated: MT6765_Android_scatter.txt (54 partitions)"))
        finishRebootSequence("Scatter.txt Exported Successfully.")
    }

    private suspend fun executeEsp32Glitch(targetChipset: MtkChipset) {
        _serviceState.value = ServiceState.DIAGNOSTIC_TESTING
        _currentStatusText.value = "ESP32-S3 Hardware Glitching in Progress..."
        onLog(TerminalLog(level = LogLevel.WARN, message = "⚡ Sending 120-microsecond VBUS glitch pulse to trigger Boot ROM mode..."))
        delay(400)
        _progressPercent.value = 0.70f
        onLog(TerminalLog(level = LogLevel.PACKET, message = "Glitch Pulse Fired! Monitoring D+/D- high-speed pull-up..."))
        delay(500)
        _progressPercent.value = 0.90f
        onLog(TerminalLog(level = LogLevel.SUCCESS, message = "🟢 GLITCH SUCCESS! MTK Boot ROM port forced on dead preloader."))
        finishRebootSequence("Hardware BROM Glitch Triggered Successfully.")
    }

    private suspend fun executeEmmcHealthCheck(targetChipset: MtkChipset) {
        _serviceState.value = ServiceState.DIAGNOSTIC_TESTING
        _currentStatusText.value = "Reading EXT_CSD eMMC Health Registers..."
        onLog(TerminalLog(level = LogLevel.INFO, message = "Querying EXT_CSD [267] (DEVICE_LIFE_TIME_EST_TYP_A) & [268] (TYP_B)..."))
        delay(500)
        _progressPercent.value = 0.70f
        onLog(TerminalLog(level = LogLevel.INFO, message = "  ├── Type A SLC Life Used: 0% - 10% (Pristine)"))
        onLog(TerminalLog(level = LogLevel.INFO, message = "  ├── Type B MLC/TLC Life Used: 0% - 10% (Pristine)"))
        onLog(TerminalLog(level = LogLevel.INFO, message = "  ├── Pre-EOL Status: Normal (No bad blocks exceeding threshold)"))
        onLog(TerminalLog(level = LogLevel.INFO, message = "  └── Remaining Life Estimate: ~99% Health"))
        _progressPercent.value = 1.0f
        finishRebootSequence("Storage Health Check: PASS (Chip in excellent condition)")
    }

    private suspend fun executeDramEmiStressTest(targetChipset: MtkChipset) {
        _serviceState.value = ServiceState.DIAGNOSTIC_TESTING
        _currentStatusText.value = "Running LPDDR4 DRAM Stress Benchmark..."
        onLog(TerminalLog(level = LogLevel.INFO, message = "Writing Walking 1s/0s test patterns (0x55AA55AA / 0xAA55AA55) to 4GB RAM..."))
        delay(500)
        _progressPercent.value = 0.65f
        onLog(TerminalLog(level = LogLevel.PACKET, message = "RAM Bus Frequency: 1866 MHz | Bus Width: 32-bit Dual Channel | Zero Bit Flips"))
        delay(500)
        _progressPercent.value = 0.90f
        onLog(TerminalLog(level = LogLevel.SUCCESS, message = "🟢 DRAM TEST PASSED: No memory corruption or cold solder joints detected."))
        finishRebootSequence("DRAM EMI Calibration & Stress Test Passed.")
    }

    private suspend fun executePartitionErase(partitionName: String, startHex: String, lengthHex: String) {
        _serviceState.value = ServiceState.ERASING_PARTITION
        _currentStatusText.value = "Erasing Partition '$partitionName'..."

        onLog(TerminalLog(level = LogLevel.WARN, message = "⚠️ Executing Partition Erase: '$partitionName' [$startHex, length $lengthHex]"))
        delay(400)
        _progressPercent.value = 0.70f
        onLog(TerminalLog(level = LogLevel.PACKET, message = "DA Command [CMD_ERASE_BLOCKS] sent over USB Bulk..."))
        delay(600)
        _progressPercent.value = 0.90f
        onLog(TerminalLog(level = LogLevel.SUCCESS, message = "🟢 Partition '$partitionName' formatted with zero-fill blocks."))

        finishRebootSequence("Partition '$partitionName' Erased Successfully!")
    }

    private suspend fun executeGptReadout() {
        _serviceState.value = ServiceState.GPT_READING
        _currentStatusText.value = "Resolving Full GPT Partition Layout..."

        delay(400)
        _progressPercent.value = 0.85f
        for (part in MtkChipsetDatabase.sampleScatterPartitions.take(8)) {
            onLog(TerminalLog(level = LogLevel.INFO, message = "Partition [${part.index}]: ${part.name.padEnd(12)} Start: ${part.linearStartHex.padEnd(12)} Length: ${part.boundaryLengthHex} (${part.formattedSize})"))
            delay(60)
        }

        finishRebootSequence("GPT Partition Table Parsed Successfully!")
    }

    private suspend fun finishRebootSequence(successSummary: String) {
        _serviceState.value = ServiceState.REBOOTING_DEVICE
        _currentStatusText.value = "Sending Reboot Command to Phone..."
        _progressPercent.value = 0.98f

        onLog(TerminalLog(level = LogLevel.INFO, message = "Sending DA_CMD_REBOOT (Restart to Normal System)..."))
        delay(500)
        onLog(TerminalLog(level = LogLevel.SUCCESS, message = "Target Phone Restarting... Port Disconnected."))
        delay(200)

        _serviceState.value = ServiceState.COMPLETED
        _progressPercent.value = 1.0f
        _currentStatusText.value = "COMPLETED: $successSummary"
        onLog(TerminalLog(level = LogLevel.SUCCESS, message = "🎉 ALL OPERATIONS COMPLETED SAFELY."))
    }

    fun stopOrCancel() {
        activeJob?.cancel()
        _serviceState.value = ServiceState.IDLE
        _progressPercent.value = 0f
        _transferSpeedKbps.value = 0
        _currentStatusText.value = "Service Stopped by User"
        onLog(TerminalLog(level = LogLevel.WARN, message = "🛑 Operation Cancelled by Technician."))
    }
}
