package com.example.flashcardapp.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.flashcardapp.data.*
import com.example.flashcardapp.util.applySmTwo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CardViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = CardRepository(
        AppDatabase.getInstance(application).cardDao()
    )

    // ── Decks ─────────────────────────────────────────
    val allDecks: StateFlow<List<Deck>> = repo.allDecks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addDeck(name: String, description: String = "") {
        viewModelScope.launch {
            repo.insertDeck(Deck(name = name, description = description))
        }
    }

    fun deleteDeck(deck: Deck) {
        viewModelScope.launch { repo.deleteDeck(deck) }
    }

    fun getCardCount(deckId: Long): Flow<Int> = repo.getCardCount(deckId)

    fun getDueCardCount(deckId: Long): Flow<Int> = repo.getDueCardCount(deckId)

    // ── Cards ─────────────────────────────────────────
    fun getCardsByDeck(deckId: Long): Flow<List<Card>> = repo.getCardsByDeck(deckId)

    fun addCard(deckId: Long, front: String, back: String) {
        viewModelScope.launch {
            repo.insertCard(Card(deckId = deckId, front = front, back = back))
        }
    }

    fun updateCard(card: Card) {
        viewModelScope.launch { repo.updateCard(card) }
    }

    fun deleteCard(card: Card) {
        viewModelScope.launch { repo.deleteCard(card) }
    }

    // ── Study session ─────────────────────────────────
    private val _dueCards = MutableStateFlow<List<Card>>(emptyList())
    val dueCards: StateFlow<List<Card>> = _dueCards.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _studyFinished = MutableStateFlow(false)
    val studyFinished: StateFlow<Boolean> = _studyFinished.asStateFlow()

    val currentCard: StateFlow<Card?> = combine(_dueCards, _currentIndex) { cards, idx ->
        cards.getOrNull(idx)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun loadDueCards(deckId: Long) {
        viewModelScope.launch {
            val cards = repo.getDueCards(deckId)
            _dueCards.value = cards
            _currentIndex.value = 0
            _studyFinished.value = cards.isEmpty()
        }
    }

    fun rateCard(quality: Int) {
        viewModelScope.launch {
            val card = currentCard.value ?: return@launch
            val updated = applySmTwo(card, quality)
            repo.updateCard(updated)

            val nextIndex = _currentIndex.value + 1
            if (nextIndex < _dueCards.value.size) {
                _currentIndex.value = nextIndex
            } else {
                _studyFinished.value = true
                _dueCards.value = emptyList()
            }
        }
    }

    fun resetStudy() {
        _studyFinished.value = false
        _dueCards.value = emptyList()
        _currentIndex.value = 0
    }
}

class CardViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CardViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}