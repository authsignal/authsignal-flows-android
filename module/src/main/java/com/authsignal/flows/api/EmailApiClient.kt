package com.authsignal.flows.api

import com.authsignal.flows.api.models.VerifyResponse
import com.authsignal.flows.api.models.email.EmailChallengeRequest
import com.authsignal.flows.api.models.email.EmailChallengeResponse
import com.authsignal.flows.api.models.email.EmailVerifyRequest

internal class EmailApiClient(tenantId: String, apiUrl: String) : BaseApiClient(tenantId, apiUrl) {
  suspend fun challenge(
    challengeToken: String,
    email: String?,
  ): EmailChallengeResponse {
    val url = "$apiUrl/v1/client/flows/challenge/email-otp"

    val body = EmailChallengeRequest(email = email)

    return postRequest(url = url, challengeToken = challengeToken, body = body)
  }

  suspend fun verify(
    challengeToken: String,
    verificationCode: String,
  ): VerifyResponse {
    val url = "$apiUrl/v1/client/flows/verify/email-otp"

    val body = EmailVerifyRequest(verificationCode = verificationCode)

    return postRequest(url = url, challengeToken = challengeToken, body = body)
  }
}
