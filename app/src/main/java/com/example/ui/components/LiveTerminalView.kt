package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LogLevel
import com.example.model.TerminalLog
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.CardSlateBorder
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.PurpleTech
import com.example.ui.theme.RedDanger
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalPurple
import com.example.ui.theme.TerminalRed
import com.example.ui.theme.TerminalText
import com.example.ui.theme.TerminalYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LiveTerminalView(
    logs: List<TerminalLog>,
    activeFilter: LogLevel?,
    onFilterChange: (LogLevel?) -> Unit,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier,
    customHeight: Dp = 260.dp,
    allowFullscreenToggle: Boolean = true,
    onToggleFullscreen: (() -> Unit)? = null,
    isFullscreen: Boolean = false
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredLogs = remember(logs, activeFilter, searchQuery) {
        var result = if (activeFilter != null) {
            logs.filter { it.level == activeFilter }
        } else {
            logs
        }
        if (searchQuery.isNotEmpty()) {
            result = result.filter {
                it.message.contains(searchQuery, ignoreCase = true) ||
                        (it.hexDump != null && it.hexDump.contains(searchQuery, ignoreCase = true)) ||
                        it.timestamp.contains(searchQuery, ignoreCase = true)
            }
        }
        result
    }

    LaunchedEffect(filteredLogs.size) {
        if (filteredLogs.isNotEmpty()) {
            listState.animateScrollToItem(filteredLogs.size - 1)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("live_terminal_view"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE0F2FE))
                            .border(1.dp, BluePrimary.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Terminal",
                            tint = BluePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "LIVE PROTOCOL TERMINAL",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0F172A),
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(GreenSuccess)
                            )
                        }
                        Text(
                            text = "${filteredLogs.size} logs / MTK BROM Protocol Feed",
                            color = Color(0xFF64748B),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Actions: Search, Copy, Clear, Fullscreen Toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isSearchVisible = !isSearchVisible },
                        modifier = Modifier.size(28.dp).testTag("terminal_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Logs",
                            tint = if (isSearchVisible) BluePrimary else Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = {
                            val allText = logs.joinToString("\n") { "[${it.timestamp}] [${it.level}] ${it.message}${if (it.hexDump != null) " | HEX: ${it.hexDump}" else ""}" }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("MTK Logs", allText))
                            Toast.makeText(context, "All logs copied (${logs.size} lines)", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(28.dp).testTag("copy_logs_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Logs",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onClearLogs,
                        modifier = Modifier.size(28.dp).testTag("clear_logs_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ClearAll,
                            contentDescription = "Clear Logs",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (allowFullscreenToggle && onToggleFullscreen != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onToggleFullscreen,
                            modifier = Modifier.size(28.dp).testTag("fullscreen_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = "Fullscreen",
                                tint = BluePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Search Bar (if opened)
            AnimatedVisibility(visible = isSearchVisible) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("terminal_search_input"),
                        placeholder = { Text("Filter logs (e.g. 0xA0, fail, NVRAM)...", fontSize = 11.sp, color = Color(0xFF94A3B8)) },
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val packetCount = logs.count { it.level == LogLevel.PACKET }
                val cmdCount = logs.count { it.level == LogLevel.COMMAND }
                val errCount = logs.count { it.level == LogLevel.ERROR }

                TerminalFilterChip(
                    label = "ALL (${logs.size})",
                    isSelected = activeFilter == null,
                    activeColor = BluePrimary,
                    onClick = { onFilterChange(null) }
                )
                TerminalFilterChip(
                    label = "PACKETS ($packetCount)",
                    isSelected = activeFilter == LogLevel.PACKET,
                    activeColor = BluePrimary,
                    onClick = { onFilterChange(LogLevel.PACKET) }
                )
                TerminalFilterChip(
                    label = "COMMANDS ($cmdCount)",
                    isSelected = activeFilter == LogLevel.COMMAND,
                    activeColor = PurpleTech,
                    onClick = { onFilterChange(LogLevel.COMMAND) }
                )
                TerminalFilterChip(
                    label = "ERRORS ($errCount)",
                    isSelected = activeFilter == LogLevel.ERROR,
                    activeColor = RedDanger,
                    onClick = { onFilterChange(LogLevel.ERROR) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Terminal Screen Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isFullscreen) Modifier.weight(1f)
                        else Modifier.height(customHeight)
                    )
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF070E1B))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                if (filteredLogs.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No logs match '$searchQuery'" else "Terminal ready. Waiting for MTK BROM events...",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Connect phone with Vol Key to stream live handshake packets",
                            color = Color(0xFF475569),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredLogs, key = { it.id }) { log ->
                            TerminalLogRow(log = log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TerminalLogRow(log: TerminalLog) {
    val levelColor = when (log.level) {
        LogLevel.INFO -> TerminalText
        LogLevel.SUCCESS -> TerminalGreen
        LogLevel.WARN -> TerminalYellow
        LogLevel.ERROR -> TerminalRed
        LogLevel.PACKET -> TerminalCyan
        LogLevel.COMMAND -> TerminalPurple
    }

    val tagText = when (log.level) {
        LogLevel.INFO -> "INF"
        LogLevel.SUCCESS -> " OK"
        LogLevel.WARN -> "WRN"
        LogLevel.ERROR -> "ERR"
        LogLevel.PACKET -> "PKT"
        LogLevel.COMMAND -> "CMD"
    }

    val tagBg = levelColor.copy(alpha = 0.15f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Timestamp
        Text(
            text = log.timestamp,
            color = Color(0xFF475569),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(tagBg)
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = tagText,
                color = levelColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Message & Hex Data
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = log.message,
                color = levelColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 14.sp
            )
            if (log.hexDump != null) {
                Row(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF091220))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HEX: ",
                        color = Color(0xFF64748B),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = log.hexDump,
                        color = CyanPrimary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TerminalFilterChip(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) activeColor.copy(alpha = 0.2f) else Color(0xFF0F172A),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, activeColor) else androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) activeColor else TextSecondary,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}
