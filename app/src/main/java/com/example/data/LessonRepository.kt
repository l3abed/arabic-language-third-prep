package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.CardItem
import com.example.model.ComponentInfo
import com.example.model.ExamData
import com.example.model.ExamPassage
import com.example.model.ExamQuestion
import com.example.model.ExamSection
import com.example.model.ExampleEntry
import com.example.model.ExerciseItem
import com.example.model.IrabStep
import com.example.model.KeyValuePair
import com.example.model.Lesson
import com.example.model.RubricItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class UserSettings(
    val fontScale: Float = 1.0f,
    val isDarkMode: Boolean = false,
    val showTashkeel: Boolean = true,
    val speechRate: Float = 0.9f
)

class LessonRepository(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("arabic_study_prefs", Context.MODE_PRIVATE)

    private val _lessons = MutableStateFlow<List<Lesson>>(emptyList())
    val lessons: StateFlow<List<Lesson>> = _lessons.asStateFlow()

    private val _completedLessons = MutableStateFlow<Set<String>>(emptySet())
    val completedLessons: StateFlow<Set<String>> = _completedLessons.asStateFlow()

    private val _settings = MutableStateFlow(UserSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun cleanDeprecatedData() {
        val editor = prefs.edit()

        // 1) Remove shart_lesson from completed lessons set
        val currentCompleted = prefs.getStringSet("completed_lessons_set", emptySet())
        if (currentCompleted != null && currentCompleted.contains("shart_lesson")) {
            val updated = currentCompleted.toMutableSet().apply { remove("shart_lesson") }
            editor.putStringSet("completed_lessons_set", updated)
        }

        // 2) Remove shart_lesson from last_studied_lesson_id
        val lastStudied = prefs.getString("last_studied_lesson_id", null)
        if (lastStudied == "shart_lesson") {
            editor.remove("last_studied_lesson_id")
        }

        // 3) Remove any preference keys belonging to shart_lesson
        editor.remove("lesson_step_shart_lesson")
        editor.remove("score_shart_lesson_val")
        editor.remove("score_shart_lesson_total")
        editor.apply()

        // 4) Delete outdated custom_lessons.json if it exists so app strictly loads new lessons.json
        val customFile = File(context.filesDir, "custom_lessons.json")
        if (customFile.exists()) {
            customFile.delete()
        }
    }

    private fun loadSettings() {
        val fontScale = prefs.getFloat("setting_font_scale", 1.0f)
        val isDarkMode = prefs.getBoolean("setting_dark_mode", false)
        val showTashkeel = prefs.getBoolean("setting_show_tashkeel", true)
        val speechRate = prefs.getFloat("setting_speech_rate", 0.9f)
        _settings.value = UserSettings(fontScale, isDarkMode, showTashkeel, speechRate)
    }

    fun updateSettings(newSettings: UserSettings) {
        _settings.value = newSettings
        prefs.edit()
            .putFloat("setting_font_scale", newSettings.fontScale)
            .putBoolean("setting_dark_mode", newSettings.isDarkMode)
            .putBoolean("setting_show_tashkeel", newSettings.showTashkeel)
            .putFloat("setting_speech_rate", newSettings.speechRate)
            .apply()
    }

    private fun loadCompletedLessons() {
        val set = prefs.getStringSet("completed_lessons_set", emptySet()) ?: emptySet()
        // Ensure no deprecated IDs in memory
        _completedLessons.value = set.filter { it != "shart_lesson" }.toSet()
    }

    fun markLessonCompleted(lessonId: String) {
        if (lessonId == "shart_lesson") return
        val updated = _completedLessons.value.toMutableSet().apply { add(lessonId) }
        _completedLessons.value = updated
        prefs.edit().putStringSet("completed_lessons_set", updated).apply()
    }

    /**
     * Resume lesson logic:
     * - Returns the previously studied lesson if unfinished and belongs to the selected unit.
     * - Otherwise returns the first uncompleted lesson in the selected unit.
     * - If all completed, returns the first lesson of that unit.
     */
    fun getResumeLesson(unit: Int? = null): Lesson? {
        val allLessons = _lessons.value
        if (allLessons.isEmpty()) return null

        val lessonsInUnit = if (unit != null) {
            allLessons.filter { it.unit == unit }
        } else {
            allLessons
        }
        if (lessonsInUnit.isEmpty()) return allLessons.firstOrNull()

        val completed = _completedLessons.value
        val lastId = prefs.getString("last_studied_lesson_id", null)

        // 1) Previous unfinished lesson in this unit?
        if (!lastId.isNullOrEmpty() && lastId != "shart_lesson") {
            val lastLesson = lessonsInUnit.firstOrNull { it.id == lastId }
            if (lastLesson != null && !completed.contains(lastId)) {
                return lastLesson
            }
        }

        // 2) First uncompleted lesson in this unit
        val firstUncompleted = lessonsInUnit.firstOrNull { !completed.contains(it.id) }
        if (firstUncompleted != null) {
            return firstUncompleted
        }

        // 3) Default to first lesson of this unit
        return lessonsInUnit.firstOrNull()
    }

    fun setLastStudiedLessonId(lessonId: String) {
        if (lessonId == "shart_lesson") return
        prefs.edit().putString("last_studied_lesson_id", lessonId).apply()
    }

    fun getLessonProgress(lessonId: String): Int {
        if (lessonId == "shart_lesson") return 0
        return prefs.getInt("lesson_step_$lessonId", 0)
    }

    fun saveLessonProgress(lessonId: String, stepIndex: Int) {
        if (lessonId == "shart_lesson") return
        prefs.edit().putInt("lesson_step_$lessonId", stepIndex).apply()
    }

    fun markCardsCompleted(lessonId: String) {
        if (lessonId == "shart_lesson") return
        prefs.edit().putBoolean("cards_completed_$lessonId", true).apply()
        checkOverallCompletion(lessonId)
    }

    fun markExercisesCompleted(lessonId: String) {
        if (lessonId == "shart_lesson") return
        prefs.edit().putBoolean("exercises_completed_$lessonId", true).apply()
        checkOverallCompletion(lessonId)
    }

    private fun checkOverallCompletion(lessonId: String) {
        val cardsDone = prefs.getBoolean("cards_completed_$lessonId", false)
        val exercisesDone = prefs.getBoolean("exercises_completed_$lessonId", false)
        if (cardsDone && exercisesDone) {
            markLessonCompleted(lessonId)
        }
    }

    fun getLessonCombinedProgress(lesson: Lesson): Float {
        if (_completedLessons.value.contains(lesson.id)) return 1.0f
        val cardsDone = prefs.getBoolean("cards_completed_${lesson.id}", false)
        val exercisesDone = prefs.getBoolean("exercises_completed_${lesson.id}", false)
        if (cardsDone && exercisesDone) return 1.0f

        val step = getLessonProgress(lesson.id)
        val cardsRatio = if (lesson.cards.isNotEmpty()) {
            if (cardsDone) 0.5f else ((step + 1).toFloat() / lesson.cards.size.toFloat()) * 0.5f
        } else 0.5f

        val exercisesRatio = if (exercisesDone) 0.5f else {
            val scorePair = getExerciseScore(lesson.id)
            if (scorePair != null && scorePair.second > 0) {
                (scorePair.first.toFloat() / scorePair.second.toFloat()) * 0.5f
            } else 0.0f
        }
        return (cardsRatio + exercisesRatio).coerceIn(0.0f, 1.0f)
    }

    fun getExerciseScore(lessonId: String): Pair<Int, Int>? {
        if (lessonId == "shart_lesson") return null
        if (!prefs.contains("score_${lessonId}_val")) return null
        val score = prefs.getInt("score_${lessonId}_val", 0)
        val total = prefs.getInt("score_${lessonId}_total", 5)
        return Pair(score, total)
    }

    fun saveExerciseScore(lessonId: String, score: Int, total: Int) {
        if (lessonId == "shart_lesson") return
        prefs.edit()
            .putInt("score_${lessonId}_val", score)
            .putInt("score_${lessonId}_total", total)
            .apply()
        markExercisesCompleted(lessonId)
    }

    fun resetProgress() {
        val editor = prefs.edit()
        val allKeys = prefs.all.keys.toList()
        for (key in allKeys) {
            if (key.startsWith("lesson_step_") || key.startsWith("score_") || key == "completed_lessons_set" || key == "last_studied_lesson_id") {
                editor.remove(key)
            }
        }
        editor.apply()
        _completedLessons.value = emptySet()
    }

    // ==========================================
    // Spaced Review System (المراجعة المتباعدة للأخطاء)
    // ==========================================
    private val _mistakeReviewItems = MutableStateFlow<List<com.example.model.MistakeReviewItem>>(emptyList())
    val mistakeReviewItems: StateFlow<List<com.example.model.MistakeReviewItem>> = _mistakeReviewItems.asStateFlow()

    fun loadMistakes() {
        val raw = prefs.getString("mistake_records_json", null) ?: "[]"
        try {
            val arr = JSONArray(raw)
            val list = mutableListOf<com.example.model.MistakeReviewItem>()
            val lessonsMap = _lessons.value.associateBy { it.id }
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val lid = obj.getString("lessonId")
                val qIdx = obj.getInt("questionIndex")
                val count = obj.getInt("errorCount")
                val timestamp = obj.getLong("lastErrorTimestamp")
                val lesson = lessonsMap[lid]
                val exercise = lesson?.exercises?.getOrNull(qIdx)
                val lessonTitle = lesson?.title ?: ""
                list.add(
                    com.example.model.MistakeReviewItem(
                        id = "$lid:$qIdx",
                        lessonId = lid,
                        questionIndex = qIdx,
                        errorCount = count,
                        lastErrorTimestamp = timestamp,
                        lessonTitle = lessonTitle,
                        exercise = exercise
                    )
                )
            }
            // Sort by newest error first
            _mistakeReviewItems.value = list.sortedByDescending { it.lastErrorTimestamp }
        } catch (e: Exception) {
            e.printStackTrace()
            _mistakeReviewItems.value = emptyList()
        }
    }

    fun recordMistake(lessonId: String, questionIndex: Int) {
        val key = "$lessonId:$questionIndex"
        val raw = prefs.getString("mistake_records_json", null) ?: "[]"
        val arr = JSONArray(raw)
        val updated = JSONArray()
        var found = false
        val now = System.currentTimeMillis()

        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            if (obj.getString("lessonId") == lessonId && obj.getInt("questionIndex") == questionIndex) {
                val count = obj.getInt("errorCount") + 1
                obj.put("errorCount", count)
                obj.put("lastErrorTimestamp", now)
                updated.put(obj)
                found = true
            } else {
                updated.put(obj)
            }
        }
        if (!found) {
            val newObj = JSONObject().apply {
                put("lessonId", lessonId)
                put("questionIndex", questionIndex)
                put("errorCount", 1)
                put("lastErrorTimestamp", now)
            }
            updated.put(newObj)
        }
        prefs.edit().putString("mistake_records_json", updated.toString()).apply()
        loadMistakes()
    }

    fun recordReviewCorrect(lessonId: String, questionIndex: Int) {
        val raw = prefs.getString("mistake_records_json", null) ?: "[]"
        val arr = JSONArray(raw)
        val updated = JSONArray()

        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            if (obj.getString("lessonId") == lessonId && obj.getInt("questionIndex") == questionIndex) {
                val count = obj.getInt("errorCount") - 1
                if (count > 0) {
                    obj.put("errorCount", count)
                    updated.put(obj)
                }
                // If count <= 0, omit it from list (removed from review list)
            } else {
                updated.put(obj)
            }
        }
        prefs.edit().putString("mistake_records_json", updated.toString()).apply()
        loadMistakes()
    }

    fun clearMistakes() {
        prefs.edit().remove("mistake_records_json").apply()
        _mistakeReviewItems.value = emptyList()
    }

    // ==========================================
    // Exam History Records (سجل نتائج الامتحانات)
    // ==========================================
    private val _examHistory = MutableStateFlow<List<com.example.model.ExamHistoryRecord>>(emptyList())
    val examHistory: StateFlow<List<com.example.model.ExamHistoryRecord>> = _examHistory.asStateFlow()

    fun loadExamHistory() {
        val raw = prefs.getString("exam_history_json", null) ?: "[]"
        try {
            val arr = JSONArray(raw)
            val list = mutableListOf<com.example.model.ExamHistoryRecord>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    com.example.model.ExamHistoryRecord(
                        examId = obj.getString("examId"),
                        examTitle = obj.getString("examTitle"),
                        timestamp = obj.getLong("timestamp"),
                        declaredTotal = obj.getInt("declaredTotal")
                    )
                )
            }
            _examHistory.value = list.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            e.printStackTrace()
            _examHistory.value = emptyList()
        }
    }

    fun recordExamSubmission(examId: String, examTitle: String, declaredTotal: Int) {
        val raw = prefs.getString("exam_history_json", null) ?: "[]"
        val arr = JSONArray(raw)
        val obj = JSONObject().apply {
            put("examId", examId)
            put("examTitle", examTitle)
            put("timestamp", System.currentTimeMillis())
            put("declaredTotal", declaredTotal)
        }
        arr.put(obj)
        prefs.edit().putString("exam_history_json", arr.toString()).apply()
        loadExamHistory()
    }

    fun clearExamHistory() {
        prefs.edit().remove("exam_history_json").apply()
        _examHistory.value = emptyList()
    }

    fun loadLessons() {
        try {
            val jsonString = readAssetJson()
            val parsed = parseJson(jsonString)
            _lessons.value = parsed.sortedWith(
                compareBy({ it.semester }, { it.unit }, { it.order })
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getActiveJsonString(): String {
        return readAssetJson()
    }

    private fun readAssetJson(): String {
        return context.assets.open("lessons.json").bufferedReader().use { it.readText() }
    }

    private fun parseJson(jsonString: String): List<Lesson> {
        val root = JSONObject(jsonString)
        val lessonsArray = root.getJSONArray("lessons")
        val result = mutableListOf<Lesson>()

        for (i in 0 until lessonsArray.length()) {
            val lObj = lessonsArray.getJSONObject(i)
            val id = lObj.optString("id", "lesson_$i")
            if (id == "shart_lesson") continue // Ensure shart_lesson is never loaded

            val component = lObj.optString("component", "language")
            val semester = lObj.optInt("semester", 1)
            val unit = lObj.optInt("unit", 1)
            val order = lObj.optInt("order", 1)
            val tag = if (lObj.has("tag") && !lObj.isNull("tag")) lObj.getString("tag") else null
            val title = lObj.optString("title", "")
            val summary = lObj.optString("summary", "")

            // Parse cards based on meta.schema
            val cardsList = mutableListOf<CardItem>()
            val cardsArray = lObj.optJSONArray("cards") ?: JSONArray()
            for (c in 0 until cardsArray.length()) {
                val cObj = cardsArray.getJSONObject(c)
                val type = cObj.optString("type", "rule")
                val cardTitle = cObj.optString("title", "")

                when (type) {
                    "rule" -> {
                        val text = if (cObj.has("text") && !cObj.isNull("text")) cObj.getString("text") else null
                        val points = jsonArrayToStringList(cObj.optJSONArray("points"))
                        cardsList.add(CardItem.RuleCard(title = cardTitle, text = text, points = points))
                    }
                    "example" -> {
                        val items = mutableListOf<ExampleEntry>()
                        val itemsArray = cObj.optJSONArray("items") ?: JSONArray()
                        for (itIdx in 0 until itemsArray.length()) {
                            val itObj = itemsArray.getJSONObject(itIdx)
                            items.add(ExampleEntry(
                                text = itObj.optString("text", ""),
                                note = itObj.optString("note", "")
                            ))
                        }
                        cardsList.add(CardItem.ExampleCard(title = cardTitle, items = items))
                    }
                    "irab" -> {
                        val sentence = cObj.optString("sentence", "")
                        val tip = if (cObj.has("tip") && !cObj.isNull("tip")) cObj.getString("tip") else null
                        val steps = mutableListOf<IrabStep>()
                        val stepsArray = cObj.optJSONArray("steps") ?: JSONArray()
                        for (sIdx in 0 until stepsArray.length()) {
                            val sObj = stepsArray.getJSONObject(sIdx)
                            steps.add(IrabStep(
                                word = sObj.optString("word", ""),
                                irab = sObj.optString("irab", "")
                            ))
                        }
                        cardsList.add(CardItem.IrabCard(sentence = sentence, steps = steps, tip = tip))
                    }
                    "facts" -> {
                        val items = parseKeyValuePairs(cObj.optJSONArray("items"))
                        cardsList.add(CardItem.FactsCard(title = cardTitle, items = items))
                    }
                    "vocab" -> {
                        val items = parseKeyValuePairs(cObj.optJSONArray("items"))
                        cardsList.add(CardItem.VocabCard(title = cardTitle, items = items))
                    }
                    "summary" -> {
                        val text = cObj.optString("text", "")
                        cardsList.add(CardItem.SummaryCard(title = cardTitle, text = text))
                    }
                    "steps" -> {
                        val points = jsonArrayToStringList(cObj.optJSONArray("points"))
                        cardsList.add(CardItem.StepsCard(title = cardTitle, points = points))
                    }
                    "model" -> {
                        val text = cObj.optString("text", "")
                        val points = jsonArrayToStringList(cObj.optJSONArray("points"))
                        cardsList.add(CardItem.ModelCard(title = cardTitle, text = text, points = points))
                    }
                    "memorize" -> {
                        val points = jsonArrayToStringList(cObj.optJSONArray("points"))
                        cardsList.add(CardItem.MemorizeCard(title = cardTitle, points = points))
                    }
                    else -> {
                        val text = cObj.optString("text", "")
                        cardsList.add(CardItem.SummaryCard(title = cardTitle, text = text))
                    }
                }
            }

            // Parse exercises based on meta.schema (mcq, true_false, order_words)
            val exercisesList = mutableListOf<ExerciseItem>()
            val exercisesArray = lObj.optJSONArray("exercises") ?: JSONArray()
            for (e in 0 until exercisesArray.length()) {
                val eObj = exercisesArray.getJSONObject(e)
                val type = eObj.optString("type", "mcq")
                val explanation = eObj.optString("explanation", "")

                when (type) {
                    "mcq" -> {
                        val question = eObj.optString("question", "")
                        val options = jsonArrayToStringList(eObj.optJSONArray("options"))
                        val answerIndex = eObj.optInt("answer", 0)
                        exercisesList.add(ExerciseItem.Mcq(
                            question = question,
                            options = options,
                            answerIndex = answerIndex,
                            explanation = explanation
                        ))
                    }
                    "true_false" -> {
                        val statement = eObj.optString("statement", "")
                        val answer = eObj.optBoolean("answer", false)
                        exercisesList.add(ExerciseItem.TrueFalse(
                            statement = statement,
                            answer = answer,
                            explanation = explanation
                        ))
                    }
                    "order_words" -> {
                        val words = jsonArrayToStringList(eObj.optJSONArray("words"))
                        val answer = jsonArrayToStringList(eObj.optJSONArray("answer"))
                        val altArray = eObj.optJSONArray("alternatives") ?: JSONArray()
                        val alternatives = mutableListOf<List<String>>()
                        for (a in 0 until altArray.length()) {
                            val inner = altArray.optJSONArray(a) ?: continue
                            alternatives.add(jsonArrayToStringList(inner))
                        }
                        exercisesList.add(ExerciseItem.OrderWords(
                            words = words,
                            answer = answer,
                            explanation = explanation,
                            alternatives = alternatives
                        ))
                    }
                }
            }

            result.add(
                Lesson(
                    id = id,
                    component = component,
                    semester = semester,
                    unit = unit,
                    order = order,
                    tag = tag,
                    title = title,
                    summary = summary,
                    cards = cardsList,
                    exercises = exercisesList
                )
            )
        }
        return result
    }

    private fun parseKeyValuePairs(array: JSONArray?): List<KeyValuePair> {
        if (array == null) return emptyList()
        val list = mutableListOf<KeyValuePair>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(KeyValuePair(
                label = obj.optString("label", ""),
                value = obj.optString("value", "")
            ))
        }
        return list
    }

    private fun jsonArrayToStringList(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until array.length()) {
            list.add(array.getString(i))
        }
        return list
    }

    fun getComponentInfos(unit: Int? = null): List<ComponentInfo> {
        val allLessons = _lessons.value
        val completed = _completedLessons.value

        val filteredLessons = if (unit != null) {
            allLessons.filter { it.unit == unit }
        } else {
            allLessons
        }

        val componentDefs = listOf(
            Triple("texts", "النصوص القرائية والشعرية", "نص + شرح + أسئلة الفهم"),
            Triple("language", "الدرس اللغوي", "قاعدة + أمثلة + إعراب وتطبيق"),
            Triple("writing", "التعبير والإنشاء", "خطوات الكتابة + نماذج مُنجزة"),
            Triple("exams", "نماذج الامتحانات", "امتحانات محلية موحدة وفروض مصححة")
        )

        return componentDefs.map { (id, title, subtitle) ->
            if (id == "exams") {
                val allExamsCount = getAllExams().size
                ComponentInfo(
                    id = id,
                    title = title,
                    subtitle = subtitle,
                    totalLessons = if (allExamsCount > 0) allExamsCount else 11,
                    completedLessons = 0
                )
            } else {
                val matching = filteredLessons.filter { it.component == id }
                val total = matching.size
                val done = matching.count { completed.contains(it.id) }
                ComponentInfo(
                    id = id,
                    title = title,
                    subtitle = subtitle,
                    totalLessons = total,
                    completedLessons = done
                )
            }
        }
    }

    private val examAssetFiles = listOf(
        "exam-s1-unit1-assessment.json",
        "exam-s1-unit2-assessment.json",
        "exam-s1-unit3-assessment.json",
        "exam-s1-general-assessment.json",
        "exam-s1-homework-1.json",
        "exam-s2-unit4-assessment.json",
        "exam-s2-unit5-assessment.json",
        "exam-s2-unit6-assessment.json",
        "exam-s2-general-assessment.json"
    )

    fun getStandardExams(): List<ExamData> {
        return examAssetFiles.mapNotNull { filename ->
            try {
                val jsonString = context.assets.open(filename).bufferedReader().use { it.readText() }
                parseExamJson(jsonString)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun getLocalExams(): List<ExamData> {
        val list = try {
            val jsonString = context.assets.open("local-exams-models.json").bufferedReader().use { it.readText() }
            val root = JSONObject(jsonString)
            val examsArray = root.optJSONArray("exams") ?: JSONArray()
            val parsedList = mutableListOf<ExamData>()
            for (i in 0 until examsArray.length()) {
                val obj = examsArray.getJSONObject(i)
                parsedList.add(parseExamJsonObject(obj))
            }
            parsedList
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }

        // Chronological order based on date of placement (2018/2019 -> 2020 -> 2022 -> 2022/2023 -> 2025)
        val chronologicalDateOrder = listOf(
            "local-exam-2018-2019-tolerance",    // 2018/2019
            "local-exam-tanseefet-2020-islam",    // 2020
            "local-exam-yousfia-2022-human",     // 2022
            "local-exam-ibn-battuta-2022-2023",  // 2022/2023
            "local-exam-tazart-2025-body-health", // 2025 (يناير)
            "local-exam-tanseefet-2025-hollywood" // 2025 (يناير)
        )

        return list.sortedBy { exam ->
            val idx = chronologicalDateOrder.indexOf(exam.id)
            if (idx >= 0) idx else 99
        }
    }

    fun getAllExams(): List<ExamData> {
        val standard = getStandardExams()
        val local = getLocalExams()
        val localIds = local.map { it.id }.toSet()
        val filteredStandard = standard.filter { !localIds.contains(it.id) }
        return filteredStandard + local
    }

    fun getExamById(examId: String): ExamData? {
        return getAllExams().firstOrNull { it.id == examId }
            ?: getHomeworkExam()
    }

    fun getHomeworkExam(): ExamData? {
        return try {
            val jsonString = context.assets.open("exam-s1-homework-1.json").bufferedReader().use { it.readText() }
            parseExamJson(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseExamJson(jsonString: String): ExamData {
        return parseExamJsonObject(JSONObject(jsonString))
    }

    fun parseExamJsonObject(root: JSONObject): ExamData {
        val id = root.optString("id", "exam-s1-homework-1")
        val component = root.optString("component", "exams")
        val semester = root.optInt("semester", 1)
        val unit = if (root.has("unit") && !root.isNull("unit")) root.getInt("unit") else null
        val type = root.optString("type", "exam")
        val order = root.optInt("order", 1)
        val title = root.optString("title", "الفرض المحروس")
        val source = root.optString("source", "")
        val subject = root.optString("subject", "")
        val duration = root.optString("duration", "مرنة")
        val declaredTotal = root.optInt("declared_total", 20)

        val passageObj = root.optJSONObject("passage") ?: JSONObject()
        val passage = ExamPassage(
            title = passageObj.optString("title", ""),
            text = passageObj.optString("text", "")
        )

        val sectionsList = mutableListOf<ExamSection>()
        val sectionsArray = root.optJSONArray("sections") ?: JSONArray()
        for (s in 0 until sectionsArray.length()) {
            val sObj = sectionsArray.getJSONObject(s)
            val sId = sObj.optString("id", "")
            val sTitle = sObj.optString("title", "")
            val sPoints = sObj.optDouble("points", 0.0)
            val sTopic = if (sObj.has("topic") && !sObj.isNull("topic")) sObj.getString("topic") else null
            val sModelText = if (sObj.has("model_text") && !sObj.isNull("model_text")) sObj.getString("model_text") else null
            val sGradingNote = if (sObj.has("grading_note") && !sObj.isNull("grading_note")) sObj.getString("grading_note") else null
            val sChooseOne = sObj.optBoolean("choose_one", false)
            val sModelPoints = jsonArrayToStringList(sObj.optJSONArray("model_points"))

            val topicsList = mutableListOf<com.example.model.ExamTopic>()
            val topicsArray = sObj.optJSONArray("topics")
            if (topicsArray != null) {
                for (tIdx in 0 until topicsArray.length()) {
                    val tObj = topicsArray.getJSONObject(tIdx)
                    topicsList.add(
                        com.example.model.ExamTopic(
                            id = tObj.optString("id", "C${tIdx + 1}"),
                            title = tObj.optString("title", ""),
                            prompt = tObj.optString("prompt", ""),
                            modelPoints = jsonArrayToStringList(tObj.optJSONArray("model_points")),
                            modelText = if (tObj.has("model_text") && !tObj.isNull("model_text")) tObj.getString("model_text") else null
                        )
                    )
                }
            }

            val questionsList = mutableListOf<ExamQuestion>()
            val questionsArray = sObj.optJSONArray("questions")
            if (questionsArray != null) {
                for (q in 0 until questionsArray.length()) {
                    val qObj = questionsArray.getJSONObject(q)
                    val no = qObj.optString("no", "${q + 1}")
                    val points = qObj.optDouble("points", 0.0)
                    val prompt = qObj.optString("prompt", "")
                    val grading = qObj.optString("grading", "")
                    val answerGroupsList = mutableListOf<List<com.example.model.AnswerItem>>()
                    val agArray = qObj.optJSONArray("answer_groups")
                    if (agArray != null && agArray.length() > 0) {
                        val firstItem = agArray.get(0)
                        if (firstItem is JSONArray) {
                            for (g in 0 until agArray.length()) {
                                val groupArray = agArray.optJSONArray(g)
                                if (groupArray != null) {
                                    val groupItems = mutableListOf<com.example.model.AnswerItem>()
                                    for (idx in 0 until groupArray.length()) {
                                        val itemObj = groupArray.getJSONObject(idx)
                                        groupItems.add(
                                            com.example.model.AnswerItem(
                                                label = itemObj.optString("label", ""),
                                                value = itemObj.optString("value", "")
                                            )
                                        )
                                    }
                                    if (groupItems.isNotEmpty()) {
                                        answerGroupsList.add(groupItems)
                                    }
                                }
                            }
                        } else if (firstItem is JSONObject) {
                            var currentGroup = mutableListOf<com.example.model.AnswerItem>()
                            val seenLabels = mutableSetOf<String>()
                            for (idx in 0 until agArray.length()) {
                                val itemObj = agArray.getJSONObject(idx)
                                val lbl = itemObj.optString("label", "")
                                val valStr = itemObj.optString("value", "")
                                if (lbl.isNotEmpty() && seenLabels.contains(lbl)) {
                                    if (currentGroup.isNotEmpty()) {
                                        answerGroupsList.add(currentGroup)
                                    }
                                    currentGroup = mutableListOf()
                                    seenLabels.clear()
                                }
                                currentGroup.add(com.example.model.AnswerItem(label = lbl, value = valStr))
                                if (lbl.isNotEmpty()) {
                                    seenLabels.add(lbl)
                                }
                            }
                            if (currentGroup.isNotEmpty()) {
                                answerGroupsList.add(currentGroup)
                            }
                        }
                    }
                    val tableObj = qObj.optJSONObject("table")
                    val examTable = tableObj?.let { t ->
                        fun rowsOf(key: String): List<List<String>> {
                            val arr = t.optJSONArray(key) ?: return emptyList()
                            return (0 until arr.length()).map { i -> jsonArrayToStringList(arr.optJSONArray(i)) }
                        }
                        com.example.model.ExamTable(
                            headers = jsonArrayToStringList(t.optJSONArray("headers")),
                            rows = rowsOf("rows"),
                            answerRows = rowsOf("answer_rows")
                        )
                    }
                    questionsList.add(ExamQuestion(no, points, prompt, grading, answerGroupsList, examTable))
                }
            }

            val rubricList = mutableListOf<RubricItem>()
            val rubricArray = sObj.optJSONArray("rubric")
            if (rubricArray != null) {
                for (r in 0 until rubricArray.length()) {
                    val rObj = rubricArray.getJSONObject(r)
                    rubricList.add(
                        RubricItem(
                            criterion = rObj.optString("criterion", ""),
                            points = rObj.optDouble("points", 0.0)
                        )
                    )
                }
            }

            sectionsList.add(
                ExamSection(
                    id = sId,
                    title = sTitle,
                    points = sPoints,
                    questions = questionsList,
                    chooseOne = sChooseOne,
                    topics = topicsList,
                    topic = sTopic,
                    rubric = rubricList,
                    modelText = sModelText,
                    modelPoints = sModelPoints,
                    gradingNote = sGradingNote
                )
            )
        }

        return ExamData(
            id = id,
            component = component,
            semester = semester,
            unit = unit,
            type = type,
            order = order,
            title = title,
            source = source,
            subject = subject,
            duration = duration,
            declaredTotal = declaredTotal,
            passage = passage,
            sections = sectionsList
        )
    }

    private fun parseJsonArrayToAny(array: JSONArray): List<Any?> {
        val list = mutableListOf<Any?>()
        for (i in 0 until array.length()) {
            val item = array.get(i)
            when (item) {
                is JSONObject -> list.add(parseJsonObjectToMap(item))
                is JSONArray -> list.add(parseJsonArrayToAny(item))
                else -> list.add(item)
            }
        }
        return list
    }

    private fun parseJsonObjectToMap(obj: JSONObject): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = obj.get(key)
            when (value) {
                is JSONObject -> map[key] = parseJsonObjectToMap(value)
                is JSONArray -> map[key] = parseJsonArrayToAny(value)
                else -> map[key] = value
            }
        }
        return map
    }

    // IMPORTANT: this init block must stay at the END of the class body.
    // Kotlin initializes properties and init blocks in textual order, and
    // loadMistakes()/loadExamHistory() use properties declared above this point.
    init {
        try {
            loadAllData()
        } catch (e: Exception) {
            e.printStackTrace()
            // Corrupted/incompatible old SharedPreferences: reset and retry once.
            try { prefs.edit().clear().commit() } catch (_: Exception) {}
            try { loadAllData() } catch (e2: Exception) { e2.printStackTrace() }
        }
    }

    private fun loadAllData() {
        cleanDeprecatedData()
        loadSettings()
        loadCompletedLessons()
        loadLessons()
        loadMistakes()
        loadExamHistory()
    }
}
