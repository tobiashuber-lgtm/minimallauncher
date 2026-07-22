package com.minimal.launcher

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppVisibilityActivity : AppCompatActivity() {

    private lateinit var hidden: MutableSet<String>
    private lateinit var tvHeader: TextView

    private fun s(key: String) = Strings.get(this, key)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_list_picker)

        tvHeader = findViewById(R.id.tvHeader)
        val recyclerView = findViewById<RecyclerView>(R.id.rvApps)
        recyclerView.layoutManager = LinearLayoutManager(this)

        hidden = Prefs.getHiddenPackages(this).toMutableSet()
        val apps = loadAllApps()
        updateHeader()

        val adapter = CheckableAppAdapter(
            apps,
            isChecked = { !hidden.contains(it) }
        ) { entry, checked ->
            if (checked) hidden.remove(entry.packageName) else hidden.add(entry.packageName)
            Prefs.setHiddenPackages(this, hidden)
            updateHeader()
            true
        }
        recyclerView.adapter = adapter

        findViewById<android.view.View>(R.id.rowSelectAll).visibility = android.view.View.VISIBLE
        findViewById<TextView>(R.id.btnSelectAll).apply {
            text = s("select_all")
            setOnClickListener {
                hidden.clear()
                Prefs.setHiddenPackages(this@AppVisibilityActivity, hidden)
                updateHeader()
                adapter.notifyDataSetChanged()
            }
        }
        findViewById<TextView>(R.id.btnSelectNone).apply {
            text = s("select_none")
            setOnClickListener {
                hidden.clear()
                hidden.addAll(apps.map { it.packageName })
                Prefs.setHiddenPackages(this@AppVisibilityActivity, hidden)
                updateHeader()
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun updateHeader() {
        tvHeader.text = "${hidden.size} ${s("visibility_hidden_count")}"
    }

    private fun loadAllApps(): List<AppEntry> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0)
            .map { AppEntry(it.loadLabel(pm).toString(), it.activityInfo.packageName) }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }
}
