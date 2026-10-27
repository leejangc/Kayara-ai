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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.DimText
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.InkBackground
import com.example.ui.theme.LineBorder
import com.example.ui.theme.MistText
import com.example.ui.theme.PanelSurface
import com.example.ui.theme.PanelSurfaceVariant
import com.example.ui.theme.RoseDark
import com.example.ui.theme.RosePrimary

@Composable
fun ChatInputBar(
    isListening: Boolean,
    isThinking: Boolean,
    onSendMessage: (String) -> Unit,
    onStartVoice: () -> Unit,
    onStopVoice: () -> Unit,
    onOpenLiveCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textValue by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val micScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        InkBackground.copy(alpha = 0.88f),
                        DeepSpace
                    )
                )
            )
            .navigationBarsPadding()
    ) {
        Column {
            HorizontalDivider(color = LineBorder, thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice Dictation Button with glowing scale
                IconButton(
                    onClick = {
                        if (isListening) onStopVoice() else onStartVoice()
                    },
                    modifier = Modifier
                        .testTag("dictate_mic_button")
                        .size(46.dp)
                        .scale(if (isListening) micScale else 1f)
                        .shadow(
                            elevation = if (isListening) 12.dp else 4.dp,
                            shape = CircleShape,
                            spotColor = RosePrimary
                        )
                        .clip(CircleShape)
                        .background(if (isListening) RosePrimary else PanelSurface)
                        .border(
                            1.dp,
                            if (isListening) GoldAccent else LineBorder,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Speak voice message",
                        tint = if (isListening) Color.White else DimText,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Text Input Capsule (Glassmorphism)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(PanelSurface)
                        .border(1.dp, LineBorder, CircleShape)
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (textValue.isEmpty()) {
                        Text(
                            text = if (isListening) "Listening to your voice..." else "Type to Kayara…",
                            style = TextStyle(
                                color = if (isListening) RosePrimary else DimText,
                                fontSize = 15.sp
                            )
                        )
                    }

                    BasicTextField(
                        value = textValue,
                        onValueChange = { textValue = it },
                        modifier = Modifier
                            .testTag("chat_text_input")
                            .fillMaxWidth(),
                        textStyle = TextStyle(
                            color = MistText,
                            fontSize = 15.sp
                        ),
                        cursorBrush = SolidColor(RosePrimary),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (textValue.isNotBlank() && !isThinking) {
                                    onSendMessage(textValue)
                                    textValue = ""
                                }
                            }
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Send Button
                IconButton(
                    onClick = {
                        if (textValue.isNotBlank() && !isThinking) {
                            onSendMessage(textValue)
                            textValue = ""
                        }
                    },
                    enabled = textValue.isNotBlank() && !isThinking,
                    modifier = Modifier
                        .testTag("send_chat_button")
                        .size(46.dp)
                        .shadow(
                            elevation = if (textValue.isNotBlank()) 8.dp else 0.dp,
                            shape = CircleShape,
                            spotColor = RosePrimary
                        )
                        .clip(CircleShape)
                        .background(
                            if (textValue.isNotBlank() && !isThinking) {
                                Brush.linearGradient(listOf(RosePrimary, ElectricViolet))
                            } else {
                                Brush.linearGradient(listOf(PanelSurfaceVariant, PanelSurfaceVariant))
                            }
                        )
                        .border(
                            1.dp,
                            if (textValue.isNotBlank() && !isThinking) RosePrimary.copy(alpha = 0.5f) else LineBorder,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (textValue.isNotBlank() && !isThinking) Color.White else DimText.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Live Audio Call Button (Hypnotic Gold-Rose Orb)
                IconButton(
                    onClick = onOpenLiveCall,
                    modifier = Modifier
                        .testTag("open_live_call_button")
                        .size(46.dp)
                        .shadow(elevation = 8.dp, shape = CircleShape, spotColor = GoldAccent)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(GoldAccent, RosePrimary)
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Start live voice call",
                        tint = InkBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
