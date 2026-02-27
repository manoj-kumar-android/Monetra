# Testing Refundable Reminders in Monetra

This document explains how to verify and test the automated reminder systems for refundable entries.

## 1. Prerequisites (Mandatory)
For reminders and SMS to work, the app must have the following permissions granted:
- **Notifications**: To show the "Due Today" alert.
- **SMS**: To send the automatic reminder to the person.
- **Background Data/Execute**: (Usually default) To allow WorkManager to run.

**How to grant:**
Go to **Settings > Apps > Monetra > Permissions** and ensure **SMS** and **Notifications** are Allowed.

---

## 2. Immediate Testing (Direct SMS)
Verify that the app can send SMS and record them in your system's Messaging app.

1. Open a **Refundable Entry** detail screen.
2. Click the **SMS** quick action button.
3. You should see a success toast: *"Direct SMS sent to..."*
4. Open your phone's default **Messaging app** (Google Messages, etc.).
5. Check if the message appears in the conversation history for that contact.

---

## 3. Testing Automated Background Reminders

### Method A: Specific Time Schedule Test (New Feature)
1. Create a new entry or edit an existing one.
2. Set the **Due Date** to today.
3. Set the **Time** strictly at least 1-2 minutes in the future from now.
4. Enable **"Remind me"** or **"Send SMS Reminder"**.
5. Ensure the entry is **Not Paid**.
6. Wait 1-2 minutes. The reminder will fire exactly at the scheduled time!

### Method B: The Daily Backup Test
The app also runs a daily backup check for things due today that might have missed their alarm.
1. Create an entry due **Today** (time doesn't matter for this test).
2. Enable reminder toggles.
3. Wait for the daily backup worker to trigger (runs once daily).

### Method B: Force Trigger via ADB (Recommended for Developers)
If you don't want to wait 24 hours, you can force the worker to run immediately using a computer with ADB installed:

1. Connect your phone to your PC.
2. Open a terminal and run the following command to find the worker ID:
   ```bash
   adb shell dumpsys jobscheduler | grep com.monetra
   ```
3. Look for `refundable_reminders`.
4. Run the worker immediately:
   ```bash
   adb shell am broadcast -a androidx.work.diagnostics.REQUEST_DIAGNOSTICS -p com.monetra
   ```
   *(Note: This varies by Android version. Alternatively, you can temporarily change the code in `MontraApplication.kt` from `1, TimeUnit.DAYS` to `15, TimeUnit.MINUTES` to test naturally)*

---

## 4. Troubleshooting
- **No Notification?** Check if "Do Not Disturb" is on or if notifications are disabled in Android settings.
- **No SMS sent?** Ensure the phone number in the entry has a valid format (e.g., +91... or a full 10-digit number).
- **Not reflecting updates?** Ensure you are on the latest version where the Detail screen observes the database in real-time.
