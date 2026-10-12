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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class QuickPrompt(
    val title: String,
    val prompt: String,
    val icon: ImageVector
)

@Composable
fun WelcomeAgentHero(
    onPromptClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val samplePrompts = listOf(
        QuickPrompt("বাংলায় কবিতা", "বাংলায় একটি সুন্দর প্রেরণাদায়ী কবিতা লিখে দাও", Icons.Default.EditNote),
        QuickPrompt("Kotlin Code", "Write a clean Kotlin Jetpack Compose function for a custom countdown timer", Icons.Default.Code),
        QuickPrompt("Quick Translator", "Translate this to Bangla: 'Artificial Intelligence empowers human creativity'", Icons.Default.Translate),
        QuickPrompt("Study Explainer", "Explain how neural networks and transformers work in simple words", Icons.Default.Bolt)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Large glowing logo
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(NeonCyan.copy(alpha = 0.4f), Color.Transparent)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(NeonCyan, NeonViolet)
                        )
                    )
                    .padding(2.5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Bappy 1.1",
                    tint = NeonCyan,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "AI Bappy 1.1",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = 0.5.sp
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00E676))
            )
            Text(
                text = " Free Agent • No Login Required",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = NeonCyan
            )
        }

        Text(
            text = "Hello! I am your personal AI assistant. How can I help you today? বাংলায় অথবা ইংরেজিতে যেকোনো প্রশ্ন করতে পারেন।",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        Text(
            text = "QUICK SUGGESTIONS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B),
            letterSpacing = 1.sp,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp)
        )

        // Prompt grid / list
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            samplePrompts.forEach { item ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF131B2A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onPromptClick(item.prompt) }
                        .testTag("quick_prompt_${item.title}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(
                                text = item.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = item.prompt,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
