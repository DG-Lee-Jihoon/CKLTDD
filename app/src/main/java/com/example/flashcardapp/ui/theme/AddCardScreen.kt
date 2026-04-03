package com.example.flashcardapp.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.flashcardapp.viewmodel.CardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    deckId: Int,
    viewModel: CardViewModel,
    onBack: () -> Unit
) {
    var front by remember { mutableStateOf("") }
    var back by remember { mutableStateOf("") }
    var savedCount by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thêm thẻ mới") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (savedCount > 0) {
                Text(
                    "✓ Đã lưu $savedCount thẻ",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            OutlinedTextField(
                value = front,
                onValueChange = { front = it },
                label = { Text("Mặt trước (câu hỏi)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            OutlinedTextField(
                value = back,
                onValueChange = { back = it },
                label = { Text("Mặt sau (đáp án)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Lưu và thêm tiếp
                OutlinedButton(
                    onClick = {
                        if (front.isNotBlank() && back.isNotBlank()) {
                            viewModel.addCard(deckId, front.trim(), back.trim())
                            front = ""
                            back = ""
                            savedCount++
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Lưu & Thêm tiếp") }

                // Lưu và quay lại
                Button(
                    onClick = {
                        if (front.isNotBlank() && back.isNotBlank()) {
                            viewModel.addCard(deckId, front.trim(), back.trim())
                        }
                        onBack()
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Lưu & Xong") }
            }
        }
    }
}