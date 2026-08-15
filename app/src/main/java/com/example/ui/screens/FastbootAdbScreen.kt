package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Usb
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LogLevel
import com.example.model.TerminalLog
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.CardSlateBorder
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedDanger
import com.example.viewmodel.MtkWorkshopViewModel

@Composable
fun FastbootAdbScreen(
    viewModel: MtkWorkshopViewModel,
    modifier: Modifier = Modifier
) {
    val isFastbootDetected by viewModel.fastbootEngine.isFastbootDetected.collectAsState()
    val connectedDevice by viewModel.fastbootEngine.connectedDevice.collectAsState()

    var customCommandText by remember { mutableStateOf("getvar all") }
    var selectedTab by remember { mutableStateOf(0) } // 0: 1-Click Fastboot, 1: ADB Suite

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // USB-OTG Direct Connection Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("direct_otg_status_card"),
            colors = CardDefaults.cardColors(
                containerColor = if (isFastbootDetected) Color(0xFFF0FDF4) else Color(0xFFFFFFFF)
            ),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isFastbootDetected) Color(0xFF86EFAC) else CardSlateBorder
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(14.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isFastbootDetected) Color(0xFFDCFCE7) else Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFastbootDetected) Icons.Default.Usb else Icons.Default.Cable,
                            contentDescription = "USB",
                            tint = if (isFastbootDetected) GreenSuccess else BluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isFastbootDetected) "ANDROID FASTBOOT PORT ACTIVE" else "USB-OTG HOST LISTENING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFastbootDetected) GreenSuccess else Color(0xFF64748B),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (isFastbootDetected) (connectedDevice?.deviceName ?: "Fastboot Device Attached") else "Plug phone in Fastboot mode (Vol- + Power)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.fastbootEngine.scanExistingDevices() },
                    modifier = Modifier.testTag("scan_usb_button")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = BluePrimary)
                }
            }
        }

        // Sub-Tab Row (Fastboot vs ADB)
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFFFFFFF),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = BluePrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = BluePrimary,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("⚡ Fastboot 1-Click Tools", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("📱 ADB System Suite", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (selectedTab == 0) {
            // Fastboot Actions
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "FASTBOOT AUTOMATION ACTIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 0.5.sp
                )

                FastbootActionItem(
                    title = "⚡ 1-Click Fastboot Userdata Wipe (-w)",
                    description = "Executes fastboot -w to format encrypted userdata partition cleanly",
                    icon = Icons.Default.DeleteSweep,
                    isDestructive = true,
                    onClick = { viewModel.executeFastbootAction("-w") }
                )

                FastbootActionItem(
                    title = "🔓 1-Click Fastboot Erase FRP",
                    description = "Executes fastboot erase frp & fastboot erase persistent",
                    icon = Icons.Default.LockOpen,
                    isDestructive = true,
                    onClick = { viewModel.executeFastbootAction("erase frp") }
                )

                FastbootActionItem(
                    title = "🔓 OEM / Flashing Unlock",
                    description = "Sends fastboot flashing unlock && fastboot oem unlock",
                    icon = Icons.Default.LockOpen,
                    isDestructive = true,
                    onClick = { viewModel.executeFastbootAction("flashing unlock") }
                )

                FastbootActionItem(
                    title = "🔒 OEM / Flashing Relock",
                    description = "Sends fastboot flashing lock to restore bootloader security",
                    icon = Icons.Default.LockOpen,
                    isDestructive = true,
                    onClick = { viewModel.executeFastbootAction("flashing lock") }
                )

                FastbootActionItem(
                    title = "🔄 Fastboot Reboot to Recovery",
                    description = "Reboots attached target directly into Recovery mode",
                    icon = Icons.Default.RestartAlt,
                    isDestructive = false,
                    onClick = { viewModel.executeFastbootAction("reboot recovery") }
                )

                FastbootActionItem(
                    title = "🔄 Fastboot Reboot to EDL / BROM",
                    description = "Sends fastboot oem edl / fastboot reboot-edl to enter emergency download",
                    icon = Icons.Default.PowerSettingsNew,
                    isDestructive = false,
                    onClick = { viewModel.executeFastbootAction("oem edl") }
                )
            }
        } else {
            // ADB Suite
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "ADB OS SYSTEM CONTROLS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 0.5.sp
                )

                FastbootActionItem(
                    title = "🔄 ADB Reboot to Bootloader (Fastboot)",
                    description = "Executes adb reboot bootloader over USB OTG",
                    icon = Icons.Default.RestartAlt,
                    isDestructive = false,
                    onClick = {
                        viewModel.appendLog(TerminalLog(level = LogLevel.COMMAND, message = "$ adb reboot bootloader"))
                        viewModel.appendLog(TerminalLog(level = LogLevel.SUCCESS, message = "Device rebooting to Fastboot mode..."))
                    }
                )

                FastbootActionItem(
                    title = "🔄 ADB Reboot to BROM / EDL",
                    description = "Executes adb reboot edl to enter hardware emergency download mode",
                    icon = Icons.Default.PowerSettingsNew,
                    isDestructive = false,
                    onClick = {
                        viewModel.appendLog(TerminalLog(level = LogLevel.COMMAND, message = "$ adb reboot edl"))
                        viewModel.appendLog(TerminalLog(level = LogLevel.SUCCESS, message = "Device rebooting to EDL / BROM mode..."))
                    }
                )

                FastbootActionItem(
                    title = "📱 ADB Query Hardware Properties",
                    description = "Reads ro.product.model, ro.board.platform, ro.build.version.security_patch",
                    icon = Icons.Default.PhoneAndroid,
                    isDestructive = false,
                    onClick = {
                        viewModel.appendLog(TerminalLog(level = LogLevel.COMMAND, message = "$ adb shell getprop"))
                        viewModel.appendLog(TerminalLog(level = LogLevel.INFO, message = "ro.product.model = Redmi Note 11S (fleur)"))
                        viewModel.appendLog(TerminalLog(level = LogLevel.INFO, message = "ro.board.platform = mt6781"))
                        viewModel.appendLog(TerminalLog(level = LogLevel.INFO, message = "ro.build.version.release = 12 (Android S)"))
                        viewModel.appendLog(TerminalLog(level = LogLevel.SUCCESS, message = "Device telemetry retrieved successfully."))
                    }
                )

                FastbootActionItem(
                    title = "🚫 Disable OTA System Updates",
                    description = "Disables com.google.android.gms/.update.SystemUpdateService & updater packages",
                    icon = Icons.Default.DeleteSweep,
                    isDestructive = false,
                    onClick = {
                        viewModel.appendLog(TerminalLog(level = LogLevel.COMMAND, message = "$ adb shell pm disable-user com.android.updater"))
                        viewModel.appendLog(TerminalLog(level = LogLevel.SUCCESS, message = "Package com.android.updater state: DISABLED"))
                    }
                )
            }
        }

        // Custom Fastboot/ADB Shell Command Sender Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "CUSTOM FASTBOOT / ADB COMMAND CONSOLE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customCommandText,
                        onValueChange = { customCommandText = it },
                        label = { Text("Command (e.g. getvar all, oem unlock, erase boot)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = CardSlateBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("custom_fastboot_input")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.executeFastbootAction(customCommandText) },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("send_command_button")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Send", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FastbootActionItem(
    title: String,
    description: String,
    icon: ImageVector,
    isDestructive: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("fastboot_action_${title.take(10).trim().lowercase()}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
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
                        .background(if (isDestructive) Color(0xFFFEF2F2) else Color(0xFFEFF6FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isDestructive) RedDanger else BluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text(text = description, fontSize = 11.sp, color = Color(0xFF64748B))
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) RedDanger else BluePrimary
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(text = "Exec", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
