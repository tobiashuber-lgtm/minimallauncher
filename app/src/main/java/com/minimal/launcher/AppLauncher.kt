package com.minimal.launcher

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

// Zentrale Stelle zum Starten einer App. Prueft Modus-Sperre, dann ob ein
// Zeitlimit greift - und falls ja, ob gerade schon eine Session fuer genau
// diese App laeuft (dann einfach fortsetzen, kein neuer Dialog) oder eine
// andere App eine Session offen hatte (dann wird die verworfen, keine
// ueberlappenden Timer).
object AppLauncher {

    fun open(context: Context, packageName: String) {
        val mode = Prefs.getCurrentMode(context)
        if (mode != "Standard" && Prefs.getModeLockedPackages(context, mode).contains(packageName)) {
            Toast.makeText(context, "Im Modus \"$mode\" gesperrt", Toast.LENGTH_SHORT).show()
            return
        }

        val activePkg = Prefs.getActiveTimeLimitPackage(context)
        val activeEndAt = Prefs.getActiveTimeLimitEndAt(context)
        val now = System.currentTimeMillis()

        if (activePkg != null && activePkg != packageName) {
            // Eine andere App wurde gestartet, waehrend fuer eine vorherige
            // noch ein Zeitlimit lief - das alte Limit wird verworfen.
            cancelActiveSession(context)
        }

        if (activePkg == packageName && now < activeEndAt) {
            // Innerhalb des urspruenglichen Zeitfensters erneut geoeffnet:
            // Timer laeuft einfach weiter, kein neuer Dialog noetig.
            resumeOverlay(context, activeEndAt)
            launchDirectly(context, packageName)
            return
        }

        if (Prefs.getTimeLimitedPackages(context).contains(packageName)) {
            showTimeLimitDialog(context, packageName)
        } else {
            launchDirectly(context, packageName)
        }
    }

    // Wird von MainActivity aufgerufen, sobald der Launcher wieder im
    // Vordergrund ist - das Overlay verschwindet, der Timer im Hintergrund
    // (Alarm) laeuft aber unveraendert bis zum urspruenglichen Ende weiter.
    fun hideOverlayOnly(context: Context) {
        context.stopService(Intent(context, CountdownOverlayService::class.java))
    }

    private fun launchDirectly(context: Context, packageName: String) {
        context.packageManager.getLaunchIntentForPackage(packageName)?.let {
            context.startActivity(it)
            applyOpenTransition(context)
        }
    }

    private fun applyOpenTransition(context: Context) {
        if (context !is Activity) return
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            context.overrideActivityTransition(
                Activity.OVERRIDE_TRANSITION_OPEN,
                R.anim.app_open_enter,
                R.anim.app_open_exit
            )
        } else {
            @Suppress("DEPRECATION")
            context.overridePendingTransition(R.anim.app_open_enter, R.anim.app_open_exit)
        }
    }

    private fun showTimeLimitDialog(context: Context, packageName: String) {
        val minutesOptions = listOf(5, 10, 15)
        AlertDialog.Builder(context, R.style.Theme_MinimalLauncher_Dialog)
            .setTitle("Wirklich öffnen?")
            .setItems(minutesOptions.map { "$it Minuten" }.toTypedArray()) { _, which ->
                startNewSession(context, packageName, minutesOptions[which])
                launchDirectly(context, packageName)
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun startNewSession(context: Context, packageName: String, minutes: Int) {
        val endAt = System.currentTimeMillis() + minutes * 60_000L
        Prefs.setActiveTimeLimitPackage(context, packageName)
        Prefs.setActiveTimeLimitEndAt(context, endAt)
        scheduleForceCloseAlarm(context, packageName, endAt)
        resumeOverlay(context, endAt)
    }

    private fun scheduleForceCloseAlarm(context: Context, packageName: String, endAt: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TimeLimitReceiver::class.java).putExtra("package", packageName)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.set(AlarmManager.RTC_WAKEUP, endAt, pendingIntent)
    }

    private fun resumeOverlay(context: Context, endAt: Long) {
        if (!Settings.canDrawOverlays(context)) return
        val remainingSeconds = ((endAt - System.currentTimeMillis()) / 1000).toInt().coerceAtLeast(0)
        if (remainingSeconds <= 0) return
        val overlayIntent = Intent(context, CountdownOverlayService::class.java)
            .putExtra("seconds", remainingSeconds)
        context.startService(overlayIntent)
    }

    private fun cancelActiveSession(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TimeLimitReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Prefs.setActiveTimeLimitPackage(context, null)
        hideOverlayOnly(context)
    }
}
