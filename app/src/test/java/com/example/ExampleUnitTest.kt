package com.example

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ExampleUnitTest {

    private fun getAssetFile(filename: String): File {
        val candidates = listOf(
            File("src/main/assets/$filename"),
            File("app/src/main/assets/$filename"),
            File("../app/src/main/assets/$filename")
        )
        val file = candidates.firstOrNull { it.exists() }
        assertNotNull("ملف الأصول $filename غير موجود في المسارات المفحوصة", file)
        return file!!
    }

    /**
     * 1) تحميل lessons.json وتحليله مع التحقق من الحقول الإلزامية لكل درس:
     * id و component و semester و unit و order و title، وعدم تكرار id.
     */
    @Test
    fun testLessonsJsonIntegrityAndRequiredFields() {
        val file = getAssetFile("lessons.json")
        val content = file.readText()
        val root = JSONObject(content)

        assertTrue("يجب أن يحتوي ملف lessons.json على مصفوفة lessons", root.has("lessons"))
        val lessonsArr = root.getJSONArray("lessons")
        val totalLessons = lessonsArr.length()
        assertEquals("عدد الدروس المقررة في lessons.json يجب أن يكون 21 درساً", 21, totalLessons)

        val seenIds = mutableSetOf<String>()
        val requiredFields = listOf("id", "component", "semester", "unit", "order", "title")

        for (i in 0 until totalLessons) {
            val lesson = lessonsArr.getJSONObject(i)

            // التحقق من الحقول الإلزامية
            for (field in requiredFields) {
                assertTrue("الدرس في الفهرس $i ينقصه الحقل الإلزامي '$field'", lesson.has(field))
                val value = lesson.get(field)
                if (value is String) {
                    assertTrue("الحقل '$field' في الدرس $i لا يجوز أن يكون فارغاً", value.isNotBlank())
                }
            }

            val lessonId = lesson.getString("id")
            assertFalse("معرّف الدرس مكرر: $lessonId", seenIds.contains(lessonId))
            seenIds.add(lessonId)

            // التحقق من صحة الوحدة (1 أو 2 أو 3 فقط)
            val unit = lesson.getInt("unit")
            assertTrue("الوحدة في الدرس $lessonId يجب أن تكون بين 1 و 3", unit in 1..3)

            // التحقق من صحة المكوّن
            val comp = lesson.getString("component")
            assertTrue(
                "المكوّن '$comp' في الدرس $lessonId غير معروف",
                comp in listOf("texts", "language", "writing", "exams")
            )
        }
    }

    /**
     * 2) اختبار كل سؤال mcq في التمارين:
     * answerIndex بين 0 وعدد الخيارات - 1، وخياراته غير مكررة.
     */
    @Test
    fun testMcqExercisesValidation() {
        val file = getAssetFile("lessons.json")
        val root = JSONObject(file.readText())
        val lessonsArr = root.getJSONArray("lessons")

        var mcqCount = 0
        for (i in 0 until lessonsArr.length()) {
            val lesson = lessonsArr.getJSONObject(i)
            val lessonId = lesson.getString("id")
            val exercises = lesson.optJSONArray("exercises") ?: JSONArray()

            for (e in 0 until exercises.length()) {
                val ex = exercises.getJSONObject(e)
                if (ex.optString("type") == "mcq") {
                    mcqCount++
                    assertTrue("سؤال MCQ في الدرس $lessonId ينقصه options", ex.has("options"))
                    assertTrue("سؤال MCQ في الدرس $lessonId ينقصه answer", ex.has("answer"))

                    val optionsArr = ex.getJSONArray("options")
                    val optionsList = mutableListOf<String>()
                    for (o in 0 until optionsArr.length()) {
                        optionsList.add(optionsArr.getString(o).trim())
                    }

                    val answerIdx = ex.getInt("answer")
                    assertTrue(
                        "في الدرس $lessonId السؤال #${e + 1}: answer ($answerIdx) خارج المجال (0..${optionsList.size - 1})",
                        answerIdx in 0 until optionsList.size
                    )

                    val uniqueOptions = optionsList.toSet()
                    assertEquals(
                        "في الدرس $lessonId السؤال #${e + 1}: خيارات MCQ مكررة: $optionsList",
                        optionsList.size,
                        uniqueOptions.size
                    )
                }
            }
        }
        assertTrue("يجب التحقق من وجود أسئلة MCQ في الدروس", mcqCount > 0)
    }

    /**
     * 3) اختبار كل سؤال order_words:
     * answer تحتوي الكلمات نفسها الموجودة في words، وأن answer ليس مطابقاً لـ words.
     */
    @Test
    fun testOrderWordsExercisesValidation() {
        val file = getAssetFile("lessons.json")
        val root = JSONObject(file.readText())
        val lessonsArr = root.getJSONArray("lessons")

        var orderCount = 0
        for (i in 0 until lessonsArr.length()) {
            val lesson = lessonsArr.getJSONObject(i)
            val lessonId = lesson.getString("id")
            val exercises = lesson.optJSONArray("exercises") ?: JSONArray()

            for (e in 0 until exercises.length()) {
                val ex = exercises.getJSONObject(e)
                if (ex.optString("type") == "order_words") {
                    orderCount++
                    assertTrue("سؤال order_words في الدرس $lessonId ينقصه words", ex.has("words"))
                    assertTrue("سؤال order_words في الدرس $lessonId ينقصه answer", ex.has("answer"))

                    val wordsArr = ex.getJSONArray("words")
                    val answerArr = ex.getJSONArray("answer")

                    val wordsList = (0 until wordsArr.length()).map { wordsArr.getString(it).trim() }
                    val answerList = (0 until answerArr.length()).map { answerArr.getString(it).trim() }

                    assertEquals(
                        "في الدرس $lessonId تمرين order_words #${e + 1}: طول words لا يطابق answer",
                        wordsList.size,
                        answerList.size
                    )

                    assertEquals(
                        "في الدرس $lessonId تمرين order_words #${e + 1}: الكلمات في answer لا تطابق words",
                        wordsList.sorted(),
                        answerList.sorted()
                    )

                    assertFalse(
                        "في الدرس $lessonId تمرين order_words #${e + 1}: مصفوفة words مطابقة للإجابة مسبقاً وغير مبعثرة",
                        wordsList == answerList
                    )
                }
            }
        }
        assertTrue("يجب التحقق من وجود تمارين order_words", orderCount > 0)
    }

    /**
     * 4) اختبار كل سؤال true_false:
     * له answer منطقي (boolean).
     */
    @Test
    fun testTrueFalseExercisesValidation() {
        val file = getAssetFile("lessons.json")
        val root = JSONObject(file.readText())
        val lessonsArr = root.getJSONArray("lessons")

        var tfCount = 0
        for (i in 0 until lessonsArr.length()) {
            val lesson = lessonsArr.getJSONObject(i)
            val lessonId = lesson.getString("id")
            val exercises = lesson.optJSONArray("exercises") ?: JSONArray()

            for (e in 0 until exercises.length()) {
                val ex = exercises.getJSONObject(e)
                if (ex.optString("type") == "true_false") {
                    tfCount++
                    assertTrue("سؤال true_false في الدرس $lessonId ينقصه حقل answer", ex.has("answer"))
                    val rawAns = ex.get("answer")
                    assertTrue(
                        "في الدرس $lessonId سؤال true_false #${e + 1}: الإجابة يجب أن تكون منطقية (true/false) وليست $rawAns",
                        rawAns is Boolean
                    )
                }
            }
        }
        assertTrue("يجب التحقق من وجود أسئلة true_false", tfCount > 0)
    }

    /**
     * 5) التحقق من كل ملفات الامتحانات (exam-s1-*.json و local-exams-models.json):
     * - مجموع نقط الأقسام يساوي declared_total.
     * - مجموع أسئلة كل قسم يساوي نقطته.
     * - مجموع معايير rubric في القسم C يساوي نقط القسم C.
     * - لا يحمل أي امتحان id مكرراً.
     */
    @Test
    fun testAllExamsTotalPointsAndRubricsValidation() {
        val individualExamFiles = listOf(
            "exam-s1-unit1-assessment.json",
            "exam-s1-unit2-assessment.json",
            "exam-s1-unit3-assessment.json",
            "exam-s1-general-assessment.json",
            "exam-s1-homework-1.json"
        )

        val allExamsList = mutableListOf<JSONObject>()
        for (filename in individualExamFiles) {
            val file = getAssetFile(filename)
            allExamsList.add(JSONObject(file.readText()))
        }

        // امتحانات الملف المجمع
        val localFile = getAssetFile("local-exams-models.json")
        val localRoot = JSONObject(localFile.readText())
        val localExamsArr = localRoot.getJSONArray("exams")
        for (i in 0 until localExamsArr.length()) {
            allExamsList.add(localExamsArr.getJSONObject(i))
        }

        assertEquals("إجمالي عدد الامتحانات يجب أن يكون 11 امتحاناً", 11, allExamsList.size)

        val seenExamIds = mutableSetOf<String>()
        for (exam in allExamsList) {
            val examId = exam.getString("id")
            assertFalse("معرّف الامتحان مكرر: $examId", seenExamIds.contains(examId))
            seenExamIds.add(examId)

            val declaredTotal = exam.getInt("declared_total")
            assertEquals("declared_total يجب أن يساوي 20 في $examId", 20, declaredTotal)

            val sectionsArr = exam.getJSONArray("sections")
            var sectionsSum = 0.0

            for (s in 0 until sectionsArr.length()) {
                val section = sectionsArr.getJSONObject(s)
                val sectionId = section.getString("id")
                val sectionPoints = section.getDouble("points")
                sectionsSum += sectionPoints

                // التحقق من مجموع أسئلة القسم
                val questionsArr = section.optJSONArray("questions")
                if (questionsArr != null && questionsArr.length() > 0) {
                    var qSum = 0.0
                    for (q in 0 until questionsArr.length()) {
                        qSum += questionsArr.getJSONObject(q).getDouble("points")
                    }
                    assertEquals(
                        "في الامتحان $examId القسم $sectionId: مجموع نقط الأسئلة ($qSum) يجب أن يساوي نقط القسم ($sectionPoints)",
                        sectionPoints,
                        qSum,
                        0.001
                    )
                }

                // التحقق من معايير rubric في القسم C
                if (sectionId == "C") {
                    val rubricArr = section.optJSONArray("rubric")
                    if (rubricArr != null && rubricArr.length() > 0) {
                        var rubricSum = 0.0
                        for (r in 0 until rubricArr.length()) {
                            rubricSum += rubricArr.getJSONObject(r).getDouble("points")
                        }
                        assertEquals(
                            "في الامتحان $examId القسم C: مجموع معايير rubric ($rubricSum) يجب أن يساوي نقط القسم ($sectionPoints)",
                            sectionPoints,
                            rubricSum,
                            0.001
                        )
                    }
                }
            }

            assertEquals(
                "في الامتحان $examId: مجموع نقط الأقسام ($sectionsSum) يجب أن يساوي declared_total ($declaredTotal)",
                declaredTotal.toDouble(),
                sectionsSum,
                0.001
            )
        }
    }
}
