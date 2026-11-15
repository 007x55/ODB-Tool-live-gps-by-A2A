package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.A2ATab
import com.example.ui.screens.LiveMapScreen
import com.example.ui.screens.ObdAndTuningScreen
import com.example.ui.screens.OverviewScreen
import com.example.ui.screens.TripHistoryScreen
import com.example.ui.screens.VehicleStudioScreen
import com.example.ui.theme.A2ABackground
import com.example.ui.theme.A2APrimary
import com.example.ui.theme.A2APrimaryContainer
import com.example.ui.theme.A2ASurface
import com.example.ui.theme.A2ATextPrimary
import com.example.ui.theme.A2ATextSecondary
import com.example.ui.theme.A2ATheme
import com.example.ui.viewmodel.A2AViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            A2ATheme {
                A2AAppRoot()
            }
        }
    }
}

@Composable
fun A2AAppRoot(
    viewModel: A2AViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: A2ATab.OVERVIEW.route
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.statusBannerMessage) {
        val msg = state.statusBannerMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissBanner()
        }
    }

    val navigateToTab: (A2ATab) -> Unit = { tab ->
        navController.navigate(tab.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = A2ABackground,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useNavRail) {
                    NavigationBar(
                        containerColor = A2ASurface,
                        tonalElevation = 3.dp
                    ) {
                        A2ATab.entries.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(tab) },
                                modifier = Modifier.testTag("nav_${tab.route}"),
                                icon = {
                                    Icon(
                                        imageVector = tabIcon(tab, selected),
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = A2APrimary,
                                    selectedTextColor = A2ATextPrimary,
                                    indicatorColor = A2APrimaryContainer,
                                    unselectedIconColor = A2ATextSecondary,
                                    unselectedTextColor = A2ATextSecondary
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    NavigationRail(
                        containerColor = A2ASurface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        A2ATab.entries.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(tab) },
                                modifier = Modifier.testTag("rail_${tab.route}"),
                                icon = {
                                    Icon(
                                        imageVector = tabIcon(tab, selected),
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = A2APrimary,
                                    selectedTextColor = A2ATextPrimary,
                                    indicatorColor = A2APrimaryContainer,
                                    unselectedIconColor = Color(0xFF64748B),
                                    unselectedTextColor = Color(0xFF64748B)
                                )
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    NavHost(
                        navController = navController,
                        startDestination = A2ATab.OVERVIEW.route
                    ) {
                        composable(A2ATab.OVERVIEW.route) {
                            OverviewScreen(
                                state = state,
                                onNavigateTab = navigateToTab,
                                onAddDevice = viewModel::addHardwareDevice,
                                onUpdateEsp32Endpoint = viewModel::updateEsp32Endpoint,
                                onDeleteNode = viewModel::deleteIoTNode
                            )
                        }
                        composable(A2ATab.STUDIO.route) {
                            VehicleStudioScreen(
                                state = state,
                                onNavigateTab = navigateToTab,
                                onSelectModel3D = viewModel::selectModel3D,
                                onUpdateCarStyle = viewModel::updateCarStudioStyle,
                                onToggleLiveTracking = viewModel::toggleLiveTracking,
                                onToggleDrivingSim = viewModel::setDrivingSimulation,
                                onThrottleChange = viewModel::setSimulatedThrottle
                            )
                        }
                        composable(A2ATab.LIVE_MAP.route) {
                            LiveMapScreen(
                                state = state,
                                onSelectHudMetric = viewModel::updateHudMetric,
                                onToggleDrivingSim = viewModel::setDrivingSimulation,
                                onNavigateTab = navigateToTab
                            )
                        }
                        composable(A2ATab.OBD_ECU.route) {
                            ObdAndTuningScreen(
                                state = state,
                                onActivateTune = viewModel::activateTuneProfile,
                                onSaveAndFlashTune = viewModel::saveAndFlashTune,
                                onScanClearDtc = viewModel::scanAndClearDtc,
                                onNavigateTab = navigateToTab
                            )
                        }
                        composable(A2ATab.HISTORY.route) {
                            TripHistoryScreen(
                                state = state,
                                onSelectTripDay = viewModel::selectTripDay
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun tabIcon(tab: A2ATab, selected: Boolean): ImageVector = when (tab) {
    A2ATab.OVERVIEW -> if (selected) Icons.Filled.Dashboard else Icons.Outlined.Dashboard
    A2ATab.STUDIO -> if (selected) Icons.Filled.DirectionsCar else Icons.Outlined.DirectionsCar
    A2ATab.LIVE_MAP -> if (selected) Icons.Filled.LocationOn else Icons.Outlined.LocationOn
    A2ATab.OBD_ECU -> if (selected) Icons.Filled.Tune else Icons.Outlined.Tune
    A2ATab.HISTORY -> if (selected) Icons.Filled.History else Icons.Outlined.History
}
