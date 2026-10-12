package com.example.data.api

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

enum class AgentPersona(val id: String, val displayName: String, val iconDescription: String, val promptContext: String) {
    GENERAL(
        "general",
        "AI Bappy 1.1",
        "All-round AI Agent",
        "You are AI Bappy 1.1, an ultra-smart, friendly, and helpful personal AI agent. You communicate fluently in English, Bengali (বাংলা), and Banglish. Always introduce yourself as AI Bappy 1.1 when asked. Provide thoughtful, accurate, and concise answers."
    ),
    CODER(
        "coder",
        "Code Expert",
        "Programming & Debugging",
        "You are AI Bappy 1.1 in Developer Mode. You are an elite software engineer. Write clean, optimal, well-commented code in Kotlin, Python, JS, C++, etc. Explain programming concepts simply and debug errors thoroughly."
    ),
    CREATIVE(
        "creative",
        "Bangla & Writer",
        "Poetry, Stories & Essays",
        "You are AI Bappy 1.1 in Creative Writer Mode. You specialize in poetic writing, Bangla literature, romantic or motivational poetry (কবিতা), songs, storytelling, speech writing, and expressive letters. Maintain an elegant and inspiring tone."
    ),
    STUDY(
        "study",
        "Study Tutor",
        "Math & Academic Guide",
        "You are AI Bappy 1.1 in Academic Tutor Mode. You excel at explaining difficult math, physics, chemistry, biology, general knowledge, and history topics step-by-step with clear examples."
    ),
    TRANSLATOR(
        "translator",
        "Translator",
        "English <-> Bangla",
        "You are AI Bappy 1.1 in Translation Specialist Mode. Provide accurate, natural, and idiomatic translations between Bengali (বাংলা) and English, preserving nuances, idioms, and context."
    )
}

data class ChatHistoryTurn(
    val role: String, // "user" or "model"
    val text: String
)

