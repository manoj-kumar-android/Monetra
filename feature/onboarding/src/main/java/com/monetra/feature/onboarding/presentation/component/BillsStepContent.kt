package com.monetra.feature.onboarding.presentation.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monetra.core.ui.components.CategoryItem
import com.monetra.core.ui.components.CategoryPickerField
import com.monetra.core.ui.components.DueDayPickerField
import com.monetra.core.ui.theme.Spacing
import com.monetra.core.ui.util.dashedBorder
import com.monetra.domain.model.MonthlyExpense
import com.monetra.feature.onboarding.R

@Composable
fun BillsStepContent(
    billName: String,
    billAmount: String,
    billCategory: String,
    billDueDay: String,
    billsList: List<MonthlyExpense>,
    onNameChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onDueDayChange: (String) -> Unit,
    onAddBill: () -> Unit,
    onDeleteBill: (MonthlyExpense) -> Unit,
    onNext: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    var nameTextFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = billName,
                selection = TextRange(billName.length)
            )
        )
    }
    LaunchedEffect(billName) {
        if (nameTextFieldValue.text != billName) {
            nameTextFieldValue = nameTextFieldValue.copy(
                text = billName,
                selection = TextRange(billName.length)
            )
        }
    }

    var amountTextFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = billAmount,
                selection = TextRange(billAmount.length)
            )
        )
    }
    LaunchedEffect(billAmount) {
        if (amountTextFieldValue.text != billAmount) {
            amountTextFieldValue = amountTextFieldValue.copy(
                text = billAmount,
                selection = TextRange(billAmount.length)
            )
        }
    }

    val categoriesList = listOf(
        CategoryItem(
            "General",
            Icons.Rounded.AccountBalanceWallet,
            stringResource(R.string.cat_general),
            Color(0xFF6C63FF)
        ),
        CategoryItem(
            "Food",
            Icons.Rounded.Restaurant,
            stringResource(R.string.cat_food),
            Color(0xFFFF6B35)
        ),
        CategoryItem(
            "Transport",
            Icons.Rounded.DirectionsCar,
            stringResource(R.string.cat_transport),
            Color(0xFF2196F3)
        ),
        CategoryItem(
            "Shopping",
            Icons.Rounded.ShoppingBag,
            stringResource(R.string.cat_shopping),
            Color(0xFFE91E8C)
        ),
        CategoryItem(
            "Groceries",
            Icons.Rounded.ShoppingCart,
            stringResource(R.string.cat_groceries),
            Color(0xFF4CAF50)
        ),
        CategoryItem(
            "Bills",
            Icons.AutoMirrored.Rounded.ReceiptLong,
            stringResource(R.string.cat_bills),
            Color(0xFFF59E0B)
        ),
        CategoryItem(
            "Rent",
            Icons.Rounded.Home,
            stringResource(R.string.cat_rent),
            Color(0xFF14B8A6)
        ),
        CategoryItem(
            "Subscription",
            Icons.Rounded.Autorenew,
            stringResource(R.string.cat_subscription),
            Color(0xFF8B5CF6)
        ),
        CategoryItem(
            "Fun",
            Icons.Rounded.SportsEsports,
            stringResource(R.string.cat_fun),
            Color(0xFFEF4444)
        ),
        CategoryItem(
            "Health",
            Icons.Rounded.HealthAndSafety,
            stringResource(R.string.cat_health),
            Color(0xFF06B6D4)
        ),
        CategoryItem(
            "Mobile Recharge",
            Icons.Rounded.PhoneAndroid,
            stringResource(R.string.cat_mobile_recharge),
            Color(0xFF10B981)
        )
    )

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    val configuration = LocalConfiguration.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    val screenWidth =
        with(density) { androidx.compose.ui.platform.LocalWindowInfo.current.containerSize.width.toDp() }
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isWide = isLandscape || screenWidth > 550.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Spacing.screenHorizontal),
        contentAlignment = Alignment.Center
    ) {
        val contentMaxWidth = if (isWide) 620.dp else 500.dp
        val contentModifier = if (screenWidth > 600.dp) {
            Modifier.widthIn(max = contentMaxWidth)
        } else {
            Modifier.fillMaxSize()
        }

        Box(modifier = contentModifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .padding(bottom = 80.dp),
                contentPadding = PaddingValues(vertical = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = stringResource(R.string.bills_title),
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.bills_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    // Stitched dashed border on the Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .dashedBorder(
                                width = 1.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(24.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(Spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (isWide) {
                                // Side-by-side row on wide screen
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(modifier = Modifier.weight(1.3f)) {
                                        OutlinedTextField(
                                            value = nameTextFieldValue,
                                            onValueChange = { newValue ->
                                                nameTextFieldValue = newValue
                                                onNameChange(newValue.text)
                                            },
                                            placeholder = { Text(stringResource(R.string.bill_name_placeholder)) },
                                            label = { Text(stringResource(R.string.bill_name_label)) },
                                            singleLine = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .focusRequester(focusRequester),
                                            shape = RoundedCornerShape(12.dp),
                                            keyboardOptions = KeyboardOptions(
                                                capitalization = KeyboardCapitalization.Words,
                                                imeAction = ImeAction.Next
                                            ),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        )
                                    }

                                    Box(modifier = Modifier.weight(0.7f)) {
                                        OutlinedTextField(
                                            value = amountTextFieldValue,
                                            onValueChange = { newValue ->
                                                amountTextFieldValue = newValue
                                                onAmountChange(newValue.text)
                                            },
                                            placeholder = { Text(stringResource(R.string.bill_amount_placeholder)) },
                                            label = { Text(stringResource(R.string.bill_amount_label)) },
                                            prefix = { Text(stringResource(R.string.currency_prefix)) },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Number,
                                                imeAction = ImeAction.Next
                                            ),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        CategoryPickerField(
                                            selectedCategoryId = billCategory,
                                            categories = categoriesList,
                                            onCategorySelected = onCategoryChange,
                                            label = stringResource(R.string.bill_category_label)
                                        )
                                    }

                                    Box(modifier = Modifier.weight(1f)) {
                                        DueDayPickerField(
                                            selectedDay = billDueDay.toIntOrNull() ?: 1,
                                            onDaySelected = { onDueDayChange(it.toString()) },
                                            label = stringResource(R.string.bill_due_day_label)
                                        )
                                    }
                                }
                            } else {
                                // Stacked elements on normal screens
                                OutlinedTextField(
                                    value = nameTextFieldValue,
                                    onValueChange = { newValue ->
                                        nameTextFieldValue = newValue
                                        onNameChange(newValue.text)
                                    },
                                    placeholder = { Text(stringResource(R.string.bill_name_placeholder)) },
                                    label = { Text(stringResource(R.string.bill_name_label)) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester),
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Words,
                                        imeAction = ImeAction.Next
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )

                                CategoryPickerField(
                                    selectedCategoryId = billCategory,
                                    categories = categoriesList,
                                    onCategorySelected = onCategoryChange,
                                    label = stringResource(R.string.bill_category_label)
                                )

                                OutlinedTextField(
                                    value = amountTextFieldValue,
                                    onValueChange = { newValue ->
                                        amountTextFieldValue = newValue
                                        onAmountChange(newValue.text)
                                    },
                                    placeholder = { Text(stringResource(R.string.bill_amount_placeholder)) },
                                    label = { Text(stringResource(R.string.bill_amount_label)) },
                                    prefix = { Text(stringResource(R.string.currency_prefix)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Next
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )

                                DueDayPickerField(
                                    selectedDay = billDueDay.toIntOrNull() ?: 1,
                                    onDaySelected = { onDueDayChange(it.toString()) },
                                    label = stringResource(R.string.bill_due_day_label)
                                )
                            }

                            Button(
                                onClick = {
                                    keyboardController?.hide()
                                    onAddBill()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.add_bill),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }

                if (billsList.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.added_bills_header),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(
                        items = billsList,
                        key = { it.remoteId }
                    ) { bill ->
                        BillListItem(
                            bill = bill,
                            categories = categoriesList,
                            onDeleteClick = { onDeleteBill(bill) }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    keyboardController?.hide()
                    onNext()
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(vertical = Spacing.md)
                    .height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.finish_onboarding),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}

@Composable
private fun BillListItem(
    bill: MonthlyExpense,
    categories: List<CategoryItem>,
    onDeleteClick: () -> Unit
) {
    val categoryItem = categories.find { it.id.equals(bill.category, ignoreCase = true) }
    val categoryColor = categoryItem?.color ?: MaterialTheme.colorScheme.primary
    val categoryIcon = categoryItem?.icon ?: Icons.Default.Receipt

    // Stitched card item border
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .dashedBorder(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(categoryColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.md))
                Column {
                    Text(
                        text = bill.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${bill.category} • Due on ${bill.dueDay}th",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "₹${bill.amount.toInt()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                IconButton(
                    onClick = onDeleteClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = stringResource(R.string.delete),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
