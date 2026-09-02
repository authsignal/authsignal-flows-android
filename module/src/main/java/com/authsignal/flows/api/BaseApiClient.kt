package com.authsignal.flows.api

import com.authsignal.flows.AuthsignalFlowsError
import com.authsignal.flows.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

internal open class BaseApiClient(
  protected val tenantId: String,
  protected val apiUrl: String,
) {
  protected suspend inline fun <reified T> postRequest(
    url: String,
    challengeToken: String,
  ): T {
    val responseBody = executeRequest(url, challengeToken, requestBodyJson = null)

    return decode(responseBody)
  }

  protected suspend inline fun <reified T, reified B> postRequest(
    url: String,
    challengeToken: String,
    body: B,
  ): T {
    val requestBodyJson = json.encodeToString(body)

    val responseBody = executeRequest(url, challengeToken, requestBodyJson)

    return decode(responseBody)
  }

  protected suspend fun executeRequest(
    url: String,
    challengeToken: String,
    requestBodyJson: String?,
  ): String {
    val requestBuilder = Request.Builder()
      .url(url)
      .post((requestBodyJson ?: "{}").toRequestBody(jsonMediaType))
      .addHeader("Content-Type", "application/json")
      .addHeader("X-Authsignal-Challenge-Token", challengeToken)

    RequestMetadata.headers(tenantId).forEach { (name, value) ->
      requestBuilder.addHeader(name, value)
    }

    val request = requestBuilder.build()

    val response = try {
      withContext(Dispatchers.IO) {
        httpClient.newCall(request).execute()
      }
    } catch (e: IOException) {
      Logger.e("API request error: ${e.message}.")

      throw AuthsignalFlowsError.NetworkError(e.message ?: "A network error occurred.")
    }

    response.use {
      val bodyString = it.body?.string().orEmpty()

      if (it.code != 200) {
        val errorBody = try {
          json.decodeFromString<ApiErrorBody>(bodyString)
        } catch (e: SerializationException) {
          ApiErrorBody()
        }

        val errorCode = errorBody.errorCode ?: "unexpected_error"
        val errorDescription = errorBody.errorDescription ?: "Unexpected error."

        Logger.e("API response error code: $errorCode")
        Logger.e("API response error description: $errorDescription")

        if (it.code == 404 && errorCode == "unexpected_error") {
          throw AuthsignalFlowsError.ApiError(
            "API endpoint not found. Ensure your Authsignal base URL is valid."
          )
        }

        throw AuthsignalFlowsError.fromApi(errorCode, errorDescription)
      }

      return bodyString
    }
  }

  protected inline fun <reified T> decode(bodyString: String): T {
    return try {
      json.decodeFromString(bodyString)
    } catch (e: SerializationException) {
      Logger.e("API response decoding error for ${T::class.simpleName}: ${e.message}")

      throw AuthsignalFlowsError.DecodingError("Decoding error: ${e.message}")
    } catch (e: IllegalArgumentException) {
      Logger.e("API response decoding error for ${T::class.simpleName}: ${e.message}")

      throw AuthsignalFlowsError.DecodingError("Decoding error: ${e.message}")
    }
  }

  companion object {
    internal val json: Json = Json {
      ignoreUnknownKeys = true
      explicitNulls = false
    }

    internal val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    internal val httpClient: OkHttpClient by lazy { OkHttpClient() }
  }
}

@Serializable
internal data class ApiErrorBody(
  val errorCode: String? = null,
  val errorDescription: String? = null,
)
