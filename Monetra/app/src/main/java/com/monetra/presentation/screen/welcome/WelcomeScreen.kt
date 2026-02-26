package com.monetra.presentation.screen.welcome

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monetra.ui.theme.Spacing
import kotlinx.coroutines.delay

private val Brand    = Color(0xFF6C63FF)   // Indigo
private val BrandAlt = Color(0xFF00C6FF)   // Sky blue
private val Accent   = Color(0xFF34C759)   // Emerald

@Composable
fun WelcomeScreen(onNavigateToOnboarding: () -> Unit) {

    // ── Pulsing scale animation on logo ────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "logoPulse")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = 1.06f,
        animationSpec = infiniteRepeatable(
            animation   = tween(1800, easing = FastOutSlowInEasing),
            repeatMode  = RepeatMode.Reverse
        ),
        label = "logoScale"
    )

    // ── Staggered card visibility ───────────────────────────────────────────
    var card1 by remember { mutableStateOf(false) }
    var card2 by remember { mutableStateOf(false) }
    var card3 by remember { mutableStateOf(false) }
    var ctaVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200);  card1 = true
        delay(150);  card2 = true
        delay(150);  card3 = true
        delay(200);  ctaVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0.0f to MaterialTheme.colorScheme.background,
                    0.45f to MaterialTheme.colorScheme.background,
                    1.0f to Brand.copy(alpha = 0.08f)
                )
            )
    ) {
        // Decorative glow circle — top right
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = 140.dp, y = (-60).dp)
                .clip(CircleShape)
                .background(Brand.copy(alpha = 0.07f))
        )
        // Decorative glow circle — bottom left
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-80).dp, y = 60.dp)
                .clip(CircleShape)
                .background(BrandAlt.copy(alpha = 0.06f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.xl, vertical = Spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xxl)
        ) {

            Spacer(modifier = Modifier.height(Spacing.xxl))

            // ── App logo ─────────────────────────────────────────────────
            Box(
                modifier = Modifier.scale(logoScale),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow ring
                Box(
                    modifier = Modifier
                        .size(124.dp)
                        .clip(CircleShape)
                        .background(Brand.copy(alpha = 0.15f))
                )
                // Gradient circle
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Brand, BrandAlt)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "₹",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 52.sp
                        ),
                        color = Color.White
                    )
                }
            }

            // ── Hero text ─────────────────────────────────────────────────
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Meet Monetra",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = "Your personal finance OS — built to guide every rupee you earn.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 26.sp
                )
            }

            // ── Feature cards ─────────────────────────────────────────────
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                modifier = Modifier.fillMaxWidth()
            ) {
                AnimatedFeatureCard(
                    visible  = card1,
                    icon     = Icons.Default.AccountBalance,
                    iconTint = Brand,
                    title    = "Complete Money Clarity",
                    subtitle = "Track every expense, EMI and fixed cost — all in one beautiful place."
                )
                AnimatedFeatureCard(
                    visible  = card2,
                    icon     = Icons.AutoMirrored.Filled.TrendingUp,
                    iconTint = Accent,
                    title    = "Grow Your Wealth",
                    subtitle = "Set savings goals, monitor investments and see your net worth rise."
                )
                AnimatedFeatureCard(
                    visible  = card3,
                    icon     = Icons.Default.AutoAwesome,
                    iconTint = Color(0xFFFF9F0A),
                    title    = "AI-Powered Insights",
                    subtitle = "Smart nudges and what-if simulations guide every financial decision."
                )
            }

            // ── Privacy badge ─────────────────────────────────────────────
            PrivacyBadge()

            Spacer(modifier = Modifier.height(Spacing.sm))

            // ── CTA button ────────────────────────────────────────────────
            val ctaScale by animateFloatAsState(
                targetValue    = if (ctaVisible) 1f else 0.85f,
                animationSpec  = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label          = "ctaScale"
            )

            Button(
                onClick   = onNavigateToOnboarding,
                modifier  = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .scale(ctaScale),
                shape     = RoundedCornerShape(18.dp),
                colors    = ButtonDefaults.buttonColors(
                    containerColor = Brand
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Text(
                    text  = "Get Started",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Icon(
                    imageVector    = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier       = Modifier.size(20.dp),
                    tint           = Color.White
                )
            }

            Spacer(modifier = Modifier.height(Spacing.lg))
        }
    }
}

@Composable
private fun AnimatedFeatureCard(
    visible:  Boolean,
    icon:     ImageVector,
    iconTint: Color,
    title:    String,
    subtitle: String,
) {
    val cardScale by animateFloatAsState(
        targetValue   = if (visible) 1f else 0.88f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label         = title
    )
    val cardAlpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(350),
        label         = "${title}Alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale),
        shape  = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier            = Modifier.padding(Spacing.lg),
            verticalAlignment   = Alignment.CenterVertically
        ) {
            Box(
                modifier          = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment  = Alignment.Center
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = iconTint,
                    modifier           = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun PrivacyBadge() {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier              = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(Accent.copy(alpha = 0.08f))
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
    ) {
        Icon(
            Icons.Default.Shield,
            contentDescription = null,
            modifier           = Modifier.size(16.dp),
            tint               = Accent
        )
        Spacer(modifier = Modifier.width(Spacing.xs))
        Text(
            text  = "100% offline & private — your data never leaves your device",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Accent,
            textAlign = TextAlign.Center
        )
    }
}
