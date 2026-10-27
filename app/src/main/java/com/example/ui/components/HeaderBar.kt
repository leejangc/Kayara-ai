package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DimText
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.InkBackground
import com.example.ui.theme.LineBorder
import com.example.ui.theme.MistText
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.PanelSurface
import com.example.ui.theme.RoseDark
import com.example.ui.theme.RosePrimary

@Composable
fun HeaderBar(
    isAutoReadActive: Boolean,
    onToggleAutoRead: () -> Unit,
    onOpenSettings: () -> Unit,
    companionName: String = "Kayara",
    personalityPresetId: String = "partner",
    modifier: Modifier = Modifier
) {
    val preset = com.example.data.model.PRESET_PERSONALITIES.find { it.id == personalityPresetId }

    val pulseTransition = rememberInfiniteTransition(label = "avatarPulse")
    val avatarScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatarScale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(InkBackground.copy(alpha = 0.96f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Elegant Holographic Avatar Orb
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .scale(avatarScale)
                    .shadow(elevation = 16.dp, shape = CircleShape, spotColor = RosePrimary)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(GoldAccent, RosePrimary, ElectricViolet, RoseDark)
                        )
                    )
                    .border(1.5.dp, GoldAccent.copy(alpha = 0.7f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = preset?.emoji ?: "✨",
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Companion Brand & Live Intelligence Status
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = companionName.ifBlank { "Kayara" },
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        letterSpacing = 0.5.sp,
                        color = MistText
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonEmerald.copy(alpha = 0.18f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LIVE",
                            color = NeonEmerald,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                Text(
                    text = "${preset?.title ?: "AI Companion"} • Brain Sync Active",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoldAccent,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Speak replies toggle
            IconButton(
                onClick = onToggleAutoRead,
                modifier = Modifier
                    .testTag("toggle_read_aloud_button")
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isAutoReadActive) RosePrimary else PanelSurface)
                    .border(1.dp, if (isAutoReadActive) GoldAccent.copy(alpha = 0.5f) else LineBorder, CircleShape)
            ) {
                Icon(
                    imageVector = if (isAutoReadActive) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeMute,
                    contentDescription = if (isAutoReadActive) "Speak replies enabled" else "Speak replies muted",
                    tint = if (isAutoReadActive) Color.White else DimText,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Settings button
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .testTag("open_settings_button")
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(PanelSurface)
                    .border(1.dp, LineBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Open Settings",
                    tint = MistText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        HorizontalDivider(color = LineBorder, thickness = 1.dp)
    }
}
