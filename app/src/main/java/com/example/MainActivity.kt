package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AiAdvisorScreen
import com.example.ui.screens.GearInventoryScreen
import com.example.ui.screens.LightingCalcScreen
import com.example.ui.screens.LiveMeterScreen
import com.example.ui.screens.OfflineGuideScreen
import com.example.ui.screens.PhotoAnalysisScreen
import com.example.ui.screens.PresetsScreen
import com.example.ui.screens.ShutterRemoteScreen
import com.example.ui.theme.LightmeterProTheme

enum class AppScreen(val title: String, val icon: ImageVector) {
    METER("Meter", Icons.Default.Speed),
    PHOTO("Photo AI", Icons.Default.PhotoCamera),
    STUDIO("Studio", Icons.Default.FlashOn),
    PRESETS("Presets", Icons.Default.Bookmark),
    REMOTE("Shutter", Icons.Default.Bluetooth),
    GEAR("Gear", Icons.Default.Inventory2),
    GUIDE("Guide", Icons.Default.Book),
    ADVISOR("AI Advisor", Icons.Default.AutoAwesome)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val isRedNightMode by viewModel.isRedNightMode.collectAsState()
            val isBatterySaver by viewModel.isBatterySaver.collectAsState()

            LightmeterProTheme(
                isRedNightMode = isRedNightMode,
                isBatterySaverMode = isBatterySaver
            ) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: MainViewModel) {
    var currentScreen by remember { mutableStateOf(AppScreen.METER) }
    var showMoreMenu by remember { mutableStateOf(false) }

    val isRedNightMode by viewModel.isRedNightMode.collectAsState()
    val isBatterySaver by viewModel.isBatterySaver.collectAsState()

    // Android back handler to return to Home screen
    BackHandler(enabled = currentScreen != AppScreen.METER) {
        currentScreen = AppScreen.METER
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Logo",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "LIGHTMETER PRO",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = currentScreen.title.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Red Night Vision Toggle
                    IconButton(
                        onClick = { viewModel.toggleRedNightMode() },
                        modifier = Modifier.testTag("topbar_red_mode")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = "Astro Red Night Mode",
                            tint = if (isRedNightMode) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Battery Saver Toggle
                    IconButton(
                        onClick = { viewModel.toggleBatterySaver() },
                        modifier = Modifier.testTag("topbar_battery_saver")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatterySaver,
                            contentDescription = "Battery Saver",
                            tint = if (isBatterySaver) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Scrollable / Multi-tab Navigation Bar
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                // Primary Tabs
                val primaryScreens = listOf(
                    AppScreen.METER,
                    AppScreen.PHOTO,
                    AppScreen.STUDIO,
                    AppScreen.PRESETS,
                    AppScreen.REMOTE
                )

                primaryScreens.forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                maxLines = 1,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                            )
                        },
                        modifier = Modifier.testTag("tab_${screen.name.lowercase()}"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                // "More..." Tab containing Gear, Guide, Advisor
                val isMoreSelected = currentScreen in listOf(AppScreen.GEAR, AppScreen.GUIDE, AppScreen.ADVISOR)
                NavigationBarItem(
                    selected = isMoreSelected,
                    onClick = { showMoreMenu = true },
                    icon = {
                        Box {
                            Icon(
                                imageVector = when (currentScreen) {
                                    AppScreen.GEAR -> AppScreen.GEAR.icon
                                    AppScreen.GUIDE -> AppScreen.GUIDE.icon
                                    AppScreen.ADVISOR -> AppScreen.ADVISOR.icon
                                    else -> Icons.Default.MoreHoriz
                                },
                                contentDescription = "More Tools"
                            )
                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Gear Inventory & Pack Weight") },
                                    leadingIcon = { Icon(Icons.Default.Inventory2, contentDescription = null) },
                                    onClick = {
                                        currentScreen = AppScreen.GEAR
                                        showMoreMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Offline Camera Guide (Sunny 16)") },
                                    leadingIcon = { Icon(Icons.Default.Book, contentDescription = null) },
                                    onClick = {
                                        currentScreen = AppScreen.GUIDE
                                        showMoreMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("AI Advisor (Multi-turn Gemini Chat)") },
                                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                                    onClick = {
                                        currentScreen = AppScreen.ADVISOR
                                        showMoreMenu = false
                                    }
                                )
                            }
                        }
                    },
                    label = {
                        Text(
                            text = when (currentScreen) {
                                AppScreen.GEAR -> "Gear"
                                AppScreen.GUIDE -> "Guide"
                                AppScreen.ADVISOR -> "Advisor"
                                else -> "More"
                            },
                            maxLines = 1,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                        )
                    },
                    modifier = Modifier.testTag("tab_more"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentScreen) {
                AppScreen.METER -> LiveMeterScreen(viewModel = viewModel)
                AppScreen.PHOTO -> PhotoAnalysisScreen(viewModel = viewModel)
                AppScreen.STUDIO -> LightingCalcScreen(viewModel = viewModel)
                AppScreen.PRESETS -> PresetsScreen(
                    viewModel = viewModel,
                    onNavigateToMeter = { currentScreen = AppScreen.METER }
                )
                AppScreen.REMOTE -> ShutterRemoteScreen(viewModel = viewModel)
                AppScreen.GEAR -> GearInventoryScreen(viewModel = viewModel)
                AppScreen.GUIDE -> OfflineGuideScreen()
                AppScreen.ADVISOR -> AiAdvisorScreen(viewModel = viewModel)
            }
        }
    }
}
