package com.minimal.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.AlarmClock
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

// Home-Screen. Fonts, Farben, Groessen, Favoriten-Auswahl, Dock an/aus
// und frei zuweisbare Swipe-Aktionen kommen erst mit dem Settings-Screen
// in einer spaeteren Etappe - hier erstmal solide Standardwerte.
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var clockRunnable: Runnable
    private lateinit var verticalGestureDetector: GestureDetector

    private val favoritePackages = listOf(
        "com.android.camera2",
        "com.google.android.GoogleCamera",
        "com.google.android.apps.messaging",
        "com.android.mms",
        "com.google.android.deskclock",
        "com.android.deskclock",
        "com.android.chrome",
        "com.google.android.calendar",
        "com.android.dialer",
        "com.google.android.dialer"
    )

    private val clockAppPackages = listOf(
        "com.google.android.deskclock",
        "com.android.deskclock"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvClock = view.findViewById<TextView>(R.id.tvClock)
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val batteryFill = view.findViewById<DottedBatteryView>(R.id.batteryFill)
        val tvBattery = view.findViewById<TextView>(R.id.tvBattery)
        val layoutFavorites = view.findViewById<LinearLayout>(R.id.layoutFavorites)

        tvDate.setOnClickListener { openCalendar() }
        tvClock.setOnClickListener { openClockApp() }

        setupDock(view)
        setupVerticalGestures(view)
        startClock(tvClock, tvDate)
        registerBatteryReceiver(batteryFill, tvBattery)
        populateFavorites(layoutFavorites)
    }

    // Vertikale Wischgesten (oben/unten) sind Platzhalter - freie Zuweisung
    // (z.B. Taschenlampe, Musiksteuerung, ...) folgt im Settings-Screen.
    private fun setupVerticalGestures(root: View) {
        verticalGestureDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false
                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y
                if (abs(diffY) > abs(diffX) && abs(diffY) > 120 && abs(velocityY) > 200) {
                    return true
                }
                return false
            }
        })
        root.setOnTouchListener { _, event ->
            verticalGestureDetector.onTouchEvent(event)
            false // horizontale Wischgesten gehen unveraendert an ViewPager2 weiter
        }
    }

    private fun setupDock(view: View) {
        view.findViewById<ImageView>(R.id.dockPhone).setOnClickListener {
            startActivity(Intent(Intent.ACTION_DIAL))
        }
        view.findViewById<ImageView>(R.id.dockCamera).setOnClickListener {
            startActivity(Intent("android.media.action.IMAGE_CAPTURE"))
        }
        view.findViewById<ImageView>(R.id.dockMessage).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("sms:")))
        }
        view.findViewById<ImageView>(R.id.dockMail).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("mailto:")))
        }
    }

    private fun openCalendar() {
        val pm = requireContext().packageManager
        val calendarIntent = pm.getLaunchIntentForPackage("com.google.android.calendar")
        if (calendarIntent != null) {
            startActivity(calendarIntent)
        } else {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://calendar.google.com")))
        }
    }

    private fun openClockApp() {
        val pm = requireContext().packageManager
        for (pkg in clockAppPackages) {
            val intent = pm.getLaunchIntentForPackage(pkg)
            if (intent != null) {
                startActivity(intent)
                return
            }
        }
        try {
            startActivity(Intent(AlarmClock.ACTION_SHOW_ALARMS))
        } catch (e: Exception) {
            // Keine Uhr-App auf dem Geraet gefunden - bewusst ignoriert
        }
    }

    private fun startClock(tvClock: TextView, tvDate: TextView) {
        clockRunnable = object : Runnable {
            override fun run() {
                val now = Calendar.getInstance()
                tvClock.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now.time)
                // Sprache/Format wird spaeter aus den Einstellungen gelesen.
                tvDate.text = SimpleDateFormat("EEEE, d. MMMM", Locale.GERMAN).format(now.time)
                handler.postDelayed(this, 30_000)
            }
        }
        handler.post(clockRunnable)
    }

    // Prozentwert wird direkt an die DottedBatteryView weitergereicht,
    // die das Punktraster selbst zeichnet.
    private fun registerBatteryReceiver(batteryFill: DottedBatteryView, tvBattery: TextView) {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        requireContext().registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) {
                    val pct = (level * 100) / scale
                    batteryFill.progressPercent = pct
                    tvBattery.text = "$pct%"
                }
            }
        }, filter)
    }

    private fun populateFavorites(layoutFavorites: LinearLayout) {
        layoutFavorites.removeAllViews()
        val pm = requireContext().packageManager
        val installedFavorites = favoritePackages.mapNotNull { pkg ->
            try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                val label = pm.getApplicationLabel(appInfo).toString()
                Pair(label, pkg)
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
        }.distinctBy { it.first }.take(6)

        val finalFavorites = if (installedFavorites.size >= 3) {
            installedFavorites
        } else {
            getAllLaunchableApps(pm).take(6)
        }

        for ((label, pkg) in finalFavorites) {
            val row = layoutInflater.inflate(R.layout.item_app, layoutFavorites, false)
            row.findViewById<TextView>(R.id.tvAppName).text = label
            // Echtes Nutzungszeit-Tracking folgt in einer spaeteren Etappe.
            row.findViewById<TextView>(R.id.tvAppUsage).text = "0 min"
            row.setOnClickListener {
                pm.getLaunchIntentForPackage(pkg)?.let { startActivity(it) }
            }
            layoutFavorites.addView(row)
        }
    }

    private fun getAllLaunchableApps(pm: PackageManager): List<Pair<String, String>> {
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        return resolveInfos.map {
            Pair(it.loadLabel(pm).toString(), it.activityInfo.packageName)
        }.distinctBy { it.second }.sortedBy { it.first.lowercase() }
    }
}
