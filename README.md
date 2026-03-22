# Spend Sense

**Spend Sense** is a sophisticated, privacy-focused financial management application built with
modern Android development practices. It empowers users to take full control of their personal
finances through comprehensive tracking, logical insights, and secure data management.

## App Tour & Graphic Previews

Here is a detailed, screen-by-screen breakdown of **Spend Sense**.

### 1. Dashboard (The Command Center)

![Dashboard](graphics/welcome_screen.png)

Provides an immediate overview of your financial health.

- **Safe to Spend Today**: Dynamically calculated based on your monthly income, fixed bills, and
  savings goals.
- **Liquid Bar**: Visually indicates how close you are to your daily spending limit.
- **Monthly Insights**: Displays your remaining free cash after all commitments.

### 2. Transactions & Analytics

![Transactions](graphics/google_login_sheet.png)

Log every spend with ease and precision.

- **Fast Entry**: Categorize income and expenses securely in seconds.
- **Swipe Actions**: Fluidly swipe to delete or modify records.
- **Trends & Waterfall**: Analyze exactly where your money leaked this month.

### 3. Financial Assistant (Impact Checker)

![Assistant](graphics/Screenshot_20260322_125539.png)

Your personal, on-device money coach.

- **Impact Checker**: Enter an item's price, and the app mathematically simulates how it damages
  your savings.
- **Financial Runway**: Calculates how many months you can survive without income based on your
  current liquid wealth.

### 4. Loans, Debt & Fixed Costs

![Loans](graphics/Screenshot_20260322_125553.png)

Strictly manage mandatory commitments so they are automatically removed from your dashboard.

- **EMIs**: Track your progress on home, car, or personal loans.
- **Fixed Bills**: Ensure rent and internet bills are automatically deducted from your daily buffer.

### 5. Investments & Wealth Tracker

![Investments](graphics/add_expense.png)

Grow your net worth efficiently.

- **Portfolio Overview**: Combine FDs, SIPs, stocks, and crypto securely.
- **Wealth Projection**: Simulate how compound interest grows your investments over 10+ years.
- **Health Nudges**: Get alerted if your cash balance is too high compared to your invested wealth.

### 6. Refundables & More

![Refundables](graphics/add_income.png)

Never lose track of money owed.

- **Lent/Borrowed**: Keep business expenses or lent money isolated from your core budget.
- **Secure Notes**: Save financial ideas safely behind device biometrics.
- **Cloud Sync**: Encrypted, hidden backups to your personal Google Drive.

## Features

*   **Comprehensive Transaction Tracking** — Log income and expenses with detailed categorization, payment modes, and timestamps.
*   **Automated Transaction Detection** — Automatically detects credit and debit alerts from bank notifications and pre-fills transaction details for quick approval.
*   **Intelligent Budgeting** — Set monthly or category-specific budget limits and receive real-time updates on your spending headroom.
*   **Investment Portfolio Management** — Track assets across various categories (stocks, mutual funds, etc.) with net worth visualization.
*   **Loan & Debt Ledger** — Maintain a clear record of borrowed and lent money, complete with due dates and settlement tracking.
*   **Personalized Financial Notes** — Attach detailed notes and descriptions to your financial records to keep track of specific contexts and reminders.
*   **Recurring Expenses & Subscriptions** — Manage fixed monthly costs and active subscriptions to ensure no payment is missed.
*   **Savings Goals** — Define financial milestones and track your progress with dedicated saving suggestions.
*   **Financial Reports** — Gain clear insights into your spending habits and receive automated financial health reports.
*   **Refundable Expense Tracking** — Specialized workflow for tracking business expenses or items awaiting reimbursement.
*   **Biometric Security** — Protect your sensitive financial data with integrated fingerprint and face unlock capabilities.
*   **Google Drive Cloud Backup** — Securely sync and restore your data using your personal Google Drive account.

