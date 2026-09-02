package com.authsignal.flows

import com.authsignal.flows.api.EmailApiClient
import com.authsignal.flows.api.models.VerifyResponse
import com.authsignal.flows.api.models.email.EmailChallengeResponse

class AuthsignalEmail(tenantId: String, apiUrl: String) {
  private val api = EmailApiClient(tenantId, apiUrl)

  suspend fun challenge(email: String? = null): EmailChallengeResponse {
    val challengeToken = TokenCache.requireChallengeToken()

    return api.challenge(challengeToken = challengeToken, email = email)
  }

  suspend fun verify(verificationCode: String): VerifyResponse {
    val challengeToken = TokenCache.requireChallengeToken()

    val verifyResponse = api.verify(challengeToken = challengeToken, verificationCode = verificationCode)

    TokenCache.challengeToken = verifyResponse.challengeToken

    return verifyResponse
  }
}
