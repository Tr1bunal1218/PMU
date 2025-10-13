package com.example.androidgamekt.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.androidgamekt.data.entity.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users ORDER BY fullName ASC")
    suspend fun getAll(): List<UserEntity>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE fullName = :name LIMIT 1")
    suspend fun getByName(name: String): UserEntity?

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteById(id: Long)
}


