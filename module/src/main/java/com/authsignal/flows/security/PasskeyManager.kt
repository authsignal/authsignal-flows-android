package com.authsignal.flows.security

import android.app.Activity
import android.os.Build
import android.os.CancellationSignal
import androidx.credentials.CreateCredentialResponse
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialManagerCallback
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.credentials.exceptions.domerrors.InvalidStateError
import androidx.credentials.exceptions.publickeycredential.CreatePublicKeyCredentialDomException
import com.authsignal.flows.AuthsignalFlowsError
import com.authsignal.flows.logger.Logger
import com.authsignal.flows.models.PasskeyAuthenticationCredential
import com.authsignal.flows.models.PasskeyRegistrationCredential
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val EXCLUDED_CREDENTIAL_ERROR_MESSAGE = "One of the excluded credentials exists on the local device"

internal class PasskeyManager {
  private val json = Json { ignoreUnknownKeys = true }

  private val directExecutor = Executor { command -> command.run() }

  @Volatile
  private var cancellationSignal: CancellationSignal? = null

  suspend fun auth(
    activity: Activity,
    requestJson: String,
    autofill: Boolean,
    preferImmediatelyAvailableCredentials: Boolean,
  ): PasskeyAuthenticationCredential {
    if (!isSupported()) {
      throw AuthsignalFlowsError.ApiError("Android version not supported.")
    }

    val credentialManager = CredentialManager.create(activity)

    val option = GetPublicKeyCredentialOption(requestJson = requestJson)

    val request = GetCredentialRequest(
      credentialOptions = listOf(option),
      preferImmediatelyAvailableCredentials = preferImmediatelyAvailableCredentials,
    )

    val response = try {
      getCredential(credentialManager, activity, request)
    } catch (e: GetCredentialCancellationException) {
      throw AuthsignalFlowsError.UserCanceled()
    } catch (e: NoCredentialException) {
      throw AuthsignalFlowsError.Unexpected(
        code = "no_credential",
        message = "No credential is available for the passkey authentication request.",
      )
    } catch (e: GetCredentialException) {
      Logger.e("Passkey authentication error: ${e.message}")

      throw AuthsignalFlowsError.ApiError(e.message ?: "Unexpected passkey authentication error.")
    }

    val credential = response.credential as? PublicKeyCredential
      ?: throw AuthsignalFlowsError.ApiError("Error unwrapping credential.")

    return try {
      json.decodeFromString<PasskeyAuthenticationCredential>(credential.authenticationResponseJson)
    } catch (e: SerializationException) {
      Logger.e("Passkey authentication decoding error: ${e.message}")

      throw AuthsignalFlowsError.DecodingError("Decoding error: ${e.message}")
    }
  }

  suspend fun register(
    activity: Activity,
    requestJson: String,
    preferImmediatelyAvailableCredentials: Boolean,
  ): PasskeyRegistrationCredential {
    if (!isSupported()) {
      throw AuthsignalFlowsError.ApiError("Android version not supported.")
    }

    val credentialManager = CredentialManager.create(activity)

    val request = CreatePublicKeyCredentialRequest(
      requestJson = requestJson,
      preferImmediatelyAvailableCredentials = preferImmediatelyAvailableCredentials,
    )

    val response = try {
      createCredential(credentialManager, activity, request)
    } catch (e: CreateCredentialCancellationException) {
      throw AuthsignalFlowsError.UserCanceled()
    } catch (e: CreatePublicKeyCredentialDomException) {
      val matchedExcludedCredential = e.domError is InvalidStateError &&
        (e.errorMessage?.contains(EXCLUDED_CREDENTIAL_ERROR_MESSAGE) == true)

      if (matchedExcludedCredential) {
        throw AuthsignalFlowsError.MatchedExcludedCredential()
      }

      Logger.e("Passkey registration error: ${e.errorMessage ?: e.message}")

      throw AuthsignalFlowsError.ApiError((e.errorMessage ?: e.message)?.toString() ?: "Passkey registration failed.")
    } catch (e: CreateCredentialException) {
      Logger.e("Passkey registration error: ${e.message}")

      throw AuthsignalFlowsError.ApiError(e.message ?: "Unexpected passkey registration error.")
    }

    val credential = response as? CreatePublicKeyCredentialResponse
      ?: throw AuthsignalFlowsError.ApiError("Error unwrapping credential.")

    return try {
      json.decodeFromString<PasskeyRegistrationCredential>(credential.registrationResponseJson)
    } catch (e: SerializationException) {
      Logger.e("Passkey registration decoding error: ${e.message}")

      throw AuthsignalFlowsError.DecodingError("Decoding error: ${e.message}")
    }
  }

  fun cancel() {
    cancellationSignal?.cancel()
  }

  fun isSupported(): Boolean {
    return Build.VERSION.SDK_INT >= 28
  }

  private suspend fun getCredential(
    credentialManager: CredentialManager,
    activity: Activity,
    request: GetCredentialRequest,
  ): GetCredentialResponse = suspendCancellableCoroutine { continuation ->
    val signal = CancellationSignal()
    cancellationSignal = signal

    continuation.invokeOnCancellation { signal.cancel() }

    credentialManager.getCredentialAsync(
      activity,
      request,
      signal,
      directExecutor,
      object : CredentialManagerCallback<GetCredentialResponse, GetCredentialException> {
        override fun onResult(result: GetCredentialResponse) {
          cancellationSignal = null

          if (continuation.isActive) continuation.resume(result)
        }

        override fun onError(e: GetCredentialException) {
          cancellationSignal = null

          if (continuation.isActive) continuation.resumeWithException(e)
        }
      },
    )
  }

  private suspend fun createCredential(
    credentialManager: CredentialManager,
    activity: Activity,
    request: CreatePublicKeyCredentialRequest,
  ): CreateCredentialResponse = suspendCancellableCoroutine { continuation ->
    val signal = CancellationSignal()
    cancellationSignal = signal

    continuation.invokeOnCancellation { signal.cancel() }

    credentialManager.createCredentialAsync(
      activity,
      request,
      signal,
      directExecutor,
      object : CredentialManagerCallback<CreateCredentialResponse, CreateCredentialException> {
        override fun onResult(result: CreateCredentialResponse) {
          cancellationSignal = null

          if (continuation.isActive) continuation.resume(result)
        }

        override fun onError(e: CreateCredentialException) {
          cancellationSignal = null

          if (continuation.isActive) continuation.resumeWithException(e)
        }
      },
    )
  }
}
