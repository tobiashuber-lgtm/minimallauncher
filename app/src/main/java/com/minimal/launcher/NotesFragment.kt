package com.minimal.launcher

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment

// Etappe 1: interne Mini-Notiz, lokal gespeichert. Die Wahl zwischen
// "interne Notizen" und "bestimmte App oeffnen" folgt im Settings-Screen.
class NotesFragment : Fragment(R.layout.fragment_notes) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val etNotes = view.findViewById<EditText>(R.id.etNotes)
        val prefs = requireContext().getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)

        etNotes.setText(prefs.getString("notes_text", ""))

        etNotes.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                prefs.edit().putString("notes_text", s.toString()).apply()
            }
        })
    }
}
