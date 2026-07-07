package com.neko.record.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import com.neko.record.ui.components.NekoBottomNavBar
import com.neko.record.ui.home.HomeScreen

/**
 * Root scaffold: bottom nav + the four top-level tabs.
 *
 * Screen Record, Tools, and Videos are placeholders in Milestone 1 — they get
 * real content in the Screen Broadcast, OpenCV/Automation, and (future) media
 * library milestones respectively. Keeping them as routes now means later
 * milestones only add composables, not navigation plumbing.
 */
@Composable
fun NekoRecordNavHost(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NekoBottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.LiveStream.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.LiveStream.route) {
                HomeScreen(onPlatformSelected = { /* wired in RTMP Engine milestone */ })
            }
            composable(Screen.ScreenRecord.route) {
                PlaceholderTab(title = "Screen Record")
            }
            composable(Screen.Tools.route) {
                PlaceholderTab(title = "Tools")
            }
            composable(Screen.Videos.route) {
                PlaceholderTab(title = "Videos")
            }
        }
    }
}

@Composable
private fun PlaceholderTab(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "$title — coming in a later milestone",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
