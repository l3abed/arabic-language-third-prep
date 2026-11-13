package com.example.util

import com.example.model.ExamData
import com.example.model.ExamGrade
import com.example.model.ExamQuestion
import com.example.model.ExamTable
import com.example.model.GradeMode
import com.example.model.GradedItem
import com.example.model.QuestionGrade
import com.example.model.SectionGrade

/**
 * Text matching for Arabic answers. It checks keywords, not meaning, so results are estimates.
 * Keep this file in sync with the Python prototype used to validate the exam data.
 */
object ArabicMatcher {
    private val HARAKAT: Set<Char> = buildSet {
        for (c in '\u064B'..'\u0652') add(c)
        add('\u0670'); add('\u0653'); add('\u0654'); add('\u0655')
    }
    private const val TATWEEL = '\u0640'
    private val STOP: Set<String> = (
        "في من على الي عن ان اذا التي الذي الذين هو هي هذا هذه ذلك تلك ما ثم او قد كان كل بين مع لا لم لن ب ل ك يا ايها"
        ).split(" ").toSet()
    private val PAREN = Regex("\\([^)]*\\)")
    private val ALT_SPLIT = Regex("\\s*/\\s*|\\s+أو\\s+")
    private val SPACES = Regex("\\s+")
    private val QUOTES = Regex("[«»\"“”]")

    fun stripTashkeel(s: String): String = s.filter { it !in HARAKAT && it != TATWEEL }

    fun normLetters(s: String): String {
        val sb = StringBuilder()
        for (ch in stripTashkeel(s)) {
            sb.append(
                when (ch) {
                    'أ', 'إ', 'آ', 'ٱ' -> 'ا'
                    'ى' -> 'ي'
                    'ة' -> 'ه'
                    'ؤ' -> 'و'
                    'ئ' -> 'ي'
                    else -> ch
                }
            )
        }
        return sb.toString()
    }

    fun norm(s: String): String {
        val letters = normLetters(s).map { if (it.isLetterOrDigit()) it else ' ' }.joinToString("")
        return letters.split(' ').filter { it.isNotEmpty() }.joinToString(" ")
    }

    fun stem(token: String): String {
        var t = token
        if (t.length > 3 && (t[0] == 'و' || t[0] == 'ف') && t.length - 1 >= 3) t = t.substring(1)
        if (t.length > 3 && (t[0] == 'ب' || t[0] == 'ل' || t[0] == 'ك')) t = t.substring(1)
        if (t.length > 3 && t.startsWith("ال")) t = t.substring(2)
        for (suf in listOf("ات", "ون", "ين", "ها", "هم")) {
            if (t.length > 4 && t.endsWith(suf)) return t.substring(0, t.length - suf.length)
        }
        if (t.length > 3 && (t.last() == 'ه' || t.last() == 'ي')) t = t.dropLast(1)
        return t
    }

    fun tokens(s: String): List<String> =
        norm(s).split(' ').filter { it.isNotEmpty() && it !in STOP && it.length >= 2 }.map { stem(it) }

    private fun tokenMatch(a: String, b: String): Boolean {
        if (a == b) return true
        val m = minOf(a.length, b.length)
        return m >= 4 && (a.startsWith(b) || b.startsWith(a))
    }

    private fun wordCount(s: String): Int = s.trim().split(SPACES).count { it.isNotEmpty() }

    /** The part of a model answer the student is expected to write (drops explanations and notes). */
    fun mainPart(expected: String): String {
        var e = PAREN.replace(expected, "").trim()
        if (e.isEmpty()) e = expected.trim()
        for (sep in listOf(':', '：')) {
            val idx = e.indexOf(sep)
            if (idx >= 0) {
                val left = e.substring(0, idx)
                val right = e.substring(idx + 1)
                val lw = wordCount(left)
                val rw = wordCount(right)
                if (lw in 1..8 && rw > lw && rw >= 3) e = left.trim()
                break
            }
        }
        return e
    }

    fun variants(main: String): List<String> {
        val parts = ALT_SPLIT.split(main).map { it.trim() }.filter { it.isNotEmpty() }
        return if (parts.isEmpty()) listOf(main) else parts
    }

