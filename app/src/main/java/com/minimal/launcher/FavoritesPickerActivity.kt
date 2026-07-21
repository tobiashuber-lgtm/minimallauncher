package com.minimal.launcher

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// Wird sowohl fuer die globalen Favoriten (kein "mode"-Extra) als auch fuer
// modusspezifische Favoriten verwendet (Intent-Extra "mode" gesetzt).
// Zwei Modi: normale Checkbox-Auswahl, und ein Reihenfolge-Modus zum
// Draufhalten und Ziehen (bzw. per Knopf alphabetisch sortieren).
class FavoritesPickerActivity : AppCompatActivity() {

    private val selected = mutableListOf<String>()
    private lateinit var tvHeader: TextView
    private lateinit var recyclerView: RecyclerView
    private var mode: String? = null
    private var apps: List<AppEntry> = emptyList()
    private var reorderMode = false
    private var itemTouchHelper: ItemTouchHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_list_picker)
        mode = intent.getStringExtra("mode")

        tvHeader = findViewById(R.id.tvHeader)
        recyclerView = findViewById(R.id.rvApps)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val excluded = Prefs.getHiddenPackages(this) + (mode?.let { Prefs.getModeLockedPackages(this, it) } ?: emptySet())
        apps = loadApps().filter { it.packageName !in excluded }

        selected.addAll(Prefs.getFavoritePackages(this, mode).filter { pkg -> apps.any { it.packageName == pkg } })

        findViewById<android.view.View>(R.id.rowSelectAll).visibility = android.view.View.VISIBLE
        findViewById<TextView>(R.id.btnSelectAll).apply {
            text = "Reihenfolge anpassen"
            setOnClickListener { toggleReorderMode() }
        }
        findViewById<TextView>(R.id.btnSelectNone).apply {
            text = "A-Z sortieren"
            setOnClickListener {
                selected.sortBy { pkg -> apps.firstOrNull { it.packageName == pkg }?.label?.lowercase() ?: pkg }
                Prefs.setFavoritePackages(this@FavoritesPickerActivity, selected, mode)
                showSelectionView()
            }
        }

        showSelectionView()
    }

    private fun toggleReorderMode() {
        reorderMode = !reorderMode
        if (reorderMode) showReorderView() else showSelectionView()
    }

    private fun showSelectionView() {
        reorderMode = false
        itemTouchHelper?.attachToRecyclerView(null)
        val maxCount = Prefs.getFavoritesCount(this)
        updateHeader(maxCount)

        val adapter = CheckableAppAdapter(
            apps,
            isChecked = { selected.contains(it) }
        ) { entry, checked ->
            if (checked) {
                if (selected.size >= maxCount) {
                    Toast.makeText(
                        this, "Maximal $maxCount Favoriten (in den Einstellungen aenderbar)", Toast.LENGTH_SHORT
                    ).show()
                    return@CheckableAppAdapter false
                }
                selected.add(entry.packageName)
            } else {
                selected.remove(entry.packageName)
            }
            Prefs.setFavoritePackages(this, selected, mode)
            updateHeader(maxCount)
            true
        }
        recyclerView.adapter = adapter
    }

    private fun showReorderView() {
        tvHeader.text = "Ziehen zum Sortieren"
        val orderedApps = selected.mapNotNull { pkg ->
            apps.firstOrNull { it.packageName == pkg }?.let { pkg to it.label }
        }.toMutableList()

        val orderAdapter = FavoritesOrderAdapter(orderedApps)
        recyclerView.adapter = orderAdapter

        val callback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                orderAdapter.moveItem(vh.bindingAdapterPosition, target.bindingAdapterPosition)
                selected.clear()
                selected.addAll(orderAdapter.currentPackageOrder())
                Prefs.setFavoritePackages(this@FavoritesPickerActivity, selected, mode)
                return true
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        }
        itemTouchHelper = ItemTouchHelper(callback).also { it.attachToRecyclerView(recyclerView) }
    }

    private fun updateHeader(maxCount: Int) {
        val prefix = mode?.let { "Modus \"$it\": " } ?: ""
        tvHeader.text = "$prefix${selected.size}/$maxCount ausgewaehlt"
    }

    private fun loadApps(): List<AppEntry> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0).map {
            AppEntry(it.loadLabel(pm).toString(), it.activityInfo.packageName)
        }.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
    }
}
