package com.waitasec.app.util

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.waitasec.app.data.AppInfo
import com.waitasec.app.data.SUGGESTED_PACKAGE_NAMES
import com.waitasec.app.service.WaitAccessibilityService

object AccessibilityHelper {
    fun isServiceEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabled = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val pkg = context.packageName
        val className = WaitAccessibilityService::class.java.name
        return enabled.any { info ->
            val id = info.id.orEmpty()
            val serviceInfo = info.resolveInfo?.serviceInfo
            id == "$pkg/$className" ||
                id == "$pkg/.${className.removePrefix("$pkg.")}" ||
                (serviceInfo?.packageName == pkg && serviceInfo.name == className)
        }
    }

    fun openAccessibilitySettings(context: Context) {
        context.startActivity(
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}

object InstalledAppsHelper {
    fun loadLauncherApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        val ownPackage = context.packageName

        return resolveInfos
            .asSequence()
            .mapNotNull { ri ->
                val packageName = ri.activityInfo?.packageName ?: return@mapNotNull null
                if (packageName == ownPackage) return@mapNotNull null
                val label = ri.loadLabel(pm)?.toString()?.trim().orEmpty()
                    .ifEmpty { packageName }
                val icon = try {
                    ri.loadIcon(pm)
                } catch (_: Exception) {
                    null
                }
                AppInfo(packageName = packageName, label = label, icon = icon)
            }
            .distinctBy { it.packageName }
            .sortedWith(
                compareByDescending<AppInfo> { it.packageName in SUGGESTED_PACKAGE_NAMES }
                    .thenBy { it.label.lowercase() },
            )
            .toList()
    }
}
