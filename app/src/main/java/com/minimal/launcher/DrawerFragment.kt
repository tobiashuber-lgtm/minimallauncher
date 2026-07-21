package com.minimal.launcher

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// App-Drawer. Ausgeblendete Apps tauchen hier gar nicht erst auf,
// Umbenennungen und Nutzungszeit werden bei jedem Anzeigen neu geladen.
data class AppEntry(
    val label: String,
    val packageName: String,
    val usageMinutes: Int = 0,
    val isHeader: Boolean = false
)

class DrawerFragment : Fragment(R.layout.fragment_drawer) {

    // A-Z plus '#' fuer Apps, die nicht mit einem Buchstaben beginnen
    // (Zahlen, Symbole - z.B. "1Password", "Ö1").
    private val letters = ('A'..'Z').toList() + '#'

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppDrawerAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private var allApps: List<AppEntry> = emptyList()
    private lateinit var rootView: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rootView = view

        recyclerView = view.findViewById(R.id.rvApps)
        layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        val style = currentStyle()
        adapter = AppDrawerAdapter(mutableListOf(), style) { pkg ->
            AppLauncher.open(requireContext(), pkg)
        }
        recyclerView.adapter = adapter

        val etSearch = view.findViewById<EditText>(R.id.etSearch)
        etSearch.typeface = style.typeface
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.lowercase().orEmpty()
                val filtered = allApps.filter { it.label.lowercase().contains(query) }
                adapter.updateData(insertHeaders(filtered))
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        setupLetterIndex(view)
    }

    override fun onResume() {
        super.onResume()
        applyPalette()
        allApps = loadApps()
        adapter.updateData(insertHeaders(allApps))
        adapter.updateStyle(currentStyle())
        rebuildLetterIndex()
    }

    private fun currentStyle(): DrawerStyle {
        val context = requireContext()
        val palette = Palettes.current(context)
        val typeface = FontFamilies.buildTypeface(context, Prefs.getFontFamily(context))
        return DrawerStyle(
            typeface = typeface,
            textColor = palette.textPrimary,
            secondaryColor = palette.textSecondary,
            sizeSp = Prefs.getAppListSizeSp(context).toFloat()
        )
    }

    private fun applyPalette() {
        val context = requireContext()
        val palette = Palettes.current(context)
        rootView.setBackgroundColor(palette.background)
        rootView.findViewById<EditText>(R.id.etSearch).setTextColor(palette.textPrimary)
        rootView.findViewById<TextView>(R.id.tvLetterPreview).apply {
            setTextColor(palette.textPrimary)
            typeface = FontFamilies.buildTypeface(context, "space_mono:bold")
        }
    }

    private fun loadApps(): List<AppEntry> {
        val context = requireContext()
        val pm = context.packageManager
        val currentMode = Prefs.getCurrentMode(context)
        val modeLocked = if (currentMode != "Standard") Prefs.getModeLockedPackages(context, currentMode) else emptySet()
        val hidden = Prefs.getHiddenPackages(context) + modeLocked
        val usage = UsageStatsHelper.getTodayUsageMinutes(context)

        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0)
            .map { it.activityInfo.packageName to it.loadLabel(pm).toString() }
            .distinctBy { it.first }
            .filter { it.first !in hidden }
            .map { (pkg, label) ->
                AppEntry(
                    label = Prefs.displayNameFor(context, pkg, label),
                    packageName = pkg,
                    usageMinutes = usage[pkg] ?: 0
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    // Fuegt vor jeder neuen Anfangsbuchstaben-Gruppe eine kleine graue
    // Ueberschriften-Zeile ein (wie im Kontakte-/Telefon-Buch).
    private fun insertHeaders(list: List<AppEntry>): List<AppEntry> {
        val result = mutableListOf<AppEntry>()
        var lastLetter: Char? = null
        for (entry in list) {
            val letter = letterFor(entry)
            if (letter != lastLetter) {
                result.add(AppEntry(label = letter.toString(), packageName = "", isHeader = true))
                lastLetter = letter
            }
            result.add(entry)
        }
        return result
    }

    private fun letterFor(entry: AppEntry): Char {
        val c = entry.label.firstOrNull()?.uppercaseChar() ?: '#'
        return if (c in 'A'..'Z') c else '#'
    }

    private fun rebuildLetterIndex() {
        val lettersContainer = rootView.findViewById<LinearLayout>(R.id.lettersContainer)
        lettersContainer.removeAllViews()
        setupLetterIndex(rootView)
    }

    private fun setupLetterIndex(root: View) {
        val lettersContainer = root.findViewById<LinearLayout>(R.id.lettersContainer)
        if (lettersContainer.childCount > 0) return
        val context = requireContext()
        val palette = Palettes.current(context)

        letters.forEach { letter ->
            val tv = TextView(context).apply {
                text = letter.toString()
                textSize = 9f
                setTextColor(palette.textSecondary)
                gravity = Gravity.CENTER
            }
            lettersContainer.addView(
                tv,
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
            )
        }

        var lastLetter: Char? = null

        lettersContainer.setOnTouchListener { v, event ->
            val tvPreview = rootView.findViewById<TextView>(R.id.tvLetterPreview)
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    val itemHeight = v.height / letters.size.toFloat()
                    val index = (event.y / itemHeight).toInt().coerceIn(0, letters.size - 1)
                    val letter = letters[index]

                    if (letter != lastLetter) {
                        vibrateTick(context)
                        lastLetter = letter

                        tvPreview.text = letter.toString()
                        tvPreview.visibility = View.VISIBLE

                        // Waehrend des Ziehens: nur Apps mit diesem Anfangsbuchstaben
                        // zeigen, damit sofort klar ist, wo man "landet".
                        val filtered = allApps.filter { letterFor(it) == letter }
                        adapter.updateData(filtered)
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    tvPreview.visibility = View.GONE
                    val query = rootView.findViewById<EditText>(R.id.etSearch).text?.toString()?.lowercase().orEmpty()
                    val restored = insertHeaders(allApps.filter { it.label.lowercase().contains(query) })
                    adapter.updateData(restored)

                    val letter = lastLetter
                    if (letter != null) {
                        val position = restored.indexOfFirst { it.isHeader && it.label.firstOrNull() == letter }
                        if (position >= 0) scrollToLetterPosition(position, restored.size)
                    }
                    lastLetter = null
                    true
                }
                else -> false
            }
        }
    }

    // Verhaelt sich wie im Kontakte-Buch: normalerweise wird der Buchstabe
    // ganz oben angeheftet. Ist man aber schon nahe am Ende der Liste
    // (der Rest passt bereits auf den Bildschirm), bleibt die Liste einfach
    // wo sie ist statt hochzuspringen - so wie beim Ueberfahren von X/Y/Z,
    // wenn Z schon unten sichtbar ist. Naeherung per sichtbarer Zeilenzahl,
    // kein pixelgenauer Nachbau.
    private fun scrollToLetterPosition(position: Int, totalItems: Int) {
        val visibleCount = layoutManager.childCount.coerceAtLeast(1)
        val maxTopPosition = (totalItems - visibleCount).coerceAtLeast(0)
        if (position >= maxTopPosition) {
            layoutManager.scrollToPositionWithOffset(maxTopPosition, 0)
        } else {
            layoutManager.scrollToPositionWithOffset(position, 0)
        }
    }

    private fun vibrateTick(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator.vibrate(VibrationEffect.createOneShot(8, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(8, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(8)
                }
            }
        } catch (e: Exception) {
            // Kein Vibrationsmotor/keine Berechtigung - bewusst ignoriert
        }
    }
}
