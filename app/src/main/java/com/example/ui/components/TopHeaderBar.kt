package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BridgeConnectionMode
import com.example.model.ServiceState
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.CardSlateBg
import com.example.ui.theme.CardSlateBorder
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedDanger
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TopHeaderBar(
    serviceState: ServiceState,
    connectionMode: BridgeConnectionMode,
    onModeChange: (BridgeConnectionMode) -> Unit,
    onRefreshPorts: () -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("top_header_bar"),
        color = Color(0xFFFFFFFF),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder),
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Drawer Menu Button + Title and App Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("open_drawer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Navigation Menu",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE0F2FE))
                            .border(1.dp, BluePrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = "MTK Logo",
                            tint = BluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "MTK WORKSHOP",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp,
                                color = Color(0xFF0F172A),
                                fontSize = 15.sp
                            )
                        )
                        Text(
                            text = "v2.5 Pro Portable Tech Suite",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF64748B),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Port Refresh Button
                IconButton(
                    onClick = onRefreshPorts,
                    modifier = Modifier.testTag("refresh_ports_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh USB Ports",
                        tint = BluePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.padding(top = 8.dp))

            // Status Bar & Mode Switcher Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // State Indicator Badge
                StateIndicatorBadge(serviceState = serviceState)

                // Bridge Mode Selector Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFF1F5F9))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(20.dp))
                        .padding(horizontal = 4.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ModeSelectorButton(
                        label = "ESP32",
                        icon = Icons.Default.Wifi,
                        isSelected = connectionMode != BridgeConnectionMode.DIRECT_USB_OTG,
                        onClick = { onModeChange(BridgeConnectionMode.ESP32_WIFI_TCP) }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    ModeSelectorButton(
                        label = "USB OTG",
                        icon = Icons.Default.Cable,
                        isSelected = connectionMode == BridgeConnectionMode.DIRECT_USB_OTG,
                        onClick = { onModeChange(BridgeConnectionMode.DIRECT_USB_OTG) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StateIndicatorBadge(serviceState: ServiceState) {
    val statusColor = when (serviceState) {
        ServiceState.IDLE -> TextSecondary
        ServiceState.ARMED_WAITING -> AmberWarning
        ServiceState.DEVICE_DETECTED,
        ServiceState.HANDSHAKING,
        ServiceState.SLA_BYPASSING,
        ServiceState.DA_LOADING,
        ServiceState.GPT_READING,
        ServiceState.STREAMING_BACKUP,
        ServiceState.ERASING_PARTITION,
        ServiceState.FLASHING_IMAGE,
        ServiceState.DIAGNOSTIC_TESTING,
        ServiceState.REBOOTING_DEVICE -> BluePrimary
        ServiceState.COMPLETED -> GreenSuccess
        ServiceState.FAILED -> RedDanger
    }

    val statusText = when (serviceState) {
        ServiceState.IDLE -> "IDLE (Select Task)"
        ServiceState.ARMED_WAITING -> "⏳ ARMED: WAIT FOR PORT"
        ServiceState.DEVICE_DETECTED -> "🟢 PORT ACTIVE"
        ServiceState.HANDSHAKING -> "⚡ BROM HANDSHAKE"
        ServiceState.SLA_BYPASSING -> "🔓 SLA BYPASS"
        ServiceState.DA_LOADING -> "📦 LOADING DA"
        ServiceState.GPT_READING -> "📑 READING GPT"
        ServiceState.STREAMING_BACKUP -> "🛡️ DUMPING NVRAM"
        ServiceState.ERASING_PARTITION -> "⚠️ ERASING"
        ServiceState.FLASHING_IMAGE -> "⚡ FLASHING IMAGE"
        ServiceState.DIAGNOSTIC_TESTING -> "🩺 DIAGNOSTICS"
        ServiceState.REBOOTING_DEVICE -> "🔄 REBOOTING"
        ServiceState.COMPLETED -> "✅ READY / DONE"
        ServiceState.FAILED -> "❌ FAILED"
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(statusColor.copy(alpha = 0.15f))
            .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(statusColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = statusText,
            color = statusColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun ModeSelectorButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) CyanPrimary.copy(alpha = 0.2f) else Color.Transparent,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary) else null,
        modifier = Modifier.padding(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) CyanPrimary else TextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) TextPrimary else TextSecondary
            )
        }
    }
}
