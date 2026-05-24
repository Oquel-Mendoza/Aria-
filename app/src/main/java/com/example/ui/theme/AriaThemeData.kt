package com.example.ui.theme

import androidx.compose.ui.graphics.Color

data class PremiumTheme(
    val id: String,
    val name: String,
    val isDark: Boolean,
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val onBackground: Color,
    val onSurface: Color,
    val accent: Color,
    val cardGradient: List<Color>,
    val chartColors: List<Color>,
    val luxuryGoldBorder: Boolean = false
)

object AriaThemes {
    val MINIMAL_WHITE = PremiumTheme(
        id = "MINIMAL_WHITE",
        name = "Minimal Bank White",
        isDark = false,
        primary = Color(0xFF000000),
        onPrimary = Color(0xFFFFFFFF),
        secondary = Color(0xFF6B7280),
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF3F4F6),
        onBackground = Color(0xFF111827),
        onSurface = Color(0xFF1F2937),
        accent = Color(0xFF10B981), // Emerald/Mint
        cardGradient = listOf(Color(0xFF000000), Color(0xFF374151)),
        chartColors = listOf(Color(0xFF10B981), Color(0xFF3B82F6), Color(0xFFF59E0B))
    )

    val DARK_FINTECH = PremiumTheme(
        id = "DARK_FINTECH",
        name = "Dark Fintech",
        isDark = true,
        primary = Color(0xFF10B981), // Neon emerald
        onPrimary = Color(0xFF000000),
        secondary = Color(0xFF9CA3AF),
        background = Color(0xFF0B0F19),
        surface = Color(0xFF151D30),
        onBackground = Color(0xFFF3F4F6),
        onSurface = Color(0xFFE5E7EB),
        accent = Color(0xFF06B6D4), // Cyan
        cardGradient = listOf(Color(0xFF151D30), Color(0xFF1E2E4A)),
        chartColors = listOf(Color(0xFF10B981), Color(0xFF06B6D4), Color(0xFF8B5CF6))
    )

    val GLASSMORPHISM = PremiumTheme(
        id = "GLASSMORPHISM",
        name = "Glassmorphism",
        isDark = true,
        primary = Color(0xFFEC4899), // Pink glass highlight
        onPrimary = Color(0xFFFFFFFF),
        secondary = Color(0xFFBDC2D0),
        background = Color(0xFF090D1A), // Deep interstellar black-blue
        surface = Color(0x331E293B), // Frosted semi-transparent surface
        onBackground = Color(0xFFFAFAFA),
        onSurface = Color(0xFFF4F4F5),
        accent = Color(0xFF6366F1), // Indigo
        cardGradient = listOf(Color(0x664F46E5), Color(0x66EC4899)),
        chartColors = listOf(Color(0xFFEC4899), Color(0xFF6366F1), Color(0xFF14B8A6))
    )

    val NEOBANK_BLUE = PremiumTheme(
        id = "NEOBANK_BLUE",
        name = "NeoBank Blue",
        isDark = true,
        primary = Color(0xFF00E5FF), // Brandeis electric cyan
        onPrimary = Color(0xFF001F3F),
        secondary = Color(0xFF93C5FD),
        background = Color(0xFF001730), // Deep blue
        surface = Color(0xFF0B2545),
        onBackground = Color(0xFFF0FDF4),
        onSurface = Color(0xFFEFF6FF),
        accent = Color(0xFF3B82F6),
        cardGradient = listOf(Color(0xFF0B2545), Color(0xFF134074)),
        chartColors = listOf(Color(0xFF00E5FF), Color(0xFF3B82F6), Color(0xFFF43F5E))
    )

    val EMERALD_FINANCE = PremiumTheme(
        id = "EMERALD_FINANCE",
        name = "Emerald Finance",
        isDark = true,
        primary = Color(0xFF34D399), // Mint green
        onPrimary = Color(0xFF022C22),
        secondary = Color(0xFFA7F3D0),
        background = Color(0xFF041812), // Forest deep background
        surface = Color(0xFF0B2E24), // Sage containers
        onBackground = Color(0xFFF0FDF4),
        onSurface = Color(0xFFECFDF5),
        accent = Color(0xFFFCD34D), // Amber gold
        cardGradient = listOf(Color(0xFF0B2E24), Color(0xFF047857)),
        chartColors = listOf(Color(0xFF34D399), Color(0xFF10B981), Color(0xFFF59E0B))
    )

    val MIDNIGHT_PREMIUM = PremiumTheme(
        id = "MIDNIGHT_PREMIUM",
        name = "Midnight Premium",
        isDark = true,
        primary = Color(0xFF38BDF8), // Royal sky blue
        onPrimary = Color(0xFF000000),
        secondary = Color(0xFF94A3B8),
        background = Color(0xFF020205), // Pitch midnight black
        surface = Color(0xFF0F172A), // Slate 900
        onBackground = Color(0xFFF1F5F9),
        onSurface = Color(0xFFE2E8F0),
        accent = Color(0xFF818CF8),
        cardGradient = listOf(Color(0xFF0F172A), Color(0xFF1E1E38)),
        chartColors = listOf(Color(0xFF38BDF8), Color(0xFF818CF8), Color(0xFFF43F5E))
    )

    val CORPORATE_BANK = PremiumTheme(
        id = "CORPORATE_BANK",
        name = "Corporate Bank",
        isDark = true,
        primary = Color(0xFFF59E0B), // Corporate gold
        onPrimary = Color(0xFF0F172A),
        secondary = Color(0xFF94A3B8),
        background = Color(0xFF0F172A), // Deep corporate slate/navy
        surface = Color(0xFF1E293B),
        onBackground = Color(0xFFF8FAFC),
        onSurface = Color(0xFFF1F5F9),
        accent = Color(0xFFD4AF37),
        cardGradient = listOf(Color(0xFF1E293B), Color(0xFF2E4057)),
        chartColors = listOf(Color(0xFFF59E0B), Color(0xFFD4AF37), Color(0xFF3B82F6))
    )

    val MODERN_CRYPTO = PremiumTheme(
        id = "MODERN_CRYPTO",
        name = "Modern Crypto Style",
        isDark = true,
        primary = Color(0xFFF59E0B), // Bitcoin Orange
        onPrimary = Color(0xFF180E29),
        secondary = Color(0xFFC084FC),
        background = Color(0xFF0D0814), // Dark purple cyber space
        surface = Color(0xFF180E29),
        onBackground = Color(0xFFFAF5FF),
        onSurface = Color(0xFFF3E8FF),
        accent = Color(0xFF8B5CF6),
        cardGradient = listOf(Color(0xFF180E29), Color(0xFF4C1D95)),
        chartColors = listOf(Color(0xFFF59E0B), Color(0xFF8B5CF6), Color(0xFF06B6D4))
    )

    val ELEGANT_GRAY = PremiumTheme(
        id = "ELEGANT_GRAY",
        name = "Elegant Gray",
        isDark = true,
        primary = Color(0xFFFAFAFA), // Pure white text headings
        onPrimary = Color(0xFF000000),
        secondary = Color(0xFFA1A1AA),
        background = Color(0xFF121212), // Silky absolute dark grey
        surface = Color(0xFF1C1C1E), // Soft matte gray surfaces
        onBackground = Color(0xFFFAFAFA),
        onSurface = Color(0xFFE4E4E7),
        accent = Color(0xFF71717A),
        cardGradient = listOf(Color(0xFF1C1C1E), Color(0xFF2A2A2D)),
        chartColors = listOf(Color(0xFFFAFAFA), Color(0xFF71717A), Color(0xFFD4D4D8))
    )

    val LUXURY_GOLD = PremiumTheme(
        id = "LUXURY_GOLD",
        name = "Luxury Black & Gold",
        isDark = true,
        primary = Color(0xFFD4AF37), // Pure gold accent link
        onPrimary = Color(0xFF000000),
        secondary = Color(0xFFF5E0A3),
        background = Color(0xFF000000), // Velvet pure deep black
        surface = Color(0xFF111111), // Matte rich layout card
        onBackground = Color(0xFFFFFFFF),
        onSurface = Color(0xFFF9F6F0), // Off-white luxury satin color
        accent = Color(0xFFA67C1E),
        cardGradient = listOf(Color(0xFF111111), Color(0xFF262115)),
        chartColors = listOf(Color(0xFFD4AF37), Color(0xFFA67C1E), Color(0xFFF5E0A3)),
        luxuryGoldBorder = true
    )

    val list = listOf(
        MINIMAL_WHITE,
        DARK_FINTECH,
        GLASSMORPHISM,
        NEOBANK_BLUE,
        EMERALD_FINANCE,
        MIDNIGHT_PREMIUM,
        CORPORATE_BANK,
        MODERN_CRYPTO,
        ELEGANT_GRAY,
        LUXURY_GOLD
    )

    fun getThemeById(id: String): PremiumTheme {
        return list.firstOrNull { it.id == id } ?: MINIMAL_WHITE
    }
}
