package com.mkumar.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.runtime.remember
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
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mkumar.common.extension.navigateWithState
import com.mkumar.ui.screens.HomeScreen
import com.mkumar.ui.screens.PreferenceScreen
import com.mkumar.ui.screens.customer.CustomerDetailsScreen
import com.mkumar.ui.screens.customer.OrderEditorScreen
import com.mkumar.ui.screens.search.SearchScreen
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
    object Search : Screen("Search")
    object Settings : Screen("Settings")
    object About : Screen("About")
}

// --- Added: simple route helpers for CustomerDetail with an argument
object Routes {
    const val CustomerGraph = "customer_graph/{customerId}"
    fun customerGraph(customerId: String) = "customer_graph/$customerId"

    const val CustomerDetail = "CustomerDetail"
    const val CustomerDetailWithArg = "CustomerDetail/{customerId}"
    fun customerDetail(id: String) = "CustomerDetail/$id"

    const val OrderEditor = "orderEditor?customerId={customerId}&orderId={orderId}"
    fun orderEditor(customerId: String, orderId: String = "") =
        "orderEditor?customerId=$customerId&orderId=$orderId"
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

    // Visual spec
    val barHeight = 64.dp
    val verticalPadding = 8.dp
    val horizontalPadding = 16.dp

    // System bottom inset so bar sits above gesture area and we animate the "real" space
    val navBarsPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // Total reserved height when bar is fully shown
    val fullReservedHeight = barHeight + (verticalPadding * 2) + navBarsPadding

    // 🔑 Animate the container height itself (this is what moves the next screen’s FAB smoothly)
    val containerHeight by animateDpAsState(
        targetValue = if (showNavBar) fullReservedHeight else 0.dp,
        // Make it a touch longer so you actually *see* the bar going down as the next screen comes in
        animationSpec = tween(durationMillis = 450, easing = EaseInOutQuart),
        label = "bottomBarContainerHeight"
    )

