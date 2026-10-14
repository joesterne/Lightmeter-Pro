package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ui.components.CameraViewfinderOverlay
import com.example.ui.components.ExposureDialControl
import com.example.ui.components.HistogramView
import com.example.ui.components.QuickAccessBar

@Composable
fun LiveMeterScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lux by viewModel.lightSensorManager.lux.collectAsState()
    val ev100 by viewModel.lightSensorManager.ev100.collectAsState()
    val isSensorAvailable by viewModel.lightSensorManager.isSensorAvailable.collectAsState()
    val isLiveMetering by viewModel.isMeteringLive.collectAsState()

    val isCameraViewfinderActive by viewModel.isCameraViewfinderActive.collectAsState()
    val cameraData by viewModel.cameraAnalyzer.luminanceData.collectAsState()

    val brands = viewModel.brands
    val selectedBrand by viewModel.selectedBrand.collectAsState()
    val cameras by viewModel.availableCameras.collectAsState()
    val selectedCamera by viewModel.selectedCamera.collectAsState()
    val lenses by viewModel.availableLenses.collectAsState()
    val selectedLens by viewModel.selectedLens.collectAsState()
    val focalLength by viewModel.focalLengthMm.collectAsState()

    val priority by viewModel.priority.collectAsState()
    val iso by viewModel.iso.collectAsState()
    val aperture by viewModel.aperture.collectAsState()
    val shutterSpeed by viewModel.shutterSpeed.collectAsState()
    val ndStops by viewModel.ndFilterStops.collectAsState()

    val isRedNightMode by viewModel.isRedNightMode.collectAsState()
    val isBatterySaver by viewModel.isBatterySaver.collectAsState()
    val syncState by viewModel.syncState.collectAsState()

    val recommendedShutter = viewModel.getRecommendedHandheldShutter()
    val isRisk = viewModel.isHandheldShutterRisk()

    var showBrandMenu by remember { mutableStateOf(false) }
    var showCameraMenu by remember { mutableStateOf(false) }
    var showLensMenu by remember { mutableStateOf(false) }
    var showSavePresetDialog by remember { mutableStateOf(false) }

    // Dialog state for custom preset saving
    var presetName by remember { mutableStateOf("") }
    var presetCategory by remember { mutableStateOf("Portrait") }
    var presetDesc by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .testTag("live_meter_screen")
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Quick Access Bar (Always on Top for high-pressure shooting)
        QuickAccessBar(
            priority = priority,
            onPriorityChange = { viewModel.setPriority(it) },
            currentIso = iso,
            onIsoSelect = { viewModel.setIso(it) },
            currentNdStops = ndStops,
            onNdSelect = { viewModel.setNdFilterStops(it) },
            isRedNightMode = isRedNightMode,
            onToggleRedNightMode = { viewModel.toggleRedNightMode() },
            isBatterySaver = isBatterySaver,
            onToggleBatterySaver = { viewModel.toggleBatterySaver() },
            syncState = syncState,
            onTriggerSync = { viewModel.triggerSync() }
        )

        // Main Luminous Incident Dome Readout
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF152033),
                            Color(0xFF0C1322)
                        )
                    )
                )
                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                // Top row: Metering Mode (Live vs Manual)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isLiveMetering) Color(0xFF10B981) else Color(0xFFF59E0B))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isLiveMetering) "INCIDENT SENSOR LIVE" else "MANUAL CALIBRATION",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(
                        onClick = { viewModel.toggleLiveMetering(!isLiveMetering) },
                        modifier = Modifier.testTag("toggle_live_meter_button")
                    ) {
                        Text(
                            text = if (isLiveMetering) "Freeze Reading" else "Resume Live",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Giant Digital EV Readout
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "EV",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = String.format(java.util.Locale.US, "%.1f", ev100),
                        style = MaterialTheme.typography.displayMedium.copy(fontSize = 54.sp),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "@ ISO 100",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }

                Text(
                    text = "${String.format(java.util.Locale.US, "%,.0f", lux)} Lux • ${String.format(java.util.Locale.US, "%,.0f", lux / 10.764f)} Foot-Candles",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.secondary
                )

                if (!isSensorAvailable || !isLiveMetering) {
                    // Manual EV slider slider when in simulated or freeze mode
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Simulate / Calibrate EV", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("EV ${String.format(java.util.Locale.US, "%.1f", ev100)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = ev100,
                            onValueChange = { viewModel.setManualEv(it) },
                            valueRange = -4f..18f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("manual_ev_slider")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Toggle Camera Viewfinder Button
                Button(
                    onClick = { viewModel.toggleCameraViewfinder() },
                    modifier = Modifier
                        .testTag("toggle_camera_viewfinder_button")
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCameraViewfinderActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera Viewfinder",
                        tint = if (isCameraViewfinderActive) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCameraViewfinderActive) "Hide Camera Viewfinder" else "Open Camera Viewfinder & Real-Time Histogram",
                        fontWeight = FontWeight.Bold,
                        color = if (isCameraViewfinderActive) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Live CameraX Preview Stream Viewfinder with Real-Time Luminance Histogram Overlay
        if (isCameraViewfinderActive) {
            CameraViewfinderOverlay(
                analyzer = viewModel.cameraAnalyzer,
                onTakeReading = { spotEv, spotLux ->
                    viewModel.applyCameraSpotReading(spotEv, spotLux)
                },
                onClose = { viewModel.toggleCameraViewfinder(false) }
            )
        }

        // Camera & Lens Selection Box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CAMERA & LENS CALIBRATION",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = selectedCamera.sensorFormat.displayName,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Brand & Camera Selectors
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Brand Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { showBrandMenu = true },
                        modifier = Modifier.testTag("select_brand_button").fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(selectedBrand, maxLines = 1, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ExpandMore, contentDescription = "Select Brand", modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(expanded = showBrandMenu, onDismissRequest = { showBrandMenu = false }) {
                        brands.forEach { brand ->
                            DropdownMenuItem(
                                text = { Text(brand) },
                                onClick = {
                                    viewModel.selectBrand(brand)
                                    showBrandMenu = false
                                }
                            )
                        }
                    }
                }

                // Camera Dropdown
                Box(modifier = Modifier.weight(1.3f)) {
                    OutlinedButton(
                        onClick = { showCameraMenu = true },
                        modifier = Modifier.testTag("select_camera_button").fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(selectedCamera.name, maxLines = 1, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ExpandMore, contentDescription = "Select Camera", modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(expanded = showCameraMenu, onDismissRequest = { showCameraMenu = false }) {
                        cameras.forEach { cam ->
                            DropdownMenuItem(
                                text = { Text("${cam.name} (${cam.sensorFormat.displayName})") },
                                onClick = {
                                    viewModel.selectCamera(cam)
                                    showCameraMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lens Selector
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { showLensMenu = true },
                    modifier = Modifier.testTag("select_lens_button").fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Lens: ${selectedLens.name}", maxLines = 1, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ExpandMore, contentDescription = "Select Lens", modifier = Modifier.size(16.dp))
                }
                DropdownMenu(expanded = showLensMenu, onDismissRequest = { showLensMenu = false }) {
                    lenses.forEach { lens ->
                        DropdownMenuItem(
                            text = { Text("${lens.name} (${lens.focalLengthDisplay})") },
                            onClick = {
                                viewModel.selectLens(lens)
                                showLensMenu = false
                            }
                        )
                    }
                }
            }

            // Focal Length Slider (if zoom lens or adjustment)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Focal Length: ${focalLength}mm",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Reciprocal: $recommendedShutter",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Slider(
                value = focalLength.toFloat(),
                onValueChange = { viewModel.setFocalLength(it.toInt()) },
                valueRange = selectedLens.minFocalLength.toFloat()..selectedLens.maxFocalLength.toFloat().coerceAtLeast(selectedLens.minFocalLength + 1f),
                steps = if (selectedLens.maxFocalLength > selectedLens.minFocalLength) (selectedLens.maxFocalLength - selectedLens.minFocalLength) / 5 else 0,
                modifier = Modifier.testTag("focal_length_slider"),
                colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.secondary)
            )
        }

        // Tactile Exposure Dial Controls (Shutter, Aperture, ISO)
        ExposureDialControl(
            shutterSpeed = shutterSpeed,
            onShutterStep = { viewModel.stepShutter(it) },
            aperture = aperture,
            onApertureStep = { viewModel.stepAperture(it) },
            iso = iso,
            onIsoStep = { viewModel.stepIso(it) },
            priority = priority,
            reciprocalWarning = isRisk,
            recommendedHandheldShutter = recommendedShutter
        )

        // Real-Time Histogram Display (Using CameraX Luminance Stream when Viewfinder is Active)
        HistogramView(
            ev100 = if (isCameraViewfinderActive) cameraData.estimatedEv100 else ev100,
            targetEv = 12f,
            customBins = if (isCameraViewfinderActive) cameraData.histogramBins else null
        )

        // Action Buttons: Export Camera Settings & Save Preset
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.exportSettingsCard(context) },
                modifier = Modifier.testTag("export_settings_button").weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Share, contentDescription = "Export Settings", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export Slate", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { showSavePresetDialog = true },
                modifier = Modifier.testTag("save_preset_button").weight(1f)
            ) {
                Icon(Icons.Default.BookmarkAdd, contentDescription = "Save Preset", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Preset", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Save Preset Dialog
    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = { Text("Save Shooting Preset") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Save current exposure ($shutterSpeed, f/$aperture, ISO $iso @ ${focalLength}mm) as a reusable shooting preset.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = presetName,
                        onValueChange = { presetName = it },
                        label = { Text("Preset Name (e.g. Sunset Backlit)") },
                        modifier = Modifier.testTag("preset_name_input").fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = presetCategory,
                        onValueChange = { presetCategory = it },
                        label = { Text("Category (e.g. Portrait, Studio)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = presetDesc,
                        onValueChange = { presetDesc = it },
                        label = { Text("Environment Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (presetName.isNotBlank()) {
                            viewModel.saveCurrentAsPreset(presetName, presetCategory, presetDesc)
                            showSavePresetDialog = false
                            presetName = ""
                            presetDesc = ""
                        }
                    },
                    modifier = Modifier.testTag("confirm_save_preset_button")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
