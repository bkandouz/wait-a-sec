package com.waitasec.app.ui.overlay

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.waitasec.app.ui.theme.SoftCoral
import kotlinx.coroutines.delay

@Composable
fun BreathOverlayContent(
    delaySeconds: Int,
    appLabel: String,
    onContinue: () -> Unit,
    onQuit: () -> Unit,
) {
    // Encourage a short breath before "continue" unlocks; quit is always available.
    var continueUnlocked by remember { mutableStateOf(delaySeconds <= 0) }

    LaunchedEffect(delaySeconds) {
        continueUnlocked = delaySeconds <= 0
        if (delaySeconds > 0) {
            delay(delaySeconds * 1000L)
            continueUnlocked = true
        }
    }

    // Deep breath: ~5s inhale, ~5s exhale (10s full cycle)
    val inhaleExhaleMs = 5_000
    val fullBreathMs = inhaleExhaleMs * 2

    val breath = rememberInfiniteTransition(label = "breath")
    val scale by breath.animateFloat(
        initialValue = 0.68f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = inhaleExhaleMs, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathScale",
    )
    val phaseProgress by breath.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = fullBreathMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase",
    )
    val cue = if (phaseProgress < 0.5f) "Inhale" else "Exhale"

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
                text = "You were about to open $appLabel",
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
                Text(
                    text = cue,
                    style = MaterialTheme.typography.headlineMedium.copy(fontFamily = DisplayFontFamily),
                    color = SageDeep,
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "Breathe with the circle.\nWhat do you actually want right now?",
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = BodyFontFamily),
                color = Cream.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onQuit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cream,
                    contentColor = SageDeep,
                ),
            ) {
                Text(
                    text = "Quit to something productive",
                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = BodyFontFamily),
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onContinue,
                enabled = continueUnlocked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = SoftCoral,
                    disabledContentColor = Cream.copy(alpha = 0.45f),
                ),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = if (continueUnlocked) SoftCoral.copy(alpha = 0.85f) else Cream.copy(alpha = 0.3f),
                ),
            ) {
                Text(
                    text = if (continueUnlocked) {
                        "Continue — burn your time"
                    } else {
                        "Breathe first…"
                    },
                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = BodyFontFamily),
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
