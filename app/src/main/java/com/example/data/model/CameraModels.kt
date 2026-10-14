package com.example.data.model

import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

enum class SensorFormat(val displayName: String, val cropFactor: Float) {
    FULL_FRAME("Full Frame (35mm)", 1.0f),
    APS_C_SONY_NIKON("APS-C (Sony/Nikon/Fuji)", 1.5f),
    APS_C_CANON("APS-C (Canon)", 1.6f),
    MICRO_FOUR_THIRDS("Micro 4/3", 2.0f),
    MEDIUM_FORMAT("Medium Format (44x33)", 0.79f)
}

data class CameraModel(
    val brand: String,
    val name: String,
    val sensorFormat: SensorFormat,
    val baseIso: Int = 100,
    val maxIso: Int = 51200,
    val maxShutterSpeed: String = "1/8000",
    val hasIbis: Boolean = true
)

data class LensModel(
    val brand: String,
    val name: String,
    val minFocalLength: Int,
    val maxFocalLength: Int,
    val maxAperture: Float,
    val minAperture: Float = 22f,
    val hasOis: Boolean = false,
    val isPrime: Boolean = minFocalLength == maxFocalLength
) {
    val focalLengthDisplay: String
        get() = if (isPrime) "${minFocalLength}mm" else "${minFocalLength}-${maxFocalLength}mm"
}

enum class ExposurePriority {
    APERTURE_PRIORITY, // User fixes Aperture, calculates Shutter Speed
    SHUTTER_PRIORITY,  // User fixes Shutter Speed, calculates Aperture
    ISO_PRIORITY,      // User fixes Aperture & Shutter, calculates ISO
    MANUAL_LOCK        // Shows EV difference / balance
}

object ExposureStandards {
    val STANDARD_APERTURES = listOf(
        1.0f, 1.2f, 1.4f, 1.8f, 2.0f, 2.8f, 3.5f, 4.0f, 5.6f, 8.0f, 11f, 16f, 22f, 32f
    )

    val STANDARD_SHUTTER_SPEEDS = listOf(
        "30s", "15s", "8s", "4s", "2s", "1s", "1/2", "1/4", "1/8", "1/15", "1/30",
        "1/60", "1/125", "1/250", "1/500", "1/1000", "1/2000", "1/4000", "1/8000"
    )

    val STANDARD_ISOS = listOf(
        50, 100, 200, 400, 800, 1600, 3200, 6400, 12800, 25600, 51200
    )

    fun shutterSpeedStringToSeconds(speedStr: String): Double {
        val trimmed = speedStr.trim().lowercase()
        return when {
            trimmed.endsWith("s") -> trimmed.removeSuffix("s").toDoubleOrNull() ?: 1.0
            trimmed.contains("/") -> {
                val parts = trimmed.split("/")
                val num = parts.getOrNull(0)?.toDoubleOrNull() ?: 1.0
                val den = parts.getOrNull(1)?.toDoubleOrNull() ?: 60.0
                if (den > 0) num / den else 1.0 / 60.0
            }
            else -> trimmed.toDoubleOrNull() ?: (1.0 / 60.0)
        }
    }

    fun secondsToShutterSpeedString(seconds: Double): String {
        return when {
            seconds >= 1.0 -> {
                val secInt = seconds.roundToInt()
                "${secInt}s"
            }
            seconds <= 0 -> "1/8000"
            else -> {
                val reciprocal = (1.0 / seconds).roundToInt()
                // Snap to nearest standard shutter speed if close
                val closest = STANDARD_SHUTTER_SPEEDS
                    .filter { it.contains("/") }
                    .minByOrNull {
                        val sSec = shutterSpeedStringToSeconds(it)
                        kotlin.math.abs(sSec - seconds)
                    }
                closest ?: "1/$reciprocal"
            }
        }
    }

    /**
     * Calculate incident EV at ISO 100 from Lux
     * Standard ISO constant: C ≈ 250, EV100 = log2(Lux * 100 / 250) = log2(Lux / 2.5)
     */
    fun luxToEv100(lux: Float): Float {
        val safeLux = lux.coerceAtLeast(0.1f)
        return (log2(safeLux / 2.5)).toFloat()
    }

    /**
     * Calculate Lux from EV100
     */
    fun ev100ToLux(ev: Float): Float {
        return (2.5 * 2.0.pow(ev.toDouble())).toFloat()
    }

    /**
     * Effective EV at given ISO: EV_S = EV_100 + log2(ISO / 100)
     */
    fun evAtIso(ev100: Float, iso: Int): Float {
        val isoMultiplier = (iso.toFloat() / 100f).coerceAtLeast(0.1f)
        return ev100 + log2(isoMultiplier)
    }

    /**
     * Exposure equation: 2^EV = N^2 / t
     * Where N = f-number, t = time in seconds
     */
    fun calculateShutterSpeed(ev100: Float, iso: Int, aperture: Float, ndStops: Int = 0): String {
        val effectiveEv = evAtIso(ev100, iso) - ndStops
        val twoPowerEv = 2.0.pow(effectiveEv.toDouble())
        val seconds = (aperture.toDouble().pow(2)) / twoPowerEv
        return secondsToShutterSpeedString(seconds)
    }

    fun calculateAperture(ev100: Float, iso: Int, shutterSpeed: String, ndStops: Int = 0): Float {
        val effectiveEv = evAtIso(ev100, iso) - ndStops
        val twoPowerEv = 2.0.pow(effectiveEv.toDouble())
        val seconds = shutterSpeedStringToSeconds(shutterSpeed)
        val nSquared = twoPowerEv * seconds
        val rawAperture = sqrt(nSquared.coerceAtLeast(0.5)).toFloat()
        // Find closest standard aperture
        return STANDARD_APERTURES.minByOrNull { kotlin.math.abs(it - rawAperture) } ?: rawAperture
    }

    fun calculateIso(ev100: Float, aperture: Float, shutterSpeed: String, ndStops: Int = 0): Int {
        val seconds = shutterSpeedStringToSeconds(shutterSpeed)
        val targetEv = log2((aperture.toDouble().pow(2)) / seconds).toFloat()
        val deltaEv = targetEv - (ev100 - ndStops)
        val calculatedIso = (100f * 2.0.pow(deltaEv.toDouble())).roundToInt()
        return STANDARD_ISOS.minByOrNull { kotlin.math.abs(it - calculatedIso) } ?: calculatedIso.coerceIn(50, 51200)
    }

    /**
     * Reciprocal rule: 1 / (focal_length * crop_factor)
     * With stabilization compensation (typically 3-5 stops)
     */
    fun recommendedHandheldShutter(
        focalLengthMm: Int,
        sensorFormat: SensorFormat,
        hasStabilization: Boolean
    ): String {
        val effectiveFocal = focalLengthMm * sensorFormat.cropFactor
        var baseSeconds = 1.0 / effectiveFocal
        if (hasStabilization) {
            // Give 3 stops of stabilization advantage
            baseSeconds *= 8.0 // 2^3
        }
        return secondsToShutterSpeedString(baseSeconds)
    }
}
