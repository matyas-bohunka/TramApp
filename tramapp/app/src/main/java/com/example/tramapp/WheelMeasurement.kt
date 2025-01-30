package com.example.tramapp

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "wheel_measurements",
    foreignKeys = [ForeignKey(
        entity = Measurement::class,
        parentColumns = ["id"],
        childColumns = ["measurementId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class WheelMeasurement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val measurementId: Long,
    val wheelNumber: Int,
    val value1: String?,
    val value2: String?,
    val value3: String?,
    val average: String?
)