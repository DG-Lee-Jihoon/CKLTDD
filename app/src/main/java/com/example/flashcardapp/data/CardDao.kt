package com.example.flashcardapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: Deck): Long

    @Delete
    suspend fun deleteDeck(deck: Deck)

    @Query("SELECT * FROM decks")
    fun getAllDecks(): Flow<List<Deck>>

    @Query("SELECT * FROM decks WHERE id = :deckId")
    suspend fun getDeckById(deckId: Int): Deck?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: Card)

    @Update
    suspend fun updateCard(card: Card)

    @Delete
    suspend fun deleteCard(card: Card)

    @Query("SELECT * FROM cards WHERE deckId = :deckId")
    fun getCardsByDeck(deckId: Int): Flow<List<Card>>

    @Query("SELECT * FROM cards WHERE deckId = :deckId AND dueDate <= :now ORDER BY dueDate ASC")
    suspend fun getDueCards(deckId: Int, now: Long = System.currentTimeMillis()): List<Card>

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId AND dueDate <= :now")
    fun getDueCardCount(deckId: Int, now: Long = System.currentTimeMillis()): Flow<Int>

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId")
    fun getTotalCardCount(deckId: Int): Flow<Int>

    @Query("SELECT * FROM cards WHERE dueDate <= :now")
    suspend fun getAllDueCards(now: Long): List<Card>

    @Query("SELECT * FROM cards WHERE deckId = :deckId")
    suspend fun getCardsByDeckOnce(deckId: Int): List<Card>
}