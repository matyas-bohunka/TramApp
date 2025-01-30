package com.example.tramapp

import androidx.room.Embedded
import androidx.room.Relation

data class MeasurementWithWheelMeasurements(
    @Embedded val measurement: Measurement,
    @Relation(
        parentColumn = "id",
        entityColumn = "measurementId"
    )
    val wheelMeasurements: List<WheelMeasurement>
)