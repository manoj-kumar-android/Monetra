# 🚀 Monetra: Next-Gen Personal Finance Intelligence

Welcome to **Monetra**, a state-of-the-art Android Personal Finance and Wealth Management application! Monetra transcends traditional budget tracking by offering intelligent insights, interactive visualizations, and powerful simulation tools designed to help you plan your financial future with confidence.

---

## 📸 Core Features

### 1. 📊 Interactive Dashboard
- **Daily Safe-To-Spend Allowance**: A dynamic, liquid-fill progress card tracks exactly how much you can spend today without breaking your long-term goals.
- **Financial Waterfall**: Visually track how your Monthly Salary trickles down through Fixed Costs, EMIs, and Savings Goals before reaching your pocket.
- **Metric Cards**: Beautifully animated snapshots of your Weekly Activity, Burn Rate, and Fixed Spend.

### 2. ⚡ The Assistant (Intelligence Hub)
- **Quick Action Control Center**: Add Goals, manage Debt, or map out Investments quickly via colorful action grids.
- **Smart Recommendations**: Evaluates your current metrics and pushes actionable tips (e.g., "Your emergency fund is fully funded. Consider shifting excess cash to high-yield investments.").
- **Goal Tracking**: Interactive circular progress rings (using Jetpack Compose Canvas APIs) for customized financial milestones.

### 3. 🔮 Premium What-If Simulator
Ever wondered: *"Can I afford this new car EMI without ruining my vacation savings?"* 
- Model how adding new EMIs, increasing your salary, or adjusting SIPs will impact your financial health.
- Features a **12-Month Accumulation Projection Graph** drawn fully natively, illustrating the real-world impact of your simulated choices against your current baseline trajectory.

### 4. 🚀 Seamless Onboarding
A beautifully crafted 4-step wizard for first-time users:
1. Input your baseline Salary and Savings Goals.
2. Outline fixed recurring commitments (e.g., Rent, WiFi).
3. Log existing EMIs and Debts.
4. Watch Monetra crunch the numbers and output your customized "Safe-to-Spend" formula.
*(The onboarding flow intelligently hides itself after your first successful setup!)*

---

## 🛠 Tech Stack & Architecture

This application is built entirely using modern Android Development standards, strictly adhering to Clean Architecture principles.

*   **Language**: [Kotlin](https://kotlinlang.org/)
*   **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material Design 3)
*   **Architecture**: Clean Architecture (Presentation, Domain, Data Layers) + MVVM 
*   **Dependency Injection**: [Hilt](https://dagger.dev/hilt/)
*   **Asynchronous Programming**: Coroutines & Flow (`StateFlow`, `SharedFlow`)
*   **Local Database**: [Room](https://developer.android.com/training/data-storage/room)
*   **Navigation**: Jetpack Navigation Compose
*   **Build System**: Gradle (Kotlin DSL) version catalogs

### Project Structure
```
app/src/main/java/com/monetra/
├── data/           # Repositories implementations, local Room db, DAOs & Entities
├── domain/         # Core business logic, Use Cases, Interfaces, and pure Kotlin Models
├── presentation/   # UI Layer: Composables, ViewModels, and Navigation Graphs
│   ├── navigation/        # MonetraNavGraph and Bottom Navigation
│   └── screen/            # Feature screens (Dashboard, Onboarding, Simulator, etc.)
└── ui/theme/       # Material 3 coloring, typography, shapes, and spacing tokens
```

---

## 💻 Getting Started (How to Run)

### Prerequisites
*   **Android Studio**: Ladybug (or newer recommended)
*   **JDK 17**
*   **Android SDK API Level**: Target SDK 34, Min SDK 24

### Installation
1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/your-username/monetra.git
    cd monetra
    ```
2.  **Open in Android Studio**:
    Open Android Studio, select `Open an existing Android Studio project`, and choose the Monetra folder.
3.  **Sync Gradle**:
    Let Android Studio sync all dependencies (Hilt, Room, Compose UI packages) specified in the `build.gradle.kts` and `libs.versions.toml` files.
4.  **Run the App**:
    Connect an Android Emulator or a Physical Device (via USB or WiFi debugging). Click **Run 'app'** (`Shift + F10`).

---

## 📱 How to Use Monetra

### 1. First Launch (Onboarding)
Upon launching the app for the very first time, you will be greeted with the Onboarding Screen. 
Input your core financial metrics. Be realistic! Monetra's algorithm relies on these numbers to calculate your actual spending boundaries.

### 2. The Dashboard Experience
Once on the dashboard, the massive "Daily Safe to Spend" card is your main compass. If it flips to red, pull your wallet back! Use the Quick Add circular button to instantly register whenever you make an expense. 

### 3. Ask the Assistant (Your Personal Financial Advisor)
Navigate to the "Assistant" tab via the bottom navigation bar to access your centralized intelligence hub. Here is how to make the most of it:
- **Net Worth & Health Score**: At the very top, view your estimated net worth, calculated by subtracting your dynamic liabilities (like outstanding EMIs) from your assets (savings, investments). Next to it is a unified "Health Score," grading your current financial standing out of 100.
- **Quick Action Grid**: Use the 4 interactive buttons to quickly manage your finances:
  - **Set Goal**: Open a popup dialog to name a new goal (e.g., "Paris Trip") and map out a target amount. The Assistant will automatically track progress via circular indicators.
  - **Investments**: Manage your active SIPs and mutual funds.
  - **Manage Debt**: Keep an eagle eye on your active loans and see how much principal is left.
  - **Simulator**: Jump into the What-if Simulator.
- **Smart Insights**: The Assistant continuously monitors your spending. If it detects that your "Daily Safe to Spend" limit is frequently exceeded, it will push a proactive advice card telling you which goal is currently "at risk."

### 4. The What-if Simulator (Risk-Free Future Planning)
Found through the Assistant tab or Quick Action Grid, this powerful tool lets you peek into the future before making large financial commitments.
- **Adjust the Sliders**: You will see several intuitive sliders:
  - *Salary Change*: Expecting a raise next month? Slide this up.
  - *New EMI Amount*: Thinking about buying an iPhone on EMI? Add the expected monthly cost here.
  - *New SIP Amount*: Want to see what happens if you increase your mutual fund investments?
- **Live Status Feedback**: As you drag the sliders, the main header card instantly updates its color-coded status. It will flash **Green (Healthy)** if your new EMI doesn't choke your budget, or **Red (At Risk)** if your new commitments will push your actual savings below zero.
- **Impact Analysis**: See a side-by-side comparison of your current "EMI to Income" and "Savings to Income" ratios against the **Projected** ratios after taking the new loan.
- **12-Month Projection Graph**: At the bottom of the screen, view a fully native, beautifully rendered line chart. The dotted grey line shows your baseline saving trajectory for the next 12 months. The solid colored line shows what your trajectory *will be* if you proceed with your simulated changes.

---

## 🎨 Design Philosophy
Monetra explicitly rejects "boring finance app" designs. You'll notice rich color gradients, dynamic interactive components, glass-morphism cards, and smooth micro-animations. Managing money should feel premium and empowering. We leverage Compose's Animation and Canvas APIs extensively to achieve this fluid UX.

---

## 🤝 Contributing
Feel free to open issues, submit pull requests, or fork the project to add your own features. Whether it's integrating machine learning for predictive spending, adding Cloud Backups, or refining the UI, contributions are deeply appreciated!

## 📄 License
```
Copyright 2026

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