    private fun variantScore(student: String, v: String): Double {
        val ex = tokens(v)
        val st = tokens(student)
        if (ex.isEmpty()) {
            val nv = norm(v)
            return if (nv.isNotEmpty() && norm(student).contains(nv)) 1.0 else 0.0
        }
        val hits = ex.count { e -> st.any { s -> tokenMatch(e, s) } }
        val c = hits.toDouble() / ex.size
        return if (ex.size <= 2) {
            if (c == 1.0) 1.0 else if (c >= 0.5) 0.5 else 0.0
        } else {
            if (c >= 0.7) 1.0 else if (c >= 0.4) 0.5 else 0.0
        }
    }

    private data class Letter(val base: Char, val marks: String)

    private fun parseLetters(s: String): List<Letter> {
        val bases = ArrayList<Char>()
        val marks = ArrayList<StringBuilder>()
        for (ch in s) {
            when {
                ch in HARAKAT -> if (marks.isNotEmpty()) marks.last().append(ch)
                ch == TATWEEL -> {}
                ch.isLetter() -> {
                    val b = when (ch) {
                        'أ', 'إ', 'آ', 'ٱ' -> 'ا'
                        'ى' -> 'ي'
                        else -> ch
                    }
                    bases.add(b)
                    marks.add(StringBuilder())
                }
            }
        }
        return bases.indices.map { Letter(bases[it], marks[it].toString().toCharArray().sorted().joinToString("")) }
    }

    /** Diacritic-sensitive comparison, for "ضبط الكلمات" questions. */
    fun strictScore(student: String, expected: String): Double {
        val exp = parseLetters(QUOTES.replace(mainPart(expected), ""))
        if (exp.isEmpty()) return 0.0
        val st = parseLetters(student)
        val n = exp.size
        var pos = -1
        var i = 0
        while (i + n <= st.size) {
            var same = true
            for (k in 0 until n) {
                if (st[i + k].base != exp[k].base) { same = false; break }
            }
            if (same) { pos = i; break }
            i++
        }
        if (pos < 0) return 0.0
        val need = exp.indices.filter { exp[it].marks.isNotEmpty() }
        if (need.isEmpty()) return 1.0
        val seg = st.subList(pos, pos + n)
        val typedAny = seg.any { it.marks.isNotEmpty() }
        val ok = need.count { seg[it].marks == exp[it].marks }
        if (ok == need.size) return 1.0
        if (!typedAny) return 0.25
        val f = ok.toDouble() / need.size
        return if (f >= 0.7) 0.75 else if (f >= 0.4) 0.5 else 0.25
    }

    fun itemScore(student: String, expected: String, strict: Boolean): Double {
        if (student.isBlank()) return 0.0
        if (strict) return strictScore(student, expected)
        return variants(mainPart(expected)).maxOf { variantScore(student, it) }
    }

    fun words(s: String): Int = wordCount(s)
}

object ExamGrader {
    private val STRICT_RE = Regex("اضبط|اشكل|أشكل|بالشكل|الضبط")
    const val CELL_SEPARATOR = "\u001F"

    /** Cells the student has to fill in a table question, in reading order. */
    fun blankCells(table: ExamTable): List<Pair<Int, Int>> {
        val result = ArrayList<Pair<Int, Int>>()
        val cols = table.headers.size.coerceAtLeast(1)
        table.rows.forEachIndexed { r, row ->
            for (c in 0 until cols) {
                if (row.getOrElse(c) { "" }.isBlank()) result.add(r to c)
            }
        }
        return result
    }

    private fun roundQuarter(x: Double): Double = Math.round(x * 4.0) / 4.0

    private fun isNoteItem(label: String, value: String): Boolean =
        value.isBlank() || label.contains("ملاحظة") || value.startsWith("يُقبل") || value.startsWith("تُقبل")

