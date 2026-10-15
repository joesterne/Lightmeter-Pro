package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.data.model.ExposureStandards
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

/**
 * Battery-optimized, hysteresis-filtered ambient light sensor manager.
 * Throttles high-frequency hardware events to 30Hz and applies an exponential moving average
 * with deadband filtering to eliminate micro-jitter and unnecessary UI recompositions.
 */
class LightSensorManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

    private val _lux = MutableStateFlow(400f) // Default standard indoor office light ~400 lux
    val lux: StateFlow<Float> = _lux.asStateFlow()

    private val _ev100 = MutableStateFlow(ExposureStandards.luxToEv100(400f))
    val ev100: StateFlow<Float> = _ev100.asStateFlow()

    private val _isSensorAvailable = MutableStateFlow(lightSensor != null)
    val isSensorAvailable: StateFlow<Boolean> = _isSensorAvailable.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private var smoothedLux = 400f
    private var lastEmittedLux = 400f
    private var lastEmittedEv = ExposureStandards.luxToEv100(400f)
    private var lastSampleTimeMs = 0L
    private val minSamplingIntervalMs = 33L // Cap at 30Hz for maximum battery life and zero UI stutter

    fun startListening(sampleDelay: Int = SensorManager.SENSOR_DELAY_UI) {
        if (lightSensor != null && !_isListening.value) {
            sensorManager?.registerListener(this, lightSensor, sampleDelay)
            _isListening.value = true
        }
    }

    fun stopListening() {
        if (_isListening.value) {
            sensorManager?.unregisterListener(this)
            _isListening.value = false
        }
    }

    fun setManualLux(manualLux: Float) {
        val safeLux = manualLux.coerceIn(0.1f, 150000f)
        smoothedLux = safeLux
        lastEmittedLux = safeLux
        val ev = ExposureStandards.luxToEv100(safeLux)
        lastEmittedEv = ev
        _lux.value = safeLux
        _ev100.value = ev
    }

    fun setManualEv100(ev: Float) {
        val safeEv = ev.coerceIn(-8f, 20f)
        val safeLux = ExposureStandards.ev100ToLux(safeEv)
        smoothedLux = safeLux
        lastEmittedLux = safeLux
        lastEmittedEv = safeEv
        _ev100.value = safeEv
        _lux.value = safeLux
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val now = System.currentTimeMillis()
            if (now - lastSampleTimeMs < minSamplingIntervalMs) {
                return
            }
            lastSampleTimeMs = now

            val rawLux = event.values[0]
            // Exponential moving average filter (EMA alpha = 0.35)
            smoothedLux = smoothedLux * 0.65f + rawLux * 0.35f

            val currentEv = ExposureStandards.luxToEv100(smoothedLux)

            // Deadband filter: ignore micro-noise (< 0.04 EV and < 1.5% lux change) unless 300ms has elapsed
            val evDelta = abs(currentEv - lastEmittedEv)
            val luxDeltaRatio = abs(smoothedLux - lastEmittedLux) / (lastEmittedLux + 0.1f)

            if (evDelta >= 0.04f || luxDeltaRatio >= 0.015f) {
                lastEmittedLux = smoothedLux
                lastEmittedEv = currentEv
                _lux.value = smoothedLux
                _ev100.value = currentEv
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
