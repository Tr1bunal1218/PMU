package com.example.androidgamekt.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.androidgamekt.data.entity.RecordEntity

@Dao
interface RecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: RecordEntity): Long

    @Query("""
        SELECT records.id, records.userId, records.score, records.difficulty, records.timestamp
        FROM records
        ORDER BY score DESC, timestamp ASC
    """)
    suspend fun getAllOrdered(): List<RecordEntity>

    @Query("DELETE FROM records")
    suspend fun clear()
}