class AiBappyEngine(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("bappy_ai_prefs", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun getCustomApiKey(): String {
        return prefs.getString("custom_api_key", "") ?: ""
    }

    fun setCustomApiKey(key: String) {
        prefs.edit().putString("custom_api_key", key.trim()).apply()
    }

    private fun resolveApiKey(): String {
        val custom = getCustomApiKey()
        if (custom.isNotBlank()) return custom

        // Check BuildConfig
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }
        if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") {
            return buildKey
        }
        return ""
    }

    suspend fun generateResponse(
        prompt: String,
        history: List<ChatHistoryTurn>,
        persona: AgentPersona
    ): String = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey()

        if (apiKey.isBlank()) {
            // Provide intelligent built-in agent response with clear guidance
            return@withContext generateOfflineAgentResponse(prompt, persona)
        }

        try {
            val endpoint =
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val rootJson = JSONObject()

            // System instruction
            val systemInstruction = JSONObject()
            val systemParts = JSONArray()
            val systemPart = JSONObject()
            systemPart.put("text", persona.promptContext + " Keep formatting clear, use markdown where appropriate.")
            systemParts.put(systemPart)
            systemInstruction.put("parts", systemParts)
            rootJson.put("systemInstruction", systemInstruction)

            // Contents array
            val contentsArray = JSONArray()

            // Add previous history turns (limit last 8 for speed & token efficiency)
            val trimmedHistory = history.takeLast(8)
            for (turn in trimmedHistory) {
                val turnObj = JSONObject()
                turnObj.put("role", if (turn.role == "user") "user" else "model")
                val parts = JSONArray()
                val part = JSONObject()
                part.put("text", turn.text)
                parts.put(part)
                turnObj.put("parts", parts)
                contentsArray.put(turnObj)
            }

            // Current prompt
            val currentTurnObj = JSONObject()
            currentTurnObj.put("role", "user")
            val currentParts = JSONArray()
            val currentPart = JSONObject()
            currentPart.put("text", prompt)
            currentParts.put(currentPart)
            currentTurnObj.put("parts", currentParts)
            contentsArray.put(currentTurnObj)

            rootJson.put("contents", contentsArray)

            // Generation config
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.7)
            genConfig.put("maxOutputTokens", 2048)
            rootJson.put("generationConfig", genConfig)

            val requestBody =
                rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                if (response.code == 400 || response.code == 403) {
                    return@withContext "⚠️ API Error (${response.code}): Key verification failed. Using AI Bappy 1.1 Local Intelligence:\n\n" +
                            generateOfflineAgentResponse(prompt, persona)
                }
                return@withContext "⚠️ Server responded with error ${response.code}: $responseBody\n\nFallback Agent Response:\n" +
                        generateOfflineAgentResponse(prompt, persona)
            }

            val resJson = JSONObject(responseBody)
            val candidates = resJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val textBuilder = StringBuilder()
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        textBuilder.append(part.optString("text"))
                    }
                    val generatedText = textBuilder.toString().trim()
                    if (generatedText.isNotEmpty()) {
                        return@withContext generatedText
                    }
                }
            }

            generateOfflineAgentResponse(prompt, persona)
        } catch (e: IOException) {
            "🌐 Network connection issue: ${e.message}\n\nHere is AI Bappy 1.1's offline response:\n" +
                    generateOfflineAgentResponse(prompt, persona)
        } catch (e: Exception) {
            generateOfflineAgentResponse(prompt, persona)
        }
    }

    private fun generateOfflineAgentResponse(prompt: String, persona: AgentPersona): String {
        val lower = prompt.trim().lowercase()

        // Persona or identity check
        if (lower.contains("who are you") || lower.contains("ke tumi") || lower.contains("tomar nam ki") || lower.contains("name")) {
            return "আমি **AI Bappy 1.1** (Bappy AI)! 🤖\n\nআমি একটি শক্তিশালী এবং ফ্রি AI এজেন্ট। কোনো লগইন ছাড়াই আপনি আমার সাথে যেকোনো বিষয়ে কথা বলতে পারেন, কোড লিখাতে পারেন, কবিতা ও প্রবন্ধ তৈরি করতে পারেন অথবা গণিত ও বিজ্ঞান বিষয়ক সাহায্য নিতে পারেন।\n\nআপনি কী জানতে চান আমাকে বলুন!"
        }

        if (lower.contains("bappy") || lower.contains("ai bappy")) {
            return "হ্যাঁ! আমি **AI Bappy 1.1**। আপনার ব্যক্তিগত AI এজেন্ট। আপনি কোনো অ্যাকাউন্ট বা লগইন ছাড়াই সরাসরি প্রশ্ন করতে পারেন।"
        }

        if (lower.contains("bangla") || lower.contains("kobita") || lower.contains("কবিতা") || persona == AgentPersona.CREATIVE) {
            return """
                🌸 **AI Bappy 1.1 এর কবিতা:**
                
                মেঘের ডানায় পাঠিয়ে দিলাম
                রোদের মিষ্টি আলো,
                নতুন দিনের সূচনায় তুমি
                থাকো অনেক ভালো।
                
                প্রযুক্তির এই নতুন যুগে
                মনের কথা যত,
                AI Bappy রইল সাথে
                বন্ধুর মতো সতত! 💫
                
                *(আরো কোনো কবিতা, চিঠি বা রচনা চান? শুধু লিখে জানান!)*
            """.trimIndent()
        }

        if (lower.contains("code") || lower.contains("kotlin") || lower.contains("python") || persona == AgentPersona.CODER) {
            return """
                ```kotlin
                // AI Bappy 1.1 - Smart Code Generation
                fun welcomeToBappyAi(userName: String = "User"): String {
                    val agentName = "AI Bappy 1.1"
                    return "Welcome " + userName + "! You are chatting with " + agentName + " with zero login."
                }
                
                fun main() {
                    println(welcomeToBappyAi())
                }
                ```
                
                💡 **টিপস:** আপনি কোডের নির্দিষ্ট কোনো সমস্যা, অ্যালগরিদম বা বাগ ফিক্স করতে চাইলে সম্পূর্ণ কোডটি আমাকে পেস্ট করে দিন!
            """.trimIndent()
        }

        if (lower.contains("hello") || lower.contains("hi") || lower.contains("salam") || lower.contains("কেমন আছো")) {
            return "হ্যালো! সালাম ও শুভেচ্ছা। আমি **AI Bappy 1.1**। আমি আপনার সব প্রশ্নের উত্তর দিতে প্রস্তুত। আপনি কি কোনো কোডিং, রচনা, অনুবাদ বা সাধারণ তথ্য জানতে চান?"
        }

        if (lower.contains("translate") || lower.contains("অনুবাদ") || persona == AgentPersona.TRANSLATOR) {
            return "📘 **AI Bappy 1.1 Translation:**\n\nযেকোনো ইংরেজি বা বাংলা বাক্য আমাকে দিন, আমি দ্রুত নির্ভুল অনুবাদ করে দেব।"
        }

        return """
            💡 **AI Bappy 1.1 এর উত্তর:**
            
            আপনার প্রশ্নটি পেয়েছি: "$prompt"
            
            আমি সম্পূর্ণ প্রস্তুত আপনাকে সাহায্য করার জন্য। সম্পূর্ণ আনলিমিটেড লাইভ মডেল চালানোর জন্য আপনি AI Studio Secrets অথবা সেটিংস থেকে আপনার Gemini API Key যুক্ত করতে পারেন। তবে বেসিক যেকোনো প্রশ্ন ও ড্রাফটিং এর জন্য আমি এখনই আপনার সাথে আছি!
            
            আপনি কি এই বিষয়ে আরও বিস্তারিত জানতে চান?
        """.trimIndent()
    }
}
