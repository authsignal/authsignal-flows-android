package com.authsignal.flows

import android.app.Activity
import com.authsignal.flows.api.PasskeyApiClient
import com.authsignal.flows.api.models.VerifyResponse
import com.authsignal.flows.security.PasskeyManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AuthsignalPasskey(tenantId: String, apiUrl: String) {
  private val api = PasskeyApiClient(tenantId, apiUrl)
  private val passkeyManager = PasskeyManager()

  suspend fun verify(
    activity: Activity,
    autofill: Boolean = false,
    preferImmediatelyAvailableCredentials: Boolean = true,
    syncCredentials: Boolean = false,
  ): VerifyResponse {
    val challengeToken = TokenCache.requireChallengeToken()

    val opts = api.authenticationOptions(challengeToken)

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
