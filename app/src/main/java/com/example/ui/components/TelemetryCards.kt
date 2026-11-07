package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisSuccess
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.viewmodel.TelemetryData

@Composable
fun TelemetryCards(
    telemetry: TelemetryData,
    onToggleTorch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Battery Card
        TelemetryItemCard(
            modifier = Modifier.weight(1f).testTag("telemetry_battery_card"),
            title = "POWER",
            value = "${telemetry.batteryPct}%",
            subtext = if (telemetry.isCharging) "Arc Charging" else telemetry.batteryHealth,
            icon = if (telemetry.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
            progress = telemetry.batteryPct / 100f,
            accentColor = if (telemetry.batteryPct > 30) JarvisCyan else JarvisGold
        )

        // Memory Card
        TelemetryItemCard(
            modifier = Modifier.weight(1f).testTag("telemetry_memory_card"),
            title = "MEMORY",
            value = "${telemetry.memoryUsedPct}%",
            subtext = "${telemetry.freeMemMB}MB Free",
            icon = Icons.Default.Memory,
            progress = telemetry.memoryUsedPct / 100f,
            accentColor = JarvisCyan
        )

        // Torch Quick Toggle Card
        Box(
            modifier = Modifier
                .weight(1f)
                .testTag("telemetry_torch_card")
                .clip(RoundedCornerShape(14.dp))
                .background(if (telemetry.isTorchActive) JarvisCyan.copy(alpha = 0.15f) else JarvisSurfaceVariant)
                .border(
                    width = 1.dp,
                    color = if (telemetry.isTorchActive) JarvisCyan else JarvisBorder,
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable(onClick = onToggleTorch)
                .padding(10.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (telemetry.isTorchActive) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                        contentDescription = "Flashlight Toggle",
                        tint = if (telemetry.isTorchActive) JarvisCyan else JarvisTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TORCH",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisTextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (telemetry.isTorchActive) "ENGAGED" else "OFFLINE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (telemetry.isTorchActive) JarvisCyan else JarvisTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap to toggle",
                    fontSize = 9.sp,
                    color = JarvisTextSecondary
                )
            }
        }
    }
}

@Composable
private fun TelemetryItemCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    progress: Float,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(JarvisSurfaceVariant)
            .border(1.dp, JarvisBorder, RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = JarvisTextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = JarvisTextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = accentColor,
                trackColor = JarvisSurfaceElevated,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtext,
                fontSize = 9.sp,
                color = JarvisTextSecondary
            )
        }
    }
}
