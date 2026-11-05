package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ExampleUnitTest {
    @Test
    fun testLessonsAssetFileValid() {
        val jsonFile = File("src/main/assets/lessons.json")
        val content = if (jsonFile.exists()) jsonFile.readText() else File("app/src/main/assets/lessons.json").readText()

        assertFalse("JSON must not contain shart_lesson", content.contains("shart_lesson"))
        // Unit 1 (7 lessons)
        assertTrue("Contains lang-s1-u1-active-participle", content.contains("lang-s1-u1-active-participle"))
        assertTrue("Contains lang-s1-u1-passive-participle", content.contains("lang-s1-u1-passive-participle"))
        assertTrue("Contains texts-s1-u1-kalim-allah", content.contains("texts-s1-u1-kalim-allah"))
        assertTrue("Contains texts-s1-u1-khisal-muslim", content.contains("texts-s1-u1-khisal-muslim"))
        assertTrue("Contains texts-s1-u1-islam-human-rights", content.contains("texts-s1-u1-islam-human-rights"))
        assertTrue("Contains texts-s1-u1-hadith-al-rooh", content.contains("texts-s1-u1-hadith-al-rooh"))
        assertTrue("Contains writing-s1-u1-diary", content.contains("writing-s1-u1-diary"))

        // Unit 3 (5 lessons)
        assertTrue("Contains texts-s1-u3-hadara-maghribiya", content.contains("texts-s1-u3-hadara-maghribiya"))
        assertTrue("Contains texts-s1-u3-internet", content.contains("texts-s1-u3-internet"))
        assertTrue("Contains texts-s1-u3-ala-bisat-al-rih", content.contains("texts-s1-u3-ala-bisat-al-rih"))
        assertTrue("Contains language-s1-u3-nisba", content.contains("language-s1-u3-nisba"))
        assertTrue("Contains language-s1-u3-maajim", content.contains("language-s1-u3-maajim"))
    }

    @Test
    fun testExamJsonValid() {
        val jsonFile = File("src/main/assets/exam-s1-homework-1.json")
        val content = if (jsonFile.exists()) jsonFile.readText() else File("app/src/main/assets/exam-s1-homework-1.json").readText()

        assertTrue(content.contains("exam-s1-homework-1"))
        assertTrue(content.contains("التَّسَامُحُ") || content.contains("التسامح"))
        assertTrue(content.contains("\"id\": \"A\"") || content.contains("\"id\":\"A\""))
        assertTrue(content.contains("\"id\": \"B\"") || content.contains("\"id\":\"B\""))
        assertTrue(content.contains("\"id\": \"C\"") || content.contains("\"id\":\"C\""))
        assertTrue(content.contains("answer_groups"))
    }
}
