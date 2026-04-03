package com.example.flashcardapp.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.flashcardapp.data.Card
import com.example.flashcardapp.viewmodel.CardViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    deckId: Int,
    viewModel: CardViewModel,
    onBack: () -> Unit
) {
    val cards by viewModel.getCardsByDeck(deckId).collectAsStateWithLifecycle(initialValue = emptyList())
    val now = System.currentTimeMillis()

    val totalCards = cards.size
    val dueCards = cards.count { it.dueDate <= now }
    val learnedCards = cards.count { it.repetition > 0 }
    val avgEase = if (cards.isEmpty()) 2.5 else cards.map { it.easeFactor }.average()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thống kê") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tổng quan
            item {
                Text("Tổng quan", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("Tổng thẻ", "$totalCards", Modifier.weight(1f))
                    StatCard("Đến hạn", "$dueCards", Modifier.weight(1f), highlight = dueCards > 0)
                    StatCard("Đã học", "$learnedCards", Modifier.weight(1f))
                }
            }

            item {
                StatCard("Độ dễ trung bình", "%.2f".format(avgEase), Modifier.fillMaxWidth())
            }

            // Danh sách thẻ kèm trạng thái
            item {
                Spacer(Modifier.height(4.dp))
                Text("Chi tiết từng thẻ", style = MaterialTheme.typography.titleMedium)
            }

            items(cards) { card ->
                CardStatRow(card = card, now = now)
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun CardStatRow(card: Card, now: Long) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dueDateStr = sdf.format(Date(card.dueDate))
    val isDue = card.dueDate <= now

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(card.front, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                Text(
                    "Lần ôn: ${card.repetition} | Khoảng: ${card.interval}d | EF: ${"%.1f".format(card.easeFactor)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (isDue) "Đến hạn" else dueDateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDue) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}