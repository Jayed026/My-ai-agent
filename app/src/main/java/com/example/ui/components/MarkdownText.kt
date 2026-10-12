package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan

@Composable
fun FormattedContent(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val context = LocalContext.current

    // Parse code blocks vs regular paragraphs
    val parts = text.split("```")

    Column(modifier = modifier) {
        for (i in parts.indices) {
            val part = parts[i]
            if (part.isBlank()) continue

            if (i % 2 == 1) {
                // Code block
                val lines = part.trim().lines()
                val lang = if (lines.isNotEmpty() && lines[0].matches(Regex("^[a-zA-Z0-9_-]+$"))) lines[0] else "code"
                val codeBody = if (lines.isNotEmpty() && lines[0] == lang) lines.drop(1).joinToString("\n") else part.trim()

                CodeBlock(
                    language = lang,
                    code = codeBody,
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Code", codeBody))
                        Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
            } else {
                // Regular markdown text
                RegularMarkdownBlock(text = part, textColor = textColor)
            }
        }
    }
}

@Composable
fun CodeBlock(
    language: String,
    code: String,
    onCopy: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0D121F))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161E2E))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy code",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = code,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = Color(0xFFE2E8F0),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun RegularMarkdownBlock(
    text: String,
    textColor: Color
) {
    val lines = text.trim().lines()

    Column {
        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                continue
            }

            if (trimmedLine.startsWith("• ") || trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ")) {
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(
                        text = "•",
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = parseInlineMarkdown(trimmedLine.substring(2)),
                        color = textColor,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }
            } else if (trimmedLine.startsWith("### ")) {
                Text(
                    text = parseInlineMarkdown(trimmedLine.removePrefix("### ")),
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else if (trimmedLine.startsWith("## ")) {
                Text(
                    text = parseInlineMarkdown(trimmedLine.removePrefix("## ")),
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else if (trimmedLine.startsWith("# ")) {
                Text(
                    text = parseInlineMarkdown(trimmedLine.removePrefix("# ")),
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                Text(
                    text = parseInlineMarkdown(trimmedLine),
                    color = textColor,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

fun parseInlineMarkdown(text: String) = buildAnnotatedString {
    var cursor = 0
    val boldPattern = Regex("\\*\\*(.*?)\\*\\*")
    val inlineCodePattern = Regex("`(.*?)`")

    val matches = (boldPattern.findAll(text).map { Triple(it.range, "bold", it.groupValues[1]) } +
            inlineCodePattern.findAll(text).map { Triple(it.range, "code", it.groupValues[1]) })
        .sortedBy { it.first.first }

    for (match in matches) {
        val range = match.first
        if (range.first > cursor) {
            append(text.substring(cursor, range.first))
        }

        when (match.second) {
            "bold" -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                    append(match.third)
                }
            }
            "code" -> {
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = Color(0xFF1E293B),
                        color = NeonCyan,
                        fontSize = 13.sp
                    )
                ) {
                    append(" ${match.third} ")
                }
            }
        }
        cursor = range.last + 1
    }

    if (cursor < text.length) {
        append(text.substring(cursor))
    }
}
