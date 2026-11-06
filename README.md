# PostsHub (Android)

A small Jetpack Compose app for browsing, favoriting, and editing blog posts. Your task is to find and fix the intentional bugs in it.

## Requirements
- A recent Android Studio (2026.1 or later — the project uses Android Gradle Plugin 9.1)
- JDK 17 or later (the one bundled with Android Studio is fine)
- An emulator or device running Android 8.0 (API 26) or later
- Internet access (the app calls a public API)

## Setup
1. In Android Studio choose **File → Open** and select this folder.
2. Wait for the Gradle sync to finish.
3. Pick an emulator or device and press **Run** (`Shift+F10` / `Ctrl+R`).

From the command line:
```sh
./gradlew installDebug
adb shell am start -n com.brivo.claude.postshub/.MainActivity
```

## Tech stack
Kotlin, Jetpack Compose (Material 3), Navigation Compose, `ViewModel` + `StateFlow`, kotlinx.serialization, OkHttp, Coil.

## What the app does

Data is fetched from [DummyJSON](https://dummyjson.com) (`/posts`, `/posts/search`, `/users`). The API is read-only — any "saved" edit lives only in memory for the session.

### Screens & navigation
- **Posts** (tab 1) — list of posts with a search field and a user-filter button (top-right). Tap a row to open the Detail screen. Pull down to refresh. Swipe a row left to delete.
- **Favorites** (tab 2) — posts you've marked with the heart icon.
- **Post Detail** — opened from the Posts list. Edit title and body, save changes, toggle favorite.
- **User Filter Sheet** — bottom sheet launched from the Posts tab top bar; pick an author to filter the list.

## The challenge

The app builds and runs, but contains more than **10 intentional bugs**. They fall into these categories:

- UI / layout bugs
- State-management bugs
- Serialization / decoding bugs
- Crash bugs
- Performance or threading bugs
- Mixed bonus bugs (Compose identity, animations, search, coroutines)

For each bug you find, please explain:
1. What you observed.
2. What is causing it.
3. How you would fix it (and why your fix is the right one).

Watch Logcat while you exercise the app — several bugs surface there first. Every log tag starts with `PostsHub.`, so filter with `tag:PostsHub` in Android Studio's Logcat, or from a terminal:
```sh
adb logcat -s PostsHub.App PostsHub.API PostsHub.Store PostsHub.List PostsHub.Detail PostsHub.Row PostsHub.Favorites PostsHub.Filter AndroidRuntime
```
Useful categories: `App`, `API`, `Store`, `List`, `Detail`, `Row`, `Favorites`, `Filter`.

Tip: long‑press the filter button (top‑right of the Posts tab) to dump the current `PostStore` state to the log.

### Sample API responses

`GET https://dummyjson.com/posts?limit=1`
```json
{
  "posts": [
    {
      "id": 1,
      "title": "His mother had always taught him",
      "body": "His mother had always taught him not to ever think of himself as better than others...",
      "tags": ["history", "american", "crime"],
      "reactions": { "likes": 192, "dislikes": 25 },
      "views": 305,
      "userId": 121
    }
  ],
  "total": 251, "skip": 0, "limit": 1
}
```

`GET https://dummyjson.com/users?limit=1`
```json
{
  "users": [
    {
      "id": 1,
      "firstName": "Emily", "lastName": "Johnson", "username": "emilys",
      "email": "emily.johnson@x.dummyjson.com", "phone": "+81 965-431-3024",
      "image": "https://dummyjson.com/icon/emilys/128",
      "address": {
        "address": "626 Main Street", "city": "Phoenix", "state": "OK",
        "postalCode": "31313",
        "coordinates": { "lat": -77.16482, "lng": -92.084824 }
      }
    }
  ],
  "total": 208, "skip": 0, "limit": 1
}
```
