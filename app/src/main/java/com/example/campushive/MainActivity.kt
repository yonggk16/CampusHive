package com.example.campushive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.campushive.navigation.CampusHiveApp
import com.example.campushive.ui.theme.CampusHiveTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampusHiveTheme {
                CampusHiveApp()
            }
        }
    }
}
