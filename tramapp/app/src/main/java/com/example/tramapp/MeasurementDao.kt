package com.example.tramapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Insert
    suspend fun insert(measurement: Measurement): Long

    @Query("SELECT * FROM measurements ORDER BY date DESC")
    fun getAll(): Flow<List<Measurement>>

    @Query("SELECT * FROM measurements WHERE id = :id")
    suspend fun getById(id: Long): Measurement?

    @Delete
    suspend fun delete(measurement: Measurement)

    @Transaction
    @Query("SELECT * FROM measurements WHERE id = :measurementId")
    suspend fun getMeasurementWithWheelMeasurements(measurementId: Long): MeasurementWithWheelMeasurements?
}