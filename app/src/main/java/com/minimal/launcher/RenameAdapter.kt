package com.minimal.launcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RenameAdapter(
    private val items: List<AppEntry>,
    private val displayNameFor: (AppEntry) -> String,
    private val onClick: (AppEntry) -> Unit
) : RecyclerView.Adapter<RenameAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDisplay: TextView = view.findViewById(R.id.tvDisplayName)
        val tvOriginal: TextView = view.findViewById(R.id.tvOriginalName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_rename, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val display = displayNameFor(item)
        holder.tvDisplay.text = display
        holder.tvOriginal.text = if (display != item.label) item.label else ""
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size

    fun notifyRenamed() = notifyDataSetChanged()
}
