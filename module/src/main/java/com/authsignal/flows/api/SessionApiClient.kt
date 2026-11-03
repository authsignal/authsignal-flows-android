package com.authsignal.flows.api

import com.authsignal.flows.api.models.session.StartSessionRequest
import com.authsignal.flows.api.models.session.StartSessionResponse

internal class SessionApiClient(tenantId: String, apiUrl: String) : BaseApiClient(tenantId, apiUrl) {
  suspend fun start(
    actionCode: String,
    clientId: String,
    codeChallenge: String,
  ): StartSessionResponse {
    val url = "$apiUrl/v1/client/flows/start"

    val body = StartSessionRequest(actionCode = actionCode, clientId = clientId, codeChallenge = codeChallenge)

    return postRequest(url = url, body = body)
  }
}
