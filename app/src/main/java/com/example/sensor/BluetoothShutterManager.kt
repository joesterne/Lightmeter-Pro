package com.example.sensor

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BluetoothCameraDevice(
    val name: String,
    val address: String,
    val brand: String,
    val isConnected: Boolean = false
)

class BluetoothShutterManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _connectedDevice = MutableStateFlow<BluetoothCameraDevice?>(null)
    val connectedDevice: StateFlow<BluetoothCameraDevice?> = _connectedDevice.asStateFlow()

    private val _availableDevices = MutableStateFlow<List<BluetoothCameraDevice>>(emptyList())
    val availableDevices: StateFlow<List<BluetoothCameraDevice>> = _availableDevices.asStateFlow()

    private val _isShutterActive = MutableStateFlow(false)
    val isShutterActive: StateFlow<Boolean> = _isShutterActive.asStateFlow()

    private val _countdownSeconds = MutableStateFlow(0)
    val countdownSeconds: StateFlow<Int> = _countdownSeconds.asStateFlow()

    private val _bulbElapsedTimeSeconds = MutableStateFlow(0)
    val bulbElapsedTimeSeconds: StateFlow<Int> = _bulbElapsedTimeSeconds.asStateFlow()

    private val _isBulbRunning = MutableStateFlow(false)
    val isBulbRunning: StateFlow<Boolean> = _isBulbRunning.asStateFlow()

    private val _intervalometerProgress = MutableStateFlow("")
    val intervalometerProgress: StateFlow<String> = _intervalometerProgress.asStateFlow()

    private var bulbJob: Job? = null
    private var countdownJob: Job? = null
    private var intervalJob: Job? = null

    init {
        // Sample standard camera remotes
        _availableDevices.value = listOf(
            BluetoothCameraDevice("Sony RMT-P1BT Remote", "E4:5F:01:8A:22:91", "Sony"),
            BluetoothCameraDevice("Canon Wireless Remote BR-E1", "CC:6A:B0:12:44:8C", "Canon"),
            BluetoothCameraDevice("Nikon ML-L7 Remote Control", "F0:18:98:55:77:31", "Nikon"),
            BluetoothCameraDevice("Godox XPro-II Wireless Trigger", "00:1B:DC:09:88:12", "Godox")
        )
    }

    private fun vibrate(durationMs: Long = 100) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMs)
                }
            }
        } catch (e: Exception) {
            // Ignore vibration error on emulators/unsupported devices
        }
    }

    fun startScan() {
        scope.launch {
            _isScanning.value = true
            delay(1500)
            _isScanning.value = false
        }
    }

    fun connectDevice(device: BluetoothCameraDevice) {
        _connectedDevice.value = device.copy(isConnected = true)
        vibrate(80)
    }

    fun disconnectDevice() {
        _connectedDevice.value = null
    }

    /**
     * Instant Shutter Release
     */
    fun triggerInstantShutter(onSuccess: (() -> Unit)? = null) {
        scope.launch {
            _isShutterActive.value = true
            vibrate(150)
            delay(300)
            _isShutterActive.value = false
            onSuccess?.invoke()
        }
    }

    /**
     * Countdown Shutter Release (2s, 5s, 10s)
     */
    fun triggerCountdownShutter(seconds: Int, onSuccess: (() -> Unit)? = null) {
        countdownJob?.cancel()
        countdownJob = scope.launch {
            _countdownSeconds.value = seconds
            while (_countdownSeconds.value > 0) {
                vibrate(50)
                delay(1000)
                _countdownSeconds.value -= 1
            }
            triggerInstantShutter(onSuccess)
        }
    }

    fun cancelCountdown() {
        countdownJob?.cancel()
        _countdownSeconds.value = 0
    }

    /**
     * Bulb Mode Long Exposure
     */
    fun startBulbExposure() {
        bulbJob?.cancel()
        _isBulbRunning.value = true
        _bulbElapsedTimeSeconds.value = 0
        vibrate(200)

        bulbJob = scope.launch {
            while (_isBulbRunning.value) {
                delay(1000)
                _bulbElapsedTimeSeconds.value += 1
            }
        }
    }

    fun stopBulbExposure(onFinished: ((seconds: Int) -> Unit)? = null) {
        val total = _bulbElapsedTimeSeconds.value
        _isBulbRunning.value = false
        bulbJob?.cancel()
        vibrate(250)
        onFinished?.invoke(total)
    }

    /**
     * Intervalometer for timelapses
     */
    fun startIntervalometer(shotsCount: Int, intervalSeconds: Int) {
        intervalJob?.cancel()
        intervalJob = scope.launch {
            for (i in 1..shotsCount) {
                _intervalometerProgress.value = "Frame $i of $shotsCount"
                vibrate(100)
                delay(intervalSeconds * 1000L)
            }
            _intervalometerProgress.value = "Timelapse Complete ($shotsCount frames)"
            vibrate(300)
            delay(2000)
            _intervalometerProgress.value = ""
        }
    }

    fun stopIntervalometer() {
        intervalJob?.cancel()
        _intervalometerProgress.value = ""
    }
}