    // We animate the space; the content inside still animates for polish
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(containerHeight) // <- animated space reported to Scaffold
            .padding(
                start = horizontalPadding,
                end = horizontalPadding,
                // When containerHeight is small (during exit), keep content clamped to bottom
                top = verticalPadding,
                bottom = verticalPadding + navBarsPadding
            )
    ) {
        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            visible = showNavBar,
            enter = slideInVertically(
                initialOffsetY = { full -> full / 2 }
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { full -> full / 2 }
            ) + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pill nav surface
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(25.dp),
                    tonalElevation = 3.dp,
                    modifier = Modifier
                        .height(barHeight)
                        .weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val items = listOf(
                            Triple(Screen.Home.route, "Home", Pair(Icons.Filled.Home, Icons.Default.Home)),
                            Triple(Screen.Settings.route, "Settings", Pair(Icons.Filled.Settings, Icons.Default.Settings))
                        )

                        items.forEach { (route, title, icons) ->
                            val isSelected = when (route) {
                                Screen.Home.route -> currentRoute == Screen.Home.route
                                Screen.Settings.route -> currentRoute == Screen.Settings.route
                                else -> false
                            }
                            val (selectedIcon, unselectedIcon) = icons

                            val animatedScale by animateFloatAsState(
                                targetValue = if (isSelected) 1.05f else 1f,
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
                            val pillWidth by animateDpAsState(
                                targetValue = if (isSelected) 120.dp else 0.dp,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "pill_$title"
                            )
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
                                                .widthIn(min = pillWidth)
                                                .padding(horizontal = 18.dp)
                                            else Modifier.padding(horizontal = 16.dp)
                                        )
                                ) {
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
                                        enter = fadeIn() + expandHorizontally(),
                                        exit = fadeOut() + shrinkHorizontally()
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

                // Trailing Search button matching bar height
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
                    modifier = Modifier.size(barHeight)
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

@SuppressLint("UnrememberedGetBackStackEntry")
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
        composable(route = Screens.Home.name,
            enterTransition = {
                when {
                    initialState.destination.route?.startsWith("Settings") == true -> {
                        // Horizontal slide animation when coming from Library
                        fadeIn(animationSpec = tween(300)) +
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(350, easing = EaseInOutQuart)
                                )
                    }

                    else -> {
                        // Default animation for other sources
                        fadeIn(animationSpec = tween(300))
                    }
                }
            },
            exitTransition = {
                when {
                    targetState.destination.route?.startsWith("Settings") == true -> {
                        // Horizontal slide animation when going to Library
                        fadeOut(animationSpec = tween(300)) +
                                slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = tween(350, easing = EaseInOutQuart)
                                )
                    }

                    else -> {
                        // Default animation for other destinations
                        fadeOut(animationSpec = tween(300))
                    }
                }
            },
            popEnterTransition = {
                when {
                    initialState.destination.route?.startsWith("Settings") == true -> {
                        // Restore horizontal slide animation when popping back from Library
                        fadeIn(animationSpec = tween(300)) +
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(350, easing = EaseInOutQuart)
                                )
                    }

                    else -> {
                        // Simple faster fade animation when popping back from other screens
                        fadeIn(animationSpec = tween(200))
                    }
                }
            },
            popExitTransition = {
                // Simple faster fade animation when being popped from
                fadeOut(animationSpec = tween(200))
            }) {
            // Navigate like: navController.navigateWithState(Routes.customerDetail(customerId))
            HomeScreen(navController = navController, vm = customerViewModel)
        }

        composable(route = Screens.Settings.name,
            enterTransition = {
                when (initialState.destination.route) {
                    Screen.Home.route -> {
                        // Horizontal slide animation when coming from Home
                        fadeIn(animationSpec = tween(300)) +
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(350, easing = EaseInOutQuart)
                                )
                    }

                    else -> {
                        // Default animation for other sources
                        fadeIn(animationSpec = tween(300))
                    }
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    Screen.Home.route -> {
                        // Horizontal slide animation when going to Home
                        fadeOut(animationSpec = tween(300)) +
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(350, easing = EaseInOutQuart)
                                )
                    }

                    else -> {
                        // Default animation for other destinations
                        fadeOut(animationSpec = tween(300))
                    }
                }
            },
            popEnterTransition = {
                when (initialState.destination.route) {
                    Screen.Home.route -> {
                        // Restore horizontal slide animation when popping back from Home
                        fadeIn(animationSpec = tween(300)) +
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(350, easing = EaseInOutQuart)
                                )
                    }

                    else -> {
                        // Simple faster fade animation when popping back from other screens
                        fadeIn(animationSpec = tween(200))
                    }
                }
            },
            popExitTransition = {
                when (targetState.destination.route) {
                    Screen.Home.route -> {
                        // Restore horizontal slide animation when popping back to Home
                        fadeOut(animationSpec = tween(300)) +
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(350, easing = EaseInOutQuart)
                                )
                    }

                    else -> {
                        // Simple faster fade animation when being popped from for other destinations
                        fadeOut(animationSpec = tween(200))
                    }
                }
            }) {
            PreferenceScreen(navController = navController)
        }

        composable(route = Screen.Search.route) {
            SearchScreen(
                navController = navController,
            )
        }

        navigation(
            startDestination = Routes.CustomerDetailWithArg,     // "CustomerDetail/{customerId}"
            route = Routes.CustomerGraph                         // "customer_graph/{customerId}"
        ) {
            composable(
                route = Routes.CustomerDetailWithArg,
                arguments = listOf(navArgument("customerId") { type = NavType.StringType })
            ) { backStackEntry ->
                val customerId = backStackEntry.arguments?.getString("customerId").orEmpty()

                val parentEntry = remember(customerId) {
                    navController.getBackStackEntry(Routes.customerGraph(customerId))
                }
                // One shared VM instance for both screens:
                val vm: CustomerDetailsViewModel = hiltViewModel(parentEntry)

                CustomerDetailsScreen(
                    navController = navController,
                    viewModel = vm
                )
            }

            composable(
                route = Routes.OrderEditor,
                arguments = listOf(
                    navArgument("customerId") { type = NavType.StringType },
                    navArgument("orderId") { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStackEntry ->
                val customerId = backStackEntry.arguments?.getString("customerId").orEmpty()
                val orderId = backStackEntry.arguments?.getString("orderId").orEmpty()

                val parentEntry = remember(customerId) {
                    navController.getBackStackEntry(Routes.customerGraph(customerId))
                }
                val vm: CustomerDetailsViewModel = hiltViewModel(parentEntry)

                OrderEditorScreen(
                    navController = navController,
                    viewModel = vm,
                    customerId = customerId,
                    editingOrderId = orderId
                )
            }
        }
    }
}
