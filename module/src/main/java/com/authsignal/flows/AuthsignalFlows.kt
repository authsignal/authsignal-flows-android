package com.authsignal.flows

import android.content.Context

class AuthsignalFlows(
  context: Context,
  tenantId: String,
  apiUrl: String,
  clientId: String? = null,
) {
  val session: AuthsignalSession = AuthsignalSession(context.applicationContext, tenantId, apiUrl)

  private val flow = FlowContext(tenantId, apiUrl, clientId, session)

  val email: AuthsignalEmail = AuthsignalEmail(tenantId, apiUrl, flow)
  val sms: AuthsignalSms = AuthsignalSms(tenantId, apiUrl, flow)
  val whatsapp: AuthsignalWhatsapp = AuthsignalWhatsapp(tenantId, apiUrl, flow)
  val passkey: AuthsignalPasskey = AuthsignalPasskey(context.applicationContext, tenantId, apiUrl, flow)
  val inapp: AuthsignalInApp = AuthsignalInApp(context.applicationContext, tenantId, apiUrl, flow)
  val push: AuthsignalPush = AuthsignalPush(tenantId, apiUrl, flow)

  fun setChallengeToken(challengeToken: String) {
    TokenCache.challengeToken = challengeToken
    TokenCache.codeVerifier = null
    TokenCache.isCompleted = false
  }
}
