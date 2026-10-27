package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.MessageRole
import com.example.ui.theme.DimText
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LineBorder
import com.example.ui.theme.MistText
import com.example.ui.theme.PanelSurface
import com.example.ui.theme.PanelSurfaceVariant
import com.example.ui.theme.RoseDark
import com.example.ui.theme.RosePrimary

@Composable
fun MessageBubble(
    message: ChatMessage,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    when (message.role) {
        MessageRole.USER -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 310.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(
                                topStart = 22.dp,
                                topEnd = 22.dp,
                                bottomStart = 22.dp,
                                bottomEnd = 6.dp
                            ),
                            spotColor = RosePrimary,
                            ambientColor = ElectricViolet
                        )
                        .clip(
                            RoundedCornerShape(
                                topStart = 22.dp,
                                topEnd = 22.dp,
                                bottomStart = 22.dp,
                                bottomEnd = 6.dp
                            )
                        )
                        .background(
                            Brush.linearGradient(
                                listOf(RosePrimary, Color(0xFFC71585), ElectricViolet)
                            )
                        )
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = 22.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }

        MessageRole.MODEL -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Column(
                    modifier = Modifier.widthIn(max = 330.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(
                                    topStart = 22.dp,
                                    topEnd = 22.dp,
                                    bottomStart = 6.dp,
                                    bottomEnd = 22.dp
                                ),
                                spotColor = Color(0x33B886FF)
                            )
                            .clip(
                                RoundedCornerShape(
                                    topStart = 22.dp,
                                    topEnd = 22.dp,
                                    bottomStart = 6.dp,
                                    bottomEnd = 22.dp
                                )
                            )
                            .background(PanelSurface)
                            .border(
                                width = 1.dp,
                                color = LineBorder,
                                shape = RoundedCornerShape(
                                    topStart = 22.dp,
                                    topEnd = 22.dp,
                                    bottomStart = 6.dp,
                                    bottomEnd = 22.dp
                                )
                            )
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = formatMarkdownBold(message.text),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 23.sp
                            ),
                            color = MistText
                        )
                    }

                    // Message Action Bar
                    Row(
                        modifier = Modifier.padding(start = 6.dp, top = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onSpeak(message.text) },
                            modifier = Modifier
                                .testTag("speak_message_button_${message.id.take(6)}")
                                .size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Read aloud",
                                tint = DimText,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Kayara", message.text)
                                clipboard.setPrimaryClip(clip)
                            },
                            modifier = Modifier
                                .testTag("copy_message_button_${message.id.take(6)}")
                                .size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy message",
                                tint = DimText,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }
        }

        MessageRole.ERROR -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF38101E)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f)),
                    modifier = Modifier.widthIn(max = 330.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = ErrorRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFFD1DC)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThinkingIndicator(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "dotsTransition")

    val dot1Offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val dot2Offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 550, delayMillis = 160, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val dot3Offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 550, delayMillis = 320, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(PanelSurface)
                .border(1.dp, LineBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .offset(y = dot1Offset.dp)
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(RosePrimary)
                )
                Box(
                    modifier = Modifier
                        .offset(y = dot2Offset.dp)
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(GoldAccent)
                )
                Box(
                    modifier = Modifier
                        .offset(y = dot3Offset.dp)
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(ElectricViolet)
                )

                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Kayara is crafting thought...",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DimText,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
fun QuickPromptSuggestions(
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val suggestions = listOf(
        "Namaste Kayara! 🙏",
        "How is your day going? ✨",
        "Tell me a sweet story 📖",
        "Let's talk about Nepal 🏔️",
        "Give me deep advice ❤️",
        "What do you remember about me? 🧠"
    )

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(suggestions) { item ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(PanelSurfaceVariant)
                    .border(1.dp, LineBorder, RoundedCornerShape(18.dp))
                    .clickable { onSelect(item) }
                    .padding(horizontal = 16.dp, vertical = 9.dp)
            ) {
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MistText
                )
            }
        }
    }
}

fun formatMarkdownBold(input: String) = buildAnnotatedString {
    val pattern = Regex("\\*\\*(.*?)\\*\\*")
    var lastIndex = 0
    for (match in pattern.findAll(input)) {
        append(input.substring(lastIndex, match.range.first))
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = GoldAccent)) {
            append(match.groupValues[1])
        }
        lastIndex = match.range.last + 1
    }
    if (lastIndex < input.length) {
        append(input.substring(lastIndex))
    }
}
