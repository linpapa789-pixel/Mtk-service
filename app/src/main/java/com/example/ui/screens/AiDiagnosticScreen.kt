package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MtkChipsetDatabase
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.CardSlateBorder
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedDanger
import com.example.viewmodel.MtkWorkshopViewModel

@Composable
fun AiDiagnosticScreen(
    viewModel: MtkWorkshopViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val aiResult by viewModel.aiDiagnosticResult.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val selectedChipset by viewModel.bromEngine.selectedChipset.collectAsState()

    var errorInput by remember { mutableStateOf("ERROR 4032 (S_FT_ENABLE_DRAM_FAIL)") }
    var operationContext by remember { mutableStateOf("NVRAM Dump & Userdata Erase") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // AI Diagnostic Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_header_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFDBEAFE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = BluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "GEMINI HARDWARE DIAGNOSTIC ADVISOR",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Intelligent MTK & Android Repair Assistant",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "AI analyzes BROM handshakes, DA stage logs, PMIC voltage drops, and DRAM test points to recommend exact solutions.",
                    fontSize = 12.sp,
                    color = Color(0xFF475569),
                    lineHeight = 16.sp
                )
            }
        }

        // Quick Preset Chips for Common Technician Errors
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "COMMON TECHNICIAN ERROR PRESETS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                letterSpacing = 0.5.sp
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val presets = listOf(
                    "ERROR 4032 (DRAM Init Fail)",
                    "0x1011 (STATUS_SEC_AUTH_FILE_NEEDED)",
                    "Red State (DM-Verity Bootloop)",
                    "Baseband Unknown (Null Radio)",
                    "0x7000 (S_BROM_CMD_STARTCOUNT_FAIL)",
                    "0x0005 (STATUS_ERR_STORAGE_LIFETIME)"
                )
                items(presets) { preset ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder),
                        modifier = Modifier.clickable {
                            errorInput = preset
                            viewModel.runAiDiagnosis(preset, selectedChipset.name, operationContext)
                        }
                    ) {
                        Text(
                            text = preset,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Diagnostic Input Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_input_card"),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = errorInput,
                    onValueChange = { errorInput = it },
                    label = { Text("Error Code / Fault Symptom") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = CardSlateBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("error_code_input")
                )

                OutlinedTextField(
                    value = operationContext,
                    onValueChange = { operationContext = it },
                    label = { Text("Operation Context / Target Action") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = CardSlateBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        viewModel.runAiDiagnosis(errorInput, selectedChipset.name, operationContext)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("run_diagnosis_button")
                ) {
                    if (isAiLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyzing Hardware Registers...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Diagnose", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Run Gemini AI Diagnosis", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // AI Diagnosis Result Card
        AnimatedVisibility(visible = aiResult != null || isAiLoading) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_result_card"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.BugReport, contentDescription = "Report", tint = BluePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DIAGNOSIS & STEP-BY-STEP FIX",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary
                            )
                        }

                        if (aiResult != null) {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Diagnosis", aiResult))
                                    Toast.makeText(context, "Diagnosis copied!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF64748B))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isAiLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            CircularProgressIndicator(color = BluePrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Consulting Gemini AI diagnostic models...", fontSize = 13.sp, color = Color(0xFF475569))
                        }
                    } else {
                        Text(
                            text = aiResult ?: "",
                            fontSize = 13.sp,
                            color = Color(0xFF1E293B),
                            lineHeight = 19.sp
                        )
                    }
                }
            }
        }
    }
}
