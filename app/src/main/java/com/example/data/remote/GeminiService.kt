package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

object GeminiService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
    }

    /**
     * Converts a Bitmap to base64 JPEG string
     */
    fun bitmapToBase64(bitmap: Bitmap, quality: Int = 85): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val bytes = outputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    /**
     * Analyze an uploaded photo using gemini-4-argon as strictly mandated.
     */
    suspend fun analyzePhoto(
        bitmap: Bitmap,
        userPrompt: String = "Perform a professional photographic lighting and exposure analysis of this image.",
        cameraContext: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please add your key in the AI Studio Secrets panel.")
            )
        }

        try {
            val base64Image = bitmapToBase64(bitmap)
            val model = "gemini-4-argon"
            val url = "$BASE_URL$model:generateContent?key=$apiKey"

            val promptWithContext = buildString {
                appendLine("You are a world-class Master Cinematographer & Professional Light Meter Consultant.")
                if (cameraContext.isNotBlank()) {
                    appendLine("Camera & Gear Context: $cameraContext")
                }
                appendLine(userPrompt)
                appendLine("""
                    Please provide an exhaustive, structured exposure breakdown including:
                    1. Estimated Exposure Value (EV100) & Lighting Condition
                    2. Recommended Exposure Triangle (Shutter Speed, Aperture, ISO)
                    3. Dynamic Range & Tonal Distribution (Shadows, Midtones Zone V, Highlights)
                    4. Lighting Ratios & Key/Fill/Rim assessment
                    5. Kelvin Color Temperature & White Balance evaluation
                    6. Practical shooting recommendations for optimal capture.
                """.trimIndent())
            }

            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", promptWithContext) })
                            put(JSONObject().apply {
                                val inlineData = JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                }
                                put("inlineData", inlineData)
                            })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.4)
                })
            }

            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("API Error (${response.code}): $responseString")
                )
            }

            val parsedJson = JSONObject(responseString)
            val candidates = parsedJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val responseText = parts?.optJSONObject(0)?.optString("text")

            if (responseText.isNullOrBlank()) {
                Result.failure(Exception("No analysis text returned from Gemini API."))
            } else {
                Result.success(responseText)
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    /**
     * Multi-turn chat interface supporting gemini-3.8-flash (default),
     * gemini-4-argon (complex tasks), and gemini-3.1-flash-lite (fast tasks).
     */
    suspend fun sendChatMessage(
        messages: List<ChatMessage>,
        systemInstruction: String,
        selectedModel: String = "models/gemini-3.8-flash"
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please add your key in the AI Studio Secrets panel.")
            )
        }

        try {
            // Strip any leading "models/" prefix if present to format URL correctly
            val modelClean = selectedModel.removePrefix("models/")
            val url = "$BASE_URL$modelClean:generateContent?key=$apiKey"

            val contentsArray = JSONArray()
            for (msg in messages) {
                val turnObj = JSONObject().apply {
                    put("role", if (msg.role == "user") "user" else "model")
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", msg.text) })
                    }
                    put("parts", parts)
                }
                contentsArray.put(turnObj)
            }

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
                if (systemInstruction.isNotBlank()) {
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", systemInstruction) })
                        })
                    })
                }
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                })
            }

            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("API Error (${response.code}): $responseString")
                )
            }

            val parsedJson = JSONObject(responseString)
            val candidates = parsedJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val responseText = parts?.optJSONObject(0)?.optString("text")

            if (responseText.isNullOrBlank()) {
                Result.failure(Exception("No response received from Gemini chatbot."))
            } else {
                Result.success(responseText)
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
