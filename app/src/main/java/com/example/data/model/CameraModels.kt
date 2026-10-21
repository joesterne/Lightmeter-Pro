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

    /**
     * Dynamic Depth of Field (DoF), Hyperfocal Distance, and Diffraction limit
     */
    fun calculateDepthOfField(
        focalLengthMm: Int,
        aperture: Float,
        subjectDistanceMeters: Float,
        sensorFormat: SensorFormat
    ): DepthOfFieldCalculation {
        val cocMm = when (sensorFormat) {
            SensorFormat.FULL_FRAME -> 0.030f
            SensorFormat.APS_C_SONY_NIKON -> 0.020f
            SensorFormat.APS_C_CANON -> 0.019f
            SensorFormat.MICRO_FOUR_THIRDS -> 0.015f
            SensorFormat.MEDIUM_FORMAT -> 0.040f
        }
        val fM = (focalLengthMm.coerceAtLeast(10)) / 1000.0
        val cM = cocMm / 1000.0
        val sM = subjectDistanceMeters.coerceAtLeast(0.1f).toDouble()
        val n = aperture.coerceAtLeast(0.7f).toDouble()

        // Hyperfocal distance H = f^2 / (N * c) + f
        val hyperfocalM = ((fM * fM) / (n * cM)) + fM

        val nearLimitM: Double
        val farLimitM: Double
        val isInfiniteFar: Boolean

        if (sM >= hyperfocalM) {
            nearLimitM = (hyperfocalM * sM) / (hyperfocalM + (sM - fM))
            farLimitM = Double.POSITIVE_INFINITY
            isInfiniteFar = true
        } else {
            val denomNear = hyperfocalM + (sM - fM)
            val denomFar = hyperfocalM - (sM - fM)
            nearLimitM = if (denomNear > 0) (hyperfocalM * sM) / denomNear else sM * 0.5
            if (denomFar <= 0.001) {
                farLimitM = Double.POSITIVE_INFINITY
                isInfiniteFar = true
            } else {
                farLimitM = (hyperfocalM * sM) / denomFar
                isInfiniteFar = false
            }
        }

        val totalDofM = if (isInfiniteFar) Double.POSITIVE_INFINITY else (farLimitM - nearLimitM).coerceAtLeast(0.0)

        // Distribution
        val frontDof = (sM - nearLimitM).coerceAtLeast(0.0)
        val rearDof = if (isInfiniteFar) Double.POSITIVE_INFINITY else (farLimitM - sM).coerceAtLeast(0.0)
        val frontPercent = if (totalDofM.isInfinite() || totalDofM <= 0.001) 33f else ((frontDof / totalDofM) * 100f).toFloat().coerceIn(0f, 100f)
        val rearPercent = if (totalDofM.isInfinite()) 100f - frontPercent else ((rearDof / totalDofM) * 100f).toFloat().coerceIn(0f, 100f)

        // Airy disk diameter = 2.44 * lambda * N (microns), lambda ~ 0.55 um
        val airyDiskMicrons = (1.342 * n).toFloat()
        val diffractionThreshold = when (sensorFormat) {
            SensorFormat.MICRO_FOUR_THIRDS -> 8.0f
            SensorFormat.APS_C_CANON, SensorFormat.APS_C_SONY_NIKON -> 11.0f
            SensorFormat.FULL_FRAME -> 16.0f
            SensorFormat.MEDIUM_FORMAT -> 22.0f
        }
        val isDiffractionLimited = aperture >= diffractionThreshold

        return DepthOfFieldCalculation(
            focalLengthMm = focalLengthMm,
            aperture = aperture,
            subjectDistanceMeters = subjectDistanceMeters,
            sensorFormat = sensorFormat,
            circleOfConfusionMm = cocMm,
            hyperfocalMeters = hyperfocalM.toFloat(),
            nearLimitMeters = nearLimitM.toFloat(),
            farLimitMeters = if (isInfiniteFar) Float.POSITIVE_INFINITY else farLimitM.toFloat(),
            totalDofMeters = if (isInfiniteFar) Float.POSITIVE_INFINITY else totalDofM.toFloat(),
            isFarLimitInfinite = isInfiniteFar,
            frontPercent = frontPercent,
            rearPercent = rearPercent,
            airyDiskMicrons = airyDiskMicrons,
            isDiffractionLimited = isDiffractionLimited
        )
    }

    /**
     * Dynamic Exposure Balance (EV Delta) between current settings and scene EV.
     * Tells whether the camera is underexposed, perfectly centered, or overexposed.
     */
    fun calculateExposureBalance(
        sceneEv100: Float,
        shutterSpeed: String,
        aperture: Float,
        iso: Int,
        ndStops: Int = 0
    ): ExposureBalanceCalculation {
        val seconds = shutterSpeedStringToSeconds(shutterSpeed)
        val nSquared = aperture.toDouble().pow(2)
        val evFromTriangle = log2(nSquared / seconds.coerceAtLeast(0.00001)).toFloat()
        val isoCompensation = log2((iso.toFloat() / 100f).coerceAtLeast(0.01f))
        val currentSettingsEv100 = evFromTriangle - isoCompensation + ndStops

        // Delta EV: settings vs scene.
        // If settings have higher EV requirement than scene produces => underexposed (negative)
        // If settings have lower EV requirement than scene produces => overexposed (positive)
        val deltaEv = sceneEv100 - currentSettingsEv100

        val zoneName = when {
            deltaEv <= -3.5f -> "Zone 0 / I: Pure Solid Black (No Texture)"
            deltaEv in -3.5f..-2.5f -> "Zone II: Deep Shadows (Threshold of Texture)"
            deltaEv in -2.5f..-1.5f -> "Zone III: Textured Dark Shadows (Dark Fabrics/Foliage)"
            deltaEv in -1.5f..-0.5f -> "Zone IV: Dark Tones (Dark Foliage / Deep Skin)"
            deltaEv in -0.5f..0.5f -> "Zone V: 18% Middle Gray (Standard Neutral Balance)"
            deltaEv in 0.5f..1.5f -> "Zone VI: Average Light Skin Tones / Light Stonework"
            deltaEv in 1.5f..2.5f -> "Zone VII: Textured Highlights (Light Concrete / Sand)"
            deltaEv in 2.5f..3.5f -> "Zone VIII: Subtle Highlight Texture (White Paper/Snow)"
            else -> "Zone IX / X: Specular Highlights / Pure Blown White"
        }

        val statusText = when {
            kotlin.math.abs(deltaEv) <= 0.2f -> "0.0 EV Balanced (18% Gray)"
            deltaEv > 0 -> "+${String.format(java.util.Locale.US, "%.1f", deltaEv)} EV Overexposed"
            else -> "${String.format(java.util.Locale.US, "%.1f", deltaEv)} EV Underexposed"
        }

        return ExposureBalanceCalculation(
            sceneEv100 = sceneEv100,
            settingsEv100 = currentSettingsEv100,
            deltaEv = deltaEv,
            zoneSystemName = zoneName,
            statusText = statusText,
            isBalanced = kotlin.math.abs(deltaEv) <= 0.3f
        )
    }

    /**
     * Dynamic Astrophotography Shutter Speed (500 Rule & NPF Rule)
     */
    fun calculateAstroStarTrailing(
        focalLengthMm: Int,
        aperture: Float,
        sensorFormat: SensorFormat
    ): AstroStarTrailCalculation {
        val effFocal = (focalLengthMm * sensorFormat.cropFactor).coerceAtLeast(10f)
        val rule500Sec = 500.0 / effFocal.toDouble()

        // NPF rule formula: (35 * N + 30 * pixelPitch) / effFocal
        // Typical modern mirrorless pixel pitch ~ 4.0 microns
        val pixelPitchMicrons = 4.0
        val npfSec = ((35.0 * aperture.toDouble()) + (30.0 * pixelPitchMicrons)) / effFocal.toDouble()

        return AstroStarTrailCalculation(
            focalLengthMm = focalLengthMm,
            aperture = aperture,
            sensorFormat = sensorFormat,
            rule500Seconds = rule500Sec,
            npfRuleSeconds = npfSec,
            rule500Display = formatAstroSeconds(rule500Sec),
            npfRuleDisplay = formatAstroSeconds(npfSec)
        )
    }

    private fun formatAstroSeconds(sec: Double): String {
        return if (sec >= 1.0) {
            "${String.format(java.util.Locale.US, "%.1f", sec)}s"
        } else {
            val rec = (1.0 / sec).roundToInt()
            "1/${rec}s"
        }
    }

    /**
     * Dynamic ND Filter Exposure Multiplier Table
     */
    fun calculateNdLongExposures(baseShutterSpeed: String): List<NdLongExposureOption> {
        val baseSec = shutterSpeedStringToSeconds(baseShutterSpeed)
        val filters = listOf(
            Triple(1, "ND2 (+1 Stop)", 1),
            Triple(3, "ND8 (+3 Stops)", 3),
            Triple(6, "ND64 (+6 Stops)", 6),
            Triple(10, "ND1000 (+10 Stops)", 10),
            Triple(15, "ND32000 (+15 Stops)", 15)
        )
        return filters.map { (stops, name, _) ->
            val extendedSec = baseSec * 2.0.pow(stops.toDouble())
            val display = if (extendedSec >= 1.0) {
                "${(extendedSec * 10).roundToInt() / 10f}s"
            } else {
                secondsToShutterSpeedString(extendedSec)
            }
            NdLongExposureOption(
                stops = stops,
                name = name,
                resultingSeconds = extendedSec,
                displayString = display,
                isBulbRequired = extendedSec >= 30.0
            )
        }
    }

    /**
     * Dynamic Motion Kinetic Interpretation
     */
    fun calculateMotionBlurEffect(shutterSpeed: String): MotionBlurEffect {
        val sec = shutterSpeedStringToSeconds(shutterSpeed)
        return when {
            sec <= 1.0 / 4000.0 -> MotionBlurEffect(
                description = "Ultra High-Speed: Freezes water drops, insect wings, explosive splashes.",
                badgeText = "Freeze Extreme Action",
                safety = "Tripod not required; safe handheld."
            )
            sec <= 1.0 / 1000.0 -> MotionBlurEffect(
                description = "Action & Sports: Freezes athletes, running children, moving pets, vehicles.",
                badgeText = "Freeze Sports & Wildlife",
                safety = "Safe handheld."
            )
            sec <= 1.0 / 250.0 -> MotionBlurEffect(
                description = "Sharp Portraiture: Freezes human blinking, gestures, gentle wind on clothes.",
                badgeText = "Standard Sharp Portrait",
                safety = "Safe handheld with standard lenses."
            )
            sec <= 1.0 / 60.0 -> MotionBlurEffect(
                description = "Moderate Handheld: Fine for static subjects; moving limbs may exhibit motion trail.",
                badgeText = "Casual Static Scene",
                safety = "Watch for camera shake with telephoto lenses."
            )
            sec <= 1.0 / 8.0 -> MotionBlurEffect(
                description = "Intentional Motion Blur: Panning vehicle shots, moving crowd blur, textural waves.",
                badgeText = "Creative Panning / Motion",
                safety = "Requires IBIS, OIS, or solid brace/monopod."
            )
            sec < 2.0 -> MotionBlurEffect(
                description = "Silky Water & Streaks: Cascading waterfalls become misty; headlights streak.",
                badgeText = "Misty Waterfalls / Night Streaks",
                safety = "Tripod mandatory to prevent camera shake."
            )
            else -> MotionBlurEffect(
                description = "Deep Long Exposure / Astrophotography: Glassy mirror seas, celestial star fields, architectural ghosting.",
                badgeText = "Bulb / Long Exposure",
                safety = "Solid tripod + remote shutter release recommended."
            )
        }
    }
}

