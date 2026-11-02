package com.obshenie.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

// те же кривые, что у меню в letnievpn
private val RevealOpen = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
private val RevealShut = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)

private val MenuW = 236.dp
private val MenuH = 120.dp // 8 + два пункта по 52 + 8

private class MenuShape(private val rect: Rect, private val radius: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val r = minOf(radius, rect.width / 2f, rect.height / 2f).coerceAtLeast(0f)
        return Outline.Rounded(RoundRect(rect, CornerRadius(r, r)))
    }
}

/**
 * Меню из трёх точек: поверхность растёт из самой кнопки до размера меню, пункты проявляются внутри неё.
 * Закрытие — обратно в кнопку. Всё движение идёт через graphicsLayer, без перекомпоновки.
 * Кладётся внутрь корневого Box экрана, поверх остального.
 */
@Composable
fun HeaderMenu(
    open: Boolean,
    onOpen: (Boolean) -> Unit,
    onNewChat: () -> Unit,
    onReadAll: () -> Unit,
) {
    val p = LocalPalette.current
    val d = LocalDensity.current

    val t = remember { Animatable(0f) }
    val shown by remember { derivedStateOf { t.value > 0.001f } }

    LaunchedEffect(open) {
        if (open) t.animateTo(1f, tween(520, easing = RevealOpen))
        else t.animateTo(0f, tween(420, easing = RevealShut))
    }

    BackHandler(enabled = open) { onOpen(false) }

    if (shown || open) {
        val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val w = maxWidth
            val button = remember(w, top, d) {
                with(d) {
                    Rect(
                        Offset((w - 8.dp - 48.dp).toPx(), (top + 6.dp + 4.dp).toPx()),
                        Size(48.dp.toPx(), 48.dp.toPx())
                    )
                }
            }
            val menu = remember(w, top, d) {
                with(d) {
                    Rect(
                        Offset((w - 12.dp - MenuW).toPx(), (top + 6.dp + 8.dp).toPx()),
                        Size(MenuW.toPx(), MenuH.toPx())
                    )
                }
            }

            // тап мимо меню закрывает его
            Spacer(
                Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = open,
                        onClick = { onOpen(false) }
                    )
            )

            // сама поверхность: прямоугольник растёт из кнопки, углы остаются круглыми
            androidx.compose.foundation.layout.Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val tt = t.value
                        shape = MenuShape(lerp(button, menu, tt), 24.dp.toPx())
                        clip = true
                        alpha = (tt / 0.15f).coerceIn(0f, 1f)
                    }
                    .background(p.menu)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
            ) {
                Column(
                    Modifier
                        .offset { IntOffset(menu.left.roundToInt(), menu.top.roundToInt()) }
                        .requiredSize(MenuW, MenuH)
                        .graphicsLayer { alpha = ((t.value - 0.45f) / 0.45f).coerceIn(0f, 1f) }
                        .padding(8.dp)
                ) {
                    // важное действие — акцентная иконка, остальные серые
                    MenuItem("Новый чат", open, { PathIcon(Glyphs.PENCIL, p.gt, Modifier.size(24.dp), strokeWidth = 1.8f) }) {
                        onNewChat()
                        onOpen(false)
                    }
                    MenuItem("Прочитать все", open, { PathIcon(Glyphs.READ_ALL, p.sub, Modifier.size(24.dp), strokeWidth = 1.8f) }) {
                        onReadAll()
                        onOpen(false)
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuItem(title: String, enabled: Boolean, icon: @Composable () -> Unit, onClick: () -> Unit) {
    val p = LocalPalette.current
    Row(
        Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(Modifier.width(14.dp))
        Text(title, color = p.txt, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}
