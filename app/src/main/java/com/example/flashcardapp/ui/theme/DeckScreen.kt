package com.example.flashcardapp.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.flashcardapp.viewmodel.CardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckScreen(
    deckId: Int,
    viewModel: CardViewModel,
    onStudyClick: () -> Unit,
    onAddCard: () -> Unit,
    onStatsClick: () -> Unit,
    onBack: () -> Unit
) {
    val cards by viewModel.getCardsByDeck(deckId).collectAsStateWithLifecycle(initialValue = emptyList())
    val dueCount by viewModel.getDueCount(deckId).collectAsStateWithLifecycle(initialValue = 0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết bộ thẻ") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←")
                    }
                },
                actions = {
                    IconButton(onClick = onStatsClick) {
                        Icon(Icons.Default.BarChart, contentDescription = "Thống kê")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCard) {
                Icon(Icons.Default.Add, contentDescription = "Thêm thẻ")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Nút Study
            Button(
                onClick = onStudyClick,
                enabled = dueCount > 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (dueCount > 0) "Học ngay ($dueCount thẻ đến hạn)" else "Không có thẻ đến hạn")
            }

            Spacer(Modifier.height(16.dp))
            Text("Tất cả thẻ (${cards.size})", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(cards) { card ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(card.front, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    card.back,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.deleteCard(card) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Xóa")
                            }
                        }
                    }
                }
            }
        }
    }
}