package com.minimal.launcher

import android.content.Intent
import android.os.Bundle
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
data class AppEntry(val label: String, val packageName: String, val usageMinutes: Int = 0)

class DrawerFragment : Fragment(R.layout.fragment_drawer) {

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
                adapter.updateData(allApps.filter { it.label.lowercase().contains(query) })
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        setupLetterIndex(view)
    }

    override fun onResume() {
        super.onResume()
        applyPalette()
        allApps = loadApps()
        adapter.updateData(allApps)
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

    private fun rebuildLetterIndex() {
        val letterIndex = rootView.findViewById<LinearLayout>(R.id.letterIndex)
        letterIndex.removeAllViews()
        setupLetterIndex(rootView)
    }

    private fun setupLetterIndex(root: View) {
        val letterIndex = root.findViewById<LinearLayout>(R.id.letterIndex)
        if (letterIndex.childCount > 0) return
        val letters = ('A'..'Z').toList()
        val context = requireContext()
        val palette = Palettes.current(context)

        letters.forEach { letter ->
            val tv = TextView(context).apply {
                text = letter.toString()
                textSize = 9f
                setTextColor(palette.textSecondary)
                gravity = Gravity.CENTER
            }
            letterIndex.addView(
                tv,
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
            )
        }

        letterIndex.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    val itemHeight = v.height / letters.size.toFloat()
                    val index = (event.y / itemHeight).toInt().coerceIn(0, letters.size - 1)
                    jumpToLetter(letters[index])
                    true
                }
                else -> false
            }
        }
    }

    private fun jumpToLetter(letter: Char) {
        val position = adapter.currentItems().indexOfFirst { it.label.firstOrNull()?.uppercaseChar() == letter }
        if (position >= 0) layoutManager.scrollToPositionWithOffset(position, 0)
    }
}
