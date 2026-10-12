package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ConversationEntity
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatDrawerContent(
    conversations: List<ConversationEntity>,
    currentConversationId: String?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onConversationSelect: (String) -> Unit,
    onNewChatClick: () -> Unit,
    onDeleteConversation: (String) -> Unit,
    onTogglePin: (String, Boolean) -> Unit,
    onRenameConversation: (String, String) -> Unit,
    onClearAll: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    var renameTargetId by remember { mutableStateOf<String?>(null) }
    var renameTargetTitle by remember { mutableStateOf("") }
    var showClearConfirm by remember { mutableStateOf(false) }

    val filteredConversations = if (searchQuery.isBlank()) {
        conversations
    } else {
        conversations.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(320.dp)
            .background(DarkBackground)
            .padding(vertical = 16.dp)
    ) {
        // App branding & Close
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Bappy AI",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "AI Bappy 1.1 Agent",
                    fontSize = 11.sp,
                    color = NeonCyan
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onCloseDrawer) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close drawer",
                    tint = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // New Chat Button
        Button(
            onClick = {
                onNewChatClick()
                onCloseDrawer()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1E293B),
                contentColor = NeonCyan
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("new_chat_button")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Chat",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "New Chat",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search conversations...", fontSize = 12.sp, color = TextSecondary) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF111827),
                unfocusedContainerColor = Color(0xFF111827),
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = DarkSurfaceBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp)
                .testTag("search_conversations_input")
        )

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = DarkSurfaceBorder, thickness = 0.5.dp)

        // Conversation List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        ) {
            if (filteredConversations.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "No chats yet.\nStart asking AI Bappy 1.1!" else "No matching chats found.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(filteredConversations, key = { it.id }) { conv ->
                    val isSelected = conv.id == currentConversationId
                    val dateFormatted = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(conv.updatedAt))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) Color(0xFF1E293B) else Color.Transparent
                            )
                            .clickable {
                                onConversationSelect(conv.id)
                                onCloseDrawer()
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("conversation_item_${conv.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = if (isSelected) NeonCyan else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = conv.title,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = dateFormatted,
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }

                        // Actions
                        IconButton(
                            onClick = { onTogglePin(conv.id, conv.isPinned) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (conv.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                contentDescription = "Pin chat",
                                tint = if (conv.isPinned) NeonCyan else Color(0xFF475569),
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                renameTargetId = conv.id
                                renameTargetTitle = conv.title
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Rename chat",
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        IconButton(
                            onClick = { onDeleteConversation(conv.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete chat",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = DarkSurfaceBorder, thickness = 0.5.dp)

        // Footer with clear all and status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AI Bappy 1.1 • No Login",
                fontSize = 11.sp,
                color = TextSecondary
            )

            if (conversations.isNotEmpty()) {
                TextButton(
                    onClick = { showClearConfirm = true }
                ) {
                    Text("Clear All", fontSize = 11.sp, color = Color(0xFFEF4444))
                }
            }
        }
    }

    // Rename Dialog
    if (renameTargetId != null) {
        var newTitleInput by remember { mutableStateOf(renameTargetTitle) }
        AlertDialog(
            onDismissRequest = { renameTargetId = null },
            title = { Text("Rename Chat", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = newTitleInput,
                    onValueChange = { newTitleInput = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = renameTargetId
                        if (id != null && newTitleInput.isNotBlank()) {
                            onRenameConversation(id, newTitleInput.trim())
                        }
                        renameTargetId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("Save", color = Color(0xFF00363D))
                }
            },
            dismissButton = {
                TextButton(onClick = { renameTargetId = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = Color(0xFF161E2E)
        )
    }

    // Clear All Confirmation Dialog
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Chats?", color = TextPrimary) },
            text = {
                Text("This will permanently remove all chat histories from this device.", color = TextSecondary)
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAll()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Clear Everything", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = Color(0xFF161E2E)
        )
    }
}
