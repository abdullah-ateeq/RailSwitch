package com.example.edgepulse.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.edgepulse.model.CrossBeamHazard
import com.example.edgepulse.model.GameState
import com.example.edgepulse.model.Hazard
import com.example.edgepulse.model.PerimeterBlockHazard
import com.example.edgepulse.model.PerimeterGeometry
import com.example.edgepulse.model.PerimeterSurgeHazard
import com.example.edgepulse.ui.theme.CyanAccent
import com.example.edgepulse.ui.theme.CyanAccentGlow
import com.example.edgepulse.ui.theme.HazardPrimary
import com.example.edgepulse.ui.theme.HazardPrimaryGlow
import com.example.edgepulse.ui.theme.HazardWarning
import com.example.edgepulse.ui.theme.PerimeterGlow
import com.example.edgepulse.ui.theme.PerimeterInactive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun GameArena(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Calculate responsive centered square arena
        val arenaSize = min(width, height) * 0.74f
        val squareLeft = (width - arenaSize) / 2.0f
        val squareTop = (height - arenaSize) / 2.0f

        // Screen shake calculation on collision
        val impactElapsed = if (gameState.lastImpactTime > 0f) {
            gameState.survivalTimeSec - gameState.lastImpactTime
        } else {
            100f
        }

        var shakeOffsetX = 0f
        var shakeOffsetY = 0f
        if (impactElapsed in 0f..0.35f) {
            val shakeIntensity = (1.0f - impactElapsed / 0.35f) * 16f
            val rand = Random((impactElapsed * 1000).toInt())
            shakeOffsetX = (rand.nextFloat() * 2f - 1f) * shakeIntensity
            shakeOffsetY = (rand.nextFloat() * 2f - 1f) * shakeIntensity
        }

        withTransform({
            translate(shakeOffsetX, shakeOffsetY)
        }) {
            // 1. Draw subtle ambient arena backing
            drawArenaBacking(squareLeft, squareTop, arenaSize)

            // 2. Draw square perimeter track
            drawPerimeterTrack(squareLeft, squareTop, arenaSize)

            // 3. Draw lap pulse wave if recently completed a lap
            drawLapPulseWave(gameState, squareLeft, squareTop, arenaSize)

            // 4. Draw all hazards
            for (hazard in gameState.hazards) {
                drawHazard(hazard, gameState.survivalTimeSec, squareLeft, squareTop, arenaSize)
            }

            // 5. Draw player trail and player glowing dot
            drawPlayer(gameState, squareLeft, squareTop, arenaSize)

            // 6. Impact flash overlay
            if (impactElapsed in 0f..0.22f) {
                val flashAlpha = (1.0f - impactElapsed / 0.22f) * 0.45f
                drawRect(
                    color = HazardPrimary.copy(alpha = flashAlpha),
                    size = size,
                    blendMode = BlendMode.SrcOver
                )
            }
        }
    }
}

private fun DrawScope.drawArenaBacking(left: Float, top: Float, size: Float) {
    // Subtle dark center surface
    drawRect(
        color = Color(0xFF0F131C).copy(alpha = 0.5f),
        topLeft = Offset(left, top),
        size = Size(size, size)
    )

    // Subtle center crosshairs/accent
    val center = Offset(left + size / 2f, top + size / 2f)
    val crossArm = size * 0.04f
    val crossColor = Color(0xFF1E2838).copy(alpha = 0.6f)
    drawLine(crossColor, Offset(center.x - crossArm, center.y), Offset(center.x + crossArm, center.y), strokeWidth = 1.5f)
    drawLine(crossColor, Offset(center.x, center.y - crossArm), Offset(center.x, center.y + crossArm), strokeWidth = 1.5f)
}

