package com.example.flashcardapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.data.*
import com.example.flashcardapp.ui.theme.ReviewQuality
import com.example.flashcardapp.util.FirebaseSync
import com.example.flashcardapp.util.Sm2Algorithm
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = CardRepository(db.cardDao())

    val allDecks: StateFlow<List<Deck>> = repository.getAllDecks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _studyQueue = MutableStateFlow<List<Card>>(emptyList())
    val studyQueue: StateFlow<List<Card>> = _studyQueue.asStateFlow()

    init {
        if (FirebaseSync.isLoggedIn()) syncFromCloud()
    }

    private fun syncFromCloud() {
        FirebaseSync.listenDecks { deck ->
            viewModelScope.launch { repository.insertDeck(deck) }
        }
        FirebaseSync.listenCards { card ->
            viewModelScope.launch { repository.insertCard(card) }
        }
    }

    fun addDeck(name: String, description: String = "") {
        viewModelScope.launch {
            val id = repository.insertDeck(Deck(name = name, description = description))
            if (FirebaseSync.isLoggedIn()) {
                FirebaseSync.uploadDeck(Deck(id = id.toInt(), name = name, description = description))
            }
        }
    }

    fun deleteDeck(deck: Deck) {
        viewModelScope.launch { repository.deleteDeck(deck) }
    }

    fun getCardsByDeck(deckId: Int): Flow<List<Card>> = repository.getCardsByDeck(deckId)
    fun getDueCount(deckId: Int): Flow<Int> = repository.getDueCardCount(deckId)
    fun getTotalCount(deckId: Int): Flow<Int> = repository.getTotalCardCount(deckId)

    fun addCard(deckId: Int, front: String, back: String) {
        viewModelScope.launch {
            val card = Card(deckId = deckId, front = front, back = back)
            repository.insertCard(card)
            if (FirebaseSync.isLoggedIn()) FirebaseSync.uploadCard(card)
        }
    }

    fun deleteCard(card: Card) {
        viewModelScope.launch { repository.deleteCard(card) }
    }

    fun loadStudyQueue(deckId: Int) {
        viewModelScope.launch { _studyQueue.value = repository.getDueCards(deckId) }
    }

    fun answerCard(card: Card, quality: ReviewQuality) {
        viewModelScope.launch {
            val updated = Sm2Algorithm.calculate(card, quality)
            repository.updateCard(updated)
            if (FirebaseSync.isLoggedIn()) FirebaseSync.uploadCard(updated)
            _studyQueue.value = _studyQueue.value.drop(1)
        }
    }
}