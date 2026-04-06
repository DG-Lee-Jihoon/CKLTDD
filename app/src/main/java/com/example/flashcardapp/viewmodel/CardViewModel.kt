package com.example.flashcardapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.flashcardapp.data.*
import com.example.flashcardapp.util.applySmTwo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CardViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "CardViewModel"
    }

    private val repo   = CardRepository(AppDatabase.getInstance(application).cardDao())
    private val rtRepo = RealtimeRepository()
    private val dao    = AppDatabase.getInstance(application).cardDao()

    // ── Trạng thái đồng bộ ────────────────────────────
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    // ── Decks ─────────────────────────────────────────
    val allDecks: StateFlow<List<Deck>> = repo.allDecks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addDeck(name: String, description: String = "") {
        viewModelScope.launch {
            try {
                val id   = repo.insertDeck(Deck(name = name, description = description))
                val deck = dao.getDeckById(id)
                deck?.let {
                    rtRepo.syncDeckToCloud(it)
                    Log.d(TAG, "addDeck: đã sync deck '${it.name}' lên cloud")
                }
            } catch (e: Exception) {
                Log.e(TAG, "addDeck thất bại: ${e.message}")
            }
        }
    }

    fun deleteDeck(deck: Deck) {
        viewModelScope.launch {
            try {
                repo.deleteDeck(deck)
                rtRepo.deleteDeckFromCloud(deck.id)
                Log.d(TAG, "deleteDeck: đã xóa deck ${deck.id} khỏi cloud")
            } catch (e: Exception) {
                Log.e(TAG, "deleteDeck thất bại: ${e.message}")
            }
        }
    }

    fun getCardCount(deckId: Long): Flow<Int>    = repo.getCardCount(deckId)
    fun getDueCardCount(deckId: Long): Flow<Int> = repo.getDueCardCount(deckId)

    // ── Cards ─────────────────────────────────────────
    fun getCardsByDeck(deckId: Long): Flow<List<Card>> = repo.getCardsByDeck(deckId)

    fun addCard(deckId: Long, front: String, back: String) {
        viewModelScope.launch {
            try {
                val card = Card(deckId = deckId, front = front, back = back)
                repo.insertCard(card)
                // ✅ FIX: Lấy card vừa insert bằng cách tìm theo front+back+deckId
                val inserted = dao.getAllCardsOnce()
                    .filter { it.deckId == deckId && it.front == front && it.back == back }
                    .maxByOrNull { it.id }
                inserted?.let {
                    rtRepo.syncCardToCloud(it)
                    Log.d(TAG, "addCard: đã sync card '${it.front}' lên cloud (id=${it.id})")
                }
            } catch (e: Exception) {
                Log.e(TAG, "addCard thất bại: ${e.message}")
            }
        }
    }

    fun updateCard(card: Card) {
        viewModelScope.launch {
            try {
                repo.updateCard(card)
                rtRepo.syncCardToCloud(card)
            } catch (e: Exception) {
                Log.e(TAG, "updateCard thất bại: ${e.message}")
            }
        }
    }

    fun deleteCard(card: Card) {
        viewModelScope.launch {
            try {
                repo.deleteCard(card)
                rtRepo.deleteCardFromCloud(card.id)
            } catch (e: Exception) {
                Log.e(TAG, "deleteCard thất bại: ${e.message}")
            }
        }
    }

    // ── Đồng bộ từ cloud về máy ───────────────────────
    fun pullFromCloud() {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.Syncing
            try {
                rtRepo.pullAllData(dao)
                _syncStatus.value = SyncStatus.Success
                Log.d(TAG, "pullFromCloud: thành công")
            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.Error(e.message ?: "Lỗi không xác định")
                Log.e(TAG, "pullFromCloud thất bại: ${e.message}")
            }
        }
    }

    // ✅ FIX MỚI: Đẩy toàn bộ dữ liệu local lên cloud
    // Dùng khi muốn "backup" dữ liệu hiện có lên Firebase
    fun pushAllToCloud() {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.Syncing
            try {
                val decks = dao.getAllDecksOnce()
                val cards = dao.getAllCardsOnce()
                decks.forEach { rtRepo.syncDeckToCloud(it) }
                cards.forEach { rtRepo.syncCardToCloud(it) }
                _syncStatus.value = SyncStatus.Success
                Log.d(TAG, "pushAllToCloud: xong - ${decks.size} deck, ${cards.size} card")
            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.Error(e.message ?: "Lỗi không xác định")
                Log.e(TAG, "pushAllToCloud thất bại: ${e.message}")
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
            rtRepo.syncCardToCloud(updated)

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
}

// ── Trạng thái đồng bộ ───────────────────────────────
sealed class SyncStatus {
    object Idle    : SyncStatus()
    object Syncing : SyncStatus()
    object Success : SyncStatus()
    data class Error(val message: String) : SyncStatus()
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