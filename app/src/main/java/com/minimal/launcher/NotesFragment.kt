package com.minimal.launcher

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// Zeilenbasierte, einfache Notizen mit echtem Wortumbruch pro Zeile.
// Enter (erzeugt intern ein '\n') teilt die aktuelle Zeile in zwei auf -
// bei Bullet/Checkliste bleibt der Typ fuer die neue Zeile erhalten.
// Backspace am Zeilenanfang: entfernt Bullet/Haken-Formatierung, oder
// verschmilzt mit der vorherigen Zeile (klassisches Editor-Verhalten).
class NotesFragment : Fragment(R.layout.fragment_notes) {

    private lateinit var lines: MutableList<NoteLine>
    private lateinit var adapter: NoteLineAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
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
        layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager

        val typeface = FontFamilies.buildTypeface(context, "space_mono:normal")
        adapter = NoteLineAdapter(
            lines,
            typeface,
            onChanged = { saveNotes() },
            onFocusChanged = { pos -> focusedPosition = pos },
            onSplitLine = { pos, before, after -> splitLine(pos, before, after) },
            onBackspaceAtStart = { pos -> handleBackspaceAtStart(pos) }
        )
        recyclerView.adapter = adapter

        // Tippen irgendwo im Hintergrund (nicht direkt auf eine Zeile
        // getroffen) fokussiert trotzdem die letzte Zeile, statt dass man
        // die Zeile exakt treffen muss.
        recyclerView.isClickable = true
        recyclerView.setOnClickListener { focusLine(lines.size - 1, toEnd = true) }

        setupKeyboardVisibilityListener(view)

