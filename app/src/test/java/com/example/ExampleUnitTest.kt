package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ExampleUnitTest {
    @Test
    fun testLessonsJsonVersion9Structure() {
        val jsonFile = File("src/main/assets/lessons.json")
        val file = if (jsonFile.exists()) jsonFile else File("app/src/main/assets/lessons.json")
        assertTrue("lessons.json must exist", file.exists())
        val content = file.readText()

        assertFalse("JSON must not contain shart_lesson", content.contains("shart_lesson"))

        // Extract lessons block
        val lessonsBlock = content.substringAfter("\"lessons\":").substringBeforeLast("]")

        // Each lesson has an "order" integer
        val orderMatches = Regex("\"order\"\\s*:\\s*(\\d+)").findAll(lessonsBlock).toList()
        assertEquals("Total lessons must be exactly 21", 21, orderMatches.size)

        // Find all unit fields in lessons block
        val unitMatches = Regex("\"unit\"\\s*:\\s*(\\d+)").findAll(lessonsBlock).map { it.groupValues[1].toInt() }.toList()
        assertEquals("Total lessons unit count must be 21", 21, unitMatches.size)

        val unitCounts = unitMatches.groupingBy { it }.eachCount()
        assertEquals("Unit 1 must have 7 lessons", 7, unitCounts[1] ?: 0)
        assertEquals("Unit 2 must have 7 lessons", 7, unitCounts[2] ?: 0)
        assertEquals("Unit 3 must have 7 lessons", 7, unitCounts[3] ?: 0)
        assertFalse("Unit 0 must not exist", unitCounts.containsKey(0))
        assertFalse("Unit 5 must not exist", unitCounts.containsKey(5))

        // Check key Unit 2 lessons
        assertTrue("Contains zaman/makan lesson", content.contains("language-s1-u2-zaman-makan") || content.contains("lang-s1-u2-zaman-makan") || content.contains("أسماء الزمان والمكان"))
        assertTrue("Contains ism ala lesson", content.contains("language-s1-u2-ism-ala") || content.contains("lang-s1-u2-ism-ala") || content.contains("اسم الآلة"))
        assertTrue("Contains rihla description writing lesson", content.contains("writing-s1-u2-travel-description") || content.contains("وصف رحلة"))

        // Check key Unit 3 lessons
        assertTrue("Contains nisba lesson", content.contains("language-s1-u3-nisba") || content.contains("النسبة"))
        assertTrue("Contains maajim lesson", content.contains("language-s1-u3-maajim") || content.contains("المعاجم"))
        assertTrue("Contains comment/critique writing lesson", content.contains("writing-s1-u3-comment-critique") || content.contains("التعقيب والتعليق"))
    }

    @Test
    fun testAllFiveExamFiles() {
        val examFiles = listOf(
            "exam-s1-unit1-assessment.json",
            "exam-s1-unit2-assessment.json",
            "exam-s1-unit3-assessment.json",
            "exam-s1-general-assessment.json",
            "exam-s1-homework-1.json"
        )

        for (filename in examFiles) {
            val jsonFile = File("src/main/assets/$filename")
            val file = if (jsonFile.exists()) jsonFile else File("app/src/main/assets/$filename")
            assertTrue("Exam file $filename must exist", file.exists())
            val content = file.readText()

            val declaredTotalMatch = Regex("\"declared_total\"\\s*:\\s*(\\d+)").find(content)
            assertTrue("Must contain declared_total in $filename", declaredTotalMatch != null)
            assertEquals("Declared total must be 20 for $filename", 20, declaredTotalMatch!!.groupValues[1].toInt())

            assertTrue("Must contain passage in $filename", content.contains("\"passage\""))
            assertTrue("Must contain sections in $filename", content.contains("\"sections\""))
            assertTrue("Must contain answer_groups or rubric in $filename", content.contains("answer_groups") || content.contains("rubric"))
        }
    }

    @Test
    fun testLocalExamsModelsJsonStructure() {
        val jsonFile = File("src/main/assets/local-exams-models.json")
        val file = if (jsonFile.exists()) jsonFile else File("app/src/main/assets/local-exams-models.json")
        assertTrue("local-exams-models.json must exist", file.exists())
        val content = file.readText()

        val localExamIds = listOf(
            "local-exam-2018-2019-tolerance",
            "local-exam-tanseefet-2020-islam",
            "local-exam-tazart-2025-body-health",
            "local-exam-yousfia-2022-human",
            "local-exam-tanseefet-2025-hollywood",
            "local-exam-ibn-battuta-2022-2023"
        )
        for (examId in localExamIds) {
            assertTrue("local-exams-models.json must contain $examId", content.contains(examId))
        }

        val declaredTotals = Regex("\"declared_total\"\\s*:\\s*(\\d+)").findAll(content).map { it.groupValues[1].toInt() }.toList()
        assertEquals("Must have 6 declared_total entries", 6, declaredTotals.size)
        assertTrue("All declared_total must be 20", declaredTotals.all { it == 20 })

        val sourceCount = Regex("\"source\"\\s*:\\s*\"").findAll(content).count()
        assertEquals("All 6 exams must have a source", 6, sourceCount)

        assertTrue("Must contain section A", content.contains("\"id\": \"A\""))
        assertTrue("Must contain section B", content.contains("\"id\": \"B\""))
        assertTrue("Must contain section C", content.contains("\"id\": \"C\""))
        assertTrue("Must contain note_for_teacher", content.contains("note_for_teacher"))
    }
}
