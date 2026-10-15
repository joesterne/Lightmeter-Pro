package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lens
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.MainViewModel
import com.example.data.local.CameraBodyEntity
import com.example.data.local.GearEntity
import com.example.data.local.LensEntity

enum class GearFilterTab(val title: String) {
    ALL("All Equipment"),
    CAMERAS("Camera Bodies"),
    LENSES("Lenses"),
    ACCESSORIES("Accessories")
}

@Composable
fun GearInventoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val cameraBodies by viewModel.cameraBodies.collectAsState()
    val lenses by viewModel.userLenses.collectAsState()
    val otherGear by viewModel.gearInventory.collectAsState()

    var activeTab by remember { mutableStateOf(GearFilterTab.ALL) }
    var showFabMenu by remember { mutableStateOf(false) }

    var showAddCameraDialog by remember { mutableStateOf(false) }
    var showAddLensDialog by remember { mutableStateOf(false) }
    var showAddGearDialog by remember { mutableStateOf(false) }

    // Combined pack weight calculation
    val packedCamerasWeight = cameraBodies.filter { it.inBag }.sumOf { it.weightGrams }
    val packedLensesWeight = lenses.filter { it.inBag }.sumOf { it.weightGrams }
    val packedOtherWeight = otherGear.filter { it.inBag }.sumOf { it.weightGrams }
    val totalWeightGrams = packedCamerasWeight + packedLensesWeight + packedOtherWeight
    val totalWeightKg = totalWeightGrams / 1000.0

    val totalItemsCount = cameraBodies.size + lenses.size + otherGear.size
    val totalPackedCount = cameraBodies.count { it.inBag } + lenses.count { it.inBag } + otherGear.count { it.inBag }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .testTag("gear_inventory_list")
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                // Header & Weight Card
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
                        Column {
                            Text(
                                text = "GEAR INVENTORY DATABASE",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "$totalItemsCount items stored • $totalPackedCount packed in shoot bag",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Luggage,
                            contentDescription = "Pack Bag",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL PACKED WEIGHT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${String.format(java.util.Locale.US, "%.2f", totalWeightKg)} kg (${totalWeightGrams}g)",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            // Category Filter Strip
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GearFilterTab.values().forEach { tab ->
                        val isSelected = activeTab == tab
                        Box(
                            modifier = Modifier
                                .testTag("filter_tab_${tab.name.lowercase()}")
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { activeTab = tab }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            val count = when (tab) {
                                GearFilterTab.ALL -> totalItemsCount
                                GearFilterTab.CAMERAS -> cameraBodies.size
                                GearFilterTab.LENSES -> lenses.size
                                GearFilterTab.ACCESSORIES -> otherGear.size
                            }
                            Text(
                                text = "${tab.title} ($count)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Camera Bodies Section
            if (activeTab == GearFilterTab.ALL || activeTab == GearFilterTab.CAMERAS) {
                if (cameraBodies.isNotEmpty()) {
                    item {
                        Text(
                            text = "CAMERA BODIES (${cameraBodies.size})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    }

                    items(cameraBodies, key = { "cam_${it.id}" }) { camera ->
                        CameraBodyCard(
                            camera = camera,
                            onToggleInBag = { viewModel.toggleCameraBodyInBag(camera) },
                            onMount = { viewModel.mountCameraBodyOnMeter(camera) },
                            onDelete = { viewModel.deleteCameraBody(camera) }
                        )
                    }
                }
            }

            // Lenses Section
            if (activeTab == GearFilterTab.ALL || activeTab == GearFilterTab.LENSES) {
                if (lenses.isNotEmpty()) {
                    item {
                        Text(
                            text = "LENSES (${lenses.size})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    }

                    items(lenses, key = { "lens_${it.id}" }) { lens ->
                        LensItemCard(
                            lens = lens,
                            onToggleInBag = { viewModel.toggleLensInBag(lens) },
                            onMount = { viewModel.mountLensOnMeter(lens) },
                            onDelete = { viewModel.deleteLens(lens) }
                        )
                    }
                }
            }

            // Other Accessories Section
            if (activeTab == GearFilterTab.ALL || activeTab == GearFilterTab.ACCESSORIES) {
                if (otherGear.isNotEmpty()) {
                    item {
                        Text(
                            text = "STROBES, METERS & ACCESSORIES (${otherGear.size})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    }

                    items(otherGear, key = { "gear_${it.id}" }) { gear ->
                        AccessoryCard(
                            gear = gear,
                            onToggleInBag = { viewModel.toggleGearInBag(gear) },
                            onDelete = { viewModel.deleteGearItem(gear) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Add Gear Multi-Option Floating Action Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            FloatingActionButton(
                onClick = { showFabMenu = true },
                modifier = Modifier.testTag("add_gear_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Equipment")
            }

            DropdownMenu(
                expanded = showFabMenu,
                onDismissRequest = { showFabMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Add Camera Body") },
                    leadingIcon = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                    onClick = {
                        showFabMenu = false
                        showAddCameraDialog = true
                    }
                )
                DropdownMenuItem(
                    text = { Text("Add Lens") },
                    leadingIcon = { Icon(Icons.Default.Lens, contentDescription = null) },
                    onClick = {
                        showFabMenu = false
                        showAddLensDialog = true
                    }
                )
                DropdownMenuItem(
                    text = { Text("Add Strobe / Accessory") },
                    leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null) },
                    onClick = {
                        showFabMenu = false
                        showAddGearDialog = true
                    }
                )
            }
        }
    }

    // Add Camera Body Dialog
    if (showAddCameraDialog) {
        AddCameraBodyDialog(
            onDismiss = { showAddCameraDialog = false },
            onConfirm = { brand, model, format, crop, baseIso, maxIso, maxShutter, ibis, sn, weight, notes ->
                viewModel.addCameraBody(brand, model, format, crop, baseIso, maxIso, maxShutter, ibis, sn, weight, notes)
                showAddCameraDialog = false
            }
        )
    }

    // Add Lens Dialog
    if (showAddLensDialog) {
        AddLensDialog(
            onDismiss = { showAddLensDialog = false },
            onConfirm = { brand, name, minFocal, maxFocal, maxAp, minAp, ois, thread, sn, weight, notes ->
                viewModel.addLens(brand, name, minFocal, maxFocal, maxAp, minAp, ois, thread, sn, weight, notes)
                showAddLensDialog = false
            }
        )
    }

    // Add Other Accessory Dialog
    if (showAddGearDialog) {
        AddAccessoryDialog(
            onDismiss = { showAddGearDialog = false },
            onConfirm = { name, brand, category, sn, weight, notes ->
                viewModel.addGearItem(name, brand, category, sn, weight, notes)
                showAddGearDialog = false
            }
        )
    }
}

@Composable
private fun CameraBodyCard(
    camera: CameraBodyEntity,
    onToggleInBag: () -> Unit,
    onMount: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .testTag("camera_body_card_${camera.id}")
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (camera.inBag) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleInBag,
                    modifier = Modifier.testTag("camera_checkbox_${camera.id}")
                ) {
                    Icon(
                        imageVector = if (camera.inBag) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Pack Camera",
                        tint = if (camera.inBag) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${camera.brand} ${camera.modelName}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${camera.weightGrams}g",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Text(
                        text = "${camera.sensorFormat} (${camera.cropFactor}x) • Max ${camera.maxShutterSpeed}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (camera.hasIbis) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF065F46))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "IBIS STABILIZED",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA7F3D0)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1E293B))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "ISO ${camera.baseIso}-${camera.maxIso}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    if (camera.serialNumber.isNotBlank()) {
                        Text(
                            text = "S/N: ${camera.serialNumber}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Camera",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onMount,
                modifier = Modifier
                    .testTag("mount_camera_${camera.id}")
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Mount Camera on Meter",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun LensItemCard(
    lens: LensEntity,
    onToggleInBag: () -> Unit,
    onMount: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .testTag("lens_card_${lens.id}")
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (lens.inBag) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleInBag,
                    modifier = Modifier.testTag("lens_checkbox_${lens.id}")
                ) {
                    Icon(
                        imageVector = if (lens.inBag) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Pack Lens",
                        tint = if (lens.inBag) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = lens.lensName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${lens.weightGrams}g",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Text(
                        text = "${lens.brand} • ${lens.focalLengthDisplay} • Max f/${lens.maxAperture}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (lens.hasOis) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF1E3A8A))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "OIS STABILIZED",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFBFDBFE)
                                )
                            }
                        }
                        if (lens.filterThreadMm > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF1E293B))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "Ø ${lens.filterThreadMm}mm THREAD",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    if (lens.serialNumber.isNotBlank()) {
                        Text(
                            text = "S/N: ${lens.serialNumber}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Lens",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onMount,
                modifier = Modifier
                    .testTag("mount_lens_${lens.id}")
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(Icons.Default.Lens, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Mount Lens on Meter",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun AccessoryCard(
    gear: GearEntity,
    onToggleInBag: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .testTag("accessory_card_${gear.id}")
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (gear.inBag) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleInBag) {
                Icon(
                    imageVector = if (gear.inBag) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Pack Item",
                    tint = if (gear.inBag) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = gear.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${gear.weightGrams}g",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Text(
                    text = "${gear.brand} • ${gear.category} • ${gear.condition}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (gear.serialNumber.isNotBlank()) {
                    Text(
                        text = "S/N: ${gear.serialNumber}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Item",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AddCameraBodyDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        brand: String,
        model: String,
        format: String,
        cropFactor: Float,
        baseIso: Int,
        maxIso: Int,
        maxShutter: String,
        hasIbis: Boolean,
        serialNumber: String,
        weightGrams: Int,
        notes: String
    ) -> Unit
) {
    var brand by remember { mutableStateOf("Canon") }
    var model by remember { mutableStateOf("") }
    var format by remember { mutableStateOf("Full Frame (35mm)") }
    var cropFactorText by remember { mutableStateOf("1.0") }
    var baseIsoText by remember { mutableStateOf("100") }
    var maxIsoText by remember { mutableStateOf("51200") }
    var maxShutter by remember { mutableStateOf("1/8000") }
    var hasIbis by remember { mutableStateOf(true) }
    var serialNumber by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("650") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Camera Body to Room DB") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Brand") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Model Name") },
                        modifier = Modifier.weight(1.2f)
                    )
                }
                OutlinedTextField(
                    value = format,
                    onValueChange = {
                        format = it
                        if (it.contains("APS-C", ignoreCase = true)) cropFactorText = "1.5"
                        else if (it.contains("Micro 4/3", ignoreCase = true)) cropFactorText = "2.0"
                        else if (it.contains("Medium", ignoreCase = true)) cropFactorText = "0.79"
                        else cropFactorText = "1.0"
                    },
                    label = { Text("Sensor Format (e.g. Full Frame, APS-C)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = baseIsoText,
                        onValueChange = { baseIsoText = it },
                        label = { Text("Base ISO") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxShutter,
                        onValueChange = { maxShutter = it },
                        label = { Text("Max Shutter") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("In-Body Stabilization (IBIS)", style = MaterialTheme.typography.bodySmall)
                    Switch(checked = hasIbis, onCheckedChange = { hasIbis = it })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = serialNumber,
                        onValueChange = { serialNumber = it },
                        label = { Text("Serial Number") },
                        modifier = Modifier.weight(1.2f)
                    )
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Weight (g)") },
                        modifier = Modifier.weight(0.8f)
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (model.isNotBlank()) {
                        val crop = cropFactorText.toFloatOrNull() ?: 1.0f
                        val baseIso = baseIsoText.toIntOrNull() ?: 100
                        val maxIso = maxIsoText.toIntOrNull() ?: 51200
                        val weight = weightText.toIntOrNull() ?: 650
                        onConfirm(brand, model, format, crop, baseIso, maxIso, maxShutter, hasIbis, serialNumber, weight, notes)
                    }
                }
            ) {
                Text("Add Camera")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddLensDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        brand: String,
        name: String,
        minFocal: Int,
        maxFocal: Int,
        maxAp: Float,
        minAp: Float,
        hasOis: Boolean,
        filterThreadMm: Int,
        serialNumber: String,
        weightGrams: Int,
        notes: String
    ) -> Unit
) {
    var brand by remember { mutableStateOf("Canon") }
    var name by remember { mutableStateOf("") }
    var minFocalText by remember { mutableStateOf("24") }
    var maxFocalText by remember { mutableStateOf("70") }
    var maxApText by remember { mutableStateOf("2.8") }
    var hasOis by remember { mutableStateOf(true) }
    var filterThreadText by remember { mutableStateOf("82") }
    var serialNumber by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("800") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Lens to Room DB") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Brand") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Lens Name") },
                        modifier = Modifier.weight(1.5f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minFocalText,
                        onValueChange = { minFocalText = it },
                        label = { Text("Min Focal (mm)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxFocalText,
                        onValueChange = { maxFocalText = it },
                        label = { Text("Max Focal (mm)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = maxApText,
                        onValueChange = { maxApText = it },
                        label = { Text("Max Aperture (e.g. 2.8)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = filterThreadText,
                        onValueChange = { filterThreadText = it },
                        label = { Text("Filter Thread (mm)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Optical Stabilization (OIS/IS/VR)", style = MaterialTheme.typography.bodySmall)
                    Switch(checked = hasOis, onCheckedChange = { hasOis = it })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = serialNumber,
                        onValueChange = { serialNumber = it },
                        label = { Text("Serial Number") },
                        modifier = Modifier.weight(1.2f)
                    )
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Weight (g)") },
                        modifier = Modifier.weight(0.8f)
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val minFocal = minFocalText.toIntOrNull() ?: 50
                        val maxFocal = maxFocalText.toIntOrNull() ?: minFocal
                        val maxAp = maxApText.toFloatOrNull() ?: 2.8f
                        val filterMm = filterThreadText.toIntOrNull() ?: 77
                        val weight = weightText.toIntOrNull() ?: 500
                        onConfirm(brand, name, minFocal, maxFocal, maxAp, 22f, hasOis, filterMm, serialNumber, weight, notes)
                    }
                }
            ) {
                Text("Add Lens")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddAccessoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, brand: String, category: String, sn: String, weight: Int, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Flash / Strobe") }
    var serialNumber by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("300") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Accessory to Inventory") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Accessory Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Brand") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = serialNumber,
                        onValueChange = { serialNumber = it },
                        label = { Text("Serial Number") },
                        modifier = Modifier.weight(1.2f)
                    )
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Weight (g)") },
                        modifier = Modifier.weight(0.8f)
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val weight = weightText.toIntOrNull() ?: 300
                        onConfirm(name, brand, category, serialNumber, weight, notes)
                    }
                }
            ) {
                Text("Add Accessory")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
