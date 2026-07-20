package com.minimal.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TimeLimitReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(homeIntent)
    }
}
