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
        _lux.value = safeLux
        _ev100.value = ExposureStandards.luxToEv100(safeLux)
    }

    fun setManualEv100(ev: Float) {
        val safeEv = ev.coerceIn(-8f, 20f)
        _ev100.value = safeEv
        _lux.value = ExposureStandards.ev100ToLux(safeEv)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val rawLux = event.values[0]
            // Exponential moving average filter for smooth meter needles
            smoothedLux = smoothedLux * 0.7f + rawLux * 0.3f
            _lux.value = smoothedLux
            _ev100.value = ExposureStandards.luxToEv100(smoothedLux)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