data class DepthOfFieldCalculation(
    val focalLengthMm: Int,
    val aperture: Float,
    val subjectDistanceMeters: Float,
    val sensorFormat: SensorFormat,
    val circleOfConfusionMm: Float,
    val hyperfocalMeters: Float,
    val nearLimitMeters: Float,
    val farLimitMeters: Float,
    val totalDofMeters: Float,
    val isFarLimitInfinite: Boolean,
    val frontPercent: Float,
    val rearPercent: Float,
    val airyDiskMicrons: Float,
    val isDiffractionLimited: Boolean
) {
    val hyperfocalDisplay: String
        get() = "${String.format(java.util.Locale.US, "%.2f", hyperfocalMeters)}m"

    val nearLimitDisplay: String
        get() = "${String.format(java.util.Locale.US, "%.2f", nearLimitMeters)}m"

    val farLimitDisplay: String
        get() = if (isFarLimitInfinite) "Infinity (∞)" else "${String.format(java.util.Locale.US, "%.2f", farLimitMeters)}m"

    val totalDofDisplay: String
        get() = if (isFarLimitInfinite) "Infinite (∞)" else if (totalDofMeters < 1.0f) "${(totalDofMeters * 100).roundToInt()} cm" else "${String.format(java.util.Locale.US, "%.2f", totalDofMeters)}m"
}

data class ExposureBalanceCalculation(
    val sceneEv100: Float,
    val settingsEv100: Float,
    val deltaEv: Float,
    val zoneSystemName: String,
    val statusText: String,
    val isBalanced: Boolean
)

data class AstroStarTrailCalculation(
    val focalLengthMm: Int,
    val aperture: Float,
    val sensorFormat: SensorFormat,
    val rule500Seconds: Double,
    val npfRuleSeconds: Double,
    val rule500Display: String,
    val npfRuleDisplay: String
)

data class NdLongExposureOption(
    val stops: Int,
    val name: String,
    val resultingSeconds: Double,
    val displayString: String,
    val isBulbRequired: Boolean
)

data class MotionBlurEffect(
    val description: String,
    val badgeText: String,
    val safety: String
)
