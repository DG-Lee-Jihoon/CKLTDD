package com.example.flashcardapp.ui.theme

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.flashcardapp.data.Card
import com.example.flashcardapp.viewmodel.CardViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatsScreen(deckId: Int, viewModel: CardViewModel, onBack: () -> Unit) {
    val cards by viewModel.getCardsByDeck(deckId).collectAsStateWithLifecycle(emptyList())
    val now = System.currentTimeMillis()

    val total = cards.size
    val due = cards.count { it.dueDate <= now }
    val learned = cards.count { it.repetition > 0 }
    val avgEF = if (cards.isEmpty()) 2.5 else cards.map { it.easeFactor }.average()

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDeep, Color(0xFF12122A))))
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack,
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(BgCard)
                    ) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) }
                    Spacer(Modifier.width(12.dp))
                    Text("Thống kê", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 24.sp)
                }
            }

            // Summary cards
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatBox("Tổng thẻ", "$total", AccentPurple, Modifier.weight(1f))
                    StatBox("Đến hạn", "$due", if (due > 0) ColorAgain else ColorGood, Modifier.weight(1f))
                    StatBox("Đã học", "$learned", ColorEasy, Modifier.weight(1f))
                }
            }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .background(BgCard).padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Độ dễ trung bình (EF)", color = TextSecondary, fontSize = 13.sp)
                            Text("%.2f".format(avgEF), color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Black)
                        }
                        // EF indicator
                        val efColor = when {
                            avgEF >= 2.5 -> ColorGood
                            avgEF >= 2.0 -> ColorHard
                            else -> ColorAgain
                        }
                        Box(
                            modifier = Modifier.size(50.dp).clip(CircleShape)
                                .background(efColor.copy(alpha = 0.15f))
                                .border(2.dp, efColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (avgEF >= 2.5) "😊" else if (avgEF >= 2.0) "😓" else "😵",
                                fontSize = 22.sp
                            )
                        }
                    }
                }
            }

            item {
                Text("Chi tiết từng thẻ", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            items(cards) { card ->
                CardStatItem(card = card, now = now)
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, color: Color, modifier: Modifier) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = color, fontSize = 26.sp, fontWeight = FontWeight.Black)
            Text(label, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun CardStatItem(card: Card, now: Long) {
    val isDue = card.dueDate <= now
    val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(BgCard).padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(card.front, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniTag("×${card.repetition}", AccentPurple)
                    MiniTag("${card.interval}d", ColorEasy)
                    MiniTag("EF ${"%.1f".format(card.easeFactor)}", ColorGood)
                }
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    .background(if (isDue) ColorAgain.copy(0.15f) else ColorGood.copy(0.1f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    if (isDue) "Hôm nay" else sdf.format(Date(card.dueDate)),
                    color = if (isDue) ColorAgain else ColorGood,
                    fontSize = 11.sp, fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun MiniTag(text: String, color: Color) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) { Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
}