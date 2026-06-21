package com.monetra.feature.onboarding.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Receipt
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monetra.core.ui.theme.Spacing
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

    var categoryTextFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = billCategory,
                selection = TextRange(billCategory.length)
            )
        )
    }
    LaunchedEffect(billCategory) {
        if (categoryTextFieldValue.text != billCategory) {
            categoryTextFieldValue = categoryTextFieldValue.copy(
                text = billCategory,
                selection = TextRange(billCategory.length)
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

    var dueDayTextFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = billDueDay,
                selection = TextRange(billDueDay.length)
            )
        )
    }
    LaunchedEffect(billDueDay) {
        if (dueDayTextFieldValue.text != billDueDay) {
            dueDayTextFieldValue = dueDayTextFieldValue.copy(
                text = billDueDay,
                selection = TextRange(billDueDay.length)
            )
        }
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = Spacing.screenHorizontal)
    ) {
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

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        OutlinedTextField(
                            value = categoryTextFieldValue,
                            onValueChange = { newValue ->
                                categoryTextFieldValue = newValue
                                onCategoryChange(newValue.text)
                            },
                            placeholder = { Text(stringResource(R.string.bill_category_placeholder)) },
                            label = { Text(stringResource(R.string.bill_category_label)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
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

                        OutlinedTextField(
                            value = dueDayTextFieldValue,
                            onValueChange = { newValue ->
                                dueDayTextFieldValue = newValue
                                onDueDayChange(newValue.text)
                            },
                            placeholder = { Text(stringResource(R.string.bill_due_day_placeholder)) },
                            label = { Text(stringResource(R.string.bill_due_day_label)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    onAddBill()
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        Button(
                            onClick = {
                                keyboardController?.hide()
                                onAddBill()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
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

@Composable
private fun BillListItem(
    bill: MonthlyExpense,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.md))
                Column {
                    Text(
                        text = bill.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${bill.category} • Due on ${bill.dueDay}th",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "₹${bill.amount.toInt()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(modifier = Modifier.width(Spacing.md))
                IconButton(
                    onClick = onDeleteClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = stringResource(R.string.delete)
                    )
                }
            }
        }
    }
}
