package com.example.campushive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.campushive.navigation.CampusHiveApp
import com.example.campushive.ui.theme.CampusHiveTheme
import com.example.campushive.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        configureImmersiveMode()

        setContent {
            CampusHiveTheme {
                CampusHiveApp()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            configureImmersiveMode()
        }
    }

    override fun onResume() {
        super.onResume()
        configureImmersiveMode()
        // Mark student online when app is active
        authViewModel.updateUserPresence(isOnline = true)
    }

    override fun onPause() {
        super.onPause()
        // Mark student offline when app is backgrounded or closed
        authViewModel.updateUserPresence(isOnline = false)
    }

    /**
     * Hides system gesture navigation bar and enables swipe-to-reveal behavior
     */
    private fun configureImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
