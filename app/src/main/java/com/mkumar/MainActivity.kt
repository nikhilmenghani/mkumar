package com.mkumar

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mkumar.data.PreferencesManager
import com.mkumar.permission.Permissions
import com.mkumar.ui.navigation.ScreenNavigator
import com.mkumar.ui.theme.LocalPreferencesManager
import com.mkumar.ui.theme.MKumarTheme
import com.mkumar.ui.theme.NikTheme
import com.mkumar.viewmodel.CustomerViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private val customerViewModel: CustomerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.RELEASE.toInt() <= 13 || Permissions.hasAllRequiredPermissions(this)) {
            setContent {
                CompositionLocalProvider(
                    LocalPreferencesManager provides preferencesManager
                ) {
                    NikTheme {
                        ScreenNavigator(customerViewModel)
                    }
                }
            }
        } else {
            startActivity(Intent(this, PermissionsActivity::class.java))
            finish()
        }
    }

    fun restartActivity() {
        startActivity(Intent(this, MainActivity::class.java))
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