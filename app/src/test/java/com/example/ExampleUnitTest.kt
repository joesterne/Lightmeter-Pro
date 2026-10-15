package com.example

import com.example.data.model.ExposureStandards
import com.example.data.model.LightReading
import com.example.data.model.LightRole
import com.example.data.model.PortraitRatioCalculation
import com.example.data.model.SensorFormat
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
}
