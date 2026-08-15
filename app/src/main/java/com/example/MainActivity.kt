package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.BridgeConnectionMode
import com.example.ui.components.FullscreenTerminalDialog
import com.example.ui.components.GlobalTerminalDockBar
import com.example.ui.components.TopHeaderBar
import com.example.ui.screens.AiDiagnosticScreen
import com.example.ui.screens.BromServiceScreen
import com.example.ui.screens.Esp32FlasherScreen
import com.example.ui.screens.FastbootAdbScreen
import com.example.ui.screens.PinoutDatabaseScreen
import com.example.ui.screens.ScatterCalculatorScreen
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.CardSlateBorder
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppTab
import com.example.viewmodel.MtkWorkshopViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: MtkWorkshopViewModel = viewModel()
                val activeTab by viewModel.activeTab.collectAsState()
                val serviceState by viewModel.bromEngine.serviceState.collectAsState()
                val connectionMode by viewModel.bromEngine.connectionMode.collectAsState()
                val logs by viewModel.logs.collectAsState()
                val logFilter by viewModel.logFilter.collectAsState()
                val selectedChipset by viewModel.bromEngine.selectedChipset.collectAsState()

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val coroutineScope = rememberCoroutineScope()
                var showFullscreenTerminal by remember { mutableStateOf(false) }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            drawerContainerColor = Color(0xFFFFFFFF),
                            modifier = Modifier
                                .width(310.dp)
                                .fillMaxHeight()
                                .testTag("navigation_drawer_sheet")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                // Drawer Header
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0F172A))
                                        .statusBarsPadding()
                                        .padding(20.dp)
                                ) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(BluePrimary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Memory,
                                                    contentDescription = "Logo",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(26.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "MTK WORKSHOP",
                                                    color = Color.White,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 0.5.sp
                                                )
                                                Text(
                                                    text = "v2.5 Pro Technician",
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Active Chipset Tag
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF1E293B),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(GreenSuccess)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Target SoC: ${selectedChipset.name} (${selectedChipset.architecture})",
                                                    color = Color(0xFFE2E8F0),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "SERVICE MODULES",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B),
                                    letterSpacing = 0.5.sp,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                                )

                                // Drawer Navigation Items
                                val drawerItems = listOf(
                                    DrawerMenuItem(
                                        tab = AppTab.BROM_SERVICE,
                                        title = "BROM Service Suite",
                                        subtitle = "Unlock, Locks, NVRAM IMEI, Diag",
                                        icon = Icons.Default.Memory,
                                        badge = "CORE"
                                    ),
                                    DrawerMenuItem(
                                        tab = AppTab.ESP32_FLASHER,
                                        title = "ESP32-S3 N16R8 Coprocessor",
                                        subtitle = "1-Click Flash, Glitch Tuner, 16MB/8MB",
                                        icon = Icons.Default.Memory,
                                        badge = "N16R8"
                                    ),
                                    DrawerMenuItem(
                                        tab = AppTab.PARTITIONS_FLASHER,
                                        title = "Partition & GPT Flasher",
                                        subtitle = "Scatter math, 1:1 Dump, Flash Image",
                                        icon = Icons.Default.Storage,
                                        badge = "SP TOOL"
                                    ),
                                    DrawerMenuItem(
                                        tab = AppTab.XIAOMI_DATABASE,
                                        title = "Xiaomi & SoC Pinouts",
                                        subtitle = "100+ Xiaomi/Redmi Testpoints & DB",
                                        icon = Icons.Default.Dns,
                                        badge = "100+"
                                    ),
                                    DrawerMenuItem(
                                        tab = AppTab.FASTBOOT_ADB,
                                        title = "Fastboot / ADB Tools",
                                        subtitle = "1-Click Wipe, Erase FRP, Shell",
                                        icon = Icons.Default.Usb,
                                        badge = "FAST"
                                    ),
                                    DrawerMenuItem(
                                        tab = AppTab.AI_DIAGNOSTICS,
                                        title = "Gemini AI Advisor",
                                        subtitle = "Hardware error & BROM diagnostic",
                                        icon = Icons.Default.AutoAwesome,
                                        badge = "AI"
                                    )
                                )

                                drawerItems.forEach { item ->
                                    val isSelected = activeTab == item.tab
                                    NavigationDrawerItem(
                                        icon = {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.title,
                                                tint = if (isSelected) BluePrimary else Color(0xFF64748B),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        },
                                        label = {
                                            Column {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = item.title,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                        fontSize = 13.sp,
                                                        color = if (isSelected) BluePrimary else Color(0xFF0F172A)
                                                    )
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = if (isSelected) Color(0xFFDBEAFE) else Color(0xFFF1F5F9)
                                                    ) {
                                                        Text(
                                                            text = item.badge,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) BluePrimary else Color(0xFF64748B),
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = item.subtitle,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
                                        },
                                        selected = isSelected,
                                        onClick = {
                                            viewModel.setActiveTab(item.tab)
                                            coroutineScope.launch {
                                                drawerState.close()
                                            }
                                        },
                                        colors = NavigationDrawerItemDefaults.colors(
                                            selectedContainerColor = Color(0xFFEFF6FF),
                                            unselectedContainerColor = Color.Transparent
                                        ),
                                        modifier = Modifier
                                            .padding(horizontal = 12.dp, vertical = 3.dp)
                                            .testTag("drawer_item_${item.tab.name.lowercase()}")
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                    color = Color(0xFFE2E8F0)
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // Connection Quick Info Footer inside Drawer
                                Text(
                                    text = "INTERFACE STATUS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B),
                                    letterSpacing = 0.5.sp,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                                )

                                Surface(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (connectionMode == BridgeConnectionMode.DIRECT_USB_OTG) Icons.Default.Cable else Icons.Default.Wifi,
                                                    contentDescription = "Mode",
                                                    tint = BluePrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (connectionMode == BridgeConnectionMode.DIRECT_USB_OTG) "Direct USB-OTG" else "ESP32 WiFi Bridge",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF0F172A)
                                                )
                                            }
                                            Text(
                                                text = "PORT READY",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GreenSuccess
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "BROM SLA/DAA Protocol Engine Armed",
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.weight(1f, fill = false))
                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }
                    }
                ) {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF8FAFC)),
                        topBar = {
                            TopHeaderBar(
                                serviceState = serviceState,
                                connectionMode = connectionMode,
                                onModeChange = { viewModel.bromEngine.setConnectionMode(it) },
                                onRefreshPorts = { viewModel.fastbootEngine.scanExistingDevices() },
                                onOpenDrawer = {
                                    coroutineScope.launch {
                                        drawerState.open()
                                    }
                                }
                            )
                        },
                        bottomBar = {
                            if (activeTab != AppTab.BROM_SERVICE) {
                                Column(modifier = Modifier.navigationBarsPadding()) {
                                    // Persistent Global Terminal Dock Bar at Bottom for other tabs
                                    GlobalTerminalDockBar(
                                        logs = logs,
                                        onOpenFullscreen = { showFullscreenTerminal = true },
                                        onClearLogs = { viewModel.clearLogs() }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFF8FAFC))
                                .padding(innerPadding)
                        ) {
                            when (activeTab) {
                                AppTab.BROM_SERVICE -> BromServiceScreen(viewModel = viewModel)
                                AppTab.ESP32_FLASHER -> Esp32FlasherScreen(viewModel = viewModel)
                                AppTab.PARTITIONS_FLASHER -> ScatterCalculatorScreen(viewModel = viewModel)
                                AppTab.XIAOMI_DATABASE -> PinoutDatabaseScreen(viewModel = viewModel)
                                AppTab.FASTBOOT_ADB -> FastbootAdbScreen(viewModel = viewModel)
                                AppTab.AI_DIAGNOSTICS -> AiDiagnosticScreen(viewModel = viewModel)
                            }
                        }
                    }
                }

                // Global Fullscreen Protocol Terminal Dialog
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
        }
    }
}

private data class DrawerMenuItem(
    val tab: AppTab,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badge: String
)
