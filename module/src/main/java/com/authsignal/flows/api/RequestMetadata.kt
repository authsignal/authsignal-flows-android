package com.authsignal.flows.api

import android.os.Build

internal object RequestMetadata {
  private const val nativeSdk = "android"
  private const val nativeVersion = "1.0.0-alpha1"
  private const val nativeUserAgentToken = "AuthsignalFlowsAndroidSDK"

  private data class WrapperSdkMetadata(
    val sdk: String,
    val version: String,
    val userAgentToken: String,
  )

  @Volatile
  private var wrapperSdkMetadata: WrapperSdkMetadata? = null

  /**
   * Called by wrapper SDKs (e.g. React Native, Flutter) to identify
   * themselves in the `X-Authsignal-Flows-SDK` / `User-Agent` headers.
   */
  fun setWrapperSDK(sdk: String, version: String, userAgentToken: String) {
    wrapperSdkMetadata = WrapperSdkMetadata(sdk, version, userAgentToken)
  }

  fun clearWrapperSDK() {
    wrapperSdkMetadata = null
  }

  fun headers(tenantId: String): Map<String, String> {
    val metadata = wrapperSdkMetadata

    val headers = mutableMapOf(
      "User-Agent" to userAgent(),
      "X-Authsignal-Flows-SDK" to (metadata?.sdk ?: nativeSdk),
      "X-Authsignal-Version" to (metadata?.version ?: nativeVersion),
      "X-Authsignal-Tenant-ID" to tenantId,
    )

    if (metadata != null) {
      headers["X-Authsignal-Native-SDK"] = nativeSdk
      headers["X-Authsignal-Native-Version"] = nativeVersion
    }

    return headers
  }

  private fun userAgent(): String {
    return "${baseUserAgent()} ${userAgentProductTokens()}"
  }

  private fun userAgentProductTokens(): String {
    val nativeProduct = "$nativeUserAgentToken/$nativeVersion"
    val metadata = wrapperSdkMetadata

    return if (metadata != null) {
      "${metadata.userAgentToken}/${metadata.version} $nativeProduct"
    } else {
      nativeProduct
    }
  }

  private fun baseUserAgent(): String {
    val release = Build.VERSION.RELEASE ?: "unknown"
    val model = Build.MODEL ?: "unknown"
    val manufacturer = Build.MANUFACTURER ?: "unknown"

    return "Mozilla/5.0 (Linux; Android $release; $manufacturer $model)"
  }
}
