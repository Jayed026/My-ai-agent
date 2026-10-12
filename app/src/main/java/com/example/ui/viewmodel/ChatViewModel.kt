package com.example.ui.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.AgentPersona
import com.example.data.api.AiBappyEngine
import com.example.data.api.ChatHistoryTurn
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ConversationEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val chatDao = db.chatDao()
    private val engine = AiBappyEngine(application)

    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    private val _selectedPersona = MutableStateFlow(AgentPersona.GENERAL)
    val selectedPersona: StateFlow<AgentPersona> = _selectedPersona.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _drawerSearchQuery = MutableStateFlow("")
    val drawerSearchQuery: StateFlow<String> = _drawerSearchQuery.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    val allConversations: StateFlow<List<ConversationEntity>> =
        chatDao.getAllConversations().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val currentMessages: StateFlow<List<ChatMessageEntity>> =
        _currentConversationId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else chatDao.getMessagesForConversation(id)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    init {
        initTts(application)
        ensureDefaultConversation()
    }

    private fun initTts(application: Application) {
        tts = TextToSpeech(application) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsInitialized = true
                tts?.language = Locale.ENGLISH
            }
        }
    }

    private fun ensureDefaultConversation() {
        viewModelScope.launch {
            allConversations.collect { list ->
                if (_currentConversationId.value == null) {
                    if (list.isNotEmpty()) {
                        _currentConversationId.value = list.first().id
                    } else {
                        startNewChat()
                    }
                }
            }
        }
    }

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun onDrawerSearchQueryChanged(query: String) {
        _drawerSearchQuery.value = query
    }

    fun setPersona(persona: AgentPersona) {
        _selectedPersona.value = persona
    }

    fun selectConversation(id: String) {
        _currentConversationId.value = id
        stopSpeaking()
    }

    fun startNewChat(initialPrompt: String? = null) {
        viewModelScope.launch {
            stopSpeaking()
            val newConv = ConversationEntity(
                id = UUID.randomUUID().toString(),
                title = "New Chat",
                agentMode = _selectedPersona.value.id
            )
            chatDao.insertConversation(newConv)
            _currentConversationId.value = newConv.id

            if (!initialPrompt.isNullOrBlank()) {
                sendMessage(initialPrompt)
            }
        }
    }

    fun sendMessage(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty() || _isLoading.value) return

        val convId = _currentConversationId.value ?: return

        _inputText.value = ""
        _isLoading.value = true

        viewModelScope.launch {
            // 1. Save user message
            val userMsg = ChatMessageEntity(
                conversationId = convId,
                role = "user",
                content = trimmed
            )
            chatDao.insertMessage(userMsg)

            // Update conversation title if default
            val currentList = currentMessages.value
            if (currentList.isEmpty() || currentList.size <= 1) {
                val titleSnippet = if (trimmed.length > 28) trimmed.take(28) + "..." else trimmed
                chatDao.updateConversationTitle(convId, titleSnippet)
            } else {
                chatDao.updateConversationTitle(convId, currentList.firstOrNull()?.content?.take(28) ?: "Chat")
            }

            // Prepare history turns
            val historyTurns = currentList.map {
                ChatHistoryTurn(
                    role = if (it.role == "user") "user" else "model",
                    text = it.content
                )
            }

            // 2. Call AI Bappy Engine
            val responseText = engine.generateResponse(trimmed, historyTurns, _selectedPersona.value)

            // 3. Save assistant message
            val assistantMsg = ChatMessageEntity(
                conversationId = convId,
                role = "assistant",
                content = responseText
            )
            chatDao.insertMessage(assistantMsg)
            _isLoading.value = false
        }
    }

    fun regenerateLastResponse() {
        val convId = _currentConversationId.value ?: return
        val messages = currentMessages.value
        val lastUserMsg = messages.lastOrNull { it.role == "user" } ?: return

        // Delete last assistant message if present
        val lastMsg = messages.lastOrNull()
        if (lastMsg?.role == "assistant") {
            viewModelScope.launch {
                chatDao.deleteMessage(lastMsg.id)
                sendMessage(lastUserMsg.content)
            }
        } else {
            sendMessage(lastUserMsg.content)
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            chatDao.deleteConversation(id)
            if (_currentConversationId.value == id) {
                _currentConversationId.value = null
                // Select next or create new
                val remaining = allConversations.value.filter { it.id != id }
                if (remaining.isNotEmpty()) {
                    _currentConversationId.value = remaining.first().id
                } else {
                    startNewChat()
                }
            }
        }
    }

    fun togglePinConversation(id: String, currentPinned: Boolean) {
        viewModelScope.launch {
            chatDao.updateConversationPinned(id, !currentPinned)
        }
    }

    fun renameConversation(id: String, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            chatDao.updateConversationTitle(id, newTitle.trim())
        }
    }

    fun clearAllConversations() {
        viewModelScope.launch {
            chatDao.deleteAllConversations()
            startNewChat()
        }
    }

    fun speakText(text: String) {
        if (!isTtsInitialized || tts == null) return
        val cleanText = text.replace(Regex("[`*#_~]"), " ").take(400)
        _isSpeaking.value = true
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "BappyTts")
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun getCustomApiKey(): String = engine.getCustomApiKey()

    fun saveCustomApiKey(key: String) {
        engine.setCustomApiKey(key)
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }
}
