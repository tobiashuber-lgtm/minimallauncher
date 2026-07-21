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
    val isHeader: Boolean = false,
    val groupLetter: Char = ' '
)

class DrawerFragment : Fragment(R.layout.fragment_drawer) {

    // A-Z plus '#' fuer Apps ohne Anfangsbuchstaben (Zahlen/Symbole).
    // '#' bekommt beim Antippen/Ziehen doppelt so viel Platz wie ein
    // normaler Buchstabe - leichter mit dem Daumen zu treffen.
    private val letters = ('A'..'Z').toList() + '#'
    private val letterWeights = List(26) { 1f } + listOf(2f)

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppDrawerAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private var allApps: List<AppEntry> = emptyList()
    private var availableLetters: List<Char> = emptyList()
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

        // Fade am unteren Rand nur zeigen, solange noch mehr Inhalt folgt -
        // sonst wuerde er die letzte (tatsaechlich unterste) App verdecken,
        // obwohl man die ja vielleicht gerade antippen will.
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                rv.isVerticalFadingEdgeEnabled = rv.canScrollVertically(1)
            }
        })

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
        availableLetters = allApps.map { letterFor(it) }.distinct()
        adapter.updateData(insertHeaders(allApps))
        adapter.updateStyle(currentStyle())
        rebuildLetterIndex()
        recyclerView.post { recyclerView.isVerticalFadingEdgeEnabled = recyclerView.canScrollVertically(1) }
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
    // Ueberschriften-Zeile ein (wie im Kontakte-/Telefon-Buch), und
    // vermerkt bei jedem Eintrag (inkl. Header) zu welcher Gruppe er
    // gehoert - fuer die Abdimm-Logik beim Ziehen an der Seitenleiste.
    private fun insertHeaders(list: List<AppEntry>): List<AppEntry> {
        val result = mutableListOf<AppEntry>()
        var lastLetter: Char? = null
        for (entry in list) {
            val letter = letterFor(entry)
            if (letter != lastLetter) {
                result.add(AppEntry(label = letter.toString(), packageName = "", isHeader = true, groupLetter = letter))
                lastLetter = letter
            }
            result.add(entry.copy(groupLetter = letter))
        }
        return result
    }

    private fun letterFor(entry: AppEntry): Char {
        val c = entry.label.firstOrNull()?.uppercaseChar() ?: '#'
        return if (c in 'A'..'Z') c else '#'
    }

    // Naechster verfuegbarer Buchstabe mit Apps: zuerst vorwaerts suchen
    // (naechste Gruppe danach), sonst rueckwaerts.
    private fun nearestAvailableLetter(target: Char): Char? {
        if (availableLetters.isEmpty()) return null
        if (target in availableLetters) return target
        val targetIndex = letters.indexOf(target)
        for (i in targetIndex until letters.size) {
            if (letters[i] in availableLetters) return letters[i]
        }
        for (i in targetIndex downTo 0) {
            if (letters[i] in availableLetters) return letters[i]
        }
        return null
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

        letters.forEachIndexed { index, letter ->
            val tv = TextView(context).apply {
                text = letter.toString()
                textSize = 9f
                setTextColor(palette.textSecondary)
                gravity = Gravity.CENTER
            }
            lettersContainer.addView(
                tv,
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, letterWeights[index])
            )
        }

        var lastLetter: Char? = null

        lettersContainer.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    val touchedLetter = letterAtY(event.y, v.height)
                    val letter = nearestAvailableLetter(touchedLetter) ?: return@setOnTouchListener true

                    if (letter != lastLetter) {
                        vibrateTick(context)
                        lastLetter = letter
                        adapter.setHighlight(letter)

                        val currentData = adapter.currentItems()
                        val position = currentData.indexOfFirst { it.isHeader && it.groupLetter == letter }
                        if (position >= 0) scrollToLetterPosition(position, currentData.size)
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Bewusst KEIN weiterer Sprung/Scroll hier - die Liste
                    // bleibt genau dort stehen, wo das Ziehen sie hingebracht
                    // hat. Nur die graue Abdimmung wird wieder aufgehoben.
                    adapter.setHighlight(null)
                    lastLetter = null
                    true
                }
                else -> false
            }
        }
    }

    // Ordnet eine Y-Position innerhalb der Seitenleiste dem passenden
    // Buchstaben zu - beruecksichtigt, dass '#' doppelt so hoch ist wie
    // ein normaler Buchstabe (siehe letterWeights).
    private fun letterAtY(y: Float, height: Int): Char {
        val totalWeight = letterWeights.sum()
        var acc = 0f
        for (i in letters.indices) {
            val segmentHeight = (letterWeights[i] / totalWeight) * height
            if (y < acc + segmentHeight) return letters[i]
            acc += segmentHeight
        }
        return letters.last()
    }

    // Verhaelt sich wie im Kontakte-Buch: der Buchstabe wird normalerweise
    // ganz oben angeheftet. Ist man aber schon nahe am Ende der Liste (der
    // Rest passt bereits auf den Bildschirm), bleibt die Ansicht einfach wo
    // sie ist statt hochzuspringen. Naeherung per sichtbarer Zeilenzahl,
    // kein pixelgenauer Nachbau.
    private fun scrollToLetterPosition(position: Int, totalItems: Int) {
        val visibleCount = layoutManager.childCount.coerceAtLeast(1)
        val maxTopPosition = (totalItems - visibleCount).coerceAtLeast(0)
        val target = position.coerceAtMost(maxTopPosition)
        layoutManager.scrollToPositionWithOffset(target, 0)
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
