package com.authsignal.flows

import android.app.Activity
import android.content.Context
import com.authsignal.flows.api.PasskeyApiClient
import com.authsignal.flows.api.models.VerifyResponse
import com.authsignal.flows.security.PasskeyManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AuthsignalPasskey internal constructor(
  context: Context,
  tenantId: String,
  apiUrl: String,
  private val flow: FlowContext,
) {
  private val api = PasskeyApiClient(tenantId, apiUrl)
  private val passkeyManager = PasskeyManager()
  private val prefs = context.getSharedPreferences("authsignal_passkey", Context.MODE_PRIVATE)

  suspend fun register(
    activity: Activity,
    username: String? = null,
    displayName: String? = null,
    preferImmediatelyAvailableCredentials: Boolean = true,
  ): VerifyResponse {
    val challengeToken = TokenCache.requireChallengeToken()

    val opts = api.registrationOptions(challengeToken, username, displayName)

    val options = opts.registrationOptions.copy(
      authenticatorSelection = opts.registrationOptions.authenticatorSelection.copy(
        userVerification = "required",
      ),
      pubKeyCredParams = opts.registrationOptions.pubKeyCredParams.filter { it.alg != -8 },
    )

    val requestJson = Json.encodeToString(options)

    val credential = passkeyManager.register(
      activity = activity,
      requestJson = requestJson,
      preferImmediatelyAvailableCredentials = preferImmediatelyAvailableCredentials,
    )

    val registerResponse = api.register(challengeToken = challengeToken, credential = credential)

    storeCredentialId(credential.rawId, options.user.name)

    flow.update(registerResponse)

    return registerResponse
  }

  suspend fun verify(
    activity: Activity,
    autofill: Boolean = false,
    preferImmediatelyAvailableCredentials: Boolean = true,
    syncCredentials: Boolean = false,
  ): VerifyResponse {
    val opts = flow.withChallengeToken { challengeToken -> api.authenticationOptions(challengeToken) }

    val challengeToken = TokenCache.requireChallengeToken()

    val options = opts.authenticationOptions.copy(userVerification = "required")

    val requestJson = Json.encodeToString(options)

    val credential = passkeyManager.auth(
      activity = activity,
      requestJson = requestJson,
      autofill = autofill,
      preferImmediatelyAvailableCredentials = preferImmediatelyAvailableCredentials,
    )

    val verifyResponse = api.verify(challengeToken = challengeToken, credential = credential)

    storeCredentialId(credential.rawId, verifyResponse.user.username)

    flow.update(verifyResponse)

    return verifyResponse
  }

  fun cancel() {
    passkeyManager.cancel()
  }

  suspend fun shouldPromptToCreatePasskey(username: String? = null): Boolean {
    val credentialId = prefs.getString(getCredentialIdKey(username), null) ?: return true

    return try {
      api.getPasskeyAuthenticator(credentialId)

      false
    } catch (e: AuthsignalFlowsError) {
      if (e.code != "invalid_credential") {
        throw e
      }

      true
    }
  }

  fun isSupported(): Boolean {
    return passkeyManager.isSupported()
  }

  private fun storeCredentialId(credentialId: String, username: String?) {
    prefs.edit().apply {
      putString(getCredentialIdKey(null), credentialId)

      username?.let { putString(getCredentialIdKey(it), credentialId) }

      apply()
    }
  }

  private fun getCredentialIdKey(username: String?): String {
    return username?.let { "${CREDENTIAL_ID_KEY}_$it" } ?: CREDENTIAL_ID_KEY
  }

  private companion object {
    const val CREDENTIAL_ID_KEY = "@as_passkey_credential_id"
  }
}
