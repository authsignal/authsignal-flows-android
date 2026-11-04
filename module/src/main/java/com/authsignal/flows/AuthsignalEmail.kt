package com.authsignal.flows

import com.authsignal.flows.api.EmailApiClient
import com.authsignal.flows.api.models.VerifyResponse
import com.authsignal.flows.api.models.email.EmailChallengeResponse

class AuthsignalEmail internal constructor(
  tenantId: String,
  apiUrl: String,
  private val flow: FlowContext,
) {
  private val api = EmailApiClient(tenantId, apiUrl)

  suspend fun challenge(email: String? = null): EmailChallengeResponse {
    return flow.withChallengeToken { challengeToken ->
      api.challenge(challengeToken = challengeToken, email = email)
    }
  }

  suspend fun verify(verificationCode: String): VerifyResponse {
    val challengeToken = TokenCache.requireChallengeToken()

    val verifyResponse = api.verify(challengeToken = challengeToken, verificationCode = verificationCode)

    flow.update(verifyResponse)

    return verifyResponse
  }
}
