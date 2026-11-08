package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp
import com.example.R

// Custom Google Fonts installed locally in res/font
val TajawalFontFamily = FontFamily(
    Font(R.font.tajawal, FontWeight.Normal)
)

val AmiriFontFamily = FontFamily(
    Font(R.font.amiri, FontWeight.Normal)
)

val ArefRuqaaFontFamily = FontFamily(
    Font(R.font.aref_ruqaa, FontWeight.Normal)
)

val AmiriQuranFontFamily = FontFamily(
    Font(R.font.amiri_quran, FontWeight.Normal)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = TajawalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        textDirection = TextDirection.Rtl,
    ),
    displayMedium = TextStyle(
        fontFamily = TajawalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        textDirection = TextDirection.Rtl,
    ),
    headlineLarge = TextStyle(
        fontFamily = TajawalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        textDirection = TextDirection.Rtl,
    ),
    headlineMedium = TextStyle(
        fontFamily = TajawalFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 27.sp,
        textDirection = TextDirection.Rtl,
    ),
    titleLarge = TextStyle(
        fontFamily = TajawalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        textDirection = TextDirection.Rtl,
    ),
    titleMedium = TextStyle(
        fontFamily = TajawalFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        textDirection = TextDirection.Rtl,
    ),
    bodyLarge = TextStyle(
        fontFamily = TajawalFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        textDirection = TextDirection.Rtl,
    ),
    bodyMedium = TextStyle(
        fontFamily = TajawalFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        textDirection = TextDirection.Rtl,
    ),
    labelLarge = TextStyle(
        fontFamily = TajawalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        textDirection = TextDirection.Rtl,
    ),
    labelMedium = TextStyle(
        fontFamily = TajawalFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        textDirection = TextDirection.Rtl,
    ),
    labelSmall = TextStyle(
        fontFamily = TajawalFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        textDirection = TextDirection.Rtl,
    )
)
