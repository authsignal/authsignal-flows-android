package com.authsignal.flows.api.models.push

import com.authsignal.flows.api.models.FlowAction
import com.authsignal.flows.api.models.FlowUser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PushVerifyResponse(
  val status: PushChallengeStatus,
  val action: FlowAction? = null,
  val challengeToken: String? = null,
  val user: FlowUser? = null,
)

@Serializable
enum class PushChallengeStatus {
  @SerialName("PENDING")
  PENDING,

  @SerialName("APPROVED")
  APPROVED,

  @SerialName("REJECTED")
  REJECTED,
}
