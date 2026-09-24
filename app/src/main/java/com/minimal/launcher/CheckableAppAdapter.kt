package com.minimal.launcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Wird sowohl fuer die Favoriten-Auswahl als auch fuer "Apps aus-/einblenden"
// verwendet - onToggle gibt zurueck, ob die Aenderung uebernommen werden darf
// (false = Checkbox wird zurueckgesetzt, z.B. bei erreichtem Favoriten-Limit).
class CheckableAppAdapter(
    private val items: List<AppEntry>,
    private val isChecked: (String) -> Boolean,
    private val onToggle: (AppEntry, Boolean) -> Boolean
) : RecyclerView.Adapter<CheckableAppAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvAppName)
        val checkbox: CheckBox = view.findViewById(R.id.checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_checkbox, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.label
        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = isChecked(item.packageName)
        holder.checkbox.setOnCheckedChangeListener { _, checked ->
            val accepted = onToggle(item, checked)
            if (!accepted) {
                holder.checkbox.setOnCheckedChangeListener(null)
                holder.checkbox.isChecked = !checked
                holder.checkbox.setOnCheckedChangeListener { _, c -> onToggle(item, c) }
            }
        }
    }

    override fun getItemCount() = items.size
}
