package com.waitasec.app.service

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import com.waitasec.app.WaitASecApp
import com.waitasec.app.data.UserSettings

class WaitAccessibilityService : AccessibilityService() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private lateinit var overlayController: BreathOverlayController

    /** Package currently considered in the foreground (last non-noise window). */
    private var foregroundPackage: String? = null

    /**
     * After the user chooses to continue, keep this package allowed until they
     * leave it for a different app (so in-app activity changes don't re-prompt).
     */
    private var allowedSessionPackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlayController = BreathOverlayController(
            context = this,
            onQuitHome = {
                allowedSessionPackage = null
                performGlobalAction(GLOBAL_ACTION_HOME)
            },
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        // Only window state changes indicate an app / activity switch.
        // Content changes fire constantly while scrolling and must be ignored.
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        if (isIgnorablePackage(packageName)) return
        if (!::overlayController.isInitialized) return
        if (overlayController.isShowing) return

        // Same package still in front — not a new launch (e.g. internal activity).
        if (packageName == foregroundPackage) return

        val previous = foregroundPackage
        foregroundPackage = packageName

        // Left a restricted session for another app → clear the allow pass.
        if (allowedSessionPackage != null &&
            packageName != allowedSessionPackage &&
            !isIgnorablePackage(packageName)
        ) {
            allowedSessionPackage = null
        }

        val settings = try {
            (application as WaitASecApp).repository.currentSettingsBlocking()
        } catch (_: Exception) {
            UserSettings()
        }

        if (!settings.protectionEnabled) return
        if (packageName !in settings.restrictedPackages) return

        // Already chose to continue for this app this session.
        if (packageName == allowedSessionPackage) return

        // Only gate when arriving from a different package (true launch / switch-in).
        if (previous == packageName) return

        mainHandler.post {
            if (overlayController.isShowing) return@post
            if (foregroundPackage != packageName) return@post
            overlayController.show(
                packageName = packageName,
                delaySeconds = settings.delaySeconds,
                onContinue = {
                    allowedSessionPackage = packageName
                },
            )
        }
    }

    override fun onInterrupt() {
        // no-op
    }

    override fun onDestroy() {
        if (::overlayController.isInitialized) {
            overlayController.dismiss()
        }
        mainHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun isIgnorablePackage(packageName: String): Boolean {
        if (packageName == this.packageName) return true
        if (packageName == "com.android.systemui") return true
        if (packageName == "com.android.settings") return true
        if (packageName.startsWith("com.android.launcher")) return true
        if (packageName.contains("launcher", ignoreCase = true)) return true
        if (packageName == "com.google.android.apps.nexuslauncher") return true
        if (packageName == "com.google.android.permissioncontroller") return true
        if (packageName == "com.android.permissioncontroller") return true
        if (packageName == "com.google.android.packageinstaller") return true
        if (packageName == "com.android.packageinstaller") return true
        return false
    }
}
