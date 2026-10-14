package com.example

import com.example.data.model.ExposureStandards
import com.example.data.model.SensorFormat
import org.junit.Assert.assertEquals
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
}
