package com.example.flashcardapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.flashcardapp.viewmodel.CardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    deckId: Long,
    viewModel: CardViewModel,
    onBack: () -> Unit
) {
    var front by remember { mutableStateOf("") }
    var back by remember { mutableStateOf("") }
    var savedCount by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                .padding(20.dp)
        ) {
            Column {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Quay lai",
                        tint = Color.White
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Them the moi",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                if (savedCount > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Da luu $savedCount the",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            Text(
                "Mat truoc",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = front,
                onValueChange = { front = it },
                placeholder = { Text("Nhap cau hoi hoac tu can hoc...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Text(
                "Mat sau",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = back,
                onValueChange = { back = it },
                placeholder = { Text("Nhap dap an hoac dinh nghia...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (front.isNotBlank() && back.isNotBlank()) {
                            viewModel.addCard(deckId, front.trim(), back.trim())
                            savedCount++
                            front = ""
                            back = ""
                        }
                    },
                    enabled = front.isNotBlank() && back.isNotBlank(),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Luu + tiep", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (front.isNotBlank() && back.isNotBlank()) {
                            viewModel.addCard(deckId, front.trim(), back.trim())
                        }
                        onBack()
                    },
                    enabled = front.isNotBlank() && back.isNotBlank(),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Luu + xong", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}