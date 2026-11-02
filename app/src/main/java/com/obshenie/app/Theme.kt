package com.obshenie.app

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
class Palette(
    val dark: Boolean,
    val bg: Color,
    val bar: Color,
    val txt: Color,
    val sub: Color,
    val gt: Color,
    val acc: Color,
    val ink: Color,
    val badge: Color,
    val badgeMuted: Color,
    val online: Color,
    val avatars: List<Pair<Color, Color>>,
)

val LightPalette = Palette(
    dark = false,
    bg = Color(0xFFFFFFFF),
    bar = Color(0xFFF5F1FC),
    txt = Color(0xFF1C1A24),
    sub = Color(0xFF6B6678),
    gt = Color(0xFF6D4FCF),
    acc = Color(0xFFB79CFF),
    ink = Color(0xFF2A1A5E),
    badge = Color(0xFF6D4FCF),
    badgeMuted = Color(0xFF6B6678),
    online = Color(0xFF2FB71F),
    avatars = listOf(
        Color(0xFFE7DFFD) to Color(0xFF2A1A5E),
        Color(0xFFD9F0E2) to Color(0xFF17492D),
        Color(0xFFFDE3D6) to Color(0xFF6A2C12),
        Color(0xFFD8E8FB) to Color(0xFF173A63),
        Color(0xFFFBE0EF) to Color(0xFF6A1A45),
        Color(0xFFECE6F6) to Color(0xFF3B2A6E),
    ),
)

val DarkPalette = Palette(
    dark = true,
    bg = Color(0xFF141218),
    bar = Color(0xFF1F1B29),
    txt = Color(0xFFECE8F5),
    sub = Color(0xFFA8A2B8),
    gt = Color(0xFFC6B2FF),
    acc = Color(0xFFB79CFF),
    ink = Color(0xFF2A1A5E),
    badge = Color(0xFF7C5CDC),
    badgeMuted = Color(0xFF5D576B),
    online = Color(0xFF3DD22B),
    avatars = listOf(
        Color(0xFF3B3160) to Color(0xFFE7DFFD),
        Color(0xFF1F4130) to Color(0xFFD9F0E2),
        Color(0xFF5A2E1D) to Color(0xFFFDE3D6),
        Color(0xFF21395A) to Color(0xFFD8E8FB),
        Color(0xFF5A2342) to Color(0xFFFBE0EF),
        Color(0xFF2F2A48) to Color(0xFFECE6F6),
    ),
)

val LocalPalette = staticCompositionLocalOf { LightPalette }
