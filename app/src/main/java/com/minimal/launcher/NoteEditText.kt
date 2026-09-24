package com.minimal.launcher

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import androidx.appcompat.widget.AppCompatEditText

// Normales EditText erkennt "Backspace am Zeilenanfang" nicht zuverlaessig
// (viele Tastaturen senden das nicht als normales Zeichen-Loeschen). Diese
// Klasse faengt es auf zwei Wegen ab: ueber echte Tastatur-Events UND ueber
// die InputConnection (fuer die meisten Software-Tastaturen).
class NoteEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    // Rueckgabe true = selbst behandelt (normales Loeschen unterdruecken).
    var onBackspaceAtStart: (() -> Boolean)? = null

    private fun isAtStart(): Boolean = selectionStart == 0 && selectionEnd == 0

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        val ic = super.onCreateInputConnection(outAttrs) ?: return null
        return object : InputConnectionWrapper(ic, true) {
            override fun sendKeyEvent(event: KeyEvent): Boolean {
                if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_DEL && isAtStart()) {
                    if (onBackspaceAtStart?.invoke() == true) return true
                }
                return super.sendKeyEvent(event)
            }

            override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
                if (beforeLength > 0 && isAtStart()) {
                    if (onBackspaceAtStart?.invoke() == true) return true
                }
                return super.deleteSurroundingText(beforeLength, afterLength)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DEL && isAtStart()) {
            if (onBackspaceAtStart?.invoke() == true) return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
