package com.github.jing332.tts_server_android.service.systts.help

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


class AIHelper {
    companion object {
        const val TAG = "AIHelper"
        @Serializable
        data class WorkflowRequest(
            val inputs: Map<String, String>,
            val response_mode: String,
            val user: String
        )

        @Serializable
        data class WorkflowResponse(
            val workflow_run_id: String,
            val task_id: String,
            val data: WorkflowData
        )

        @Serializable
        data class WorkflowData(
            val id: String,
            val workflow_id: String,
            val status: String,
            val outputs: Outputs,
            val error: String?,
            val elapsed_time: Double,
            val total_tokens: Int,
            val total_steps: Int,
            val created_at: Long,
            val finished_at: Long
        )

        @Serializable
        data class Outputs(
            val text: String
        )
//        fun runWorkflow(apiUrl: String, apiKey: String, request: WorkflowRequest): String = runBlocking {
//            val client = HttpClient(CIO){
//                engine {
//                    requestTimeout = 30000 // Increase the timeout to 60 seconds (60000 ms)
//                }
//            }
//            val json = Json {
//                ignoreUnknownKeys = true
//                prettyPrint = true
//                isLenient = true
//            }
//
//            try {
//                val response = client.post {
//                    url(apiUrl)
//                    headers {
//                        append("Authorization", "Bearer $apiKey")
//                        append("Content-Type", "application/json")
//                    }
//                    setBody(json.encodeToString(WorkflowRequest.serializer(), request))
//                }
//
//                Log.d("runWorkflow", "1")
//
//                val responseBody = response.bodyAsText()
//                val workflowRequest = json.decodeFromString<WorkflowResponse>(responseBody)
//                workflowRequest.data.outputs.text
//            } catch (e: Exception) {
//                Log.d("runWorkflow", "e ${e.message}")
//                e.printStackTrace()
////                throw e
//                ""
//            } finally {
//                client.close()
//            }
//        }
        fun runWorkflow(apiUrl: String, apiKey: String, request: WorkflowRequest): String {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val json = Json {
                ignoreUnknownKeys = true
//                prettyPrint = true
                isLenient = true
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.encodeToString(WorkflowRequest.serializer(), request).toRequestBody(mediaType)

            val httpRequest = Request.Builder()
                .url(apiUrl)
                .header("Authorization", "Bearer $apiKey")
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build()

            try {
                client.newCall(httpRequest).execute().use { response ->
                    if (!response.isSuccessful) throw Exception("Unexpected code ${response.code}")

                    val responseBody = response.body?.string() ?: throw Exception("Response body is null")

                    val workflowResponse = json.decodeFromString<WorkflowResponse>(responseBody)
                    return workflowResponse.data.outputs.text
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return ""
            }
        }
        fun fetchWorkflowResponse(text: String): String {
            val apiUrl = "https://api.dify.ai/v1/workflows/run"
            val apiKey = "app-5su9JOZosymLmzdLOELhTuRR" // replace with your actual API key
            val inputs = mapOf("input_text" to text, "needCheck" to "False")

            val request = WorkflowRequest(
                inputs = inputs,
                response_mode = "blocking",
                user = "abc-123"
            )

            return try {
                runWorkflow(apiUrl, apiKey, request)
            } catch (e: Exception) {
                e.printStackTrace()
//                throw e
                ""
            }
        }
    }
}