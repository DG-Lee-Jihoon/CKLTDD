package com.example.flashcardapp.ui.theme

import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashcardapp.data.Card
import com.example.flashcardapp.viewmodel.CardViewModel
import java.util.Locale

@Composable
fun StudyScreen(
    deckId: Int,
    viewModel: CardViewModel,
    onFinish: () -> Unit
) {
    val queue by viewModel.studyQueue.collectAsState()
    val currentCard = queue.firstOrNull()

    // Text-to-Speech setup
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.ENGLISH
            }
        }
    }
    DisposableEffect(Unit) { onDispose { tts?.shutdown() } }

    LaunchedEffect(deckId) { viewModel.loadStudyQueue(deckId) }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDeep, Color(0xFF12122A))))
    ) {
        if (currentCard == null) {
            // Hoàn thành
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🎉", fontSize = 72.sp)
                Spacer(Modifier.height(16.dp))
                Text("Hoàn thành!", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Text("Bạn đã ôn xong hết thẻ hôm nay", color = TextSecondary, fontSize = 15.sp)
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = onFinish,
                    modifier = Modifier.clip(RoundedCornerShape(16.dp)).height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                ) { Text("  Về trang chính  ", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onFinish,
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(BgCard)
                    ) { Icon(Icons.Default.Close, null, tint = TextPrimary) }
                    Spacer(Modifier.weight(1f))
                    // Progress
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(BgCard)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("${queue.size} thẻ còn lại", color = TextSecondary, fontSize = 13.sp)
                    }
                }

                StudyCardContent(
                    card = currentCard,
                    tts = tts,
                    onAnswer = { quality -> viewModel.answerCard(currentCard, quality) }
                )
            }
        }
    }
}

@Composable
private fun StudyCardContent(
    card: Card,
    tts: TextToSpeech?,
    onAnswer: (ReviewQuality) -> Unit
) {
    var isFlipped by remember(card.id) { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "flip"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        // Flip hint
        Text(
            if (!isFlipped) "Nhấn thẻ để xem đáp án" else "Chọn mức độ ghi nhớ",
            color = TextSecondary, fontSize = 13.sp
        )
        Spacer(Modifier.height(16.dp))

        // 3D Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .graphicsLayer { rotationY = rotation; cameraDistance = 14f * density }
                .clickable { isFlipped = !isFlipped },
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                // Front
                Box(
                    modifier = Modifier.fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF1A1A3A), Color(0xFF252550)))),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text("CÂU HỎI", color = AccentPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Spacer(Modifier.height(20.dp))
                        Text(card.front, color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(20.dp))
                        // TTS button
                        IconButton(
                            onClick = { tts?.speak(card.front, TextToSpeech.QUEUE_FLUSH, null, null) },
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF2A2A5A))
                        ) { Icon(Icons.Default.VolumeUp, null, tint = AccentPurple) }
                    }
                }
            } else {
                // Back (flipped)
                Box(
                    modifier = Modifier.fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF1A2A3A), Color(0xFF1F3050))))
                        .graphicsLayer { rotationY = 180f },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text("ĐÁP ÁN", color = ColorGood, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Spacer(Modifier.height(20.dp))
                        Text(card.back, color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(20.dp))
                        IconButton(
                            onClick = { tts?.speak(card.back, TextToSpeech.QUEUE_FLUSH, null, null) },
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF1A3A2A))
                        ) { Icon(Icons.Default.VolumeUp, null, tint = ColorGood) }
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Answer buttons (chỉ hiện sau khi lật)
        if (isFlipped) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                data class BtnData(val q: ReviewQuality, val color: Color, val emoji: String)
                val btns = listOf(
                    BtnData(ReviewQuality.Again, ColorAgain, "😵"),
                    BtnData(ReviewQuality.Hard,  ColorHard,  "😓"),
                    BtnData(ReviewQuality.Good,  ColorGood,  "😊"),
                    BtnData(ReviewQuality.Easy,  ColorEasy,  "😄")
                )
                btns.forEach { btn ->
                    Button(
                        onClick = { onAnswer(btn.q) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = btn.color.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, btn.color),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text(
                            btn.q.label,
                            color = btn.color,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        } else {
            Spacer(Modifier.height(96.dp))
        }
    }
}