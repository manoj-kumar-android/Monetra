package com.monetra.feature.onboarding.presentation.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monetra.core.ui.theme.Spacing
import com.monetra.feature.onboarding.R

@Composable
fun WelcomeStepContent(
    onGetStarted: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "welcomeIllustration")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "illustrationFloat"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.screenHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .size(240.dp)
                .offset(y = floatOffset.dp),
            contentAlignment = Alignment.Center
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondary

            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.15f), Color.Transparent),
                        center = center,
                        radius = size.width / 2f
                    ),
                    radius = size.width / 2f,
                    center = center
                )
                drawCircle(
                    color = primaryColor.copy(alpha = 0.25f),
                    radius = size.width / 2.8f,
                    center = center,
                    style = Stroke(width = 4.dp.toPx())
                )
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(primaryColor, secondaryColor)
                    ),
                    radius = size.width / 4f,
                    center = center
                )
            }

            Text(
                text = stringResource(R.string.currency_prefix).trim(),
                fontSize = 72.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.offset(y = (-4).dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.welcome_title),
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            Text(
                text = stringResource(R.string.welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.md)
            )
        }

        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = stringResource(R.string.get_started),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}
