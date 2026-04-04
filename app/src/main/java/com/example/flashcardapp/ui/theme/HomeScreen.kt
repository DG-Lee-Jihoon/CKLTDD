package com.example.flashcardapp.ui.theme

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.flashcardapp.viewmodel.CardViewModel

@Composable
fun HomeScreen(
    viewModel: CardViewModel,
    onDeckClick: (Int) -> Unit
) {
    val decks by viewModel.allDecks.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var newDeckName by remember { mutableStateOf("") }
    var newDeckDesc by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDeep, Color(0xFF12122A))))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 16.dp)) {
                Text("FlashCard", fontSize = 34.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                Text("Học thông minh hơn mỗi ngày ✨", fontSize = 14.sp, color = TextSecondary)
            }

            if (decks.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AutoStories, null, tint = TextHint, modifier = Modifier.size(72.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Chưa có bộ thẻ nào", color = TextSecondary, fontSize = 16.sp)
                        Text("Nhấn + để bắt đầu", color = TextHint, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(decks, key = { it.id }) { deck ->
                        val dueCount by viewModel.getDueCount(deck.id).collectAsStateWithLifecycle(0)
                        val totalCount by viewModel.getTotalCount(deck.id).collectAsStateWithLifecycle(0)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(6.dp, RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                                .background(Brush.linearGradient(listOf(BgCard, BgCardAlt)))
                                .clickable { onDeckClick(deck.id) }
                                .padding(18.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(AccentPurple, AccentViolet))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(deck.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(deck.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                    Text("$totalCount thẻ", color = TextSecondary, fontSize = 12.sp)
                                }
                                if (dueCount > 0) {
                                    Box(
                                        modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(ColorAgain).padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) { Text("$dueCount hạn", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                } else {
                                    Text("✓", color = ColorGood, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp).size(60.dp),
            shape = CircleShape,
            containerColor = AccentPurple
        ) { Icon(Icons.Default.Add, null, modifier = Modifier.size(28.dp)) }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = BgCard,
            title = { Text("Tạo bộ thẻ mới", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    StyledTextField(value = newDeckName, onValueChange = { newDeckName = it }, label = "Tên bộ thẻ")
                    StyledTextField(value = newDeckDesc, onValueChange = { newDeckDesc = it }, label = "Mô tả (tùy chọn)")
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newDeckName.isNotBlank()) {
                        viewModel.addDeck(newDeckName.trim(), newDeckDesc.trim())
                        newDeckName = ""; newDeckDesc = ""; showDialog = false
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)) {
                    Text("Tạo", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Hủy", color = TextSecondary) } }
        )
    }
}

@Composable
fun StyledTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label) }, singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentPurple, focusedLabelColor = AccentPurple,
            cursorColor = AccentPurple, focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary, unfocusedLabelColor = TextSecondary,
            unfocusedBorderColor = Color(0xFF333355)
        )
    )
}