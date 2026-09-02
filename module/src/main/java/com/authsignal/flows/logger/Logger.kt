package com.authsignal.flows.logger

import android.util.Log

internal object Logger {
  private const val TAG = "Authsignal"

  fun d(message: String) {
    Log.d(TAG, "[Authsignal] $message")
  }

  fun i(message: String) {
    Log.i(TAG, "[Authsignal] $message")
  }

  fun e(message: String) {
    Log.e(TAG, "[Authsignal] $message")
  }
}
