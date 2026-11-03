package com.authsignal.flows

import com.authsignal.flows.api.WhatsappApiClient
import com.authsignal.flows.api.models.VerifyResponse
import com.authsignal.flows.api.models.whatsapp.WhatsappChallengeResponse

class AuthsignalWhatsapp(tenantId: String, apiUrl: String) {
  private val api = WhatsappApiClient(tenantId, apiUrl)

  suspend fun challenge(phoneNumber: String? = null): WhatsappChallengeResponse {
    val challengeToken = TokenCache.requireChallengeToken()

    return api.challenge(challengeToken = challengeToken, phoneNumber = phoneNumber)
  }

  suspend fun verify(verificationCode: String): VerifyResponse {
    val challengeToken = TokenCache.requireChallengeToken()

    val verifyResponse = api.verify(challengeToken = challengeToken, verificationCode = verificationCode)

    TokenCache.update(verifyResponse)

    return verifyResponse
  }
}
