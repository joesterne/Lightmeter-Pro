package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.LensModel

@Entity(tableName = "lenses")
data class LensEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val brand: String,
    val lensName: String,
    val minFocalLength: Int = 50,
    val maxFocalLength: Int = 50,
    val maxAperture: Float = 1.8f,
    val minAperture: Float = 22f,
    val hasOis: Boolean = false,
    val filterThreadMm: Int = 77,
    val serialNumber: String = "",
    val weightGrams: Int = 450,
    val inBag: Boolean = true,
    val notes: String = ""
) {
    val focalLengthDisplay: String
        get() = if (minFocalLength == maxFocalLength) "${minFocalLength}mm" else "${minFocalLength}-${maxFocalLength}mm"

    fun toLensModel(): LensModel {
        return LensModel(
            brand = brand,
            name = lensName,
            minFocalLength = minFocalLength,
            maxFocalLength = maxFocalLength,
            maxAperture = maxAperture,
            minAperture = minAperture,
            hasOis = hasOis,
            isPrime = minFocalLength == maxFocalLength
        )
    }
}
