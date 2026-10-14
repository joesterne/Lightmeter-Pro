package com.example

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DefaultData
import com.example.data.local.ExposureLogEntity
import com.example.data.local.GearEntity
import com.example.data.local.PresetEntity
import com.example.data.model.CameraModel
import com.example.data.model.ExposurePriority
import com.example.data.model.ExposureStandards
import com.example.data.model.LensModel
import com.example.data.model.LightRole
import com.example.data.model.MultiLightSetup
import com.example.data.model.StudioLight
import com.example.data.remote.ChatMessage
import com.example.data.remote.GeminiService
import com.example.data.repository.LightmeterRepository
import com.example.data.repository.SyncState
import com.example.sensor.BluetoothShutterManager
import com.example.sensor.LightSensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LightmeterRepository(application)
    val lightSensorManager = LightSensorManager(application)
    val shutterManager = BluetoothShutterManager(application)
    val cameraAnalyzer = com.example.sensor.CameraLuminanceAnalyzer()

    private val _isCameraViewfinderActive = MutableStateFlow(false)
    val isCameraViewfinderActive: StateFlow<Boolean> = _isCameraViewfinderActive.asStateFlow()

    // Flow from Room Database
    val presets = repository.getPresets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gearInventory = repository.getAllGear()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exposureLogs = repository.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val syncState: StateFlow<SyncState> = repository.syncState

    // UI Modes
    private val _isRedNightMode = MutableStateFlow(false)
    val isRedNightMode: StateFlow<Boolean> = _isRedNightMode.asStateFlow()

    private val _isBatterySaver = MutableStateFlow(false)
    val isBatterySaver: StateFlow<Boolean> = _isBatterySaver.asStateFlow()

    // Camera & Lens Selection
    val brands: List<String> = repository.getBrands()

    private val _selectedBrand = MutableStateFlow(brands.first())
    val selectedBrand: StateFlow<String> = _selectedBrand.asStateFlow()

    private val _availableCameras = MutableStateFlow(repository.getCamerasForBrand(brands.first()))
    val availableCameras: StateFlow<List<CameraModel>> = _availableCameras.asStateFlow()

    private val _selectedCamera = MutableStateFlow(availableCameras.value.first())
    val selectedCamera: StateFlow<CameraModel> = _selectedCamera.asStateFlow()

    private val _availableLenses = MutableStateFlow(repository.getLensesForBrand(brands.first()))
    val availableLenses: StateFlow<List<LensModel>> = _availableLenses.asStateFlow()

    private val _selectedLens = MutableStateFlow(availableLenses.value.first())
    val selectedLens: StateFlow<LensModel> = _selectedLens.asStateFlow()

    private val _focalLengthMm = MutableStateFlow(50)
    val focalLengthMm: StateFlow<Int> = _focalLengthMm.asStateFlow()

    // Exposure Parameters
    private val _priority = MutableStateFlow(ExposurePriority.APERTURE_PRIORITY)
    val priority: StateFlow<ExposurePriority> = _priority.asStateFlow()

    private val _iso = MutableStateFlow(100)
    val iso: StateFlow<Int> = _iso.asStateFlow()

    private val _aperture = MutableStateFlow(2.8f)
    val aperture: StateFlow<Float> = _aperture.asStateFlow()

    private val _shutterSpeed = MutableStateFlow("1/125")
    val shutterSpeed: StateFlow<String> = _shutterSpeed.asStateFlow()

    private val _ndFilterStops = MutableStateFlow(0)
    val ndFilterStops: StateFlow<Int> = _ndFilterStops.asStateFlow()

    private val _isMeteringLive = MutableStateFlow(true)
    val isMeteringLive: StateFlow<Boolean> = _isMeteringLive.asStateFlow()

    // Multi-Light Studio Setup
    private val _studioSetup = MutableStateFlow(
        MultiLightSetup(
            name = "Commercial Portrait Setup",
            lights = listOf(
                StudioLight("1", "Main Octabox Key", LightRole.KEY, 60f, 1.0f, 1.8f, -1.0f, true),
                StudioLight("2", "Fill Strip Box", LightRole.FILL, 60f, 0.25f, 2.5f, -1.0f, true),
                StudioLight("3", "Hair / Rim Light", LightRole.RIM, 45f, 0.5f, 1.5f, 0.0f, true),
                StudioLight("4", "Seamless Background", LightRole.BACKGROUND, 45f, 0.5f, 2.0f, 0.0f, false)
            ),
            targetAperture = 8.0f,
            iso = 100,
            syncSpeed = "1/160"
        )
    )
    val studioSetup: StateFlow<MultiLightSetup> = _studioSetup.asStateFlow()

    // Gemini Photo Understanding State (gemini-4-argon)
    private val _uploadedPhoto = MutableStateFlow<Bitmap?>(null)
    val uploadedPhoto: StateFlow<Bitmap?> = _uploadedPhoto.asStateFlow()

    private val _isAnalyzingPhoto = MutableStateFlow(false)
    val isAnalyzingPhoto: StateFlow<Boolean> = _isAnalyzingPhoto.asStateFlow()

    private val _photoAnalysisResult = MutableStateFlow<String?>(null)
    val photoAnalysisResult: StateFlow<String?> = _photoAnalysisResult.asStateFlow()

    // Gemini Chatbot State (models/gemini-3.8-flash, etc.)
    val chatRoles = listOf(
        "Master Lighting Technician & Exposure Director" to "You are a master lighting technician and veteran cinematographer. Guide photographers on exact exposure triangle settings, incident vs reflected metering, lighting ratios (Key, Fill, Rim), Kelvin white balance, and ND filters. Keep answers professional, crisp, and direct.",
        "Studio Strobe & Flash Specialist" to "You are a professional studio photographer specialized in multi-strobe ratios, guide numbers, inverse square law, HSS high-speed sync, and lighting modifiers (octaboxes, beauty dishes, grids). Give precise mathematical flash power and distance advice.",
        "Astrophotography & Night Sky Guide" to "You are an expert astrophotographer. Give pinpoint guidance on the 500 Rule, NPF rule for avoiding star trailing, dark sky Bortle scale compensation, sensor noise at high ISO, and milky way exposure planning."
    )

    private val _selectedChatRoleIndex = MutableStateFlow(0)
    val selectedChatRoleIndex: StateFlow<Int> = _selectedChatRoleIndex.asStateFlow()

    private val _selectedChatModel = MutableStateFlow("models/gemini-3.8-flash")
    val selectedChatModel: StateFlow<String> = _selectedChatModel.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                role = "model",
                text = "Welcome to Lightmeter Pro AI Advisor. I am your Master Lighting Technician. Ask me anything about exposure ratios, reciprocal handheld limits, strobe setups, or camera calibration!"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatSending = MutableStateFlow(false)
    val isChatSending: StateFlow<Boolean> = _isChatSending.asStateFlow()

    init {
        // Start listening to light sensor
        lightSensorManager.startListening()

        // Recalculate exposure as sensor lux changes if in live metering mode
        viewModelScope.launch {
            lightSensorManager.ev100.collect { ev ->
                if (_isMeteringLive.value) {
                    recalculateExposure(ev)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        lightSensorManager.stopListening()
    }

    fun toggleRedNightMode() {
        _isRedNightMode.value = !_isRedNightMode.value
    }

    fun toggleBatterySaver() {
        _isBatterySaver.value = !_isBatterySaver.value
        if (_isBatterySaver.value) {
            // Low sensor frequency for battery preservation
            lightSensorManager.stopListening()
            lightSensorManager.startListening(android.hardware.SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            lightSensorManager.stopListening()
            lightSensorManager.startListening(android.hardware.SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun selectBrand(brand: String) {
        _selectedBrand.value = brand
        val cameras = repository.getCamerasForBrand(brand)
        _availableCameras.value = cameras
        if (cameras.isNotEmpty()) {
            selectCamera(cameras.first())
        }
        val lenses = repository.getLensesForBrand(brand)
        _availableLenses.value = lenses
        if (lenses.isNotEmpty()) {
            selectLens(lenses.first())
        }
    }

    fun selectCamera(camera: CameraModel) {
        _selectedCamera.value = camera
        recalculateExposure()
    }

    fun selectLens(lens: LensModel) {
        _selectedLens.value = lens
        val currentFocal = _focalLengthMm.value
        if (currentFocal < lens.minFocalLength || currentFocal > lens.maxFocalLength) {
            _focalLengthMm.value = lens.minFocalLength
        }
        // Clamp aperture if needed
        if (_aperture.value < lens.maxAperture) {
            _aperture.value = lens.maxAperture
        }
        recalculateExposure()
    }

    fun setFocalLength(mm: Int) {
        val lens = _selectedLens.value
        _focalLengthMm.value = mm.coerceIn(lens.minFocalLength, lens.maxFocalLength)
    }

    fun setPriority(p: ExposurePriority) {
        _priority.value = p
        recalculateExposure()
    }

    fun setIso(newIso: Int) {
        _iso.value = newIso
        recalculateExposure()
    }

    fun stepIso(increase: Boolean) {
        val isos = ExposureStandards.STANDARD_ISOS
        val currentIndex = isos.indexOf(_iso.value).let { if (it == -1) 1 else it }
        val nextIndex = if (increase) (currentIndex + 1).coerceAtMost(isos.size - 1) else (currentIndex - 1).coerceAtLeast(0)
        _iso.value = isos[nextIndex]
        recalculateExposure()
    }

    fun stepAperture(closeDown: Boolean) {
        val apertures = ExposureStandards.STANDARD_APERTURES
        val currentIndex = apertures.indexOfFirst { kotlin.math.abs(it - _aperture.value) < 0.1f }
            .let { if (it == -1) 5 else it }
        val nextIndex = if (closeDown) (currentIndex + 1).coerceAtMost(apertures.size - 1) else (currentIndex - 1).coerceAtLeast(0)
        _aperture.value = apertures[nextIndex]
        recalculateExposure()
    }

    fun stepShutter(faster: Boolean) {
        val speeds = ExposureStandards.STANDARD_SHUTTER_SPEEDS
        val currentIndex = speeds.indexOf(_shutterSpeed.value).let { if (it == -1) 11 else it }
        val nextIndex = if (faster) (currentIndex + 1).coerceAtMost(speeds.size - 1) else (currentIndex - 1).coerceAtLeast(0)
        _shutterSpeed.value = speeds[nextIndex]
        recalculateExposure()
    }

    fun setNdFilterStops(stops: Int) {
        _ndFilterStops.value = stops
        recalculateExposure()
    }

    fun toggleLiveMetering(enabled: Boolean) {
        _isMeteringLive.value = enabled
        if (enabled) {
            lightSensorManager.startListening()
        }
    }

    fun toggleCameraViewfinder(active: Boolean? = null) {
        _isCameraViewfinderActive.value = active ?: !_isCameraViewfinderActive.value
    }

    fun applyCameraSpotReading(ev: Float, lux: Float) {
        _isMeteringLive.value = false
        lightSensorManager.setManualEv100(ev)
        recalculateExposure(ev)
        Toast.makeText(getApplication(), "Locked Spot Exposure: EV ${String.format(java.util.Locale.US, "%.1f", ev)}", Toast.LENGTH_SHORT).show()
    }

    fun setManualEv(ev: Float) {
        _isMeteringLive.value = false
        lightSensorManager.setManualEv100(ev)
        recalculateExposure(ev)
    }

    private fun recalculateExposure(ev100: Float = lightSensorManager.ev100.value) {
        val nd = _ndFilterStops.value
        when (_priority.value) {
            ExposurePriority.APERTURE_PRIORITY -> {
                _shutterSpeed.value = ExposureStandards.calculateShutterSpeed(
                    ev100 = ev100,
                    iso = _iso.value,
                    aperture = _aperture.value,
                    ndStops = nd
                )
            }
            ExposurePriority.SHUTTER_PRIORITY -> {
                _aperture.value = ExposureStandards.calculateAperture(
                    ev100 = ev100,
                    iso = _iso.value,
                    shutterSpeed = _shutterSpeed.value,
                    ndStops = nd
                )
            }
            ExposurePriority.ISO_PRIORITY -> {
                _iso.value = ExposureStandards.calculateIso(
                    ev100 = ev100,
                    aperture = _aperture.value,
                    shutterSpeed = _shutterSpeed.value,
                    ndStops = nd
                )
            }
            ExposurePriority.MANUAL_LOCK -> {
                // Keep manual values, user inspects histogram / balance
            }
        }
    }

    fun getRecommendedHandheldShutter(): String {
        val camera = _selectedCamera.value
        val lens = _selectedLens.value
        val hasStab = camera.hasIbis || lens.hasOis
        return ExposureStandards.recommendedHandheldShutter(
            focalLengthMm = _focalLengthMm.value,
            sensorFormat = camera.sensorFormat,
            hasStabilization = hasStab
        )
    }

    fun isHandheldShutterRisk(): Boolean {
        val currentSec = ExposureStandards.shutterSpeedStringToSeconds(_shutterSpeed.value)
        val safeSec = ExposureStandards.shutterSpeedStringToSeconds(getRecommendedHandheldShutter())
        return currentSec > (safeSec * 1.25)
    }

    // Presets
    fun applyPreset(preset: PresetEntity) {
        _iso.value = preset.iso
        _aperture.value = preset.aperture
        _shutterSpeed.value = preset.shutterSpeed
        _focalLengthMm.value = preset.focalLengthMm
        _ndFilterStops.value = preset.ndFilterStops
        _priority.value = ExposurePriority.MANUAL_LOCK
        lightSensorManager.setManualEv100(preset.targetEv100)
        Toast.makeText(getApplication(), "Applied Preset: ${preset.name}", Toast.LENGTH_SHORT).show()
    }

    fun saveCurrentAsPreset(name: String, category: String, description: String) {
        viewModelScope.launch {
            val preset = PresetEntity(
                name = name,
                category = category,
                environmentDescription = description,
                targetEv100 = lightSensorManager.ev100.value,
                iso = _iso.value,
                shutterSpeed = _shutterSpeed.value,
                aperture = _aperture.value,
                focalLengthMm = _focalLengthMm.value,
                ndFilterStops = _ndFilterStops.value,
                lightingRatioNotes = "Saved from live session",
                isCustom = true
            )
            repository.savePreset(preset)
            Toast.makeText(getApplication(), "Saved custom preset '$name'", Toast.LENGTH_SHORT).show()
        }
    }

    fun deletePreset(preset: PresetEntity) {
        viewModelScope.launch {
            repository.deletePreset(preset)
        }
    }

    // Gear
    fun addGearItem(name: String, brand: String, category: String, serialNumber: String, weightGrams: Int, notes: String) {
        viewModelScope.launch {
            val item = GearEntity(
                name = name,
                brand = brand,
                category = category,
                serialNumber = serialNumber,
                weightGrams = weightGrams,
                inBag = true,
                notes = notes
            )
            repository.addGear(item)
            Toast.makeText(getApplication(), "Added to inventory: $name", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleGearInBag(gear: GearEntity) {
        viewModelScope.launch {
            repository.toggleGearInBag(gear.id, !gear.inBag)
        }
    }

    fun deleteGearItem(gear: GearEntity) {
        viewModelScope.launch {
            repository.deleteGear(gear)
        }
    }

    // Multi-Light Studio
    fun updateStudioLightPower(lightId: String, powerFraction: Float) {
        val current = _studioSetup.value
        val updatedLights = current.lights.map { light ->
            if (light.id == lightId) light.copy(powerFraction = powerFraction) else light
        }
        _studioSetup.value = current.copy(lights = updatedLights)
    }

    fun toggleStudioLight(lightId: String) {
        val current = _studioSetup.value
        val updatedLights = current.lights.map { light ->
            if (light.id == lightId) light.copy(isEnabled = !light.isEnabled) else light
        }
        _studioSetup.value = current.copy(lights = updatedLights)
    }

    fun updateStudioLightDistance(lightId: String, distanceM: Float) {
        val current = _studioSetup.value
        val updatedLights = current.lights.map { light ->
            if (light.id == lightId) light.copy(distanceMeters = distanceM) else light
        }
        _studioSetup.value = current.copy(lights = updatedLights)
    }

    // Cloud Sync
    fun triggerSync() {
        viewModelScope.launch {
            repository.triggerCloudSync()
            Toast.makeText(getApplication(), "Metadata and gear synced to cloud", Toast.LENGTH_SHORT).show()
        }
    }

    // Export Settings Card
    fun exportSettingsCard(context: Context) {
        val formatted = repository.formatExportSettings(
            camera = _selectedCamera.value,
            lens = _selectedLens.value,
            focalLengthMm = _focalLengthMm.value,
            ev100 = lightSensorManager.ev100.value,
            lux = lightSensorManager.lux.value,
            iso = _iso.value,
            shutterSpeed = _shutterSpeed.value,
            aperture = _aperture.value,
            ndStops = _ndFilterStops.value,
            reciprocalRuleShutter = getRecommendedHandheldShutter(),
            lightingNotes = "Lighting Ratio: ${_studioSetup.value.lightingRatioString}"
        )

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Camera Exposure Settings", formatted)
        clipboard.setPrimaryClip(clip)

        // Also save log
        viewModelScope.launch {
            repository.saveExposureLog(
                ExposureLogEntity(
                    title = "${_selectedCamera.value.name} @ ${_focalLengthMm.value}mm",
                    ev100 = lightSensorManager.ev100.value,
                    lux = lightSensorManager.lux.value,
                    iso = _iso.value,
                    shutterSpeed = _shutterSpeed.value,
                    aperture = _aperture.value,
                    cameraBrand = _selectedCamera.value.brand,
                    cameraModel = _selectedCamera.value.name,
                    lensName = _selectedLens.value.name,
                    focalLengthMm = _focalLengthMm.value
                )
            )
        }

        Toast.makeText(context, "Settings copied to clipboard & saved to log!", Toast.LENGTH_LONG).show()
    }

    // Gemini Photo Analysis (gemini-4-argon)
    fun setPhotoForAnalysis(bitmap: Bitmap) {
        _uploadedPhoto.value = bitmap
        _photoAnalysisResult.value = null
    }

    fun runPhotoAnalysis(userNote: String = "") {
        val bitmap = _uploadedPhoto.value ?: return
        viewModelScope.launch {
            _isAnalyzingPhoto.value = true
            _photoAnalysisResult.value = null

            val cameraContext = "${_selectedCamera.value.brand} ${_selectedCamera.value.name}, Lens: ${_selectedLens.value.name} @ ${_focalLengthMm.value}mm"
            val prompt = if (userNote.isNotBlank()) userNote else "Analyze this photo's exposure, lighting ratio, dynamic range highlights/shadows, and recommended camera settings."

            val result = GeminiService.analyzePhoto(bitmap, prompt, cameraContext)
            _isAnalyzingPhoto.value = false
            result.onSuccess {
                _photoAnalysisResult.value = it
            }.onFailure { err ->
                _photoAnalysisResult.value = "Analysis Error: ${err.message}"
            }
        }
    }

    // Gemini Chatbot (models/gemini-3.8-flash, etc.)
    fun selectChatRole(index: Int) {
        _selectedChatRoleIndex.value = index
    }

    fun selectChatModel(model: String) {
        _selectedChatModel.value = model
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val currentList = _chatMessages.value.toMutableList()
        val userMsg = ChatMessage(role = "user", text = text)
        currentList.add(userMsg)
        _chatMessages.value = currentList

        viewModelScope.launch {
            _isChatSending.value = true
            val systemInstruction = chatRoles[_selectedChatRoleIndex.value].second
            val result = GeminiService.sendChatMessage(
                messages = currentList,
                systemInstruction = systemInstruction,
                selectedModel = _selectedChatModel.value
            )
            _isChatSending.value = false

            result.onSuccess { reply ->
                val updated = _chatMessages.value.toMutableList()
                updated.add(ChatMessage(role = "model", text = reply))
                _chatMessages.value = updated
            }.onFailure { err ->
                val updated = _chatMessages.value.toMutableList()
                updated.add(ChatMessage(role = "model", text = "Error: ${err.message}"))
                _chatMessages.value = updated
            }
        }
    }
}
