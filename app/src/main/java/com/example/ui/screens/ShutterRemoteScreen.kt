package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel

@Composable
fun ShutterRemoteScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val shutterManager = viewModel.shutterManager
    val connectedDevice by shutterManager.connectedDevice.collectAsState()
    val availableDevices by shutterManager.availableDevices.collectAsState()
    val isScanning by shutterManager.isScanning.collectAsState()
    val isShutterActive by shutterManager.isShutterActive.collectAsState()
    val countdown by shutterManager.countdownSeconds.collectAsState()
    val isBulbRunning by shutterManager.isBulbRunning.collectAsState()
    val bulbElapsed by shutterManager.bulbElapsedTimeSeconds.collectAsState()
    val intervalProgress by shutterManager.intervalometerProgress.collectAsState()

    var intervalShots by remember { mutableIntStateOf(30) }
    var intervalSeconds by remember { mutableIntStateOf(5) }

    Column(
        modifier = modifier
            .testTag("shutter_remote_screen")
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Bluetooth Status Card
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (connectedDevice != null) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                        contentDescription = "Bluetooth Status",
                        tint = if (connectedDevice != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (connectedDevice != null) connectedDevice!!.name else "BLUETOOTH CAMERA TRIGGER",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (connectedDevice != null) "Connected • Ready to Trigger" else "Pair camera or wireless shutter remote",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (connectedDevice == null) {
                    OutlinedButton(
                        onClick = { shutterManager.startScan() },
                        enabled = !isScanning,
                        modifier = Modifier.testTag("scan_bluetooth_button")
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Scan")
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { shutterManager.disconnectDevice() },
                        modifier = Modifier.testTag("disconnect_bluetooth_button")
                    ) {
                        Text("Disconnect")
                    }
                }
            }

            // Available Devices List if not connected
            if (connectedDevice == null && availableDevices.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "AVAILABLE CAMERAS & REMOTES:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                availableDevices.forEach { device ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F141E))
                            .clickable { shutterManager.connectDevice(device) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(device.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(device.brand, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        }
                        Text(
                            "Connect",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Giant Shutter Release Trigger
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .testTag("trigger_shutter_button")
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        if (isShutterActive) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
                    .border(
                        6.dp,
                        if (isShutterActive) Color.White else MaterialTheme.colorScheme.primaryContainer,
                        CircleShape
                    )
                    .clickable {
                        shutterManager.triggerInstantShutter()
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Shutter Button",
                        tint = if (isShutterActive) Color.White else MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(52.dp)
                    )
                    Text(
                        text = if (isShutterActive) "RELEASED" else "SHUTTER",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = if (isShutterActive) Color.White else MaterialTheme.colorScheme.onPrimary,
                        letterSpacing = 2.sp
                    )
                }
            }
        }

        // Timed Countdown Release (2s, 5s, 10s)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELF-TIMER RELEASE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (countdown > 0) {
                        Text(
                            text = "${countdown}s",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(2, 5, 10).forEach { sec ->
                        OutlinedButton(
                            onClick = { shutterManager.triggerCountdownShutter(sec) },
                            modifier = Modifier.testTag("timer_${sec}s_button").weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("${sec}s Timer")
                        }
                    }
                }

                if (countdown > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { shutterManager.cancelCountdown() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Cancel Countdown")
                    }
                }
            }
        }

        // Bulb Mode Long Exposure Stopwatch
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "BULB MODE (LONG EXPOSURE STOPWATCH)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val mins = bulbElapsed / 60
                        val secs = bulbElapsed % 60
                        Text(
                            text = String.format(java.util.Locale.US, "%02d:%02d", mins, secs),
                            style = MaterialTheme.typography.displaySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isBulbRunning) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBulbRunning) "Shutter Curtains Open..." else "Ready for bulb capture",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = {
                            if (isBulbRunning) shutterManager.stopBulbExposure()
                            else shutterManager.startBulbExposure()
                        },
                        modifier = Modifier.testTag("bulb_toggle_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBulbRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (isBulbRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isBulbRunning) "Stop Bulb" else "Start Bulb"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBulbRunning) "Close Shutter" else "Open Bulb")
                    }
                }
            }
        }

        // Intervalometer for Timelapse
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "INTERVALOMETER (TIMELAPSE SEQUENCER)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Shots: $intervalShots frames", style = MaterialTheme.typography.bodySmall)
                    Text("Interval: ${intervalSeconds}s", style = MaterialTheme.typography.bodySmall)
                }

                Slider(
                    value = intervalShots.toFloat(),
                    onValueChange = { intervalShots = it.toInt() },
                    valueRange = 10f..200f,
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.secondary)
                )

                if (intervalProgress.isNotBlank()) {
                    Text(
                        text = intervalProgress,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Button(
                        onClick = { shutterManager.stopIntervalometer() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Stop Timelapse")
                    }
                } else {
                    OutlinedButton(
                        onClick = { shutterManager.startIntervalometer(intervalShots, intervalSeconds) },
                        modifier = Modifier.testTag("start_intervalometer_button").fillMaxWidth()
                    ) {
                        Text("Start Timelapse Sequence")
                    }
                }
            }
        }
    }
}
