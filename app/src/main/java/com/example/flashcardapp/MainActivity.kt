package com.example.flashcardapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.example.flashcardapp.ui.theme.theme.FlashcardAppTheme
import com.example.flashcardapp.viewmodel.CardViewModel
import com.example.flashcardapp.viewmodel.CardViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: CardViewModel by viewModels {
        CardViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlashcardAppTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }
}