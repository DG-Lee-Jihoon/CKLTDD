package com.example.flashcardapp.ui.theme

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.flashcardapp.worker.ReminderPrefs
import com.example.flashcardapp.worker.ReminderScheduler
import com.example.flashcardapp.worker.ReminderWorker

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var reminderEnabled by remember { mutableStateOf(ReminderPrefs.isEnabled(context)) }
    var selectedHour    by remember { mutableStateOf(ReminderPrefs.getHour(context)) }
    var selectedMinute  by remember { mutableStateOf(ReminderPrefs.getMinute(context)) }
    var showSaved       by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Header ────────────────────────────────────
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
                    Icon(Icons.Default.ArrowBack, contentDescription = "Quay lai", tint = Color.White)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Cai dat",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Tuy chinh lich hoc cua ban",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // ── NÚT TEST (xóa sau khi test xong) ─────
            Button(
                onClick = {
                    android.util.Log.d("ReminderTest", "Bam nut test!")
                    val request = OneTimeWorkRequestBuilder<ReminderWorker>().build()
                    WorkManager.getInstance(context).enqueue(request)
                    android.util.Log.d("ReminderTest", "Worker da duoc enqueue!")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("TEST THONG BAO NGAY", fontWeight = FontWeight.Bold)
            }

            // ── Section nhắc nhở ──────────────────────
            Text(
                "Nhac nho hoc tap",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            // Toggle bật/tắt
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Bat nhac nho",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Nhan thong bao khi co the can on",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { enabled ->
                            reminderEnabled = enabled
                            ReminderPrefs.setEnabled(context, enabled)
                            if (enabled) {
                                ReminderScheduler.schedule(context, selectedHour, selectedMinute)
                            } else {
                                ReminderScheduler.cancel(context)
                            }
                        }
                    )
                }
            }

            // Chọn giờ (chỉ hiện khi bật nhắc nhở)
            if (reminderEnabled) {
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showTimePicker(context, selectedHour, selectedMinute) { h, m ->
                                    selectedHour = h
                                    selectedMinute = m
                                    ReminderPrefs.setTime(context, h, m)
                                    ReminderScheduler.schedule(context, h, m)
                                    showSaved = true
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Gio nhac nho",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Nhan de thay doi gio",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "%02d:%02d".format(selectedHour, selectedMinute),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Quick presets
                Text(
                    "Chon nhanh",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple("Sang",  7,  0),
                        Triple("Trua",  12, 0),
                        Triple("Chieu", 17, 0),
                        Triple("Toi",   20, 0)
                    ).forEach { (label, hour, minute) ->
                        val isSelected = selectedHour == hour && selectedMinute == minute
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedHour = hour
                                selectedMinute = minute
                                ReminderPrefs.setTime(context, hour, minute)
                                ReminderScheduler.schedule(context, hour, minute)
                                showSaved = true
                            },
                            label = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(label, fontWeight = FontWeight.Medium)
                                    Text(
                                        "%02d:%02d".format(hour, minute),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── Section thông tin ─────────────────────
            Spacer(Modifier.height(4.dp))
            Text(
                "Thong tin",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsInfoRow(
                        icon  = Icons.Outlined.Psychology,
                        label = "Thuat toan",
                        value = "SuperMemo SM-2"
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    SettingsInfoRow(
                        icon  = Icons.Outlined.Repeat,
                        label = "Lap lai toi thieu",
                        value = "1 ngay"
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    SettingsInfoRow(
                        icon  = Icons.Outlined.TrendingUp,
                        label = "He so kho mac dinh",
                        value = "2.5"
                    )
                }
            }

            // ── Thông báo đã lưu ──────────────────────
            if (showSaved) {
                LaunchedEffect(showSaved) {
                    kotlinx.coroutines.delay(2000)
                    showSaved = false
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ColorGood.copy(alpha = 0.15f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ColorGood,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Da luu lich nhac nho luc %02d:%02d".format(selectedHour, selectedMinute),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        content()
    }
}

@Composable
fun SettingsInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

fun showTimePicker(
    context: Context,
    currentHour: Int,
    currentMinute: Int,
    onTimeSelected: (Int, Int) -> Unit
) {
    TimePickerDialog(
        context,
        { _, hour, minute -> onTimeSelected(hour, minute) },
        currentHour,
        currentMinute,
        true
    ).show()
}