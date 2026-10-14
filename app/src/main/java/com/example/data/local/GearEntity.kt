package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gear_inventory")
data class GearEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val brand: String,
    val category: String, // "Camera Body", "Lens", "Flash / Strobe", "Light Meter", "Filter", "Trigger", "Tripod"
    val modelNumber: String = "",
    val serialNumber: String = "",
    val condition: String = "Excellent", // "Mint", "Excellent", "Good", "Fair"
    val weightGrams: Int = 0,
    val inBag: Boolean = true,
    val notes: String = ""
)
