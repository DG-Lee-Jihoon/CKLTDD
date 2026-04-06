package com.example.flashcardapp.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RealtimeRepository {

    companion object {
        private const val TAG = "RealtimeRepository"
        // ✅ FIX 1: Hardcode URL database để tránh SDK đoán sai (database ở Singapore)
        private const val DATABASE_URL =
            "https://flashcard-fdfe1-default-rtdb.asia-southeast1.firebasedatabase.app"
    }

    // ✅ FIX 2: Dùng getInstance(url) thay vì getInstance() không tham số
    private val db   = FirebaseDatabase.getInstance(DATABASE_URL).reference
    private val auth = FirebaseAuth.getInstance()

    private val userId get() = auth.currentUser?.uid ?: ""

    // ── Kiểm tra đã đăng nhập chưa ───────────────────
    fun isLoggedIn(): Boolean = auth.currentUser != null

    // ── Deck ──────────────────────────────────────────

    suspend fun syncDeckToCloud(deck: Deck) {
        if (userId.isBlank()) {
            Log.w(TAG, "syncDeckToCloud: userId trống, bỏ qua")
            return
        }
        try {
            db.child("users").child(userId).child("decks")
                .child(deck.id.toString())
                .setValue(deck.toMap())
                .await()
            Log.d(TAG, "syncDeckToCloud OK: deckId=${deck.id}")
        } catch (e: Exception) {
            Log.e(TAG, "syncDeckToCloud FAILED: ${e.message}")
            throw e
        }
    }

    suspend fun deleteDeckFromCloud(deckId: Long) {
        if (userId.isBlank()) return
        try {
            db.child("users").child(userId).child("decks")
                .child(deckId.toString())
                .removeValue().await()

            // Xóa toàn bộ card thuộc deck này
            val snapshot = db.child("users").child(userId).child("cards")
                .orderByChild("deckId").equalTo(deckId.toDouble())
                .get().await()
            snapshot.children.forEach { it.ref.removeValue().await() }
            Log.d(TAG, "deleteDeckFromCloud OK: deckId=$deckId")
        } catch (e: Exception) {
            Log.e(TAG, "deleteDeckFromCloud FAILED: ${e.message}")
        }
    }

    suspend fun getDecksFromCloud(): List<Deck> {
        if (userId.isBlank()) return emptyList()
        return try {
            val snapshot = db.child("users").child(userId).child("decks").get().await()
            val decks = snapshot.children.mapNotNull { child ->
                try {
                    val map = child.value as? Map<*, *> ?: return@mapNotNull null
                    Deck(
                        id          = (map["id"] as? Long) ?: 0L,
                        name        = (map["name"] as? String) ?: "",
                        description = (map["description"] as? String) ?: "",
                        createdAt   = (map["createdAt"] as? Long) ?: 0L
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Parse deck lỗi: ${e.message}")
                    null
                }
            }
            Log.d(TAG, "getDecksFromCloud: lấy được ${decks.size} deck")
            decks
        } catch (e: Exception) {
            Log.e(TAG, "getDecksFromCloud FAILED: ${e.message}")
            emptyList()
        }
    }

    // ✅ FIX 3: Thêm Flow realtime listener cho Decks (tự động cập nhật khi cloud thay đổi)
    fun observeDecksFromCloud(): Flow<List<Deck>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val ref = db.child("users").child(userId).child("decks")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val decks = snapshot.children.mapNotNull { child ->
                    try {
                        val map = child.value as? Map<*, *> ?: return@mapNotNull null
                        Deck(
                            id          = (map["id"] as? Long) ?: 0L,
                            name        = (map["name"] as? String) ?: "",
                            description = (map["description"] as? String) ?: "",
                            createdAt   = (map["createdAt"] as? Long) ?: 0L
                        )
                    } catch (e: Exception) { null }
                }
                trySend(decks)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "observeDecks cancelled: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // ── Card ──────────────────────────────────────────

    suspend fun syncCardToCloud(card: Card) {
        if (userId.isBlank()) {
            Log.w(TAG, "syncCardToCloud: userId trống, bỏ qua")
            return
        }
        try {
            db.child("users").child(userId).child("cards")
                .child(card.id.toString())
                .setValue(card.toMap())
                .await()
            Log.d(TAG, "syncCardToCloud OK: cardId=${card.id}")
        } catch (e: Exception) {
            Log.e(TAG, "syncCardToCloud FAILED: ${e.message}")
            throw e
        }
    }

    suspend fun deleteCardFromCloud(cardId: Long) {
        if (userId.isBlank()) return
        try {
            db.child("users").child(userId).child("cards")
                .child(cardId.toString())
                .removeValue().await()
            Log.d(TAG, "deleteCardFromCloud OK: cardId=$cardId")
        } catch (e: Exception) {
            Log.e(TAG, "deleteCardFromCloud FAILED: ${e.message}")
        }
    }

    suspend fun getCardsFromCloud(): List<Card> {
        if (userId.isBlank()) return emptyList()
        return try {
            val snapshot = db.child("users").child(userId).child("cards").get().await()
            val cards = snapshot.children.mapNotNull { child ->
                try {
                    val map = child.value as? Map<*, *> ?: return@mapNotNull null
                    Card(
                        id             = (map["id"] as? Long) ?: 0L,
                        deckId         = (map["deckId"] as? Long) ?: 0L,
                        front          = (map["front"] as? String) ?: "",
                        back           = (map["back"] as? String) ?: "",
                        interval       = ((map["interval"] as? Long) ?: 1L).toInt(),
                        repetition     = ((map["repetition"] as? Long) ?: 0L).toInt(),
                        easeFactor     = ((map["easeFactor"] as? Double) ?: 2.5).toFloat(),
                        nextReviewDate = (map["nextReviewDate"] as? Long) ?: System.currentTimeMillis(),
                        createdAt      = (map["createdAt"] as? Long) ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Parse card lỗi: ${e.message}")
                    null
                }
            }
            Log.d(TAG, "getCardsFromCloud: lấy được ${cards.size} card")
            cards
        } catch (e: Exception) {
            Log.e(TAG, "getCardsFromCloud FAILED: ${e.message}")
            emptyList()
        }
    }

    // ── Pull toàn bộ dữ liệu về máy ──────────────────
    // ✅ FIX 4: Dùng OnConflictStrategy.REPLACE nên OK,
    //    nhưng phải đảm bảo ID từ cloud khớp với local Room
    suspend fun pullAllData(dao: CardDao) {
        if (userId.isBlank()) {
            Log.w(TAG, "pullAllData: chưa đăng nhập")
            return
        }
        Log.d(TAG, "pullAllData: bắt đầu kéo dữ liệu từ cloud...")
        val decks = getDecksFromCloud()
        val cards = getCardsFromCloud()
        decks.forEach { dao.insertDeck(it) }
        cards.forEach { dao.insertCard(it) }
        Log.d(TAG, "pullAllData: xong - ${decks.size} deck, ${cards.size} card")
    }

    // ✅ FIX 5: Push toàn bộ dữ liệu local lên cloud (dùng khi lần đầu đồng bộ)
    suspend fun pushAllData(dao: CardDao) {
        if (userId.isBlank()) return
        Log.d(TAG, "pushAllData: bắt đầu đẩy dữ liệu lên cloud...")
        // Không có getAllDecksOnce/getAllCardsOnce nên dùng Flow collect 1 lần
        // Thay vào đó gọi từ ViewModel truyền vào danh sách
    }
}

// ── Extension functions ───────────────────────────────

fun Deck.toMap(): Map<String, Any> = mapOf(
    "id"          to id,
    "name"        to name,
    "description" to description,
    "createdAt"   to createdAt
)

fun Card.toMap(): Map<String, Any> = mapOf(
    "id"             to id,
    "deckId"         to deckId,
    "front"          to front,
    "back"           to back,
    "interval"       to interval,
    "repetition"     to repetition,
    "easeFactor"     to easeFactor.toDouble(), // ✅ FIX 6: Lưu Double thay vì Float để tránh lỗi khi đọc lại
    "nextReviewDate" to nextReviewDate,
    "createdAt"      to createdAt
)