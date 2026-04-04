package com.example.flashcardapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.example.flashcardapp.ui.theme.FlashcardAppTheme
import com.example.flashcardapp.util.StudyReminderWorker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Lên lịch nhắc nhở học hàng ngày lúc 8h sáng
        StudyReminderWorker.schedule(this)

        setContent {
            FlashcardAppTheme {
                NavGraph()
            }
        }
    }
}