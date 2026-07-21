package com.minimal.launcher

import android.graphics.Paint
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteLineAdapter(
    private val lines: MutableList<NoteLine>,
    private val typeface: Typeface,
    private val onChanged: () -> Unit,
    private val onFocusChanged: (Int) -> Unit
) : RecyclerView.Adapter<NoteLineAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkbox: CheckBox = view.findViewById(R.id.checkbox)
        val tvBullet: TextView = view.findViewById(R.id.tvBullet)
        val etLine: EditText = view.findViewById(R.id.etLine)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note_line, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val line = lines[position]

        // Alte Listener entfernen bevor die Zeile (Recycling!) neu befuellt wird.
        holder.etLine.setOnFocusChangeListener(null)
        (holder.etLine.tag as? TextWatcher)?.let { holder.etLine.removeTextChangedListener(it) }

        holder.etLine.typeface = typeface
        if (holder.etLine.text.toString() != line.text) holder.etLine.setText(line.text)

        holder.checkbox.visibility =
            if (line.type == NoteLineType.CHECK_OFF || line.type == NoteLineType.CHECK_ON) View.VISIBLE else View.GONE
        holder.tvBullet.visibility = if (line.type == NoteLineType.BULLET) View.VISIBLE else View.GONE

        when (line.type) {
            NoteLineType.BOLD -> {
                holder.etLine.setTypeface(typeface, Typeface.BOLD)
                holder.etLine.paintFlags = holder.etLine.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            NoteLineType.CHECK_ON -> {
                holder.etLine.setTypeface(typeface, Typeface.NORMAL)
                holder.etLine.paintFlags = holder.etLine.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
            else -> {
                holder.etLine.setTypeface(typeface, Typeface.NORMAL)
                holder.etLine.paintFlags = holder.etLine.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }

        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = line.type == NoteLineType.CHECK_ON
        holder.checkbox.setOnCheckedChangeListener { _, checked ->
            line.type = if (checked) NoteLineType.CHECK_ON else NoteLineType.CHECK_OFF
            onChanged()
            notifyItemChanged(holder.bindingAdapterPosition)
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                line.text = s.toString()
                onChanged()
            }
        }
        holder.etLine.addTextChangedListener(watcher)
        holder.etLine.tag = watcher

        holder.etLine.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) onFocusChanged(holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount() = lines.size
}
