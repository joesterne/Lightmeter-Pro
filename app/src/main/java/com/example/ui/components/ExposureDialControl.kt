package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExposurePriority

@Composable
fun ExposureDialControl(
    shutterSpeed: String,
    onShutterStep: (Boolean) -> Unit, // true = faster, false = slower
    aperture: Float,
    onApertureStep: (Boolean) -> Unit, // true = close down, false = open wider
    iso: Int,
    onIsoStep: (Boolean) -> Unit, // true = higher ISO, false = lower ISO
    priority: ExposurePriority,
    reciprocalWarning: Boolean = false,
    recommendedHandheldShutter: String = "",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        // Top Status Header: Handheld Reciprocal Warning
        if (reciprocalWarning) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Handheld Blur Warning",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Handheld Blur Risk: Recommend >= $recommendedHandheldShutter (or use tripod/IBIS)",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 3-Column Dial: Shutter | Aperture | ISO
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Shutter Speed Block
            ParameterCard(
                label = "SHUTTER SPEED",
                value = shutterSpeed,
                isPriorityLocked = priority == ExposurePriority.SHUTTER_PRIORITY,
                isCalculated = priority == ExposurePriority.APERTURE_PRIORITY,
                onDecrease = { onShutterStep(false) },
                onIncrease = { onShutterStep(true) },
                modifier = Modifier.weight(1f),
                testTagPrefix = "shutter"
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 2. Aperture Block
            ParameterCard(
                label = "APERTURE",
                value = "f/${if (aperture % 1f == 0f) "${aperture.toInt()}" else "$aperture"}",
                isPriorityLocked = priority == ExposurePriority.APERTURE_PRIORITY,
                isCalculated = priority == ExposurePriority.SHUTTER_PRIORITY,
                onDecrease = { onApertureStep(false) },
                onIncrease = { onApertureStep(true) },
                modifier = Modifier.weight(1f),
                testTagPrefix = "aperture"
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 3. ISO Block
            ParameterCard(
                label = "ISO SENSITIVITY",
                value = "$iso",
                isPriorityLocked = priority == ExposurePriority.ISO_PRIORITY,
                isCalculated = priority == ExposurePriority.ISO_PRIORITY,
                onDecrease = { onIsoStep(false) },
                onIncrease = { onIsoStep(true) },
                modifier = Modifier.weight(1f),
                testTagPrefix = "iso"
            )
        }
    }
}

@Composable
private fun ParameterCard(
    label: String,
    value: String,
    isPriorityLocked: Boolean,
    isCalculated: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isPriorityLocked) MaterialTheme.colorScheme.surfaceVariant else Color(0xFF0C1017))
            .border(
                1.5.dp,
                if (isPriorityLocked) MaterialTheme.colorScheme.primary else Color(0xFF1E293B),
                RoundedCornerShape(12.dp)
            )
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Label & Lock Indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = if (isPriorityLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            if (isPriorityLocked) {
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked Priority",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Big Readout Value
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = when {
                isPriorityLocked -> MaterialTheme.colorScheme.primary
                isCalculated -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Stepper Buttons (+ / -)
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .testTag("${testTagPrefix}_decrease_button")
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .clickable { onDecrease() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease $label",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(14.dp)
                )
            }

            Box(
                modifier = Modifier
                    .testTag("${testTagPrefix}_increase_button")
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .clickable { onIncrease() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase $label",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
