package com.example.flashcardapp.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.flashcardapp.data.*
import com.example.flashcardapp.util.applySmTwo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CardViewModel(application: Application) : AndroidViewModel(application) {

    private val repo   = CardRepository(AppDatabase.getInstance(application).cardDao())
    private val fsRepo = FirestoreRepository()
    private val dao    = AppDatabase.getInstance(application).cardDao()

    private var deckListener: ListenerRegistration? = null
    private var cardListener: ListenerRegistration? = null

    // ── Decks ─────────────────────────────────────────
    val allDecks: StateFlow<List<Deck>> = repo.allDecks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addDeck(name: String, description: String = "") {
        viewModelScope.launch {
            val id = repo.insertDeck(Deck(name = name, description = description))
            val deck = dao.getDeckById(id)
            deck?.let { fsRepo.syncDeckToCloud(it) }
        }
    }

    fun deleteDeck(deck: Deck) {
        viewModelScope.launch {
            repo.deleteDeck(deck)
            fsRepo.deleteDeckFromCloud(deck.id)
        }
    }

    fun getCardCount(deckId: Long): Flow<Int>    = repo.getCardCount(deckId)
    fun getDueCardCount(deckId: Long): Flow<Int> = repo.getDueCardCount(deckId)

    // ── Cards ─────────────────────────────────────────
    fun getCardsByDeck(deckId: Long): Flow<List<Card>> = repo.getCardsByDeck(deckId)

    fun addCard(deckId: Long, front: String, back: String) {
        viewModelScope.launch {
            val card = Card(deckId = deckId, front = front, back = back)
            repo.insertCard(card)
            val cards = dao.getDueCards(deckId, Long.MAX_VALUE)
            cards.find { it.front == front && it.back == back }
                ?.let { fsRepo.syncCardToCloud(it) }
        }
    }

    fun updateCard(card: Card) {
        viewModelScope.launch {
            repo.updateCard(card)
            fsRepo.syncCardToCloud(card)
        }
    }

    fun deleteCard(card: Card) {
        viewModelScope.launch {
            repo.deleteCard(card)
            fsRepo.deleteCardFromCloud(card.id)
        }
    }

    // ── Đồng bộ từ cloud về máy ───────────────────────
    fun pullFromCloud() {
        viewModelScope.launch {
            try {
                fsRepo.pullAllData(dao)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ── Lắng nghe realtime từ Firestore ───────────────
    fun observeCloudData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db     = FirebaseFirestore.getInstance()

        // Hủy listener cũ nếu có
        deckListener?.remove()
        cardListener?.remove()

        // Lắng nghe deck realtime
        deckListener = db.collection("users")
            .document(userId)
            .collection("decks")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                snapshot.documentChanges.forEach { change ->
                    when (change.type) {
                        com.google.firebase.firestore.DocumentChange.Type.ADDED,
                        com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                            val deck = change.document.toDeck() ?: return@forEach
                            viewModelScope.launch(Dispatchers.IO) {
                                dao.insertDeck(deck)
                            }
                        }
                        com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                            val deck = change.document.toDeck() ?: return@forEach
                            viewModelScope.launch(Dispatchers.IO) {
                                dao.deleteDeck(deck)
                            }
                        }
                    }
                }
            }

        // Lắng nghe card realtime
        cardListener = db.collection("users")
            .document(userId)
            .collection("cards")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                snapshot.documentChanges.forEach { change ->
                    when (change.type) {
                        com.google.firebase.firestore.DocumentChange.Type.ADDED,
                        com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                            val card = change.document.toCard() ?: return@forEach
                            viewModelScope.launch(Dispatchers.IO) {
                                dao.insertCard(card)
                            }
                        }
                        com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                            val card = change.document.toCard() ?: return@forEach
                            viewModelScope.launch(Dispatchers.IO) {
                                dao.deleteCard(card)
                            }
                        }
                    }
                }
            }
    }

    // ── Study session ─────────────────────────────────
    private val _dueCards      = MutableStateFlow<List<Card>>(emptyList())
    val dueCards: StateFlow<List<Card>> = _dueCards.asStateFlow()

    private val _currentIndex  = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _studyFinished = MutableStateFlow(false)
    val studyFinished: StateFlow<Boolean> = _studyFinished.asStateFlow()

    val currentCard: StateFlow<Card?> = combine(_dueCards, _currentIndex) { cards, idx ->
        cards.getOrNull(idx)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun loadDueCards(deckId: Long) {
        viewModelScope.launch {
            val cards = repo.getDueCards(deckId)
            _dueCards.value      = cards
            _currentIndex.value  = 0
            _studyFinished.value = cards.isEmpty()
        }
    }

    fun rateCard(quality: Int) {
        viewModelScope.launch {
            val card    = currentCard.value ?: return@launch
            val updated = applySmTwo(card, quality)
            repo.updateCard(updated)
            fsRepo.syncCardToCloud(updated)

            if (quality < 3) {
                val currentList = _dueCards.value.toMutableList()
                currentList.removeAt(_currentIndex.value)
                currentList.add(updated)
                _dueCards.value = currentList
                if (currentList.isEmpty()) _studyFinished.value = true
            } else {
                val nextIndex = _currentIndex.value + 1
                if (nextIndex < _dueCards.value.size) {
                    _currentIndex.value = nextIndex
                } else {
                    _studyFinished.value = true
                    _dueCards.value      = emptyList()
                }
            }
        }
    }

    fun resetStudy() {
        _studyFinished.value = false
        _dueCards.value      = emptyList()
        _currentIndex.value  = 0
    }

    // ── Hủy listener khi ViewModel bị destroy ─────────
    override fun onCleared() {
        super.onCleared()
        deckListener?.remove()
        cardListener?.remove()
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