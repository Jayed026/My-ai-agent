package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatMessageEntity
import com.example.ui.theme.AssistantBubbleColor
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UserBubbleColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatBubble(
    message: ChatMessageEntity,
    isSpeaking: Boolean,
    onSpeakClick: (String) -> Unit,
    onStopSpeaking: () -> Unit,
    onRegenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == "user"
    val context = LocalContext.current
    val timeFormatted = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            // AI Bappy 1.1 Avatar
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(NeonCyan, NeonViolet)
                        )
                    )
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Bappy 1.1",
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 330.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // Label above bubble
            if (!isUser) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 3.dp, start = 4.dp)
                ) {
                    Text(
                        text = "AI Bappy 1.1",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "AGENT v1.1",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonViolet
                        )
                    }
                }
            }

            // Message container
            Surface(
                shape = RoundedCornerShape(
                    topStart = if (isUser) 18.dp else 4.dp,
                    topEnd = if (isUser) 4.dp else 18.dp,
                    bottomStart = 18.dp,
                    bottomEnd = 18.dp
                ),
                color = if (isUser) UserBubbleColor else AssistantBubbleColor,
                border = if (isUser) null else androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                shadowElevation = 2.dp,
                modifier = Modifier.testTag(if (isUser) "user_message_bubble" else "assistant_message_bubble")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    FormattedContent(
                        text = message.content,
                        textColor = if (isUser) Color.White else TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = timeFormatted,
                        fontSize = 10.sp,
                        color = if (isUser) Color(0xFFC7D2FE) else TextSecondary,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            // Action row for Assistant responses
            if (!isUser) {
                Row(
                    modifier = Modifier.padding(top = 2.dp, start = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Copy
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Bappy AI", message.content))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy message",
                            tint = TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Share
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "${message.content}\n\n— Generated by Bappy AI (AI Bappy 1.1)")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share AI Response"))
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // TTS Speaker
                    IconButton(
                        onClick = {
                            if (isSpeaking) {
                                onStopSpeaking()
                            } else {
                                onSpeakClick(message.content)
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Listen",
                            tint = if (isSpeaking) NeonCyan else TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Regenerate
                    IconButton(
                        onClick = onRegenerate,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate",
                            tint = TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // User Avatar
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF312E81)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun TypingIndicator(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(NeonCyan, NeonViolet))
                )
                .padding(2.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI Bappy 1.1",
                tint = NeonCyan,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AssistantBubbleColor,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier.testTag("typing_indicator")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI Bappy 1.1 is thinking",
                    fontSize = 13.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                PulsingDots()
            }
        }
    }
}

@Composable
fun PulsingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    val dot1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val dot2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val dot3Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .offset(y = dot1Offset.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(NeonCyan)
        )
        Box(
            modifier = Modifier
                .offset(y = dot2Offset.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(NeonViolet)
        )
        Box(
            modifier = Modifier
                .offset(y = dot3Offset.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(NeonCyan)
        )
    }
}
