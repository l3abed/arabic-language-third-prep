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
        assertTrue("Contains lang-s1-u1-active-participle", content.contains("lang-s1-u1-active-participle"))
        assertTrue("Contains lang-s1-u1-passive-participle", content.contains("lang-s1-u1-passive-participle"))
        assertTrue("Contains texts-s1-u1-kalim-allah", content.contains("texts-s1-u1-kalim-allah"))
        assertTrue("Contains texts-s1-u1-khisal-muslim", content.contains("texts-s1-u1-khisal-muslim"))
        assertTrue("Contains texts-s1-u1-islam-human-rights", content.contains("texts-s1-u1-islam-human-rights"))
        assertTrue("Contains texts-s1-u1-hadith-al-rooh", content.contains("texts-s1-u1-hadith-al-rooh"))
        assertTrue("Contains writing-s1-u1-diary", content.contains("writing-s1-u1-diary"))
    }
}
