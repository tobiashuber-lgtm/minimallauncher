package com.minimal.launcher

import android.content.Context
import androidx.core.app.NotificationManagerCompat

object NotificationAccessHelper {
    fun hasPermission(context: Context): Boolean {
        val enabled = NotificationManagerCompat.getEnabledListenerPackages(context)
        return enabled.contains(context.packageName)
    }
}
