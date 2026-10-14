package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shooting_presets")
data class PresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // "Landscape", "Portrait", "Astrophotography", "Studio", "Street", "Custom"
    val environmentDescription: String,
    val targetEv100: Float,
    val iso: Int,
    val shutterSpeed: String,
    val aperture: Float,
    val focalLengthMm: Int = 50,
    val ndFilterStops: Int = 0,
    val lightingRatioNotes: String = "",
    val isCustom: Boolean = true
)
