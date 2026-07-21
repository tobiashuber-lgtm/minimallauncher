package com.minimal.launcher

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// Ganz einfache formatierte Notizen: Zeilen-Liste statt freiem Fliesstext,
// dafuer robust und ohne fragile Text-Span-Klick-Erkennung. Fett wirkt auf
// die ganze aktuell fokussierte Zeile, kein Teil-Text-Bold.
class NotesFragment : Fragment(R.layout.fragment_notes) {

    private lateinit var lines: MutableList<NoteLine>
    private lateinit var adapter: NoteLineAdapter
    private lateinit var recyclerView: RecyclerView
    private var focusedPosition = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()

        lines = NoteSerializer.parse(Prefs.getNotesRaw(context))
        recyclerView = view.findViewById(R.id.rvNoteLines)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val typeface = FontFamilies.buildTypeface(context, "space_mono:normal")
        adapter = NoteLineAdapter(
            lines,
            typeface,
            onChanged = { saveNotes() },
            onFocusChanged = { pos -> focusedPosition = pos }
        )
        recyclerView.adapter = adapter

        view.findViewById<View>(R.id.btnBold).setOnClickListener { toggleType(NoteLineType.BOLD) }
        view.findViewById<View>(R.id.btnBullet).setOnClickListener { toggleType(NoteLineType.BULLET) }
        view.findViewById<View>(R.id.btnCheck).setOnClickListener { toggleType(NoteLineType.CHECK_OFF) }
        view.findViewById<View>(R.id.btnAddLine).setOnClickListener { addLine() }
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

    private fun addLine() {
        lines.add(NoteLine(NoteLineType.PLAIN, ""))
        adapter.notifyItemInserted(lines.size - 1)
        recyclerView.scrollToPosition(lines.size - 1)
        saveNotes()
    }

    private fun saveNotes() {
        Prefs.setNotesRaw(requireContext(), NoteSerializer.serialize(lines))
    }
}
