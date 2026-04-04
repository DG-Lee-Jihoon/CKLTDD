package com.example.flashcardapp.ui.theme

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Deck : Screen("deck/{deckId}") {
        fun createRoute(deckId: Int) = "deck/$deckId"
    }
    object AddCard : Screen("addCard/{deckId}") {
        fun createRoute(deckId: Int) = "addCard/$deckId"
    }
    object Study : Screen("study/{deckId}") {
        fun createRoute(deckId: Int) = "study/$deckId"
    }
    object Stats : Screen("stats/{deckId}") {
        fun createRoute(deckId: Int) = "stats/$deckId"
    }
}

enum class ReviewQuality(val value: Int, val label: String) {
    Again(0, "Again"),
    Hard(1, "Hard"),
    Good(3, "Good"),
    Easy(5, "Easy")
}