package com.example.tramapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tram_types")
data class TramType(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val numAxles: Int
)