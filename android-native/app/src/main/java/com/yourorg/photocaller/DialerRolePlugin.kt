package com.yourorg.photocaller

import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.ActivityCallback
import com.getcapacitor.annotation.CapacitorPlugin

@CapacitorPlugin(name = "DialerRole")
class DialerRolePlugin : Plugin() {

    /** Returns whether this app is currently the default phone/dialer app. */
    @PluginMethod
    fun isDefaultDialer(call: PluginCall) {
        val telecomManager = context.getSystemService(TelecomManager::class.java)
        val isDefault = telecomManager?.defaultDialerPackage == context.packageName
        val result = JSObject()
        result.put("isDefault", isDefault)
        call.resolve(result)
    }

    /**
     * Launches the system prompt asking the user to make this app the
     * default dialer. Result comes back via handleOnActivityResult below.
     */
    @PluginMethod
    fun requestDefaultDialer(call: PluginCall) {
        val intent: Intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
        } else {
            // Pre-Android 10 fallback
            Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, context.packageName)
            }
        }
        saveCall(call)
        startActivityForResult(call, intent, "dialerRoleResult")
    }

    @ActivityCallback
    private fun dialerRoleResult(call: PluginCall?, result: androidx.activity.result.ActivityResult) {
        if (call == null) return
        val telecomManager = context.getSystemService(TelecomManager::class.java)
        val isDefault = telecomManager?.defaultDialerPackage == context.packageName
        val response = JSObject()
        response.put("granted", isDefault)
        call.resolve(response)
    }
}
