package com.example.edgepulse.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edgepulse.model.PerimeterGeometry
import com.example.edgepulse.ui.theme.BackgroundDark
import com.example.edgepulse.ui.theme.CyanAccent
import com.example.edgepulse.ui.theme.CyanAccentGlow
import com.example.edgepulse.ui.theme.HazardPrimary
import com.example.edgepulse.ui.theme.PerimeterInactive
import com.example.edgepulse.ui.theme.TextMuted
import com.example.edgepulse.ui.theme.TextWhite
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.85f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val orbitProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit"
    )

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        scaleAnim.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        delay(1300)
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .clickable { onNavigateToHome() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Pulse Square Logo
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(110.dp)) {
                    val side = size.width
                    // Outer square glow
                    drawRect(
                        color = CyanAccentGlow,
                        topLeft = Offset(0f, 0f),
                        size = Size(side, side),
                        style = Stroke(width = 8f)
                    )
                    // Sharp border
                    drawRect(
                        color = PerimeterInactive,
                        topLeft = Offset(0f, 0f),
                        size = Size(side, side),
                        style = Stroke(width = 3f)
                    )

                    // Orbiting cyan pulse dot
                    val normPt = PerimeterGeometry.perimeterToPoint(orbitProgress)
                    val dotPos = Offset(normPt.x * side, normPt.y * side)

                    drawCircle(CyanAccentGlow, radius = 12f, center = dotPos)
                    drawCircle(CyanAccent, radius = 6.5f, center = dotPos)
                    drawCircle(Color.White, radius = 3f, center = dotPos)

                    // Hazard spark at opposite side
                    val oppPt = PerimeterGeometry.perimeterToPoint((orbitProgress + 0.5f) % 1f)
                    val oppPos = Offset(oppPt.x * side, oppPt.y * side)
                    drawCircle(HazardPrimary, radius = 4f, center = oppPos)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "EDGEPULSE",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 6.sp,
                    color = TextWhite
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "STAY ON THE EDGE. NEVER GET CAUGHT.",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 2.sp,
                    color = CyanAccent,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "TAP TO START",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted.copy(alpha = 0.6f),
                    letterSpacing = 1.5.sp
                )
            )
        }
    }
}
