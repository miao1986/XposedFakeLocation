package com.noobexon.xposedfakelocation.manager.ui.navigation

sealed class Screen(val route: String) {
    object About : Screen("about")
    object Favorites : Screen("favorites")
    object HowToUse : Screen("howtouse")
    object Map : Screen("map")
    object Permissions : Screen("permissions")
    object Settings : Screen("settings")
}
