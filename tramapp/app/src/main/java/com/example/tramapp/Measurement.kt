package com.example.tramapp

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "measurements",
    foreignKeys = [ForeignKey(
        entity = TramType::class,
        parentColumns = ["id"],
        childColumns = ["tramTypeId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Measurement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tramTypeId: Long,
    val trackNumber: String,
    val date: Long // Store date as a timestamp
)