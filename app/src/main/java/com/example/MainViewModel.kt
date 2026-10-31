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
    val cameraBodies = repository.getAllCameraBodies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userLenses = repository.getAllLenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val presets = repository.getPresets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gearInventory = repository.getAllGear()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exposureLogs = repository.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val syncState: StateFlow<SyncState> = repository.syncState

    // UI Modes & Customizable Color Theme
    private val _selectedAccentColor = MutableStateFlow(repository.getSavedAccentColor())
    val selectedAccentColor: StateFlow<com.example.ui.theme.AppAccentColor> = _selectedAccentColor.asStateFlow()

    private val _selectedBackgroundVibe = MutableStateFlow(repository.getSavedBackgroundVibe())
    val selectedBackgroundVibe: StateFlow<com.example.ui.theme.AppBackgroundVibe> = _selectedBackgroundVibe.asStateFlow()

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

    private val _subjectDistanceMeters = MutableStateFlow(2.5f)
    val subjectDistanceMeters: StateFlow<Float> = _subjectDistanceMeters.asStateFlow()

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

    private val _portraitCalculation = MutableStateFlow(com.example.data.model.PortraitRatioCalculation())
    val portraitCalculation: StateFlow<com.example.data.model.PortraitRatioCalculation> = _portraitCalculation.asStateFlow()

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
        shutterManager.close()
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

    fun setAccentColor(accent: com.example.ui.theme.AppAccentColor) {
        _selectedAccentColor.value = accent
        repository.saveAccentColor(accent)
    }

    fun setBackgroundVibe(vibe: com.example.ui.theme.AppBackgroundVibe) {
        _selectedBackgroundVibe.value = vibe
        repository.saveBackgroundVibe(vibe)
    }

    fun resetThemeColors() {
        setAccentColor(com.example.ui.theme.AppAccentColor.STUDIO_AMBER)
        setBackgroundVibe(com.example.ui.theme.AppBackgroundVibe.OBSIDIAN_SLATE)
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
        // coerceIn throws when min > max; a lens saved with an inverted
        // range used to crash the app on the first slider move.
        val lo = minOf(lens.minFocalLength, lens.maxFocalLength)
        val hi = maxOf(lens.minFocalLength, lens.maxFocalLength)
        _focalLengthMm.value = mm.coerceIn(lo, hi)
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
        // Step only within the mounted lens's range: this used to walk the
        // full f/1.0-f/32 list, so the meter could be set to f/1.0 on an
        // f/2.8 lens. (Aperture numbers: maxAperture is the widest/smallest
        // f-number, minAperture the narrowest/largest.)
        val lens = _selectedLens.value
        val firstAllowed = apertures.indexOfFirst { it >= lens.maxAperture - 0.05f }.let { if (it == -1) 0 else it }
        val lastAllowed = apertures.indexOfLast { it <= lens.minAperture + 0.05f }.let { if (it == -1) apertures.size - 1 else it }
        val currentIndex = apertures.indexOfFirst { kotlin.math.abs(it - _aperture.value) < 0.1f }
            .let { if (it == -1) 5 else it }
            .coerceIn(firstAllowed, lastAllowed)
        val nextIndex = if (closeDown) (currentIndex + 1).coerceAtMost(lastAllowed) else (currentIndex - 1).coerceAtLeast(firstAllowed)
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
        } else {
            // Was missing: the listener kept running, so the next hardware
            // sensor events overwrote any "locked"/manual EV (see
            // applyCameraSpotReading / setManualEv) within a few samples.
            lightSensorManager.stopListening()
        }
    }

    fun toggleCameraViewfinder(active: Boolean? = null) {
        _isCameraViewfinderActive.value = active ?: !_isCameraViewfinderActive.value
    }

    fun applyCameraSpotReading(ev: Float, lux: Float) {
        _isMeteringLive.value = false
        lightSensorManager.stopListening()
        lightSensorManager.setManualEv100(ev)
        recalculateExposure(ev)
        Toast.makeText(getApplication(), "Locked Spot Exposure: EV ${String.format(java.util.Locale.US, "%.1f", ev)}", Toast.LENGTH_SHORT).show()
    }

    fun setManualEv(ev: Float) {
        _isMeteringLive.value = false
        lightSensorManager.stopListening()
        lightSensorManager.setManualEv100(ev)
        recalculateExposure(ev)
    }

    private fun recalculateExposure(ev100: Float = lightSensorManager.ev100.value) {
        val nd = _ndFilterStops.value
        when (_priority.value) {
            ExposurePriority.APERTURE_PRIORITY -> {
                val newShutter = ExposureStandards.calculateShutterSpeed(
                    ev100 = ev100,
                    iso = _iso.value,
                    aperture = _aperture.value,
                    ndStops = nd
                )
                if (_shutterSpeed.value != newShutter) {
                    _shutterSpeed.value = newShutter
                }
            }
            ExposurePriority.SHUTTER_PRIORITY -> {
                val newAperture = ExposureStandards.calculateAperture(
                    ev100 = ev100,
                    iso = _iso.value,
                    shutterSpeed = _shutterSpeed.value,
                    ndStops = nd
                )
                if (kotlin.math.abs(_aperture.value - newAperture) > 0.05f) {
                    // Clamp the computed aperture to the mounted lens's range.
                    val lens = _selectedLens.value
                    val lo = minOf(lens.maxAperture, lens.minAperture)
                    val hi = maxOf(lens.maxAperture, lens.minAperture)
                    _aperture.value = newAperture.coerceIn(lo, hi)
                }
            }
            ExposurePriority.ISO_PRIORITY -> {
                val newIso = ExposureStandards.calculateIso(
                    ev100 = ev100,
                    aperture = _aperture.value,
                    shutterSpeed = _shutterSpeed.value,
                    ndStops = nd
                )
                if (_iso.value != newIso) {
                    _iso.value = newIso
                }
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

    // Dynamic Optical & Exposure Calculations
    fun setSubjectDistance(distanceMeters: Float) {
        _subjectDistanceMeters.value = distanceMeters.coerceIn(0.2f, 50.0f)
    }

    fun getDepthOfFieldCalculation(): com.example.data.model.DepthOfFieldCalculation {
        return ExposureStandards.calculateDepthOfField(
            focalLengthMm = _focalLengthMm.value,
            aperture = _aperture.value,
            subjectDistanceMeters = _subjectDistanceMeters.value,
            sensorFormat = _selectedCamera.value.sensorFormat
        )
    }

    fun getExposureBalanceCalculation(): com.example.data.model.ExposureBalanceCalculation {
        val sceneEv = if (_isCameraViewfinderActive.value) cameraAnalyzer.luminanceData.value.estimatedEv100 else lightSensorManager.ev100.value
        return ExposureStandards.calculateExposureBalance(
            sceneEv100 = sceneEv,
            shutterSpeed = _shutterSpeed.value,
            aperture = _aperture.value,
            iso = _iso.value,
            ndStops = _ndFilterStops.value
        )
    }

    fun getAstroCalculation(): com.example.data.model.AstroStarTrailCalculation {
        return ExposureStandards.calculateAstroStarTrailing(
            focalLengthMm = _focalLengthMm.value,
            aperture = _aperture.value,
            sensorFormat = _selectedCamera.value.sensorFormat
        )
    }

    fun getNdLongExposures(): List<com.example.data.model.NdLongExposureOption> {
        return ExposureStandards.calculateNdLongExposures(_shutterSpeed.value)
    }

    fun getMotionBlurEffect(): com.example.data.model.MotionBlurEffect {
        return ExposureStandards.calculateMotionBlurEffect(_shutterSpeed.value)
    }

    fun applyDynamicFillRatio(targetSourceRatio: Float) {
        val keyLight = _portraitCalculation.value.keyLight
        val (baseAp, tenths) = com.example.data.model.DynamicLightingPhysics.solveFillAperture(
            keyEffectiveFStop = keyLight.effectiveFStop,
            targetSourceRatio = targetSourceRatio
        )
        updatePortraitLightReading(com.example.data.model.LightRole.FILL, baseAp, tenths)
        Toast.makeText(getApplication(), "Dynamic Fill set to f/$baseAp.$tenths for ${targetSourceRatio.toInt()}:1 ratio", Toast.LENGTH_SHORT).show()
    }

    // Presets
    fun applyPreset(preset: PresetEntity) {
        // Clamp to the mounted lens: a preset recorded on another lens could
        // otherwise leave the meter in a state this lens cannot produce
        // (e.g. an 85mm preset focal on a 24-70mm zoom).
        val lens = _selectedLens.value
        val loFocal = minOf(lens.minFocalLength, lens.maxFocalLength)
        val hiFocal = maxOf(lens.minFocalLength, lens.maxFocalLength)
        val loAp = minOf(lens.maxAperture, lens.minAperture)
        val hiAp = maxOf(lens.maxAperture, lens.minAperture)
        _iso.value = preset.iso
        _aperture.value = preset.aperture.coerceIn(loAp, hiAp)
        _shutterSpeed.value = preset.shutterSpeed
        _focalLengthMm.value = preset.focalLengthMm.coerceIn(loFocal, hiFocal)
        _ndFilterStops.value = preset.ndFilterStops
        _priority.value = ExposurePriority.MANUAL_LOCK
        lightSensorManager.setManualEv100(preset.targetEv100)
        Toast.makeText(getApplication(), "Applied Preset: ${preset.name}", Toast.LENGTH_SHORT).show()
    }

    // The Create Preset dialog collects Shutter/Aperture/ISO/Focal and used
    // to throw them away, calling saveCurrentAsPreset (a snapshot of the
    // live meter) instead. This saves what the user actually typed.
    fun saveCustomPreset(name: String, category: String, description: String, shutter: String, aperture: Float, iso: Int, focal: Int) {
        viewModelScope.launch {
            val preset = PresetEntity(
                name = name,
                category = category,
                environmentDescription = description,
                targetEv100 = lightSensorManager.ev100.value,
                iso = iso,
                shutterSpeed = shutter,
                aperture = aperture,
                focalLengthMm = focal,
                ndFilterStops = _ndFilterStops.value,
                lightingRatioNotes = "Custom values entered in the Create Preset dialog",
                isCustom = true
            )
            repository.savePreset(preset)
            Toast.makeText(getApplication(), "Created Preset: $name", Toast.LENGTH_SHORT).show()
        }
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

    // Camera Bodies Management
    fun addCameraBody(
        brand: String,
        modelName: String,
        sensorFormat: String,
        cropFactor: Float,
        baseIso: Int,
        maxIso: Int,
        maxShutter: String,
        hasIbis: Boolean,
        serialNumber: String,
        weightGrams: Int,
        notes: String
    ) {
        viewModelScope.launch {
            val camera = com.example.data.local.CameraBodyEntity(
                brand = brand,
                modelName = modelName,
                sensorFormat = sensorFormat,
                cropFactor = cropFactor,
                baseIso = baseIso,
                maxIso = maxIso,
                maxShutterSpeed = maxShutter,
                hasIbis = hasIbis,
                serialNumber = serialNumber,
                weightGrams = weightGrams,
                inBag = true,
                notes = notes
            )
            repository.addCameraBody(camera)
            Toast.makeText(getApplication(), "Added Camera Body: $brand $modelName", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteCameraBody(camera: com.example.data.local.CameraBodyEntity) {
        viewModelScope.launch {
            repository.deleteCameraBody(camera)
        }
    }

    fun toggleCameraBodyInBag(camera: com.example.data.local.CameraBodyEntity) {
        viewModelScope.launch {
            repository.toggleCameraBodyInBag(camera.id, !camera.inBag)
        }
    }

    fun mountCameraBodyOnMeter(camera: com.example.data.local.CameraBodyEntity) {
        val model = camera.toCameraModel()
        selectBrand(camera.brand)
        selectCamera(model)
        Toast.makeText(getApplication(), "Mounted ${camera.brand} ${camera.modelName} on Meter", Toast.LENGTH_SHORT).show()
    }

    // Lenses Management
    fun addLens(
        brand: String,
        lensName: String,
        minFocal: Int,
        maxFocal: Int,
        maxAperture: Float,
        minAperture: Float,
        hasOis: Boolean,
        filterThreadMm: Int,
        serialNumber: String,
        weightGrams: Int,
        notes: String
    ) {
        viewModelScope.launch {
            // Normalize an inverted focal range instead of persisting a lens
            // that crashes setFocalLength's coerceIn when mounted.
            val loFocal = minOf(minFocal, maxFocal)
            val hiFocal = maxOf(minFocal, maxFocal)
            val lens = com.example.data.local.LensEntity(
                brand = brand,
                lensName = lensName,
                minFocalLength = loFocal,
                maxFocalLength = hiFocal,
                maxAperture = maxAperture,
                minAperture = minAperture,
                hasOis = hasOis,
                filterThreadMm = filterThreadMm,
                serialNumber = serialNumber,
                weightGrams = weightGrams,
                inBag = true,
                notes = notes
            )
            repository.addLens(lens)
            Toast.makeText(getApplication(), "Added Lens: $lensName", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteLens(lens: com.example.data.local.LensEntity) {
        viewModelScope.launch {
            repository.deleteLens(lens)
        }
    }

    fun toggleLensInBag(lens: com.example.data.local.LensEntity) {
        viewModelScope.launch {
            repository.toggleLensInBag(lens.id, !lens.inBag)
        }
    }

    fun mountLensOnMeter(lens: com.example.data.local.LensEntity) {
        val model = lens.toLensModel()
        selectBrand(lens.brand)
        selectLens(model)
        Toast.makeText(getApplication(), "Mounted ${lens.lensName} on Meter", Toast.LENGTH_SHORT).show()
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

    // Portrait Multi-Light Ratio Calculator
    fun updatePortraitLightReading(role: com.example.data.model.LightRole, fStop: Float, tenths: Int) {
        val current = _portraitCalculation.value
        val updated = when (role) {
            com.example.data.model.LightRole.KEY -> current.copy(keyLight = current.keyLight.copy(fStop = fStop, fractionalTenths = tenths.coerceIn(0, 9)))
            com.example.data.model.LightRole.FILL -> current.copy(fillLight = current.fillLight.copy(fStop = fStop, fractionalTenths = tenths.coerceIn(0, 9)))
            com.example.data.model.LightRole.RIM -> current.copy(hairLight = current.hairLight.copy(fStop = fStop, fractionalTenths = tenths.coerceIn(0, 9)))
            com.example.data.model.LightRole.BACKGROUND -> current.copy(backgroundLight = (current.backgroundLight ?: com.example.data.model.LightReading(com.example.data.model.LightRole.BACKGROUND, "Background Light")).copy(fStop = fStop, fractionalTenths = tenths.coerceIn(0, 9)))
        }
        _portraitCalculation.value = updated
    }

    fun togglePortraitLightEnabled(role: com.example.data.model.LightRole) {
        val current = _portraitCalculation.value
        val updated = when (role) {
            com.example.data.model.LightRole.KEY -> current.copy(keyLight = current.keyLight.copy(isEnabled = !current.keyLight.isEnabled))
            com.example.data.model.LightRole.FILL -> current.copy(fillLight = current.fillLight.copy(isEnabled = !current.fillLight.isEnabled))
            com.example.data.model.LightRole.RIM -> current.copy(hairLight = current.hairLight.copy(isEnabled = !current.hairLight.isEnabled))
            com.example.data.model.LightRole.BACKGROUND -> {
                val bg = current.backgroundLight ?: com.example.data.model.LightReading(com.example.data.model.LightRole.BACKGROUND, "Background Light")
                current.copy(backgroundLight = bg.copy(isEnabled = !bg.isEnabled))
            }
        }
        _portraitCalculation.value = updated
    }

    fun applyPortraitPreset(
        keyF: Float,
        fillF: Float,
        fillTenths: Int,
        hairF: Float,
        hairTenths: Int,
        bgF: Float,
        bgEnabled: Boolean,
        presetName: String
    ) {
        val current = _portraitCalculation.value
        _portraitCalculation.value = current.copy(
            keyLight = current.keyLight.copy(fStop = keyF, fractionalTenths = 0, isEnabled = true),
            fillLight = current.fillLight.copy(fStop = fillF, fractionalTenths = fillTenths, isEnabled = true),
            hairLight = current.hairLight.copy(fStop = hairF, fractionalTenths = hairTenths, isEnabled = true),
            backgroundLight = (current.backgroundLight ?: com.example.data.model.LightReading(com.example.data.model.LightRole.BACKGROUND, "Background Light"))
                .copy(fStop = bgF, fractionalTenths = 0, isEnabled = bgEnabled)
        )
        Toast.makeText(getApplication(), "Applied Lighting Preset: $presetName", Toast.LENGTH_SHORT).show()
    }

    fun importCurrentExposureToPortraitLight(role: com.example.data.model.LightRole) {
        val currentAp = _aperture.value
        val standardAps = ExposureStandards.STANDARD_APERTURES
        val closestAp = standardAps.minByOrNull { kotlin.math.abs(it - currentAp) } ?: currentAp
        updatePortraitLightReading(role, closestAp, 0)
        Toast.makeText(getApplication(), "Imported f/$closestAp to ${role.title}", Toast.LENGTH_SHORT).show()
    }

    fun applyWorkingApertureToCamera() {
        val workingF = _portraitCalculation.value.workingCameraAperture
        _aperture.value = workingF
        _priority.value = ExposurePriority.APERTURE_PRIORITY
        recalculateExposure()
        Toast.makeText(getApplication(), "Set Camera Lens to Calculated f/$workingF", Toast.LENGTH_SHORT).show()
    }

    // There is NO cloud backend in this build: the old triggerCloudSync
    // only delayed 1.2s, marked every log synced, and this toast announced
    // "Metadata and gear synced to cloud" for an upload that never
    // happened. Sync is local-only until a real backend exists.
    fun triggerSync() {
        Toast.makeText(getApplication(), "Cloud sync isn't available in this build — everything is stored on this device.", Toast.LENGTH_LONG).show()
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
                // Was: the error was appended as a MODEL message and then
                // resent verbatim as conversation history on every later
                // send, polluting the API context. Failures are transient
                // UI feedback now and never enter the history.
                Toast.makeText(getApplication(), "AI reply failed: ${err.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
