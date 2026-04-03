package com.example.flashcardapp.util

import com.example.flashcardapp.data.Card
import com.example.flashcardapp.ui.theme.ReviewQuality

object Sm2Algorithm {

    /**
     * Tính toán lịch ôn tập tiếp theo theo thuật toán SM-2.
     *
     * @param card   Thẻ hiện tại
     * @param quality Mức độ nhớ: Again(0), Hard(1), Good(3), Easy(5)
     * @return Card mới với interval, repetition, easeFactor, dueDate đã cập nhật
     */
    fun calculate(card: Card, quality: ReviewQuality): Card {
        val q = quality.value

        val newEaseFactor = (card.easeFactor + 0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
            .coerceAtLeast(1.3) // EF tối thiểu là 1.3

        return if (q < 3) {
            // Trả lời sai → reset, ôn lại ngay ngày mai
            card.copy(
                repetition = 0,
                interval = 1,
                easeFactor = newEaseFactor,
                dueDate = System.currentTimeMillis() + daysToMillis(1)
            )
        } else {
            // Trả lời đúng
            val newInterval = when (card.repetition) {
                0 -> 1
                1 -> 6
                else -> (card.interval * newEaseFactor).toInt()
            }
            card.copy(
                repetition = card.repetition + 1,
                interval = newInterval,
                easeFactor = newEaseFactor,
                dueDate = System.currentTimeMillis() + daysToMillis(newInterval)
            )
        }
    }

    private fun daysToMillis(days: Int): Long = days * 24L * 60 * 60 * 1000
}