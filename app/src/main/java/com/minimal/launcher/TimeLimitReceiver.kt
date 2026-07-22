package com.minimal.launcher

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// Feuert per Alarm zum Ende der gewaehlten Zeit. Bringt zuerst den
// Home-Screen in den Vordergrund (damit die Ziel-App in den Hintergrund
// faellt), und versucht sie danach wirklich zu beenden. Ein Launcher kann
// keine fremde App im Vordergrund zwingend killen - killBackgroundProcesses
// funktioniert zuverlaessig nur fuer Apps, die bereits im Hintergrund sind,
// was durch den vorherigen Schritt hier sichergestellt wird.
class TimeLimitReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.getStringExtra("package")

        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(homeIntent)

        if (packageName != null) {
            try {
                val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                am.killBackgroundProcesses(packageName)
            } catch (e: Exception) {
                // Manche Apps/Geraete lassen das nicht zu - bewusst ignoriert,
                // der Nutzer landet in jedem Fall wieder auf dem Home-Screen.
            }
        }

        Prefs.setActiveTimeLimitPackage(context, null)
        context.stopService(Intent(context, CountdownOverlayService::class.java))
    }
}
