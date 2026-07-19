package com.minimal.launcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppDrawerAdapter(
    private var items: MutableList<AppEntry>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<AppDrawerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvAppName)
        val tvUsage: TextView = view.findViewById(R.id.tvAppUsage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.label
        // Echtes Nutzungszeit-Tracking folgt in Etappe 3.
        holder.tvUsage.text = "${item.usageMinutes} min"
        holder.itemView.setOnClickListener { onClick(item.packageName) }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<AppEntry>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
    }
}
