package com.monetra.feature.onboarding.presentation.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.monetra.core.ui.theme.Spacing
import com.monetra.feature.onboarding.R
import com.monetra.feature.onboarding.presentation.OnboardingStep

@Composable
fun OnboardingHeader(
    currentStep: OnboardingStep,
    onBackClick: () -> Unit
) {
    // If Welcome screen, render empty header with identical height to prevent layout shifts
    if (currentStep == OnboardingStep.WELCOME) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )
        return
    }

    val totalSteps = 3
    val activeIndex = when (currentStep) {
        OnboardingStep.INCOME -> 0
        OnboardingStep.SAVINGS -> 1
        OnboardingStep.BILLS -> 2
        OnboardingStep.WELCOME -> -1
    }

    val density = androidx.compose.ui.platform.LocalDensity.current
    val screenWidth =
        with(density) { androidx.compose.ui.platform.LocalWindowInfo.current.containerSize.width.toDp() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        val headerMaxWidth = if (currentStep == OnboardingStep.BILLS) 600.dp else 550.dp
        val contentModifier = if (screenWidth > 600.dp) {
            Modifier.widthIn(max = headerMaxWidth)
        } else {
            Modifier.fillMaxWidth()
        }

        Row(
            modifier = contentModifier
                .fillMaxHeight()
                .padding(horizontal = Spacing.screenHorizontal),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.width(Spacing.lg))

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 0 until totalSteps) {
                    val isActive = i <= activeIndex
                    val color =
                        if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    val animateWidth by animateFloatAsState(
                        targetValue = if (i == activeIndex) 2.5f else 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "barWidthAnimation"
                    )

                    Box(
                        modifier = Modifier
                            .weight(animateWidth)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                    )
                }
            }
        }
    }
}
