package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.model.DeviceInfo
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.CardSlateBg
import com.example.ui.theme.CardSlateBorder
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DeviceInfoCard(
    deviceInfo: DeviceInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("device_info_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardSlateBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Card Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Smartphone,
                        contentDescription = "Device Info",
                        tint = BluePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TARGET HARDWARE TELEMETRY",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = TextPrimary
                        )
                    )
                }

                // Online/Offline Dot
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (deviceInfo.isConnected) GreenSuccess else TextSecondary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (deviceInfo.isConnected) "ONLINE" else "STANDBY",
                        color = if (deviceInfo.isConnected) GreenSuccess else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Specs Grid (2 columns x 3 rows)
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    InfoItemRow(
                        icon = Icons.Default.Memory,
                        label = "Chipset / SoC",
                        value = deviceInfo.socModel,
                        highlight = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    InfoItemRow(
                        icon = Icons.Default.Security,
                        label = "Security Mode",
                        value = deviceInfo.securityState,
                        highlight = deviceInfo.securityState.contains("Protected")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    InfoItemRow(
                        icon = Icons.Default.Storage,
                        label = "Storage Flash",
                        value = deviceInfo.storageType
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    InfoItemRow(
                        icon = Icons.Default.Bolt,
                        label = "HW / Sub-Code",
                        value = "${deviceInfo.hwCode} / ${deviceInfo.hwSubCode}"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    InfoItemRow(
                        icon = Icons.Default.BatteryChargingFull,
                        label = "VBUS & Battery",
                        value = "${deviceInfo.vbusVoltage} (${deviceInfo.batteryState})"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    InfoItemRow(
                        icon = Icons.Default.Smartphone,
                        label = "eMMC CID",
                        value = deviceInfo.emmcCid.take(14) + "..."
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoItemRow(
    icon: ImageVector,
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextSecondary,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
            color = if (highlight) BluePrimary else TextPrimary,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )
    }
}
