# Monetra 🚀

**Monetra** is a premium, personal finance operating system designed to guide every rupee you earn.
Unlike traditional expense trackers, Monetra acts as a financial coach, using proactive intelligence
to help you hit savings goals, manage debt, and simulate life-changing financial decisions.

---

## 🌟 Overview

Monetra is built on the principle of **Financial Clarity**. It transforms static transaction logs
into dynamic insights. By integrating your income, mandatory debts (EMIs), and daily spending into a
single "Safe-To-Spend" engine, Monetra ensures you never overspend or miss a savings target.

## ✨ Key Features

### 1. Daily Safe-To-Spend (STS)

* **What it is**: A dynamic daily allowance calculator.
* **Why it exists**: To provide a simple "North Star" number. If you stay under this limit today,
  you are mathematically guaranteed to hit your monthly savings goal.

### 2. Transaction Candidate Detection (Smart Suggestions)

* **What it is**: Automatic detection of financial transactions from device notifications.
* **Why it exists**: To slash the friction of manual entry. Monetra listens for bank/UPI
  notifications and allows you to "One-Tap" convert them into app transactions.

### 3. Wealth Tracker & Net Worth Center

* **What it is**: A consolidated view of all Assets (SIPs, FDs, Stocks) and Liabilities (Loans,
  EMIs).
* **Why it exists**: To track your true financial position beyond just your bank balance.

### 4. Refundable Module

* **What it is**: A specialized tracker for money you've lent or borrowed.
* **Why it exists**: To isolate "reimbursable" expenses from your actual personal spending,
  featuring automated reminders.

### 5. Budget Guard

* **What it is**: Granular category-based spending ceilings.
* **Why it exists**: To provide leakage detection and alert you when specific lifestyle categories (
  like Fun or Food) are draining your savings.

## 🧠 Advanced Technical Features

* **Heuristic Pattern Matching**: An engine that identifies recurring liabilities (
  subscriptions/bills) by analyzing transaction titles and billing cycles.
* **Compound Interest Engine**: Calculates future value of projections for SIPs and investments
  using actual annuity formulas.
* **Scenario Modelling (What-If Simulator)**: A high-fidelity sandbox where users can simulate
  salary hikes, new loan burdens, or SIP changes to see their impact on financial health scores over
  12 months.

## 🛠 Tech Stack

* **Language**: Kotlin (100%)
* **UI Framework**: Jetpack Compose (Modern, Declarative UI)
* **Persistence**: Room Database (Local-first, encrypted approach)
* **Dependency Injection**: Hilt (Dagger-based DI)
* **Reactive Flow**: Kotlin Coroutines & Flow (Unidirectional Data Flow)
* **Background Tasks**: WorkManager (Scheduled reminders and pattern detection)
* **Navigation**: Navigation3 (Newest Android navigation implementation)
* **Local Backup**: Multi-module architecture with a dedicated `drive_backup` layer.

## 🏗 Architecture

Monetra follows **Clean Architecture** combined with **Domain-Driven Design (DDD)**.

* **Presentation**: UI-agnostic ViewModels and Jetpack Compose screens.
* **Domain**: Pure Kotlin logic containing UseCases and business entities.
* **Data**: Repository implementations, Room DAOs, and service integrations.

## 📜 Permissions Used

| Permission                           | Purpose                                                       |
|:-------------------------------------|:--------------------------------------------------------------|
| `BIND_NOTIFICATION_LISTENER_SERVICE` | To detect bank/UPI transactions from notifications.           |
| `POST_NOTIFICATIONS`                 | To send reminders for bills, loans, and refundable entries.   |
| `READ_CONTACTS`                      | To easily select lenders/borrowers for the Refundable module. |
| `INTERNET`                           | To support secure backup and restore functions.               |

## 📂 Project Structure

```text
com.monetra
├── data           # Repository implementations, Room Entities, and Workers
├── di             # Hilt Dependency Injection modules
├── domain         # Business logic (UseCases, Models, Repository Interfaces)
├── presentation   # UI components, Screens, and ViewModels
├── service        # Background services (Notification Listener)
└── util           # Notification parsers and math engines
```

## 🚀 Build & Run

1. Clone the repository.
2. Open in **Android Studio Ladybug** or higher.
3. Ensure you have **SDK 36** (Android 15+) installed.
4. Sync Gradle and run the `:app` module.

---
*Monetra is built with a focus on privacy and performance. All financial data remains encrypted on
your device.*
