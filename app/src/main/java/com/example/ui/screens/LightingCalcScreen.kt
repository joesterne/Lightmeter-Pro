package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.model.LightReading
import com.example.data.model.LightRole
import com.example.data.model.PortraitRatioCalculation
import com.example.data.model.StudioLight
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// Reusable static lists to eliminate per-recomposition allocations
private val STANDARD_APERTURES = floatArrayOf(1.4f, 2.0f, 2.8f, 4.0f, 5.6f, 8.0f, 11.0f, 16.0f, 22.0f)

private val STROBE_POWERS = listOf(
    1.0f to "1/1",
    0.5f to "1/2",
    0.25f to "1/4",
    0.125f to "1/8",
    0.0625f to "1/16",
    0.03125f to "1/32",
    0.015625f to "1/64"
)

@Composable
fun LightingCalcScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val portraitCalc by viewModel.portraitCalculation.collectAsState()
    val studioSetup by viewModel.studioSetup.collectAsState()
    val iso by viewModel.iso.collectAsState()
    val currentAperture by viewModel.aperture.collectAsState()

    Column(
        modifier = modifier
            .testTag("lighting_calc_screen")
            .fillMaxSize()
    ) {
        // Tab Row: Portrait Ratios vs Strobe Setup
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier.testTag("tab_portrait_ratios"),
                text = {
                    Text(
                        text = "PORTRAIT RATIOS",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        letterSpacing = 0.5.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier.testTag("tab_strobe_setup"),
                text = {
                    Text(
                        text = "STROBE / GN SETUP",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        letterSpacing = 0.5.sp
                    )
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (selectedTab == 0) {
                // Portrait Ratio Calculator
                PortraitRatioCalculatorSection(
                    calc = portraitCalc,
                    currentCameraAperture = currentAperture,
                    onUpdateReading = { role, fStop, tenths ->
                        viewModel.updatePortraitLightReading(role, fStop, tenths)
                    },
                    onToggleEnabled = { role ->
                        viewModel.togglePortraitLightEnabled(role)
                    },
                    onImportCurrentMeter = { role ->
                        viewModel.importCurrentExposureToPortraitLight(role)
                    },
                    onApplyWorkingAperture = {
                        viewModel.applyWorkingApertureToCamera()
                    },
                    onApplyPreset = { keyF, fillF, fillTenths, hairF, hairTenths, bgF, bgEnabled, name ->
                        viewModel.applyPortraitPreset(
                            keyF = keyF,
                            fillF = fillF,
                            fillTenths = fillTenths,
                            hairF = hairF,
                            hairTenths = hairTenths,
                            bgF = bgF,
                            bgEnabled = bgEnabled,
                            presetName = name
                        )
                    }
                )
            } else {
                // Strobe Guide Number & Power Fraction Section
                StrobeGuideNumberSection(
                    setup = studioSetup,
                    iso = iso,
                    onPowerChange = { id, frac -> viewModel.updateStudioLightPower(id, frac) },
                    onDistanceChange = { id, d -> viewModel.updateStudioLightDistance(id, d) },
                    onToggleEnabled = { id -> viewModel.toggleStudioLight(id) }
                )
            }
        }
    }
}

/**
 * Multi-Light Ratio Calculator section for studio portraiture.
 */
@Composable
private fun PortraitRatioCalculatorSection(
    calc: PortraitRatioCalculation,
    currentCameraAperture: Float,
    onUpdateReading: (LightRole, Float, Int) -> Unit,
    onToggleEnabled: (LightRole) -> Unit,
    onImportCurrentMeter: (LightRole) -> Unit,
    onApplyWorkingAperture: () -> Unit,
    onApplyPreset: (Float, Float, Int, Float, Int, Float, Boolean, String) -> Unit
) {
    // Top Ratio Dashboard Card
    PortraitRatioDashboardCard(
        calc = calc,
        currentCameraAperture = currentCameraAperture,
        onApplyWorkingAperture = onApplyWorkingAperture
    )

    // Visual Studio Diagram
    StudioLightingDiagramCard(calc = calc)

    // Style Interpretation & Hair Warning Banner
    StyleAndWarningsCard(calc = calc)

    // Quick Portrait Lighting Presets
    PortraitPresetsRow(onApplyPreset = onApplyPreset)

    // Key Light Input Card
    LightReadingInputCard(
        title = "KEY LIGHT (Main Modeling)",
        subtitle = "Primary directional light defining facial shape and eye catchlight",
        reading = calc.keyLight,
        accentColor = Color(0xFFF59E0B),
        onUpdateReading = { f, tenths -> onUpdateReading(LightRole.KEY, f, tenths) },
        onToggleEnabled = { onToggleEnabled(LightRole.KEY) },
        onImportCurrentMeter = { onImportCurrentMeter(LightRole.KEY) },
        isKeyLight = true
    )

    // Fill Light Input Card
    LightReadingInputCard(
        title = "FILL LIGHT (Shadow Control)",
        subtitle = "Soft frontal or on-axis fill controlling shadow contrast depth",
        reading = calc.fillLight,
        accentColor = Color(0xFF38BDF8),
        stopDifference = calc.keyFillStopDiff,
        isRelative = true,
        relativeToName = "Key Light",
        onUpdateReading = { f, tenths -> onUpdateReading(LightRole.FILL, f, tenths) },
        onToggleEnabled = { onToggleEnabled(LightRole.FILL) },
        onImportCurrentMeter = { onImportCurrentMeter(LightRole.FILL) }
    )

    // Hair / Rim Light Input Card
    LightReadingInputCard(
        title = "HAIR / RIM LIGHT (Edge Separation)",
        subtitle = "Back/overhead kicker separating hair & shoulders from background",
        reading = calc.hairLight,
        accentColor = Color(0xFFEC4899),
        stopDifference = calc.hairKeyStopDiff,
        isRelative = true,
        relativeToName = "Key Light",
        hairToneAdvice = true,
        onUpdateReading = { f, tenths -> onUpdateReading(LightRole.RIM, f, tenths) },
        onToggleEnabled = { onToggleEnabled(LightRole.RIM) },
        onImportCurrentMeter = { onImportCurrentMeter(LightRole.RIM) }
    )

    // Background Light Input Card
    LightReadingInputCard(
        title = "BACKGROUND LIGHT (Depth & Tone)",
        subtitle = "Illuminates backdrop: +1 EV for pure high-key white, -2 EV for slate",
        reading = calc.backgroundLight,
        accentColor = Color(0xFFA855F7),
        stopDifference = calc.backgroundKeyStopDiff,
        isRelative = true,
        relativeToName = "Key Light",
        onUpdateReading = { f, tenths -> onUpdateReading(LightRole.BACKGROUND, f, tenths) },
        onToggleEnabled = { onToggleEnabled(LightRole.BACKGROUND) },
        onImportCurrentMeter = { onImportCurrentMeter(LightRole.BACKGROUND) }
    )
}

/**
 * Summary card showing output ratios, total scene ratio, and recommended camera aperture.
 */
@Composable
private fun PortraitRatioDashboardCard(
    calc: PortraitRatioCalculation,
    currentCameraAperture: Float,
    onApplyWorkingAperture: () -> Unit
) {
    Card(
        modifier = Modifier
            .testTag("portrait_ratio_dashboard")
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "STUDIO LIGHTING RATIOS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "${calc.syncSpeed} @ ISO ${calc.cameraIso}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary 3 Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Key : Fill Source Ratio
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SOURCE RATIO",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = calc.sourceRatioString,
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Key alone vs Fill",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Total Highlight : Shadow Ratio (Standard Portraiture)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SCENE CONTRAST",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = calc.totalToShadowRatioString,
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                    Text(
                        text = "(Key+Fill) : Fill",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Hair / Rim Delta
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "HAIR / RIM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (calc.hairLight.isEnabled) {
                            val diff = calc.hairKeyStopDiff
                            val prefix = if (diff >= 0) "+" else ""
                            "$prefix${String.format(java.util.Locale.US, "%.1f", diff)} EV"
                        } else "Off",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEC4899)
                    )
                    Text(
                        text = "Relative to Key",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)))
            Spacer(modifier = Modifier.height(12.dp))

            // Recommended Working Camera Aperture & Apply Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CALCULATED CAMERA APERTURE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "f/${calc.workingCameraAperture}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "√(f_key² + f_fill²)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                Button(
                    onClick = onApplyWorkingAperture,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("apply_working_aperture_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Set Aperture",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Set Camera f/${calc.workingCameraAperture}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Top-down visual studio diagram showing subject, key (45°), fill (-30°), hair (135°), and background.
 */
@Composable
private fun StudioLightingDiagramCard(calc: PortraitRatioCalculation) {
    val nosePath = remember { Path() }

    Card(
        modifier = Modifier
            .testTag("studio_diagram_card")
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF090D14)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STUDIO PLACEMENT DIAGRAM (TOP-DOWN)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Overhead View",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Canvas drawing the studio
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val cx = size.width / 2f
                val cy = size.height / 2f - 10f
                val radius = 62.dp.toPx()

                // Backdrop line at top
                val backdropY = 18.dp.toPx()
                drawLine(
                    color = if (calc.backgroundLight.isEnabled) Color(0xFFA855F7) else Color(0xFF334155),
                    start = Offset(cx - 100.dp.toPx(), backdropY),
                    end = Offset(cx + 100.dp.toPx(), backdropY),
                    strokeWidth = 3.dp.toPx()
                )

                // Background Light indicator
                if (calc.backgroundLight.isEnabled) {
                    drawCircle(
                        color = Color(0xFFA855F7),
                        radius = 8.dp.toPx(),
                        center = Offset(cx, backdropY + 16.dp.toPx())
                    )
                }

                // Orbit ring
                drawCircle(
                    color = Color(0xFF1E293B),
                    radius = radius,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Subject Head (Center)
                drawCircle(
                    color = Color(0xFFE2E8F0),
                    radius = 16.dp.toPx(),
                    center = Offset(cx, cy)
                )

                // Nose direction (pointing downward toward camera) - reused path
                nosePath.reset()
                nosePath.moveTo(cx - 4.dp.toPx(), cy + 12.dp.toPx())
                nosePath.lineTo(cx, cy + 22.dp.toPx())
                nosePath.lineTo(cx + 4.dp.toPx(), cy + 12.dp.toPx())
                nosePath.close()
                drawPath(nosePath, Color(0xFFF59E0B))

                // Camera at bottom
                val cameraY = size.height - 18.dp.toPx()
                drawCircle(
                    color = Color(0xFF64748B),
                    radius = 10.dp.toPx(),
                    center = Offset(cx, cameraY)
                )

                // Key Light (+45° right, below horizontal)
                val keyAngleRad = Math.toRadians(55.0)
                val keyX = (cx + radius * cos(keyAngleRad)).toFloat()
                val keyY = (cy + radius * sin(keyAngleRad)).toFloat()
                if (calc.keyLight.isEnabled) {
                    drawLine(
                        color = Color(0xFFF59E0B).copy(alpha = 0.45f),
                        start = Offset(keyX, keyY),
                        end = Offset(cx, cy),
                        strokeWidth = 3.dp.toPx()
                    )
                    drawCircle(
                        color = Color(0xFFF59E0B),
                        radius = 11.dp.toPx(),
                        center = Offset(keyX, keyY)
                    )
                }

                // Fill Light (-40° left, below horizontal)
                val fillX = (cx - radius * cos(Math.toRadians(55.0))).toFloat()
                val fillY = (cy + radius * sin(Math.toRadians(55.0))).toFloat()
                if (calc.fillLight.isEnabled) {
                    drawLine(
                        color = Color(0xFF38BDF8).copy(alpha = 0.45f),
                        start = Offset(fillX, fillY),
                        end = Offset(cx, cy),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawCircle(
                        color = Color(0xFF38BDF8),
                        radius = 9.dp.toPx(),
                        center = Offset(fillX, fillY)
                    )
                }

                // Hair / Rim Light (behind subject, at top-left ~ 135°)
                val hairX = (cx - radius * 0.75f * cos(Math.toRadians(45.0))).toFloat()
                val hairY = (cy - radius * 0.75f * sin(Math.toRadians(45.0))).toFloat()
                if (calc.hairLight.isEnabled) {
                    drawLine(
                        color = Color(0xFFEC4899).copy(alpha = 0.45f),
                        start = Offset(hairX, hairY),
                        end = Offset(cx, cy),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawCircle(
                        color = Color(0xFFEC4899),
                        radius = 8.dp.toPx(),
                        center = Offset(hairX, hairY)
                    )
                }
            }

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendIndicator(color = Color(0xFFF59E0B), text = "Key: ${calc.keyLight.displayString}")
                LegendIndicator(color = Color(0xFF38BDF8), text = "Fill: ${if (calc.fillLight.isEnabled) calc.fillLight.displayString else "Off"}")
                LegendIndicator(color = Color(0xFFEC4899), text = "Hair: ${if (calc.hairLight.isEnabled) calc.hairLight.displayString else "Off"}")
                LegendIndicator(color = Color(0xFFA855F7), text = "Backdrop")
            }
        }
    }
}

