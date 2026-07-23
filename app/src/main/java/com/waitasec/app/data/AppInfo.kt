package com.waitasec.app.data

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
)

data class UserSettings(
    val restrictedPackages: Set<String> = emptySet(),
    val delaySeconds: Int = DEFAULT_DELAY_SECONDS,
    val protectionEnabled: Boolean = true,
    val onboardingComplete: Boolean = false,
) {
    companion object {
        const val DEFAULT_DELAY_SECONDS = 3
        const val MIN_DELAY_SECONDS = 2
        const val MAX_DELAY_SECONDS = 10
    }
}

/** Well-known packages suggested as time-wasters when installed. */
val SUGGESTED_PACKAGE_NAMES = setOf(
    "com.instagram.android",
    "com.zhiliaoapp.musically", // TikTok
    "com.ss.android.ugc.trill", // TikTok alternate
    "com.google.android.youtube",
    "com.twitter.android",
    "com.reddit.frontpage",
    "com.facebook.katana",
    "com.facebook.orca",
    "com.snapchat.android",
    "com.netflix.mediaclient",
    "com.discord",
)
