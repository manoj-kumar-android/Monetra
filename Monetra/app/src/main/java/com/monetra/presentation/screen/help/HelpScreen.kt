package com.monetra.presentation.screen.help

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monetra.R
import com.monetra.presentation.components.HelpSection
import com.monetra.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    screenType: String,
    onNavigateBack: () -> Unit
) {
    val content = helpContent(screenType)
    val title = content.first
    val sections = content.second

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            sections.forEachIndexed { index, section ->
                HelpCard(index + 1, section)
            }
            
            Spacer(modifier = Modifier.height(Spacing.xxl))
        }
    }
}

@Composable
private fun HelpCard(index: Int, section: HelpSection) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = index.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(Spacing.md))
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                text = section.description,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun helpContent(screenType: String): Pair<String, List<HelpSection>> {
    return when (screenType) {
        "DASHBOARD" -> Pair(
            "Dashboard Guide",
            listOf(
                HelpSection(
                    "Total Balance & Net Worth",
                    "Your dashboard shows your total liquid balance (Cash/Savings) and your overall Net Worth, which includes all your investments adjusted for market volatility."
                ),
                HelpSection(
                    "Cash Flow tracking",
                    "We tracks your daily expenses and income to show you exactly where your money is going. The 'Safe-to-Spend' logic helps you stay within your limits after considering fixed costs and goals."
                ),
                HelpSection(
                    "Budget Progress",
                    "See how much of your monthly budget is remaining for each category. We update these values in real-time as you log new transactions."
                )
            )
        )
        "ASSISTANT" -> Pair(
            stringResource(R.string.help_assistant_guide_title),
            listOf(
                HelpSection(stringResource(R.string.help_assistant_step1_title), stringResource(R.string.help_assistant_step1_desc)),
                HelpSection(stringResource(R.string.help_assistant_step2_title), stringResource(R.string.help_assistant_step2_desc)),
                HelpSection(stringResource(R.string.help_assistant_step3_title), stringResource(R.string.help_assistant_step3_desc)),
                HelpSection(stringResource(R.string.help_assistant_step4_title), stringResource(R.string.help_assistant_step4_desc))
            )
        )
        "TRANSACTIONS" -> Pair(
            stringResource(R.string.help_transactions_guide_title),
            listOf(
                HelpSection(stringResource(R.string.help_transactions_step1_title), stringResource(R.string.help_transactions_step1_desc)),
                HelpSection(stringResource(R.string.help_transactions_step2_title), stringResource(R.string.help_transactions_step2_desc))
            )
        )
        "SIMULATOR" -> Pair(
            stringResource(R.string.help_simulator_guide_title),
            listOf(
                HelpSection(stringResource(R.string.help_simulator_step1_title), stringResource(R.string.help_simulator_step1_desc)),
                HelpSection(stringResource(R.string.help_simulator_step2_title), stringResource(R.string.help_simulator_step2_desc))
            )
        )
        "LOANS" -> Pair(
            "Loan Management",
            listOf(
                HelpSection(
                    "EMI Calculation",
                    "Your EMIs are calculated using the standard Reducing Balance method. This means interest is calculated only on the remaining principal amount each month."
                ),
                HelpSection(
                    "Interest vs Principal",
                    "In the early stages of a loan, a larger portion of your EMI goes toward interest. Over time, more of it goes toward principal repayment."
                ),
                HelpSection(
                    "Debt Status",
                    "Track your total outstanding debt across all loans. This helps you understand your liability and plan for prepayments to save on interest."
                )
            )
        )
        "INVESTMENTS" -> Pair(
            "Investment Guide",
            listOf(
                HelpSection(
                    "Understanding Your Portfolio",
                    "This screen provides a comprehensive view of your wealth. We distinguish between 'Invested Amount' (the actual money you added), 'Current Value' (your wealth today), and 'Returns' (your gains or losses)."
                ),
                HelpSection(
                    "How Wealth is Calculated",
                    "In our app, current wealth is calculated using industry-standard mathematical compounding formulas. Based on your 'Expected Return Rate', the app projects how your money grows over time."
                ),
                HelpSection(
                    "Monthly Investments (SIP/RD/PPF)",
                    "For monthly contributions, each installment is treated as an individual investment. Each month's money grows separately from its specific contribution date until today, and the 'Wealth' you see is the sum of all these compounded monthly parts."
                ),
                HelpSection(
                    "One-Time Investments (Lump Sum)",
                    "For one-time investments like Stocks, Gold, or FD, your entire initial amount grows from the start date using Compound Annual Growth Rate (CAGR) logic. This ensures your wealth accurately reflects time-based growth."
                ),
                HelpSection(
                    "Step-Up & Step-Down",
                    "If you modify your monthly contribution (Step-Up or Step-Down), the system smartly applies the new amount only from its effective date. Your past contributions remain exactly as they were, ensuring your historical data stays accurate."
                ),
                HelpSection(
                    "Liquidity: Liquid Assets (100%)",
                    "These are your immediate safety nets. We count 100% of their current value toward your emergency buffer. Example: If you have ₹50,000 in Cash or Savings, the system assumes the full ₹50,000 is available for use today."
                ),
                HelpSection(
                    "Liquidity: Semi-Liquid (Haircut)",
                    "These assets can be sold quickly but are subject to market volatility. We apply a 'Haircut' (a safety discount). Example: For Stocks with a 30% haircut, ₹1,00,000 in market value is counted as ₹70,000. This ensures your emergency plan is safe even if the market drops."
                ),
                HelpSection(
                    "Liquidity: Locked (0% Buffer)",
                    "These are long-term assets like Real Estate or PPF. While they contribute 100% to your Net Worth, we count them as 0% for your emergency buffer because you cannot convert them to cash instantly during a crisis."
                ),
                HelpSection(
                    "Offline Mode & Privacy",
                    "Monetra works completely offline for your privacy. This means wealth values are calculated using mathematical projection formulas rather than real-time market feeds. All values shown are estimates based on your provided return rates."
                )
            )
        )
        "FIXED_COSTS" -> Pair(
            "Fixed Bills & Reservations",
            listOf(
                HelpSection(
                    "What are Fixed Bills?",
                    "Fixed bills represent your planned monthly obligations like WiFi, Rent, or Subscriptions. When you add a fixed bill, it doesn't reduce your balance immediately; instead, it 'reserves' that money."
                ),
                HelpSection(
                    "The Reservation System",
                    "Your 'Available Balance' is calculated as Total Balance minus Reserved Amount. This ensures you don't accidentally spend money that you've already committed to bills."
                ),
                HelpSection(
                    "Automatic Linking",
                    "When you add a real transaction (e.g., categorized as 'Bills'), the app automatically links it to your pending fixed bills. This updates the bill status to 'Paid' or 'Partial' and releases the corresponding reserved amount back into your balance logic."
                )
            )
        )
        "REFUNDABLE" -> Pair(
            stringResource(R.string.help_refundable_guide_title),
            listOf(
                HelpSection(stringResource(R.string.help_refundable_step1_title), stringResource(R.string.help_refundable_step1_desc)),
                HelpSection(stringResource(R.string.help_refundable_step2_title), stringResource(R.string.help_refundable_step2_desc))
            )
        )
        "BUDGETS" -> Pair(
            stringResource(R.string.help_budgets_guide_title),
            listOf(
                HelpSection(stringResource(R.string.help_budgets_step1_title), stringResource(R.string.help_budgets_step1_desc)),
                HelpSection(stringResource(R.string.help_budgets_step2_title), stringResource(R.string.help_budgets_step2_desc))
            )
        )
        else -> Pair(stringResource(R.string.help_center), listOf(HelpSection(stringResource(R.string.help_general_title), stringResource(R.string.help_general_desc))))
    }
}
