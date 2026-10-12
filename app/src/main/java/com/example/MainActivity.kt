package com.example

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AgentPersonaSelector
import com.example.ui.components.ChatBubble
import com.example.ui.components.ChatDrawerContent
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TypingIndicator
import com.example.ui.components.WelcomeAgentHero
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BappyAiApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BappyAiApp(viewModel: ChatViewModel = viewModel()) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val currentConversationId by viewModel.currentConversationId.collectAsStateWithLifecycle()
    val conversations by viewModel.allConversations.collectAsStateWithLifecycle()
    val messages by viewModel.currentMessages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val selectedPersona by viewModel.selectedPersona.collectAsStateWithLifecycle()
    val drawerSearchQuery by viewModel.drawerSearchQuery.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()

    var showSettingsDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Voice recognition launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.onInputTextChanged(spokenText)
            }
        }
    }

    BackHandler(enabled = drawerState.isOpen) {
        coroutineScope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DarkBackground
            ) {
                ChatDrawerContent(
                    conversations = conversations,
                    currentConversationId = currentConversationId,
                    searchQuery = drawerSearchQuery,
                    onSearchQueryChange = viewModel::onDrawerSearchQueryChanged,
                    onConversationSelect = viewModel::selectConversation,
                    onNewChatClick = viewModel::startNewChat,
                    onDeleteConversation = viewModel::deleteConversation,
                    onTogglePin = viewModel::togglePinConversation,
                    onRenameConversation = viewModel::renameConversation,
                    onClearAll = viewModel::clearAllConversations,
                    onCloseDrawer = { coroutineScope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(NeonCyan, NeonViolet))
                                    )
                                    .padding(1.5.dp)
                                    .clip(CircleShape)
                                    .background(DarkBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Bappy AI",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF00E676))
                                    )
                                }
                                Text(
                                    text = "AI Bappy 1.1 • Ready",
                                    fontSize = 11.sp,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("menu_drawer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Chat History",
                                tint = TextPrimary
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.startNewChat() },
                            modifier = Modifier.testTag("top_new_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New Chat",
                                tint = NeonCyan
                            )
                        }

                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier.testTag("settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = TextSecondary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkBackground,
                        titleContentColor = TextPrimary
                    )
                )
            },
            containerColor = DarkBackground
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
            ) {
                // Agent persona selector pills
                AgentPersonaSelector(
                    selectedPersona = selectedPersona,
                    onPersonaSelected = viewModel::setPersona
                )

                // Message Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (messages.isEmpty() && !isLoading) {
                        WelcomeAgentHero(
                            onPromptClick = { prompt ->
                                viewModel.sendMessage(prompt)
                            }
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(messages, key = { it.id }) { msg ->
                                ChatBubble(
                                    message = msg,
                                    isSpeaking = isSpeaking,
                                    onSpeakClick = viewModel::speakText,
                                    onStopSpeaking = viewModel::stopSpeaking,
                                    onRegenerate = viewModel::regenerateLastResponse
                                )
                            }

                            if (isLoading) {
                                item {
                                    TypingIndicator()
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }

                // Bottom Input Bar
                Surface(
                    color = DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Voice input button
                        IconButton(
                            onClick = {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to AI Bappy 1.1")
                                }
                                try {
                                    speechRecognizerLauncher.launch(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Voice input not available", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B))
                                .testTag("voice_input_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Input",
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Text Field
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = viewModel::onInputTextChanged,
                            placeholder = {
                                Text(
                                    text = "Ask AI Bappy 1.1 anything...",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            },
                            maxLines = 4,
                            shape = RoundedCornerShape(22.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A),
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = DarkSurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_field")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Send Button
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank() && !isLoading) {
                                    viewModel.sendMessage(inputText)
                                }
                            },
                            enabled = inputText.isNotBlank() && !isLoading,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    if (inputText.isNotBlank() && !isLoading) NeonCyan else Color(0xFF1E293B)
                                )
                                .testTag("send_message_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = NeonCyan,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send message",
                                    tint = if (inputText.isNotBlank()) Color(0xFF00363D) else Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentApiKey = viewModel.getCustomApiKey(),
            onSaveApiKey = viewModel::saveCustomApiKey,
            onDismiss = { showSettingsDialog = false }
        )
    }
}
