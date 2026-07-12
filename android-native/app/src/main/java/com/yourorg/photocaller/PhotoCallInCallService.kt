package com.yourorg.photocaller

import android.content.Intent
import android.telecom.Call
import android.telecom.InCallService
import android.util.Log

/**
 * Android's Telecom framework binds to this service once this app holds
 * ROLE_DIALER. Every incoming/outgoing/active call is reported here via
 * onCallAdded/onCallRemoved. We don't build UI in this class directly —
 * we just track the current Call object and launch IncomingCallActivity,
 * which reads caller info off this service (see companion object below).
 */
class PhotoCallInCallService : InCallService() {

    companion object {
        private const val TAG = "PhotoCallInCallService"

        // Simple static reference so the Activity can grab the live Call
        // object. Fine for a single-call-at-a-time consumer dialer; if you
        // want call-waiting/multi-call support later, swap this for a
        // small in-process call registry.
        var currentCall: Call? = null
            private set
    }

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            Log.d(TAG, "Call state changed: $state")
            when (state) {
                Call.STATE_DISCONNECTED, Call.STATE_DISCONNECTING -> {
                    currentCall = null
                }
            }
        }
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        Log.d(TAG, "onCallAdded: ${call.details.handle}")
        currentCall = call
        call.registerCallback(callCallback)
        launchCallScreen()
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        Log.d(TAG, "onCallRemoved")
        call.unregisterCallback(callCallback)
        if (currentCall == call) {
            currentCall = null
        }
    }

    private fun launchCallScreen() {
        val intent = Intent(this, IncomingCallActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
        }
        startActivity(intent)
    }
}
