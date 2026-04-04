package com.example.flashcardapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.flashcardapp.data.Card
import com.example.flashcardapp.viewmodel.CardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckScreen(
    deckId: Long,
    deckName: String,
    viewModel: CardViewModel,
    onStudyClick: () -> Unit,
    onAddCard: () -> Unit,
    onStatsClick: () -> Unit,
    onBack: () -> Unit
) {
    val cards by viewModel.getCardsByDeck(deckId).collectAsState(initial = emptyList())
    val dueCount by viewModel.getDueCardCount(deckId).collectAsState(initial = 0)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCard,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm thẻ")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header
            item {
                DeckHeader(
                    deckName = deckName,
                    totalCards = cards.size,
                    dueCount = dueCount,
                    onBack = onBack,
                    onStudyClick = onStudyClick,
                    onStatsClick = onStatsClick
                )
            }

            // Section title
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Tất cả thẻ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "${cards.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (cards.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.NoteAdd,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Chưa có thẻ nào",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(cards, key = { it.id }) { card ->
                CardItem(card = card, onDelete = { viewModel.deleteCard(card) })
            }
        }
    }
}

@Composable
fun DeckHeader(
    deckName: String,
    totalCards: Int,
    dueCount: Int,
    onBack: () -> Unit,
    onStudyClick: () -> Unit,
    onStatsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Back button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
            }

            Spacer(Modifier.height(12.dp))

            Text(
                deckName,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatPill(label = "$totalCards thẻ", icon = Icons.Outlined.Style)
                if (dueCount > 0) {
                    StatPill(label = "$dueCount cần ôn", icon = Icons.Outlined.Notifications)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onStudyClick,
                    enabled = dueCount > 0,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = Color.White.copy(alpha = 0.4f),
                        disabledContentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Ôn tập", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onStatsClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.5.dp
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.BarChart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Thống kê", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun CardItem(card: Card, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    card.front,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    card.back,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MiniChip(text = "Lần ${card.repetition}", color = MaterialTheme.colorScheme.primaryContainer)
                    MiniChip(text = "${card.interval}d", color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Xoá",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}