package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exposure_logs")
data class ExposureLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val ev100: Float,
    val lux: Float,
    val iso: Int,
    val shutterSpeed: String,
    val aperture: Float,
    val cameraBrand: String,
    val cameraModel: String,
    val lensName: String,
    val focalLengthMm: Int,
    val lightingSetupNotes: String = "",
    val isSyncedToCloud: Boolean = true
)
