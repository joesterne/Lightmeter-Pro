package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.CameraModel
import com.example.data.model.SensorFormat

@Entity(tableName = "camera_bodies")
data class CameraBodyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val brand: String,
    val modelName: String,
    val sensorFormat: String = "Full Frame (35mm)",
    val cropFactor: Float = 1.0f,
    val baseIso: Int = 100,
    val maxIso: Int = 51200,
    val maxShutterSpeed: String = "1/8000",
    val hasIbis: Boolean = true,
    val serialNumber: String = "",
    val weightGrams: Int = 650,
    val inBag: Boolean = true,
    val notes: String = ""
) {
    fun toCameraModel(): CameraModel {
        val format = when {
            sensorFormat.contains("APS-C (Canon)", ignoreCase = true) -> SensorFormat.APS_C_CANON
            sensorFormat.contains("APS-C", ignoreCase = true) -> SensorFormat.APS_C_SONY_NIKON
            sensorFormat.contains("Micro 4/3", ignoreCase = true) || sensorFormat.contains("MFT", ignoreCase = true) -> SensorFormat.MICRO_FOUR_THIRDS
            sensorFormat.contains("Medium Format", ignoreCase = true) -> SensorFormat.MEDIUM_FORMAT
            else -> SensorFormat.FULL_FRAME
        }
        return CameraModel(
            brand = brand,
            name = modelName,
            sensorFormat = format,
            baseIso = baseIso,
            maxIso = maxIso,
            maxShutterSpeed = maxShutterSpeed,
            hasIbis = hasIbis
        )
    }
}
