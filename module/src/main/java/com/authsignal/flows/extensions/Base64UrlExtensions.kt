package com.authsignal.flows.extensions

import android.util.Base64

fun ByteArray.toBase64UrlString(): String {
  return Base64.encodeToString(this, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
}

fun String.fromBase64Url(): ByteArray {
  return Base64.decode(this, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
}
