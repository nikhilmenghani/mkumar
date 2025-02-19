package com.mkumar.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mkumar.common.extension.navigateWithState
import com.mkumar.ui.screens.HomeScreen
import com.mkumar.ui.screens.PreferenceScreen
import com.mkumar.viewmodel.CustomerViewModel

data class NavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val listOfNavItems = listOf(
    NavItem("Home", Icons.Default.Home, Screens.Home.name),
    NavItem("Settings", Icons.Default.Settings, Screens.Settings.name)
)

enum class Screens {
    Home, Profile, Download, Settings, Apps
}

val excludedScreens = listOf(Screens.Profile.name, Screens.Apps.name)

@Composable
fun ScreenNavigator(customerViewModel: CustomerViewModel) {
    val navController: NavHostController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        contentWindowInsets = WindowInsets(left = 0, top = 0, right = 0, bottom = 0)
    ) { innerPadding ->
        NavigationHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            customerViewModel = customerViewModel
        )
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    if (currentDestination?.route !in excludedScreens) {
        NavigationBar {
            listOfNavItems.forEach { navItem: NavItem ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = navItem.icon,
                            contentDescription = null
                        )
                    },
                    label = { Text(text = navItem.label) },
                    selected = currentDestination?.hierarchy?.any { it.route == navItem.route } == true,
                    onClick = {
                        if (currentDestination?.route != navItem.route) {
                            navController.navigateWithState(
                                route = navItem.route
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun NavigationHost(
    navController: NavHostController,
    modifier: Modifier,
    customerViewModel: CustomerViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screens.Home.name,
        modifier = modifier
    ) {
        composable(route = Screens.Home.name) {
            HomeScreen(navController = navController, customerViewModel = customerViewModel)
        }
        composable(route = Screens.Settings.name) {
            PreferenceScreen(navController = navController)
        }
    }
}