@Composable
private fun LegendIndicator(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Style guidance & hair light warning alert banner.
 */
@Composable
private fun StyleAndWarningsCard(calc: PortraitRatioCalculation) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Photographic Style Interpretation
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Style Info",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "PORTRAIT LIGHTING STYLE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = calc.portraitStyleInterpretation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Hair Light Clipping Warning (if any)
        calc.hairLightWarning?.let { warningText ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF3B1E08)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = warningText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFDE68A),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Quick portrait preset chips.
 */
@Composable
private fun PortraitPresetsRow(
    onApplyPreset: (Float, Float, Int, Float, Int, Float, Boolean, String) -> Unit
) {
    Column {
        Text(
            text = "CLASSICAL PORTRAIT PRESETS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PresetChip(
                name = "1:1 Beauty / Flat",
                ratioDesc = "Shadowless High-Key",
                onClick = { onApplyPreset(8.0f, 8.0f, 0, 8.0f, 0, 11.0f, true, "1:1 Beauty") }
            )
            PresetChip(
                name = "2:1 Soft Commercial",
                ratioDesc = "1 Stop Fill (-1 EV)",
                onClick = { onApplyPreset(8.0f, 5.6f, 0, 8.0f, 3, 5.6f, true, "2:1 Commercial") }
            )
            PresetChip(
                name = "3:1 Classic Rembrandt",
                ratioDesc = "1.5 Stops Fill",
                onClick = { onApplyPreset(8.0f, 4.0f, 5, 8.0f, 0, 4.0f, false, "3:1 Rembrandt") }
            )
            PresetChip(
                name = "4:1 Editorial Dramatic",
                ratioDesc = "2 Stops Fill (-2 EV)",
                onClick = { onApplyPreset(8.0f, 4.0f, 0, 8.0f, 5, 4.0f, false, "4:1 Editorial") }
            )
            PresetChip(
                name = "8:1 Film Noir",
                ratioDesc = "3 Stops Fill (-3 EV)",
                onClick = { onApplyPreset(8.0f, 2.8f, 0, 5.6f, 0, 2.8f, false, "8:1 Film Noir") }
            )
            PresetChip(
                name = "Rim / Edge Kicker",
                ratioDesc = "Strong Backlit Edge",
                onClick = { onApplyPreset(5.6f, 2.8f, 0, 11.0f, 0, 4.0f, false, "Rim Kicker") }
            )
        }
    }
}

