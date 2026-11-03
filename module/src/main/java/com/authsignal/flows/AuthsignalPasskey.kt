package com.authsignal.flows

import android.app.Activity
import com.authsignal.flows.api.PasskeyApiClient
import com.authsignal.flows.api.models.VerifyResponse
import com.authsignal.flows.security.PasskeyManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AuthsignalPasskey internal constructor(
  tenantId: String,
  apiUrl: String,
  private val session: FlowSession,
) {
  private val api = PasskeyApiClient(tenantId, apiUrl)
  private val passkeyManager = PasskeyManager()

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

    TokenCache.challengeToken = registerResponse.challengeToken

    return registerResponse
  }

  suspend fun verify(
    activity: Activity,
    autofill: Boolean = false,
    preferImmediatelyAvailableCredentials: Boolean = true,
    syncCredentials: Boolean = false,
  ): VerifyResponse {
    val opts = session.withChallengeToken { challengeToken -> api.authenticationOptions(challengeToken) }

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

    TokenCache.challengeToken = verifyResponse.challengeToken

    return verifyResponse
  }

  fun cancel() {
    passkeyManager.cancel()
  }

  fun isSupported(): Boolean {
    return passkeyManager.isSupported()
  }
}
