package com.authsignal.flows

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthsignalFlowsErrorTest {
  @Test
  fun `fromApi maps invalid_code to InvalidCode`() {
    val error = AuthsignalFlowsError.fromApi("invalid_code", "The code you entered is incorrect.")

    assertTrue(error is AuthsignalFlowsError.InvalidCode)
    assertEquals("invalid_code", error.code)
    assertEquals("The code you entered is incorrect.", error.message)
  }

  @Test
  fun `fromApi maps unknown codes to Unexpected, preserving the code and message`() {
    val error = AuthsignalFlowsError.fromApi("some_new_error_code", "Something else went wrong.")

    assertTrue(error is AuthsignalFlowsError.Unexpected)
    assertEquals("some_new_error_code", error.code)
    assertEquals("Something else went wrong.", error.message)
  }

  @Test
  fun `flow not initialized has a stable code`() {
    val error = AuthsignalFlowsError.FlowNotInitialized()

    assertEquals("flow_not_initialized", error.code)
  }
}
