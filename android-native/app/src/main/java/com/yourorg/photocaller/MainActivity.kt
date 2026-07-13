package com.yourorg.photocaller

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telecom.TelecomManager
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var permissionText: TextView
    private lateinit var defaultDialerButton: Button
    private lateinit var permissionButton: Button

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            updateState()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        bindActions()
    }

    override fun onResume() {
        super.onResume()
        updateState()
    }

    private fun buildUi() {
        val density = resources.displayMetrics.density
        val padding = (24 * density).toInt()

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(padding, padding, padding, padding)
        }

        val title = TextView(this).apply {
            text = "PhotoCaller"
            textSize = 30f
            gravity = Gravity.CENTER
        }

        statusText = TextView(this).apply {
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, padding, 0, padding / 2)
        }

        permissionText = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, padding)
        }

        defaultDialerButton = Button(this).apply {
            text = "Set as default phone app"
        }

        permissionButton = Button(this).apply {
            text = "Allow required permissions"
        }

        val settingsButton = Button(this).apply {
            text = "Open default app settings"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
            }
        }

        layout.addView(title)
        layout.addView(statusText)
        layout.addView(permissionText)
        layout.addView(defaultDialerButton)
        layout.addView(permissionButton)
        layout.addView(settingsButton)

        setContentView(layout)
    }

    private fun bindActions() {
        defaultDialerButton.setOnClickListener {
            requestDefaultDialer()
        }
        permissionButton.setOnClickListener {
            permissionLauncher.launch(requiredRuntimePermissions())
        }
    }

    private fun updateState() {
        val isDefaultDialer = isDefaultDialer()
        val hasPermissions = requiredRuntimePermissions().all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }

        statusText.text = if (isDefaultDialer) {
            "Ready. PhotoCaller is your default phone app."
        } else {
            "Not active yet. Make PhotoCaller your default phone app."
        }

        permissionText.text = if (hasPermissions) {
            "Permissions are allowed."
        } else {
            "Permissions are needed to read contacts and show caller photos."
        }

        defaultDialerButton.visibility = if (isDefaultDialer) View.GONE else View.VISIBLE
        permissionButton.visibility = if (hasPermissions) View.GONE else View.VISIBLE
    }

    private fun requiredRuntimePermissions(): Array<String> {
        val permissions = mutableListOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.ANSWER_PHONE_CALLS
        )

        return permissions.toTypedArray()
    }

    private fun isDefaultDialer(): Boolean {
        val telecomManager = getSystemService(TelecomManager::class.java)
        return telecomManager?.defaultDialerPackage == packageName
    }

    private fun requestDefaultDialer() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager == null || !roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
                Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            } else {
                roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
            }
        } else {
            Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
            }
        }

        startActivity(intent)
    }
}
