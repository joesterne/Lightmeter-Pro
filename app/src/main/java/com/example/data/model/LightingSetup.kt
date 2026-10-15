package com.example.data.model

import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

enum class LightRole(val title: String, val defaultAngleDeg: Float) {
    KEY("Key Light", 45f),
    FILL("Fill Light", -30f),
    RIM("Hair / Rim Light", 135f),
    BACKGROUND("Background Light", 180f)
}

data class LightReading(
    val role: LightRole,
    val name: String,
    val fStop: Float = 8.0f,          // Base f-number: e.g. 2.8, 4.0, 5.6, 8.0, 11
    val fractionalTenths: Int = 0,    // 0 to 9 tenths of a stop, standard on professional light meters
    val ev100: Float = 11.0f,
    val isEnabled: Boolean = true
) {
    /**
     * Exact effective f-number with fractional tenths:
     * f_effective = fStop * 2^(fractionalTenths / 20.0)
     */
    val effectiveFStop: Float
        get() = (fStop * 2.0.pow(fractionalTenths / 20.0)).toFloat()

    val displayString: String
        get() = if (fractionalTenths == 0) {
            "f/${if (fStop % 1f == 0f) "${fStop.toInt()}" else "$fStop"}"
        } else {
            "f/${if (fStop % 1f == 0f) "${fStop.toInt()}" else "$fStop"}.$fractionalTenths"
        }
}

data class PortraitRatioCalculation(
    val keyLight: LightReading = LightReading(LightRole.KEY, "Key Light", 8.0f, 0),
    val fillLight: LightReading = LightReading(LightRole.FILL, "Fill Light", 5.6f, 0),
    val hairLight: LightReading = LightReading(LightRole.RIM, "Hair / Rim Light", 8.0f, 3),
    val backgroundLight: LightReading = LightReading(LightRole.BACKGROUND, "Background Light", 5.6f, 0, isEnabled = false),
    val cameraIso: Int = 100,
    val syncSpeed: String = "1/160"
) {
    /**
     * Difference in stops between Key and Fill:
     * deltaStops = 2 * log2(f_key / f_fill)
     */
    val keyFillStopDiff: Float
        get() {
            if (!fillLight.isEnabled || fillLight.effectiveFStop <= 0.1f) return 0f
            return (2.0 * log2((keyLight.effectiveFStop / fillLight.effectiveFStop).toDouble())).toFloat()
        }

    /**
     * Light Source Output Ratio (Key alone vs Fill alone): e.g. 2:1 (1 stop), 4:1 (2 stops), 8:1 (3 stops)
     */
    val sourceRatioString: String
        get() {
            if (!fillLight.isEnabled) return "1:1 (Key Only)"
            val ratio = 2.0.pow(keyFillStopDiff.toDouble())
            return "${(ratio * 10).roundToInt() / 10f} : 1"
        }

    /**
     * Total Scene Reflectance / Lighting Ratio (Key + Fill on highlight side : Fill on shadow side):
     * The standard classical portraiture ratio metric (Dean Collins / Sekonic standard)
     */
    val totalToShadowRatioString: String
        get() {
            if (!fillLight.isEnabled) return "1:1"
            val keyPower = 2.0.pow(keyFillStopDiff.toDouble())
            val totalHighlight = keyPower + 1.0 // Key + Fill
            val shadow = 1.0 // Fill alone
            val ratio = totalHighlight / shadow
            return "${(ratio * 10).roundToInt() / 10f} : 1"
        }

    /**
     * Recommended working lens aperture to set on camera:
     * f_camera = sqrt(f_key^2 + f_fill^2)
     */
    val workingCameraAperture: Float
        get() {
            val keyF = if (keyLight.isEnabled) keyLight.effectiveFStop else 0f
            val fillF = if (fillLight.isEnabled) fillLight.effectiveFStop else 0f
            val combined = sqrt(keyF * keyF + fillF * fillF)
            return (combined * 10).roundToInt() / 10f
        }

    /**
     * Hair Light difference relative to Key (in stops):
     */
    val hairKeyStopDiff: Float
        get() {
            if (!hairLight.isEnabled || keyLight.effectiveFStop <= 0.1f) return 0f
            return (2.0 * log2((hairLight.effectiveFStop / keyLight.effectiveFStop).toDouble())).toFloat()
        }

    val hairRatioString: String
        get() {
            if (!hairLight.isEnabled) return "Off"
            val diff = hairKeyStopDiff
            val prefix = if (diff >= 0) "+" else ""
            val ratio = 2.0.pow(diff.toDouble())
            return "${(ratio * 10).roundToInt() / 10f} : 1 ($prefix${String.format(java.util.Locale.US, "%.1f", diff)} EV)"
        }

    /**
     * Background Light difference relative to Key:
     */
    val backgroundKeyStopDiff: Float
        get() {
            if (!backgroundLight.isEnabled || keyLight.effectiveFStop <= 0.1f) return 0f
            return (2.0 * log2((backgroundLight.effectiveFStop / keyLight.effectiveFStop).toDouble())).toFloat()
        }

    val backgroundRatioString: String
        get() {
            if (!backgroundLight.isEnabled) return "Off"
            val diff = backgroundKeyStopDiff
            val prefix = if (diff >= 0) "+" else ""
            return "$prefix${String.format(java.util.Locale.US, "%.1f", diff)} EV vs Key"
        }

    /**
     * Photographic style interpretation based on ratio
     */
    val portraitStyleInterpretation: String
        get() {
            val diff = keyFillStopDiff
            return when {
                diff < 0.5f -> "1:1 / Flat High-Key: Shadowless beauty, cosmetic, catalog and baby portraits."
                diff in 0.5f..1.2f -> "2:1 (~1 Stop): Soft dimensional commercial portraiture, universally flattering."
                diff in 1.3f..1.8f -> "3:1 (~1.5 Stops): Classic Rembrandt / Loop, distinct facial contouring and natural depth."
                diff in 1.9f..2.5f -> "4:1 (~2 Stops): Dramatic editorial, strong character portraits, defined shadows."
                diff in 2.6f..3.5f -> "8:1 (~3 Stops): Film Noir / Low-Key, moody, high-tension cinematic lighting."
                else -> ">8:1: Split / Extreme High Contrast, graphic silhouette or edge-only rim."
            }
        }

    val hairLightWarning: String?
        get() {
            if (!hairLight.isEnabled) return null
            val diff = hairKeyStopDiff
            return when {
                diff > 1.8f -> "Hair light is +${String.format(java.util.Locale.US, "%.1f", diff)} EV over Key: high risk of blowing out highlights on light/blonde hair!"
                diff < -1.5f -> "Hair light is ${String.format(java.util.Locale.US, "%.1f", diff)} EV under Key: may be too faint to provide edge separation on dark hair."
                else -> null
            }
        }
}

// Backward-compatible StudioLight & MultiLightSetup
data class StudioLight(
    val id: String,
    val name: String,
    val role: LightRole,
    val guideNumberMeters: Float = 60f,
    val powerFraction: Float = 1.0f,
    val distanceMeters: Float = 2.0f,
    val modifierFactorStops: Float = -1.0f,
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
