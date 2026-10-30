package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Default Brand Emerald
val EmeraldPrimary = Color(0xFF10B981)
val EmeraldDark = Color(0xFF059669)
val EmeraldContainer = Color(0xFFD1FAE5)

// 8 Theme Colors Available
val ThemeColors = listOf(
    ThemeColorOption("سبز زمردی", Color(0xFF10B981), Color(0xFF059669), "#10B981"),
    ThemeColorOption("آبی اقیانوسی", Color(0xFF3B82F6), Color(0xFF2563EB), "#3B82F6"),
    ThemeColorOption("بنفش سلطنتی", Color(0xFF8B5CF6), Color(0xFF7C3AED), "#8B5CF6"),
    ThemeColorOption("نارنجی پرتقالی", Color(0xFFF97316), Color(0xFFEA580C), "#F97316"),
    ThemeColorOption("صورتی یاسی", Color(0xFFEC4899), Color(0xFFDB2777), "#EC4899"),
    ThemeColorOption("قرمز یاقوتی", Color(0xFFEF4444), Color(0xFFDC2626), "#EF4444"),
    ThemeColorOption("فیروزه‌ای مدرن", Color(0xFF06B6D4), Color(0xFF0891B2), "#06B6D4"),
    ThemeColorOption("طلایی کهربایی", Color(0xFFF59E0B), Color(0xFFD97706), "#F59E0B")
)

data class ThemeColorOption(
    val name: String,
    val primary: Color,
    val dark: Color,
    val hex: String
)

// Status Colors
val StatusPaid = Color(0xFF10B981)
val StatusPaidBg = Color(0xFFECFDF5)
val StatusCredit = Color(0xFFEF4444)
val StatusCreditBg = Color(0xFFFEF2F2)
val StatusPartial = Color(0xFF3B82F6)
val StatusPartialBg = Color(0xFFEFF6FF)

// Neutrals
val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)
val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF64748B)

val BackgroundDark = Color(0xFF0F172A)
val SurfaceDark = Color(0xFF1E293B)
val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF94A3B8)
