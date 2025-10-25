@file:Suppress("unused")

package com.mkumar.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min

/**
 * AppColors: a semantic layer on top of MaterialTheme.colorScheme
 * – Expressive combinations (primary/secondary/tertiary containers, surface containers, inverse, fixed)
 * – Contrast-aware helpers
 * – Component color factories (Buttons, Chips, TextFields, AppBars, Cards)
 *
 * Usage:
 *   Surface(color = AppColors.page, contentColor = AppColors.onPage) { … }
 *   Button(colors = AppColors.primaryButtonColors()) { Text("Go") }
 *   FilterChip(colors = AppColors.filterChipColors()) { … }
 *   OutlinedTextField(colors = AppColors.outlinedTextFieldColors()) …
 */
object AppColors {

    // -------------------------
    // 0) Contrast utilities
    // -------------------------

    /** WCAG relative luminance (sRGB). */
    fun Color.luminance(): Double {
        fun chan(v: Double): Double =
            if (v <= 0.03928) v / 12.92 else Math.pow((v + 0.055) / 1.055, 2.4)
        val r = chan(red.toDouble())
        val g = chan(green.toDouble())
        val b = chan(blue.toDouble())
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    /** WCAG contrast ratio between two colors. */
    fun contrastRatio(a: Color, b: Color): Double {
        val l1 = a.luminance()
        val l2 = b.luminance()
        val light = max(l1, l2)
        val dark = min(l1, l2)
        return (light + 0.05) / (dark + 0.05)
    }

    /** Given a background, choose black/white with higher contrast. */
    fun contentOn(bg: Color): Color {
        val black = Color(0xFF000000)
        val white = Color(0xFFFFFFFF)
        return if (contrastRatio(black, bg) >= contrastRatio(white, bg)) black else white
    }

    /**
     * Prefer `preferred` on `bg` if contrast >= minRatio, otherwise pick black/white.
     * Great for places where dynamic palettes could yield low-contrast pairs.
     */
    fun onReadable(bg: Color, preferred: Color, minRatio: Double = 3.0): Color {
        return if (contrastRatio(preferred, bg) >= minRatio) preferred else contentOn(bg)
    }

    // -------------------------------------------
    // 1) Raw tokens (semantic aliases for screens)
    // -------------------------------------------

    // App/page backgrounds
    val page: Color @Composable get() = MaterialTheme.colorScheme.background
    val onPage: Color @Composable get() = MaterialTheme.colorScheme.onBackground

    // Default cards / sheets
    val card: Color @Composable get() = MaterialTheme.colorScheme.surface
    val onCard: Color @Composable get() = MaterialTheme.colorScheme.onSurface
    val cardMeta: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

    // Tonal containers for grouped content (expressive surfaces)
    val section: Color @Composable get() = MaterialTheme.colorScheme.surfaceContainerHigh
    val onSection: Color @Composable get() = MaterialTheme.colorScheme.onSurface
    val sectionMeta: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

    // Dividers / borders
    val divider: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant
    val border: Color @Composable get() = MaterialTheme.colorScheme.outline

    // Primary actions
    val ctaBg: Color @Composable get() = MaterialTheme.colorScheme.primary
    val ctaContent: Color @Composable get() = MaterialTheme.colorScheme.onPrimary

    // Secondary and tertiary accents
    val accentSecondary: Color @Composable get() = MaterialTheme.colorScheme.secondary
    val onAccentSecondary: Color @Composable get() = MaterialTheme.colorScheme.onSecondary
    val accentTertiary: Color @Composable get() = MaterialTheme.colorScheme.tertiary
    val onAccentTertiary: Color @Composable get() = MaterialTheme.colorScheme.onTertiary

    // Hero / large decorative blocks (expressive fixed)
    val hero: Color @Composable get() = MaterialTheme.colorScheme.primaryFixed
    val onHero: Color @Composable get() = MaterialTheme.colorScheme.onPrimaryFixed
    val heroDim: Color @Composable get() = MaterialTheme.colorScheme.primaryFixedDim
    val onHeroVariant: Color @Composable get() = MaterialTheme.colorScheme.onPrimaryFixedVariant

    // Feedback / status (expressive mapping)
    // success -> tertiary container; info -> primary container; warning -> secondary container; error -> error container
    val info: Color @Composable get() = MaterialTheme.colorScheme.primaryContainer
    val onInfo: Color @Composable get() = MaterialTheme.colorScheme.onPrimaryContainer
    val success: Color @Composable get() = MaterialTheme.colorScheme.tertiaryContainer
    val onSuccess: Color @Composable get() = MaterialTheme.colorScheme.onTertiaryContainer
    val warning: Color @Composable get() = MaterialTheme.colorScheme.secondaryContainer
    val onWarning: Color @Composable get() = MaterialTheme.colorScheme.onSecondaryContainer
    val danger: Color @Composable get() = MaterialTheme.colorScheme.errorContainer
    val onDanger: Color @Composable get() = MaterialTheme.colorScheme.onErrorContainer

    // App bars / navigation
    val topBarBg: Color @Composable get() = MaterialTheme.colorScheme.surface
    val topBarContent: Color @Composable get() = MaterialTheme.colorScheme.onSurface
    val topBarElevatedBg: Color @Composable get() = MaterialTheme.colorScheme.surfaceContainer

    val bottomBarBg: Color @Composable get() = MaterialTheme.colorScheme.surface
    val bottomBarContent: Color @Composable get() = MaterialTheme.colorScheme.onSurface
    val navIndicator: Color @Composable get() = MaterialTheme.colorScheme.secondaryContainer
    val onNavIndicator: Color @Composable get() = MaterialTheme.colorScheme.onSecondaryContainer

    // FAB
    val fabContainer: Color @Composable get() = MaterialTheme.colorScheme.primary
    val onFab: Color @Composable get() = MaterialTheme.colorScheme.onPrimary

    // Inverse & scrim (sheets, high contrast bars)
    val inverseSurface: Color @Composable get() = MaterialTheme.colorScheme.inverseSurface
    val inverseOnSurface: Color @Composable get() = MaterialTheme.colorScheme.inverseOnSurface
    val scrim: Color @Composable get() = MaterialTheme.colorScheme.scrim

    // -------------------------------------------
    // 2) Expressive “pairings” (bg/fg suggestions)
    // -------------------------------------------

    /** Page background text. */
    val pagePair: Pair<Color, Color> @Composable get() = page to onPage

    /** Standard card/sheet pairing. */
    val cardPair: Pair<Color, Color> @Composable get() = card to onCard

    /** Tonal section pairing for grouped content. */
    val sectionPair: Pair<Color, Color> @Composable get() = section to onSection

    /** Primary hero/header block (container + onContainer). */
    val primaryHeader: Pair<Color, Color> @Composable get() =
        MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer

    /** Secondary tag/pill background (container). */
    val secondaryTag: Pair<Color, Color> @Composable get() =
        MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer

    /** Success/info/warning/error banners. */
    val bannerInfo: Pair<Color, Color> @Composable get() = info to onInfo
    val bannerSuccess: Pair<Color, Color> @Composable get() = success to onSuccess
    val bannerWarning: Pair<Color, Color> @Composable get() = warning to onWarning
    val bannerError: Pair<Color, Color> @Composable get() = danger to onDanger

    /** Elevated surface choices if you need finer control. */
    val surfaceLow: Pair<Color, Color> @Composable get() =
        MaterialTheme.colorScheme.surfaceContainerLowest to onCard
    val surfaceHigh: Pair<Color, Color> @Composable get() =
        MaterialTheme.colorScheme.surfaceContainerHighest to onCard

    // -------------------------------------------------
    // 3) Component color factories (ready-to-use params)
    // -------------------------------------------------

    // Buttons
    @Composable
    fun primaryButtonColors(): ButtonColors =
        ButtonDefaults.buttonColors(
            containerColor = ctaBg,
            contentColor = ctaContent
        )

    @Composable
    fun tonalButtonColors(): ButtonColors =
        ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )

