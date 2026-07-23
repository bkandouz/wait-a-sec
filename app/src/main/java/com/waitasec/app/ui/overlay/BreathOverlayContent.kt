package com.waitasec.app.ui.overlay

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.waitasec.app.ui.theme.BodyFontFamily
import com.waitasec.app.ui.theme.Cream
import com.waitasec.app.ui.theme.DisplayFontFamily
import com.waitasec.app.ui.theme.Mist
import com.waitasec.app.ui.theme.SageDeep
import com.waitasec.app.ui.theme.SageMid
import com.waitasec.app.ui.theme.SagePale
import com.waitasec.app.ui.theme.SageSoft
import kotlinx.coroutines.delay
import kotlin.math.ceil

@Composable
fun BreathOverlayContent(
    delaySeconds: Int,
    appLabel: String,
    onComplete: () -> Unit,
    onLeave: () -> Unit,
) {
    var remainingMs by remember(delaySeconds) { mutableIntStateOf(delaySeconds * 1000) }

    LaunchedEffect(delaySeconds) {
        remainingMs = delaySeconds * 1000
        while (remainingMs > 0) {
            delay(50)
            remainingMs = (remainingMs - 50).coerceAtLeast(0)
        }
        onComplete()
    }

    // One full inhale+exhale cycle ~ 4s (2s each)
    val breath = rememberInfiniteTransition(label = "breath")
    val scale by breath.animateFloat(
        initialValue = 0.72f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathScale",
    )
    val phaseProgress by breath.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase",
    )
    val cue = if (phaseProgress < 0.5f) "Inhale" else "Exhale"
    val remainingSec = ceil(remainingMs / 1000.0).toInt().coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SageDeep.copy(alpha = 0.97f),
                        SageMid.copy(alpha = 0.95f),
                        Mist.copy(alpha = 0.98f),
                    ),
                ),
            )
            .systemBarsPadding()
            .padding(28.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Wait a sec…",
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = DisplayFontFamily),
                color = Cream,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Before opening $appLabel",
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = BodyFontFamily),
                color = SagePale,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.weight(1f))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(260.dp)) {
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(scale)
                        .background(SageSoft.copy(alpha = 0.35f), CircleShape),
                )
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(scale * 0.92f)
                        .background(Cream.copy(alpha = 0.92f), CircleShape),
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = cue,
                        style = MaterialTheme.typography.headlineMedium.copy(fontFamily = DisplayFontFamily),
                        color = SageDeep,
                    )
                    Text(
                        text = "${remainingSec}s",
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = BodyFontFamily),
                        color = SageMid,
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "Breathe with the circle.\nIs this how you want to spend your time?",
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = BodyFontFamily),
                color = Cream.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = onLeave) {
                Text(
                    text = "Leave",
                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = BodyFontFamily),
                    color = Cream,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { /* wait must complete */ },
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = Cream.copy(alpha = 0.2f),
                    disabledContentColor = Cream.copy(alpha = 0.7f),
                ),
            ) {
                Text(
                    text = if (remainingSec > 0) "Opening in ${remainingSec}s" else "Opening…",
                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = BodyFontFamily),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
