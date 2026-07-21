package com.minimal.launcher

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// Zeilenbasierte, einfache Notizen. Jede Zeile bricht wie gewohnt
// automatisch um (kein manuelles "+Zeile" mehr noetig) - Enter erzeugt
// eine neue Zeile darunter, bei Bullet/Checkliste mit demselben Typ
// (wie bei einem einfachen Google-Docs-Listenverhalten).
class NotesFragment : Fragment(R.layout.fragment_notes) {

    private lateinit var lines: MutableList<NoteLine>
    private lateinit var adapter: NoteLineAdapter
    private lateinit var recyclerView: RecyclerView
    private var focusedPosition = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()

        view.findViewById<TextView>(R.id.tvNotesTitle).apply {
            text = Strings.get(context, "notes_title")
            typeface = FontFamilies.buildTypeface(context, "space_mono:bold")
        }

        lines = NoteSerializer.parse(Prefs.getNotesRaw(context))
        recyclerView = view.findViewById(R.id.rvNoteLines)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val typeface = FontFamilies.buildTypeface(context, "space_mono:normal")
        adapter = NoteLineAdapter(
            lines,
            typeface,
            onChanged = { saveNotes() },
            onFocusChanged = { pos -> focusedPosition = pos },
            onEnterPressed = { pos -> insertLineAfter(pos) }
        )
        recyclerView.adapter = adapter

        view.findViewById<View>(R.id.btnBold).setOnClickListener { toggleType(NoteLineType.BOLD) }
        view.findViewById<View>(R.id.btnBullet).setOnClickListener { toggleType(NoteLineType.BULLET) }
        view.findViewById<View>(R.id.btnCheck).setOnClickListener { toggleType(NoteLineType.CHECK_OFF) }
        view.findViewById<ImageView>(R.id.btnDeleteNote).setOnClickListener { confirmDeleteAll() }
    }

    private fun toggleType(target: NoteLineType) {
        if (lines.isEmpty()) return
        val pos = focusedPosition.coerceIn(0, lines.size - 1)
        val current = lines[pos]
        val isChecklist = current.type == NoteLineType.CHECK_OFF || current.type == NoteLineType.CHECK_ON
        val targetIsChecklist = target == NoteLineType.CHECK_OFF
        current.type = if (current.type == target || (isChecklist && targetIsChecklist)) {
            NoteLineType.PLAIN
        } else {
            target
        }
        adapter.notifyItemChanged(pos)
        saveNotes()
    }

    // Enter auf einer Zeile: neue Zeile darunter einfuegen. Bullet/Checkliste
    // bleiben als Typ erhalten (naechster Punkt), Haken einer angehakten
    // Checkliste startet aber wieder unangehakt.
    private fun insertLineAfter(position: Int) {
        val current = lines.getOrNull(position) ?: return
        val newType = if (current.type == NoteLineType.CHECK_ON) NoteLineType.CHECK_OFF else current.type
        val insertAt = position + 1
        lines.add(insertAt, NoteLine(newType, ""))
        adapter.notifyItemInserted(insertAt)
        focusedPosition = insertAt
        recyclerView.post {
            recyclerView.scrollToPosition(insertAt)
            recyclerView.post {
                val holder = recyclerView.findViewHolderForAdapterPosition(insertAt) as? NoteLineAdapter.ViewHolder
                holder?.etLine?.requestFocus()
            }
        }
        saveNotes()
    }

    private fun confirmDeleteAll() {
        val context = requireContext()
        AlertDialog.Builder(context, R.style.Theme_MinimalLauncher_Dialog)
            .setTitle(Strings.get(context, "notes_delete_title"))
            .setPositiveButton(Strings.get(context, "notes_delete_yes")) { _, _ -> deleteAll() }
            .setNegativeButton(Strings.get(context, "notes_delete_no"), null)
            .show()
    }

    private fun deleteAll() {
        lines.clear()
        lines.add(NoteLine(NoteLineType.PLAIN, ""))
        adapter.notifyDataSetChanged()
        saveNotes()
    }

    private fun saveNotes() {
        Prefs.setNotesRaw(requireContext(), NoteSerializer.serialize(lines))
    }
}
