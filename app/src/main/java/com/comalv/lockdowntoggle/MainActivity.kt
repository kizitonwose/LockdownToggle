package com.comalv.lockdowntoggle

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity



class MainActivity : AppCompatActivity() {
    companion object {
        // Allows test code to override this to inject a mock
        var adminControllerProvider: ((Context) -> AdminController)? = null
    }
    private lateinit var adminController: AdminController
    private lateinit var componentName: ComponentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        componentName = ComponentName(this, LockReceiver::class.java)
        adminController = adminControllerProvider?.invoke(this)
            ?: RealAdminController(this, componentName)

        val requestAdminButton = findViewById<Button>(R.id.btn_request_admin)
        val lockNowButton = findViewById<Button>(R.id.btn_lock_now)
        val removeAdminButton = findViewById<Button>(R.id.btn_remove_admin)
        val requestAdminSection = findViewById<LinearLayout>(R.id.section_request_admin)
        val removeAdminSection = findViewById<LinearLayout>(R.id.section_revoke_admin)

        updateRemoveSectionVisibility()

        requestAdminButton.setOnClickListener {
            requestAdminSection.visibility = View.GONE
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "To allow this app to enter Lockdown mode"
                )
            }
            startActivity(intent)

            // Add quick tile
            val tileServiceIntent =
                Intent("android.service.quicksettings.action.REQUEST_QS_TILE").apply {
                    component = ComponentName(this@MainActivity, LockdownTileService::class.java)
                }
            startService(tileServiceIntent)

            Handler(Looper.getMainLooper()).postDelayed({
                updateRemoveSectionVisibility()
            }, 3000)
        }

        lockNowButton.setOnClickListener {
            if (adminController.isAdminActive()) {
                adminController.lockNow()
            } else {
                Toast.makeText(this, "Admin permission not granted", Toast.LENGTH_SHORT).show()
            }
        }

        removeAdminButton.setOnClickListener {
            if (adminController.isAdminActive()) {
                adminController.removeAdmin()
                removeAdminSection.visibility = View.GONE
                Toast.makeText(this, "Admin rights removed", Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    updateRemoveSectionVisibility()
                }, 3000)
            } else {
                Toast.makeText(this, "No admin rights to remove", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateRemoveSectionVisibility()
    }

    private fun updateRemoveSectionVisibility() {
        val requestAdminSection = findViewById<LinearLayout>(R.id.section_request_admin)
        val removeAdminSection = findViewById<LinearLayout>(R.id.section_revoke_admin)

        if (adminController.isAdminActive()) {
            removeAdminSection.visibility = View.VISIBLE
            requestAdminSection.visibility = View.GONE
        } else {
            removeAdminSection.visibility = View.GONE
            requestAdminSection.visibility = View.VISIBLE
        }
    }
}
