# 📌 Task Manager

A clean, offline-first task manager for Android, built entirely with **Jetpack Compose** and **Material 3**.
Plan your day, track progress at a glance, and keep work organised with filters, search, and swipe gestures.

<p align="center">
  <img src="docs/screenshots/list-light.png" alt="Task list (light)" width="260" />
  <img src="docs/screenshots/list-dark.png" alt="Task list (dark)" width="260" />
  <img src="docs/screenshots/editor.png" alt="Task editor" width="260" />
</p>

---

## 📥 Download

Grab the latest signed APK from [**GitHub Releases**](https://github.com/Umar888/TaskManager/releases/latest) and open it on your phone (you may need to allow installs from your browser or file manager). Requires Android 8.0 (API 26) or newer.

---

## ✨ Features

### Tasks
- **Create, edit and delete** tasks with a title, description, priority, status and due date + time
- **Quick dates**: one tap for *Today*, *Tomorrow* or *Next week*
- **Inline completion**: tap the round checkbox on any card to finish a task
- **Swipe gestures**: swipe right to complete/reopen, swipe left to delete
- **Undo everywhere**: deletes, completions and "Clear completed" can all be undone from the snackbar
- **Overdue highlighting**: late tasks are flagged in red and counted in the header
- **Unsaved-changes guard**: leaving the editor with edits asks before discarding them
- **Reminders**: get a notification at the due time, or 10 minutes, 1 hour or 1 day before
- **Repeating tasks**: daily, weekdays, weekly or monthly; completing one schedules the next (and Undo takes it back)
- **Checklists**: break a task into steps; progress shows on the card

### Organise
- **Today at a glance**: greeting, date, completion ring, overdue and due-today badges
- **Smart sections**: *Overdue, Today, Tomorrow, Upcoming, Completed*, each collapsible
- **Tap-to-filter stats**: the Pending / In progress / Done tiles double as filters
- **Status chips** with live counts: *All · Pending · In Progress · Completed*
- **Priority and date filters**: *Overdue, Today, Tomorrow, This week, This month*
- **Sorting**: *Due soonest, Due latest, Priority, Recently added* (completed tasks always sink to the bottom)
- **Search** across titles *and* descriptions
- Filters live in the ViewModel, so they survive opening a task and coming back

### Personalise
- **Theme mode**: System, Light or Dark
- **Accent colour**: 13 curated colours, each checked for contrast in both themes
- **Wallpaper colours** (Material You) on Android 12+
- **Themed app icon** support on Android 13+
- Edge-to-edge layout with smooth, shared slide/fade navigation transitions
- Haptic feedback when completing tasks, large touch targets, screen-reader labels throughout

---

## 🧱 Tech stack

| Area | Library |
| --- | --- |
| Language | Kotlin 2.4 |
| UI | Jetpack Compose (BOM 2026.09), Material 3 |
| Navigation | Navigation Compose 2.10 |
| Persistence | Room 2.8 (KSP), DataStore Preferences 1.2 |
| Background work | WorkManager 2.11 (reminders) |
| Async | Kotlin Coroutines 1.11 + Flow |
| Architecture | MVVM, unidirectional data flow, repository pattern |
| Gestures | [saket/swipe](https://github.com/saket/swipe) 1.3 |
| Testing | JUnit 4, Truth, kotlinx-coroutines-test, Compose UI Test, Room MigrationTestHelper |
| Build | Gradle 9.8, Android Gradle Plugin 9.4, version catalog |

**SDK levels:** `minSdk 26` · `targetSdk 37` · `compileSdk 37.2`

---

## 🗂️ Project structure

```
app/src/main/java/com/example/taskmanager
├── MainActivity.kt              # Entry point: edge-to-edge, theme, nav host
├── data
│   ├── local                    # Room database + DAO
│   ├── model                    # Task entity, TaskStatus, TaskPriority, RepeatRule, Checklist
│   └── repository               # TaskRepository (interface + Room impl), SettingsRepository
├── reminder                     # WorkManager scheduler + worker that posts notifications
├── ui
│   ├── navigation               # Routes + AppNavHost with transitions
│   ├── theme                    # Colours, typography, dynamic colour-scheme generator
│   ├── tasks                    # Task list screen, TaskViewModel, filters/sort/stats
│   │   └── components           # Task card, progress card, filter bar, app bar, FAB, empty state
│   ├── editor                   # Add / edit task screen (one screen for both)
│   └── settings                 # Settings screen + SettingsViewModel
└── util                         # Date helpers
```

**How data flows:** Room emits a `Flow<List<Task>>` → `TaskViewModel` combines it with the current
`TaskFilters` into a single `TaskListUiState` → Compose collects it with `collectAsStateWithLifecycle`.
One-off actions (delete, complete, clear) are sent back as `TaskEvent`s, which the list screen turns into
snackbars with **Undo**.

`TaskRepository` is an interface, so UI tests run against an in-memory fake instead of a real database.

---

## 🚀 Getting started

### Requirements
- Android Studio (latest stable)
- JDK 17+
- Android SDK Platform **37.2** and Build-Tools **37**

### Build and run
```bash
git clone https://github.com/Umar888/TaskManager.git
cd TaskManager
./gradlew installDebug        # or press ▶ Run in Android Studio
```

### Tests
```bash
./gradlew testDebugUnitTest          # JVM unit tests: filters, sorting, stats, dates, ViewModel undo
./gradlew connectedDebugAndroidTest  # Compose UI tests (needs a device or emulator)
```

---

## 📝 What's new in 2.1

**New**
- Reminders with notifications (Android 13+ asks for permission only when you first set one)
- Repeating tasks: daily, weekdays, weekly, monthly
- Checklists inside tasks, with progress on the card
- List grouped into collapsible *Overdue / Today / Tomorrow / Upcoming / Completed* sections
- Notifications status card in Settings

**Redesign**
- Greeting header and a new hero card built from the theme's container colours, so it reads well in light, dark and wallpaper themes (fixes low contrast in dark mode)
- Cleaner task cards: priority-coloured checkbox, flag badge, and icons for reminder, repeat and checklist
- Editor reworked into a large title field plus *Details*, *Schedule* and *Checklist* cards
- Rounder shapes and a fuller type scale; haptics and better screen-reader labels

**Under the hood**
- Database v6 with a real migration: upgrading from 2.0 keeps all your tasks (covered by an instrumented migration test)
- 26 unit tests and 11 instrumented tests (including the migration)

## 📝 What's new in 2.0

**Fixes**
- Back button did nothing on the task detail screen (a second, empty `BackHandler` swallowed it)
- Dark-mode toggle couldn't switch to light while the phone was in dark mode, and the accent colour didn't react to theme changes
- "This week / month / year" filters skipped tasks due earlier on the first day
- Due dates could shift by a day in timezones west of UTC
- Overdue tasks couldn't be edited because the date picker blocked past dates
- Drag-to-reorder looked like it worked but never actually changed anything (removed)
- Snackbars waited a full second before appearing

**Improvements**
- Redesigned list, editor and settings screens; new adaptive app icon
- Add and edit merged into a single editor; delete now asks for confirmation
- Due time picker, quick-date chips, title validation, discard-changes prompt
- Overdue filter, priority sort, description search, clear completed, undo for every action
- Upgraded to the latest Gradle, AGP, Kotlin, Compose and AndroidX libraries
- Reorganised packages by layer/feature and removed unused code and resources
- Added unit tests and rewrote the UI tests for the new screens
