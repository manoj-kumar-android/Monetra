# Monetra - Technical Feature Specifications

Monetra is a comprehensive personal finance ecosystem that combines reactive data tracking with proactive financial intelligence. This document details the core engines and features that power the application.

---

## 🧠 Financial Intelligence Engines

### 1. Safe-To-Spend (STS) Algorithm
The STS engine dynamically calculates a user's daily spending capacity to ensure they meet their savings targets and debt obligations.

*   **Logic**: `Daily Allowance = (Income - Savings Goal - Monthly EMIs - Reserved Fixed Bills - Month Spent so Far) / Remaining Days`
*   **Real-time Tracking**: The algorithm adjusts immediately as transactions are logged, redistributing any overspending over the remaining days of the month.
*   **Priority Clipping**: Mandatory outflows (EMIs and Fixed Bills) are "reserved" and never factored into the spendable allowance.

### 2. Financial Health Scorecard
A comparative metric (0-100) that evaluates financial discipline across four weighted pillars:
*   **Budget Discipline (30 pts)**: Adherence to user-defined category limits.
*   **Savings Rate (30 pts)**: Percentage of income saved after all outflows (Target: >20% for full points).
*   **Debt Burden (20 pts)**: EMI-to-Income ratio (Target: <30% for full points; penalized above 45%).
*   **Spending Stability (20 pts)**: Burn rate trend compared to the previous month's velocity.

### 3. Recurring Expense Detection
A background heuristic engine that identifies patterns in spending to predict future liabilities.
*   **Pattern Matching**: Groups transactions by normalized title and exact amount.
*   **Interval Testing**: Analyzes temporal gaps between occurrences, flagging sequences of 27–33 days as monthly subscriptions or bills.
*   **Confidence Scoring**: Marks detected items as "High Stability" if the pattern repeats over multiple billing cycles.

---

## 📊 Portfolio & Wealth Management

### 1. Net Worth Hero Card
Aggregates data across the entire system to provide a single-view financial position:
*   **Assets**: Liquid savings + current valuation of all investments (Stocks, SIPs, FDs).
*   **Liabilities**: Total remaining principal across all active loans.
*   **Formula**: `Net Worth = (Bank Balance + Investment Value) - (Total Loan Debt)`.

### 2. Wealth Projection & SIP Analysis
Simulates future growth based on current monthly investment velocity.
*   **Algorithm**: Uses future value of annuity formula: `FV = P * [((1 + r)^n - 1) / r] * (1 + r)`
*   **Variables**:
    *   `P`: Monthly recurring investment.
    *   `r`: Monthly expected return rate (derived from user-set annual projection rate).
    *   `n`: Total months based on projection period (default: user preference).
*   **Management & Updates**: Supports modification of any asset parameter with immediate persistence and recalculated wealth projections.

### 3. "Free Money" Insight
Calculates the actual disposable surplus after all monthly obligations.
*   **Calculation**: `Free Money = Income - Fixed Bills - Total EMI - Monthly Investment Commitments`.
*   **Utilization**: If `Free Money < 0`, the app flags "Overspending" and suggests budget adjustments.

---

## 🏠 Scenario Modelling (What-If Simulator)

The simulator allows users to project the stability of their financial health under hypothetical changes.

*   **Adjustable Parameters**: Salary Hike/Cut, New EMI additions (e.g., car/home loan planning), SIP modifications, and Savings Target adjustments.
*   **Impact Metrics**: 
    *   **EMI Ratio Analysis**: Predicts if a new loan will push the user into the "Debt Trap" zone (>40%).
    *   **Liquidity Change**: Shows the new "Free Money" position.
    *   **Savings Gap Tool**: Measures the distance between projected actual savings and defined targets.
*   **Visualization**: A dual-path line graph comparing current pace vs. simulated trajectory over 12 months.

---

## 🛠️ Automation & Background Workers

### 1. Refundable Reminder System
A specialized module for managing "Lent" or "Borrowed" money with automated follow-ups.
*   **Scheduled Reminders**: Users can set specific dates and times for alerts.
*   **SMS Automation**: Optionally sends pre-composed, context-aware SMS messages to lenders or borrowers.
*   **WorkManager Integration**: Uses `RefundableReminderWorker` to trigger high-priority notifications and SMS events even when the app is inactive.
*   **Reliability**: Includes logic for valid phone number normalization (e.g., adding country codes like +91) and saves sent messages to the system SMS database.

### 2. Monthly Bill Preparation
Automatically initializes tracking instances for fixed monthly bills at the start of each month, ensuring "Reserved Money" is accounted for before the user begins daily spending.
*   **Bill Modification**: Direct editing support for recurring bill amounts and due dates, ensuring the financial engine adjusts the "Safe to Spend" allowance accurately.

### 3. EMI Automation
Monitors loan tenures and adjusts the Global Debt position as monthly installments are processed.
*   **Active Revision**: Users can edit loan principals, rates, or start dates to reflect reality, triggering automated recalculation of EMIs and remaining tenure.

---
*Monetra leverages a clean MVVM architecture with Domain-Driven Design (DDD) to ensure financial calculations remain precise and modular.*
