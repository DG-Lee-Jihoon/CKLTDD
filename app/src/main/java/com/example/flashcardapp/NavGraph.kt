package com.example.flashcardapp

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.flashcardapp.ui.theme.*
import com.example.flashcardapp.util.FirebaseSync
import com.example.flashcardapp.viewmodel.CardViewModel

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val viewModel: CardViewModel = viewModel()

    // Nếu chưa đăng nhập → vào LoginScreen trước
    val startDest = if (FirebaseSync.isLoggedIn()) Screen.Home.route else "login"

    NavHost(navController = navController, startDestination = startDest) {

        composable("login") {
            LoginScreen(onLoginSuccess = {
                navController.navigate(Screen.Home.route) {
                    popUpTo("login") { inclusive = true }
                }
            })
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onDeckClick = { navController.navigate(Screen.Deck.createRoute(it)) }
            )
        }

        composable(
            Screen.Deck.route,
            arguments = listOf(navArgument("deckId") { type = NavType.IntType })
        ) { back ->
            val deckId = back.arguments?.getInt("deckId") ?: return@composable
            DeckScreen(
                deckId = deckId, viewModel = viewModel,
                onStudyClick = { navController.navigate(Screen.Study.createRoute(deckId)) },
                onAddCard = { navController.navigate(Screen.AddCard.createRoute(deckId)) },
                onStatsClick = { navController.navigate(Screen.Stats.createRoute(deckId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.AddCard.route,
            arguments = listOf(navArgument("deckId") { type = NavType.IntType })
        ) { back ->
            val deckId = back.arguments?.getInt("deckId") ?: return@composable
            AddCardScreen(deckId = deckId, viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable(
            Screen.Study.route,
            arguments = listOf(navArgument("deckId") { type = NavType.IntType })
        ) { back ->
            val deckId = back.arguments?.getInt("deckId") ?: return@composable
            StudyScreen(deckId = deckId, viewModel = viewModel, onFinish = { navController.popBackStack() })
        }

        composable(
            Screen.Stats.route,
            arguments = listOf(navArgument("deckId") { type = NavType.IntType })
        ) { back ->
            val deckId = back.arguments?.getInt("deckId") ?: return@composable
            StatsScreen(deckId = deckId, viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}
