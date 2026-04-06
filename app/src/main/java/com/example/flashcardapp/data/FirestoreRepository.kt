package com.example.flashcardapp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val userId get() = auth.currentUser?.uid ?: ""

    // ── Deck ──────────────────────────────────────────

    suspend fun syncDeckToCloud(deck: Deck) {
        if (userId.isBlank()) return
        db.collection("users")
            .document(userId)
            .collection("decks")
            .document(deck.id.toString())
            .set(deck.toMap())
            .await()
    }

    suspend fun deleteDeckFromCloud(deckId: Long) {
        if (userId.isBlank()) return
        // Xóa deck
        db.collection("users")
            .document(userId)
            .collection("decks")
            .document(deckId.toString())
            .delete()
            .await()
        // Xóa toàn bộ card trong deck
        val cards = db.collection("users")
            .document(userId)
            .collection("cards")
            .whereEqualTo("deckId", deckId)
            .get()
            .await()
        cards.documents.forEach { it.reference.delete().await() }
    }

    suspend fun getDecksFromCloud(): List<Deck> {
        if (userId.isBlank()) return emptyList()
        return db.collection("users")
            .document(userId)
            .collection("decks")
            .get()
            .await()
            .documents
            .mapNotNull { it.toDeck() }
    }

    // ── Card ──────────────────────────────────────────

    suspend fun syncCardToCloud(card: Card) {
        if (userId.isBlank()) return
        db.collection("users")
            .document(userId)
            .collection("cards")
            .document(card.id.toString())
            .set(card.toMap())
            .await()
    }

    suspend fun deleteCardFromCloud(cardId: Long) {
        if (userId.isBlank()) return
        db.collection("users")
            .document(userId)
            .collection("cards")
            .document(cardId.toString())
            .delete()
            .await()
    }

    suspend fun getCardsFromCloud(): List<Card> {
        if (userId.isBlank()) return emptyList()
        return db.collection("users")
            .document(userId)
            .collection("cards")
            .get()
            .await()
            .documents
            .mapNotNull { it.toCard() }
    }

    // ── Pull toàn bộ dữ liệu về máy ──────────────────

    suspend fun pullAllData(dao: CardDao) {
        if (userId.isBlank()) return

        val cloudDecks = getDecksFromCloud()
        val cloudCards = getCardsFromCloud()

        // Lưu decks vào local
        cloudDecks.forEach { dao.insertDeck(it) }
        // Lưu cards vào local
        cloudCards.forEach { dao.insertCard(it) }
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
    "easeFactor"     to easeFactor,
    "nextReviewDate" to nextReviewDate,
    "createdAt"      to createdAt
)

fun com.google.firebase.firestore.DocumentSnapshot.toDeck(): Deck? {
    return try {
        Deck(
            id          = getLong("id") ?: 0L,
            name        = getString("name") ?: "",
            description = getString("description") ?: "",
            createdAt   = getLong("createdAt") ?: 0L
        )
    } catch (e: Exception) { null }
}

fun com.google.firebase.firestore.DocumentSnapshot.toCard(): Card? {
    return try {
        Card(
            id             = getLong("id") ?: 0L,
            deckId         = getLong("deckId") ?: 0L,
            front          = getString("front") ?: "",
            back           = getString("back") ?: "",
            interval       = getLong("interval")?.toInt() ?: 1,
            repetition     = getLong("repetition")?.toInt() ?: 0,
            easeFactor     = (getDouble("easeFactor") ?: 2.5).toFloat(),
            nextReviewDate = getLong("nextReviewDate") ?: System.currentTimeMillis(),
            createdAt      = getLong("createdAt") ?: System.currentTimeMillis()
        )
    } catch (e: Exception) { null }
}