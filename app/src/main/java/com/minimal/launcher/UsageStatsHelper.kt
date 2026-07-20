package com.minimal.launcher

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import java.util.Calendar

// Liest die heutige Nutzungszeit pro App ueber die Android-eigene
// Nutzungsstatistik aus. Erfordert einmalig eine manuelle Freigabe durch
// den Nutzer (Einstellungen -> Apps mit Nutzungszugriff), da Android das
// aus Datenschutzgruenden nicht automatisch erlaubt.
object UsageStatsHelper {

    fun hasPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    // Minuten pro Package seit Mitternacht heute.
    fun getTodayUsageMinutes(context: Context): Map<String, Int> {
        if (!hasPermission(context)) return emptyMap()

        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val now = System.currentTimeMillis()

        val statsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startOfDay,
            now
        ) ?: return emptyMap()

        val result = mutableMapOf<String, Int>()
        for (stats in statsList) {
            val minutes = (stats.totalTimeInForeground / 1000 / 60).toInt()
            if (minutes > 0) {
                result[stats.packageName] = (result[stats.packageName] ?: 0) + minutes
            }
        }
        return result
    }
}
