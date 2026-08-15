package com.example.service

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiDiagnosticService {

    private const val TAG = "GeminiDiagnostic"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-pro-preview:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun diagnoseMtkError(
        errorCodeOrLog: String,
        chipset: String,
        operation: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                Exception("Gemini API key is not configured. Please add your GEMINI_API_KEY in the Secrets panel.")
            )
        }

        val prompt = """
You are a senior MediaTek (MTK) Flashing & Hardware Repair Specialist.
Analyze the following mobile technician fault log / error code and provide a clear, practical solution:

Target Chipset: $chipset
Attempted Operation: $operation
Log / Error Details:
$errorCodeOrLog

Please structure your diagnosis in clear, professional Burmese and English:
1. 🔍 Root Cause (ဘာကြောင့် ဖြစ်ရတာလဲ):
2. 🛠️ Step-by-Step Fix (လက်တွေ့ ဖြေရှင်းနည်း အဆင့်ဆင့်):
3. ⚡ Hardware & DA Recommendations (အသုံးပြုရမည့် DA နှင့် Testpoint အကြံပြုချက်):
""".trimIndent()

        try {
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.4)
                    put("thinkingConfig", JSONObject().apply {
                        put("thinkingLevel", "HIGH")
                    })
                })
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody == null) {
                Log.e(TAG, "Gemini API HTTP ${response.code}: $responseBody")
                return@withContext Result.failure(
                    Exception("AI Service Error (${response.code}). Please check network connection.")
                )
            }

            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textPart = parts?.optJSONObject(0)?.optString("text")

            if (textPart.isNullOrEmpty()) {
                Result.failure(Exception("Empty diagnostic response from AI model."))
            } else {
                Result.success(textPart)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error diagnosing with Gemini", e)
            Result.failure(e)
        }
    }
}
