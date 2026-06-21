package com.monetra.feature.onboarding.presentation.component

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monetra.core.ui.components.CategoryItem
import com.monetra.core.ui.components.CategoryPickerField
import com.monetra.core.ui.components.CommonBottomSheet
import com.monetra.core.ui.components.ConfirmationDialog
import com.monetra.core.ui.components.DueDayPickerField
import com.monetra.core.ui.theme.Spacing
import com.monetra.core.ui.util.AmountFormatter
import com.monetra.core.ui.util.IndianCurrencyVisualTransformation
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

    val density = androidx.compose.ui.platform.LocalDensity.current
    val screenWidth =
        with(density) { androidx.compose.ui.platform.LocalWindowInfo.current.containerSize.width.toDp() }

    var showAddBillSheet by remember { mutableStateOf(false) }
    var editingBill by remember { mutableStateOf<MonthlyExpense?>(null) }
    var billToDelete by remember { mutableStateOf<MonthlyExpense?>(null) }

    LaunchedEffect(showAddBillSheet) {
        if (showAddBillSheet) {
            kotlinx.coroutines.delay(200)
            focusRequester.requestFocus()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Spacing.screenHorizontal),
        contentAlignment = Alignment.Center
    ) {
        val contentMaxWidth = if (screenWidth > 600.dp) 600.dp else 500.dp
        val contentModifier = if (screenWidth > 600.dp) {
            Modifier.widthIn(max = contentMaxWidth)
        } else {
            Modifier.fillMaxSize()
        }

        Box(modifier = contentModifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
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

                if (billsList.isEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .dashedBorder(
                                    width = 1.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .padding(Spacing.xxl),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No recurring bills added yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Add monthly recurring bills like rent, wifi, electricity or recharge bills.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            text = stringResource(R.string.added_bills_header),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
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
                            onEditClick = {
                                onNameChange(bill.name)
                                onAmountChange(bill.amount.toInt().toString())
                                onCategoryChange(bill.category)
                                onDueDayChange(bill.dueDay.toString())
                                editingBill = bill
                                showAddBillSheet = true
                            },
                            onDeleteClick = {
                                billToDelete = bill
                            }
                        )
                    }
                }

                // Add Bill Text Button
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Button(
                            onClick = {
                                editingBill = null
                                onNameChange("")
                                onAmountChange("")
                                onCategoryChange("Bills")
                                onDueDayChange("1")
                                showAddBillSheet = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.add_bill),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // Bottom Next Button
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
                    text = stringResource(R.string.next),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }

    // Add Bill Sheet popup
    CommonBottomSheet(
        isOpen = showAddBillSheet,
        onDismissRequest = {
            showAddBillSheet = false
            if (editingBill != null) {
                onNameChange("")
                onAmountChange("")
                onCategoryChange("Bills")
                onDueDayChange("1")
                editingBill = null
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = if (editingBill != null) "Edit Bill" else "Add Bill",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
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
                        val digitsOnly = newValue.text.filter { it.isDigit() }
                        amountTextFieldValue = newValue.copy(text = digitsOnly)
                        onAmountChange(digitsOnly)
                    },
                    placeholder = { Text(stringResource(R.string.bill_amount_placeholder)) },
                    label = { Text(stringResource(R.string.bill_amount_label)) },
                    prefix = { Text(stringResource(R.string.currency_prefix)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val isNameValid = billName.trim().isNotEmpty()
                            val isAmountValid = billAmount.toDoubleOrNull()?.let { it > 0 } ?: false
                            if (isNameValid && isAmountValid) {
                                if (editingBill != null) {
                                    onDeleteBill(editingBill!!)
                                }
                                onAddBill()
                                showAddBillSheet = false
                                editingBill = null
                            } else {
                                onAddBill()
                            }
                        }
                    ),
                    visualTransformation = IndianCurrencyVisualTransformation(),
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

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val isNameValid = billName.trim().isNotEmpty()
                        val isAmountValid = billAmount.toDoubleOrNull()?.let { it > 0 } ?: false
                        if (isNameValid && isAmountValid) {
                            if (editingBill != null) {
                                onDeleteBill(editingBill!!)
                            }
                            onAddBill()
                            showAddBillSheet = false
                            editingBill = null
                        } else {
                            onAddBill()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(
                        imageVector = if (editingBill != null) Icons.Default.Edit else Icons.Default.Add,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (editingBill != null) "Save Changes" else stringResource(R.string.add_bill),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    // Deletion confirmation dialog
    ConfirmationDialog(
        isOpen = billToDelete != null,
        title = "Delete Bill",
        message = "Are you sure you want to delete '${billToDelete?.name}' of ${
            billToDelete?.amount?.let {
                AmountFormatter.formatWithSymbol(
                    it
                )
            }
        }?",
        confirmText = "Delete",
        cancelText = "Cancel",
        onConfirm = {
            billToDelete?.let { onDeleteBill(it) }
        },
        onDismiss = {
            billToDelete = null
        }
    )
}

@Composable
private fun BillListItem(
    bill: MonthlyExpense,
    categories: List<CategoryItem>,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val categoryItem = categories.find { it.id.equals(bill.category, ignoreCase = true) }
    val categoryColor = categoryItem?.color ?: MaterialTheme.colorScheme.primary
    val categoryIcon = categoryItem?.icon ?: Icons.Default.Receipt

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
                    text = AmountFormatter.formatWithSymbol(bill.amount),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(modifier = Modifier.width(Spacing.md))
                IconButton(
                    onClick = onEditClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.xs))
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
