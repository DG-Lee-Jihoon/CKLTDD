package com.example.flashcardapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "cards",
    foreignKeys = [ForeignKey(
        entity = Deck::class,
        parentColumns = ["id"],
        childColumns = ["deckId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Card(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deckId: Int,
    val front: String,
    val back: String,

    // SM-2 fields
    val interval: Int = 1,         // số ngày đến lần ôn tiếp theo
    val repetition: Int = 0,       // số lần ôn đúng liên tiếp
    val easeFactor: Double = 2.5,  // hệ số dễ/khó
    val dueDate: Long = System.currentTimeMillis() // thời điểm cần ôn
)