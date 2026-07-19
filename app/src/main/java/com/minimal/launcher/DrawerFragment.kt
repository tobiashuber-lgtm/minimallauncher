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

// App-Drawer. Ausgeblendete Apps (Etappe 3) tauchen hier gar nicht erst auf.
data class AppEntry(val label: String, val packageName: String, val usageMinutes: Int = 0)

class DrawerFragment : Fragment(R.layout.fragment_drawer) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppDrawerAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private var allApps: List<AppEntry> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvApps)
        layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        allApps = loadApps()
        adapter = AppDrawerAdapter(allApps.toMutableList()) { pkg ->
            requireContext().packageManager.getLaunchIntentForPackage(pkg)?.let { startActivity(it) }
        }
        recyclerView.adapter = adapter

        val etSearch = view.findViewById<EditText>(R.id.etSearch)
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

    private fun loadApps(): List<AppEntry> {
        val pm = requireContext().packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        return resolveInfos.map {
            AppEntry(it.loadLabel(pm).toString(), it.activityInfo.packageName)
        }.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
    }

    private fun setupLetterIndex(root: View) {
        val letterIndex = root.findViewById<LinearLayout>(R.id.letterIndex)
        val letters = ('A'..'Z').toList()

        letters.forEach { letter ->
            val tv = TextView(requireContext()).apply {
                text = letter.toString()
                textSize = 9f
                setTextColor(0xFF555555.toInt())
                gravity = Gravity.CENTER
            }
            letterIndex.addView(
                tv,
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
            )
        }

        // Tippen ODER Ziehen entlang der Leiste springt direkt zum Buchstaben,
        // genau wie im klassischen Kontaktbuch.
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
        val currentList = adapter.currentItems()
        val position = currentList.indexOfFirst { it.label.firstOrNull()?.uppercaseChar() == letter }
        if (position >= 0) {
            layoutManager.scrollToPositionWithOffset(position, 0)
        }
    }
}
