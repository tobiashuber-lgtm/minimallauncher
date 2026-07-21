package com.minimal.launcher

import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class DrawerStyle(
    val typeface: Typeface,
    val textColor: Int,
    val secondaryColor: Int,
    val sizeSp: Float
)

class AppDrawerAdapter(
    private var items: MutableList<AppEntry>,
    private var style: DrawerStyle,
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

        if (item.isHeader) {
            holder.tvName.text = item.label
            holder.tvName.typeface = style.typeface
            holder.tvName.setTextColor(style.secondaryColor)
            holder.tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            holder.tvUsage.text = ""
            holder.itemView.setOnClickListener(null)
            holder.itemView.setPadding(0, dpToPx(holder.itemView, 14), 0, dpToPx(holder.itemView, 2))
            return
        }

        holder.tvName.text = item.label
        holder.tvName.typeface = style.typeface
        holder.tvName.setTextColor(style.textColor)
        holder.tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, style.sizeSp)

        holder.tvUsage.text = "${item.usageMinutes} min"
        holder.tvUsage.typeface = style.typeface
        holder.tvUsage.setTextColor(style.secondaryColor)

        holder.itemView.setOnClickListener { onClick(item.packageName) }
        holder.itemView.setPadding(0, dpToPx(holder.itemView, 9), 0, dpToPx(holder.itemView, 9))
    }

    private fun dpToPx(view: View, dp: Int): Int = (dp * view.resources.displayMetrics.density).toInt()

    override fun getItemCount() = items.size

    fun updateData(newItems: List<AppEntry>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
    }

    fun updateStyle(newStyle: DrawerStyle) {
        style = newStyle
        notifyDataSetChanged()
    }

    fun currentItems(): List<AppEntry> = items.toList()
}
