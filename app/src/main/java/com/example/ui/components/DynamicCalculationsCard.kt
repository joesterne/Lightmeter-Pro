package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AstroStarTrailCalculation
import com.example.data.model.DepthOfFieldCalculation
import com.example.data.model.ExposureBalanceCalculation
import com.example.data.model.MotionBlurEffect
import com.example.data.model.NdLongExposureOption
import kotlin.math.roundToInt

@Composable
fun DynamicCalculationsCard(
    dof: DepthOfFieldCalculation,
    balance: ExposureBalanceCalculation,
    astro: AstroStarTrailCalculation,
    ndOptions: List<NdLongExposureOption>,
    motion: MotionBlurEffect,
    subjectDistanceMeters: Float,
    onDistanceChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val categories = listOf("DOF & OPTICS", "EXPOSURE BALANCE", "MOTION & ND", "ASTROPHOTOGRAPHY")

    Card(
        modifier = modifier
            .testTag("dynamic_calculations_card")
            .fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Dynamic Calculations",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DYNAMIC CALCULATIONS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "LIVE PHYSICS",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-category selector tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEachIndexed { index, name ->
                    val isSelected = selectedCategoryIndex == index
                    Box(
                        modifier = Modifier
                            .testTag("dynamic_tab_$index")
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF131B27))
                            .clickable { selectedCategoryIndex = index }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic Body per Category
            when (selectedCategoryIndex) {
                0 -> DynamicDofSection(
                    dof = dof,
                    subjectDistanceMeters = subjectDistanceMeters,
                    onDistanceChange = onDistanceChange
                )
                1 -> DynamicExposureBalanceSection(balance = balance)
                2 -> DynamicMotionAndNdSection(motion = motion, ndOptions = ndOptions)
                3 -> DynamicAstroSection(astro = astro)
            }
        }
    }
}

/**
 * 1. Dynamic Depth of Field & Optical Physics
 */
