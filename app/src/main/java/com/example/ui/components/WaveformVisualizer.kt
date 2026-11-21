package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.PurpleAccent

@Composable
fun WaveformVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 42,
    height: Dp = 48.dp,
    barColor: Color = CyanAccent
) {
    val transition = rememberInfiniteTransition(label = "waveform_anim")

    val anim1 by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_h1"
    )

    val anim2 by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_h2"
    )

    val baseHeights = listOf(
        0.3f, 0.5f, 0.8f, 0.4f, 0.9f, 0.7f, 0.3f, 0.6f, 0.85f, 0.4f,
        0.75f, 0.95f, 0.6f, 0.35f, 0.8f, 0.9f, 0.5f, 0.7f, 0.4f, 0.85f,
        0.65f, 0.9f, 0.75f, 0.4f, 0.85f, 0.55f, 0.3f, 0.95f, 0.6f, 0.45f
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until barCount) {
                val base = baseHeights[i % baseHeights.size]
                val currentScale = if (isPlaying) {
                    if (i % 2 == 0) (base * anim1).coerceIn(0.15f, 1f)
                    else (base * anim2).coerceIn(0.15f, 1f)
                } else {
                    base.coerceIn(0.2f, 0.8f)
                }

                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight(currentScale)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(CyanAccent, ElectricBlue, PurpleAccent)
                            )
                        )
                )
            }
        }
    }
}
