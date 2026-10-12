package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.AgentPersona
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AgentPersonaSelector(
    selectedPersona: AgentPersona,
    onPersonaSelected: (AgentPersona) -> Unit,
    modifier: Modifier = Modifier
) {
    val personas = AgentPersona.values()

    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        personas.forEach { persona ->
            val isSelected = persona == selectedPersona
            val icon = getPersonaIcon(persona)

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) Color(0xFF1E293B) else Color(0xFF111827)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) NeonCyan else Color(0xFF1F2937),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onPersonaSelected(persona) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("persona_chip_${persona.id}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = persona.displayName,
                    tint = if (isSelected) NeonCyan else TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Box(modifier = Modifier.padding(start = 6.dp)) {
                    Text(
                        text = persona.displayName,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) TextPrimary else TextSecondary
                    )
                }
            }
        }
    }
}

fun getPersonaIcon(persona: AgentPersona): ImageVector {
    return when (persona) {
        AgentPersona.GENERAL -> Icons.Default.AutoAwesome
        AgentPersona.CODER -> Icons.Default.Code
        AgentPersona.CREATIVE -> Icons.Default.Create
        AgentPersona.STUDY -> Icons.Default.School
        AgentPersona.TRANSLATOR -> Icons.Default.Translate
    }
}
