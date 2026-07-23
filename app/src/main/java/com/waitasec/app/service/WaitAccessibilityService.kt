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

    /** After a successful wait, ignore re-triggers for this package briefly. */
    private val cooldownUntilMs = mutableMapOf<String, Long>()
    private var lastHandledPackage: String? = null
    private var lastHandledAtMs: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlayController = BreathOverlayController(
            context = this,
            onLeaveHome = {
                performGlobalAction(GLOBAL_ACTION_HOME)
                // Cooldown so returning to launcher doesn't immediately re-fire
                lastHandledPackage = null
            },
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            return
        }

        val packageName = event.packageName?.toString() ?: return
        if (event.isOwnOrSystemNoise(ownPackage = this.packageName)) return
        if (!::overlayController.isInitialized) return
        if (overlayController.isShowing) return

        val now = System.currentTimeMillis()
        // Debounce rapid duplicate events for same package
        if (packageName == lastHandledPackage && now - lastHandledAtMs < 800L) return

        val cooldown = cooldownUntilMs[packageName] ?: 0L
        if (now < cooldown) return

        val settings = try {
            (application as WaitASecApp).repository.currentSettingsBlocking()
        } catch (_: Exception) {
            UserSettings()
        }

        if (!settings.protectionEnabled) return
        if (packageName !in settings.restrictedPackages) return

        lastHandledPackage = packageName
        lastHandledAtMs = now

        mainHandler.post {
            if (overlayController.isShowing) return@post
            overlayController.show(
                packageName = packageName,
                delaySeconds = settings.delaySeconds,
                onComplete = {
                    // Allow the restricted app to stay in foreground without re-prompting
                    cooldownUntilMs[packageName] = System.currentTimeMillis() + COOLDOWN_MS
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

    companion object {
        private const val COOLDOWN_MS = 45_000L
    }
}
