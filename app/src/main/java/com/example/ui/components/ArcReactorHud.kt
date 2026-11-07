package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisAlert
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisSuccess
import com.example.ui.theme.JarvisVoid
import com.example.viewmodel.AssistantMode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorHud(
    mode: AssistantMode,
    speechRms: Float,
    isSpeaking: Boolean,
    onReactorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "arc_reactor")

    // Outer slow rotation
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (mode) {
                    AssistantMode.PROCESSING -> 3000
                    AssistantMode.LISTENING -> 5000
                    AssistantMode.SPEAKING -> 6000
                    else -> 12000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_rotation"
    )

    // Inner counter rotation
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (mode) {
                    AssistantMode.PROCESSING -> 2000
                    AssistantMode.LISTENING -> 4000
                    else -> 8000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_rotation"
    )

    // Breathing pulse for core
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (mode == AssistantMode.LISTENING) 500 else 1800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_pulse"
    )

    // Dynamic color based on assistant mode
    val coreColor by animateColorAsState(
        targetValue = when (mode) {
            AssistantMode.LISTENING -> JarvisCyan
            AssistantMode.PROCESSING -> JarvisGold
            AssistantMode.SPEAKING -> JarvisBlue
            AssistantMode.IDLE -> JarvisCyanGlow
        },
        label = "core_color"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .testTag("arc_reactor_touch_zone")
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, radius = 120.dp),
                    onClick = onReactorClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(240.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                val baseRadius = size.width / 2

                // 1. Ambient Background Glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            coreColor.copy(alpha = 0.28f * pulse),
                            coreColor.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = baseRadius
                    ),
                    radius = baseRadius,
                    center = center
                )

                // 2. Outer Segmented Ring
                drawArc(
                    color = coreColor.copy(alpha = 0.35f),
                    startAngle = outerRotation,
                    sweepAngle = 260f,
                    useCenter = false,
                    topLeft = Offset(12.dp.toPx(), 12.dp.toPx()),
                    size = Size(size.width - 24.dp.toPx(), size.height - 24.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )

                // Outer decorative ticks
                drawOuterTicks(center, baseRadius - 16.dp.toPx(), outerRotation, coreColor)

                // 3. Middle Rotating Arc Wedges
                val midRadius = baseRadius * 0.68f
                drawMiddleSegments(center, midRadius, innerRotation, coreColor)

                // 4. Equalizer Waveform Ring (Audio Reactive)
                val effectiveRms = if (mode == AssistantMode.LISTENING) speechRms else if (mode == AssistantMode.SPEAKING) (pulse - 0.85f) / 0.3f else 0.1f
                drawAudioEqualizerRing(center, baseRadius * 0.52f, effectiveRms, coreColor)

                // 5. Triangular / Hexagonal Core Geometry
                drawStarkTriCore(center, baseRadius * 0.35f * pulse, outerRotation * 0.5f, coreColor)

                // 6. Center Glowing Orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            coreColor,
                            coreColor.copy(alpha = 0.4f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = 28.dp.toPx() * pulse
                    ),
                    radius = 28.dp.toPx() * pulse,
                    center = center
                )
            }

            // Center Icon Overlay
            when (mode) {
                AssistantMode.LISTENING -> {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Microphone Active",
                        tint = JarvisVoid,
                        modifier = Modifier.size(24.dp)
                    )
                }
                AssistantMode.SPEAKING -> {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Jarvis Speaking",
                        tint = JarvisVoid,
                        modifier = Modifier.size(24.dp)
                    )
                }
                else -> Unit
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Status Badge Pill
        val statusText = when (mode) {
            AssistantMode.LISTENING -> "AUDIO SENSORS ENGAGED"
            AssistantMode.PROCESSING -> "COMPUTING TELEMETRY..."
            AssistantMode.SPEAKING -> "TRANSMITTING RESPONSE"
            AssistantMode.IDLE -> "MARK VII // ONLINE"
        }

        val badgeDotColor = when (mode) {
            AssistantMode.LISTENING -> JarvisSuccess
            AssistantMode.PROCESSING -> JarvisGold
            AssistantMode.SPEAKING -> JarvisBlue
            AssistantMode.IDLE -> JarvisCyan
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(JarvisVoid.copy(alpha = 0.8f))
                .border(1.dp, coreColor.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(badgeDotColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = statusText,
                color = coreColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

private fun DrawScope.drawOuterTicks(
    center: Offset,
    radius: Float,
    angleOffset: Float,
    color: Color
) {
    val tickCount = 24
    for (i in 0 until tickCount) {
        val angleDeg = (i * (360f / tickCount)) + angleOffset
        val angleRad = Math.toRadians(angleDeg.toDouble())
        val isMajor = i % 4 == 0
        val tickLength = if (isMajor) 10.dp.toPx() else 5.dp.toPx()

        val startX = center.x + (radius - tickLength) * cos(angleRad).toFloat()
        val startY = center.y + (radius - tickLength) * sin(angleRad).toFloat()
        val endX = center.x + radius * cos(angleRad).toFloat()
        val endY = center.y + radius * sin(angleRad).toFloat()

        drawLine(
            color = if (isMajor) color else color.copy(alpha = 0.4f),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
        )
    }
}

private fun DrawScope.drawMiddleSegments(
    center: Offset,
    radius: Float,
    angleOffset: Float,
    color: Color
) {
    val segments = 8
    val sweep = 28f
    for (i in 0 until segments) {
        val start = (i * (360f / segments)) + angleOffset
        drawArc(
            color = color.copy(alpha = 0.65f),
            startAngle = start,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

private fun DrawScope.drawAudioEqualizerRing(
    center: Offset,
    radius: Float,
    amplitude: Float,
    color: Color
) {
    val barCount = 18
    for (i in 0 until barCount) {
        val angleDeg = i * (360f / barCount)
        val angleRad = Math.toRadians(angleDeg.toDouble())
        // Vary heights around the circle based on amplitude
        val waveMod = sin((i * 1.5).toFloat()).coerceAtLeast(0f)
        val barHeight = (4.dp.toPx() + (18.dp.toPx() * amplitude * (0.4f + waveMod)))

        val startX = center.x + (radius - barHeight / 2) * cos(angleRad).toFloat()
        val startY = center.y + (radius - barHeight / 2) * sin(angleRad).toFloat()
        val endX = center.x + (radius + barHeight / 2) * cos(angleRad).toFloat()
        val endY = center.y + (radius + barHeight / 2) * sin(angleRad).toFloat()

        drawLine(
            color = color.copy(alpha = (0.5f + (amplitude * 0.5f)).coerceIn(0f, 1f)),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawStarkTriCore(
    center: Offset,
    radius: Float,
    rotation: Float,
    color: Color
) {
    val path = Path()
    for (i in 0..2) {
        val angle = Math.toRadians((i * 120.0) + rotation)
        val x = center.x + (radius * cos(angle)).toFloat()
        val y = center.y + (radius * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()

    drawPath(
        path = path,
        color = color.copy(alpha = 0.7f),
        style = Stroke(width = 2.5.dp.toPx())
    )
}
