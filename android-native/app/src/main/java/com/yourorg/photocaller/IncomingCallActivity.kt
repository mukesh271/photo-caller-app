package com.yourorg.photocaller

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.PhoneAccount
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Full-screen UI shown for an incoming or active call. Reads the live
 * Call object from PhotoCallInCallService.currentCall, resolves the
 * caller's name + photo from the Contacts provider by phone number, and
 * shows a big photo-first layout — designed for a user who identifies
 * people by face, not by reading a name.
 */
class IncomingCallActivity : AppCompatActivity() {

    private var call: Call? = null

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(c: Call, state: Int) {
            super.onStateChanged(c, state)
            when (state) {
                Call.STATE_DISCONNECTED -> finish()
            }
            updateButtonsForState(state)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_call)

        call = PhotoCallInCallService.currentCall
        call?.registerCallback(callCallback)

        bindCallerInfo()
        bindControls()
    }

    private fun bindCallerInfo() {
        val handle = call?.details?.handle // tel:+91XXXXXXXXXX
        val phoneNumber = handle?.schemeSpecificPart ?: ""

        val nameView = findViewById<TextView>(R.id.callerName)
        val photoView = findViewById<ImageView>(R.id.callerPhoto)

        val contact = lookupContactByNumber(phoneNumber)
        if (contact != null) {
            nameView.text = contact.name
            if (contact.photoUri != null) {
                photoView.setImageURI(Uri.parse(contact.photoUri))
            } else {
                photoView.setImageResource(R.drawable.default_avatar)
            }
        } else {
            // Unknown number — no contact match found
            nameView.text = phoneNumber.ifBlank { "Unknown" }
            photoView.setImageResource(R.drawable.default_avatar)
        }
    }

    private data class ContactInfo(val name: String, val photoUri: String?)

    /**
     * Matches by normalized phone number rather than raw string equality —
     * raw numbers from Call.Details often differ in formatting
     * (+91 vs 0 vs spaces) from what's stored in the Contacts provider.
     */
    private fun lookupContactByNumber(phoneNumber: String): ContactInfo? {
        if (phoneNumber.isBlank()) return null

        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val projection = arrayOf(
            ContactsContract.PhoneLookup._ID,
            ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup.PHOTO_URI
        )

        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(uri, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val name = cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)
                )
                val photoUri = cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.PHOTO_URI)
                )
                return ContactInfo(name, photoUri)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun bindControls() {
        findViewById<ImageView>(R.id.answerButton).setOnClickListener {
            call?.answer(0)
        }
        findViewById<ImageView>(R.id.declineButton).setOnClickListener {
            call?.reject(false, null)
            finish()
        }
        updateButtonsForState(call?.state ?: Call.STATE_NEW)
    }

    private fun updateButtonsForState(state: Int) {
        val answerBtn = findViewById<ImageView>(R.id.answerButton)
        // Only show "answer" while actually ringing; once active, only
        // decline/hang-up makes sense.
        answerBtn.visibility =
            if (state == Call.STATE_RINGING) android.view.View.VISIBLE else android.view.View.GONE
    }

    override fun onDestroy() {
        call?.unregisterCallback(callCallback)
        super.onDestroy()
    }
}
