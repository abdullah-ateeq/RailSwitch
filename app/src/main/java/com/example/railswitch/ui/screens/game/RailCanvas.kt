package com.example.railswitch.ui.screens.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import com.example.railswitch.model.Block
import com.example.railswitch.model.GameState
import com.example.railswitch.model.Rail
import com.example.railswitch.ui.theme.BackgroundBlack
import com.example.railswitch.ui.theme.BlockEdge
import com.example.railswitch.ui.theme.BlockFace
import com.example.railswitch.ui.theme.BlockGlow
import com.example.railswitch.ui.theme.ElectricCyan
import com.example.railswitch.ui.theme.ElectricCyanDark
import com.example.railswitch.ui.theme.ElectricCyanGlow
import com.example.railswitch.ui.theme.ElectricCyanSubtle
import com.example.railswitch.ui.theme.ImpactDanger
import com.example.railswitch.ui.theme.ImpactDangerGlow
import com.example.railswitch.ui.theme.PlayerCore
import com.example.railswitch.ui.theme.PlayerPulseGlow
import com.example.railswitch.ui.theme.RailGlow
import com.example.railswitch.ui.theme.RailTrackBase
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun RailCanvas(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    // Continuous subtle pulse animation for player glow
    val pulseAnim = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        pulseAnim.animateTo(
            targetValue = 1.35f,
            animationSpec = infiniteRepeatable(
                animation = tween(850, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    // Dodge pulse ring animation
    val dodgeRingRadius = remember { Animatable(0f) }
    val dodgeRingAlpha = remember { Animatable(0f) }

    LaunchedEffect(gameState.blocksAvoided) {
        if (gameState.blocksAvoided > 0) {
            dodgeRingRadius.snapTo(10f)
            dodgeRingAlpha.snapTo(0.85f)
            dodgeRingRadius.animateTo(
                targetValue = 85f,
                animationSpec = tween(320, easing = FastOutSlowInEasing)
            )
            dodgeRingAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(320, easing = LinearEasing)
            )
        }
    }

    // Dynamic track sleeper offset
    val trackScroll = remember { Animatable(0f) }
    LaunchedEffect(gameState.isGameOver, gameState.isPaused) {
        if (!gameState.isGameOver && !gameState.isPaused) {
            trackScroll.animateTo(
                targetValue = 1000f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Shake offset for impact effect
        val shakeX = if (gameState.shakeIntensity > 0f) {
            (Random.nextFloat() * 2f - 1f) * gameState.shakeIntensity * 24f
        } else 0f
        val shakeY = if (gameState.shakeIntensity > 0f) {
            (Random.nextFloat() * 2f - 1f) * gameState.shakeIntensity * 16f
        } else 0f

        translate(left = shakeX, top = shakeY) {
            // Virtual coordinate scale (virtual height = 1000f)
            val scaleY = canvasHeight / 1000f

            // Responsive rail positions
            val centerX = canvasWidth / 2f
            val maxSpacing = canvasWidth * 0.40f
            val railSpacing = maxSpacing.coerceIn(120f, 260f)
            val leftRailX = centerX - railSpacing / 2f
            val rightRailX = centerX + railSpacing / 2f

            // 1. Draw subtle ambient backdrop
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BackgroundBlack,
                        Color(0xFF0D121B),
                        BackgroundBlack
                    )
                ),
                size = size
            )

            // 2. Draw Rail Sleepers / Ties for speed sensation
            val sleeperSpacing = 48f * scaleY
            val scrollOffset = (trackScroll.value * scaleY) % sleeperSpacing
            var sleeperY = -sleeperSpacing + scrollOffset
            while (sleeperY < canvasHeight + sleeperSpacing) {
                val alpha = (sin(sleeperY / canvasHeight * 3.14159f) * 0.12f).coerceIn(0.02f, 0.12f)
                drawLine(
                    color = ElectricCyan.copy(alpha = alpha),
                    start = Offset(leftRailX - 16f, sleeperY),
                    end = Offset(rightRailX + 16f, sleeperY),
                    strokeWidth = 2f
                )
                sleeperY += sleeperSpacing
            }

            // 3. Draw The Two Parallel Rails
            drawRail(leftRailX, canvasHeight)
            drawRail(rightRailX, canvasHeight)

            // 4. Draw Incoming Blocks
            val blockWidth = (railSpacing * 0.52f).coerceIn(52f, 96f)
            for (block in gameState.blocks) {
                val blockRailX = if (block.rail == Rail.LEFT) leftRailX else rightRailX
                val blockTopPx = block.y * scaleY
                val blockHeightPx = block.height * scaleY

                drawObstacleBlock(
                    centerX = blockRailX,
                    topY = blockTopPx,
                    width = blockWidth,
                    height = blockHeightPx
                )
            }

            // 5. Draw Player Marker
            val playerPixelX = leftRailX + (rightRailX - leftRailX) * gameState.railProgress
            val playerPixelY = gameState.playerY * scaleY
            val playerRadiusPx = gameState.playerRadius * (canvasWidth / 380f).coerceIn(0.85f, 1.25f)

            // Dodge ripple pulse ring
            if (dodgeRingAlpha.value > 0f) {
                drawCircle(
                    color = ElectricCyan.copy(alpha = dodgeRingAlpha.value),
                    radius = dodgeRingRadius.value * (canvasWidth / 400f),
                    center = Offset(playerPixelX, playerPixelY),
                    style = Stroke(width = 3f)
                )
            }

            // Draw glowing player marker
            drawPlayerMarker(
                centerX = playerPixelX,
                centerY = playerPixelY,
                radius = playerRadiusPx,
                pulseScale = pulseAnim.value,
                isGameOver = gameState.isGameOver
            )

            // 6. Draw impact overlay if collision occurred
            if (gameState.shakeIntensity > 0f) {
                drawRect(
                    color = ImpactDanger.copy(alpha = (gameState.shakeIntensity * 0.35f).coerceIn(0f, 0.4f)),
                    size = size
                )
            }
        }
    }
}

private fun DrawScope.drawRail(railX: Float, height: Float) {
    // Outer glow
    drawLine(
        color = RailGlow.copy(alpha = 0.18f),
        start = Offset(railX, 0f),
        end = Offset(railX, height),
        strokeWidth = 10f,
        cap = StrokeCap.Round
    )
    // Base track structure
    drawLine(
        color = RailTrackBase,
        start = Offset(railX, 0f),
        end = Offset(railX, height),
        strokeWidth = 4f,
        cap = StrokeCap.Round
    )
    // Core luminous line
    drawLine(
        color = ElectricCyan.copy(alpha = 0.85f),
        start = Offset(railX, 0f),
        end = Offset(railX, height),
        strokeWidth = 1.5f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawObstacleBlock(
    centerX: Float,
    topY: Float,
    width: Float,
    height: Float
) {
    val left = centerX - width / 2f
    val cornerRadius = CornerRadius(8f, 8f)

    // Subtle glow backing
    drawRoundRect(
        color = BlockGlow,
        topLeft = Offset(left - 2f, topY - 2f),
        size = Size(width + 4f, height + 4f),
        cornerRadius = CornerRadius(10f, 10f)
    )

    // Obstacle solid body
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF223247),
                BlockFace
            ),
            startY = topY,
            endY = topY + height
        ),
        topLeft = Offset(left, topY),
        size = Size(width, height),
        cornerRadius = cornerRadius
    )

    // Geometric edge accent
    drawRoundRect(
        color = BlockEdge,
        topLeft = Offset(left, topY),
        size = Size(width, height),
        cornerRadius = cornerRadius,
        style = Stroke(width = 2f)
    )

    // Center warning chevron/notch
    val midY = topY + height / 2f
    val notchPath = Path().apply {
        moveTo(centerX - 10f, midY - 4f)
        lineTo(centerX, midY + 4f)
        lineTo(centerX + 10f, midY - 4f)
    }
    drawPath(
        path = notchPath,
        color = ElectricCyan.copy(alpha = 0.7f),
        style = Stroke(width = 2f, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawPlayerMarker(
    centerX: Float,
    centerY: Float,
    radius: Float,
    pulseScale: Float,
    isGameOver: Boolean
) {
    if (isGameOver) {
        // Red impact shattered marker
        drawCircle(
            color = ImpactDangerGlow,
            radius = radius * 2.2f,
            center = Offset(centerX, centerY)
        )
        drawCircle(
            color = ImpactDanger,
            radius = radius * 0.9f,
            center = Offset(centerX, centerY)
        )
        return
    }

    // 1. Ambient outer pulse aura
    drawCircle(
        color = PlayerPulseGlow.copy(alpha = 0.35f),
        radius = radius * 2.0f * pulseScale,
        center = Offset(centerX, centerY)
    )

    // 2. Inner cyan bloom
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                ElectricCyan,
                ElectricCyanSubtle,
                Color.Transparent
            ),
            center = Offset(centerX, centerY),
            radius = radius * 1.5f
        ),
        radius = radius * 1.5f,
        center = Offset(centerX, centerY)
    )

    // 3. Sleek diamond / arrowhead ship marker
    val markerPath = Path().apply {
        moveTo(centerX, centerY - radius * 1.25f) // Top tip
        lineTo(centerX + radius * 0.95f, centerY + radius * 0.8f) // Bottom right
        lineTo(centerX, centerY + radius * 0.3f) // Inner notch
        lineTo(centerX - radius * 0.95f, centerY + radius * 0.8f) // Bottom left
        close()
    }

    drawPath(
        path = markerPath,
        color = ElectricCyanDark
    )
    drawPath(
        path = markerPath,
        color = ElectricCyan,
        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
    )

    // 4. White core photon dot
    drawCircle(
        color = PlayerCore,
        radius = radius * 0.35f,
        center = Offset(centerX, centerY - radius * 0.15f)
    )
}
