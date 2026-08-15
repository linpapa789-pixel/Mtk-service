package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MtkChipsetDatabase
import com.example.data.XiaomiDeviceEntry
import com.example.data.XiaomiKernelDatabase
import com.example.engine.Esp32FlasherEngine
import com.example.engine.MtkBromBridgeEngine
import com.example.engine.ScatterMathParser
import com.example.engine.UsbFastbootAdbEngine
import com.example.model.BridgeConnectionMode
import com.example.model.DeviceInfo
import com.example.model.LogLevel
import com.example.model.MtkChipset
import com.example.model.MtkOperation
import com.example.model.MtkOperationCategory
import com.example.model.PartitionEntry
import com.example.model.ServiceState
import com.example.model.TerminalLog
import com.example.service.GeminiDiagnosticService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppTab(val title: String, val iconName: String) {
    BROM_SERVICE("BROM Suite", "Memory"),
    ESP32_FLASHER("ESP32-S3 N16R8", "DeveloperBoard"),
    PARTITIONS_FLASHER("Partition & Flash", "Storage"),
    XIAOMI_DATABASE("Xiaomi & SoC Lib", "Dns"),
    FASTBOOT_ADB("Fastboot / ADB", "Usb"),
    AI_DIAGNOSTICS("AI Diagnostics", "AutoAwesome")
}

class MtkWorkshopViewModel(application: Application) : AndroidViewModel(application) {

    private val _logs = MutableStateFlow<List<TerminalLog>>(emptyList())
    val logs = _logs.asStateFlow()

    private val _activeTab = MutableStateFlow(AppTab.BROM_SERVICE)
    val activeTab = _activeTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow(MtkOperationCategory.ALL)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedOperation = MutableStateFlow(MtkOperation.UNLOCK_BOOTLOADER_SECCFG)
    val selectedOperation = _selectedOperation.asStateFlow()

    private val _scatterPartitions = MutableStateFlow(MtkChipsetDatabase.sampleScatterPartitions)
    val scatterPartitions = _scatterPartitions.asStateFlow()

    private val _aiDiagnosticResult = MutableStateFlow<String?>(null)
    val aiDiagnosticResult = _aiDiagnosticResult.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading = _isAiLoading.asStateFlow()

    private val _logFilter = MutableStateFlow<LogLevel?>(null)
    val logFilter = _logFilter.asStateFlow()

    // Xiaomi Database Search
    private val _xiaomiSearchQuery = MutableStateFlow("")
    val xiaomiSearchQuery = _xiaomiSearchQuery.asStateFlow()

    private val _xiaomiMtkFilterOnly = MutableStateFlow(false)
    val xiaomiMtkFilterOnly = _xiaomiMtkFilterOnly.asStateFlow()

    private val _selectedXiaomiDevice = MutableStateFlow<XiaomiDeviceEntry?>(null)
    val selectedXiaomiDevice = _selectedXiaomiDevice.asStateFlow()

    fun appendLog(log: TerminalLog) {
        _logs.value = (_logs.value + log).takeLast(400)
    }

    val bromEngine = MtkBromBridgeEngine(viewModelScope) { log ->
        appendLog(log)
    }

    val esp32Engine = Esp32FlasherEngine(viewModelScope) { log ->
        appendLog(log)
    }

    val fastbootEngine = UsbFastbootAdbEngine(application.applicationContext, viewModelScope) { log ->
        appendLog(log)
    }

    init {
        fastbootEngine.register()
        appendLog(
            TerminalLog(
                level = LogLevel.SUCCESS,
                message = "🟢 MTK Master Service Workshop initialized. All BROM, Exploit, NVRAM & Flash Modules Ready."
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        fastbootEngine.unregister()
    }

    fun setActiveTab(tab: AppTab) {
        _activeTab.value = tab
    }

    fun setSelectedCategory(cat: MtkOperationCategory) {
        _selectedCategory.value = cat
    }

    fun setSelectedOperation(op: MtkOperation) {
        _selectedOperation.value = op
    }

    fun setLogFilter(level: LogLevel?) {
        _logFilter.value = level
    }

    fun setXiaomiSearchQuery(query: String) {
        _xiaomiSearchQuery.value = query
    }

    fun setXiaomiMtkFilterOnly(mtkOnly: Boolean) {
        _xiaomiMtkFilterOnly.value = mtkOnly
    }

    fun selectXiaomiDevice(device: XiaomiDeviceEntry) {
        _selectedXiaomiDevice.value = device
        // Match to MtkChipset if possible
        val matchedChipset = MtkChipsetDatabase.supportedChipsets.firstOrNull {
            device.soc.lowercase().contains(it.hwCodeHex.lowercase()) ||
            device.soc.lowercase().contains(it.name.substring(0, 6).lowercase())
        }
        if (matchedChipset != null) {
            bromEngine.setSelectedChipset(matchedChipset)
        }
        appendLog(TerminalLog(level = LogLevel.INFO, message = "🎯 Target Device selected: ${device.devices} (${device.soc}) - Branch: ${device.branch}"))
    }

    fun clearLogs() {
        _logs.value = emptyList()
        appendLog(TerminalLog(level = LogLevel.INFO, message = "Terminal buffer cleared."))
    }

    fun startBromService(
        op: MtkOperation? = null,
        param1: String = "",
        param2: String = ""
    ) {
        val operationToRun = op ?: _selectedOperation.value
        bromEngine.startServiceOperation(
            operation = operationToRun,
            extraParam1 = param1,
            extraParam2 = param2
        )
    }

    fun stopBromService() {
        bromEngine.stopOrCancel()
    }

    fun executeFastbootAction(command: String) {
        fastbootEngine.executeFastbootCommand(
            command = command,
            onProgress = { pct, status -> },
            onComplete = { success, msg ->
                if (success) {
                    appendLog(TerminalLog(level = LogLevel.SUCCESS, message = "✅ Fastboot Action Finished: $msg"))
                } else {
                    appendLog(TerminalLog(level = LogLevel.ERROR, message = "❌ Fastboot Action Failed: $msg"))
                }
            }
        )
    }

    fun parseScatterText(text: String) {
        val parsed = ScatterMathParser.parseScatterContent(text)
        if (parsed.isNotEmpty()) {
            _scatterPartitions.value = parsed
            appendLog(TerminalLog(level = LogLevel.SUCCESS, message = "✅ Parsed ${parsed.size} partitions from scatter file."))
        } else {
            appendLog(TerminalLog(level = LogLevel.WARN, message = "⚠️ Could not parse scatter partitions from provided text."))
        }
    }

    fun runAiDiagnosis(errorCode: String, chipset: String, operation: String) {
        _isAiLoading.value = true
        _aiDiagnosticResult.value = null
        appendLog(TerminalLog(level = LogLevel.INFO, message = "🤖 Querying Gemini AI Repair Diagnostics for: $errorCode..."))

        viewModelScope.launch {
            val result = GeminiDiagnosticService.diagnoseMtkError(errorCode, chipset, operation)
            _isAiLoading.value = false
            result.onSuccess { diagnosis ->
                _aiDiagnosticResult.value = diagnosis
                appendLog(TerminalLog(level = LogLevel.SUCCESS, message = "🤖 AI Diagnosis Received."))
            }.onFailure { error ->
                _aiDiagnosticResult.value = "Diagnosis Error: ${error.message}\n\nPlease verify your Gemini API key in the Secrets panel."
                appendLog(TerminalLog(level = LogLevel.ERROR, message = "❌ AI Diagnosis Failed: ${error.message}"))
            }
        }
    }
}
