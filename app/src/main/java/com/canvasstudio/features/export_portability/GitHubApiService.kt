package com.canvasstudio.features.export_portability

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.io.File
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class GitHubUploadResult(
    val fileName: String,
    val success: Boolean,
    val publicUrl: String,
    val htmlUrl: String? = null,
    val errorMessage: String? = null
)

class GitHubApiService {

    suspend fun uploadFile(
        owner: String = "SollonSoares",
        repo: String = "galeria",
        path: String = "imagens",
        branch: String = "main",
        token: String,
        file: File,
        customFileName: String? = null
    ): GitHubUploadResult = withContext(Dispatchers.IO) {
        val fileName = customFileName ?: file.name
        val cleanPath = path.trim('/').ifEmpty { "imagens" }
        val apiUrl = "https://api.github.com/repos/$owner/$repo/contents/$cleanPath/$fileName"
        val publicUrl = "https://$owner.github.io/$repo/$cleanPath/$fileName"

        if (token.isBlank()) {
            return@withContext GitHubUploadResult(
                fileName = fileName,
                success = false,
                publicUrl = publicUrl,
                errorMessage = "Token do GitHub não configurado nas Configurações."
            )
        }

        try {
            // 1. Checar se o arquivo já existe para obter o SHA (necessário para update na API do GitHub)
            val existingSha = getExistingFileSha(apiUrl, token, branch)

            // 2. Codificar bytes do arquivo em Base64
            val fileBytes = file.readBytes()
            val base64Content = Base64.encodeToString(fileBytes, Base64.NO_WRAP)

            // 3. Montar payload JSON
            val payload = buildJsonObject {
                put("message", "feat(galeria): upload $fileName via Canvas Studio")
                put("content", base64Content)
                put("branch", branch)
                if (existingSha != null) {
                    put("sha", existingSha)
                }
            }.toString()

            // 4. Executar PUT na API do GitHub
            val connection = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                doOutput = true
                connectTimeout = 15000
                readTimeout = 20000
            }

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(payload)
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                val responseJson = try { Json.parseToJsonElement(responseBody).jsonObject } catch (e: Exception) { null }
                val htmlUrl = responseJson?.get("content")?.jsonObject?.get("html_url")?.jsonPrimitive?.contentOrNull
                
                Log.d("GitHubApiService", "Upload successful for $fileName: $publicUrl")
                GitHubUploadResult(
                    fileName = fileName,
                    success = true,
                    publicUrl = publicUrl,
                    htmlUrl = htmlUrl
                )
            } else {
                val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $responseCode"
                Log.e("GitHubApiService", "GitHub API error ($responseCode): $errorBody")
                val parsedError = try {
                    Json.parseToJsonElement(errorBody).jsonObject["message"]?.jsonPrimitive?.content ?: errorBody
                } catch (e: Exception) {
                    errorBody
                }
                GitHubUploadResult(
                    fileName = fileName,
                    success = false,
                    publicUrl = publicUrl,
                    errorMessage = "GitHub ($responseCode): $parsedError"
                )
            }
        } catch (e: Exception) {
            Log.e("GitHubApiService", "Exception during upload: ${e.message}", e)
            GitHubUploadResult(
                fileName = fileName,
                success = false,
                publicUrl = publicUrl,
                errorMessage = e.message ?: "Erro de rede ao conectar com o GitHub."
            )
        }
    }

    private fun getExistingFileSha(apiUrl: String, token: String, branch: String): String? {
        return try {
            val urlWithBranch = "$apiUrl?ref=$branch"
            val connection = (URL(urlWithBranch).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
                connectTimeout = 8000
                readTimeout = 8000
            }
            if (connection.responseCode == 200) {
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                val json = Json.parseToJsonElement(body).jsonObject
                json["sha"]?.jsonPrimitive?.contentOrNull
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
