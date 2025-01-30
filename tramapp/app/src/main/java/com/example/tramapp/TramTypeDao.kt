package com.example.tramapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface TramTypeDao {
    @Insert
    suspend fun insert(tramType: TramType)

    @Query("SELECT * FROM tram_types")
    fun getAll(): Flow<List<TramType>>

    @Query("SELECT * FROM tram_types WHERE id = :id")
    suspend fun getById(id: Long): TramType?

    @Delete
    suspend fun delete(tramType: TramType)
}