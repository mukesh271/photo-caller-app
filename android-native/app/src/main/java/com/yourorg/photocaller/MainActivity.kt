package com.yourorg.photocaller

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telecom.TelecomManager
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var permissionText: TextView
    private lateinit var nextStepText: TextView
    private lateinit var defaultDialerButton: Button
    private lateinit var permissionButton: Button
    private lateinit var testButton: Button
    private lateinit var testNumberInput: EditText

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            updateState()
        }

    private val roleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
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

        nextStepText = TextView(this).apply {
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

        testButton = Button(this).apply {
            text = "Test call screen"
        }

        testNumberInput = EditText(this).apply {
            hint = "Saved contact number for test"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
        }

        val settingsButton = Button(this).apply {
            text = "Open default app settings"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
            }
        }

        val appInfoButton = Button(this).apply {
            text = "Open PhotoCaller app info"
            setOnClickListener {
                startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                )
            }
        }

        layout.addView(title)
        layout.addView(statusText)
        layout.addView(permissionText)
        layout.addView(nextStepText)
        layout.addView(defaultDialerButton)
        layout.addView(permissionButton)
        layout.addView(testNumberInput)
        layout.addView(testButton)
        layout.addView(settingsButton)
        layout.addView(appInfoButton)

        setContentView(ScrollView(this).apply { addView(layout) })
    }

    private fun bindActions() {
        defaultDialerButton.setOnClickListener {
            requestDefaultDialer()
        }
        permissionButton.setOnClickListener {
            permissionLauncher.launch(requiredRuntimePermissions())
        }
        testButton.setOnClickListener {
            val testNumber = testNumberInput.text?.toString()?.trim().orEmpty()
            startActivity(
                Intent(this, IncomingCallActivity::class.java)
                    .putExtra(IncomingCallActivity.EXTRA_TEST_MODE, true)
                    .putExtra(IncomingCallActivity.EXTRA_TEST_NUMBER, testNumber.ifBlank { "Test Caller" })
            )
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
            "Not active yet. Android will not send calls to PhotoCaller until it is the default phone app."
        }

        permissionText.text = if (hasPermissions) {
            "Permissions are allowed."
        } else {
            "Contacts permission is needed to show saved names and photos."
        }

        nextStepText.text = when {
            !hasPermissions -> "Next: allow contacts permission, then set PhotoCaller as the default phone app."
            !isDefaultDialer -> "Next: tap the default phone app button and choose PhotoCaller."
            else -> "Next: call this phone from another number. The big photo screen should open for incoming calls."
        }

        defaultDialerButton.visibility = if (isDefaultDialer) View.GONE else View.VISIBLE
        permissionButton.visibility = if (hasPermissions) View.GONE else View.VISIBLE
    }

    private fun requiredRuntimePermissions(): Array<String> {
        return arrayOf(Manifest.permission.READ_CONTACTS)
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

        roleLauncher.launch(intent)
    }
}