@Composable
private fun DynamicDofSection(
    dof: DepthOfFieldCalculation,
    subjectDistanceMeters: Float,
    onDistanceChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Real-time Subject Distance Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Subject Focus Distance",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${String.format(java.util.Locale.US, "%.2f", subjectDistanceMeters)} m (${(subjectDistanceMeters * 3.28084f).let { String.format(java.util.Locale.US, "%.1f", it) }} ft)",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Slider(
            value = subjectDistanceMeters,
            onValueChange = onDistanceChange,
            valueRange = 0.3f..15.0f,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.testTag("dof_distance_slider")
        )

        // Visual DoF Distribution Scale Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "NEAR LIMIT: ${dof.nearLimitDisplay}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF38BDF8)
                    )
                    Text(
                        text = "FAR LIMIT: ${dof.farLimitDisplay}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF818CF8)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Canvas Graphic Range Bar
                val animatedFront by animateFloatAsState(
                    targetValue = dof.frontPercent / 100f,
                    animationSpec = tween(200),
                    label = "front_dof"
                )
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // Background track
                    drawRoundRect(
                        color = Color(0xFF1E293B),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )

                    // Sharp Focus Band (DoF)
                    val focusStart = w * 0.15f
                    val focusEnd = if (dof.isFarLimitInfinite) w else (w * 0.85f)
                    val focusWidth = (focusEnd - focusStart).coerceAtLeast(8.dp.toPx())

                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF0284C7), Color(0xFF6366F1))
                        ),
                        topLeft = Offset(focusStart, 0f),
                        size = Size(focusWidth, h),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )

                    // Subject cursor tick in the middle of the DoF
                    val subjectX = focusStart + (focusWidth * animatedFront)
                    drawCircle(
                        color = Color(0xFFF59E0B),
                        radius = 4.dp.toPx(),
                        center = Offset(subjectX, h / 2f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Front: ${dof.frontPercent.roundToInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Subject: ${String.format(java.util.Locale.US, "%.2f", subjectDistanceMeters)}m",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B)
                    )
                    Text(
                        text = "Rear: ${dof.rearPercent.roundToInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Summary Metric Grid: Total DoF, Hyperfocal, CoC, Diffraction
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricTile(
                label = "TOTAL DoF",
                value = dof.totalDofDisplay,
                subLabel = if (dof.isFarLimitInfinite) "To Infinity" else "In Focus",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "HYPERFOCAL",
                value = dof.hyperfocalDisplay,
                subLabel = "Focus here for max DoF",
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }

        // Optical Diffraction & CoC Detail Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (dof.isDiffractionLimited) Color(0xFF3B1E08) else Color(0xFF131B27))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (dof.isDiffractionLimited) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Diffraction Warning",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Diffraction Softening at f/${dof.aperture} (Airy Disk: ${String.format(java.util.Locale.US, "%.1f", dof.airyDiskMicrons)}μm)",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color(0xFFFDE68A),
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CenterFocusStrong,
                        contentDescription = "Sharp Optics",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Diffraction Safe • Airy Disk ${String.format(java.util.Locale.US, "%.1f", dof.airyDiskMicrons)}μm",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color(0xFF6EE7B7)
                    )
                }
            }
            Text(
                text = "CoC: ${String.format(java.util.Locale.US, "%.3f", dof.circleOfConfusionMm)}mm",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 2. Dynamic Exposure Balance & Zone System Gauge
 */
@Composable
private fun DynamicExposureBalanceSection(balance: ExposureBalanceCalculation) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Status row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dynamic EV Balance vs Scene",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = balance.statusText,
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                color = if (balance.isBalanced) Color(0xFF10B981) else if (balance.deltaEv > 0) Color(0xFFF59E0B) else Color(0xFF38BDF8)
            )
        }

        // Analog +/- EV Gauge Canvas
        val animatedDelta by animateFloatAsState(
            targetValue = balance.deltaEv.coerceIn(-3.5f, 3.5f),
            animationSpec = tween(150),
            label = "delta_ev"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Column {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val centerY = h * 0.65f
                    val centerX = w / 2f

                    // Baseline track
                    drawLine(
                        color = Color(0xFF334155),
                        start = Offset(10f, centerY),
                        end = Offset(w - 10f, centerY),
                        strokeWidth = 2.dp.toPx()
                    )

                    // Draw stop ticks from -3 to +3
                    for (i in -3..3) {
                        val tickX = centerX + (i * (w * 0.13f))
                        val isCenter = i == 0
                        val tickHeight = if (isCenter) 14.dp.toPx() else 8.dp.toPx()
                        val tickColor = if (isCenter) Color(0xFF10B981) else Color(0xFF64748B)

                        drawLine(
                            color = tickColor,
                            start = Offset(tickX, centerY - tickHeight),
                            end = Offset(tickX, centerY + (if (isCenter) 4.dp.toPx() else 0f)),
                            strokeWidth = if (isCenter) 2.5.dp.toPx() else 1.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // Indicator needle
                    val needleX = centerX + (animatedDelta * (w * 0.13f))
                    val needleColor = if (balance.isBalanced) Color(0xFF10B981) else if (animatedDelta > 0) Color(0xFFF59E0B) else Color(0xFF38BDF8)

                    drawLine(
                        color = needleColor,
                        start = Offset(needleX, centerY - 18.dp.toPx()),
                        end = Offset(needleX, centerY + 8.dp.toPx()),
                        strokeWidth = 3.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = needleColor,
                        radius = 4.dp.toPx(),
                        center = Offset(needleX, centerY - 18.dp.toPx())
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("-3 EV (Shadow)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color(0xFF38BDF8))
                    Text("0.0 (18% Gray)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    Text("+3 EV (Highlight)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color(0xFFF59E0B))
                }
            }
        }

        // Zone System Mapping
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF131B27))
                .padding(10.dp)
        ) {
            Column {
                Text(
                    text = "ANSEL ADAMS ZONE SYSTEM EQUIVALENT:",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = balance.zoneSystemName,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * 3. Dynamic Motion Kinetic & ND Long Exposure Table
 */
@Composable
private fun DynamicMotionAndNdSection(
    motion: MotionBlurEffect,
    ndOptions: List<NdLongExposureOption>
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Motion Effect Badge & Description
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kinetic Shutter Effect",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = motion.badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = motion.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• ${motion.safety}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Dynamic ND Long Exposure Multipliers
        Text(
            text = "DYNAMIC ND FILTER SHUTTER CONVERSION:",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ndOptions.forEach { opt ->
                Card(
                    modifier = Modifier.width(105.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131B27)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = opt.name,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = opt.displayString,
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (opt.isBulbRequired) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary
                        )
                        if (opt.isBulbRequired) {
                            Text(
                                text = "BULB TIMER",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 4. Dynamic Astrophotography (500 Rule & NPF Rule)
 */
@Composable
private fun DynamicAstroSection(astro: AstroStarTrailCalculation) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Pinpoint Star Exposure (No Star Trailing)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricTile(
                label = "500 RULE",
                value = astro.rule500Display,
                subLabel = "Classic 35mm formula",
                color = Color(0xFF38BDF8),
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "NPF RULE (PRECISION)",
                value = astro.npfRuleDisplay,
                subLabel = "Accounts for pixel pitch",
                color = Color(0xFF818CF8),
                modifier = Modifier.weight(1f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF131B27))
                .padding(10.dp)
        ) {
            Column {
                Text(
                    text = "ASTRONOMICAL GUIDANCE:",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "For ${astro.focalLengthMm}mm lens on ${astro.sensorFormat.displayName} @ f/${astro.aperture}, shutter speeds longer than ${astro.npfRuleDisplay} will cause celestial stars to elongate into trails due to Earth's rotation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    subLabel: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
