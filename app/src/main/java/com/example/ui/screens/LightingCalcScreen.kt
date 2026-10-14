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
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.model.LightRole
import com.example.data.model.StudioLight

@Composable
fun LightingCalcScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val setup by viewModel.studioSetup.collectAsState()
    val iso by viewModel.iso.collectAsState()

    Column(
        modifier = modifier
            .testTag("lighting_calc_screen")
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    text = "MULTI-LIGHT / STROBE STUDIO",
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
                onPowerChange = { frac -> viewModel.updateStudioLightPower(light.id, frac) },
                onDistanceChange = { d -> viewModel.updateStudioLightDistance(light.id, d) },
                onToggleEnabled = { viewModel.toggleStudioLight(light.id) }
            )
        }
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

                val powers = listOf(
                    1.0f to "1/1",
                    0.5f to "1/2",
                    0.25f to "1/4",
                    0.125f to "1/8",
                    0.0625f to "1/16",
                    0.03125f to "1/32",
                    0.015625f to "1/64"
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    powers.forEach { (frac, label) ->
                        val isSelected = kotlin.math.abs(light.powerFraction - frac) < 0.005f
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
