package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.game.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    
    // Simple Constructor ViewModel injection
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()

                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        "menu" -> MainMenuScreen(viewModel) { viewModel.startGame() }
                        "settings" -> SettingsScreen(viewModel)
                        "garage" -> GarageScreen(viewModel)
                        "leaderboard" -> LeaderboardScreen(viewModel)
                        "challenges" -> ChallengesScreen(viewModel)
                        "shop" -> ShopScreen(viewModel)
                        "play" -> GamePlayScreen(viewModel)
                        else -> MainMenuScreen(viewModel) { viewModel.startGame() }
                    }
                }
            }
        }
    }
}
