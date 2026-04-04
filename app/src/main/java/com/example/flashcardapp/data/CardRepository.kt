package com.example.flashcardapp.data

import kotlinx.coroutines.flow.Flow

class CardRepository(private val dao: CardDao) {

    fun getAllDecks(): Flow<List<Deck>> = dao.getAllDecks()

    suspend fun getDeckById(id: Int): Deck? = dao.getDeckById(id)

    suspend fun insertDeck(deck: Deck): Long = dao.insertDeck(deck)

    suspend fun deleteDeck(deck: Deck) = dao.deleteDeck(deck)

    fun getCardsByDeck(deckId: Int): Flow<List<Card>> = dao.getCardsByDeck(deckId)

    suspend fun insertCard(card: Card) = dao.insertCard(card)

    suspend fun updateCard(card: Card) = dao.updateCard(card)

    suspend fun deleteCard(card: Card) = dao.deleteCard(card)

    suspend fun getDueCards(deckId: Int): List<Card> = dao.getDueCards(deckId)

    fun getDueCardCount(deckId: Int): Flow<Int> = dao.getDueCardCount(deckId)

    fun getTotalCardCount(deckId: Int): Flow<Int> = dao.getTotalCardCount(deckId)

    suspend fun getCardsByDeckOnce(deckId: Int): List<Card> = dao.getCardsByDeckOnce(deckId)
}