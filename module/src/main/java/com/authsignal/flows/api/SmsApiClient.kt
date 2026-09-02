package com.authsignal.flows.api

import com.authsignal.flows.api.models.VerifyResponse
import com.authsignal.flows.api.models.sms.SmsChallengeRequest
import com.authsignal.flows.api.models.sms.SmsChallengeResponse
import com.authsignal.flows.api.models.sms.SmsVerifyRequest

internal class SmsApiClient(tenantId: String, apiUrl: String) : BaseApiClient(tenantId, apiUrl) {
  suspend fun challenge(
    challengeToken: String,
    phoneNumber: String?,
  ): SmsChallengeResponse {
    val url = "$apiUrl/v1/client/flows/challenge/sms"

    val body = SmsChallengeRequest(phoneNumber = phoneNumber)

    return postRequest(url = url, challengeToken = challengeToken, body = body)
  }

  suspend fun verify(
    challengeToken: String,
    verificationCode: String,
  ): VerifyResponse {
    val url = "$apiUrl/v1/client/flows/verify/sms"

    val body = SmsVerifyRequest(verificationCode = verificationCode)

    return postRequest(url = url, challengeToken = challengeToken, body = body)
  }
}