    @Composable
    fun secondaryButtonColors(): ButtonColors =
        ButtonDefaults.buttonColors(
            containerColor = accentSecondary,
            contentColor = onAccentSecondary
        )

    @Composable
    fun tertiaryButtonColors(): ButtonColors =
        ButtonDefaults.buttonColors(
            containerColor = accentTertiary,
            contentColor = onAccentTertiary
        )

    @Composable
    fun outlinedButtonColors(): ButtonColors =
        ButtonDefaults.outlinedButtonColors(contentColor = onCard)

    // Chips
    @Composable
    fun filterChipColors(): SelectableChipColors =
        FilterChipDefaults.filterChipColors(
            containerColor = card,
            labelColor = onCard,
            iconColor = onCard,
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer
        )

    @Composable
    fun inputChipColors(): SelectableChipColors =
        FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            labelColor = onCard,
            iconColor = onCard,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
        )

    // Text fields
    @Composable
    fun outlinedTextFieldColors(): TextFieldColors =
        OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = divider,
            errorBorderColor = MaterialTheme.colorScheme.error,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            errorLabelColor = MaterialTheme.colorScheme.error,
            focusedContainerColor = card,
            unfocusedContainerColor = card,
            errorContainerColor = card,
            focusedTextColor = onCard,
            unfocusedTextColor = onCard,
            errorSupportingTextColor = onDanger
        )

    // Cards
    @Composable
    fun elevatedCardColors(): CardColors =
        CardDefaults.elevatedCardColors(containerColor = card, contentColor = onCard)

    @Composable
    fun outlinedCardColors(): CardColors =
        CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)

    // App bars
    @Composable
    fun smallTopAppBarColors(): TopAppBarColors =
        TopAppBarDefaults.topAppBarColors(
            containerColor = topBarBg,
            titleContentColor = topBarContent,
            navigationIconContentColor = topBarContent,
            actionIconContentColor = topBarContent
        )

    @Composable
    fun smallTopAppBarElevatedColors(): TopAppBarColors =
        TopAppBarDefaults.topAppBarColors(
            containerColor = topBarElevatedBg,
            titleContentColor = topBarContent,
            navigationIconContentColor = topBarContent,
            actionIconContentColor = topBarContent
        )

    // FAB (Compose M3 has defaults; expose explicit colors for consistency)
    @Composable
    fun fabContainerColor(): Color = fabContainer
    @Composable
    fun fabContentColor(): Color = onFab

    // Navigation bar / rail
    @Composable
    fun navigationBarItemColors(selected: Boolean): NavigationBarItemColors =
        NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
            indicatorColor = navIndicator,
            unselectedIconColor = onCard.copy(alpha = 0.80f),
            unselectedTextColor = onCard.copy(alpha = 0.80f)
        )

    // Banners / snackbars / toasts (expressive containers)
    @Composable
    fun infoBannerColors(): Pair<Color, Color> = bannerInfo
    @Composable
    fun successBannerColors(): Pair<Color, Color> = bannerSuccess
    @Composable
    fun warningBannerColors(): Pair<Color, Color> = bannerWarning
    @Composable
    fun errorBannerColors(): Pair<Color, Color> = bannerError

    // Dialogs / sheets
    @Composable
    fun dialogColors(): Pair<Color, Color> =
        MaterialTheme.colorScheme.surfaceContainerHigh to onSection

    @Composable
    fun bottomSheetColors(): Pair<Color, Color> =
        MaterialTheme.colorScheme.surface to onCard

    // Avatars / badges (subtle outlines on variants)
    @Composable
    fun badgeColors(): Pair<Color, Color> =
        MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer

    // -------------------------------------------
    // 4) Convenience content-color resolvers
    // -------------------------------------------

    /**
     * For arbitrary backgrounds (e.g., dynamically tinted images), pick readable content color.
     * Example: Text(color = AppColors.contentFor(bg = myTint, preferred = MaterialTheme.colorScheme.onPrimary))
     */
    @Composable
    fun contentFor(bg: Color, preferred: Color): Color = onReadable(bg, preferred)

    /** Given a bg, return black/white with higher contrast. */
    @Composable
    fun contentFor(bg: Color): Color = contentOn(bg)
}
