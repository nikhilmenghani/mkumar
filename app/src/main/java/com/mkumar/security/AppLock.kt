package com.mkumar.security

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.hardware.biometrics.BiometricPrompt
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.os.CancellationSignal
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private var appLockSessionAuthenticated = false

fun markAppLockSessionAuthenticated() {
    appLockSessionAuthenticated = true
}

fun canUseAppLock(context: Context): Boolean =
    context.getSystemService(KeyguardManager::class.java)?.isDeviceSecure == true

@Composable
fun AppLock(enabled: Boolean, activity: Activity, content: @Composable () -> Unit) {
    var unlocked by remember(enabled) {
        mutableStateOf(!enabled || appLockSessionAuthenticated)
    }
    var request by remember { mutableStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }
    if (unlocked) { content(); return }
    LaunchedEffect(request) {
        authenticateForAppLock(activity, {
            markAppLockSessionAuthenticated()
            unlocked = true
        }, { error = it })
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.Fingerprint, contentDescription = null)
        Text("Unlock MKumar", style = MaterialTheme.typography.headlineSmall)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = { error = null; request++ }) { Text("Authenticate") }
    }
}

fun authenticateForAppLock(
    activity: Activity,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    title: String = "Unlock MKumar",
    subtitle: String = "Authenticate to continue"
) {
    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) = onSuccess()
        override fun onAuthenticationError(code: Int, message: CharSequence?) =
            onError(message?.toString().orEmpty().ifBlank { "Authentication cancelled" })
    }
    runCatching {
        BiometricPrompt.Builder(activity)
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()
            .authenticate(CancellationSignal(), activity.mainExecutor, callback)
    }.onFailure { failure ->
        onError(failure.message ?: "Authentication is unavailable")
    }
}
