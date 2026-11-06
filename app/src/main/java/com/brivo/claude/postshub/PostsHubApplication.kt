// =====================================================================
// PostsHub — Jetpack Compose debugging exercise
//
// HOW TO RUN
// 1. Open this folder in Android Studio (2026.1 or later).
// 2. Let Gradle sync, then pick any emulator or device running API 26+.
// 3. Press Run (Shift+F10) or run `./gradlew installDebug`.
//
// The app fetches data from https://dummyjson.com. The INTERNET
// permission is already declared in AndroidManifest.xml.
// =====================================================================

package com.brivo.claude.postshub

import android.app.Application
import android.os.Build
import com.brivo.claude.postshub.logging.Log

class PostsHubApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.app.event("──── PostsHub launching ────")
        Log.app.event(
            "version=${BuildConfig.VERSION_NAME} build=${BuildConfig.VERSION_CODE} " +
                "os=Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        )
        Log.app.event("baseURL=https://dummyjson.com")
    }
}
