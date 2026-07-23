package com.waitasec.app.service

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.waitasec.app.ui.overlay.BreathOverlayContent
import com.waitasec.app.ui.theme.WaitASecTheme

class BreathOverlayController(
    private val context: Context,
    private val onLeaveHome: () -> Unit,
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: ComposeView? = null
    private var host: OverlayLifecycleHost? = null
    private var showingForPackage: String? = null

    val isShowing: Boolean get() = overlayView != null

    fun show(packageName: String, delaySeconds: Int, onComplete: () -> Unit) {
        if (overlayView != null) return
        showingForPackage = packageName

        val label = resolveLabel(packageName)
        val host = OverlayLifecycleHost().also { this.host = it }
        host.onCreate()

        val view = ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setViewTreeLifecycleOwner(host)
            setViewTreeSavedStateRegistryOwner(host)
            setContent {
                WaitASecTheme {
                    BreathOverlayContent(
                        delaySeconds = delaySeconds,
                        appLabel = label,
                        onComplete = {
                            dismiss()
                            onComplete()
                        },
                        onLeave = {
                            dismiss()
                            onLeaveHome()
                        },
                    )
                }
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        try {
            windowManager.addView(view, params)
            overlayView = view
            host.onResume()
        } catch (_: Exception) {
            host.onDestroy()
            this.host = null
            overlayView = null
            showingForPackage = null
        }
    }

    fun dismiss() {
        val view = overlayView ?: return
        try {
            windowManager.removeView(view)
        } catch (_: Exception) {
            // already removed
        }
        host?.onDestroy()
        host = null
        overlayView = null
        showingForPackage = null
    }

    fun showingFor(): String? = showingForPackage

    private fun resolveLabel(packageName: String): String {
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
        }
    }
}

/** Minimal Lifecycle + SavedState host so ComposeView works outside an Activity. */
private class OverlayLifecycleHost : LifecycleOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    fun onCreate() {
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    fun onResume() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}

/** Packages / classes that should never trigger the breath gate. */
fun AccessibilityEvent.isOwnOrSystemNoise(ownPackage: String): Boolean {
    val pkg = packageName?.toString() ?: return true
    if (pkg == ownPackage) return true
    if (pkg == "com.android.systemui") return true
    if (pkg.startsWith("com.android.launcher")) return true
    if (pkg.contains("launcher", ignoreCase = true)) return true
    return false
}