@Composable
private fun PresetChip(
    name: String,
    ratioDesc: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = ratioDesc,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Card for inputting meter readings (f-stop base + fractional tenths) for a specific light role.
 */
@Composable
private fun LightReadingInputCard(
    title: String,
    subtitle: String,
    reading: LightReading,
    accentColor: Color,
    stopDifference: Float? = null,
    isRelative: Boolean = false,
    relativeToName: String = "Key Light",
    hairToneAdvice: Boolean = false,
    isKeyLight: Boolean = false,
    onUpdateReading: (Float, Int) -> Unit,
    onToggleEnabled: () -> Unit,
    onImportCurrentMeter: () -> Unit
) {
    Card(
        modifier = Modifier
            .testTag("light_reading_card_${reading.role.name.lowercase()}")
            .fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reading.isEnabled) MaterialTheme.colorScheme.surface else Color(0xFF0F141C)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (reading.isEnabled) accentColor.copy(alpha = 0.5f) else Color(0xFF1E293B)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Light Name, Role, Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (reading.isEnabled) accentColor else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (reading.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!isKeyLight) {
                    Switch(
                        checked = reading.isEnabled,
                        onCheckedChange = { onToggleEnabled() },
                        colors = SwitchDefaults.colors(checkedThumbColor = accentColor)
                    )
                }
            }

            if (reading.isEnabled) {
                Spacer(modifier = Modifier.height(12.dp))

                // Current Light Output & Difference Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = reading.displayString,
                            style = MaterialTheme.typography.headlineMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor
                        )
                        if (reading.fractionalTenths > 0) {
                            Text(
                                text = " (f/${String.format(java.util.Locale.US, "%.2f", reading.effectiveFStop)})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                            )
                        }
                    }

                    if (isRelative && stopDifference != null) {
                        val diff = stopDifference
                        val prefix = if (diff >= 0) "+" else ""
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(accentColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$prefix${String.format(java.util.Locale.US, "%.1f", diff)} EV vs $relativeToName",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                    }
                }

                // Base Aperture Chips Row
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "BASE f-STOP METER READING:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    STANDARD_APERTURES.forEach { ap ->
                        val isSelected = abs(reading.fStop - ap) < 0.1f
                        val label = if (ap % 1f == 0f) "${ap.toInt()}" else "$ap"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) accentColor else Color(0xFF1E293B))
                                .clickable { onUpdateReading(ap, reading.fractionalTenths) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "f/$label",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.Black else Color.White
                            )
                        }
                    }
                }

                // Fractional Tenths of a Stop (0 to 9 tenths)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FRACTIONAL TENTHS: .${reading.fractionalTenths} Stop",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "+${(reading.fractionalTenths * 0.1f).let { String.format(java.util.Locale.US, "%.1f", it) }} EV",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = accentColor
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    (0..9).forEach { tenths ->
                        val isSelected = reading.fractionalTenths == tenths
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) accentColor else Color(0xFF16202E))
                                .clickable { onUpdateReading(reading.fStop, tenths) }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ".$tenths",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontFamily = FontFamily.Monospace,
                                color = if (isSelected) Color.Black else Color(0xFF94A3B8)
                            )
                        }
                        if (tenths < 9) Spacer(modifier = Modifier.width(2.dp))
                    }
                }

                // Import Current Live Meter Reading Button
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onImportCurrentMeter,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("import_meter_to_${reading.role.name.lowercase()}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Meter Icon",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Import Current Meter Reading",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                // Hair Tone Advice note
                if (hairToneAdvice) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "💡 Guideline: Blonde / Light hair: -0.5 to 0.0 EV vs Key. Brunette / Dark hair: +0.5 to +1.5 EV vs Key.",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = Color(0xFFFDE68A)
                    )
                }
            }
        }
    }
}

