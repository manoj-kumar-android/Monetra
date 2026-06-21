package com.monetra.feature.onboarding.presentation.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monetra.core.ui.components.CustomNumericKeypad
import com.monetra.core.ui.theme.Spacing
import com.monetra.core.ui.util.IndianCurrencyVisualTransformation
import com.monetra.core.ui.util.UiText
import com.monetra.core.ui.util.dashedBorder
import com.monetra.feature.onboarding.R

@Composable
fun IncomeStepContent(
    incomeValue: String,
    errorMsg: UiText?,
    onIncomeChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    val screenWidth =
        with(density) { androidx.compose.ui.platform.LocalWindowInfo.current.containerSize.width.toDp() }
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Theme-aware accessible colors for the Tip Card
    val isDark = isSystemInDarkTheme()
    val tipBgColor = if (isDark) Color(0x1A30D158) else Color(0x1234C759)
    val tipBorderColor = if (isDark) Color(0x4030D158) else Color(0x3534C759)
    val tipIconColor = if (isDark) Color(0xFF30D158) else Color(0xFF22C55E)
    val tipTextColor = if (isDark) Color(0xFF86EFAC) else Color(0xFF1E7036)

    // Keypad input handlers
    val handleDigitClick: (Char) -> Unit = { char ->
        if (incomeValue.length < 9) { // length safety limit
            val newVal =
                if (incomeValue == "0" || incomeValue == "") char.toString() else incomeValue + char
            onIncomeChange(newVal)
        }
    }

    val handleBackspaceClick: () -> Unit = {
        if (incomeValue.isNotEmpty()) {
            val newVal = incomeValue.dropLast(1)
            onIncomeChange(newVal)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        val contentModifier = if (screenWidth > 600.dp && !isLandscape) {
            Modifier.widthIn(max = 550.dp)
        } else {
            Modifier.fillMaxSize()
        }

        Box(
            modifier = contentModifier
                .fillMaxSize()
                .padding(horizontal = Spacing.screenHorizontal)
        ) {
            if (isLandscape) {
                // Landscape layout: Left side details, Right side keypad
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column (Scrollable): Title, Subtitle, Input, Next Button
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.income_title),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = stringResource(R.string.income_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = TextFieldValue(text = incomeValue),
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.income_hint)) },
                            prefix = {
                                Text(
                                    text = stringResource(R.string.currency_prefix),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            textStyle = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            singleLine = true,
                            visualTransformation = IndianCurrencyVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.3f
                                ),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.3f
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )

                        if (errorMsg != null) {
                            Text(
                                text = errorMsg.asString(),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onNext,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.next),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Right Column: Keypad
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CustomNumericKeypad(
                            onDigitClick = handleDigitClick,
                            onBackspaceClick = handleBackspaceClick,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                // Portrait Layout: Top details (scrollable), Bottom controls (fixed)
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top scrollable area
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 16.dp),
                        verticalArrangement = Arrangement.Top
                    ) {
                        Spacer(modifier = Modifier.height(Spacing.xl))

                        Text(
                            text = stringResource(R.string.income_title),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 30.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(Spacing.xs))

                        Text(
                            text = stringResource(R.string.income_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(Spacing.xxl))

                        OutlinedTextField(
                            value = TextFieldValue(text = incomeValue),
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.income_hint)) },
                            prefix = {
                                Text(
                                    text = stringResource(R.string.currency_prefix),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            textStyle = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp
                            ),
                            singleLine = true,
                            visualTransformation = IndianCurrencyVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.3f
                                ),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.3f
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )

                        if (errorMsg != null) {
                            Text(
                                text = errorMsg.asString(),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.xxl))

                        // Stitched Tip Card
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = tipBgColor
                            ),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .dashedBorder(
                                    width = 1.5.dp,
                                    color = tipBorderColor,
                                    shape = RoundedCornerShape(18.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(Spacing.lg),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = "Tip",
                                    tint = tipIconColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(Spacing.md))
                                Text(
                                    text = stringResource(R.string.settings_change_tip),
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                                    color = tipTextColor
                                )
                            }
                        }
                    }

                    // Bottom controls (numpad and Next button anchored together at bottom)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CustomNumericKeypad(
                            onDigitClick = handleDigitClick,
                            onBackspaceClick = handleBackspaceClick,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = onNext,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.next),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
