package com.rudrafuturetech.voicereminder

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE completed = 0 ORDER BY triggerAt ASC")
    fun active(): Flow<List<Reminder>>

    @Insert
    suspend fun insert(reminder: Reminder): Long

    @Update
    suspend fun update(reminder: Reminder)

    @Delete
    suspend fun delete(reminder: Reminder)

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun find(id: Long): Reminder?
}