        view.findViewById<View>(R.id.btnBold).setOnClickListener { toggleType(NoteLineType.BOLD) }
        view.findViewById<View>(R.id.btnBullet).setOnClickListener { toggleType(NoteLineType.BULLET) }
        view.findViewById<View>(R.id.btnCheck).setOnClickListener { toggleType(NoteLineType.CHECK_OFF) }
        view.findViewById<ImageView>(R.id.btnDeleteNote).setOnClickListener { confirmDeleteAll() }
    }

    override fun onResume() {
        super.onResume()
        // Leeres Notepad: gleich in die erste Zeile springen und Tastatur
        // direkt oeffnen, damit man sofort lostippen kann.
        if (lines.size == 1 && lines[0].text.isBlank()) {
            recyclerView.post {
                focusLine(0, toEnd = false, showKeyboard = true)
            }
        }
    }

    private fun focusLine(position: Int, toEnd: Boolean, showKeyboard: Boolean = false) {
        if (lines.isEmpty()) return
        val pos = position.coerceIn(0, lines.size - 1)
        recyclerView.post {
            val holder = recyclerView.findViewHolderForAdapterPosition(pos) as? NoteLineAdapter.ViewHolder
            holder?.etLine?.apply {
                requestFocus()
                setSelection(if (toEnd) (text?.length ?: 0) else 0)
                if (showKeyboard) {
                    val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                        as android.view.inputmethod.InputMethodManager
                    imm.showSoftInput(this, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
                }
            }
            ensureLineVisible(pos)
        }
    }

    // Scrollt so, dass die Zeile wirklich komplett innerhalb des sichtbaren
    // (nicht von der Tastatur belegten) Bereichs liegt - misst dafuer die
    // tatsaechliche Position nach, statt sich auf "irgendwie sichtbar" von
    // RecyclerView.scrollToPosition zu verlassen (das reicht bei frisch
    // eingefuegten/gewachsenen Zeilen oft nicht aus).
    private fun ensureLineVisible(position: Int) {
        recyclerView.post {
            recyclerView.post {
                val child = layoutManager.findViewByPosition(position)
                val visibleBottom = recyclerView.height - recyclerView.paddingBottom
                if (child != null) {
                    when {
                        child.bottom > visibleBottom -> recyclerView.scrollBy(0, child.bottom - visibleBottom)
                        child.top < recyclerView.paddingTop -> recyclerView.scrollBy(0, child.top - recyclerView.paddingTop)
                    }
                } else {
                    recyclerView.scrollToPosition(position)
                }
            }
        }
    }

    // Fragt die tatsaechliche Tastatur-Hoehe direkt bei Android ab (statt sie
    // ueber die Fensterhoehe zu schaetzen) und haelt so viel Platz am unteren
    // Rand frei, wie die Tastatur gerade braucht - reagiert live auch waehrend
    // die Tastatur ein-/ausfaehrt.
    private fun setupKeyboardVisibilityListener(root: View) {
        var wasKeyboardVisible = false
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val imeHeight = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime()).bottom
            val navBarHeight = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars()).bottom
            val bottomPadding = (imeHeight - navBarHeight).coerceAtLeast(0)

            recyclerView.setPadding(
                recyclerView.paddingLeft, recyclerView.paddingTop, recyclerView.paddingRight, bottomPadding
            )

            val isKeyboardVisible = imeHeight > 0
            if (isKeyboardVisible) {
                ensureLineVisible(focusedPosition)
            } else if (wasKeyboardVisible) {
                recyclerView.post { recyclerView.scrollToPosition((lines.size - 1).coerceAtLeast(0)) }
            }
            wasKeyboardVisible = isKeyboardVisible

            insets
        }
        androidx.core.view.ViewCompat.requestApplyInsets(root)
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

    // Enter (bzw. eingefuegtes '\n') teilt eine Zeile in zwei auf. Bullet/
    // Checkliste vererben sich an die neue Zeile (Haken startet unangehakt).
    private fun splitLine(position: Int, textBefore: String, textAfter: String) {
        val current = lines.getOrNull(position) ?: return
        current.text = textBefore
        val newType = if (current.type == NoteLineType.CHECK_ON) NoteLineType.CHECK_OFF else current.type
        val insertAt = position + 1
        lines.add(insertAt, NoteLine(newType, textAfter))
        adapter.notifyItemChanged(position)
        adapter.notifyItemInserted(insertAt)
        focusedPosition = insertAt
        recyclerView.post {
            val holder = recyclerView.findViewHolderForAdapterPosition(insertAt) as? NoteLineAdapter.ViewHolder
            holder?.etLine?.apply {
                requestFocus()
                setSelection(0)
            }
            ensureLineVisible(insertAt)
        }
        saveNotes()
    }

    // Backspace ganz am Anfang einer Zeile:
    // - Bullet/Checkliste -> wird zu normalem Text (Formatierung entfernt)
    // - normale Zeile -> verschmilzt mit der vorherigen Zeile, Cursor an
    //   der Verschmelzungsstelle
    private fun handleBackspaceAtStart(position: Int): Boolean {
        val current = lines.getOrNull(position) ?: return false
        val isSpecial = current.type == NoteLineType.BULLET ||
            current.type == NoteLineType.CHECK_OFF || current.type == NoteLineType.CHECK_ON

        if (isSpecial) {
            current.type = NoteLineType.PLAIN
            adapter.notifyItemChanged(position)
            saveNotes()
            return true
        }

        if (position == 0) return false

        val previous = lines[position - 1]
        val mergeCursorPos = previous.text.length
        previous.text = previous.text + current.text
        lines.removeAt(position)
        adapter.notifyItemRemoved(position)
        adapter.notifyItemChanged(position - 1)
        focusedPosition = position - 1
        recyclerView.post {
            val holder = recyclerView.findViewHolderForAdapterPosition(position - 1) as? NoteLineAdapter.ViewHolder
            holder?.etLine?.apply {
                requestFocus()
                setSelection(mergeCursorPos.coerceIn(0, text?.length ?: 0))
            }
            ensureLineVisible(position - 1)
        }
        saveNotes()
        return true
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
