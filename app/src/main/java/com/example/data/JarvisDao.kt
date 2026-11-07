package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JarvisDao {
    @Query("SELECT * FROM command_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentLogs(): Flow<List<CommandLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CommandLogEntity): Long

    @Query("DELETE FROM command_logs")
    suspend fun clearLogs()

    @Query("SELECT * FROM jarvis_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<JarvisNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: JarvisNoteEntity): Long

    @Delete
    suspend fun deleteNote(note: JarvisNoteEntity)
}
