package com.example.flashcardapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.data.*
import com.example.flashcardapp.ui.theme.ReviewQuality
import com.example.flashcardapp.util.Sm2Algorithm
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = CardRepository(db.cardDao())

    // Danh sách tất cả bộ thẻ
    val allDecks: StateFlow<List<Deck>> = repository.getAllDecks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Hàng đợi thẻ cần học
    private val _studyQueue = MutableStateFlow<List<Card>>(emptyList())
    val studyQueue: StateFlow<List<Card>> = _studyQueue.asStateFlow()

    // ── Deck ──────────────────────────────────────────────
    fun addDeck(name: String, description: String = "") {
        viewModelScope.launch {
            repository.insertDeck(Deck(name = name, description = description))
        }
    }

    fun deleteDeck(deck: Deck) {
        viewModelScope.launch { repository.deleteDeck(deck) }
    }

    // ── Card ──────────────────────────────────────────────
    fun getCardsByDeck(deckId: Int): Flow<List<Card>> = repository.getCardsByDeck(deckId)

    fun getDueCount(deckId: Int): Flow<Int> = repository.getDueCardCount(deckId)

    fun addCard(deckId: Int, front: String, back: String) {
        viewModelScope.launch {
            repository.insertCard(Card(deckId = deckId, front = front, back = back))
        }
    }

    fun deleteCard(card: Card) {
        viewModelScope.launch { repository.deleteCard(card) }
    }

    // ── Study ─────────────────────────────────────────────
    fun loadStudyQueue(deckId: Int) {
        viewModelScope.launch {
            val dueCards = repository.getDueCards(deckId)
            _studyQueue.value = dueCards
        }
    }

    fun answerCard(card: Card, quality: ReviewQuality) {
        viewModelScope.launch {
            val updated = Sm2Algorithm.calculate(card, quality)
            repository.updateCard(updated)
            // Xóa thẻ vừa trả lời khỏi hàng đợi
            _studyQueue.value = _studyQueue.value.drop(1)
        }
    }
}
