
---

# 📱 PenDown

## 🚀 Overview

This project is a **full-featured blogging platform Android application** designed to provide a smooth and interactive experience for both **content creators and readers**.

As an Android developer, my focus was on building a **real-time, scalable, and user-friendly app** using modern tools and architecture. The platform allows users to **create, explore, and engage with articles seamlessly**, with real-time updates powered by Firebase.

---

## 💡 Product Perspective

The app is built to bridge the gap between **bloggers and readers** by offering:

* A clean and intuitive UI for content consumption
* Easy content publishing and management for writers
* Social interaction features like **likes and saves**
* Real-time synchronization for a dynamic experience

---

## 🛠️ Tech Stack

* **Language:** Kotlin
* **Backend & Database:** Firebase Realtime Database
* **Authentication:** Firebase Authentication
* **Storage:** Appwrite (for profile images)
* **UI Components:** RecyclerView, Activities
* **Image Loading:** Glide

---

## 🔑 Key Features

### 🔐 User Authentication & Profile Management

* Implemented secure authentication using **Firebase Authentication**
* Supports **user registration, login, and session persistence**
* User profiles include:

  * Profile image
  * Username
  * Activity data
* Profile data stored and managed via Firebase

---

### ✍️ Content Creation & Editing

* Users can create and manage blog posts using `AddArticleActivity.kt`
* Each post includes:

  * Title
  * Content
  * Metadata
* Posts are stored in Firebase under a unique ID structure (`blogs`)
* Supports **editing and updating existing articles**

---

### 📰 Content Feed & Navigation

* `MainActivity.kt` displays all blog posts using **RecyclerView**
* `BlogAdapter.kt` handles efficient rendering of dynamic content
* Users can navigate to detailed views via `ReadMoreActivity.kt`

---

### 👤 User-Specific Articles

* `ArticleActivity.kt` shows **only the logged-in user’s posts**
* Enables creators to easily manage their own content

---

### ❤️ Interaction Features

#### 👍 Likes

* Users can like/unlike posts
* Updates handled in real-time:

  * `likeCount`
  * `likedBy` list
* Implemented in adapters (`BlogAdapter.kt`, `ArticleAdapter.kt`)

#### 🔖 Saves

* Users can save posts for later
* Saved posts are stored under user-specific nodes (`saveBlogPosts`)
* Visual feedback provided for saved state

---

### 🗂️ Content Management

* Users can:

  * Edit their posts
  * Delete posts
* Deletion removes data directly from Firebase
* Managed via `ArticleAdapter.kt`

---

### 🔄 Real-Time Data Sync

* Used Firebase `ValueEventListener` to:

  * Listen for changes
  * Update UI instantly
* Ensures:

  * Live updates for posts
  * Real-time likes and saves
  * Seamless user experience

---

### 🔁 Navigation & Session Handling

* `StartActivity.kt` manages authentication flow
* Maintains user sessions across app launches
* `ProfileActivity.kt` allows:

  * Viewing profile details
  * Logging out

---

### 🖼️ Additional Features

* Profile image upload via **Appwrite Storage** during registration
* Detailed article view with images
* Saved articles screen (`SavedArticlesActivity.kt`)
* Efficient asynchronous data fetching

---

## 🧠 Architecture Approach

From an Android development standpoint, the app is designed with:

* **Activity-based architecture** for modular flow
* **Adapter pattern** for scalable UI rendering
* **Firebase integration** for backend simplicity and real-time updates
* Clean separation of concerns between:

  * UI
  * Data handling
  * User interaction

---

## 📌 Conclusion

This project demonstrates my ability to build a **real-world Android application** with:

* End-to-end functionality
* Real-time database integration
* Interactive UI/UX
* Scalable architecture

---
