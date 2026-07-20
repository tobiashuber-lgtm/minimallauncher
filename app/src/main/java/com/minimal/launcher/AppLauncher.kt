package com.minimal.launcher

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

// Zentrale Stelle zum Starten einer App aus Favoriten/Drawer/Dock. Prueft
// zuerst, ob die App im aktuellen Modus gesperrt ist, danach ob ein
// Zeitlimit-Dialog noetig ist, und startet erst dann wirklich.
object AppLauncher {

    fun open(context: Context, packageName: String) {
        val mode = Prefs.getCurrentMode(context)
        if (mode != "Standard" && Prefs.getModeLockedPackages(context, mode).contains(packageName)) {
            Toast.makeText(context, "Im Modus \"$mode\" gesperrt", Toast.LENGTH_SHORT).show()
            return
        }

        if (Prefs.getTimeLimitedPackages(context).contains(packageName)) {
            showTimeLimitDialog(context, packageName)
        } else {
            launchDirectly(context, packageName)
        }
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
            // Ab Android 14 gibt es eine eigene, modernere API dafuer -
            // manche Geraete-Hersteller ignorieren die alte Methode sonst.
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
                scheduleReturnHome(context, minutesOptions[which])
                launchDirectly(context, packageName)
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    // Statt die fremde App aktiv zu "killen" (technisch ohne Root nicht
    // moeglich), holt ein Alarm nach Ablauf der Zeit einfach den eigenen
    // Home-Screen wieder in den Vordergrund - fuer den Nutzer fuehlt sich
    // das wie ein automatisches Schliessen an.
    private fun scheduleReturnHome(context: Context, minutes: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TimeLimitReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = System.currentTimeMillis() + minutes * 60_000L
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)

        if (android.provider.Settings.canDrawOverlays(context)) {
            val overlayIntent = Intent(context, CountdownOverlayService::class.java)
                .putExtra("minutes", minutes)
            context.startService(overlayIntent)
        }
    }
}
