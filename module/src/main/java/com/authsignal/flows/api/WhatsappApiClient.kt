package com.authsignal.flows.api

import com.authsignal.flows.api.models.VerifyResponse
import com.authsignal.flows.api.models.whatsapp.WhatsappChallengeRequest
import com.authsignal.flows.api.models.whatsapp.WhatsappChallengeResponse
import com.authsignal.flows.api.models.whatsapp.WhatsappVerifyRequest

internal class WhatsappApiClient(tenantId: String, apiUrl: String) : BaseApiClient(tenantId, apiUrl) {
  suspend fun challenge(
    challengeToken: String,
    phoneNumber: String?,
  ): WhatsappChallengeResponse {
    val url = "$apiUrl/v1/client/flows/challenge/whatsapp"

    val body = WhatsappChallengeRequest(phoneNumber = phoneNumber)

    return postRequest(url = url, challengeToken = challengeToken, body = body)
  }

  suspend fun verify(
    challengeToken: String,
    verificationCode: String,
  ): VerifyResponse {
    val url = "$apiUrl/v1/client/flows/verify/whatsapp"

    val body = WhatsappVerifyRequest(verificationCode = verificationCode)

    return postRequest(url = url, challengeToken = challengeToken, body = body)
  }
}
