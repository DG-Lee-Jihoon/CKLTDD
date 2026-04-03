package com.example.flashcardapp

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.flashcardapp.ui.theme.*
import com.example.flashcardapp.viewmodel.CardViewModel

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val viewModel: CardViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onDeckClick = { deckId ->
                    navController.navigate(Screen.Deck.createRoute(deckId))
                }
            )
        }

        composable(
            route = Screen.Deck.route,
            arguments = listOf(navArgument("deckId") { type = NavType.IntType })
        ) { back ->
            val deckId = back.arguments?.getInt("deckId") ?: return@composable
            DeckScreen(
                deckId = deckId,
                viewModel = viewModel,
                onStudyClick = { navController.navigate(Screen.Study.createRoute(deckId)) },
                onAddCard = { navController.navigate(Screen.AddCard.createRoute(deckId)) },
                onStatsClick = { navController.navigate(Screen.Stats.createRoute(deckId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AddCard.route,
            arguments = listOf(navArgument("deckId") { type = NavType.IntType })
        ) { back ->
            val deckId = back.arguments?.getInt("deckId") ?: return@composable
            AddCardScreen(
                deckId = deckId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Study.route,
            arguments = listOf(navArgument("deckId") { type = NavType.IntType })
        ) { back ->
            val deckId = back.arguments?.getInt("deckId") ?: return@composable
            StudyScreen(
                deckId = deckId,
                viewModel = viewModel,
                onFinish = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Stats.route,
            arguments = listOf(navArgument("deckId") { type = NavType.IntType })
        ) { back ->
            val deckId = back.arguments?.getInt("deckId") ?: return@composable
            StatsScreen(
                deckId = deckId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}