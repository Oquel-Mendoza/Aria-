package com.example.ui.widgets

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalPremiumTheme

@Composable
fun SavingGoalProgressRing(
    progress: Float, // 0.0 to 1.0
    colorHex: String,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 10.dp
) {
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 1000)
        )
    }

    val baseColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        LocalPremiumTheme.current.accent
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sizeMin = size.minDimension
            val strokeWidthPx = strokeWidth.toPx()
            val radius = (sizeMin - strokeWidthPx) / 2f
            val centerOffset = Offset(size.width / 2f, size.height / 2f)

            // background arc
            drawCircle(
                color = baseColor.copy(alpha = 0.15f),
                radius = radius,
                center = centerOffset,
                style = Stroke(width = strokeWidthPx)
            )

            // active animated arc
            drawArc(
                color = baseColor,
                startAngle = -90f,
                sweepAngle = animatedProgress.value * 360f,
                useCenter = false,
                topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = LocalPremiumTheme.current.onBackground
            )
            Text(
                text = "meta",
                style = MaterialTheme.typography.labelSmall,
                color = LocalPremiumTheme.current.secondary,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
fun PremiumDonutChart(
    slices: List<Pair<Float, Color>>, // Value, Color
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 26.dp
) {
    val animatedScale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animatedScale.animateTo(1f, animationSpec = tween(1200))
    }

    val total = slices.sumOf { it.first.toDouble() }.toFloat()

    Canvas(modifier = modifier) {
        val strokeWidthPx = strokeWidth.toPx()
        val radius = (size.minDimension - strokeWidthPx) / 2f
        val centerOffset = Offset(size.width / 2f, size.height / 2f)
        val arcSize = Size(radius * 2, radius * 2)
        val arcTopLeft = Offset(centerOffset.x - radius, centerOffset.y - radius)

        if (total == 0f) {
            // Draw empty state circle
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.3f),
                radius = radius,
                center = centerOffset,
                style = Stroke(width = strokeWidthPx)
            )
        } else {
            var currentStartAngle = -90f
            for ((value, color) in slices) {
                if (value <= 0) continue
                val sweepAngle = (value / total) * 360f * animatedScale.value
                drawArc(
                    color = color,
                    startAngle = currentStartAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidthPx)
                )
                currentStartAngle += sweepAngle
            }
        }
    }
}

@Composable
fun SmoothLineChart(
    points: List<Float>, // Y coords indices, value represents amount
    modifier: Modifier = Modifier,
    lineColor: Color = LocalPremiumTheme.current.primary,
    gradientColors: List<Color> = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent)
) {
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animationProgress.animateTo(1f, animationSpec = tween(1000))
    }

    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas

        val maxVal = (points.maxOrNull() ?: 1f).coerceAtLeast(1f)
        val minVal = points.minOrNull() ?: 0f
        val range = (maxVal - minVal).coerceAtLeast(1f)

        val width = size.width
        val height = size.height

        val stepX = width / (points.size - 1)
        val path = Path()
        val connectionPoints = mutableListOf<Offset>()

        // Generate canvas points
        for (i in points.indices) {
            val x = i * stepX
            // Normalización e inversión para las coordenadas del Canvas (0 es arriba)
            val normalizedY = (points[i] - minVal) / range
            val y = height - (normalizedY * height * 0.8f + height * 0.1f)
            connectionPoints.add(Offset(x, y))
        }

        // Dibuja gridlines de fondo
        val gridLines = 4
        for (g in 0..gridLines) {
            val yGrid = height * 0.1f + (height * 0.8f * g / gridLines)
            drawLine(
                color = lineColor.copy(alpha = 0.08f),
                start = Offset(0f, yGrid),
                end = Offset(width, yGrid),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Crear curvas de Bezier suaves
        path.moveTo(connectionPoints[0].x, connectionPoints[0].y)
        for (i in 0 until connectionPoints.size - 1) {
            val p0 = connectionPoints[i]
            val p1 = connectionPoints[i + 1]
            val controlPointX1 = p0.x + (p1.x - p0.x) / 2
            val controlPointY1 = p0.y
            val controlPointX2 = p0.x + (p1.x - p0.x) / 2
            val controlPointY2 = p1.y

            path.cubicTo(
                controlPointX1, controlPointY1,
                controlPointX2, controlPointY2,
                p1.x, p1.y
            )
        }

        // Crear una versión recortada para animar el trazo
        val pathMeasure = PathMeasure()
        pathMeasure.setPath(path, false)
        val animatedPath = Path()
        pathMeasure.getSegment(0f, pathMeasure.length * animationProgress.value, animatedPath, true)

        // Dibuja el gradiente debajo de la línea
        if (animationProgress.value > 0f) {
            val fillPath = Path()
            fillPath.addPath(animatedPath)
            // Cierra el polígono hacia abajo de forma limpia
            val currentProgressX = stepX * (points.size - 1) * animationProgress.value
            val lastY = connectionPoints.lastOrNull()?.y ?: height
            
            fillPath.lineTo(currentProgressX, height)
            fillPath.lineTo(0f, height)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = gradientColors,
                    startY = 0f,
                    endY = height
                )
            )
        }

        // Dibuja la línea bezier principal
        drawPath(
            path = animatedPath,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Dibuja puntos coordinados en las cumbres principales
        for (i in connectionPoints.indices) {
            if (i == 0 || i == connectionPoints.size - 1 || points[i] == maxVal) {
                val pt = connectionPoints[i]
                if (pt.x <= stepX * (points.size - 1) * animationProgress.value) {
                    drawCircle(
                        color = Color.White,
                        radius = 5.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = lineColor,
                        radius = 4.dp.toPx(),
                        center = pt,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }
    }
}
