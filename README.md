📌 Task Manager - Android Application

A Task Manager app built with Jetpack Compose, sorting, filtering, haptic feedback, swipe actions, and smooth animations.


🚀 Features

✅ Task Creation & Management – Add, edit, and delete tasks.

✅ Sorting – Sort tasks by due date, priority, or status.

✅ Filtering – Filter tasks by status (Pending, In Progress, Completed).

✅ Swipe Actions – Swipe left/right to delete or complete tasks.

✅ Animations – Smooth transitions and list animations.

✅ Persistent Storage – Data saved using Room Database.

✅ Dark Mode Support – Seamless light/dark theme switching.

✅ Primary Color Customization – Choose your own theme color.

✅ Modern UI – Built with Jetpack Compose.


📦 Tech Stack

Kotlin – Primary language

Jetpack Compose – UI framework

Room Database – Local storage

Hilt – Dependency Injection

Navigation Component – Multi-screen navigation

Coroutines & Flow – Asynchronous operations


🛠️ Setup Instructions

1️⃣ Clone the Repository

git clone https://github.com/YOUR_GITHUB_USERNAME/TaskManager.git

cd TaskManager

2️⃣ Open in Android Studio

Open Android Studio (latest version recommended).

Select "Open an Existing Project" and choose the TaskManager directory.

Wait for Gradle sync to complete.

3️⃣ Run the App

Connect a physical device or emulator.

Click Run ▶ in Android Studio.


Design Rationale

🌟 1. Jetpack Compose for UI

Declarative UI improves readability and reduces boilerplate.

State management with remember and LaunchedEffect ensures UI updates efficiently.

LazyColumn + animateItemPlacement enables smooth animations.

<img width="372" alt="Screenshot 2025-03-08 at 12 04 10 PM" src="https://github.com/user-attachments/assets/da73e287-311a-4372-bfef-82a43a518738" />


📌 2. Sorting & Filtering for Task Organization

Sorting options allow users to arrange tasks by due date, priority, or status.

Filtering lets users view tasks based on status (Pending, In Progress, Completed).

Uses StateFlow to update UI reactively when sorting or filtering is applied.

<img width="372" alt="Screenshot 2025-03-08 at 12 04 10 PM" src="https://github.com/user-attachments/assets/e43614cd-dcee-470a-b56a-5f0cce2eef7a" />


🌙 3. Dark Mode & Theme Customization

Automatic theme switching based on system settings.

User can manually toggle Light/Dark mode from settings.

Primary color customization lets users personalize their experience.

<img width="372" alt="Screenshot 2025-03-08 at 12 04 10 PM" src="https://github.com/user-attachments/assets/9e546626-45df-4260-9474-1c7d344d0951" />


🔄 4. Swipe Actions for Quick Task Management

Swipe left to delete.

Swipe right to mark complete.

SwipeableActionsBox provides intuitive interactions.

<img width="372" alt="Screenshot 2025-03-08 at 12 04 10 PM" src="https://github.com/user-attachments/assets/b9350c8a-f1a7-4b86-9ffe-7d8e6e3e241b" />

APK File for Testing: 
https://drive.google.com/file/d/1KGY56p5Od076wqvRQbsID3YjijQRfwqb/view



📅 5. Room Database for Persistence
Stores tasks locally using Room.
Uses Flow & LiveData to reactively update UI.
Ensures data persistence even after app restarts.
