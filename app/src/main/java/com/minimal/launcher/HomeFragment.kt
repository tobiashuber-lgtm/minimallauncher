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
import android.provider.AlarmClock
import android.provider.Settings
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Home-Screen. Liest bei jedem Anzeigen (onResume) alle Einstellungen aus
// Prefs neu ein, damit Aenderungen aus dem Settings-Screen sofort greifen.
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var clockRunnable: Runnable
    private lateinit var rootView: View

    private val favoritePackagesFallback = listOf(
        "com.android.camera2", "com.google.android.GoogleCamera",
        "com.google.android.apps.messaging", "com.android.mms",
        "com.google.android.deskclock", "com.android.deskclock",
        "com.android.chrome", "com.google.android.calendar",
        "com.android.dialer", "com.google.android.dialer"
    )

    private val clockAppPackages = listOf(
        "com.google.android.deskclock",
        "com.android.deskclock",
        "com.sec.android.app.clockpackage", // Samsung
        "com.miui.clock", // Xiaomi/MIUI
        "com.oneplus.deskclock", // OnePlus
        "com.huawei.deskclock" // Huawei
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rootView = view

        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val tvClock = view.findViewById<TextView>(R.id.tvClock)
        tvDate.setOnClickListener { openCalendar() }
        tvDate.setOnLongClickListener { openNewCalendarEvent(); true }
        tvClock.setOnClickListener { openClockApp() }

        registerBatteryReceiver(view)

        view.findViewById<ImageView>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        view.findViewById<TextView>(R.id.tvModeIndicator).setOnClickListener {
            showModeSwitcher()
        }

        view.findViewById<TextView>(R.id.tvUsageHint).setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        view.findViewById<View>(R.id.weatherRow).setOnClickListener { openWeatherDetails() }
    }

    // Googles eigene Wetter-Uebersicht ist ideal hierfuer: sehr uebersichtlich,
    // schnell, keine Anmeldung noetig - egal ob per Ortsname oder Koordinaten.
    private fun openWeatherDetails() {
        val context = requireContext()
        val customLink = Prefs.getWeatherCustomLink(context)
        if (customLink.isNotBlank()) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(customLink)))
                return
            } catch (e: Exception) {
                // ungueltiger Link - Fallback auf Standard-Suche unten
            }
        }
        val query = if (Prefs.getWeatherUseGps(context)) {
            "weather ${Prefs.getWeatherLat(context)},${Prefs.getWeatherLon(context)}"
        } else {
            val name = Prefs.getWeatherLocationName(context)
            if (name.isBlank()) return
            "weather $name"
        }
        val encoded = java.net.URLEncoder.encode(query, "UTF-8")
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$encoded")))
    }

    override fun onResume() {
        super.onResume()
        applyPaletteAndFonts()
        startClock()
        populateFavorites()
        updateUsageHint()
        updateModeIndicator()
        updateWeather()
    }

    private fun showModeSwitcher() {
        val context = requireContext()
        val modeValues = listOf("Standard") + Prefs.getModeNames(context)
        val modeLabels = modeValues.map { if (it == "Standard") Prefs.getStandardModeDisplayName(context) else it }
        val current = Prefs.getCurrentMode(context)
        val currentIndex = modeValues.indexOf(current).coerceAtLeast(0)

        androidx.appcompat.app.AlertDialog.Builder(context, R.style.Theme_MinimalLauncher_Dialog)
            .setTitle("Modus wechseln")
            .setSingleChoiceItems(modeLabels.toTypedArray(), currentIndex) { dialog, which ->
                Prefs.setCurrentMode(context, modeValues[which])
                dialog.dismiss()
                applyPaletteAndFonts()
                populateFavorites()
                updateModeIndicator()
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun updateModeIndicator() {
        val context = requireContext()
        val mode = Prefs.getCurrentMode(context)
        val tvMode = rootView.findViewById<TextView>(R.id.tvModeIndicator)
        tvMode.text = if (mode == "Standard") Prefs.getStandardModeDisplayName(context) else mode
        tvMode.visibility = View.VISIBLE
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
    }

    private fun applyPaletteAndFonts() {
        val context = requireContext()
        val palette = Palettes.current(context)
        val mainTypeface = FontFamilies.buildTypeface(context, Prefs.getFontFamily(context))
        val clockTypeface = FontFamilies.buildTypeface(context, Prefs.getClockFontFamily(context))

        rootView.setBackgroundColor(palette.background)

        val tvClock = rootView.findViewById<TextView>(R.id.tvClock)
        val tvDate = rootView.findViewById<TextView>(R.id.tvDate)
        val tvBattery = rootView.findViewById<TextView>(R.id.tvBattery)

        tvClock.typeface = clockTypeface
        tvDate.typeface = mainTypeface
        tvBattery.typeface = mainTypeface

        tvClock.setTextColor(effectiveAccent(context, palette))
        tvDate.setTextColor(palette.textPrimary)
        tvBattery.setTextColor(palette.textSecondary)

        tvClock.setTextSize(TypedValue.COMPLEX_UNIT_SP, Prefs.getClockSizeSp(context).toFloat())
        tvDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, Prefs.getDateSizeSp(context).toFloat())

        val mode = Prefs.getCurrentMode(context)
        val dockOverride = if (mode != "Standard") Prefs.getModeDockEnabled(context, mode) else null
        val dockEnabled = dockOverride ?: Prefs.getDockEnabled(context)
        rootView.findViewById<View>(R.id.dockRow).visibility = if (dockEnabled) View.VISIBLE else View.GONE

        updateDock(rootView)
        rootView.findViewById<TextView>(R.id.tvModeIndicator).apply {
            typeface = mainTypeface
            setTextColor(palette.textSecondary)
        }

        val weatherSize = Prefs.getWeatherSizeSp(context).toFloat()
        rootView.findViewById<TextView>(R.id.tvWeather).apply {
            typeface = mainTypeface
            setTextColor(palette.textSecondary)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, weatherSize)
        }
        rootView.findViewById<ImageView>(R.id.weatherIcon).apply {
            setColorFilter(palette.textSecondary)
            val sizePx = (weatherSize * resources.displayMetrics.density * 1.15f).toInt()
            layoutParams = layoutParams.apply { width = sizePx; height = sizePx }
        }
    }

    private fun effectiveAccent(context: Context, palette: Palette): Int {
        val mode = Prefs.getCurrentMode(context)
        if (mode == "Standard") return palette.accent
        val override = Prefs.getModeAccentColorSlider(context, mode) ?: return palette.accent
        return ColorUtils.colorForSliderValue(override)
    }

    private fun updateDock(view: View) {
        val context = requireContext()
        val pm = context.packageManager
        val palette = Palettes.current(context)

        val slots = listOf(
            Triple(R.id.dockPhone, Intent(Intent.ACTION_DIAL), R.drawable.ic_dock_phone),
            Triple(R.id.dockCamera, Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA), R.drawable.ic_dock_camera),
            Triple(R.id.dockMessage, Intent(Intent.ACTION_VIEW, Uri.parse("sms:")), R.drawable.ic_dock_message),
            Triple(R.id.dockMail, Intent(Intent.ACTION_VIEW, Uri.parse("mailto:")), R.drawable.ic_dock_mail)
        )

        slots.forEachIndexed { index, (viewId, fallbackIntent, fallbackIconRes) ->
            val imageView = view.findViewById<ImageView>(viewId)
            val assignedPkg = Prefs.getDockPackage(context, index)

            when {
                assignedPkg == "__EMPTY__" -> {
                    imageView.visibility = View.INVISIBLE
                    imageView.setOnClickListener(null)
                }
                assignedPkg != null -> {
                    imageView.visibility = View.VISIBLE
                    val iconKey = Prefs.getDockIcon(context, index)
                    if (iconKey != null) {
                        imageView.setImageResource(DockIcons.drawableFor(iconKey))
                        imageView.setColorFilter(palette.textPrimary)
                        imageView.setOnClickListener { AppLauncher.open(context, assignedPkg) }
                    } else {
                        try {
                            imageView.setImageDrawable(pm.getApplicationIcon(assignedPkg))
                            imageView.clearColorFilter() // echtes App-Icon nicht eintoenen
                            imageView.setOnClickListener { AppLauncher.open(context, assignedPkg) }
                        } catch (e: PackageManager.NameNotFoundException) {
                            imageView.setImageResource(fallbackIconRes)
                            imageView.setColorFilter(palette.textPrimary)
                            imageView.setOnClickListener { startActivity(fallbackIntent) }
                        }
                    }
                }
                else -> {
                    imageView.visibility = View.VISIBLE
                    imageView.setImageResource(fallbackIconRes)
                    imageView.setColorFilter(palette.textPrimary)
                    imageView.setOnClickListener { startActivity(fallbackIntent) }
                }
            }
        }
    }

    private fun updateWeather() {
        val context = requireContext()
        val row = rootView.findViewById<View>(R.id.weatherRow)

        if (!Prefs.getWeatherEnabled(context)) {
            row.visibility = View.GONE
            return
        }
        row.visibility = View.VISIBLE

        val (cachedTemp, cachedCode, cachedTime) = Prefs.getWeatherCache(context)
        if (cachedCode != -1) {
            showWeather(cachedTemp, cachedCode)
        }

        val useGps = Prefs.getWeatherUseGps(context)
        val isStale = System.currentTimeMillis() - cachedTime > 5 * 60 * 1000L
        if (!isStale && !useGps) return // frisch genug - bei GPS trotzdem pruefen, da sich der Ort geaendert haben koennte

        if (useGps) {
            fetchGpsLocationAndWeather(context)
        } else {
            val lat = Prefs.getWeatherLat(context)
            val lon = Prefs.getWeatherLon(context)
            if (lat != 0f || lon != 0f) {
                WeatherHelper.fetchWeather(lat, lon) { result ->
                    if (result != null && isAdded) {
                        Prefs.setWeatherCache(context, result.temperatureC, result.weatherCode)
                        showWeather(result.temperatureC, result.weatherCode)
                    }
                }
            }
        }
    }

    private fun fetchGpsLocationAndWeather(context: Context) {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
            var bestLocation: android.location.Location? = null
            for (provider in locationManager.getProviders(true)) {
                val loc = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || loc.accuracy < bestLocation!!.accuracy) bestLocation = loc
            }
            bestLocation?.let { location ->
                WeatherHelper.fetchWeather(location.latitude.toFloat(), location.longitude.toFloat()) { result ->
                    if (result != null && isAdded) {
                        Prefs.setWeatherCache(context, result.temperatureC, result.weatherCode)
                        showWeather(result.temperatureC, result.weatherCode)
                    }
                }
            }
        } catch (e: SecurityException) {
            // Berechtigung doch nicht (mehr) vorhanden - bewusst ignoriert
        }
    }

    private fun showWeather(tempC: Int, code: Int) {
        rootView.findViewById<TextView>(R.id.tvWeather).text = "$tempC°"
        rootView.findViewById<ImageView>(R.id.weatherIcon).setImageResource(WeatherIcons.drawableFor(code))
    }

    private fun openCalendar() {
        val pm = requireContext().packageManager
        val calendarIntent = pm.getLaunchIntentForPackage("com.google.android.calendar")
        if (calendarIntent != null) startActivity(calendarIntent)
        else startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://calendar.google.com")))
    }

    // Nutzt die offizielle CalendarContract-Schnittstelle - jede Kalender-App
    // (inkl. Google Kalender) oeffnet damit direkt die "Neuer Termin"-Eingabe,
    // ohne erst durch die Kalenderansicht navigieren zu muessen.
    private fun openNewCalendarEvent() {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = android.provider.CalendarContract.Events.CONTENT_URI
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            openCalendar()
        }
    }

    private fun openClockApp() {
        // Erst der universelle Weg (funktioniert bei praktisch jeder Uhr-App,
        // unabhaengig vom Hersteller/Paketnamen).
        try {
            startActivity(Intent(AlarmClock.ACTION_SHOW_ALARMS))
            return
        } catch (e: Exception) {
            // Kein Handler dafuer - weiter zu bekannten Paketnamen als Rueckfall
        }
        val pm = requireContext().packageManager
        for (pkg in clockAppPackages) {
            pm.getLaunchIntentForPackage(pkg)?.let { startActivity(it); return }
        }
    }

    private fun startClock() {
        val tvClock = rootView.findViewById<TextView>(R.id.tvClock)
        val tvDate = rootView.findViewById<TextView>(R.id.tvDate)
        val is24h = Prefs.getClockFormat24h(requireContext())
        val pattern = if (is24h) "HH:mm" else "hh:mm a"
        val language = Prefs.getAppLanguage(requireContext())
        val dateLocale = if (language == "en") Locale.ENGLISH else Locale.GERMAN
        val datePattern = if (language == "en") "EEEE, MMMM d" else "EEEE, d. MMMM"

        clockRunnable = object : Runnable {
            override fun run() {
                val now = Calendar.getInstance()
                tvClock.text = SimpleDateFormat(pattern, Locale.getDefault()).format(now.time)
                tvDate.text = SimpleDateFormat(datePattern, dateLocale).format(now.time)
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
        val currentMode = Prefs.getCurrentMode(context)
        val modeLocked = if (currentMode != "Standard") Prefs.getModeLockedPackages(context, currentMode) else emptySet()
        val hidden = Prefs.getHiddenPackages(context) + modeLocked
        val maxCount = Prefs.getFavoritesCount(context)
        val modeFavorites = if (currentMode != "Standard") Prefs.getFavoritePackages(context, currentMode) else emptyList()
        val chosen = modeFavorites.ifEmpty { Prefs.getFavoritePackages(context) }
        val palette = Palettes.current(context)
        val typeface = FontFamilies.buildTypeface(context, Prefs.getFontFamily(context))
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
            row.setOnClickListener { AppLauncher.open(context, pkg) }
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
