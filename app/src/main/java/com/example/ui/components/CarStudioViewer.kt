package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CarPaintFinish
import com.example.data.model.VehicleModel3D
import com.example.ui.theme.A2ATextMuted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

@Composable
fun CarStudioViewer(
    model: VehicleModel3D,
    paintColorLong: Long,
    finish: CarPaintFinish,
    glassColorLong: Long,
    autoOrbit: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var dragYaw by remember { mutableFloatStateOf(0f) }
    var processedBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(model.id, paintColorLong, finish.id, glassColorLong) {
        processedBitmap = withContext(Dispatchers.Default) {
            renderTintedCarBitmap(
                context = context,
                resId = model.imageRes,
                paintColorLong = paintColorLong,
                finish = finish,
                glassColorLong = glassColorLong
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "car_orbit")
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = -14f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbit_yaw"
    )

    val animatedYaw by animateFloatAsState(
        targetValue = if (autoOrbit) orbitAngle else dragYaw,
        animationSpec = spring(stiffness = 220f, dampingRatio = 0.72f),
        label = "car_yaw"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(215.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FAFC),
                        Color(0xFFEEF2F7),
                        Color(0xFFE2E8F0).copy(alpha = 0.55f)
                    )
                )
            )
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        dragYaw = (dragYaw + dragAmount * 0.2f).coerceIn(-28f, 28f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // 3D turntable ring and studio floor shadow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawOval(
                color = Color(0xFFCBD5E1).copy(alpha = 0.55f),
                topLeft = Offset(w * 0.14f, h * 0.68f),
                size = Size(w * 0.72f, h * 0.22f),
                style = Stroke(width = 2.5f)
            )
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F172A).copy(alpha = 0.22f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.78f),
                    radius = w * 0.36f
                ),
                topLeft = Offset(w * 0.16f, h * 0.66f),
                size = Size(w * 0.68f, h * 0.22f)
            )
        }

        val bmp = processedBitmap
        val carModifier = Modifier
            .fillMaxWidth(0.92f)
            .height(178.dp)
            .graphicsLayer {
                rotationY = animatedYaw
                rotationX = -abs(animatedYaw) * 0.08f
                cameraDistance = 14f * density
                translationY = if (autoOrbit) sin(orbitAngle * 0.15f) * 3f else 0f
            }

        if (bmp != null) {
            Image(
                bitmap = bmp,
                contentDescription = model.displayName,
                modifier = carModifier,
                contentScale = ContentScale.Fit
            )
        } else {
            Image(
                painter = painterResource(id = model.imageRes),
                contentDescription = model.displayName,
                modifier = carModifier,
                contentScale = ContentScale.Fit
            )
        }

        Text(
            text = "ลากซ้าย-ขวาเพื่อหมุนมุมมอง 3D • ${finish.label}",
            color = A2ATextMuted,
            fontSize = 10.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
        )
    }
}

private fun renderTintedCarBitmap(
    context: Context,
    resId: Int,
    paintColorLong: Long,
    finish: CarPaintFinish,
    glassColorLong: Long
): ImageBitmap? {
    return runCatching {
        val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
        val decoded = BitmapFactory.decodeResource(context.resources, resId, opts) ?: return null
        val w = decoded.width
        val h = decoded.height
        val pixels = IntArray(w * h)
        decoded.getPixels(pixels, 0, w, 0, 0, w, h)
        decoded.recycle()

        // 1. Flood-fill from 4 corners to remove pure white studio background cleanly
        val visited = BooleanArray(w * h)
        val queue = IntArray(w * h)
        var head = 0
        var tail = 0

        fun isStudioBg(argb: Int): Boolean {
            val r = (argb ushr 16) and 0xFF
            val g = (argb ushr 8) and 0xFF
            val b = argb and 0xFF
            val maxC = max(r, max(g, b))
            val minC = min(r, min(g, b))
            return r >= 224 && g >= 224 && b >= 224 && (maxC - minC) <= 20
        }

        fun enqueue(x: Int, y: Int) {
            if (x in 0 until w && y in 0 until h) {
                val idx = y * w + x
                if (!visited[idx] && isStudioBg(pixels[idx])) {
                    visited[idx] = true
                    queue[tail++] = idx
                }
            }
        }

        enqueue(0, 0)
        enqueue(w - 1, 0)
        enqueue(0, h - 1)
        enqueue(w - 1, h - 1)

        while (head < tail) {
            val idx = queue[head++]
            pixels[idx] = 0x00000000
            val x = idx % w
            val y = idx / w
            enqueue(x + 1, y)
            enqueue(x - 1, y)
            enqueue(x, y + 1)
            enqueue(x, y - 1)
        }

        // 2. Apply Car Studio paint & glass tint to car bodywork pixels
        val targetR = ((paintColorLong ushr 16) and 0xFF).toFloat() / 255f
        val targetG = ((paintColorLong ushr 8) and 0xFF).toFloat() / 255f
        val targetB = (paintColorLong and 0xFF).toFloat() / 255f
        val isWhitePaint = paintColorLong == 0xFFF8FAFC

        val glassR = ((glassColorLong ushr 16) and 0xFF).toFloat() / 255f
        val glassG = ((glassColorLong ushr 8) and 0xFF).toFloat() / 255f
        val glassB = (glassColorLong and 0xFF).toFloat() / 255f

        for (i in pixels.indices) {
            val argb = pixels[i]
            val a = (argb ushr 24) and 0xFF
            if (a == 0) continue

            val r = ((argb ushr 16) and 0xFF) / 255f
            val g = ((argb ushr 8) and 0xFF) / 255f
            val b = (argb and 0xFF) / 255f
            val maxC = max(r, max(g, b))
            val minC = min(r, min(g, b))
            val sat = if (maxC > 0f) (maxC - minC) / maxC else 0f
            val lum = 0.299f * r + 0.587f * g + 0.114f * b

            // Tint neutral bodywork pixels
            if (!isWhitePaint && sat < 0.24f && lum > 0.36f) {
                val shadedLum = when (finish) {
                    CarPaintFinish.GLOSS -> (lum * 1.05f).coerceIn(0f, 1f)
                    CarPaintFinish.METALLIC -> (lum * 0.96f + 0.08f * lum * lum).coerceIn(0f, 1f)
                    CarPaintFinish.MATTE -> (0.12f + lum * 0.72f).coerceIn(0f, 1f)
                }
                val newR = (targetR * shadedLum * 255f).toInt().coerceIn(0, 255)
                val newG = (targetG * shadedLum * 255f).toInt().coerceIn(0, 255)
                val newB = (targetB * shadedLum * 255f).toInt().coerceIn(0, 255)
                pixels[i] = (a shl 24) or (newR shl 16) or (newG shl 8) or newB
            } else if (sat < 0.25f && lum in 0.14f..0.34f) {
                // Subtle glass tint on dark window reflections
                val blend = 0.25f
                val nr = ((r * (1f - blend) + glassR * lum * blend) * 255f).toInt().coerceIn(0, 255)
                val ng = ((g * (1f - blend) + glassG * lum * blend) * 255f).toInt().coerceIn(0, 255)
                val nb = ((b * (1f - blend) + glassB * lum * blend) * 255f).toInt().coerceIn(0, 255)
                pixels[i] = (a shl 24) or (nr shl 16) or (ng shl 8) or nb
            }
        }

        val outBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        outBitmap.setPixels(pixels, 0, w, 0, 0, w, h)
        outBitmap.asImageBitmap()
    }.getOrNull()
}
