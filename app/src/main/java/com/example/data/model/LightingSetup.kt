package com.example.data.model

import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

enum class LightRole(val title: String) {
    KEY("Key Light"),
    FILL("Fill Light"),
    RIM("Rim / Hair Light"),
    BACKGROUND("Background Light")
}

data class StudioLight(
    val id: String,
    val name: String,
    val role: LightRole,
    val guideNumberMeters: Float = 60f, // GN at ISO 100
    val powerFraction: Float = 1.0f,     // 1/1, 1/2, 1/4, 1/8, 1/16, etc.
    val distanceMeters: Float = 2.0f,
    val modifierFactorStops: Float = -1.0f, // Softbox / diffuser loss (e.g. -1 stop)
    val isEnabled: Boolean = true
) {
    val powerString: String
        get() = when {
            powerFraction >= 1.0f -> "1/1"
            powerFraction >= 0.5f -> "1/2"
            powerFraction >= 0.25f -> "1/4"
            powerFraction >= 0.125f -> "1/8"
            powerFraction >= 0.0625f -> "1/16"
            powerFraction >= 0.03125f -> "1/32"
            powerFraction >= 0.015625f -> "1/64"
            else -> "1/128"
        }

    /**
     * Effective f-stop contribution at subject:
     * f = (GN * sqrt(power) * 2^(modifier/2)) / distance
     */
    fun calculatedAperture(iso: Int = 100): Float {
        val isoMultiplier = sqrt(iso.toDouble() / 100.0)
        val effectiveGn = guideNumberMeters * isoMultiplier * sqrt(powerFraction.toDouble()) * 2.0.pow(modifierFactorStops / 2.0)
        val fStop = (effectiveGn / distanceMeters.coerceAtLeast(0.3f)).toFloat()
        return (fStop * 10f).roundToInt() / 10f
    }
}

data class MultiLightSetup(
    val name: String,
    val lights: List<StudioLight>,
    val targetAperture: Float = 5.6f,
    val iso: Int = 100,
    val syncSpeed: String = "1/200"
) {
    val keyLight: StudioLight?
        get() = lights.firstOrNull { it.role == LightRole.KEY && it.isEnabled }

    val fillLight: StudioLight?
        get() = lights.firstOrNull { it.role == LightRole.FILL && it.isEnabled }

    /**
     * Lighting ratio Key : Fill (e.g. 2:1 = 1 stop difference, 4:1 = 2 stops difference)
     */
    val lightingRatioString: String
        get() {
            val key = keyLight ?: return "1:1 (Single Key)"
            val fill = fillLight ?: return "High Contrast (No Fill)"
            val keyF = key.calculatedAperture(iso)
            val fillF = fill.calculatedAperture(iso)
            if (fillF <= 0.1f) return "Key Only"
            val stopDiff = 2.0 * log2((keyF / fillF).toDouble())
            val ratioVal = 2.0.pow(stopDiff)
            return if (ratioVal >= 1.0) {
                "${(ratioVal * 10).roundToInt() / 10f} : 1 (+${(stopDiff * 10).roundToInt() / 10f} EV)"
            } else {
                "1 : ${((1.0 / ratioVal) * 10).roundToInt() / 10f}"
            }
        }
}
