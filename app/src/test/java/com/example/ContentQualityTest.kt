package com.example

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Guards the quality of the exercise bank so edits to lessons.json cannot reintroduce easy guessing. */
class ContentQualityTest {

    private fun lessons(): JSONArray {
        val file = listOf(
            File("src/main/assets/lessons.json"),
            File("app/src/main/assets/lessons.json"),
            File("../app/src/main/assets/lessons.json")
        ).firstOrNull { it.exists() }
        assertNotNull("lessons.json not found", file)
        return JSONObject(file!!.readText()).getJSONArray("lessons")
    }

    private inline fun forEachExercise(action: (lessonId: String, index: Int, e: JSONObject) -> Unit) {
        val all = lessons()
        for (i in 0 until all.length()) {
            val lesson = all.getJSONObject(i)
            val ex = lesson.getJSONArray("exercises")
            for (j in 0 until ex.length()) action(lesson.getString("id"), j, ex.getJSONObject(j))
        }
    }

    @Test
    fun trueFalseAnswersAreBalanced() {
        var total = 0
        var trues = 0
        forEachExercise { _, _, e ->
            if (e.getString("type") == "true_false") {
                total++
                if (e.getBoolean("answer")) trues++
            }
        }
        val share = trues.toDouble() / total
        assertTrue("true/false share of 'true' is $share (expected between 0.40 and 0.60)", share in 0.40..0.60)
    }

    @Test
    fun mcqOptionsAreDistinctAndAnswerIndexIsValid() {
        forEachExercise { id, i, e ->
            if (e.getString("type") == "mcq") {
                val options = e.getJSONArray("options")
                val set = HashSet<String>()
                for (k in 0 until options.length()) set.add(options.getString(k).trim())
                assertEquals("$id[$i] has duplicate options", options.length(), set.size)
                val answer = e.getInt("answer")
                assertTrue("$id[$i] answer index out of range", answer in 0 until options.length())
            }
        }
    }

    @Test
    fun newExerciseTypesAreWellFormed() {
        forEachExercise { id, i, e ->
            when (e.getString("type")) {
                "fill_blank" -> {
                    assertTrue("$id[$i] fill_blank needs ____", e.getString("sentence").contains("____"))
                    assertTrue("$id[$i] fill_blank answer empty", e.getString("answer").isNotBlank())
                    val opts = e.optJSONArray("options")
                    if (opts != null && opts.length() > 0) {
                        val list = (0 until opts.length()).map { opts.getString(it) }
                        assertTrue("$id[$i] answer must be one of the options", list.any { it.trim() == e.getString("answer").trim() })
                    }
                }
                "matching" -> {
                    val pairs = e.getJSONArray("pairs")
                    assertTrue("$id[$i] matching needs 2+ pairs", pairs.length() >= 2)
                    val rights = (0 until pairs.length()).map { pairs.getJSONObject(it).getString("right") }
                    assertEquals("$id[$i] matching rights must be unique", rights.size, rights.toSet().size)
                }
                "short_answer" -> assertTrue("$id[$i] short_answer needs answer", e.getString("answer").isNotBlank())
            }
        }
    }

    @Test
    fun orderWordsAlternativesUseTheSameWords() {
        forEachExercise { id, i, e ->
            if (e.getString("type") == "order_words") {
                fun sorted(a: JSONArray) = (0 until a.length()).map { a.getString(it) }.sorted()
                val words = sorted(e.getJSONArray("words"))
                assertEquals("$id[$i] answer", words, sorted(e.getJSONArray("answer")))
                val alts = e.optJSONArray("alternatives") ?: JSONArray()
                for (k in 0 until alts.length()) {
                    assertEquals("$id[$i] alternative $k", words, sorted(alts.getJSONArray(k)))
                }
            }
        }
    }
}
