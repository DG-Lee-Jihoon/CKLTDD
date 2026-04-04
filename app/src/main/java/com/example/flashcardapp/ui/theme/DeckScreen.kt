package com.example.flashcardapp.ui.theme

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.flashcardapp.viewmodel.CardViewModel

@Composable
fun DeckScreen(
    deckId: Int,
    viewModel: CardViewModel,
    onStudyClick: () -> Unit,
    onAddCard: () -> Unit,
    onStatsClick: () -> Unit,
    onBack: () -> Unit
) {
    val cards by viewModel.getCardsByDeck(deckId).collectAsStateWithLifecycle(emptyList())
    val dueCount by viewModel.getDueCount(deckId).collectAsStateWithLifecycle(0)

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDeep, Color(0xFF12122A))))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack,
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(BgCard)
                ) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onStatsClick,
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(BgCard)
                ) { Icon(Icons.Default.BarChart, null, tint = AccentPurple) }
            }

            // Study button
            Box(
                modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (dueCount > 0)
                            Brush.linearGradient(listOf(AccentPurple, AccentViolet))
                        else
                            Brush.linearGradient(listOf(Color(0xFF2A2A4A), Color(0xFF2A2A4A)))
                    )
                    .clickable(enabled = dueCount > 0) { onStudyClick() }
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            if (dueCount > 0) "Học ngay" else "Không có thẻ đến hạn",
                            color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp
                        )
                        if (dueCount > 0)
                            Text("$dueCount thẻ đang chờ bạn", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Cards list header
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tất cả thẻ", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(BgCard).padding(horizontal = 8.dp, vertical = 2.dp)
                ) { Text("${cards.size}", color = AccentPurple, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            }

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(cards, key = { it.id }) { card ->
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(BgCard)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(card.front, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(card.back, color = TextSecondary, fontSize = 13.sp)
                            }
                            IconButton(onClick = { viewModel.deleteCard(card) }) {
                                Icon(Icons.Default.Delete, null, tint = Color(0xFF555577))
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }

        // FAB thêm thẻ
        FloatingActionButton(
            onClick = onAddCard,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp).size(60.dp),
            shape = CircleShape, containerColor = AccentPurple
        ) { Icon(Icons.Default.Add, null, modifier = Modifier.size(28.dp)) }
    }
}