private fun DrawScope.drawPerimeterTrack(left: Float, top: Float, size: Float) {
    // Outer glow
    drawRect(
        color = PerimeterGlow,
        topLeft = Offset(left - 2f, top - 2f),
        size = Size(size + 4f, size + 4f),
        style = Stroke(width = 6f)
    )

    // Main sharp perimeter line
    drawRect(
        color = PerimeterInactive,
        topLeft = Offset(left, top),
        size = Size(size, size),
        style = Stroke(width = 2.5f)
    )

    // Corner accent brackets
    val cornerLen = 14f
    val cornerColor = CyanAccent.copy(alpha = 0.8f)
    val stroke = 2.5f

    // Top-Left
    drawLine(cornerColor, Offset(left, top), Offset(left + cornerLen, top), strokeWidth = stroke)
    drawLine(cornerColor, Offset(left, top), Offset(left, top + cornerLen), strokeWidth = stroke)

    // Top-Right
    drawLine(cornerColor, Offset(left + size, top), Offset(left + size - cornerLen, top), strokeWidth = stroke)
    drawLine(cornerColor, Offset(left + size, top), Offset(left + size, top + cornerLen), strokeWidth = stroke)

    // Bottom-Right
    drawLine(cornerColor, Offset(left + size, top + size), Offset(left + size - cornerLen, top + size), strokeWidth = stroke)
    drawLine(cornerColor, Offset(left + size, top + size), Offset(left + size, top + size - cornerLen), strokeWidth = stroke)

    // Bottom-Left
    drawLine(cornerColor, Offset(left, top + size), Offset(left + cornerLen, top + size), strokeWidth = stroke)
    drawLine(cornerColor, Offset(left, top + size), Offset(left, top + size - cornerLen), strokeWidth = stroke)
}

private fun DrawScope.drawLapPulseWave(
    gameState: GameState,
    left: Float,
    top: Float,
    size: Float
) {
    if (gameState.lastLapPulseTime < 0f) return
    val elapsed = gameState.survivalTimeSec - gameState.lastLapPulseTime
    if (elapsed !in 0f..0.55f) return

    val progress = elapsed / 0.55f
    val expansion = size * 0.18f * progress
    val alpha = (1.0f - progress) * 0.7f

    drawRect(
        color = CyanAccent.copy(alpha = alpha),
        topLeft = Offset(left - expansion, top - expansion),
        size = Size(size + expansion * 2f, size + expansion * 2f),
        style = Stroke(width = 3.5f * (1.0f - progress))
    )
}

private fun DrawScope.drawHazard(
    hazard: Hazard,
    currentTimeSec: Float,
    squareLeft: Float,
    squareTop: Float,
    squareSize: Float
) {
    val isTelegraph = hazard.isTelegraphing(currentTimeSec)
    val isActive = hazard.isActive(currentTimeSec)
    if (!isTelegraph && !isActive) return

    val blink = if (isTelegraph) {
        val progress = hazard.telegraphProgress(currentTimeSec)
        // Faster blinking as telegraph time nears end
        val freq = 4.0 + progress * 6.0
        val sinVal = sin(currentTimeSec * freq * 2.0 * PI).toFloat()
        (sinVal * 0.5f + 0.5f).coerceIn(0.2f, 1.0f)
    } else {
        1.0f
    }

    when (hazard) {
        is PerimeterBlockHazard -> {
            val center = if (isActive) hazard.currentCenter(currentTimeSec) else hazard.startPos
            val halfLen = hazard.length / 2.0f
            val startP = PerimeterGeometry.normalize(center - halfLen)
            val endP = PerimeterGeometry.normalize(center + halfLen)

            val color = if (isTelegraph) {
                HazardWarning.copy(alpha = blink * 0.85f)
            } else {
                HazardPrimary
            }

            val strokeWidth = if (isActive) 7f else 3.5f

            drawPerimeterArc(startP, endP, squareLeft, squareTop, squareSize, color, strokeWidth)

            if (isActive) {
                // Outer glow
                drawPerimeterArc(startP, endP, squareLeft, squareTop, squareSize, HazardPrimaryGlow, strokeWidth + 6f)
            }
        }
        is PerimeterSurgeHazard -> {
            val color = if (isTelegraph) {
                HazardWarning.copy(alpha = blink * 0.85f)
            } else {
                HazardPrimary
            }
            val strokeWidth = if (isActive) 8f else 4f

            drawPerimeterArc(hazard.startPos, hazard.endPos, squareLeft, squareTop, squareSize, color, strokeWidth)
            if (isActive) {
                drawPerimeterArc(hazard.startPos, hazard.endPos, squareLeft, squareTop, squareSize, HazardPrimaryGlow, strokeWidth + 8f)
            }
        }
        is CrossBeamHazard -> {
            val pt1 = PerimeterGeometry.toCanvasCoords(
                PerimeterGeometry.perimeterToPoint(hazard.contactP1),
                squareLeft, squareTop, squareSize
            )
            val pt2 = PerimeterGeometry.toCanvasCoords(
                PerimeterGeometry.perimeterToPoint(hazard.contactP2),
                squareLeft, squareTop, squareSize
            )

            val p1Offset = Offset(pt1.x, pt1.y)
            val p2Offset = Offset(pt2.x, pt2.y)

            if (isTelegraph) {
                // Dashed warning line
                drawLine(
                    color = HazardWarning.copy(alpha = blink * 0.8f),
                    start = p1Offset,
                    end = p2Offset,
                    strokeWidth = 2.0f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )
                // Warning contact indicators
                drawCircle(HazardWarning.copy(alpha = blink), radius = 6f, center = p1Offset)
                drawCircle(HazardWarning.copy(alpha = blink), radius = 6f, center = p2Offset)
            } else {
                // Active laser
                drawLine(
                    color = HazardPrimaryGlow,
                    start = p1Offset,
                    end = p2Offset,
                    strokeWidth = 10f
                )
                drawLine(
                    color = HazardPrimary,
                    start = p1Offset,
                    end = p2Offset,
                    strokeWidth = 4f
                )
                drawCircle(HazardPrimary, radius = 9f, center = p1Offset)
                drawCircle(Color.White, radius = 4f, center = p1Offset)
                drawCircle(HazardPrimary, radius = 9f, center = p2Offset)
                drawCircle(Color.White, radius = 4f, center = p2Offset)
            }
        }
    }
}

