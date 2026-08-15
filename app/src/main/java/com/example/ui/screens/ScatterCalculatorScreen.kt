package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
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
import com.example.engine.ScatterMathParser
import com.example.model.MtkOperation
import com.example.model.PartitionEntry
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.CardSlateBorder
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedDanger
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MtkWorkshopViewModel

@Composable
fun ScatterCalculatorScreen(
    viewModel: MtkWorkshopViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val partitions by viewModel.scatterPartitions.collectAsState()

    var startHexInput by remember { mutableStateOf("0x1800000") }
    var lengthHexInput by remember { mutableStateOf("0x500000") }
    var showScatterExportDialog by remember { mutableStateOf(false) }

    val calculatedEndHex = remember(startHexInput, lengthHexInput) {
        ScatterMathParser.calculateHexOffset(startHexInput, lengthHexInput)
    }

    val calculatedBytes = remember(lengthHexInput) {
        ScatterMathParser.parseHexSize(lengthHexInput)
    }

    val formattedSize = remember(calculatedBytes) {
        when {
            calculatedBytes >= 1024 * 1024 * 1024 -> String.format("%.2f GB", calculatedBytes / (1024.0 * 1024.0 * 1024.0))
            calculatedBytes >= 1024 * 1024 -> String.format("%.2f MB", calculatedBytes / (1024.0 * 1024.0))
            calculatedBytes >= 1024 -> String.format("%.2f KB", calculatedBytes / 1024.0)
            else -> "$calculatedBytes Bytes"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hex Offset Calculator Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hex_calculator_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Calculate, contentDescription = "Calc", tint = BluePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HEX SCATTER OFFSET CALCULATOR",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 0.5.sp
                        )
                    }

                    Button(
                        onClick = { showScatterExportDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = "Export", tint = BluePrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Export Scatter.txt", color = BluePrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = startHexInput,
                        onValueChange = { startHexInput = it },
                        label = { Text("Linear Start Address (HEX)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = CardSlateBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_linear_start")
                    )

                    OutlinedTextField(
                        value = lengthHexInput,
                        onValueChange = { lengthHexInput = it },
                        label = { Text("Boundary Length (HEX)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = CardSlateBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_boundary_length")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Output Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEFF6FF))
                        .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Calculated End Address", fontSize = 11.sp, color = Color(0xFF1E40AF), fontWeight = FontWeight.SemiBold)
                            Text(
                                text = calculatedEndHex,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A8A),
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Calculated Size", fontSize = 11.sp, color = Color(0xFF1E40AF), fontWeight = FontWeight.SemiBold)
                            Text(
                                text = formattedSize,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenSuccess
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    viewModel.startBromService(MtkOperation.PATCH_VBMETA_DMVERITY)
                    Toast.makeText(context, "Patching VBMeta (Disabling DM-Verity)...", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.Shield, contentDescription = "VBMeta", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Patch VBMeta (DM-Verity)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    viewModel.startBromService(MtkOperation.DUMP_FULL_RAW_EMMC)
                    Toast.makeText(context, "Cloning Full eMMC RAW Image...", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.Storage, contentDescription = "Dump", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("1:1 RAW eMMC Dump", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Partition Map Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RESOLVED PARTITION MAP (${partitions.size} BLOCKS)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                letterSpacing = 0.5.sp
            )
            Text(
                text = "EMMC / UFS Boot 1/2 + User",
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )
        }

        // Partition Table LazyColumn
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(partitions) { part ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            startHexInput = part.linearStartHex
                            lengthHexInput = part.boundaryLengthHex
                        }
                        .testTag("partition_item_${part.name.lowercase()}"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
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
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (part.isProtected) Color(0xFFFEF2F2) else Color(0xFFEFF6FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (part.isProtected) Icons.Default.Lock else Icons.Default.Storage,
                                    contentDescription = "Part",
                                    tint = if (part.isProtected) RedDanger else BluePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = part.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    if (part.isProtected) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "PROTECTED",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RedDanger
                                        )
                                    }
                                }
                                Text(
                                    text = "Start: ${part.linearStartHex}  |  Len: ${part.boundaryLengthHex}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B),
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = part.formattedSize,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Button(
                                onClick = {
                                    viewModel.startBromService(MtkOperation.FLASH_SINGLE_PARTITION, part.name, "${part.name}.img")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(imageVector = Icons.Default.FlashOn, contentDescription = "Flash", tint = BluePrimary, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Flash", fontSize = 10.sp, color = BluePrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showScatterExportDialog) {
        ScatterExportModal(
            partitions = partitions,
            onDismiss = { showScatterExportDialog = false }
        )
    }
}

@Composable
fun ScatterExportModal(
    partitions: List<PartitionEntry>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scatterContent = remember(partitions) {
        buildString {
            appendLine("############################################################################################################")
            appendLine("#")
            appendLine("#  MediaTek Scatter File Generated by MTK Service Workshop Suite")
            appendLine("#")
            appendLine("############################################################################################################")
            appendLine("General:")
            appendLine("  MTK_PLATFORM_CFG: V1.1.2")
            appendLine("  chip: MT6765")
            appendLine("  platform: MT6765")
            appendLine("  storage_type: EMMC")
            appendLine("############################################################################################################")
            partitions.forEach { part ->
                appendLine("- partition_index: SYS${part.index}")
                appendLine("  partition_name: ${part.name}")
                appendLine("  file_name: ${part.name}.img")
                appendLine("  is_download: true")
                appendLine("  type: NORMAL_ROM")
                appendLine("  linear_start_addr: ${part.linearStartHex}")
                appendLine("  physical_start_addr: ${part.linearStartHex}")
                appendLine("  partition_size: ${part.boundaryLengthHex}")
                appendLine("  region: EMMC_USER")
                appendLine("  storage: HW_STORAGE_EMMC")
                appendLine("  boundary_check: true")
                appendLine("  is_reserved: false")
                appendLine("  operation_type: UPDATE")
                appendLine("  reserve: 0x00")
                appendLine("")
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generated MTK_Android_scatter.txt", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
        text = {
            Column {
                Text(
                    text = "SP Flash Tool V5 / V6 compatible scatter header generated for ${partitions.size} partitions:",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A))
                        .padding(10.dp)
                ) {
                    Text(
                        text = scatterContent.take(1500) + "\n... (more partitions)",
                        color = Color(0xFF38BDF8),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Scatter", scatterContent))
                    Toast.makeText(context, "Scatter.txt copied to clipboard!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Copy Full Scatter.txt")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
