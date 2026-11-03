package com.authsignal.flows

import com.authsignal.flows.api.SmsApiClient
import com.authsignal.flows.api.models.VerifyResponse
import com.authsignal.flows.api.models.sms.SmsChallengeResponse

class AuthsignalSms internal constructor(
  tenantId: String,
  apiUrl: String,
  private val session: FlowSession,
) {
  private val api = SmsApiClient(tenantId, apiUrl)

  suspend fun challenge(phoneNumber: String? = null): SmsChallengeResponse {
    return session.withChallengeToken { challengeToken ->
      api.challenge(challengeToken = challengeToken, phoneNumber = phoneNumber)
    }
  }

  suspend fun verify(verificationCode: String): VerifyResponse {
    val challengeToken = TokenCache.requireChallengeToken()

    val verifyResponse = api.verify(challengeToken = challengeToken, verificationCode = verificationCode)

    TokenCache.challengeToken = verifyResponse.challengeToken

    return verifyResponse
  }
}
