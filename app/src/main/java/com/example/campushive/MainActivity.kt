package com.example.campushive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.campushive.navigation.CampusHiveApp
import com.example.campushive.ui.theme.CampusHiveTheme
import com.example.campushive.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampusHiveTheme {
                CampusHiveApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Mark student online when app is active
        authViewModel.updateUserPresence(isOnline = true)
    }

    override fun onPause() {
        super.onPause()
        // Mark student offline when app is backgrounded or closed
        authViewModel.updateUserPresence(isOnline = false)
    }
}