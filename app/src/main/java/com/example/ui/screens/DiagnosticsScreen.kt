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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisVoid
import com.example.viewmodel.JarvisViewModel

@Composable
fun DiagnosticsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.telemetry.collectAsState()
    val isMicAvailable = viewModel.speechManager.isRecognitionAvailable

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisVoid)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SYSTEM TELEMETRY",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = JarvisCyan,
                    letterSpacing = 1.5.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "MARK VII PROTOCOL // STATUS: NOMINAL",
                    fontSize = 10.sp,
                    color = JarvisTextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(JarvisSuccess.copy(alpha = 0.15f))
                    .border(1.dp, JarvisSuccess, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "SEC LEVEL: ALPHA",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = JarvisSuccess,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cognitive Architecture Section
        DiagnosticSectionTitle(title = "NEURAL COGNITION MATRIX", icon = Icons.Default.Psychology)
        DiagnosticInfoCard {
            DiagnosticMetricRow("AI Core Engine", "Gemini 3.8-Flash (Firebase AI Logic)")
            DiagnosticMetricRow("Security Attestation", "App Check Protected")
            DiagnosticMetricRow("Inference Protocol", "Streaming Tokens (Real-Time)")
            DiagnosticMetricRow("Assistant Personality", "J.A.R.V.I.S. (Polite Butler)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hardware Subsystems Section
        DiagnosticSectionTitle(title = "HARDWARE SUBSYSTEMS", icon = Icons.Default.Memory)
        DiagnosticInfoCard {
            DiagnosticMetricRow("Power Reserve", "${telemetry.batteryPct}% (${if (telemetry.isCharging) "Charging" else telemetry.batteryHealth})")
            DiagnosticMetricRow("Memory Allocation", "${telemetry.totalMemMB - telemetry.freeMemMB}MB Used / ${telemetry.totalMemMB}MB Total")
            DiagnosticMetricRow("Uplink Interface", telemetry.networkStatus)
            DiagnosticMetricRow("Illumination Core", if (telemetry.isTorchActive) "Engaged" else "Offline")
            DiagnosticMetricRow("Audio Sensors (STT)", if (isMicAvailable) "Hardware Ready" else "Service Unavailable")
            DiagnosticMetricRow("Voice Synthesizer (TTS)", "English (UK Accent) Active")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Diagnostic Diagnostics Actions
        DiagnosticSectionTitle(title = "DIAGNOSTIC SUBSYSTEM TESTS", icon = Icons.Default.Build)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    viewModel.processVoiceCommand("Run system diagnostics")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("run_diagnostics_test_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceElevated, contentColor = JarvisCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("RUN FULL TELEMETRY AUDIT", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.replayResponse("All auditory synthesizers functioning within peak parameters, sir.")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("test_audio_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("TEST AUDIO", fontSize = 11.sp, color = JarvisCyan, fontFamily = FontFamily.Monospace)
                }

                OutlinedButton(
                    onClick = {
                        viewModel.processVoiceCommand("toggle flashlight")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("test_torch_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FlashlightOn, contentDescription = null, tint = JarvisGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CYCLE TORCH", fontSize = 11.sp, color = JarvisGold, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun DiagnosticSectionTitle(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier.padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = JarvisCyan,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = JarvisTextSecondary,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun DiagnosticInfoCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(JarvisSurface)
            .border(1.dp, JarvisBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            content()
        }
    }
}

@Composable
private fun DiagnosticMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = JarvisTextSecondary
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = JarvisTextPrimary,
            fontFamily = FontFamily.Monospace
        )
    }
}
