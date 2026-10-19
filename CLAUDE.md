# Task Manager - Development Guide

Android app (Kotlin, Jetpack Compose, Material 3). Package `com.example.taskmanager`, single `:app` module.
Offline-only: Room for tasks, DataStore for settings. MVVM + unidirectional data flow, no DI framework.

## Build

- Gradle 9.8, AGP 9.4 (built-in Kotlin: there is no `kotlin-android` plugin and no `kotlinOptions`), Kotlin 2.4, KSP 2.3
- `compileSdk` 37.2 (`release(37) { minorApiLevel = 2 }`), `targetSdk` 37, `minSdk` 26, Java 17
- All versions live in `gradle/libs.versions.toml`; add dependencies there, never inline in `build.gradle.kts`
- Compose versions come from the BOM; don't pin individual Compose artifacts
- SDK packages are installed with the new Android CLI: `~/Library/Android/sdk/cmdline-tools/latest/bin/android sdk install <pkg>` (`sdkmanager` is deprecated)

```bash
./gradlew testDebugUnitTest             # JVM unit tests (fast; run these first)
./gradlew connectedDebugAndroidTest     # Compose UI tests, need an emulator (AVD: Pixel_9_Pro_XL)
./gradlew assembleRelease               # signed, R8-minified APK -> app/build/outputs/apk/release/
```

## Release signing

- Keystore: `~/StudioProjects/keystores/taskmanager-release.jks` (alias `taskmanager`), outside the repo
- Passwords: `keystore.properties` in the repo root, git-ignored. Without it, release builds are left unsigned
- Never commit the keystore or `keystore.properties`. Losing the keystore means future APKs can't update installed ones
- Bump `versionCode`/`versionName` in `app/build.gradle.kts` for every release; APKs go on GitHub Releases, not in git

## Layout

```
data/model        Task entity + TaskStatus/TaskPriority/RepeatRule enums, Checklist encoding
data/local        TaskDatabase (Room, version 6, MIGRATION_5_6), TaskDao
data/repository   TaskRepository interface + RoomTaskRepository, SettingsRepository (DataStore)
reminder          ReminderScheduler (interface + WorkManager impl), ReminderWorker/ReminderNotifications
ui/navigation     Screen routes + AppNavHost (all transitions defined here)
ui/theme          AppTheme, ColorSchemeGenerator (builds the scheme from the user's accent), TaskColors
ui/tasks          TaskListScreen, TaskViewModel, TaskFilters (filter/sort/stats/section grouping)
ui/tasks/components   TaskItem, TaskProgressCard, TaskFilterBar, TaskListTopAppBar, EmptyState, BouncyFAB, TaskShimmerItem
ui/editor         TaskEditorScreen, used for both add (taskId = null) and edit
ui/settings       SettingsScreen, SettingsViewModel
util              DateUtils
```

## Conventions

- **DB compatibility:** `priority`/`status` are stored as display strings ("High", "In Progress") so existing installs keep their data. Read them through `task.priorityLevel` / `task.statusType`, never compare raw strings. Schema changes need a Room `Migration` and a version bump; exported schemas live in `app/schemas/`
- **State:** screens collect `TaskViewModel.uiState` with `collectAsStateWithLifecycle`. Filters live in the ViewModel (not `remember`) so they survive navigation
- **New columns:** additive `ALTER TABLE` in a `Migration`, then extend `MigrationTest` (it validates against `app/schemas/`). `fallbackToDestructiveMigration` is still set, so a missing migration silently wipes user data
- **Reminders:** any code path that saves, completes, deletes or restores a task must call `ReminderScheduler.sync`/`cancel` (see `TaskViewModel`). Completing a repeating task spawns `Task.nextOccurrence()` and the Undo event carries it so it can be removed
- **One-off actions:** delete/complete/clear send a `TaskEvent`; `TaskListScreen` turns it into a snackbar with Undo. Add new undoable actions the same way
- **ViewModels:** constructed via `companion object { val Factory = viewModelFactory { ... } }`. `TaskViewModel` takes a `TaskRepository` so tests can pass a fake
- **Dates:** go through `DateUtils`. Material `DatePicker` returns UTC midnight; convert with `combineUtcDateWithLocalTime` / `toUtcDateMillis` or dates shift a day west of UTC
- **Colours:** use `MaterialTheme.colorScheme` or `TaskColors`; no hardcoded `Color.White`/`Color.Gray` (light and dark must both work)
- **Strings:** user-facing text goes in `res/values/strings.xml`. Enum `displayName`s are the exception (persisted in the DB)
- **Test tags:** UI tests rely on tags such as `TaskItem_<index>` (on the clickable Card, so its merged text is assertable), `TaskCheck_<index>`, `StatusOption_*`, `PriorityFiltering`/`PriorityOption_*`, `SortFiltering`/`SortOption_<ENUM>`, `TitleField`, `SaveTask`, `DatePickerField`/`DatePickerOK`, `DeleteTask`/`ConfirmDelete`, `StatTile_<STATUS>`, `Section_<SECTION>`, `ReminderOption_*`, `RepeatOption_<ENUM>`, `ChecklistNewItem`/`ChecklistCheck_<index>`. Keep them when refactoring
- **Icons:** only `material-icons-core` is included (no extended set); extra Material Symbols live as vector drawables in `res/drawable` (`ic_schedule`, `ic_repeat`, `ic_flag`, `ic_checklist`, `ic_notification`)
- **Commits:** no AI attribution lines in commit messages

## Release status (2026-10-20)

v2.1 is released (signed APK on GitHub Releases as `TaskManager-v2.1.apk`): redesign, reminders, repeating tasks, checklists, sections. DB is v6.
26/26 unit tests and 11/11 instrumented tests pass on `Pixel_9_Pro_XL`. The release build was smoke-tested on the emulator, including an in-place upgrade from v2.0 (data kept) and a forced reminder job posting its notification under R8.
README screenshots live in `docs/screenshots/`. To regenerate: build a seeded `app_database` on the host from `app/schemas/.../<version>.json` (create SQL + `room_master_table` identity hash + `PRAGMA user_version`), `adb push` it, then `run-as com.example.taskmanager cp` it into `databases/` (debug build only; release isn't debuggable).
Emulator tips: this Mac has 16 GB, so close other heavy apps. If `connectedDebugAndroidTest` says "No compatible devices" or the package service disappears, the emulator's watchdog is restarting `system_server`; start it with `-no-window` and wait for `/proc/loadavg` to drop before testing. `adb shell am instrument -w -r com.example.taskmanager.test/androidx.test.runner.AndroidJUnitRunner` gives clearer output than Gradle.
