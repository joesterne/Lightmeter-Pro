package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.exp
import kotlin.math.pow

@Composable
fun HistogramView(
    ev100: Float,
    targetEv: Float = 12f,
    modifier: Modifier = Modifier,
    customBins: FloatArray? = null,
    showRgbCurves: Boolean = true
) {
    // Calculate normalized exposure balance (-3.0 to +3.0 stops)
    val stopDiff = (ev100 - targetEv).coerceIn(-3.5f, 3.5f)
    val animatedStopDiff by animateFloatAsState(
        targetValue = stopDiff,
        animationSpec = tween(150),
        label = "histogram_ev_anim"
    )

    // Center of distribution shifted by stop difference
    val meanBin = (128f + (animatedStopDiff / 3.0f) * 80f).coerceIn(20f, 235f)
    val stdDev = 38f

    val bins = remember(meanBin, customBins) {
        if (customBins != null && customBins.size >= 64) {
            customBins
        } else {
            FloatArray(64) { i ->
                val x = i * 4f
                val gaussian = exp(-((x - meanBin).pow(2)) / (2 * stdDev.pow(2)))
                (gaussian * 0.9f).coerceIn(0.05f, 1.0f)
            }
        }
    }

    val isShadowClipped = (customBins != null && customBins.isNotEmpty() && customBins[0] > 0.30f) || stopDiff <= -2.5f
    val isHighlightClipped = (customBins != null && customBins.isNotEmpty() && customBins[customBins.size - 1] > 0.30f) || stopDiff >= 2.5f

    Column(
        modifier = modifier
            .testTag("histogram_container")
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        // Header with Tonal Range & Clipping Warning
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (customBins != null) "CAMERAX LUMINANCE (LIVE)" else "LIVE HISTOGRAM",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${if (animatedStopDiff >= 0) "+" else ""}${String.format(java.util.Locale.US, "%.1f", animatedStopDiff)} EV",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isHighlightClipped || isShadowClipped) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Clipping Indicators
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                if (isShadowClipped) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Shadows Clipped",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "SHADOW CRUSH",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (isHighlightClipped) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Highlights Clipped",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "HIGHLIGHT BLOWN",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Canvas for Histogram Curves
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(Color(0xFF070B11), RoundedCornerShape(6.dp))
                .border(0.5.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            val cyanColor = MaterialTheme.colorScheme.secondary
            val redColor = MaterialTheme.colorScheme.error

            Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 2.dp)) {
                val width = size.width
                val height = size.height

                // Draw Zone Grid Lines (Zone 0, III, V Middle Gray, VII, X)
                val zoneV = width * 0.5f
                val zoneIII = width * 0.25f
                val zoneVII = width * 0.75f

                drawLine(
                    color = Color(0x3364748B),
                    start = Offset(zoneIII, 0f),
                    end = Offset(zoneIII, height),
                    strokeWidth = 1f
                )
                // Zone V (Middle Gray 18%)
                drawLine(
                    color = Color(0x66F59E0B),
                    start = Offset(zoneV, 0f),
                    end = Offset(zoneV, height),
                    strokeWidth = 1.5f
                )
                drawLine(
                    color = Color(0x3364748B),
                    start = Offset(zoneVII, 0f),
                    end = Offset(zoneVII, height),
                    strokeWidth = 1f
                )

                // Draw filled luminance histogram
                val step = width / (bins.size - 1)
                val lumPath = Path()
                lumPath.moveTo(0f, height)

                for (i in bins.indices) {
                    val x = i * step
                    val y = height - (bins[i] * height * 0.92f)
                    if (i == 0) lumPath.moveTo(x, y) else lumPath.lineTo(x, y)
                }
                lumPath.lineTo(width, height)
                lumPath.close()

                drawPath(
                    path = lumPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.45f),
                            primaryColor.copy(alpha = 0.05f)
                        )
                    )
                )

                // Outline curve
                val strokePath = Path()
                for (i in bins.indices) {
                    val x = i * step
                    val y = height - (bins[i] * height * 0.92f)
                    if (i == 0) strokePath.moveTo(x, y) else strokePath.lineTo(x, y)
                }
                drawPath(
                    path = strokePath,
                    color = primaryColor,
                    style = Stroke(width = 2f)
                )

                if (showRgbCurves) {
                    // Slight Red and Cyan channel variations for true pro colorist look
                    val rPath = Path()
                    val bPath = Path()
                    for (i in bins.indices) {
                        val x = i * step
                        val yr = height - (bins[i] * (0.85f + (i % 3) * 0.05f) * height * 0.9f)
                        val yb = height - (bins[i] * (0.88f + ((i + 1) % 3) * 0.04f) * height * 0.9f)
                        if (i == 0) {
                            rPath.moveTo(x, yr)
                            bPath.moveTo(x, yb)
                        } else {
                            rPath.lineTo(x, yr)
                            bPath.lineTo(x, yb)
                        }
                    }
                    drawPath(path = rPath, color = redColor.copy(alpha = 0.35f), style = Stroke(width = 1f))
                    drawPath(path = bPath, color = cyanColor.copy(alpha = 0.35f), style = Stroke(width = 1f))
                }

                // Balance meter needle at bottom
                val needleX = (zoneV + (animatedStopDiff / 3.0f) * (width * 0.42f)).coerceIn(4f, width - 4f)
                drawLine(
                    color = if (isHighlightClipped || isShadowClipped) redColor else cyanColor,
                    start = Offset(needleX, height - 8f),
                    end = Offset(needleX, height),
                    strokeWidth = 3f
                )
            }
        }

        // Tonal Scale Labels: Darks (Zone 0) -> Midtones (Zone V) -> Highlights (Zone X)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("SHADOWS (Zone 0)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("ZONE V (18% Gray)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.primary)
            Text("HIGHLIGHTS (Zone X)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
