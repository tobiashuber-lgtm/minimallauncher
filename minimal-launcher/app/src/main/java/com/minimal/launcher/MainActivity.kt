package com.minimal.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

// Etappe 1: Grundgerüst. Fonts, Farben, Groessen, Favoriten-Auswahl etc.
// kommen in einer spaeteren Etappe ueber einen Settings-Screen dazu.
class MainActivity : AppCompatActivity() {

    private lateinit var tvClock: TextView
    private lateinit var tvDate: TextView
    private lateinit var pbBattery: ProgressBar
    private lateinit var tvBattery: TextView
    private lateinit var layoutFavorites: LinearLayout

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var clockRunnable: Runnable
    private lateinit var gestureDetector: GestureDetector

    // Vorlaeufige, fest hinterlegte Favoriten-Kandidaten (Etappe 2: frei waehlbar).
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvClock = findViewById(R.id.tvClock)
        tvDate = findViewById(R.id.tvDate)
        pbBattery = findViewById(R.id.pbBattery)
        tvBattery = findViewById(R.id.tvBattery)
        layoutFavorites = findViewById(R.id.layoutFavorites)

        tvDate.setOnClickListener { openCalendar() }

        setupGestures()
        startClock()
        registerBatteryReceiver()
        populateFavorites()
    }

    private fun setupGestures() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false
                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y
                if (abs(diffX) > abs(diffY) && abs(diffX) > 120 && abs(velocityX) > 200) {
                    if (diffX < 0) {
                        openAppDrawer()
                    } else {
                        openNotesPlaceholder()
                    }
                    return true
                }
                return false
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    private fun openAppDrawer() {
        startActivity(Intent(this, AppDrawerActivity::class.java))
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }

    // Platzhalter: in einer spaeteren Etappe hier zwischen "interne Mini-Notizen"
    // und "beliebige App oeffnen" umschaltbar (Einstellungen).
    private fun openNotesPlaceholder() {
        Toast.makeText(this, "Notizen-Aktion folgt in einer spaeteren Version", Toast.LENGTH_SHORT).show()
    }

    private fun openCalendar() {
        val pm = packageManager
        val calendarIntent = pm.getLaunchIntentForPackage("com.google.android.calendar")
        if (calendarIntent != null) {
            startActivity(calendarIntent)
        } else {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://calendar.google.com"))
            startActivity(webIntent)
        }
    }

    private fun startClock() {
        clockRunnable = object : Runnable {
            override fun run() {
                val now = Calendar.getInstance()
                tvClock.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now.time)
                // Sprache/Format wird in Etappe 2 aus den Einstellungen gelesen.
                tvDate.text = SimpleDateFormat("EEEE, d. MMMM", Locale.GERMAN).format(now.time)
                handler.postDelayed(this, 30_000)
            }
        }
        handler.post(clockRunnable)
    }

    private fun registerBatteryReceiver() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) {
                    val pct = (level * 100) / scale
                    pbBattery.progress = pct
                    tvBattery.text = "$pct%"
                }
            }
        }, filter)
    }

    private fun populateFavorites() {
        layoutFavorites.removeAllViews()
        val pm = packageManager
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
            getAllLaunchableApps().take(6)
        }

        for ((label, pkg) in finalFavorites) {
            val row = layoutInflater.inflate(R.layout.item_app, layoutFavorites, false)
            val tvName = row.findViewById<TextView>(R.id.tvAppName)
            val tvUsage = row.findViewById<TextView>(R.id.tvAppUsage)
            tvName.text = label
            // Echtes Nutzungszeit-Tracking folgt in Etappe 3.
            tvUsage.text = "0 min"
            row.setOnClickListener {
                pm.getLaunchIntentForPackage(pkg)?.let { startActivity(it) }
            }
            layoutFavorites.addView(row)
        }
    }

    private fun getAllLaunchableApps(): List<Pair<String, String>> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        return resolveInfos.map {
            Pair(it.loadLabel(pm).toString(), it.activityInfo.packageName)
        }.distinctBy { it.second }.sortedBy { it.first.lowercase() }
    }
}
