// File: 'app/src/main/java/com/mkumar/DemoActivity.kt'
@file:Suppress("unused")

package com.mkumar

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults.exitUntilCollapsedScrollBehavior
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class ExpressiveActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ExpressiveApp() }
    }
}

@Composable
fun ExpressiveApp() {
    val dynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val context = LocalContext.current
    val dark = isSystemInDarkTheme()

    MaterialTheme(
        colorScheme = expressiveColorScheme(dark, dynamic, context),
        typography = expressiveTypography(),
        shapes = expressiveShapes()
    ) {
        Surface(Modifier.fillMaxSize()) {
            ExpressiveScaffold()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
private fun ExpressiveScaffold() {
    var page by remember { mutableStateOf(0) }
    var showFilters by remember { mutableStateOf(false) }

    val pages = listOf("Home", "Faves", "Shine")
    val scrollBehavior = exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Expressive", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    val rotation by animateFloatAsState(
                        targetValue = if (page == 0) 0f else 180f,
                        animationSpec = tween(450, easing = FastOutSlowInEasing),
                        label = "nav-rotate"
                    )
                    IconButton(onClick = { page = (page + pages.lastIndex) % pages.size }) {
                        Icon(
                            imageVector = if (page == 0) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.LightMode,
                            contentDescription = null,
                            modifier = Modifier.rotate(rotation)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filters")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            AnimatedVisibility(visible = page != 2) {
                ExtendedFloatingActionButton(
                    onClick = { /* CTA */ },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Create") }
                )
            }
        },
        bottomBar = {
            NavigationBar {
                pages.forEachIndexed { i, label ->
                    NavigationBarItem(
                        selected = page == i,
                        onClick = { page = i },
                        icon = {
                            when (i) {
                                0 -> Icon(Icons.Default.AutoAwesome, null)
                                1 -> Icon(Icons.Default.Favorite, null)
                                else -> Icon(Icons.Default.LightMode, null)
                            }
                        },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            AnimatedVisibility(
                visible = showFilters,
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it })
            ) {
                FiltersTray(onClose = { showFilters = false })
            }

            AnimatedContent(
                targetState = page,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(initialOffsetX = { it }) + fadeIn()) togetherWith
                                (slideOutHorizontally(targetOffsetX = { -it }) + fadeOut())
                    } else {
                        (slideInHorizontally(initialOffsetX = { -it }) + fadeIn()) togetherWith
                                (slideOutHorizontally(targetOffsetX = { it }) + fadeOut())
                    }.using(SizeTransform(clip = false))
                },
                label = "page"
            ) { target ->
                when (target) {
                    0 -> PageList(title = "Trending", accent = MaterialTheme.colorScheme.primary)
                    1 -> PageList(title = "Favorites", accent = MaterialTheme.colorScheme.tertiary)
                    else -> ShinePage()
                }
            }
        }
    }
}

@Composable
private fun FiltersTray(onClose: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 6.dp,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AssistChip(onClick = { }, label = { Text("New") })
            AssistChip(onClick = { }, label = { Text("Popular") })
            AssistChip(onClick = { }, label = { Text("Nearby") })
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onClose) { Text("Done") }
        }
    }
}

@Composable
private fun PageList(title: String, accent: Color) {
    val bg by animateColorAsState(
        targetValue = accent.copy(alpha = 0.08f),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessLow),
        label = "bg"
    )
    Box(Modifier.fillMaxSize().background(bg)) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Text(title, style = MaterialTheme.typography.headlineMedium) }
            items(sampleItems) { item -> ExpressiveCard(item) }
        }
    }
}

@Composable
private fun ExpressiveCard(item: CardItem) {
    var loved by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        if (loved) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
        label = "border"
    )
    ElevatedCard(
        onClick = { loved = !loved },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = if (loved) Icons.Default.Favorite else Icons.Default.AutoAwesome,
                contentDescription = null
            )
            Column(Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(
                    item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            OutlinedButton(
                onClick = { loved = !loved },
                border = BorderStroke(1.dp, borderColor)
            ) { Text(if (loved) "Liked" else "Like") }
        }
    }
}

@Composable
private fun ShinePage() {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Shine Mode", style = MaterialTheme.typography.headlineLarge)
        Text(
            "A vivid, high-chroma look using tertiary tones. Toggle components to feel expressive motion and color.",
            style = MaterialTheme.typography.bodyLarge
        )
        var on by remember { mutableStateOf(true) }
        FilledTonalButton(onClick = { on = !on }) { Text(if (on) "Turn Off" else "Turn On") }
        AnimatedVisibility(on, enter = fadeIn(), exit = fadeOut()) {
            AssistChip(onClick = {}, label = { Text("It's alive ✨") })
        }
    }
}

@Composable
private fun expressiveColorScheme(dark: Boolean, dynamic: Boolean, context: android.content.Context): ColorScheme {
    if (dynamic) {
        return if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    return if (dark) expressiveDarkColors() else expressiveLightColors()
}

private fun expressiveLightColors() = lightColorScheme(
    primary = Color(0xFF5B3DF5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7DDFF),
    onPrimaryContainer = Color(0xFF1A0061),
    secondary = Color(0xFF006E5E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF91F1DF),
    onSecondaryContainer = Color(0xFF00201B),
    tertiary = Color(0xFFAD1457),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD7E3),
    onTertiaryContainer = Color(0xFF3E001D),
    surface = Color(0xFFFBF8FF),
    surfaceContainer = Color(0xFFF3EEFF),
    onSurface = Color(0xFF1B1B1F),
    outline = Color(0xFF7A7787)
)

private fun expressiveDarkColors() = darkColorScheme(
    primary = Color(0xFFCABEFF),
    onPrimary = Color(0xFF251257),
    primaryContainer = Color(0xFF3D2B8F),
    onPrimaryContainer = Color(0xFFE7DDFF),
    secondary = Color(0xFF73D4C2),
    onSecondary = Color(0xFF003730),
    secondaryContainer = Color(0xFF005046),
    onSecondaryContainer = Color(0xFF91F1DF),
    tertiary = Color(0xFFFFB1C8),
    onTertiary = Color(0xFF5B0A2B),
    tertiaryContainer = Color(0xFF7D2E48),
    onTertiaryContainer = Color(0xFFFFD7E3),
    surface = Color(0xFF12121A),
    surfaceContainer = Color(0xFF1A1923),
    onSurface = Color(0xFFE4E1EC),
    outline = Color(0xFF948FA5)
)

@Composable
private fun expressiveTypography() = Typography(
    displayLarge = Typography().displayLarge.copy(fontWeight = FontWeight.ExtraBold),
    headlineLarge = Typography().headlineLarge.copy(fontWeight = FontWeight.Bold),
    titleLarge = Typography().titleLarge.copy(fontWeight = FontWeight.SemiBold)
)

@Composable
private fun expressiveShapes() = Shapes(
    extraSmall = ShapeDefaults.ExtraSmall,
    small = ShapeDefaults.Small,
    medium = ShapeDefaults.Medium,
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

data class CardItem(val title: String, val subtitle: String, @DrawableRes val icon: Int? = null)

private val sampleItems = List(12) { i ->
    CardItem(
        title = "Card #$i",
        subtitle = "This card demonstrates tonal surfaces, animated borders, and expressive typography. Tap to toggle like."
    )
}

@Preview
@Composable
private fun PreviewExpressiveApp() { ExpressiveApp() }
