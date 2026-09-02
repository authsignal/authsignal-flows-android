<img width="1070" alt="Authsignal" src="https://raw.githubusercontent.com/authsignal/authsignal-ios/main/.github/images/authsignal.png">

# Authsignal Flows Android

## Installation

Add authsignal-flows-android to your module's `build.gradle`/`build.gradle.kts` dependencies:

```kotlin
dependencies {
    implementation("com.authsignal:authsignal-flows-android:1.0.0-alpha1")
}
```

## Initialization

```kotlin
import com.authsignal.flows.AuthsignalFlows

val authsignal = AuthsignalFlows(tenantId = "YOUR_TENANT_ID", apiUrl = "YOUR_REGION_API_URL")
```

You can find your tenant ID in the [Authsignal Portal](https://portal.authsignal.com/organisations/tenants/api).

You must specify the correct base URL for your tenant's region.

| Region        | Base URL                      |
| ------------- | ----------------------------- |
| US (Oregon)   | https://api.authsignal.com    |
| CA (Montreal) | https://ca.api.authsignal.com |
| AU (Sydney)   | https://au.api.authsignal.com |
| EU (Dublin)   | https://eu.api.authsignal.com |
| UK (London)   | https://uk.api.authsignal.com |
