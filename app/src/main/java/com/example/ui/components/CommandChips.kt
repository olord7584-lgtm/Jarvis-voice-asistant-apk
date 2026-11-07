package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary

data class QuickCommand(
    val label: String,
    val command: String,
    val icon: ImageVector
)

val defaultQuickCommands = listOf(
    QuickCommand("Flashlight", "Toggle the flashlight", Icons.Default.FlashlightOn),
    QuickCommand("Power Level", "What is my battery level?", Icons.Default.BatteryChargingFull),
    QuickCommand("Diagnostics", "Run system diagnostics", Icons.Default.Speed),
    QuickCommand("Camera", "Open the camera", Icons.Default.CameraAlt),
    QuickCommand("YouTube", "Open YouTube", Icons.Default.PlayArrow),
    QuickCommand("Maps", "Open maps for directions", Icons.Default.Map),
    QuickCommand("Timer", "Set a timer for 1 minute", Icons.Default.Timer),
    QuickCommand("Log Directive", "Note: Upgrade Mark VII thruster protocols", Icons.Default.NoteAdd),
    QuickCommand("Web Search", "Search for latest space discoveries", Icons.Default.Search)
)

@Composable
fun CommandChipsRow(
    onCommandClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        defaultQuickCommands.forEach { cmd ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(JarvisSurfaceVariant)
                    .border(1.dp, JarvisBorder, RoundedCornerShape(18.dp))
                    .clickable { onCommandClick(cmd.command) }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
                    .testTag("chip_${cmd.label.lowercase().replace(" ", "_")}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = cmd.icon,
                    contentDescription = cmd.label,
                    tint = JarvisCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = cmd.label,
                    fontSize = 11.sp,
                    color = JarvisTextPrimary,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
