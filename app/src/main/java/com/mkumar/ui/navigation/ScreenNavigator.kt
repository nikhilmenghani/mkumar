package com.mkumar.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mkumar.common.extension.navigateWithState
import com.mkumar.ui.screens.HomeScreen
import com.mkumar.ui.screens.PreferenceScreen
import com.mkumar.ui.screens.customer.CustomerDetailsScreen
import com.mkumar.viewmodel.CustomerDetailsViewModel
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
    Home, Profile, Download, Settings, Apps, AddCustomer, CustomerDetail
}

sealed class Screen(val route: String) {
    object Home : Screen("Home")
    object Search : Screen("search")
    object Settings : Screen("Settings")
    object About : Screen("About")
}

// --- Added: simple route helpers for CustomerDetail with an argument
object Routes {
    const val CustomerDetail = "CustomerDetail"
    const val CustomerDetailWithArg = "CustomerDetail/{customerId}"
    fun customerDetail(id: String) = "CustomerDetail/$id"
}

val excludedScreens = listOf(Screens.Profile.name, Screens.Apps.name)

@Composable
fun ScreenNavigator(
    customerViewModel: CustomerViewModel // <- keep only this one
) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { Material3BottomNavigationBar(navController) },
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
fun Material3BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route
    val showNavBar = currentRoute == Screen.Home.route || currentRoute == Screen.Settings.route
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars) // Handle system navigation bars once
    ) {

        // Navigation bar shown only on specific routes with spring animation
        AnimatedVisibility(
            visible = showNavBar,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight / 2 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight / 2 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeOut(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        ) {
            Column {
                // New outer Box to layer navigation bar and search icon
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 8.dp,
                            bottom = 8.dp // Simple spacing - no system insets here
                        )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp) // Overall horizontal padding for the row
                        .align(Alignment.CenterHorizontally), // Align the row to the center horizontally within the Column
                    verticalAlignment = Alignment.Bottom // Align items to the bottom of the row
                ) {
                    // Navigation bar Surface
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(25.dp),
                        tonalElevation = 3.dp,
                        modifier = Modifier
                            .height(64.dp)
                            .weight(1f) // Make it take up available space
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val items = listOf(
                                Triple(
                                    Screen.Home.route, "Home",
                                    Pair(Icons.Filled.Home, Icons.Default.Home)
                                ),
                                Triple(
                                    Screen.Settings.route, "Settings",
                                    Pair(Icons.Filled.Settings, Icons.Default.Settings)
                                )
                            )

                            items.forEachIndexed { index, (route, title, icons) ->
                                val isSelected = when (route) {
                                    Screen.Home.route -> currentRoute == Screen.Home.route
                                    Screen.Settings.route -> currentRoute == Screen.Settings.route
                                    else -> false
                                }

                                val (selectedIcon, unselectedIcon) = icons

                                // Enhanced animation values with spring physics
                                val animatedScale by animateFloatAsState(
                                    targetValue = if (isSelected) 1.05f else 1.0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    label = "scale_$title"
                                )

                                val animatedAlpha by animateFloatAsState(
                                    targetValue = if (isSelected) 1f else 0.7f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    label = "alpha_$title"
                                )

                                // Background pill animation with spring
                                val pillWidth by animateDpAsState(
                                    targetValue = if (isSelected) 120.dp else 0.dp,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    label = "pillWidth_$title"
                                )

                                // Icon color animation
                                val iconColor by animateColorAsState(
                                    targetValue = if (isSelected)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    animationSpec = tween(300),
                                    label = "iconColor_$title"
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            navController.navigate(route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Horizontal layout for icon and text with animated pill background
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier
                                            .graphicsLayer {
                                                scaleX = animatedScale
                                                scaleY = animatedScale
                                                alpha = animatedAlpha
                                            }
                                            .then(
                                                if (isSelected) Modifier
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                                    .height(48.dp)
                                                    .widthIn(min = pillWidth) // Animated width
                                                    .padding(horizontal = 18.dp)
                                                else Modifier.padding(horizontal = 16.dp)
                                            )
                                    ) {
                                        // Animated icon with crossfade
                                        Crossfade(
                                            targetState = isSelected,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessVeryLow
                                            ),
                                            label = "iconCrossfade_$title"
                                        ) { selected ->
                                            Icon(
                                                imageVector = if (selected) selectedIcon else unselectedIcon,
                                                contentDescription = title,
                                                tint = iconColor,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        AnimatedVisibility(
                                            visible = isSelected,
                                            enter = fadeIn(
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessMedium
                                                )
                                            ) + expandHorizontally(
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessLow
                                                )
                                            ),
                                            exit = fadeOut(
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                                    stiffness = Spring.StiffnessLow
                                                )
                                            ) + shrinkHorizontally(
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                                    stiffness = Spring.StiffnessLow
                                                )
                                            )
                                        ) {
                                            Row {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = title,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = iconColor,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp)) // Gap between nav bar and search icon

                    // Separate Search Icon Button
                    FilledIconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate(Screen.Search.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .size(64.dp) // Match height of navigation bar
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    // If you want to hide bottom bar on any CustomerDetail route, use startsWith:
    if (currentDestination?.route !in excludedScreens &&
        currentDestination?.route?.startsWith(Routes.CustomerDetail) != true
    ) {
        NavigationBar {
            listOfNavItems.forEach { navItem: NavItem ->
                NavigationBarItem(
                    icon = { Icon(imageVector = navItem.icon, contentDescription = null) },
                    label = { Text(text = navItem.label) },
                    selected = currentDestination?.hierarchy?.any { it.route == navItem.route } == true,
                    onClick = {
                        if (currentDestination?.route != navItem.route) {
                            navController.navigateWithState(route = navItem.route)
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
            // Navigate like: navController.navigateWithState(Routes.customerDetail(customerId))
            HomeScreen(navController = navController, vm = customerViewModel)
        }

        composable(route = Screens.Settings.name) {
            PreferenceScreen(navController = navController)
        }

        composable(
            route = Routes.CustomerDetailWithArg, // "CustomerDetail/{customerId}"
            arguments = listOf(navArgument("customerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val vm: CustomerDetailsViewModel =
                hiltViewModel(backStackEntry)

            CustomerDetailsScreen(
                navController = navController,
                viewModel = vm
            )
        }
    }
}
