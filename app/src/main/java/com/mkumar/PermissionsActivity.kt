package com.mkumar


import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mkumar.permission.Permissions
import com.mkumar.ui.screens.PermissionsScreen
import com.mkumar.ui.theme.NikTheme

class PermissionsActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NikTheme {
                // Your composable content
                PermissionsScreen(
                    onAllPermissionsGranted = ::onAllPermissionsGranted
                )
            }
        }
    }

    private fun onAllPermissionsGranted() {
        if (Permissions.hasAllRequiredPermissions(this)) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
