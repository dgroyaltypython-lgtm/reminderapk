package com.rudrafuturetech.voicereminder

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val triggerAt: Long,
    val repeat: String = "Never",
    val enabled: Boolean = true,
    val completed: Boolean = false,
    val voiceEnabled: Boolean = true
)