/**
 * Draws a continuous stroke along the square perimeter between startP and endP.
 * Handles wrapping around corners seamlessly.
 */
private fun DrawScope.drawPerimeterArc(
    startP: Float,
    endP: Float,
    squareLeft: Float,
    squareTop: Float,
    squareSize: Float,
    color: Color,
    strokeWidth: Float
) {
    val normStart = PerimeterGeometry.normalize(startP)
    val normEnd = PerimeterGeometry.normalize(endP)

    val segments = mutableListOf<Pair<Float, Float>>()
    if (normStart <= normEnd) {
        segments.add(normStart to normEnd)
    } else {
        // Wraps through 1.0/0.0
        segments.add(normStart to 1.0f)
        segments.add(0.0f to normEnd)
    }

    for ((segStart, segEnd) in segments) {
        // Split segment at corner boundaries (0.25, 0.50, 0.75)
        val cutPoints = listOf(segStart, 0.25f, 0.50f, 0.75f, segEnd)
            .filter { it in segStart..segEnd }
            .distinct()
            .sorted()

        for (i in 0 until cutPoints.size - 1) {
            val pA = cutPoints[i]
            val pB = cutPoints[i + 1]
            if (pB - pA < 0.0001f) continue

            val ptA = PerimeterGeometry.toCanvasCoords(
                PerimeterGeometry.perimeterToPoint(pA),
                squareLeft, squareTop, squareSize
            )
            val ptB = PerimeterGeometry.toCanvasCoords(
                PerimeterGeometry.perimeterToPoint(pB),
                squareLeft, squareTop, squareSize
            )

            drawLine(
                color = color,
                start = Offset(ptA.x, ptA.y),
                end = Offset(ptB.x, ptB.y),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

private fun DrawScope.drawPlayer(
    gameState: GameState,
    squareLeft: Float,
    squareTop: Float,
    squareSize: Float
) {
    val player = gameState.player

    // 1. Draw motion trail
    val trail = player.trailPoints
    for (i in trail.indices) {
        val trailP = trail[i]
        val pt = PerimeterGeometry.toCanvasCoords(
            PerimeterGeometry.perimeterToPoint(trailP),
            squareLeft, squareTop, squareSize
        )
        val progress = 1.0f - (i.toFloat() / trail.size.coerceAtLeast(1))
        val trailAlpha = progress * 0.40f
        val trailRadius = 3f + progress * 5f

        drawCircle(
            color = CyanAccent.copy(alpha = trailAlpha),
            radius = trailRadius,
            center = Offset(pt.x, pt.y)
        )
    }

    // 2. Draw Player Dot
    val playerPt = PerimeterGeometry.toCanvasCoords(
        PerimeterGeometry.perimeterToPoint(player.position),
        squareLeft, squareTop, squareSize
    )
    val playerCenter = Offset(playerPt.x, playerPt.y)

    // Outer luminous halo
    drawCircle(
        color = CyanAccentGlow,
        radius = 16f,
        center = playerCenter
    )

    // Main electric cyan body
    drawCircle(
        color = CyanAccent,
        radius = 8.5f,
        center = playerCenter
    )

    // Pure white brilliant core
    drawCircle(
        color = Color.White,
        radius = 4f,
        center = playerCenter
    )
}