/**
 * Secondary Strobe Guide Number & Power Fraction Section.
 */
@Composable
private fun StrobeGuideNumberSection(
    setup: com.example.data.model.MultiLightSetup,
    iso: Int,
    onPowerChange: (String, Float) -> Unit,
    onDistanceChange: (String, Float) -> Unit,
    onToggleEnabled: (String) -> Unit
) {
    // Title & Ratio Summary Card
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "STROBE POWER & DISTANCE SETUP",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Icon(
                imageVector = Icons.Default.FlashOn,
                contentDescription = "Flash Setup",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("KEY : FILL RATIO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = setup.lightingRatioString,
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("CAMERA SYNC", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "${setup.syncSpeed} @ ISO $iso",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }

    // List of Studio Lights
    Text(
        text = "ACTIVE STROBES & LIGHTS",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp
    )

    setup.lights.forEach { light ->
        StudioLightCard(
            light = light,
            iso = iso,
            onPowerChange = { frac -> onPowerChange(light.id, frac) },
            onDistanceChange = { d -> onDistanceChange(light.id, d) },
            onToggleEnabled = { onToggleEnabled(light.id) }
        )
    }
}

@Composable
private fun StudioLightCard(
    light: StudioLight,
    iso: Int,
    onPowerChange: (Float) -> Unit,
    onDistanceChange: (Float) -> Unit,
    onToggleEnabled: () -> Unit
) {
    val fStop = light.calculatedAperture(iso)

    Card(
        modifier = Modifier
            .testTag("light_card_${light.id}")
            .fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (light.isEnabled) MaterialTheme.colorScheme.surface else Color(0xFF0F141C)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (light.isEnabled) MaterialTheme.colorScheme.outline else Color(0xFF1E293B)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Name, Role, Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (light.role) {
                                    LightRole.KEY -> Color(0xFFF59E0B)
                                    LightRole.FILL -> Color(0xFF38BDF8)
                                    LightRole.RIM -> Color(0xFFEC4899)
                                    LightRole.BACKGROUND -> Color(0xFFA855F7)
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = light.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (light.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${light.role.title} • GN ${light.guideNumberMeters.toInt()}m",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "f/$fStop",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (light.isEnabled) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Switch(
                        checked = light.isEnabled,
                        onCheckedChange = { onToggleEnabled() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            if (light.isEnabled) {
                Spacer(modifier = Modifier.height(10.dp))

                // Power Fractions Selector
                Text(
                    text = "Power: ${light.powerString}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    STROBE_POWERS.forEach { (frac, label) ->
                        val isSelected = abs(light.powerFraction - frac) < 0.005f
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF1E293B))
                                .clickable { onPowerChange(frac) }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontFamily = FontFamily.Monospace,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Distance Slider
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Subject Distance: ${String.format(java.util.Locale.US, "%.1f", light.distanceMeters)}m",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Slider(
                    value = light.distanceMeters,
                    onValueChange = { onDistanceChange(it) },
                    valueRange = 0.5f..8.0f,
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.secondary)
                )
            }
        }
    }
}
