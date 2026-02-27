package com.example.flightsearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flightsearch.ui.screen.FlightSearchScreen
import com.example.flightsearch.ui.theme.FlightSearchTheme
import com.example.flightsearch.viewmodel.FlightSearchViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            FlightSearchTheme {
                val viewModel: FlightSearchViewModel = viewModel(
                    factory = FlightSearchViewModel.Factory
                )
                FlightSearchScreen(viewModel = viewModel)
            }
        }
    }
}
