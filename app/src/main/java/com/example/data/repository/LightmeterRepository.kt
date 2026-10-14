package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.DefaultData
import com.example.data.local.ExposureLogEntity
import com.example.data.local.GearEntity
import com.example.data.local.PresetEntity
import com.example.data.model.CameraModel
import com.example.data.model.LensModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SyncState {
    SYNCED,
    SYNCING,
    OFFLINE_ONLY
}

class LightmeterRepository(private val context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val gearDao = database.gearDao()
    private val presetDao = database.presetDao()
    private val logDao = database.exposureLogDao()

    private val _syncState = MutableStateFlow(SyncState.SYNCED)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    init {
        // Pre-populate if empty
        CoroutineScope(Dispatchers.IO).launch {
            if (presetDao.getCount() == 0) {
                DefaultData.INITIAL_PRESETS.forEach { presetDao.insertPreset(it) }
                DefaultData.INITIAL_GEAR.forEach { gearDao.insertGear(it) }
            }
        }
    }

    // Presets
    fun getPresets(): Flow<List<PresetEntity>> = presetDao.getAllPresets()
    suspend fun savePreset(preset: PresetEntity) = presetDao.insertPreset(preset)
    suspend fun updatePreset(preset: PresetEntity) = presetDao.updatePreset(preset)
    suspend fun deletePreset(preset: PresetEntity) = presetDao.deletePreset(preset)

    // Gear
    fun getAllGear(): Flow<List<GearEntity>> = gearDao.getAllGear()
    suspend fun addGear(gear: GearEntity) = gearDao.insertGear(gear)
    suspend fun updateGear(gear: GearEntity) = gearDao.updateGear(gear)
    suspend fun deleteGear(gear: GearEntity) = gearDao.deleteGear(gear)
    suspend fun toggleGearInBag(id: Long, inBag: Boolean) = gearDao.toggleInBag(id, inBag)

    // Logs
    fun getAllLogs(): Flow<List<ExposureLogEntity>> = logDao.getAllLogs()
    suspend fun saveExposureLog(log: ExposureLogEntity) = logDao.insertLog(log)
    suspend fun deleteExposureLog(log: ExposureLogEntity) = logDao.deleteLog(log)

    // Cloud Sync
    suspend fun triggerCloudSync() {
        _syncState.value = SyncState.SYNCING
        kotlinx.coroutines.delay(1200) // Simulated robust background cloud sync
        logDao.markAllSynced()
        _lastSyncTimestamp.value = System.currentTimeMillis()
        _syncState.value = SyncState.SYNCED
    }

    fun setOfflineMode(isOffline: Boolean) {
        _syncState.value = if (isOffline) SyncState.OFFLINE_ONLY else SyncState.SYNCED
    }

    // Camera and Lens catalog
    fun getBrands(): List<String> = DefaultData.BRANDS

    fun getCamerasForBrand(brand: String): List<CameraModel> {
        return DefaultData.CAMERAS.filter { it.brand.equals(brand, ignoreCase = true) }
    }

    fun getLensesForBrand(brand: String): List<LensModel> {
        val directMatches = DefaultData.LENSES.filter { it.brand.equals(brand, ignoreCase = true) }
        return if (directMatches.isNotEmpty()) directMatches else DefaultData.LENSES
    }

    /**
     * Formats camera settings into a professional production card ready to export
     */
    fun formatExportSettings(
        camera: CameraModel,
        lens: LensModel,
        focalLengthMm: Int,
        ev100: Float,
        lux: Float,
        iso: Int,
        shutterSpeed: String,
        aperture: Float,
        ndStops: Int,
        reciprocalRuleShutter: String,
        lightingNotes: String = ""
    ): String {
        return buildString {
            appendLine("=== LIGHTMETER PRO — CAMERA READY EXPOSURE SLATE ===")
            appendLine("Timestamp: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())}")
            appendLine("Camera Body: ${camera.brand} ${camera.name} (${camera.sensorFormat.displayName})")
            appendLine("Lens: ${lens.name} @ ${focalLengthMm}mm")
            appendLine("Ambient Luminance: ${String.format(java.util.Locale.US, "%.1f", lux)} Lux")
            appendLine("Base Exposure Value: EV100 ${String.format(java.util.Locale.US, "%.1f", ev100)}")
            if (ndStops > 0) {
                appendLine("Neutral Density Filter: ND${1 shl ndStops} (-$ndStops Stops)")
            }
            appendLine("-------------------------------------------------")
            appendLine("EXPOSURE TRIANGLE:")
            appendLine("  • SHUTTER SPEED : $shutterSpeed")
            appendLine("  • APERTURE      : f/$aperture")
            appendLine("  • ISO           : $iso")
            appendLine("-------------------------------------------------")
            appendLine("Handheld Reciprocal Threshold : $reciprocalRuleShutter")
            if (lightingNotes.isNotBlank()) {
                appendLine("Lighting Notes : $lightingNotes")
            }
            appendLine("=================================================")
        }
    }
}