    fun gradeQuestion(q: ExamQuestion, answer: String): QuestionGrade {
        val strict = STRICT_RE.containsMatchIn(q.prompt)
        val table = q.table
        val items = ArrayList<GradedItem>()

        if (table != null && table.answerRows.isNotEmpty()) {
            val cells = blankCells(table)
            val parts = if (answer.isEmpty()) emptyList() else answer.split(CELL_SEPARATOR)
            cells.forEachIndexed { idx, cell ->
                val expected = table.answerRows.getOrNull(cell.first)?.getOrNull(cell.second).orEmpty()
                val student = parts.getOrElse(idx) { "" }
                val header = table.headers.getOrElse(cell.second) { "" }
                val rowLabel = table.rows.getOrNull(cell.first)?.firstOrNull().orEmpty()
                val label = if (cell.second == 0 || rowLabel.isBlank()) header else "$rowLabel - $header"
                items.add(GradedItem(label, expected, ArabicMatcher.itemScore(student, expected, strict), student))
            }
        } else {
            val all = q.answerGroups.flatten()
            val usable = all.filter { !isNoteItem(it.label, it.value) }
            if (usable.isEmpty()) {
                // Opinion-style question: any reasoned answer is accepted; grade by completeness.
                val words = ArabicMatcher.words(answer)
                val fraction = if (words >= 8) 1.0 else if (words >= 4) 0.5 else 0.0
                val expected = all.firstOrNull()?.value.orEmpty()
                return QuestionGrade(
                    earned = roundQuarter(q.points * fraction),
                    max = q.points,
                    mode = GradeMode.OPINION,
                    items = listOf(GradedItem(all.firstOrNull()?.label.orEmpty(), expected, fraction, answer))
                )
            }
            usable.forEach { item ->
                items.add(GradedItem(item.label, item.value, ArabicMatcher.itemScore(answer, item.value, strict), ""))
            }
        }

        if (items.isEmpty()) {
            return QuestionGrade(0.0, q.points, GradeMode.AUTO, emptyList())
        }
        val fraction = items.sumOf { it.fraction } / items.size
        return QuestionGrade(roundQuarter(q.points * fraction), q.points, GradeMode.AUTO, items)
    }

    fun gradeExam(exam: ExamData, answers: Map<String, String>, ratings: Map<String, Float>): ExamGrade {
        val questionGrades = LinkedHashMap<String, QuestionGrade>()
        val sections = ArrayList<SectionGrade>()
        for (s in exam.sections) {
            var earned = 0.0
            for (q in s.questions) {
                val key = "${exam.id}_${s.id}_q${q.no}"
                val g = gradeQuestion(q, answers[key].orEmpty())
                questionGrades["${s.id}_${q.no}"] = g
                earned += g.earned
            }
            var pending = false
            val hasWriting = s.id == "C" || s.topic != null || s.topics.isNotEmpty()
            if (hasWriting && s.rubric.isNotEmpty()) {
                val writing = answers["${exam.id}_${s.id}_writing"].orEmpty()
                if (writing.isNotBlank()) {
                    s.rubric.forEachIndexed { i, r ->
                        val rating = ratings["${exam.id}_${s.id}_r$i"]
                        if (rating == null) pending = true else earned += r.points * rating
                    }
                }
            }
            sections.add(SectionGrade(s.id, s.title, earned, s.points, pending))
        }
        return ExamGrade(
            total = sections.sumOf { it.earned },
            max = exam.declaredTotal.toDouble(),
            sections = sections,
            questions = questionGrades
        )
    }

    /** Score text with western digits, e.g. 14.5 or 12. */
    fun formatScore(x: Double): String =
        java.text.DecimalFormat("0.##", java.text.DecimalFormatSymbols(java.util.Locale.US)).format(x)

    /** Duration text -> seconds, or null for flexible/unspecified. */
    fun durationSeconds(duration: String): Int? {
        val d = duration.trim()
        return when {
            d.contains("ساعتان") || d.contains("ساعتين") -> 7200
            d.contains("ساعة ونصف") -> 5400
            d.contains("ساعة") -> 3600
            d.contains("45") -> 2700
            d.contains("30") || d.contains("نصف ساعة") -> 1800
            else -> null
        }
    }
}
