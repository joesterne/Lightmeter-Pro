package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Base Color Constants
val MeterAmber = Color(0xFFF59E0B)
val MeterAmberLight = Color(0xFFFBBF24)
val MeterAmberDark = Color(0xFFD97706)
val MeterAmberContainer = Color(0xFF2E2211)

val MeterCyan = Color(0xFF38BDF8)
val MeterGreen = Color(0xFF34D399)
val MeterRed = Color(0xFFEF4444)

val ProDarkBackground = Color(0xFF090D14)
val ProDarkSurface = Color(0xFF111827)
val ProDarkSurfaceVariant = Color(0xFF1E293B)
val ProDarkSurfaceCard = Color(0xFF151F30)
val ProDarkBorder = Color(0xFF334155)

val ProDarkTextPrimary = Color(0xFFF8FAFC)
val ProDarkTextSecondary = Color(0xFF94A3B8)
val ProDarkTextTertiary = Color(0xFF64748B)

// Red Night Vision (Astro Mode)
val AstroBlack = Color(0xFF000000)
val AstroRedSurface = Color(0xFF150404)
val AstroRedSurfaceVariant = Color(0xFF260808)
val AstroRedBorder = Color(0xFF4C0E0E)
val AstroRedPrimary = Color(0xFFEF4444)
val AstroRedSecondary = Color(0xFFDC2626)
val AstroRedText = Color(0xFFFCA5A5)
val AstroRedTextMuted = Color(0xFFB91C1C)

/**
 * Customizable App Accent Colors tailored for photographers & cinematographers.
 */
enum class AppAccentColor(
    val id: String,
    val title: String,
    val subtitle: String,
    val primary: Color,
    val light: Color,
    val dark: Color,
    val container: Color,
    val secondary: Color = MeterCyan,
    val tertiary: Color = MeterGreen
) {
    STUDIO_AMBER(
        id = "amber",
        title = "Studio Amber",
        subtitle = "Tungsten 3200K (Default)",
        primary = Color(0xFFF59E0B),
        light = Color(0xFFFBBF24),
        dark = Color(0xFFD97706),
        container = Color(0xFF2E2211),
        secondary = Color(0xFF38BDF8),
        tertiary = Color(0xFF34D399)
    ),
    LEICA_CRIMSON(
        id = "crimson",
        title = "Leica Crimson",
        subtitle = "Classic Red Dot",
        primary = Color(0xFFE11D48),
        light = Color(0xFFFB7185),
        dark = Color(0xFFBE123C),
        container = Color(0xFF3B1219),
        secondary = Color(0xFF38BDF8),
        tertiary = Color(0xFFF59E0B)
    ),
    ZEISS_COBALT(
        id = "cobalt",
        title = "Zeiss Cobalt",
        subtitle = "Precision Optical Blue",
        primary = Color(0xFF3B82F6),
        light = Color(0xFF60A5FA),
        dark = Color(0xFF1D4ED8),
        container = Color(0xFF132238),
        secondary = Color(0xFF38BDF8),
        tertiary = Color(0xFF34D399)
    ),
    FUJI_EMERALD(
        id = "emerald",
        title = "Fuji Emerald",
        subtitle = "Velvia Film Green",
        primary = Color(0xFF10B981),
        light = Color(0xFF34D399),
        dark = Color(0xFF047857),
        container = Color(0xFF0E2E22),
        secondary = Color(0xFF38BDF8),
        tertiary = Color(0xFFF59E0B)
    ),
    NEON_VIOLET(
        id = "violet",
        title = "Neon Violet",
        subtitle = "Cinema Mood Lighting",
        primary = Color(0xFF8B5CF6),
        light = Color(0xFFA78BFA),
        dark = Color(0xFF6D28D9),
        container = Color(0xFF26183B),
        secondary = Color(0xFFEC4899),
        tertiary = Color(0xFF38BDF8)
    ),
    KODAK_GOLD(
        id = "gold",
        title = "Kodak Gold",
        subtitle = "Portra Golden Hour",
        primary = Color(0xFFEAB308),
        light = Color(0xFFFDE047),
        dark = Color(0xFFCA8A04),
        container = Color(0xFF332A0C),
        secondary = Color(0xFFFB923C),
        tertiary = Color(0xFF34D399)
    ),
    TITANIUM_SILVER(
        id = "silver",
        title = "Titanium Silver",
        subtitle = "Monochrome Stealth",
        primary = Color(0xFFE2E8F0),
        light = Color(0xFFFFFFFF),
        dark = Color(0xFF94A3B8),
        container = Color(0xFF1E293B),
        secondary = Color(0xFF64748B),
        tertiary = Color(0xFF38BDF8)
    ),
    CINEMATIC_TEAL(
        id = "teal",
        title = "Cinematic Teal",
        subtitle = "Hollywood Teal & Orange",
        primary = Color(0xFF06B6D4),
        light = Color(0xFF22D3EE),
        dark = Color(0xFF0E7490),
        container = Color(0xFF082830),
        secondary = Color(0xFFF97316),
        tertiary = Color(0xFF34D399)
    );

    companion object {
        fun fromId(id: String): AppAccentColor {
            return entries.firstOrNull { it.id == id } ?: STUDIO_AMBER
        }
    }
}

/**
 * Customizable Background Vibe themes for studio ambiance.
 */
enum class AppBackgroundVibe(
    val id: String,
    val title: String,
    val subtitle: String,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val border: Color
) {
    OBSIDIAN_SLATE(
        id = "obsidian",
        title = "Obsidian Slate",
        subtitle = "Pro Studio Dark",
        background = Color(0xFF090D14),
        surface = Color(0xFF111827),
        surfaceVariant = Color(0xFF1E293B),
        border = Color(0xFF334155)
    ),
    MIDNIGHT_NAVY(
        id = "midnight",
        title = "Midnight Navy",
        subtitle = "Deep Darkroom Blue",
        background = Color(0xFF080D1A),
        surface = Color(0xFF0F172A),
        surfaceVariant = Color(0xFF1E293B),
        border = Color(0xFF25334D)
    ),
    CARBON_TITANIUM(
        id = "carbon",
        title = "Carbon Neutral",
        subtitle = "Cinema Film Gray",
        background = Color(0xFF121316),
        surface = Color(0xFF1A1C20),
        surfaceVariant = Color(0xFF282A30),
        border = Color(0xFF383B44)
    ),
    PITCH_BLACK(
        id = "pitch_black",
        title = "AMOLED Pitch Black",
        subtitle = "Pure #000000 Contrast",
        background = Color(0xFF000000),
        surface = Color(0xFF080808),
        surfaceVariant = Color(0xFF141414),
        border = Color(0xFF262626)
    );

    companion object {
        fun fromId(id: String): AppBackgroundVibe {
            return entries.firstOrNull { it.id == id } ?: OBSIDIAN_SLATE
        }
    }
}
