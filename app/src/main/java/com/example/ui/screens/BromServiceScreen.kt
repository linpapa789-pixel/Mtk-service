package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MtkChipsetDatabase
import com.example.model.DaPreloaderConfig
import com.example.model.LogLevel
import com.example.model.MtkChipset
import com.example.model.MtkOperation
import com.example.model.MtkOperationCategory
import com.example.model.ServiceState
import com.example.model.TerminalLog
import com.example.ui.components.FullscreenTerminalDialog
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.CardSlateBorder
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedDanger
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalPurple
import com.example.ui.theme.TerminalRed
import com.example.ui.theme.TerminalYellow
import com.example.viewmodel.MtkWorkshopViewModel

@Composable
fun BromServiceScreen(
    viewModel: MtkWorkshopViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val serviceState by viewModel.bromEngine.serviceState.collectAsState()
    val progressPercent by viewModel.bromEngine.progressPercent.collectAsState()
    val statusText by viewModel.bromEngine.currentStatusText.collectAsState()
    val transferSpeedKbps by viewModel.bromEngine.transferSpeedKbps.collectAsState()
    val selectedChipset by viewModel.bromEngine.selectedChipset.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedOperation by viewModel.selectedOperation.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val logFilter by viewModel.logFilter.collectAsState()

    val isRunning = serviceState != ServiceState.IDLE &&
            serviceState != ServiceState.COMPLETED &&
            serviceState != ServiceState.FAILED

    // Brand and Model states for Quick Selection
    val brands = listOf("Xiaomi / Redmi / POCO", "Samsung (MTK)", "Oppo / Realme", "Vivo / iQOO", "Infinix / Tecno", "Generic MediaTek")
    var selectedBrand by remember { mutableStateOf(brands[0]) }
    var showBrandDropdown by remember { mutableStateOf(false) }

    var showChipsetDropdown by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showOperationDropdown by remember { mutableStateOf(false) }

    // Dynamic Parameter Inputs
    var imei1Input by remember { mutableStateOf("86420104" + (1000000..9999999).random().toString()) }
    var imei2Input by remember { mutableStateOf("86420104" + (1000000..9999999).random().toString()) }
    var selectedPartitionInput by remember { mutableStateOf("boot") }
    var flashImageFileInput by remember { mutableStateOf("boot_magisk_patched.img") }

    // Custom DA & Preloader State
    val daConfig by viewModel.bromEngine.daPreloaderConfig.collectAsState()
    var isDaConfigExpanded by remember { mutableStateOf(false) }
    var showDaDropdown by remember { mutableStateOf(false) }
    var showPreloaderDropdown by remember { mutableStateOf(false) }
    var showAuthDropdown by remember { mutableStateOf(false) }
    var customDaInput by remember { mutableStateOf(daConfig.customDaPath) }
    var customPreloaderInput by remember { mutableStateOf(daConfig.customPreloaderPath) }
    var customAuthInput by remember { mutableStateOf(daConfig.customAuthPath) }

    var showFullscreenTerminal by remember { mutableStateOf(false) }

    // Filter operations by category
    val categoryOperations = remember(selectedCategory) {
        val cat = if (selectedCategory == MtkOperationCategory.ALL) MtkOperationCategory.SECURITY_LOCKS else selectedCategory
        MtkOperation.values().filter { it.category == cat && it.category != MtkOperationCategory.FASTBOOT_ADB }
    }

    // Ensure selected operation belongs to current category
    LaunchedEffect(selectedCategory) {
        if (categoryOperations.isNotEmpty() && !categoryOperations.contains(selectedOperation)) {
            viewModel.setSelectedOperation(categoryOperations.first())
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // =========================================================================
        // 1. TOP COCKPIT CONTROLS: BRAND / MODEL & CATEGORY / FUNCTION DROP-BOXES
        // =========================================================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("cockpit_selector_card"),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header Label Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DeveloperBoard,
                            contentDescription = "Control",
                            tint = BluePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "BROM SERVICE CONTROL COCKPIT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            letterSpacing = 0.5.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFDCFCE7),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(GreenSuccess))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AUTH BYPASS READY",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenSuccess
                            )
                        }
                    }
                }

                // Row 1: Brand Dropdown & Model/SoC Dropdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Brand Selector Box
                    Box(modifier = Modifier.weight(1f)) {
                        CockpitDropdownSelector(
                            label = "TARGET BRAND",
                            value = selectedBrand,
                            icon = Icons.Default.PhoneAndroid,
                            onClick = { showBrandDropdown = true }
                        )
                        DropdownMenu(
                            expanded = showBrandDropdown,
                            onDismissRequest = { showBrandDropdown = false }
                        ) {
                            brands.forEach { brand ->
                                DropdownMenuItem(
                                    text = { Text(brand, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        selectedBrand = brand
                                        showBrandDropdown = false
                                        viewModel.appendLog(TerminalLog(level = LogLevel.INFO, message = "🏷️ Brand profile switched: $brand"))
                                    }
                                )
                            }
                        }
                    }

                    // SoC / Model Selector Box
                    Box(modifier = Modifier.weight(1.2f)) {
                        CockpitDropdownSelector(
                            label = "CHIPSET / SOC",
                            value = "${selectedChipset.name} (${selectedChipset.architecture})",
                            icon = Icons.Default.Memory,
                            onClick = { showChipsetDropdown = true }
                        )
                        DropdownMenu(
                            expanded = showChipsetDropdown,
                            onDismissRequest = { showChipsetDropdown = false }
                        ) {
                            MtkChipsetDatabase.supportedChipsets.forEach { chip ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = "${chip.name} (${chip.popularPhones})",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = "HW: ${chip.hwCodeHex} | ${chip.architecture} | ${chip.slaType}",
                                                fontSize = 10.sp,
                                                color = Color(0xFF64748B),
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.bromEngine.setSelectedChipset(chip)
                                        showChipsetDropdown = false
                                        Toast.makeText(context, "SoC Profile: ${chip.name}", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }

                // Row 2: Service Category Dropdown & Specific Function Dropdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category Dropdown Box
                    val categories = listOf(
                        MtkOperationCategory.SECURITY_LOCKS,
                        MtkOperationCategory.IMEI_BASEBAND,
                        MtkOperationCategory.FLASH_PARTITION,
                        MtkOperationCategory.HARDWARE_HEALTH
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        CockpitDropdownSelector(
                            label = "CATEGORY",
                            value = selectedCategory.displayName,
                            icon = getCategoryIcon(selectedCategory),
                            onClick = { showCategoryDropdown = true }
                        )
                        DropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = getCategoryIcon(cat),
                                            contentDescription = null,
                                            tint = BluePrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    text = { Text(cat.displayName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                    onClick = {
                                        viewModel.setSelectedCategory(cat)
                                        showCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Function Dropdown Box
                    Box(modifier = Modifier.weight(1.3f)) {
                        CockpitDropdownSelector(
                            label = "SELECTED FUNCTION",
                            value = selectedOperation.title,
                            icon = getOperationIcon(selectedOperation),
                            onClick = { showOperationDropdown = true }
                        )
                        DropdownMenu(
                            expanded = showOperationDropdown,
                            onDismissRequest = { showOperationDropdown = false }
                        ) {
                            categoryOperations.forEach { op ->
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = getOperationIcon(op),
                                            contentDescription = null,
                                            tint = if (op.isDestructive) RedDanger else BluePrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    text = {
                                        Column {
                                            Text(
                                                text = op.title,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (op.isDestructive) RedDanger else Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = op.description,
                                                fontSize = 10.sp,
                                                color = Color(0xFF64748B),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.setSelectedOperation(op)
                                        showOperationDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 2. CUSTOM DA / PRELOADER / AUTH SELECTION BOX
        // =========================================================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("custom_da_preloader_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Clickable Header to Expand / Collapse
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isDaConfigExpanded = !isDaConfigExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE0E7FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = Color(0xFF4338CA),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "CUSTOM DA & PRELOADER CONFIGURATION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "DA: ${daConfig.selectedDa.take(22)}... | PL: ${daConfig.selectedPreloader.take(18)}...",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (daConfig.selectedDa.startsWith("Custom") || daConfig.selectedPreloader.startsWith("Custom")) Color(0xFFFEF3C7) else Color(0xFFE0E7FF)
                        ) {
                            Text(
                                text = if (daConfig.selectedDa.startsWith("Custom") || daConfig.selectedPreloader.startsWith("Custom")) "CUSTOM" else "AUTO/STD",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (daConfig.selectedDa.startsWith("Custom") || daConfig.selectedPreloader.startsWith("Custom")) Color(0xFFB45309) else Color(0xFF4338CA),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (isDaConfigExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                AnimatedVisibility(visible = isDaConfigExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Download Agent (DA) Dropdown Box
                        Box(modifier = Modifier.fillMaxWidth()) {
                            CockpitDropdownSelector(
                                label = "DOWNLOAD AGENT (DA)",
                                value = daConfig.selectedDa,
                                icon = Icons.Default.Download,
                                onClick = { showDaDropdown = true }
                            )
                            DropdownMenu(
                                expanded = showDaDropdown,
                                onDismissRequest = { showDaDropdown = false }
                            ) {
                                DaPreloaderConfig.DA_PRESETS.forEach { daPreset ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = daPreset,
                                                fontSize = 12.sp,
                                                fontWeight = if (daConfig.selectedDa == daPreset) FontWeight.Bold else FontWeight.Normal,
                                                color = if (daConfig.selectedDa == daPreset) BluePrimary else Color(0xFF0F172A)
                                            )
                                        },
                                        onClick = {
                                            viewModel.bromEngine.updateDaPreloaderConfig(daConfig.copy(selectedDa = daPreset))
                                            showDaDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Custom DA File Path Input (if Custom DA is selected)
                        if (daConfig.selectedDa.startsWith("Custom")) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = customDaInput,
                                    onValueChange = {
                                        customDaInput = it
                                        viewModel.bromEngine.updateDaPreloaderConfig(daConfig.copy(customDaPath = it))
                                    },
                                    label = { Text("Custom DA Path (.bin)", fontSize = 10.sp) },
                                    placeholder = { Text("/sdcard/Download/MTK_Custom_DA.bin", fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BluePrimary,
                                        unfocusedBorderColor = CardSlateBorder
                                    )
                                )
                                OutlinedButton(
                                    onClick = {
                                        customDaInput = "/sdcard/Download/MTK_Custom_DA_${selectedChipset.name}.bin"
                                        viewModel.bromEngine.updateDaPreloaderConfig(daConfig.copy(customDaPath = customDaInput))
                                        Toast.makeText(context, "Custom DA File loaded!", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = "Browse", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Browse", fontSize = 11.sp)
                                }
                            }
                        }

                        // 2. Preloader Dropdown Box
                        Box(modifier = Modifier.fillMaxWidth()) {
                            CockpitDropdownSelector(
                                label = "PRELOADER BINARY",
                                value = daConfig.selectedPreloader,
                                icon = Icons.Default.Memory,
                                onClick = { showPreloaderDropdown = true }
                            )
                            DropdownMenu(
                                expanded = showPreloaderDropdown,
                                onDismissRequest = { showPreloaderDropdown = false }
                            ) {
                                DaPreloaderConfig.PRELOADER_PRESETS.forEach { plPreset ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = plPreset,
                                                fontSize = 12.sp,
                                                fontWeight = if (daConfig.selectedPreloader == plPreset) FontWeight.Bold else FontWeight.Normal,
                                                color = if (daConfig.selectedPreloader == plPreset) BluePrimary else Color(0xFF0F172A)
                                            )
                                        },
                                        onClick = {
                                            viewModel.bromEngine.updateDaPreloaderConfig(daConfig.copy(selectedPreloader = plPreset))
                                            showPreloaderDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Custom Preloader File Path Input (if Custom Preloader is selected)
                        if (daConfig.selectedPreloader.startsWith("Custom")) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = customPreloaderInput,
                                    onValueChange = {
                                        customPreloaderInput = it
                                        viewModel.bromEngine.updateDaPreloaderConfig(daConfig.copy(customPreloaderPath = it))
                                    },
                                    label = { Text("Custom Preloader Path (.bin)", fontSize = 10.sp) },
                                    placeholder = { Text("/sdcard/Download/preloader_${selectedChipset.name.lowercase()}.bin", fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BluePrimary,
                                        unfocusedBorderColor = CardSlateBorder
                                    )
                                )
                                OutlinedButton(
                                    onClick = {
                                        customPreloaderInput = "/sdcard/Download/preloader_${selectedChipset.name.lowercase()}.bin"
                                        viewModel.bromEngine.updateDaPreloaderConfig(daConfig.copy(customPreloaderPath = customPreloaderInput))
                                        Toast.makeText(context, "Custom Preloader loaded!", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = "Browse", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Browse", fontSize = 11.sp)
                                }
                            }
                        }

                        // 3. Auth / SLA / DAA Dropdown Box
                        Box(modifier = Modifier.fillMaxWidth()) {
                            CockpitDropdownSelector(
                                label = "AUTH / SLA / DAA TOKEN",
                                value = daConfig.selectedAuth,
                                icon = Icons.Default.Key,
                                onClick = { showAuthDropdown = true }
                            )
                            DropdownMenu(
                                expanded = showAuthDropdown,
                                onDismissRequest = { showAuthDropdown = false }
                            ) {
                                DaPreloaderConfig.AUTH_PRESETS.forEach { authPreset ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = authPreset,
                                                fontSize = 12.sp,
                                                fontWeight = if (daConfig.selectedAuth == authPreset) FontWeight.Bold else FontWeight.Normal,
                                                color = if (daConfig.selectedAuth == authPreset) BluePrimary else Color(0xFF0F172A)
                                            )
                                        },
                                        onClick = {
                                            viewModel.bromEngine.updateDaPreloaderConfig(daConfig.copy(selectedAuth = authPreset))
                                            showAuthDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Hardware Switches
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bypass SLA/DAA via Hardware Glitch",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF334155)
                            )
                            Switch(
                                checked = daConfig.bypassSlaDaaSecurity,
                                onCheckedChange = {
                                    viewModel.bromEngine.updateDaPreloaderConfig(daConfig.copy(bypassSlaDaaSecurity = it))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = BluePrimary,
                                    checkedTrackColor = Color(0xFFDBEAFE)
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "High-Speed DRAM Controller Init",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF334155)
                            )
                            Switch(
                                checked = daConfig.enableHighSpeedDramInit,
                                onCheckedChange = {
                                    viewModel.bromEngine.updateDaPreloaderConfig(daConfig.copy(enableHighSpeedDramInit = it))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = GreenSuccess,
                                    checkedTrackColor = Color(0xFFDCFCE7)
                                )
                            )
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 3. CONTEXT-AWARE FUNCTION ACTION & PARAMETER EXECUTION CARD
        // =========================================================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("operation_executor_card"),
            colors = CardDefaults.cardColors(
                containerColor = if (isRunning) Color(0xFFF0FDF4) else Color(0xFFFFFFFF)
            ),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isRunning) Color(0xFF86EFAC) else CardSlateBorder
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Operation Info & Target Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedOperation.isDestructive) Color(0xFFFEF2F2) else Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getOperationIcon(selectedOperation),
                                contentDescription = selectedOperation.title,
                                tint = if (selectedOperation.isDestructive) RedDanger else BluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedOperation.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (selectedOperation.isDestructive) Color(0xFFFEE2E2) else Color(0xFFDBEAFE)
                                ) {
                                    Text(
                                        text = if (selectedOperation.isDestructive) "WIPE" else "BROM",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedOperation.isDestructive) RedDanger else BluePrimary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = selectedOperation.description,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // SLA & Status Badge
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Mode: BROM Direct",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "SLA: Bypass OK",
                            fontSize = 9.sp,
                            color = GreenSuccess,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Dynamic Input 1: Dual IMEI Repair Inputs (If WRITE_DUAL_IMEI selected)
                if (selectedOperation == MtkOperation.WRITE_DUAL_IMEI) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = imei1Input,
                                onValueChange = { if (it.length <= 15) imei1Input = it },
                                label = { Text("IMEI 1 (SIM 1)", fontSize = 10.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BluePrimary,
                                    unfocusedBorderColor = CardSlateBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("imei1_input")
                            )

                            OutlinedTextField(
                                value = imei2Input,
                                onValueChange = { if (it.length <= 15) imei2Input = it },
                                label = { Text("IMEI 2 (SIM 2)", fontSize = 10.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BluePrimary,
                                    unfocusedBorderColor = CardSlateBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("imei2_input")
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                imei1Input = "86420104" + (1000000..9999999).random().toString()
                                imei2Input = "86420104" + (1000000..9999999).random().toString()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Gen", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Auto-Generate Valid Test Pair (Luhn Checksum)", fontSize = 11.sp)
                        }
                    }
                }

                // Dynamic Input 2: Single Partition Flash Inputs (If FLASH_SINGLE_PARTITION selected)
                if (selectedOperation == MtkOperation.FLASH_SINGLE_PARTITION) {
                    val partitions = listOf("boot", "recovery", "vbmeta", "dtbo", "super")
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            partitions.forEach { part ->
                                val isSel = selectedPartitionInput == part
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isSel) BluePrimary else Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) BluePrimary else CardSlateBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            selectedPartitionInput = part
                                            flashImageFileInput = "${part}.img"
                                        }
                                ) {
                                    Text(
                                        text = part,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.White else Color(0xFF1E293B),
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = flashImageFileInput,
                            onValueChange = { flashImageFileInput = it },
                            label = { Text("Image File Name / Path", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Active Progress Bar when running
                if (isRunning) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = statusText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary
                            )
                            Text(
                                text = "${(progressPercent * 100).toInt()}% (${transferSpeedKbps} KB/s)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        LinearProgressIndicator(
                            progress = { progressPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = BluePrimary,
                            trackColor = Color(0xFFE2E8F0)
                        )
                    }
                }

                // BIG PRIMARY EXECUTE / CANCEL BUTTON
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isRunning) {
                        Button(
                            onClick = { viewModel.stopBromService() },
                            colors = ButtonDefaults.buttonColors(containerColor = RedDanger),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("stop_operation_button")
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "STOP / CANCEL OPERATION",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                when (selectedOperation) {
                                    MtkOperation.WRITE_DUAL_IMEI -> viewModel.startBromService(selectedOperation, imei1Input, imei2Input)
                                    MtkOperation.FLASH_SINGLE_PARTITION -> viewModel.startBromService(selectedOperation, selectedPartitionInput, flashImageFileInput)
                                    else -> viewModel.startBromService(selectedOperation)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedOperation.isDestructive) RedDanger else BluePrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("start_operation_button")
                        ) {
                            Icon(
                                imageVector = if (selectedOperation.isDestructive) Icons.Default.Key else Icons.Default.PlayArrow,
                                contentDescription = "Start",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EXECUTE: ${selectedOperation.title.uppercase()}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 3. MASSIVE DEDICATED LIVE TERMINAL CONSOLE (MAIN STAGE)
        // =========================================================================
        CockpitLiveTerminalConsole(
            logs = logs,
            activeFilter = logFilter,
            onFilterChange = { viewModel.setLogFilter(it) },
            onClearLogs = { viewModel.clearLogs() },
            onOpenFullscreen = { showFullscreenTerminal = true },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("cockpit_live_terminal_box")
        )
    }

    // Fullscreen Terminal Modal
    if (showFullscreenTerminal) {
        FullscreenTerminalDialog(
            logs = logs,
            activeFilter = logFilter,
            onFilterChange = { viewModel.setLogFilter(it) },
            onClearLogs = { viewModel.clearLogs() },
            onDismiss = { showFullscreenTerminal = false }
        )
    }
}

/**
 * Reusable Dropdown Trigger Button for Cockpit Selectors
 */
@Composable
fun CockpitDropdownSelector(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 7.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = BluePrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = label,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = value,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Dropdown",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * High-Priority Dedicated Live Terminal Console
 */
@Composable
fun CockpitLiveTerminalConsole(
    logs: List<TerminalLog>,
    activeFilter: LogLevel?,
    onFilterChange: (LogLevel?) -> Unit,
    onClearLogs: () -> Unit,
    onOpenFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    val filteredLogs = remember(logs, activeFilter) {
        if (activeFilter != null) {
            logs.filter { it.level == activeFilter }
        } else {
            logs
        }
    }

    LaunchedEffect(filteredLogs.size) {
        if (filteredLogs.isNotEmpty()) {
            listState.animateScrollToItem(filteredLogs.size - 1)
        }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = TerminalBg),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Terminal Header with Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(GreenSuccess)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE HANDSHAKE LOG",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            text = "${filteredLogs.size} lines",
                            fontSize = 9.sp,
                            color = Color(0xFF38BDF8),
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                // Quick Tool Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val allLogsText = logs.joinToString("\n") { "[${it.timestamp}] ${it.level}: ${it.message}" }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Terminal Logs", allLogsText))
                            Toast.makeText(context, "All logs copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF94A3B8), modifier = Modifier.size(15.dp))
                    }

                    IconButton(
                        onClick = onClearLogs,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ClearAll, contentDescription = "Clear", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = onOpenFullscreen,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Fullscreen, contentDescription = "Fullscreen", tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Real-Time Log Messages Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                if (filteredLogs.isEmpty()) {
                    item {
                        Text(
                            text = "[IDLE] Port listening... Connect device in BROM / Preloader mode (Hold Vol+ & Vol- and insert USB-C cable).",
                            color = Color(0xFF64748B),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                } else {
                    items(filteredLogs) { log ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "[${log.timestamp}] ",
                                color = Color(0xFF475569),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal
                            )

                            val levelColor = when (log.level) {
                                LogLevel.INFO -> TerminalCyan
                                LogLevel.SUCCESS -> TerminalGreen
                                LogLevel.WARN -> TerminalYellow
                                LogLevel.ERROR -> TerminalRed
                                LogLevel.COMMAND -> Color(0xFFF472B6)
                                LogLevel.PACKET -> TerminalPurple
                            }

                            Text(
                                text = log.message,
                                color = levelColor,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = if (log.level == LogLevel.SUCCESS || log.level == LogLevel.ERROR) FontWeight.Bold else FontWeight.Normal,
                                lineHeight = 15.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getCategoryIcon(cat: MtkOperationCategory): ImageVector {
    return when (cat) {
        MtkOperationCategory.ALL -> Icons.Default.DeveloperBoard
        MtkOperationCategory.SECURITY_LOCKS -> Icons.Default.Security
        MtkOperationCategory.IMEI_BASEBAND -> Icons.Default.SimCard
        MtkOperationCategory.FLASH_PARTITION -> Icons.Default.Storage
        MtkOperationCategory.HARDWARE_HEALTH -> Icons.Default.HealthAndSafety
        MtkOperationCategory.FASTBOOT_ADB -> Icons.Default.Build
    }
}

fun getOperationIcon(op: MtkOperation): ImageVector {
    return when (op) {
        MtkOperation.UNLOCK_BOOTLOADER_SECCFG -> Icons.Default.LockOpen
        MtkOperation.LOCK_BOOTLOADER_SECCFG -> Icons.Default.Shield
        MtkOperation.MI_ACCOUNT_RESET -> Icons.Default.Cloud
        MtkOperation.PERMANENT_DEMO_REMOVE -> Icons.Default.Smartphone
        MtkOperation.ERASE_FRP -> Icons.Default.Key
        MtkOperation.ERASE_USERDATA -> Icons.Default.VpnKey
        MtkOperation.RPMB_READ_KEYS -> Icons.Default.Security
        MtkOperation.BACKUP_NVRAM -> Icons.Default.Shield
        MtkOperation.RESTORE_NVRAM -> Icons.Default.Download
        MtkOperation.REPAIR_BASEBAND_UNKNOWN -> Icons.Default.SimCard
        MtkOperation.WRITE_DUAL_IMEI -> Icons.Default.Edit
        MtkOperation.SIM_NETWORK_UNLOCK -> Icons.Default.LockOpen
        MtkOperation.READ_GPT -> Icons.Default.Storage
        MtkOperation.FLASH_SINGLE_PARTITION -> Icons.Default.FlashOn
        MtkOperation.PATCH_VBMETA_DMVERITY -> Icons.Default.Shield
        MtkOperation.DUMP_FULL_RAW_EMMC -> Icons.Default.Storage
        MtkOperation.GENERATE_SCATTER_FROM_GPT -> Icons.Default.Storage
        MtkOperation.ESP32_FORCE_BROM_GLITCH -> Icons.Default.FlashOn
        MtkOperation.EMMC_HEALTH_CHECK -> Icons.Default.HealthAndSafety
        MtkOperation.DRAM_EMI_STRESS_TEST -> Icons.Default.Memory
        else -> Icons.Default.Build
    }
}