## Tech Stack

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Modern declarative UI)
*   **Design System:** Material 3 (with support for Dynamic Color)
*   **Architecture:** Clean Architecture + MVVM (Model-View-ViewModel)
*   **Dependency Injection:** Hilt
*   **Local Database:** Room Persistence Library
*   **Preferences Storage:** DataStore (Type-safe key-value storage)
*   **Asynchronous Logic:** Kotlin Coroutines & Flow
*   **Navigation:** Navigation3 (Experimental Jetpack Navigation)
*   **Background Tasks:** WorkManager (for periodic Cloud Backups)
*   **Cloud Integration:** Google Drive API v3
*   **Security:** Biometric API & Credentials Manager
*   **Image Loading:** Coil (implied/common in Compose projects)

## Architecture

The project follows **Clean Architecture** principles combined with **MVVM** to ensure a separation of concerns, testability, and maintainability.

*   **Presentation Layer:** Uses ViewModels to manage UI state and expose data via `StateFlow` to Jetpack Compose screens.
*   **Domain Layer:** Contains the core business logic, domain models, and repository interfaces, keeping the app logic independent of external frameworks.
*   **Data Layer:** Implements repository interfaces, manages Room database operations, handles API interactions (Google Drive), and coordinates background workers.
*   **Data Flow:** Unidirectional Data Flow (UDF) is maintained where the UI sends events to the ViewModel, and the ViewModel updates the UI state based on repository responses.

## Project Structure

```text
app/
├── data/           # Repository implementations, Room DAOs/Entities, WorkManager Workers
├── domain/         # Domain models, Repository interfaces, Use Cases
├── presentation/   # ViewModels, UI State definitions, Navigation logic
├── ui/             # Composable screens, components, and Material 3 theme styling
├── di/             # Hilt modules for dependency injection
├── service/        # Background services (Notification Listener)
└── util/           # Helper classes (Notification Parser, formatting)

drive_backup/       # Independent library module for Google Drive sync logic
```

## Key Components

* **`Spend SenseNavGraph`**: The central navigation engine managing transitions between 17+
  different screens using the latest Navigation3 API.
* **`Spend SenseNotificationListenerService`**: A background service that monitors bank
  notifications and extracts transaction data using regex patterns.
*   **`TransactionRepository`**: The primary data hub that coordinates all financial entry operations across local and remote sources.
*   **`DriveBackupWorker`**: A robust background task that ensures user data is safely backed up to the cloud without manual intervention.
* **`Spend SenseTheme`**: A customized Material 3 design implementation ensuring a premium and
  consistent aesthetic throughout the app.

## Setup & Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-username/Spend Sense.git
    ```
2.  **Open in Android Studio:**
    Use Android Studio Ladybug (2024.2.1) or newer.
3.  **Google Cloud Setup:**
    *   Create a project on [Google Cloud Console](https://console.cloud.google.com/).
    *   Enable the **Google Drive API**.
    *   Configure the OAuth consent screen and create an Android OAuth 2.0 Client ID using your local SHA-1.
4.  **Sync Gradle:**
    Allow the project to download all dependencies defined in `libs.versions.toml`.
5.  **Run:**
    Select a device with **API 30+** and click the "Run" button.

## Requirements

*   **Android Version:** SDK 30 (Android 11) or higher.
*   **Build System:** Gradle 8.0+ with KSP support.
*   **Tooling:** Kotlin 2.0.0+ and Jetpack Compose Compiler.
*   **Hardware:** Biometric hardware (optional, for security features).

## Future Improvements

*   **Multi-Currency Support:** Add ability to track accounts in different currencies with live exchange rates.
*   **Receipt Scanning:** Integrate OCR to automatically extract data from physical receipts.
*   **Enhanced SMS Parsing:** Expand detection patterns for a wider range of international banks and fintech apps.
*   **Paging 3 Integration:** Enhance transaction lists with Paging 3 for smoother scrolling in large datasets.
* **Desktop/Web Version:** Explore Kotlin Multiplatform (KMP) to bring Spend Sense to more platforms
  while sharing the domain and data logic.
