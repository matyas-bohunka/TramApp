package com.example.tramapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WheelMeasurementDao {
    @Insert
    suspend fun insert(wheelMeasurement: WheelMeasurement)

    @Insert
    suspend fun insertAll(wheelMeasurements: List<WheelMeasurement>)

    @Update
    suspend fun update(wheelMeasurement: WheelMeasurement)

    @Query("SELECT * FROM wheel_measurements WHERE measurementId = :measurementId")
    fun getByMeasurementId(measurementId: Long): Flow<List<WheelMeasurement>>

    @Query("SELECT * FROM wheel_measurements WHERE id = :id")
    suspend fun getById(id: Long): WheelMeasurement?

    @Delete
    suspend fun delete(wheelMeasurement: WheelMeasurement)
}