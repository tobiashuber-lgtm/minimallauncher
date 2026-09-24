package com.minimal.launcher

enum class NoteLineType { PLAIN, BOLD, BULLET, CHECK_OFF, CHECK_ON }

data class NoteLine(var type: NoteLineType, var text: String)

// Sehr einfaches Speicherformat: eine Zeile pro Notizzeile, "TYP|Text".
object NoteSerializer {
    fun parse(raw: String): MutableList<NoteLine> {
        if (raw.isBlank()) return mutableListOf(NoteLine(NoteLineType.PLAIN, ""))
        val lines = raw.split("\n").map { line ->
            val parts = line.split("|", limit = 2)
            val type = try {
                NoteLineType.valueOf(parts.getOrElse(0) { "PLAIN" })
            } catch (e: Exception) {
                NoteLineType.PLAIN
            }
            NoteLine(type, parts.getOrElse(1) { "" })
        }.toMutableList()
        return if (lines.isEmpty()) mutableListOf(NoteLine(NoteLineType.PLAIN, "")) else lines
    }

    fun serialize(lines: List<NoteLine>): String =
        lines.joinToString("\n") { "${it.type.name}|${it.text}" }
}
