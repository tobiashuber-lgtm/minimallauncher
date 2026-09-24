package com.minimal.launcher

import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

// Fuehrt Schritt fuer Schritt durch alle Berechtigungen, die der Launcher
// fuer seine Funktionen braucht. Jeder Punkt oeffnet die richtige Android-
// Einstellungsseite - die eigentliche Bestaetigung (Antippen des Schalters)
// muss aus Sicherheitsgruenden aber immer der Nutzer selbst machen; keine
// App darf das automatisch fuer sich selbst freischalten.
class SetupActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    private fun s(key: String) = Strings.get(this, key)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        container = findViewById(R.id.settingsContainer)
        Prefs.setSeenSetup(this, true)
        build()
    }

    override fun onResume() {
        super.onResume()
        container.removeAllViews()
        build()
    }

    private fun build() {
        val title = TextView(this).apply {
            text = s("setup_title")
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 18f
            typeface = FontFamilies.buildTypeface(this@SetupActivity, "space_mono:bold")
            setPadding(0, dp(8), 0, dp(8))
        }
        container.addView(title)

        val subtitle = TextView(this).apply {
            text = s("setup_subtitle")
            setTextColor(0xFF888888.toInt())
            textSize = 13f
            typeface = android.graphics.Typeface.MONOSPACE
            setPadding(0, 0, 0, dp(24))
        }
        container.addView(subtitle)

        addStep(s("setup_default_launcher"), isDefaultLauncher()) { requestDefaultLauncher() }
        addStep(s("setup_usage_access"), UsageStatsHelper.hasPermission(this)) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
        addStep(s("setup_notification_access"), NotificationAccessHelper.hasPermission(this)) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
        addStep(s("setup_overlay"), Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }

        val done = TextView(this).apply {
            text = s("setup_done")
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 15f
            typeface = android.graphics.Typeface.MONOSPACE
            gravity = Gravity.CENTER
            setPadding(0, dp(32), 0, dp(16))
            setOnClickListener { finish() }
        }
        container.addView(done)
    }

    private fun isDefaultLauncher(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo?.activityInfo?.packageName == packageName
    }

    private fun requestDefaultLauncher() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = getSystemService(RoleManager::class.java)
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
                    !roleManager.isRoleHeld(RoleManager.ROLE_HOME)
                ) {
                    startActivity(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME))
                    return
                }
            }
        } catch (e: Exception) {
            // RoleManager macht auf manchen Geraeten Probleme - Fallback unten
        }
        try {
            startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                this, "Konnte die Einstellungen nicht öffnen", android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun addStep(label: String, done: Boolean, onClick: () -> Unit) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(14), 0, dp(14))
            setOnClickListener { onClick() }
        }
        val tvLabel = TextView(this).apply {
            text = label
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 15f
            typeface = android.graphics.Typeface.MONOSPACE
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val tvStatus = TextView(this).apply {
            text = if (done) "✓" else "→"
            setTextColor(if (done) 0xFFFFFFFF.toInt() else 0xFF666666.toInt())
            textSize = 16f
            typeface = android.graphics.Typeface.MONOSPACE
        }
        row.addView(tvLabel)
        row.addView(tvStatus)
        container.addView(row)

        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1) / 2)
            setBackgroundColor(0xFF2A2A2A.toInt())
        }
        container.addView(divider)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
