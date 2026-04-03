package com.example.flashcardapp.ui.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.flashcardapp.viewmodel.CardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CardViewModel,
    onDeckClick: (Int) -> Unit
) {
    val decks by viewModel.allDecks.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var newDeckName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Flashcard App") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Thêm bộ thẻ")
            }
        }
    ) { padding ->
        if (decks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Chưa có bộ thẻ nào. Nhấn + để tạo mới!")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(decks) { deck ->
                    val dueCount by viewModel.getDueCount(deck.id)
                        .collectAsStateWithLifecycle(initialValue = 0)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDeckClick(deck.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(deck.name, style = MaterialTheme.typography.titleMedium)
                                if (deck.description.isNotBlank())
                                    Text(deck.description, style = MaterialTheme.typography.bodySmall)
                            }
                            if (dueCount > 0) {
                                Badge { Text("$dueCount") }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Tạo bộ thẻ mới") },
                text = {
                    OutlinedTextField(
                        value = newDeckName,
                        onValueChange = { newDeckName = it },
                        label = { Text("Tên bộ thẻ") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newDeckName.isNotBlank()) {
                            viewModel.addDeck(newDeckName.trim())
                            newDeckName = ""
                            showDialog = false
                        }
                    }) { Text("Tạo") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Hủy") }
                }
            )
        }
    }
}