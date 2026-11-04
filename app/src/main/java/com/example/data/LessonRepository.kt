package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ComponentInfo
import com.example.model.Exercise
import com.example.model.ExerciseType
import com.example.model.Lesson
import com.example.model.LessonCard
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

    init {
        loadSettings()
        loadCompletedLessons()
        loadLessons()
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
        _completedLessons.value = set
    }

    fun markLessonCompleted(lessonId: String) {
        val updated = _completedLessons.value.toMutableSet().apply { add(lessonId) }
        _completedLessons.value = updated
        prefs.edit().putStringSet("completed_lessons_set", updated).apply()
    }

    fun getLastStudiedLessonId(): String {
        val saved = prefs.getString("last_studied_lesson_id", null)
        if (!saved.isNullOrEmpty() && _lessons.value.any { it.id == saved }) {
            return saved
        }
        return _lessons.value.firstOrNull()?.id ?: "shart_lesson"
    }

    fun setLastStudiedLessonId(lessonId: String) {
        prefs.edit().putString("last_studied_lesson_id", lessonId).apply()
    }

    fun getLessonProgress(lessonId: String): Int {
        return prefs.getInt("lesson_step_$lessonId", 0)
    }

    fun saveLessonProgress(lessonId: String, stepIndex: Int) {
        prefs.edit().putInt("lesson_step_$lessonId", stepIndex).apply()
    }

    fun getExerciseScore(lessonId: String): Pair<Int, Int>? {
        if (!prefs.contains("score_${lessonId}_val")) return null
        val score = prefs.getInt("score_${lessonId}_val", 0)
        val total = prefs.getInt("score_${lessonId}_total", 5)
        return Pair(score, total)
    }

    fun saveExerciseScore(lessonId: String, score: Int, total: Int) {
        prefs.edit()
            .putInt("score_${lessonId}_val", score)
            .putInt("score_${lessonId}_total", total)
            .apply()
        if (score >= (total / 2)) {
            markLessonCompleted(lessonId)
        }
    }

    fun resetProgress() {
        prefs.edit()
            .remove("completed_lessons_set")
            .remove("last_studied_lesson_id")
            .apply()
        // Clear all step and score prefs
        val allKeys = prefs.all.keys.toList()
        val editor = prefs.edit()
        for (key in allKeys) {
            if (key.startsWith("lesson_step_") || key.startsWith("score_")) {
                editor.remove(key)
            }
        }
        editor.apply()
        _completedLessons.value = emptySet()
    }

    fun loadLessons() {
        try {
            val jsonString = getActiveJsonString()
            val parsed = parseJson(jsonString)
            _lessons.value = parsed
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to asset directly if custom corrupted
            val assetJson = readAssetJson()
            _lessons.value = parseJson(assetJson)
        }
    }

    fun getActiveJsonString(): String {
        val customFile = File(context.filesDir, "custom_lessons.json")
        return if (customFile.exists()) {
            customFile.readText()
        } else {
            readAssetJson()
        }
    }

    private fun readAssetJson(): String {
        return context.assets.open("lessons.json").bufferedReader().use { it.readText() }
    }

    fun saveCustomLessonsJson(jsonString: String): Boolean {
        return try {
            // Validate first
            val parsed = parseJson(jsonString)
            if (parsed.isEmpty()) return false
            val customFile = File(context.filesDir, "custom_lessons.json")
            customFile.writeText(jsonString)
            _lessons.value = parsed
            true
        } catch (e: Exception) {
            false
        }
    }

    fun restoreDefaultLessons(): Boolean {
        val customFile = File(context.filesDir, "custom_lessons.json")
        if (customFile.exists()) {
            customFile.delete()
        }
        loadLessons()
        return true
    }

    private fun parseJson(jsonString: String): List<Lesson> {
        val root = JSONObject(jsonString)
        val lessonsArray = root.getJSONArray("lessons")
        val result = mutableListOf<Lesson>()

        for (i in 0 until lessonsArray.length()) {
            val lObj = lessonsArray.getJSONObject(i)
            val id = lObj.optString("id", "lesson_$i")
            val title = lObj.optString("title", "")
            val componentId = lObj.optString("componentId", "linguistic")
            val componentName = lObj.optString("componentName", "الدرس اللغوي")
            val semester = lObj.optInt("semester", 1)
            val semesterName = lObj.optString("semesterName", "الدورة الأولى")
            val shortDesc = lObj.optString("shortDescription", "")

            // Parse Cards
            val cardsList = mutableListOf<LessonCard>()
            val cardsArray = lObj.optJSONArray("cards") ?: JSONArray()
            for (c in 0 until cardsArray.length()) {
                val cObj = cardsArray.getJSONObject(c)
                cardsList.add(
                    LessonCard(
                        id = cObj.optString("id", "c_$c"),
                        badge = cObj.optString("badge", "القاعدة"),
                        rule = cObj.optString("rule", ""),
                        exampleTitle = cObj.optString("exampleTitle", "مثال"),
                        exampleText = cObj.optString("exampleText", ""),
                        exampleBreakdown = cObj.optString("exampleBreakdown", ""),
                        noteTitle = cObj.optString("noteTitle", "احفظ هذا"),
                        noteText = cObj.optString("noteText", "")
                    )
                )
            }

            // Parse Exercises
            val exercisesList = mutableListOf<Exercise>()
            val exercisesArray = lObj.optJSONArray("exercises") ?: JSONArray()
            for (e in 0 until exercisesArray.length()) {
                val eObj = exercisesArray.getJSONObject(e)
                val typeStr = eObj.optString("type", "single_choice")
                val type = when (typeStr) {
                    "true_false" -> ExerciseType.TRUE_FALSE
                    "word_order" -> ExerciseType.WORD_ORDER
                    else -> ExerciseType.SINGLE_CHOICE
                }

                val options = mutableListOf<String>()
                val optArray = eObj.optJSONArray("options")
                if (optArray != null) {
                    for (o in 0 until optArray.length()) {
                        options.add(optArray.getString(o))
                    }
                }

                val words = mutableListOf<String>()
                val wordsArray = eObj.optJSONArray("words")
                if (wordsArray != null) {
                    for (w in 0 until wordsArray.length()) {
                        words.add(wordsArray.getString(w))
                    }
                }

                val correctOrder = mutableListOf<String>()
                val coArray = eObj.optJSONArray("correctOrder")
                if (coArray != null) {
                    for (co in 0 until coArray.length()) {
                        correctOrder.add(coArray.getString(co))
                    }
                }

                exercisesList.add(
                    Exercise(
                        id = eObj.optString("id", "e_$e"),
                        type = type,
                        title = eObj.optString("title", "تمرين"),
                        question = eObj.optString("question", ""),
                        options = options,
                        correctAnswerIndex = eObj.optInt("correctAnswerIndex", 0),
                        words = words,
                        correctOrder = correctOrder,
                        explanation = eObj.optString("explanation", "")
                    )
                )
            }

            result.add(
                Lesson(
                    id = id,
                    title = title,
                    componentId = componentId,
                    componentName = componentName,
                    semester = semester,
                    semesterName = semesterName,
                    shortDescription = shortDesc,
                    cards = cardsList,
                    exercises = exercisesList
                )
            )
        }
        return result
    }

    fun getComponentInfos(): List<ComponentInfo> {
        val allLessons = _lessons.value
        val completed = _completedLessons.value

        val componentDefs = listOf(
            Triple("reading", "النصوص القرائية والشعرية", "نص + شرح + أسئلة الفهم"),
            Triple("linguistic", "الدرس اللغوي", "قاعدة + أمثلة + تمرين"),
            Triple("composition", "التعبير والإنشاء", "خطوات الكتابة + نماذج مُنجزة"),
            Triple("exams", "نماذج الامتحانات", "فروض + تصحيح مفصّل")
        )

        return componentDefs.map { (id, title, subtitle) ->
            val matching = allLessons.filter { it.componentId == id }
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
