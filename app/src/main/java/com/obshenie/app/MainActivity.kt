package com.obshenie.app

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntSize
import androidx.core.view.WindowCompat
import androidx.core.view.drawToBitmap
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { MessengerApp() }
    }
}

private class Reveal(val shot: ImageBitmap, val center: Offset, val toDark: Boolean)

@Composable
fun MessengerApp() {
    var dark by rememberSaveable { mutableStateOf(false) }
    val systemDark = isSystemInDarkTheme()
    var initialised by rememberSaveable { mutableStateOf(false) }
    if (!initialised) {
        dark = systemDark
        initialised = true
    }

    val palette = if (dark) DarkPalette else LightPalette
    val view = LocalView.current
    SideEffect {
        val window = view.context.findActivity()?.window ?: return@SideEffect
        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.TRANSPARENT
        if (Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced = false
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = !dark
        controller.isAppearanceLightNavigationBars = !dark
    }

    // смена темы «волной» из кнопки: снимок старого экрана поверх, в нём растёт (или сжимается) круг
    var reveal by remember { mutableStateOf<Reveal?>(null) }
    val revealT = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val toggleTheme: (Offset) -> Unit = { from ->
        if (reveal == null) {
            val toDark = !dark
            captureWindow(view) { bmp ->
                if (bmp == null) {
                    dark = toDark
                } else {
                    reveal = Reveal(bmp.asImageBitmap(), from, toDark)
                    dark = toDark
                    scope.launch {
                        revealT.snapTo(0f)
                        revealT.animateTo(1f, tween(560, easing = FastOutSlowInEasing))
                        reveal = null
                    }
                }
            }
        }
    }

    CompositionLocalProvider(LocalPalette provides palette) {
        Box(Modifier.fillMaxSize()) {
            ChatsScreen(onToggleTheme = toggleTheme)

            // в тёмную — из кнопки растёт «дыра» с новой темой, в светлую — старая тёмная сжимается в кнопку
            reveal?.let { r ->
                Canvas(
                    Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) { detectTapGestures { } } // пока идёт анимация, нажатия не проходят
                ) {
                    val maxR = maxOf(
                        hypot(r.center.x, r.center.y),
                        hypot(size.width - r.center.x, r.center.y),
                        hypot(r.center.x, size.height - r.center.y),
                        hypot(size.width - r.center.x, size.height - r.center.y),
                    )
                    val k = revealT.value
                    val radius = if (r.toDark) maxR * k else maxR * (1f - k)
                    val hole = Path().apply { addOval(Rect(r.center, radius)) }
                    clipPath(hole, clipOp = if (r.toDark) ClipOp.Difference else ClipOp.Intersect) {
                        drawImage(r.shot, dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()))
                    }
                }
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var c: Context? = this
    while (c is ContextWrapper && c !is Activity) c = c.baseContext
    return c as? Activity
}

/** Снимок окна приложения (PixelCopy, на старых Android — через draw). null — снять не вышло. */
private fun captureWindow(view: View, done: (Bitmap?) -> Unit) {
    val win = view.context.findActivity()?.window
    val root = win?.decorView
    if (win == null || root == null || root.width <= 0 || root.height <= 0) {
        done(null)
        return
    }
    try {
        if (Build.VERSION.SDK_INT >= 26) {
            val bmp = Bitmap.createBitmap(root.width, root.height, Bitmap.Config.ARGB_8888)
            PixelCopy.request(win, bmp, { res ->
                if (res == PixelCopy.SUCCESS) done(bmp) else {
                    bmp.recycle()
                    done(null)
                }
            }, Handler(Looper.getMainLooper()))
        } else {
            done(root.drawToBitmap())
        }
    } catch (e: Throwable) {
        done(null)
    }
}
