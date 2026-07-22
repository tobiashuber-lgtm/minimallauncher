package com.minimal.launcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FavoritesOrderAdapter(
    private val items: MutableList<Pair<String, String>> // packageName to label
) : RecyclerView.Adapter<FavoritesOrderAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvAppName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvName.text = items[position].second
    }

    override fun getItemCount() = items.size

    fun moveItem(from: Int, to: Int) {
        val item = items.removeAt(from)
        items.add(to, item)
        notifyItemMoved(from, to)
    }

    fun sortAlphabetically() {
        items.sortBy { it.second.lowercase() }
        notifyDataSetChanged()
    }

    fun currentPackageOrder(): List<String> = items.map { it.first }
}
