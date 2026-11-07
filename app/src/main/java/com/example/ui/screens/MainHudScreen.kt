package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ArcReactorHud
import com.example.ui.components.CommandChipsRow
import com.example.ui.components.TelemetryCards
import com.example.ui.theme.JarvisAlert
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisBorderBright
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisSuccess
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisVoid
import com.example.viewmodel.AssistantMode
import com.example.viewmodel.JarvisViewModel

@Composable
fun MainHudScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val mode by viewModel.mode.collectAsState()
    val latestCommand by viewModel.latestCommand.collectAsState()
    val latestResponse by viewModel.latestResponse.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val notification by viewModel.actionNotification.collectAsState()

    val isListening by viewModel.speechManager.isListening.collectAsState()
    val speechRms by viewModel.speechManager.speechRms.collectAsState()
    val partialText by viewModel.speechManager.partialText.collectAsState()
    val speechError by viewModel.speechManager.lastError.collectAsState()

    val isSpeaking by viewModel.ttsManager.isSpeaking.collectAsState()
    val isMuted by viewModel.ttsManager.isMuted.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisVoid)
    ) {
        // Top HUD Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (mode == AssistantMode.LISTENING) JarvisSuccess else JarvisCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "J.A.R.V.I.S.",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = JarvisCyan,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "MARK VII // NEURAL INTERFACE ONLINE",
                    fontSize = 9.sp,
                    color = JarvisTextSecondary,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Refresh Telemetry
                IconButton(
                    onClick = { viewModel.refreshTelemetry() },
                    modifier = Modifier.size(36.dp).testTag("refresh_telemetry_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh telemetry",
                        tint = JarvisTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Voice Mute Toggle
                IconButton(
                    onClick = { viewModel.toggleMute() },
                    modifier = Modifier.size(36.dp).testTag("voice_mute_btn")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = if (isMuted) "Unmute Jarvis Voice" else "Mute Jarvis Voice",
                        tint = if (isMuted) JarvisAlert else JarvisCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Action Notification Banner
        AnimatedVisibility(
            visible = notification != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            notification?.let { notif ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(JarvisCyan.copy(alpha = 0.12f))
                        .border(1.dp, JarvisCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "PROTOCOL: ${notif.message}",
                        color = JarvisCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Main Scrollable HUD Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Central Animated Arc Reactor
            ArcReactorHud(
                mode = mode,
                speechRms = speechRms,
                isSpeaking = isSpeaking,
                onReactorClick = {
                    viewModel.toggleVoiceListening()
                },
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Speech Status Feedback
            if (isListening) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(JarvisSuccess.copy(alpha = 0.1f))
                        .border(1.dp, JarvisSuccess.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (partialText.isNotBlank()) "Hearing: \"$partialText\"" else "Listening for command... speak clearly, sir",
                        fontSize = 12.sp,
                        color = JarvisSuccess,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else if (speechError != null) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(JarvisAlert.copy(alpha = 0.1f))
                        .border(1.dp, JarvisAlert.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = speechError ?: "",
                        fontSize = 11.sp,
                        color = JarvisAlert,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User Command & Jarvis Response Console Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(JarvisSurface)
                    .border(1.dp, JarvisBorderBright, RoundedCornerShape(18.dp))
                    .padding(16.dp)
                    .testTag("conversation_hud_card")
            ) {
                Column {
                    // User Command Label
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "COMMAND DIRECTIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisTextSecondary,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(JarvisSurfaceElevated)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "INPUT",
                                fontSize = 9.sp,
                                color = JarvisCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "\"$latestCommand\"",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = JarvisTextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(JarvisBorder)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Jarvis Response
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isSpeaking) JarvisBlue else JarvisCyan)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "J.A.R.V.I.S. RESPONSE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisCyan,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Replay / Stop voice speech button
                        Row {
                            if (isSpeaking) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(JarvisAlert.copy(alpha = 0.2f))
                                        .border(1.dp, JarvisAlert, RoundedCornerShape(6.dp))
                                        .clickable { viewModel.stopSpeaking() }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .testTag("stop_speaking_btn"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = "Stop speaking",
                                        tint = JarvisAlert,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "SILENCE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = JarvisAlert,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(JarvisCyan.copy(alpha = 0.15f))
                                        .border(1.dp, JarvisCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                        .clickable { viewModel.replayResponse(latestResponse) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .testTag("replay_voice_btn"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Replay voice",
                                        tint = JarvisCyan,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "SPEAK",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = JarvisCyan,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = latestResponse,
                        fontSize = 14.sp,
                        color = JarvisTextPrimary,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hardware Telemetry Section
            TelemetryCards(
                telemetry = telemetry,
                onToggleTorch = {
                    viewModel.processVoiceCommand("toggle flashlight")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Voice Command Chips
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "QUICK VOICE PROTOCOLS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = JarvisTextSecondary,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
                )

                CommandChipsRow(
                    onCommandClick = { cmd ->
                        viewModel.processVoiceCommand(cmd)
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Bottom Input Dock & Floating Mic Control
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(JarvisSurface)
                .border(1.dp, JarvisBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Text Input for manual commands
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            text = "Speak or enter command...",
                            fontSize = 13.sp,
                            color = JarvisTextSecondary
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("command_text_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = JarvisSurfaceVariant,
                        unfocusedContainerColor = JarvisSurfaceVariant,
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisBorder,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        cursorColor = JarvisCyan
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (textInput.isNotBlank()) {
                                viewModel.processVoiceCommand(textInput)
                                textInput = ""
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Send or Mic Button
                if (textInput.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(JarvisCyan)
                            .clickable {
                                viewModel.processVoiceCommand(textInput)
                                textInput = ""
                            }
                            .testTag("send_command_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Command",
                            tint = JarvisVoid,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isListening) JarvisSuccess else JarvisCyan)
                            .clickable {
                                viewModel.toggleVoiceListening()
                            }
                            .testTag("voice_mic_fab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = if (isListening) "Stop Listening" else "Start Voice Command",
                            tint = JarvisVoid,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}
