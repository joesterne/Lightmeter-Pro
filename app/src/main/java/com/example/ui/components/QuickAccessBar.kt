package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.repository.SyncState

@Composable
fun QuickAccessBar(
    priority: ExposurePriority,
    onPriorityChange: (ExposurePriority) -> Unit,
    currentIso: Int,
    onIsoSelect: (Int) -> Unit,
    currentNdStops: Int,
    onNdSelect: (Int) -> Unit,
    isRedNightMode: Boolean,
    onToggleRedNightMode: () -> Unit,
    isBatterySaver: Boolean,
    onToggleBatterySaver: () -> Unit,
    syncState: SyncState,
    onTriggerSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        // Top row: Quick Toggle Header & Toggles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .testTag("quick_access_toggle_button")
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Quick Parameters",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "QUICK CONTROLS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (isExpanded) " ▲" else " ▼",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick Actions: Red Night Mode, Battery Saver, Sync Status
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Red Night Vision Button
                Box(
                    modifier = Modifier
                        .testTag("toggle_red_night_mode")
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isRedNightMode) Color(0xFFEF4444) else MaterialTheme.colorScheme.surface)
                        .clickable { onToggleRedNightMode() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = "Red Night Vision",
                        tint = if (isRedNightMode) Color.Black else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Battery Saver Button
                Box(
                    modifier = Modifier
                        .testTag("toggle_battery_saver")
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isBatterySaver) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .clickable { onToggleBatterySaver() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BatterySaver,
                        contentDescription = "Battery Saver",
                        tint = if (isBatterySaver) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Sync Status
                IconButton(
                    onClick = onTriggerSync,
                    modifier = Modifier.testTag("cloud_sync_button").size(32.dp)
                ) {
                    Icon(
                        imageVector = if (syncState == SyncState.SYNCING) Icons.Default.CloudSync else Icons.Default.CloudDone,
                        contentDescription = "Cloud Sync",
                        tint = if (syncState == SyncState.SYNCING) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Horizontal Quick Mode Strip (Always visible for fast high-pressure access)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val modes = listOf(
                ExposurePriority.APERTURE_PRIORITY to "Av (Aperture)",
                ExposurePriority.SHUTTER_PRIORITY to "Tv (Shutter)",
                ExposurePriority.ISO_PRIORITY to "Sv (ISO)",
                ExposurePriority.MANUAL_LOCK to "M (Lock)"
            )

            modes.forEach { (mode, label) ->
                val isSelected = priority == mode
                Box(
                    modifier = Modifier
                        .testTag("mode_${mode.name}")
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { onPriorityChange(mode) }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Expanded Quick Parameter Sliders / Chips
        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                // Quick ISO Row
                Text(
                    text = "QUICK ISO",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(100, 200, 400, 800, 1600, 3200, 6400, 12800).forEach { isoVal ->
                        val isSelected = currentIso == isoVal
                        Box(
                            modifier = Modifier
                                .testTag("quick_iso_$isoVal")
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface)
                                .clickable { onIsoSelect(isoVal) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$isoVal",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Quick ND Filter Row
                Text(
                    text = "NEUTRAL DENSITY (ND FILTER)",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val ndOptions = listOf(
                        0 to "None (0 EV)",
                        1 to "ND2 (-1 EV)",
                        2 to "ND4 (-2 EV)",
                        3 to "ND8 (-3 EV)",
                        6 to "ND64 (-6 EV)",
                        10 to "ND1000 (-10 EV)"
                    )
                    ndOptions.forEach { (stops, label) ->
                        val isSelected = currentNdStops == stops
                        Box(
                            modifier = Modifier
                                .testTag("quick_nd_$stops")
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { onNdSelect(stops) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
