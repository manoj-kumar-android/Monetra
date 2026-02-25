package com.monetra.presentation.screen.help

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monetra.presentation.components.HelpSection
import com.monetra.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    screenType: String,
    onNavigateBack: () -> Unit
) {
    val (title, sections) = getHelpContent(screenType)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            sections.forEach { section ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            text = section.description,
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun getHelpContent(screenType: String): Pair<String, List<HelpSection>> {
    return when (screenType) {
        "DASHBOARD" -> Pair(
            "Dashboard Guide (Kaise use karein?)",
            listOf(
                HelpSection(
                    title = "Safe to Spend Today",
                    description = "This hero card shows exactly how much money you can spend RIGHT NOW. The progress bar filling up shows how much of today's budget you've already used.\n\n(Aaj kitne paise kharch kar sakte hain ye card bata raha hai. Progress bar bharega toh matlab aaj ka limit khatam ho raha hai.)"
                ),
                HelpSection(
                    title = "Smart Insights",
                    description = "Check the badge next to your bills to see your 'Financial Health'. GREEN means you are spending at a safe speed, RED means slow down!\n\n(Badge pe dhyan dein - Green matlab budget sahi hai, Red matlab kharch kam karna padega.)"
                ),
                HelpSection(
                    title = "Monthly Health & Logic",
                    description = "See your total available cash and follow the 'Budget Logic' waterfall to understand how your salary is split between Savings, EMIs, and daily allowance.\n\n(Saile ka hisaab 'Budget Logic' mein dekhein—Salary se Savings aur EMI nikalne ke baad jo bachega, wahi aapka asli kharcha hai.)"
                ),
                HelpSection(
                    title = "Recent Activity",
                    description = "A quick look at your last few spends. Tap 'See All' to dive into your full transaction history.\n\n(Aapne haal hi mein jo kharcha kiya hai uski list. Poora hisaab dekhne ke liye 'See All' dabayein.)"
                ),
                HelpSection(
                    title = "Safe to Spend Formula",
                    description = "How we calculate your daily limit:\n\n" +
                            "1. Monthly Allowance = [Salary - Savings Goal - EMIs - Fixed Bills]\n" +
                            "2. Daily Budget = [Monthly Allowance ÷ Days in Month]\n" +
                            "3. Safe to Spend Today = [Daily Budget - Amount Spent Today]\n\n" +
                            "(Ye formula batata hai ki kitne paise kharch karna safe hai.)"
                )
            )
        )
        "TRANSACTIONS" -> Pair(
            "Transactions Guide",
            listOf(
                HelpSection(
                    title = "Available to Spend",
                    description = "The updated summary card shows your 'real' balance after accounting for income and expenses for the current month.\n\n(Is mahine kitne paise bach gaye hain, ye card pe bada dikhega.)"
                ),
                HelpSection(
                    title = "Navigation & Editing",
                    description = "You don't need buttons to change months—just swipe left or right! Tap any item to change details or fix mistakes.\n\n(Mahina badalne ke liye swipe karein. Kuch galat ho gaya toh click karke theek karein.)"
                )
            )
        )
        "ASSISTANT" -> Pair(
            "Assistant (Money Coach) Guide",
            listOf(
                HelpSection(
                    title = "Calculation Formulas",
                    description = "How your coach thinks:\n\n" +
                            "• Savings Gap = Target Savings - (Monthly Income - Actual Spend - EMIs)\n" +
                            "• EMI Ratio = (Total EMIs ÷ Income) × 100 (Risk if > 40%)\n" +
                            "• Survival Runway = Total Cash ÷ (Fixed Bills + EMIs + 30% Essential Spend)\n" +
                            "• 7-Day Plan = ((Disposable Cash) ÷ Remaining Days) × 7\n\n" +
                            "(Monetra ye saare formulas use karta hai aapki financial health janne ke liye.)"
                ),
                HelpSection(
                    title = "Am I Safe This Month?",
                    description = "Checks your 'Savings Gap'. If the gap is more than ₹0, you are behind your goal. Formula: [Goal - Real Savings]\n\n(Aap apne target bachat se kitne peeche hain, yahan pata chalta hai.)"
                ),
                HelpSection(
                    title = "Can I Afford This?",
                    description = "Instantly simulates adding the new expense: [Current Spend + New Amount]. If this exceeds [Income - EMIs], your status turns RED.\n\n(Naya kharcha karne se pehle ye check karta hai ki aapki salary usse cover kar payegi ya nahi.)"
                ),
                HelpSection(
                    title = "Emergency Safety",
                    description = "Calculates 'Survival Runway'. Formula: [Total Assets ÷ Monthly Commitments]. Ideally should be > 6 months.\n\n(Bina naukri ke kitne mahine ghar chal sakta hai—Formula: [Total Paisa ÷ Har mahine ka fix kharch].)"
                )
            )
        )
        "SIMULATOR" -> Pair(
            "What-If Simulator Guide",
            listOf(
                HelpSection(
                    title = "12-Month Projection Formula",
                    description = "Calculates your net wealth 1 year from now:\n" +
                            "Formula: [Net Worth] + ([New Income - New EMI - Monthly Expense] × 12 months)\n\n" +
                            "(Ye aapki agle 1 saal ki bachat ka andaza lagata hai.)"
                ),
                HelpSection(
                    title = "Safety Logic",
                    description = "If the 'Projected Wealth' in 12 months becomes negative or drops significantly compared to your baseline, the status flashes RED.\n\n(Agar 1 saal baad aapke paas paise khatam hone ka darr hai, toh indicator Red ho jayega.)"
                )
            )
        )
        "SNAPSHOT" -> Pair(
            "Snapshot Guide (Financial Report)",
            listOf(
                HelpSection(
                    title = "Health Status Header",
                    description = "A quick status badge (Excellent/Stable/Action Required) telling you if your finances are balanced for the current month.\n\n(Is mahine aapki financial halat kaisi hai—sahi hai ya sudhaar ki zaroorat hai, ye badge batayega.)"
                ),
                HelpSection(
                    title = "Monthly Overview",
                    description = "A high-level summary of your Salary vs Expenses, EMIs, and Investments.\n\n(Aapki total salary aur total kharch ka ek saath report.)"
                ),
                HelpSection(
                    title = "Savings Analysis",
                    description = "Track your 'Savings Gap'. Formula: [Target Savings Goal - Current Month Savings]. It shows how much more you need to save to meet your target.\n\n(Goal tak pahunchne ke liye kitne aur paise bachane hain: [Target - Asli Bachat].)"
                ),
                HelpSection(
                    title = "Health Status Logic",
                    description = "Your status is 'Excellent' if your Savings Gap is ₹0 and your EMI ratio is below 30%. It becomes 'Action Required' if you are overspending your daily limit consistently.\n\n(Status 'Excellent' tab hota hai jab bachat target poora ho.)"
                ),
                HelpSection(
                    title = "Suggested Improvements",
                    description = "Customized AI recommendations specifically for you to help close the savings gap.\n\n(Monetra ki taraf se tips jo aapko zyada bachane mein madad karengi.)"
                )
            )
        )
        "LOANS" -> Pair(
            "Debt Management Guide",
            listOf(
                HelpSection(
                    title = "Tracking EMIs",
                    description = "Add all your active loans here. The total EMI amount is automatically subtracted from your salary in the 'Budget Logic' on the dashboard.\n\n(Apne saare loans yahan add karein. Inki total EMI aapki salary se pehle hi minus ho jayegi taaki budget sahi rahe.)"
                )
            )
        )
        "INVESTMENTS" -> Pair(
            "Investments Guide",
            listOf(
                HelpSection(
                    title = "Mutual Funds & SIPs",
                    description = "Track your monthly investments. This helps Monetra calculate your total wealth and emergency runway accurately.\n\n(Har mahine ki bachat/investments yahan likhein taaki aapki total wealth ka sahi pata chale.)"
                )
            )
        )
        "BUDGETS" -> Pair(
            "Budget Guard Guide",
            listOf(
                HelpSection(
                    title = "Category Limits",
                    description = "Set a maximum monthly limit for categories like 'Food' or 'Entertainment'. Crossing these will trigger alerts on your dashboard.\n\n(Specific kharchon ki limit set karein—jaise khana ya ghumna. Limit paar hone par alert milega.)"
                )
            )
        )
        "FIXED_COSTS" -> Pair(
            "Fixed Bills Guide",
            listOf(
                HelpSection(
                    title = "Recurring Expenses",
                    description = "Add bills that remain the same every month (Rent, WiFi, Subscriptions). These are treated as 'Committed Spend' for your safety calculations.\n\n(Wo kharche jo har mahine pakke hain, jaise kiraaye ya internet bill. Inhe system 'Safe to Spend' se pehle hi reserve kar leta hai.)"
                )
            )
        )
        else -> Pair("Help Center", listOf(HelpSection("General Help", "Explore the dashboard to manage your finances better.")))
    }
}
