package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.DashboardScreen
import com.example.ui.WellbeingViewModel
import com.example.ui.theme.ScreenWellbeingTheme

class MainActivity : ComponentActivity() {

    private val viewModel: WellbeingViewModel by viewModels {
        val app = application as WellbeingApplication
        WellbeingViewModel.Factory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScreenWellbeingTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }
    }
}
