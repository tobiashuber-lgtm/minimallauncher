package com.minimal.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.AlarmClock
import android.provider.Settings
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

// Home-Screen. Liest bei jedem Anzeigen (onResume) alle Einstellungen aus
// Prefs neu ein, damit Aenderungen aus dem Settings-Screen sofort greifen.
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var clockRunnable: Runnable
    private lateinit var verticalGestureDetector: GestureDetector
    private lateinit var rootView: View

    private val favoritePackagesFallback = listOf(
        "com.android.camera2", "com.google.android.GoogleCamera",
        "com.google.android.apps.messaging", "com.android.mms",
        "com.google.android.deskclock", "com.android.deskclock",
        "com.android.chrome", "com.google.android.calendar",
        "com.android.dialer", "com.google.android.dialer"
    )

    private val clockAppPackages = listOf("com.google.android.deskclock", "com.android.deskclock")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rootView = view

        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val tvClock = view.findViewById<TextView>(R.id.tvClock)
        tvDate.setOnClickListener { openCalendar() }
        tvClock.setOnClickListener { openClockApp() }

        setupDock(view)
        setupVerticalGestures(view)
        registerBatteryReceiver(view)

        view.findViewById<ImageView>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        view.findViewById<TextView>(R.id.tvUsageHint).setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }

    override fun onResume() {
        super.onResume()
        applyPaletteAndFonts()
        startClock()
        populateFavorites()
        updateUsageHint()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
    }

    private fun applyPaletteAndFonts() {
        val context = requireContext()
        val palette = Palettes.forName(Prefs.getColorScheme(context))
        val typeface = Typeface.create(Prefs.getFontFamily(context), Typeface.NORMAL)

        rootView.setBackgroundColor(palette.background)

        val tvClock = rootView.findViewById<TextView>(R.id.tvClock)
        val tvDate = rootView.findViewById<TextView>(R.id.tvDate)
        val tvBattery = rootView.findViewById<TextView>(R.id.tvBattery)

        tvClock.typeface = typeface
        tvDate.typeface = typeface
        tvBattery.typeface = typeface

        tvClock.setTextColor(palette.accent)
        tvDate.setTextColor(palette.textPrimary)
        tvBattery.setTextColor(palette.textSecondary)

        tvClock.setTextSize(TypedValue.COMPLEX_UNIT_SP, Prefs.getClockSizeSp(context).toFloat())
        tvDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, Prefs.getDateSizeSp(context).toFloat())

        rootView.findViewById<View>(R.id.dockRow).visibility =
            if (Prefs.getDockEnabled(context)) View.VISIBLE else View.GONE

        // Dock-Icons folgen ebenfalls der Textfarbe des gewaehlten Schemas.
        listOf(R.id.dockPhone, R.id.dockCamera, R.id.dockMessage, R.id.dockMail).forEach {
            rootView.findViewById<ImageView>(it).setColorFilter(palette.textPrimary)
        }
        rootView.findViewById<ImageView>(R.id.btnSettings).setColorFilter(palette.textSecondary)
    }

    private fun setupVerticalGestures(root: View) {
        // Platzhalter: freie Zuweisung (z.B. Taschenlampe) folgt in einer spaeteren Etappe.
        verticalGestureDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (e1 == null) return false
                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y
                return abs(diffY) > abs(diffX) && abs(diffY) > 120 && abs(velocityY) > 200
            }
        })
        root.setOnTouchListener { _, event ->
            verticalGestureDetector.onTouchEvent(event)
            false
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
        if (calendarIntent != null) startActivity(calendarIntent)
        else startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://calendar.google.com")))
    }

    private fun openClockApp() {
        val pm = requireContext().packageManager
        for (pkg in clockAppPackages) {
            pm.getLaunchIntentForPackage(pkg)?.let { startActivity(it); return }
        }
        try {
            startActivity(Intent(AlarmClock.ACTION_SHOW_ALARMS))
        } catch (e: Exception) { /* keine Uhr-App gefunden */ }
    }

    private fun startClock() {
        val tvClock = rootView.findViewById<TextView>(R.id.tvClock)
        val tvDate = rootView.findViewById<TextView>(R.id.tvDate)
        val is24h = Prefs.getClockFormat24h(requireContext())
        val pattern = if (is24h) "HH:mm" else "hh:mm a"

        clockRunnable = object : Runnable {
            override fun run() {
                val now = Calendar.getInstance()
                tvClock.text = SimpleDateFormat(pattern, Locale.getDefault()).format(now.time)
                tvDate.text = SimpleDateFormat("EEEE, d. MMMM", Locale.GERMAN).format(now.time)
                handler.postDelayed(this, 30_000)
            }
        }
        handler.post(clockRunnable)
    }

    private fun registerBatteryReceiver(view: View) {
        val batteryFill = view.findViewById<View>(R.id.batteryFill)
        val tvBattery = view.findViewById<TextView>(R.id.tvBattery)
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        requireContext().registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) {
                    val pct = (level * 100) / scale
                    val density = resources.displayMetrics.density
                    val trackWidthPx = (70 * density).toInt()
                    val params = batteryFill.layoutParams
                    params.width = (trackWidthPx * pct) / 100
                    batteryFill.layoutParams = params
                    tvBattery.text = "$pct%"
                }
            }
        }, filter)
    }

    private fun updateUsageHint() {
        val hint = rootView.findViewById<TextView>(R.id.tvUsageHint)
        hint.visibility = if (UsageStatsHelper.hasPermission(requireContext())) View.GONE else View.VISIBLE
    }

    private fun populateFavorites() {
        val context = requireContext()
        val layoutFavorites = rootView.findViewById<LinearLayout>(R.id.layoutFavorites)
        layoutFavorites.removeAllViews()

        val pm = context.packageManager
        val hidden = Prefs.getHiddenPackages(context)
        val maxCount = Prefs.getFavoritesCount(context)
        val chosen = Prefs.getFavoritePackages(context)
        val palette = Palettes.forName(Prefs.getColorScheme(context))
        val typeface = Typeface.create(Prefs.getFontFamily(context), Typeface.NORMAL)
        val usage = UsageStatsHelper.getTodayUsageMinutes(context)

        val finalPackages: List<String> = if (chosen.isNotEmpty()) {
            chosen.filter { it !in hidden && isInstalled(pm, it) }
        } else {
            favoritePackagesFallback.filter { it !in hidden && isInstalled(pm, it) }.distinct()
        }.take(maxCount).ifEmpty {
            getAllLaunchableApps(pm).map { it.second }.filter { it !in hidden }.take(maxCount)
        }

        for (pkg in finalPackages) {
            val label = try {
                pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
            } catch (e: PackageManager.NameNotFoundException) {
                continue
            }
            val displayName = Prefs.displayNameFor(context, pkg, label)
            val row = layoutInflater.inflate(R.layout.item_app, layoutFavorites, false)
            val tvName = row.findViewById<TextView>(R.id.tvAppName)
            val tvUsage = row.findViewById<TextView>(R.id.tvAppUsage)
            tvName.text = displayName
            tvName.typeface = typeface
            tvName.setTextColor(palette.textPrimary)
            tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, Prefs.getAppListSizeSp(context).toFloat())
            tvUsage.typeface = typeface
            tvUsage.setTextColor(palette.textSecondary)
            tvUsage.text = "${usage[pkg] ?: 0} min"
            row.setOnClickListener { pm.getLaunchIntentForPackage(pkg)?.let { startActivity(it) } }
            layoutFavorites.addView(row)
        }
    }

    private fun isInstalled(pm: PackageManager, pkg: String): Boolean = try {
        pm.getApplicationInfo(pkg, 0); true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    private fun getAllLaunchableApps(pm: PackageManager): List<Pair<String, String>> {
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0).map {
            Pair(it.loadLabel(pm).toString(), it.activityInfo.packageName)
        }.distinctBy { it.second }.sortedBy { it.first.lowercase() }
    }
}
