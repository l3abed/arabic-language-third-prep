package com.example

import com.example.model.AnswerItem
import com.example.model.ExamQuestion
import com.example.model.ExamTable
import com.example.model.GradeMode
import com.example.util.ArabicMatcher
import com.example.util.ExamGrader
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ExamGraderTest {

    @Test
    fun normalizationIgnoresTashkeelAndHamzaForms() {
        assertEquals(ArabicMatcher.norm("مُكْرِمٌ"), ArabicMatcher.norm("مكرم"))
        assertEquals(ArabicMatcher.norm("إِكْرَاه"), ArabicMatcher.norm("اكراه"))
    }

    @Test
    fun singleWordAnswersMatchRegardlessOfDiacritics() {
        assertEquals(1.0, ArabicMatcher.itemScore("مسبح", "مَسْبَح", false), 0.0)
        assertEquals(0.0, ArabicMatcher.itemScore("مسجد", "مَسْبَح", false), 0.0)
        assertEquals(0.0, ArabicMatcher.itemScore("", "مَسْبَح", false), 0.0)
    }

    @Test
    fun alternativesAndExplanationsAreHandled() {
        assertEquals(1.0, ArabicMatcher.itemScore("ساعوي", "ساعيّ أو ساعَويّ", false), 0.0)
        assertEquals(1.0, ArabicMatcher.itemScore("قائل", "قائِلٌ (بقلب حرف العلة همزة)", false), 0.0)
    }

    @Test
    fun strictModeComparesDiacritics() {
        assertEquals(1.0, ArabicMatcher.strictScore("مَسارِ", "مَسارِ"), 0.0)
        assertEquals(0.25, ArabicMatcher.strictScore("مسار", "مَسارِ"), 0.0)
        assertEquals(0.5, ArabicMatcher.strictScore("مَسارَ", "مَسارِ"), 0.0)
        assertEquals(0.0, ArabicMatcher.strictScore("كتاب", "مَسارِ"), 0.0)
    }

    @Test
    fun tableQuestionIsGradedCellByCell() {
        val q = ExamQuestion(
            no = "1",
            points = 1.0,
            prompt = "انسب إلى الكلمتين",
            table = ExamTable(
                headers = listOf("الكلمة", "النسبة"),
                rows = listOf(listOf("فاس", ""), listOf("حضارة", "")),
                answerRows = listOf(listOf("فاس", "فاسيّ"), listOf("حضارة", "حضاريّ"))
            )
        )
        val sep = ExamGrader.CELL_SEPARATOR
        assertEquals(1.0, ExamGrader.gradeQuestion(q, "فاسي${sep}حضاري").earned, 0.0)
        assertEquals(0.5, ExamGrader.gradeQuestion(q, "فاسي${sep}مدني").earned, 0.0)
        assertEquals(0.0, ExamGrader.gradeQuestion(q, "").earned, 0.0)
    }

    @Test
    fun opinionQuestionsAreGradedByCompleteness() {
        val q = ExamQuestion(
            no = "8",
            points = 1.0,
            prompt = "أبد رأيك",
            answerGroups = listOf(listOf(AnswerItem("الرأي", "يُقبل أي رأي معلل")))
        )
        val full = ExamGrader.gradeQuestion(q, "أرى أن الصحة أهم ما يملكه الإنسان لأنها أساس العمل والعطاء")
        assertEquals(GradeMode.OPINION, full.mode)
        assertEquals(1.0, full.earned, 0.0)
        assertEquals(0.0, ExamGrader.gradeQuestion(q, "نعم").earned, 0.0)
    }

    @Test
    fun durationTextIsParsed() {
        assertEquals(7200, ExamGrader.durationSeconds("ساعتان"))
        assertEquals(3600, ExamGrader.durationSeconds("ساعة"))
        assertEquals(null, ExamGrader.durationSeconds("مرنة"))
    }

    /** Every table in the exam files must have rows and answers with the same number of columns. */
    @Test
    fun allExamTablesAreConsistent() {
        val files = listOf(
            "exam-s1-unit1-assessment.json", "exam-s1-unit2-assessment.json", "exam-s1-unit3-assessment.json",
            "exam-s1-general-assessment.json", "exam-s1-homework-1.json",
            "exam-s2-unit4-assessment.json", "exam-s2-unit5-assessment.json", "exam-s2-unit6-assessment.json",
            "exam-s2-general-assessment.json"
        )
        var tables = 0
        for (name in files) {
            val file = listOf(
                File("src/main/assets/$name"), File("app/src/main/assets/$name"), File("../app/src/main/assets/$name")
            ).firstOrNull { it.exists() }
            assertNotNull("missing $name", file)
            val sections: JSONArray = JSONObject(file!!.readText()).getJSONArray("sections")
            for (i in 0 until sections.length()) {
                val qs = sections.getJSONObject(i).optJSONArray("questions") ?: continue
                for (j in 0 until qs.length()) {
                    val table = qs.getJSONObject(j).optJSONObject("table") ?: continue
                    tables++
                    val cols = table.getJSONArray("headers").length()
                    val rows = table.getJSONArray("rows")
                    val answers = table.getJSONArray("answer_rows")
                    assertEquals("$name rows vs answers", rows.length(), answers.length())
                    for (r in 0 until rows.length()) {
                        assertEquals("$name row $r", cols, rows.getJSONArray(r).length())
                        assertEquals("$name answer row $r", cols, answers.getJSONArray(r).length())
                    }
                }
            }
        }
        assertTrue("expected at least one table question", tables > 0)
    }
}
