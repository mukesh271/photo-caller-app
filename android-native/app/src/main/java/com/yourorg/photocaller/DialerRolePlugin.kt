package com.yourorg.photocaller

import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager

/**
 * Plain Android helper used by the native default-dialer flow. This keeps the
 * module buildable without the Capacitor Android runtime, while still exposing
 * the same default-dialer behavior needed by the app.
 */
class DialerRolePlugin(private val activity: Activity) {

    fun isDefaultDialer(): Boolean {
        val telecomManager = activity.getSystemService(TelecomManager::class.java)
        return telecomManager?.defaultDialerPackage == activity.packageName
    }

    fun requestDefaultDialer() {
        val intent: Intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = activity.getSystemService(RoleManager::class.java)
            roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
        } else {
            Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, activity.packageName)
            }
        }

        activity.startActivity(intent)
    }
}
