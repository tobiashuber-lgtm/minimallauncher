package com.minimal.launcher

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ModeNotificationMuteActivity : AppCompatActivity() {

    private lateinit var muted: MutableSet<String>
    private lateinit var tvHeader: TextView
    private lateinit var mode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_list_picker)
        mode = intent.getStringExtra("mode") ?: run { finish(); return }

        tvHeader = findViewById(R.id.tvHeader)
        val recyclerView = findViewById<RecyclerView>(R.id.rvApps)
        recyclerView.layoutManager = LinearLayoutManager(this)

        muted = Prefs.getModeMutedNotificationPackages(this, mode).toMutableSet()
        val hidden = Prefs.getHiddenPackages(this)
        val apps = loadApps().filter { it.packageName !in hidden }
        updateHeader()

        val adapter = CheckableAppAdapter(
            apps,
            isChecked = { muted.contains(it) }
        ) { entry, checked ->
            if (checked) muted.add(entry.packageName) else muted.remove(entry.packageName)
            Prefs.setModeMutedNotificationPackages(this, mode, muted)
            updateHeader()
            true
        }
        recyclerView.adapter = adapter

        findViewById<android.view.View>(R.id.rowSelectAll).visibility = android.view.View.VISIBLE
        findViewById<TextView>(R.id.btnSelectAll).setOnClickListener {
            muted.clear(); muted.addAll(apps.map { it.packageName })
            Prefs.setModeMutedNotificationPackages(this, mode, muted)
            updateHeader(); adapter.notifyDataSetChanged()
        }
        findViewById<TextView>(R.id.btnSelectNone).setOnClickListener {
            muted.clear()
            Prefs.setModeMutedNotificationPackages(this, mode, muted)
            updateHeader(); adapter.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()
        updateHeader()
    }

    private fun updateHeader() {
        val access = if (NotificationAccessHelper.hasPermission(this)) "" else
            "\n⚠️ Benachrichtigungszugriff fehlt noch - antippen zum Aktivieren"
        tvHeader.text = "Modus \"$mode\": ${muted.size} App(s) stummgeschaltet$access"
        tvHeader.setOnClickListener {
            if (!NotificationAccessHelper.hasPermission(this)) {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }
    }

    private fun loadApps(): List<AppEntry> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0).map {
            AppEntry(it.loadLabel(pm).toString(), it.activityInfo.packageName)
        }.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
    }
}
