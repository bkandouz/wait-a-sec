package com.waitasec.app.ui.onboarding

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.waitasec.app.ui.theme.Cream
import com.waitasec.app.ui.theme.Mist
import com.waitasec.app.ui.theme.SageDeep
import com.waitasec.app.ui.theme.SageMid
import com.waitasec.app.ui.theme.SagePale
import com.waitasec.app.ui.theme.SageSoft

@Composable
fun OnboardingScreen(
    accessibilityEnabled: Boolean,
    onOpenAccessibility: () -> Unit,
    onRefreshAccessibility: () -> Unit,
    onContinue: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) onRefreshAccessibility()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scale",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Mist, Cream, SagePale.copy(alpha = 0.55f))),
            )
            .statusBarsPadding()
            .padding(horizontal = 28.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(scale)
                        .background(SageSoft.copy(alpha = 0.35f), CircleShape),
                )
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(SageMid.copy(alpha = 0.85f), CircleShape),
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "Wait a sec…",
                style = MaterialTheme.typography.displayLarge,
                color = SageDeep,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "A short breath before the scroll. Pause on apps that steal your time, then decide if they’re worth it.",
                style = MaterialTheme.typography.bodyLarge,
                color = SageMid,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                StepLine("1", "Pick the apps that tend to pull you in.")
                StepLine("2", "When you open one, a calm breath screen appears.")
                StepLine("3", "Quit to something productive — or consciously continue.")
            }

            Spacer(modifier = Modifier.weight(1f))

            if (accessibilityEnabled) {
                Text(
                    text = "Accessibility is on. You’re ready.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SageMid,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SageDeep,
                        contentColor = Cream,
                    ),
                ) {
                    Text("Continue to settings", style = MaterialTheme.typography.labelLarge)
                }
            } else {
                Text(
                    text = "Enable the Wait a sec… accessibility service so we can notice when a watched app opens. We never read your content.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SageMid,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                Button(
                    onClick = onOpenAccessibility,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SageDeep,
                        contentColor = Cream,
                    ),
                ) {
                    Text("Enable accessibility", style = MaterialTheme.typography.labelLarge)
                }
                TextButton(onClick = onContinue) {
                    Text("Skip for now", color = SageMid)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun StepLine(number: String, text: String) {
    androidx.compose.foundation.layout.Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .background(SagePale, CircleShape),
        ) {
            Text(number, style = MaterialTheme.typography.labelLarge, color = SageDeep)
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = SageDeep,
            modifier = Modifier.weight(1f),
        )
    }
}
