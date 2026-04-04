package com.example.flashcardapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import com.example.flashcardapp.viewmodel.CardViewModel
import androidx.compose.foundation.BorderStroke

@Composable
fun AddCardScreen(
    deckId: Int,
    viewModel: CardViewModel,
    onBack: () -> Unit
) {
    var front by remember { mutableStateOf("") }
    var back by remember { mutableStateOf("") }
    var savedCount by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDeep, Color(0xFF12122A))))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack,
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(BgCard)
                ) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) }
                Spacer(Modifier.width(12.dp))
                Text("Thêm thẻ mới", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }

            // Saved count badge
            if (savedCount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1A3A1A)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = ColorGood, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Đã lưu $savedCount thẻ", color = ColorGood, fontSize = 14.sp)
                }
                Spacer(Modifier.height(16.dp))
            }

            // Front card input
            Text("MẶT TRƯỚC", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp))
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(BgCard)
            ) {
                OutlinedTextField(
                    value = front, onValueChange = { front = it },
                    placeholder = { Text("Câu hỏi, từ vựng...", color = TextHint) },
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple, unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        cursorColor = AccentPurple
                    )
                )
            }

            Spacer(Modifier.height(16.dp))

            // Back card input
            Text("MẶT SAU", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp))
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(BgCard)
            ) {
                OutlinedTextField(
                    value = back, onValueChange = { back = it },
                    placeholder = { Text("Đáp án, nghĩa của từ...", color = TextHint) },
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentViolet, unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        cursorColor = AccentViolet
                    )
                )
            }

            Spacer(Modifier.height(24.dp))

            // Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        if (front.isNotBlank() && back.isNotBlank()) {
                            viewModel.addCard(deckId, front.trim(), back.trim())
                            front = ""; back = ""; savedCount++
                        }
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, AccentPurple)
                ) { Text("Lưu & Thêm tiếp", color = AccentPurple, fontWeight = FontWeight.SemiBold) }

                Button(
                    onClick = {
                        if (front.isNotBlank() && back.isNotBlank()) {
                            viewModel.addCard(deckId, front.trim(), back.trim())
                        }
                        onBack()
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                ) { Text("Lưu & Xong", fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}