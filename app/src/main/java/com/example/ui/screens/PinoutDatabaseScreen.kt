package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MtkChipsetDatabase
import com.example.data.XiaomiDeviceEntry
import com.example.data.XiaomiKernelDatabase
import com.example.model.MtkChipset
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.CardSlateBorder
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MtkWorkshopViewModel

@Composable
fun PinoutDatabaseScreen(
    viewModel: MtkWorkshopViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeSubTab by remember { mutableStateOf(0) } // 0 = Xiaomi & Device Library, 1 = MTK SoC Specs, 2 = ESP32 Hardware Wiring
    val searchQuery by viewModel.xiaomiSearchQuery.collectAsState()
    val mtkOnlyFilter by viewModel.xiaomiMtkFilterOnly.collectAsState()
    val selectedXiaomiDevice by viewModel.selectedXiaomiDevice.collectAsState()

    var showFirmwareDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Sub-Tab Switcher
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFFFFFFF),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
        ) {
            TabRow(
                selectedTabIndex = activeSubTab,
                containerColor = Color.Transparent,
                contentColor = BluePrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeSubTab]),
                        color = BluePrimary,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 },
                    text = { Text("Xiaomi & Redmi Library", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    text = { Text("MTK SoC Pinouts", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeSubTab == 2,
                    onClick = { activeSubTab = 2 },
                    text = { Text("ESP32-S3 Bridge HW", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        when (activeSubTab) {
            0 -> {
                // Xiaomi Device Search & Filter List
                XiaomiDeviceDatabaseView(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setXiaomiSearchQuery(it) },
                    mtkOnly = mtkOnlyFilter,
                    onMtkFilterToggle = { viewModel.setXiaomiMtkFilterOnly(it) },
                    onSelectDevice = { device ->
                        viewModel.selectXiaomiDevice(device)
                        Toast.makeText(context, "Target set: ${device.devices}", Toast.LENGTH_SHORT).show()
                    },
                    selectedDevice = selectedXiaomiDevice,
                    onOpenLink = { url ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("URL", url))
                            Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
            1 -> {
                // MTK SoC Specs List
                MtkSocSpecsView(
                    onSelectChipset = { chipset ->
                        viewModel.bromEngine.setSelectedChipset(chipset)
                        Toast.makeText(context, "Active SoC: ${chipset.name}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            2 -> {
                // ESP32 Hardware Wiring & Glitcher
                Esp32HardwareWiringView(
                    onViewFirmware = { showFirmwareDialog = true }
                )
            }
        }
    }

    if (showFirmwareDialog) {
        Esp32FirmwareModal(onDismiss = { showFirmwareDialog = false })
    }
}

@Composable
fun XiaomiDeviceDatabaseView(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    mtkOnly: Boolean,
    onMtkFilterToggle: (Boolean) -> Unit,
    onSelectDevice: (XiaomiDeviceEntry) -> Unit,
    selectedDevice: XiaomiDeviceEntry?,
    onOpenLink: (String) -> Unit
) {
    val filteredList = remember(searchQuery, mtkOnly) {
        XiaomiKernelDatabase.searchDevices(searchQuery, mtkOnly)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search by codename (begonia, dandelion), model (Redmi 9A, Note 11), or SoC...", fontSize = 12.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF64748B)) },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = BluePrimary,
                unfocusedBorderColor = CardSlateBorder
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_xiaomi_input")
        )

        // Filter Toggle Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MATCHED DEVICES (${filteredList.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                letterSpacing = 0.5.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (!mtkOnly) BluePrimary else Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (!mtkOnly) BluePrimary else CardSlateBorder),
                    modifier = Modifier.clickable { onMtkFilterToggle(false) }
                ) {
                    Text(
                        text = "All Platforms",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!mtkOnly) Color.White else Color(0xFF1E293B),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (mtkOnly) BluePrimary else Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (mtkOnly) BluePrimary else CardSlateBorder),
                    modifier = Modifier.clickable { onMtkFilterToggle(true) }
                ) {
                    Text(
                        text = "MediaTek Only",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (mtkOnly) Color.White else Color(0xFF1E293B),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredList) { item ->
                val isSelected = selectedDevice?.branch == item.branch
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectDevice(item) }
                        .testTag("device_card_${item.branch}"),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) BluePrimary else CardSlateBorder
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (item.isMtk) Color(0xFFEFF6FF) else Color(0xFFF3E8FF))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (item.isMtk) "MTK" else "QUALCOMM",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.isMtk) BluePrimary else Color(0xFF7E22CE)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.branch,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Button(
                                onClick = { onSelectDevice(item) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) GreenSuccess else BluePrimary
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Smartphone,
                                    contentDescription = "Set",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = if (isSelected) "Active Target" else "Select", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.devices,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = "SoC: ${item.soc}", fontSize = 11.sp, color = Color(0xFF475569), fontWeight = FontWeight.Medium)
                            Text(text = "OS: ${item.androidVersion}", fontSize = 11.sp, color = Color(0xFF64748B))
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Base Tag: ${item.baseTag}",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF8FAFC))
                                .padding(6.dp)
                        ) {
                            Text(
                                text = "📍 TP: ${item.testPointGuide}",
                                fontSize = 10.sp,
                                color = Color(0xFF334155)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { onOpenLink(item.link) },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Link, contentDescription = "Link", modifier = Modifier.size(14.dp), tint = BluePrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "MiCode Kernel Source", fontSize = 11.sp, color = BluePrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MtkSocSpecsView(
    onSelectChipset: (MtkChipset) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(MtkChipsetDatabase.supportedChipsets) { chipset ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("soc_card_${chipset.hwCodeHex.lowercase()}"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = chipset.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "HW Code: ${chipset.hwCodeHex} | Sub: ${chipset.hwSubCodeHex}",
                                fontSize = 11.sp,
                                color = BluePrimary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { onSelectChipset(chipset) },
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Set Target", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Popular Models: ${chipset.popularPhones}",
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "DA: ${chipset.daRequired}", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text(text = "SLA: ${chipset.slaType}", fontSize = 11.sp, color = Color(0xFF64748B))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFEFF6FF))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(text = "🔌 ${chipset.bromKeyCombo}", fontSize = 11.sp, color = Color(0xFF1E40AF), fontWeight = FontWeight.SemiBold)
                            Text(text = "📍 ${chipset.testPointGuide}", fontSize = 10.sp, color = Color(0xFF475569))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Esp32HardwareWiringView(
    onViewFirmware: () -> Unit
) {
    val context = LocalContext.current
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.DeveloperBoard, contentDescription = "ESP32", tint = BluePrimary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("ESP32-S3 Hardware Glitcher & Bridge", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Hardware VBUS Control & USB Host BROM Bypass", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Hardware Pinout Connections:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(4.dp))
                Text("• GPIO 19 (USB D-) -> Phone USB D- Line", fontSize = 12.sp, color = Color(0xFF475569))
                Text("• GPIO 20 (USB D+) -> Phone USB D+ Line", fontSize = 12.sp, color = Color(0xFF475569))
                Text("• GPIO 4 (VBUS EN) -> P-Channel MOSFET Gate (VBUS Pulse)", fontSize = 12.sp, color = Color(0xFF475569))
                Text("• GPIO 5 (GLITCH TP) -> Phone CLK / KCOL0 Test Point", fontSize = 12.sp, color = Color(0xFF475569))
                Text("• GND -> Common Ground Plane", fontSize = 12.sp, color = Color(0xFF475569))

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onViewFirmware,
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Code, contentDescription = "Code", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("View C Firmware", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Pinout", "GPIO19=D-, GPIO20=D+, GPIO4=VBUS, GPIO5=TP_CLK, GND=GND"))
                            Toast.makeText(context, "Pinout copied!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Pinout")
                    }
                }
            }
        }
    }
}

@Composable
fun Esp32FirmwareModal(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val code = """
// ESP32-S3 MTK BROM Glitcher & Bridge Firmware
#include <Arduino.h>
#include "esp_timer.h"

#define PIN_VBUS_EN 4
#define PIN_GLITCH_TP 5
#define BROM_SYNC_BYTE 0xA0

void setup() {
    Serial.begin(115200);
    pinMode(PIN_VBUS_EN, OUTPUT);
    pinMode(PIN_GLITCH_TP, OUTPUT);
    digitalWrite(PIN_VBUS_EN, HIGH);
    digitalWrite(PIN_GLITCH_TP, LOW);
    Serial.println("MTK_BRIDGE_READY_V2");
}

void trigger_brom_glitch() {
    digitalWrite(PIN_VBUS_EN, LOW); // Cut VBUS
    delayMicroseconds(50);
    digitalWrite(PIN_GLITCH_TP, HIGH); // Short TP
    delayMicroseconds(120);
    digitalWrite(PIN_VBUS_EN, HIGH); // Restore VBUS
    delayMicroseconds(80);
    digitalWrite(PIN_GLITCH_TP, LOW); // Release TP
}

void loop() {
    if (Serial.available()) {
        char cmd = Serial.read();
        if (cmd == 'G') trigger_brom_glitch();
    }
}
    """.trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ESP32-S3 C++ Source Code", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
        text = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A))
                        .padding(10.dp)
                ) {
                    Text(text = code, color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Firmware", code))
                    Toast.makeText(context, "Firmware code copied to clipboard!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Copy Code")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
