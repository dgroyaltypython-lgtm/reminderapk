package com.rudrafuturetech.voicereminder

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private val Bg = Color(0xFF070B17)
private val Card = Color(0xFF11182B)
private val Purple = Color(0xFF7C5CFC)
private val Cyan = Color(0xFF00C2FF)
private val TextMain = Color(0xFFF8FAFC)
private val TextMuted = Color(0xFF94A3B8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ReminderScheduler.createChannel(this)
        setContent { RudraApp() }
    }
}

@Composable
fun RudraApp() {
    val context = LocalContext.current
    val dao = remember { AppDatabase.get(context).reminderDao() }
    val reminders by dao.active().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var showAdd by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {}

    LaunchedEffect(Unit) {
        val permissions = buildList {
            if (android.os.Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
            add(Manifest.permission.RECORD_AUDIO)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Bg,
            surface = Card,
            primary = Purple,
            secondary = Cyan,
            onBackground = TextMain,
            onSurface = TextMain
        )
    ) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0xFF090D1D), Bg))
            )
        ) {
            Column(Modifier.fillMaxSize().padding(20.dp)) {
                Spacer(Modifier.height(18.dp))
                Text("Good evening", color = TextMuted, fontSize = 16.sp)
                Text("Ready when you are.", color = TextMain, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(20.dp))

                GlassCard {
                    Text("VOICE ASSISTANT", color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Tap the microphone and speak your reminder.",
                        color = TextMain, fontSize = 18.sp
                    )
                    Spacer(Modifier.height(20.dp))
                    Box(
                        Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        FloatingActionButton(
                            onClick = { showAdd = true },
                            modifier = Modifier.size(92.dp).shadow(20.dp, CircleShape),
                            containerColor = Purple,
                            contentColor = Color.White
                        ) {
                            Icon(Icons.Default.Mic, "Speak", Modifier.size(42.dp))
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Text(
                        "Try: “Remind me tomorrow at 9 AM to check inventory.”",
                        color = TextMuted, fontSize = 13.sp
                    )
                }

                Spacer(Modifier.height(22.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Upcoming reminders", color = TextMain, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("${reminders.size}", color = Cyan, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(reminders, key = { it.id }) { reminder ->
                        ReminderCard(
                            reminder,
                            onDone = {
                                scope.launch {
                                    dao.update(reminder.copy(completed = true))
                                    ReminderScheduler.cancel(context, reminder.id)
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    dao.delete(reminder)
                                    ReminderScheduler.cancel(context, reminder.id)
                                }
                            }
                        )
                    }
                    if (reminders.isEmpty()) {
                        item {
                            GlassCard {
                                Text("No reminders yet.", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(6.dp))
                                Text("Create one using the microphone button.", color = TextMuted)
                            }
                        }
                    }
                }
            }

            if (showAdd) {
                AddReminderDialog(
                    onDismiss = { showAdd = false },
                    onSave = { title, time ->
                        scope.launch {
                            val id = dao.insert(Reminder(title = title, triggerAt = time))
                            val saved = dao.find(id)
                            if (saved != null) ReminderScheduler.schedule(context, saved)
                            showAdd = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun GlassCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Card.copy(alpha = 0.92f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(Modifier.padding(20.dp), content = content)
    }
}

@Composable
fun ReminderCard(reminder: Reminder, onDone: () -> Unit, onDelete: () -> Unit) {
    val formatter = remember { SimpleDateFormat("EEE, dd MMM • hh:mm a", Locale.getDefault()) }
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).background(
                    Brush.linearGradient(listOf(Purple, Cyan)), CircleShape
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Notifications, null, tint = Color.White)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(reminder.title, color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                Text(formatter.format(Date(reminder.triggerAt)), color = TextMuted, fontSize = 13.sp)
            }
            IconButton(onClick = onDone) { Icon(Icons.Default.Check, "Done", tint = Color(0xFF22C55E)) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.DeleteOutline, "Delete", tint = Color(0xFFEF4444)) }
        }
    }
}

@Composable
fun AddReminderDialog(onDismiss: () -> Unit, onSave: (String, Long) -> Unit) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf(System.currentTimeMillis() + 3600000) }
    val cal = remember { Calendar.getInstance().apply { timeInMillis = selectedTime } }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Card,
        title = { Text("Create reminder", color = TextMain, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("What should I remind you about?") },
                    singleLine = false
                )
                OutlinedButton(
                    onClick = {
                        val now = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                TimePickerDialog(
                                    context,
                                    { _, h, min ->
                                        cal.set(y, m, d, h, min, 0)
                                        selectedTime = cal.timeInMillis
                                    },
                                    now.get(Calendar.HOUR_OF_DAY),
                                    now.get(Calendar.MINUTE),
                                    false
                                ).show()
                            },
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(SimpleDateFormat("EEE, dd MMM • hh:mm a", Locale.getDefault()).format(Date(selectedTime)))
                }
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank() && selectedTime > System.currentTimeMillis(),
                onClick = { onSave(title.trim(), selectedTime) }
            ) { Text("Schedule") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
