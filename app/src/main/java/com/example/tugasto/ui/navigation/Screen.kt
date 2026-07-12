package com.example.tugasto.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String? = null, val icon: ImageVector? = null) {
    object Chat : Screen("chat", "Chat", Icons.Filled.Chat)
    object Dashboard : Screen("dashboard", "Resumen", Icons.Filled.PieChart)
    object Analysis : Screen("analysis", "Pro", Icons.Filled.Analytics)
    object Profile : Screen("profile", "Perfil", Icons.Filled.Person)
    object YapeConfirm : Screen("yape_confirm")
    object ProUpsell : Screen("pro_upsell")
    object History : Screen("history")
    object PinLock : Screen("pin_lock")
    object PinSetup : Screen("pin_setup")
    object Categories : Screen("categories")
}

val bottomNavItems = listOf(
    Screen.Chat,
    Screen.Dashboard,
    Screen.Analysis,
    Screen.Profile
)
