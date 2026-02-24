package com.comalv.lockdowntoggle

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.os.VibratorManager

class QuickLockdownActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(this, LockReceiver::class.java)

        if (dpm.isAdminActive(adminComponent)) {
            dpm.lockNow()
        }
        vibrate(this)
        finishAffinity() // Close all activities
    }
}

private fun vibrate(context: Context) {
    if (Build.VERSION.SDK_INT >= 31) {
        (context.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager)
            .defaultVibrator.action()
    } else {
        (context.getSystemService(VIBRATOR_SERVICE) as Vibrator).action()
    }
}

// (longArrayOf(delay, vibrate, sleep, vibrate, sleep....), repeatStartIndex)
private fun Vibrator.action() = vibrate(longArrayOf(0, 300, 150, 300), -1)
