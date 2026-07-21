package com.minimal.launcher

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

// Technischer Hinweis: Eine App kann eine andere App nicht "richtig" stumm
// schalten (das entscheidet immer Android/der Nutzer selbst). Was hier
// passiert: sobald eine Benachrichtigung einer stummgeschalteten App im
// aktuellen Modus hereinkommt, wird sie sofort automatisch wieder entfernt.
// Fuer den Nutzer fuehlt sich das wie "aus" an, ist technisch aber
// "sofort entfernt statt verhindert".
class MuteNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val context = applicationContext
        val mode = Prefs.getCurrentMode(context)
        if (mode == "Standard") return

        val muted = Prefs.getModeMutedNotificationPackages(context, mode)
        if (sbn.packageName in muted) {
            cancelNotification(sbn.key)
        }
    }
}
