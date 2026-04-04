package com.example.flashcardapp.util

import com.example.flashcardapp.data.Card
import com.example.flashcardapp.data.Deck
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseSync {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun userId() = auth.currentUser?.uid

    // ── Đẩy thẻ lên cloud ────────────────────────────────
    fun uploadCard(card: Card) {
        val uid = userId() ?: return
        val data = mapOf(
            "id"          to card.id,
            "deckId"      to card.deckId,
            "front"       to card.front,
            "back"        to card.back,
            "interval"    to card.interval,
            "repetition"  to card.repetition,
            "easeFactor"  to card.easeFactor,
            "dueDate"     to card.dueDate
        )
        db.collection("users").document(uid)
            .collection("cards").document(card.id.toString())
            .set(data)
    }

    // ── Đẩy deck lên cloud ───────────────────────────────
    fun uploadDeck(deck: Deck) {
        val uid = userId() ?: return
        val data = mapOf(
            "id"          to deck.id,
            "name"        to deck.name,
            "description" to deck.description
        )
        db.collection("users").document(uid)
            .collection("decks").document(deck.id.toString())
            .set(data)
    }

    // ── Máy B ngóng thẻ từ cloud ─────────────────────────
    fun listenCards(onCardReceived: (Card) -> Unit) {
        val uid = userId() ?: return
        db.collection("users").document(uid)
            .collection("cards")
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener
                for (doc in snapshots.documents) {
                    val card = Card(
                        id         = (doc.getLong("id") ?: 0).toInt(),
                        deckId     = (doc.getLong("deckId") ?: 0).toInt(),
                        front      = doc.getString("front") ?: "",
                        back       = doc.getString("back") ?: "",
                        interval   = (doc.getLong("interval") ?: 1).toInt(),
                        repetition = (doc.getLong("repetition") ?: 0).toInt(),
                        easeFactor = doc.getDouble("easeFactor") ?: 2.5,
                        dueDate    = doc.getLong("dueDate") ?: System.currentTimeMillis()
                    )
                    onCardReceived(card)
                }
            }
    }

    // ── Máy B ngóng deck từ cloud ────────────────────────
    fun listenDecks(onDeckReceived: (Deck) -> Unit) {
        val uid = userId() ?: return
        db.collection("users").document(uid)
            .collection("decks")
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener
                for (doc in snapshots.documents) {
                    val deck = Deck(
                        id          = (doc.getLong("id") ?: 0).toInt(),
                        name        = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: ""
                    )
                    onDeckReceived(deck)
                }
            }
    }

    // ── Đăng nhập bằng email ─────────────────────────────
    fun signIn(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Đăng nhập thất bại") }
    }

    // ── Đăng ký tài khoản mới ────────────────────────────
    fun signUp(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Đăng ký thất bại") }
    }

    fun isLoggedIn() = auth.currentUser != null

    fun signOut() = auth.signOut()

    fun currentEmail() = auth.currentUser?.email ?: ""
}