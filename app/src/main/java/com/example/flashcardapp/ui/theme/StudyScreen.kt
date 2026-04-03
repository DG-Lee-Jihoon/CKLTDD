package com.example.flashcardapp.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.flashcardapp.data.Card
import com.example.flashcardapp.ui.theme.ReviewQuality
import com.example.flashcardapp.viewmodel.CardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    deckId: Int,
    viewModel: CardViewModel,
    onFinish: () -> Unit
) {
    val queue by viewModel.studyQueue.collectAsState()
    val currentCard = queue.firstOrNull()

    LaunchedEffect(deckId) {
        viewModel.loadStudyQueue(deckId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đang học (${queue.size} thẻ còn lại)") },
                navigationIcon = {
                    IconButton(onClick = onFinish) { Text("✕") }
                }
            )
        }
    ) { padding ->
        if (currentCard == null) {
            // Xong hết rồi
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎉", style = MaterialTheme.typography.displayMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Bạn đã ôn xong hết thẻ hôm nay!", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onFinish) { Text("Về trang chính") }
                }
            }
        } else {
            StudyCardContent(
                card = currentCard,
                modifier = Modifier.padding(padding),
                onAnswer = { quality ->
                    viewModel.answerCard(currentCard, quality)
                }
            )
        }
    }
}

@Composable
private fun StudyCardContent(
    card: Card,
    modifier: Modifier = Modifier,
    onAnswer: (ReviewQuality) -> Unit
) {
    var isFlipped by remember(card.id) { mutableStateOf(false) }

    // Animation lật thẻ 3D
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "cardFlip"
    )

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        // Thẻ lật 3D
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }
                .clickable { isFlipped = !isFlipped },
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                // Mặt trước
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = CardFront)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                            Text("Câu hỏi", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                card.front,
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(16.dp))
                            Text("Nhấn để lật thẻ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                // Mặt sau (lật ngược lại để text đọc được)
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f },
                    colors = CardDefaults.cardColors(containerColor = CardBack)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                            Text("Đáp án", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                card.back,
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Nút đánh giá (chỉ hiện sau khi lật)
        if (isFlipped) {
            Text("Bạn nhớ được mức nào?", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReviewQuality.entries.forEach { quality ->
                    val color = when (quality) {
                        ReviewQuality.Again -> ColorAgain
                        ReviewQuality.Hard  -> ColorHard
                        ReviewQuality.Good  -> ColorGood
                        ReviewQuality.Easy  -> ColorEasy
                    }
                    Button(
                        onClick = { onAnswer(quality) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = color)
                    ) {
                        Text(quality.label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}