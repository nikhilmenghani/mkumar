package com.mkumar

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mkumar.permission.Permissions
import com.mkumar.ui.navigation.ScreenNavigator
import com.mkumar.ui.theme.MKumarTheme
import com.mkumar.ui.theme.NikTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.RELEASE.toInt()  <= 13 || Permissions.hasAllRequiredPermissions(this)) {
            setContent {
                NikTheme {
                    // Your composable content
                    ScreenNavigator()
                }
            }
        } else {
            // Launch PermissionsActivity if any permissions are missing
            startActivity(Intent(this, PermissionsActivity::class.java))
            finish()
        }
    }

    fun restartActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MKumarTheme {
        Greeting("Android")
    }
}