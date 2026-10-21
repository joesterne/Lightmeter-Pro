package com.example

import com.example.data.model.ExposureStandards
import com.example.data.model.LightReading
import com.example.data.model.LightRole
import com.example.data.model.PortraitRatioCalculation
import com.example.data.model.SensorFormat
import com.example.ui.theme.AppAccentColor
import com.example.ui.theme.AppBackgroundVibe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testSunny16Calculation() {
        // Bright direct sun: EV 15 at ISO 100
        val ev100 = 15.0f
        val calculatedShutter = ExposureStandards.calculateShutterSpeed(
            ev100 = ev100,
            iso = 100,
            aperture = 16f
        )
        // At f/16, ISO 100, EV 15 => shutter should be ~1/125s
        assertEquals("1/125", calculatedShutter)
    }

    @Test
    fun testLuxToEvConversion() {
        // EV 0 is 2.5 lux
        val ev0 = ExposureStandards.luxToEv100(2.5f)
        assertEquals(0.0f, ev0, 0.05f)

        // 80,000 lux (bright summer sun) should be around 15 EV
        val evSun = ExposureStandards.luxToEv100(81920f)
        assertEquals(15.0f, evSun, 0.1f)
    }

    @Test
    fun testReciprocalRule() {
        // 50mm on Full Frame without stabilization: minimum ~1/50s or 1/60s
        val safeShutter = ExposureStandards.recommendedHandheldShutter(
            focalLengthMm = 50,
            sensorFormat = SensorFormat.FULL_FRAME,
            hasStabilization = false
        )
        assertEquals("1/60", safeShutter)

        // 50mm on Full Frame with 3-stop stabilization: safe down to ~1/8s
        val safeShutterWithIbis = ExposureStandards.recommendedHandheldShutter(
            focalLengthMm = 50,
            sensorFormat = SensorFormat.FULL_FRAME,
            hasStabilization = true
        )
        assertEquals("1/8", safeShutterWithIbis)
    }

    @Test
    fun testPortraitMultiLightRatios() {
        // Key @ f/8.0, Fill @ f/5.6 -> exactly 1 stop difference
        val setup1Stop = PortraitRatioCalculation(
            keyLight = LightReading(LightRole.KEY, "Key", 8.0f, 0),
            fillLight = LightReading(LightRole.FILL, "Fill", 5.6f, 0),
            hairLight = LightReading(LightRole.RIM, "Hair", 8.0f, 0)
        )

        // Key/Fill Stop difference should be ~1.0 EV
        assertEquals(1.03f, setup1Stop.keyFillStopDiff, 0.1f)
        // Source Ratio should be ~2:1
        assertTrue(setup1Stop.sourceRatioString.startsWith("2"))
        // Total scene ratio (Key + Fill) : Fill should be ~3:1
        assertTrue(setup1Stop.totalToShadowRatioString.startsWith("3"))
        // Combined working aperture: sqrt(8^2 + 5.6^2) = sqrt(64 + 31.36) = sqrt(95.36) ~ 9.8
        assertEquals(9.8f, setup1Stop.workingCameraAperture, 0.2f)
        // Hair light matches Key -> 0.0 EV diff
        assertEquals(0.0f, setup1Stop.hairKeyStopDiff, 0.05f)
        assertNull(setup1Stop.hairLightWarning)

        // 2 Stops difference: Key @ f/8.0, Fill @ f/4.0
        val setup2Stop = PortraitRatioCalculation(
            keyLight = LightReading(LightRole.KEY, "Key", 8.0f, 0),
            fillLight = LightReading(LightRole.FILL, "Fill", 4.0f, 0),
            hairLight = LightReading(LightRole.RIM, "Hair", 16.0f, 0) // Excessive hair light (+2 stops over Key)
        )
        assertEquals(2.0f, setup2Stop.keyFillStopDiff, 0.05f)
        assertEquals("4.0 : 1", setup2Stop.sourceRatioString)
        assertEquals("5.0 : 1", setup2Stop.totalToShadowRatioString)
        // Hair light is 2 stops above key (f/16 vs f/8) -> triggers blowout warning
        assertEquals(2.0f, setup2Stop.hairKeyStopDiff, 0.05f)
        assertNotNull(setup2Stop.hairLightWarning)
        assertTrue(setup2Stop.hairLightWarning!!.contains("blowing out highlights"))
    }

    @Test
    fun testAppColorsCustomization() {
        // Verify AppAccentColor resolution and fallback
        assertEquals(AppAccentColor.STUDIO_AMBER, AppAccentColor.fromId("amber"))
        assertEquals(AppAccentColor.LEICA_CRIMSON, AppAccentColor.fromId("crimson"))
        assertEquals(AppAccentColor.ZEISS_COBALT, AppAccentColor.fromId("cobalt"))
        assertEquals(AppAccentColor.FUJI_EMERALD, AppAccentColor.fromId("emerald"))
        assertEquals(AppAccentColor.NEON_VIOLET, AppAccentColor.fromId("violet"))
        assertEquals(AppAccentColor.KODAK_GOLD, AppAccentColor.fromId("gold"))
        assertEquals(AppAccentColor.TITANIUM_SILVER, AppAccentColor.fromId("silver"))
        assertEquals(AppAccentColor.CINEMATIC_TEAL, AppAccentColor.fromId("teal"))
        // Unknown id fallback
        assertEquals(AppAccentColor.STUDIO_AMBER, AppAccentColor.fromId("unknown_color"))

        // Verify AppBackgroundVibe resolution and fallback
        assertEquals(AppBackgroundVibe.OBSIDIAN_SLATE, AppBackgroundVibe.fromId("obsidian"))
        assertEquals(AppBackgroundVibe.MIDNIGHT_NAVY, AppBackgroundVibe.fromId("midnight"))
        assertEquals(AppBackgroundVibe.CARBON_TITANIUM, AppBackgroundVibe.fromId("carbon"))
        assertEquals(AppBackgroundVibe.PITCH_BLACK, AppBackgroundVibe.fromId("pitch_black"))
        assertEquals(AppBackgroundVibe.OBSIDIAN_SLATE, AppBackgroundVibe.fromId("non_existent_vibe"))
    }

    @Test
    fun testDepthOfFieldDynamicCalculation() {
        // 50mm @ f/2.8 on Full Frame at 2.5m subject distance
        val dof = ExposureStandards.calculateDepthOfField(
            focalLengthMm = 50,
            aperture = 2.8f,
            subjectDistanceMeters = 2.5f,
            sensorFormat = SensorFormat.FULL_FRAME
        )

        // Hyperfocal distance should be ~29.8 meters
        assertEquals(29.8f, dof.hyperfocalMeters, 0.5f)
        // Near limit around ~2.3m, far limit around ~2.75m
        assertEquals(2.3f, dof.nearLimitMeters, 0.1f)
        assertEquals(2.74f, dof.farLimitMeters, 0.1f)
        // Total DoF around ~0.44m (44cm)
        assertEquals(0.44f, dof.totalDofMeters, 0.1f)
        // At f/2.8, diffraction is safe on full frame
        org.junit.Assert.assertFalse(dof.isDiffractionLimited)

        // At f/22, diffraction limit should trigger
        val dofSmall = ExposureStandards.calculateDepthOfField(
            focalLengthMm = 50,
            aperture = 22.0f,
            subjectDistanceMeters = 2.5f,
            sensorFormat = SensorFormat.FULL_FRAME
        )
        assertTrue(dofSmall.isDiffractionLimited)
    }

    @Test
    fun testExposureBalanceDynamicCalculation() {
        // Scene EV100 is 12.0. With 1/60s, f/8, ISO 100 -> Settings EV is ~12.0
        val balanced = ExposureStandards.calculateExposureBalance(
            sceneEv100 = 12.0f,
            shutterSpeed = "1/60",
            aperture = 8.0f,
            iso = 100,
            ndStops = 0
        )
        assertTrue(balanced.isBalanced)
        assertTrue(balanced.zoneSystemName.contains("Zone V"))

        // Faster shutter 1/500s -> underexposes scene by ~3 stops
        val under = ExposureStandards.calculateExposureBalance(
            sceneEv100 = 12.0f,
            shutterSpeed = "1/500",
            aperture = 8.0f,
            iso = 100,
            ndStops = 0
        )
        org.junit.Assert.assertFalse(under.isBalanced)
        assertTrue(under.deltaEv < -2.0f)
        assertTrue(under.statusText.contains("Underexposed"))
    }

    @Test
    fun testDynamicInverseSquareLaw() {
        // Light distance doubles from 1.5m to 3.0m -> exact -2.0 EV drop (quarter power)
        val deltaDrop = com.example.data.model.DynamicLightingPhysics.calculateInverseSquareDeltaStops(1.5f, 3.0f)
        assertEquals(-2.0f, deltaDrop, 0.05f)

        // Light distance halves from 2.0m to 1.0m -> exact +2.0 EV boost (4x power)
        val deltaBoost = com.example.data.model.DynamicLightingPhysics.calculateInverseSquareDeltaStops(2.0f, 1.0f)
        assertEquals(2.0f, deltaBoost, 0.05f)
    }

    @Test
    fun testDynamicTargetRatioSolver() {
        // Key at f/8.0. For 4:1 source ratio, Fill should be f/4.0
        val (aperture4, tenths4) = com.example.data.model.DynamicLightingPhysics.solveFillAperture(8.0f, 4.0f)
        assertEquals(4.0f, aperture4, 0.1f)
        assertEquals(0, tenths4)

        // For 2:1 ratio (1 stop diff), Fill should be ~f/5.6
        val (aperture2, _) = com.example.data.model.DynamicLightingPhysics.solveFillAperture(8.0f, 2.0f)
        assertEquals(5.6f, aperture2, 0.1f)
    }

    @Test
    fun testAstroStarTrailingRules() {
        // 24mm @ f/1.4 on Full Frame
        val astro = ExposureStandards.calculateAstroStarTrailing(
            focalLengthMm = 24,
            aperture = 1.4f,
            sensorFormat = SensorFormat.FULL_FRAME
        )
        // 500 / 24 ~ 20.8s
        assertEquals(20.8, astro.rule500Seconds, 0.5)
        // NPF: (35 * 1.4 + 30 * 4) / 24 = (49 + 120) / 24 = 169 / 24 ~ 7.04s
        assertEquals(7.0, astro.npfRuleSeconds, 0.5)
    }
}
