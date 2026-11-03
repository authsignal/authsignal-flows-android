package com.authsignal.flows

import com.authsignal.flows.api.EmailApiClient
import com.authsignal.flows.api.models.VerifyResponse
import com.authsignal.flows.api.models.email.EmailChallengeResponse

class AuthsignalEmail internal constructor(
  tenantId: String,
  apiUrl: String,
  private val session: FlowSession,
) {
  private val api = EmailApiClient(tenantId, apiUrl)

  suspend fun challenge(email: String? = null): EmailChallengeResponse {
    return session.withChallengeToken { challengeToken ->
      api.challenge(challengeToken = challengeToken, email = email)
    }
  }

  suspend fun verify(verificationCode: String): VerifyResponse {
    val challengeToken = TokenCache.requireChallengeToken()

    val verifyResponse = api.verify(challengeToken = challengeToken, verificationCode = verificationCode)

    TokenCache.challengeToken = verifyResponse.challengeToken

    return verifyResponse
  }
}
