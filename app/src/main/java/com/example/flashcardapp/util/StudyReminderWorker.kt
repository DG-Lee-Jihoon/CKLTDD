package com.example.flashcardapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.flashcardapp.data.AppDatabase
import java.util.concurrent.TimeUnit

class StudyReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(context)
        val now = System.currentTimeMillis()

        // Đếm tổng thẻ đến hạn toàn bộ deck
        val allCards = db.cardDao().getAllDueCards(now)
        if (allCards.isEmpty()) return Result.success()

        showNotification(allCards.size)
        return Result.success()
    }

    private fun showNotification(dueCount: Int) {
        val channelId = "study_reminder"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Nhắc nhở học", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⏰ Đến giờ ôn thẻ rồi!")
            .setContentText("Bạn có $dueCount thẻ đang chờ được ôn tập hôm nay.")
            .setAutoCancel(true)
            .build()

        manager.notify(1001, notification)
    }

    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<StudyReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(calculateDelayUntil8AM(), TimeUnit.MILLISECONDS)
                .setConstraints(Constraints.Builder().build())
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "study_reminder",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        private fun calculateDelayUntil8AM(): Long {
            val now = System.currentTimeMillis()
            val cal = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 8)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                if (timeInMillis <= now) add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
            return cal.timeInMillis - now
        }
    }
}
