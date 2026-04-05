package com.example.flashcardapp

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.flashcardapp.ui.theme.AddCardScreen
import com.example.flashcardapp.ui.theme.DeckScreen
import com.example.flashcardapp.ui.theme.HomeScreen
import com.example.flashcardapp.ui.theme.StatsScreen
import com.example.flashcardapp.ui.theme.StudyScreen
import com.example.flashcardapp.viewmodel.CardViewModel
import com.example.flashcardapp.ui.theme.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: CardViewModel
) {
    val decks by viewModel.allDecks.collectAsState()

    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onDeckClick = { deckId -> navController.navigate("deck/$deckId") },
                onSettingsClick = { navController.navigate("settings") }
            )
        }

        composable("deck/{deckId}") { backStack ->
            val deckId = backStack.arguments?.getString("deckId")?.toLongOrNull() ?: return@composable
            val deckName = decks.find { it.id == deckId }?.name ?: "Bộ thẻ"

            DeckScreen(
                deckId       = deckId,
                deckName     = deckName,
                viewModel    = viewModel,
                onStudyClick = { navController.navigate("study/$deckId") },
                onAddCard    = { navController.navigate("addcard/$deckId") },
                onStatsClick = { navController.navigate("stats/$deckId") },
                onBack       = { navController.popBackStack() }
            )
        }

        composable("study/{deckId}") { backStack ->
            val deckId = backStack.arguments?.getString("deckId")?.toLongOrNull() ?: return@composable
            val deckName = decks.find { it.id == deckId }?.name ?: "Ôn tập"

            StudyScreen(
                deckId    = deckId,
                deckName  = deckName,
                viewModel = viewModel,
                onBack    = { navController.popBackStack() }
            )
        }

        composable("addcard/{deckId}") { backStack ->
            val deckId = backStack.arguments?.getString("deckId")?.toLongOrNull() ?: return@composable

            AddCardScreen(
                deckId    = deckId,
                viewModel = viewModel,
                onBack    = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("stats/{deckId}") { backStack ->
            val deckId = backStack.arguments?.getString("deckId")?.toLongOrNull() ?: return@composable
            val deckName = decks.find { it.id == deckId }?.name ?: "Thống kê"

            StatsScreen(
                deckId    = deckId,
                deckName  = deckName,
                viewModel = viewModel,
                onBack    = { navController.popBackStack() }
            )
        }
    }
}