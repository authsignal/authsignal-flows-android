package com.authsignal.flows.api

import com.authsignal.flows.api.models.flow.StartFlowRequest
import com.authsignal.flows.api.models.flow.StartFlowResponse
import com.authsignal.flows.api.models.flow.VerifyFlowRequest
import com.authsignal.flows.api.models.flow.VerifyFlowResponse

internal class FlowApiClient(tenantId: String, apiUrl: String) : BaseApiClient(tenantId, apiUrl) {
  suspend fun start(
    actionCode: String,
    clientId: String,
    codeChallenge: String,
  ): StartFlowResponse {
    val url = "$apiUrl/v1/client/flows/start"

    val body = StartFlowRequest(actionCode = actionCode, clientId = clientId, codeChallenge = codeChallenge)

    return postRequest(url = url, body = body)
  }

  suspend fun verify(challengeToken: String, codeVerifier: String): VerifyFlowResponse {
    val url = "$apiUrl/v1/client/flows/verify"

    val body = VerifyFlowRequest(codeVerifier = codeVerifier)

    return postRequest(url = url, challengeToken = challengeToken, body = body)
  }
}
