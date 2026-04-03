package com.example.flashcardapp.ui.theme

// Navigation destinations
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

// Mức độ trả lời khi ôn thẻ (dùng cho SM-2)
enum class ReviewQuality(val value: Int, val label: String) {
    Again(0, "Again"),   // Không nhớ, ôn lại ngay
    Hard(1, "Hard"),     // Nhớ nhưng khó
    Good(3, "Good"),     // Nhớ bình thường
    Easy(5, "Easy")      // Nhớ rất dễ
}