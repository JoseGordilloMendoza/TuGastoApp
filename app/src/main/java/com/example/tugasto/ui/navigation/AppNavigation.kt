package com.example.tugasto.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tugasto.ui.screens.AnalysisScreen
import com.example.tugasto.ui.screens.CategoriesScreen
import com.example.tugasto.ui.screens.ChatScreen
import com.example.tugasto.ui.screens.DashboardScreen
import com.example.tugasto.ui.screens.HistoryScreen
import com.example.tugasto.ui.screens.PinScreen
import com.example.tugasto.ui.screens.ProfileScreen
import com.example.tugasto.ui.screens.CloudAuthState
import com.example.tugasto.ui.screens.ProfileViewModel
import com.example.tugasto.ui.screens.ProUpsellScreen
import com.example.tugasto.ui.screens.YapeConfirmScreen

@Composable
fun AppNavigation(profileViewModel: ProfileViewModel = hiltViewModel()) {
    val isProUser by profileViewModel.isProUser.collectAsState()
    val cloudAuthState by profileViewModel.cloudAuthState.collectAsState()
    val cloudBannerDismissed by profileViewModel.cloudBannerDismissed.collectAsState()
    val showCloudBanner = cloudAuthState is CloudAuthState.NotSignedIn && !cloudBannerDismissed
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val hiddenRoutes = setOf(
        Screen.YapeConfirm.route,
        Screen.History.route,
        Screen.PinLock.route,
        Screen.PinSetup.route,
        Screen.Categories.route
    )
    val showBottomBar = currentDestination?.route !in hiddenRoutes

    // Calculado una vez al inicio: si hay PIN, arranca en lock
    val startDestination = remember {
        if (profileViewModel.isPinSet()) Screen.PinLock.route else Screen.Chat.route
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentDestination = currentDestination,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.PinLock.route) {
                PinScreen(
                    isSetup = false,
                    onSuccess = {
                        navController.navigate(Screen.Chat.route) {
                            popUpTo(Screen.PinLock.route) { inclusive = true }
                        }
                    },
                    onResetPin = {
                        navController.navigate(Screen.PinSetup.route) {
                            popUpTo(Screen.PinLock.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.PinSetup.route) {
                PinScreen(
                    isSetup = true,
                    onSuccess = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(Screen.Chat.route) {
                ChatScreen(onNavigateToYape = { navController.navigate(Screen.YapeConfirm.route) })
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onUpgradeClick = {
                        if (isProUser) navController.navigate(Screen.Analysis.route)
                        else navController.navigate(Screen.ProUpsell.route)
                    },
                    onDetailsClick = { navController.navigate(Screen.History.route) },
                    showCloudBanner = showCloudBanner,
                    onDismissBanner = { profileViewModel.dismissCloudBanner() },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.Analysis.route) {
                AnalysisScreen(
                    onUpgradeClick = { navController.navigate(Screen.ProUpsell.route) },
                    onHistoryClick = { navController.navigate(Screen.History.route) },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.ProUpsell.route) {
                val isVerifyingPro by profileViewModel.isVerifyingPro.collectAsState()
                val proVerificationResult by profileViewModel.proVerificationResult.collectAsState()
                ProUpsellScreen(
                    isSignedIn = cloudAuthState is CloudAuthState.SignedIn,
                    isVerifyingPro = isVerifyingPro,
                    verificationResult = proVerificationResult,
                    onVerifyPro = { profileViewModel.refreshProStatus() },
                    onClearVerificationResult = { profileViewModel.clearProVerificationResult() },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onSetupPin = { navController.navigate(Screen.PinSetup.route) },
                    onNavigateToCategories = { navController.navigate(Screen.Categories.route) },
                    onNavigateToProUpsell = { navController.navigate(Screen.ProUpsell.route) }
                )
            }
            composable(Screen.Categories.route) {
                CategoriesScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.YapeConfirm.route) {
                YapeConfirmScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.History.route) {
                HistoryScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
