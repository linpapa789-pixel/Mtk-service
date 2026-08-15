package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Esp32FirmwareTemplate
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.CardSlateBorder
import com.example.ui.theme.GreenSuccess
import com.example.viewmodel.MtkWorkshopViewModel

@Composable
fun Esp32FlasherScreen(viewModel: MtkWorkshopViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val telemetry by viewModel.esp32Engine.telemetry.collectAsState()
    val isFlashing by viewModel.esp32Engine.isFlashing.collectAsState()
    val flashProgress by viewModel.esp32Engine.flashProgress.collectAsState()
    val flashStatus by viewModel.esp32Engine.flashStatus.collectAsState()

    var selectedSourceTab by remember { mutableIntStateOf(0) }
    var glitchDelay by remember { mutableFloatStateOf(telemetry.glitchDelayUs.toFloat()) }
    var glitchPulse by remember { mutableFloatStateOf(telemetry.glitchPulseNs.toFloat()) }
    var targetOffset by remember { mutableStateOf("0x00000000") }
    var selectedBaud by remember { mutableStateOf("921600") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hardware Status & Specification Header Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("esp32_hardware_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF2563EB)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeveloperBoard,
                                    contentDescription = "ESP32",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "ESP32-S3 N16R8 Coprocessor",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "16MB Flash (QIO) • 8MB Octal PSRAM (OPI)",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Online/Offline Pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (telemetry.isConnected) Color(0xFF14532D) else Color(0xFF334155),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (telemetry.isConnected) GreenSuccess else Color(0xFF475569)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (telemetry.isConnected) GreenSuccess else Color(0xFF94A3B8))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (telemetry.isConnected) "ARMED" else "DISARMED",
                                    color = if (telemetry.isConnected) Color(0xFF86EFAC) else Color(0xFFCBD5E1),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Specs Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SpecTag(title = "FLASH", value = "16 MB QIO", subtitle = "80 MHz")
                        SpecTag(title = "OCTAL PSRAM", value = "8 MB OPI", subtitle = "512KB DMA")
                        SpecTag(title = "CPU CLOCK", value = "240 MHz", subtitle = "Dual-Core")
                        SpecTag(title = "GLITCH PIN", value = "GPIO 4", subtitle = "50-5000ns")
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Connection Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.esp32Engine.pingEsp32Bridge()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("btn_ping_esp32")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ping / Probe Coprocessor", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.esp32Engine.triggerHardwareGlitchTest()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFBBF24)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("btn_trigger_glitch_test")
                        ) {
                            Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Glitch (GPIO 4)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Dual Type-C Ports Explanation (COM vs USB/OTG)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dual_typec_guide_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Usb,
                                contentDescription = null,
                                tint = BluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ESP32-S3 Dual Type-C Ports (COM vs USB)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFDBEAFE)
                        ) {
                            Text(
                                text = "2x Type-C",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "ESP32-S3 DevKitC-1 ဘုတ်တွင် Type-C ပေါက် (၂) ခု ပါဝင်ပြီး လုပ်ဆောင်ချက် မတူညီပါ:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF334155)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Port 1: COM Port Box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
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
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2563EB))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "1. \"COM\" (UART) Port",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFEFF6FF)
                                ) {
                                    Text(
                                        text = "FLASH & LOGS",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2563EB),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• အသုံးပြုရန်: Android APK Flasher / PC esptool.py ဖြင့် Firmware Flash ရန် နှင့် Serial Monitor logs ဖတ်ရန်။\n• ချိတ်ဆက်ရန်: Android Master Phone (သို့မဟုတ် PC) သို့ Type-C OTG ကြိုးဖြင့် ချိတ်ပါ။",
                                fontSize = 11.sp,
                                color = Color(0xFF475569),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Port 2: USB (OTG) Port Box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
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
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF059669))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "2. \"USB\" (Native OTG) Port",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFECFDF5)
                                ) {
                                    Text(
                                        text = "TARGET PHONE / PASSTHROUGH",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF059669),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• အသုံးပြုရန်: Target Phone ကို USB Host ဖြင့် ချိတ်ဆက်ရန် သို့မဟုတ် High-Speed 12Mbps Native CDC DMA Pass-through အတွက်။\n• ချိတ်ဆက်ရန်: ပြုပြင်မည့် Target Phone သို့ Type-C / MicroUSB ကြိုးဖြင့် ချိတ်ပါ။",
                                fontSize = 11.sp,
                                color = Color(0xFF475569),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. 1-Click Firmware Flashing Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("flasher_tool_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = BluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "1-Click ESP32-S3 Firmware Flasher",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFEFF6FF)
                        ) {
                            Text(
                                text = "MERGED 0x00000",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Flash the pre-compiled MTK Coprocessor firmware (with 16MB/8MB Octal PSRAM + microsecond BROM glitch engine) directly to ESP32-S3 over USB OTG/CDC cable.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Flasher Controls (Offset & Baud)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = targetOffset,
                            onValueChange = { targetOffset = it },
                            label = { Text("Flash Offset", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_flash_offset"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BluePrimary,
                                unfocusedBorderColor = CardSlateBorder
                            )
                        )

                        OutlinedTextField(
                            value = selectedBaud,
                            onValueChange = { selectedBaud = it },
                            label = { Text("Baud Rate", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_flash_baud"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BluePrimary,
                                unfocusedBorderColor = CardSlateBorder
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isFlashing) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = flashStatus,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = BluePrimary
                                )
                                Text(
                                    text = "${(flashProgress * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BluePrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { flashProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = BluePrimary,
                                trackColor = Color(0xFFDBEAFE)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.esp32Engine.flashFirmware(
                                offsetHex = targetOffset,
                                baudRate = selectedBaud.toIntOrNull() ?: 921600
                            )
                        },
                        enabled = !isFlashing,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_start_flash_esp32")
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isFlashing) "Flashing ESP32-S3..." else "⚡ 1-Click Flash 'esp32s3_n16r8_firmware_merged.bin'",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // 3. Glitch Timing & Coprocessor Calibration
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BROM Glitch Hardware Tuner (GPIO 4)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = "MT6765/MT6833",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Calibrate the precision nanosecond pulse to exploit the MediaTek BootROM SLA check at VBUS plug-in.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Glitch Delay Slider
                    Text(
                        text = "Trigger Delay: ${glitchDelay.toInt()} µs",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                    Slider(
                        value = glitchDelay,
                        onValueChange = { glitchDelay = it },
                        valueRange = 50f..1500f,
                        colors = SliderDefaults.colors(thumbColor = BluePrimary, activeTrackColor = BluePrimary),
                        modifier = Modifier.testTag("slider_glitch_delay")
                    )

                    // Glitch Pulse Width Slider
                    Text(
                        text = "Pulse Duration: ${glitchPulse.toInt()} ns (NOP loop calibration)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                    Slider(
                        value = glitchPulse,
                        onValueChange = { glitchPulse = it },
                        valueRange = 100f..3000f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF8B5CF6), activeTrackColor = Color(0xFF8B5CF6)),
                        modifier = Modifier.testTag("slider_glitch_pulse")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.esp32Engine.configureGlitchParams(
                                delayUs = glitchDelay.toInt(),
                                pulseNs = glitchPulse.toInt()
                            )
                            Toast.makeText(context, "Glitch timings synchronized to ESP32!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("btn_save_glitch_params")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Timings to ESP32 Hardware Profile", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. Hardware Wiring & Pinout Schematic
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Cable, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Wiring Schematic & Pin Connections",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF0F172A)
                            )
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(Esp32FirmwareTemplate.WIRING_GUIDE))
                                Toast.makeText(context, "Wiring Schematic copied!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = BluePrimary, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Wiring Table Items
                    WiringRow(espPin = "GPIO 19", targetPin = "Target USB D- (White Wire)", purpose = "UART1 RX / BROM Handshake")
                    WiringRow(espPin = "GPIO 20", targetPin = "Target USB D+ (Green Wire)", purpose = "UART1 TX / BROM Handshake")
                    WiringRow(espPin = "GPIO 4", targetPin = "Target CLK / VBUS MOSFET", purpose = "Microsecond Glitch Pulse Out")
                    WiringRow(espPin = "GPIO 48", targetPin = "On-board WS2812 RGB LED", purpose = "Status Visualizer (Cyan/Green/Red)")
                    WiringRow(espPin = "GPIO 8/9", targetPin = "SSD1306 OLED (SDA / SCL)", purpose = "Live Telemetry Display")
                    WiringRow(espPin = "GND", targetPin = "Phone GND + 5V Adapter GND", purpose = "Common Ground (MANDATORY)")
                    WiringRow(espPin = "EXT 5V 2A", targetPin = "Phone USB VBUS (Red Wire)", purpose = "External Power Supply (Do NOT use 3.3V)")
                }
            }
        }

        // 5. Embedded C++ Code, PlatformIO & GitHub Actions YAML Viewer
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Firmware Code & GitHub CI/CD",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }

                        val activeContent = when (selectedSourceTab) {
                            0 -> Esp32FirmwareTemplate.ESP32_C_CODE
                            1 -> Esp32FirmwareTemplate.PLATFORMIO_INI
                            2 -> Esp32FirmwareTemplate.PARTITIONS_CSV
                            3 -> Esp32FirmwareTemplate.GITHUB_WORKFLOW_YML
                            else -> ""
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(activeContent))
                                Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF60A5FA), modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Code Tab Selection
                    val tabs = listOf("main.cpp", "platformio.ini", "partitions_16mb.csv", "build.yml")
                    ScrollableTabRow(
                        selectedTabIndex = selectedSourceTab,
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color.White,
                        edgePadding = 0.dp,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedSourceTab == index,
                                onClick = { selectedSourceTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (selectedSourceTab == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedSourceTab == index) Color(0xFF60A5FA) else Color(0xFF94A3B8)
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Code Box
                    val displayCode = when (selectedSourceTab) {
                        0 -> Esp32FirmwareTemplate.ESP32_C_CODE
                        1 -> Esp32FirmwareTemplate.PLATFORMIO_INI
                        2 -> Esp32FirmwareTemplate.PARTITIONS_CSV
                        3 -> Esp32FirmwareTemplate.GITHUB_WORKFLOW_YML
                        else -> ""
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF020617),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = displayCode,
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // Bottom spacing for persistent terminal dock
        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SpecTag(title: String, value: String, subtitle: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E293B))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(text = title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(1.dp))
        Text(text = subtitle, fontSize = 9.sp, color = Color(0xFF60A5FA))
    }
}

@Composable
private fun WiringRow(espPin: String, targetPin: String, purpose: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.2f)) {
                Text(
                    text = espPin,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = BluePrimary
                )
                Text(
                    text = targetPin,
                    fontSize = 11.sp,
                    color = Color(0xFF0F172A)
                )
            }
            Text(
                text = purpose,
                fontSize = 10.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
