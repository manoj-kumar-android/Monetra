package com.monetra.presentation.screen.welcome

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monetra.ui.theme.Spacing
import kotlinx.coroutines.delay
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monetra.R
import com.monetra.presentation.screen.settings.PasswordDialog
import com.monetra.presentation.screen.settings.PasswordDialogMode

private val Brand    = Color(0xFF6C63FF)   // Indigo
private val BrandAlt = Color(0xFF00C6FF)   // Sky blue
private val Accent   = Color(0xFF34C759)   // Emerald

@Composable
fun WelcomeScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: WelcomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showPasswordDialog by remember { mutableStateOf(false) }
    var pendingUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val restoreLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let { 
                pendingUri = it
                showPasswordDialog = true
            }
        }
    )

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

    var card1 by remember { mutableStateOf(false) }
    var card2 by remember { mutableStateOf(false) }
    var card3 by remember { mutableStateOf(false) }
    var ctaVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        card1 = true
        delay(150)
        card2 = true
        delay(150)
        card3 = true
        delay(200)
        ctaVisible = true
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is WelcomeEvent.RestoreSuccess -> {
                    snackbarHostState.showSnackbar("Data restored! Welcome back.")
                    onNavigateToDashboard()
                }
                is WelcomeEvent.RestoreError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    if (showPasswordDialog) {
        PasswordDialog(
            mode = PasswordDialogMode.IMPORT,
            onDismiss = { showPasswordDialog = false },
            onConfirm = { password ->
                showPasswordDialog = false
                pendingUri?.let { uri ->
                    viewModel.onRestoreFromEncryptedUri(uri, password)
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
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
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .offset(x = 140.dp, y = (-60).dp)
                    .clip(CircleShape)
                    .background(Brand.copy(alpha = 0.07f))
            )
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

                Box(
                    modifier = Modifier.scale(logoScale),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(124.dp)
                            .clip(CircleShape)
                            .background(Brand.copy(alpha = 0.15f))
                    )
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
                            text = stringResource(R.string.rupee_symbol),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 52.sp
                            ),
                            color = Color.White
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.meet_monetra),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(
                        text = stringResource(R.string.hero_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 26.sp
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AnimatedFeatureCard(
                        visible  = card1,
                        icon     = Icons.Default.AccountBalance,
                        iconTint = Brand,
                        title    = stringResource(R.string.feature1_title),
                        subtitle = stringResource(R.string.feature1_subtitle)
                    )
                    AnimatedFeatureCard(
                        visible  = card2,
                        icon     = Icons.AutoMirrored.Filled.TrendingUp,
                        iconTint = Accent,
                        title    = stringResource(R.string.feature2_title),
                        subtitle = stringResource(R.string.feature2_subtitle)
                    )
                    AnimatedFeatureCard(
                        visible  = card3,
                        icon     = Icons.Default.AutoAwesome,
                        iconTint = Color(0xFFFF9F0A),
                        title    = stringResource(R.string.feature3_title),
                        subtitle = stringResource(R.string.feature3_subtitle)
                    )
                }

                PrivacyBadge()

                val ctaScale by animateFloatAsState(
                    targetValue    = if (ctaVisible) 1f else 0.85f,
                    animationSpec  = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label          = "ctaScale"
                )

                Column(
                    modifier = Modifier.fillMaxWidth().scale(ctaScale),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Button(
                        onClick   = onNavigateToOnboarding,
                        modifier  = Modifier.fillMaxWidth().height(56.dp),
                        shape     = RoundedCornerShape(16.dp),
                        colors    = ButtonDefaults.buttonColors(containerColor = Brand),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text  = stringResource(R.string.im_new),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick   = { restoreLauncher.launch(arrayOf("*/*")) },
                        modifier  = Modifier.fillMaxWidth().height(56.dp),
                        shape     = RoundedCornerShape(16.dp),
                        enabled   = !uiState.isRestoring,
                        border    = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Brand.copy(alpha = 0.4f)))
                    ) {
                        if (uiState.isRestoring) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Brand)
                        } else {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Brand, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Text(
                                text  = "Select Encrypted Backup",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Brand
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.lg))
            }
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
            text  = "Cross-Device Encrypted Recovery Active",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Accent,
            textAlign = TextAlign.Center
        )
    }
}
