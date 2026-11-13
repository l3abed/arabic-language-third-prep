package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LessonRepository
import com.example.data.UserSettings
import com.example.model.CardItem
import com.example.model.ComponentInfo
import com.example.model.Lesson
import com.example.model.ExamData
import com.example.model.ExamGrade
import com.example.util.ArabicAudioPlayer
import com.example.util.ExamGrader
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class NavTab {
    HOME,
    LESSONS,
    EXERCISES,
    ACCOUNT
}

enum class ActiveScreen {
    TABS,
    LESSON_DETAIL,
    EXERCISE_SESSION,
    EXAM_VIEW
}

class ArabicAppViewModel(application: Application) : AndroidViewModel(application) {
    val repository = LessonRepository(application)
    val audioPlayer = ArabicAudioPlayer(application)

    // Exam data & in-memory answers (strictly not saved in localStorage / SharedPreferences)
    /** False until lessons and exams finish loading in the background. */
    val isDataLoaded: StateFlow<Boolean> get() = repository.isLoaded

    val allExams: List<com.example.model.ExamData> get() = repository.getAllExams()
    val localExams: List<com.example.model.ExamData> get() = repository.getLocalExams()
    val standardExams: List<com.example.model.ExamData> get() = repository.getStandardExams()
    val unitAssessments: List<com.example.model.ExamData>
        get() {
            val list = standardExams.filter { it.id.contains("assessment") || it.title.contains("تقويم") }
            val assessmentOrder = listOf(
                "exam-s1-unit1-assessment",
                "exam-s1-unit2-assessment",
                "exam-s1-unit3-assessment",
                "exam-s1-general-assessment",
                "exam-s2-unit4-assessment",
                "exam-s2-unit5-assessment",
                "exam-s2-unit6-assessment",
                "exam-s2-general-assessment"
            )
            return list.sortedBy { exam ->
                val idx = assessmentOrder.indexOf(exam.id)
                if (idx >= 0) idx else 99
            }
        }
    val homeworkExams: List<com.example.model.ExamData>
        get() = standardExams.filter { it.id.contains("homework") || it.title.contains("الفرض") }
    val homeworkExam: com.example.model.ExamData? get() = repository.getExamById(_selectedExamId.value) ?: repository.getHomeworkExam()

    private val _selectedExamId = MutableStateFlow("exam-s1-homework-1")
    val selectedExamId: StateFlow<String> = _selectedExamId.asStateFlow()

    val currentExam: StateFlow<com.example.model.ExamData?> = combine(
        _selectedExamId,
        repository.lessons
    ) { examId, _ ->
        repository.getExamById(examId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val examAnswers = androidx.compose.runtime.mutableStateMapOf<String, String>()
    val selectedExamTopics = androidx.compose.runtime.mutableStateMapOf<String, String>()
    val submittedExams = androidx.compose.runtime.mutableStateMapOf<String, Boolean>()
    val showCorrectionExams = androidx.compose.runtime.mutableStateMapOf<String, Boolean>()
    /** Exams the student has started (timer running / answers being written). */
    val startedExams = androidx.compose.runtime.mutableStateMapOf<String, Boolean>()
    /** Epoch millis at which a timed exam ends; 0 or absent for untimed exams. */
    val examEndsAt = androidx.compose.runtime.mutableStateMapOf<String, Long>()
    val examAutoSubmitted = androidx.compose.runtime.mutableStateMapOf<String, Boolean>()
    /** Essay self-ratings. Key: "<examId>_<sectionId>_r<index>", value 0, 0.5 or 1. */
    val examRatings = androidx.compose.runtime.mutableStateMapOf<String, Float>()

    val isExamSubmitted: Boolean
        get() = submittedExams[_selectedExamId.value] == true

    val isExamStarted: Boolean
        get() = startedExams[_selectedExamId.value] == true

    val showCorrectionModel: Boolean
        get() = showCorrectionExams[_selectedExamId.value] == true

    private val _currentTab = MutableStateFlow(NavTab.HOME)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    private val _activeScreen = MutableStateFlow(ActiveScreen.TABS)
    val activeScreen: StateFlow<ActiveScreen> = _activeScreen.asStateFlow()

    private val _activeLessonId = MutableStateFlow<String?>(null)
    val activeLessonId: StateFlow<String?> = _activeLessonId.asStateFlow()

    // Filter states for lessons screen & home screen
    private val _selectedUnit = MutableStateFlow<Int?>(1) // Default: Unit 1 (الوحدة الأولى)
    val selectedUnit: StateFlow<Int?> = _selectedUnit.asStateFlow()

    private val _selectedSemester = MutableStateFlow<Int?>(null) // null = all
    val selectedSemester: StateFlow<Int?> = _selectedSemester.asStateFlow()

    private val _selectedComponentId = MutableStateFlow<String?>(null) // null = all
    val selectedComponentId: StateFlow<String?> = _selectedComponentId.asStateFlow()

    val settings: StateFlow<UserSettings> = repository.settings
    val lessons: StateFlow<List<Lesson>> = repository.lessons
    val completedLessons: StateFlow<Set<String>> = repository.completedLessons

    val componentInfos: StateFlow<List<ComponentInfo>> = combine(
        lessons,
        completedLessons,
        _selectedUnit
    ) { _, _, unit ->
        repository.getComponentInfos(unit)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val resumeLesson: StateFlow<Lesson?> = combine(
        lessons,
        completedLessons,
        _selectedUnit,
        _activeLessonId
    ) { _, _, unit, _ ->
        repository.getResumeLesson(unit)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setUnitFilter(unit: Int?) {
        _selectedUnit.value = unit
    }

    fun selectTab(tab: NavTab) {
        _currentTab.value = tab
        _activeScreen.value = ActiveScreen.TABS
    }

    fun openLesson(lessonId: String) {
        _activeLessonId.value = lessonId
        repository.setLastStudiedLessonId(lessonId)
        _activeScreen.value = ActiveScreen.LESSON_DETAIL
    }

    fun openExercise(lessonId: String) {
        _reviewSession.value = null
        _activeLessonId.value = lessonId
        _activeScreen.value = ActiveScreen.EXERCISE_SESSION
    }

    /** A cross-lesson session made of the mistakes that are due for spaced review today. */
    private val _reviewSession = MutableStateFlow<List<com.example.model.MistakeReviewItem>?>(null)
    val reviewSession: StateFlow<List<com.example.model.MistakeReviewItem>?> = _reviewSession.asStateFlow()

    fun getDueMistakesCount(): Int = repository.getDueMistakes().size

    fun startReviewSession(maxItems: Int = 10) {
        val due = repository.getDueMistakes().take(maxItems)
        if (due.isEmpty()) return
        _reviewSession.value = due
        _activeScreen.value = ActiveScreen.EXERCISE_SESSION
    }

    fun navigateBackToTabs() {
        audioPlayer.stop()
        _reviewSession.value = null
        _activeScreen.value = ActiveScreen.TABS
    }

    fun setSemesterFilter(semester: Int?) {
        _selectedSemester.value = semester
    }

    fun setComponentFilter(componentId: String?) {
        _selectedComponentId.value = componentId
    }

    fun updateSettings(newSettings: UserSettings) {
        repository.updateSettings(newSettings)
    }

    fun toggleDarkMode() {
        val current = settings.value
        updateSettings(current.copy(isDarkMode = !current.isDarkMode))
    }

    fun toggleTashkeel() {
        val current = settings.value
        updateSettings(current.copy(showTashkeel = !current.showTashkeel))
    }

    fun setFontScale(scale: Float) {
        val current = settings.value
        updateSettings(current.copy(fontScale = scale))
    }

    fun resetAllProgress() {
        repository.resetProgress()
    }

    fun speakCard(card: CardItem) {
        val speechBuilder = StringBuilder()
        when (card) {
            is CardItem.RuleCard -> {
                speechBuilder.append(card.title).append(". ")
                card.text?.let { speechBuilder.append(it).append(". ") }
                card.points.forEach { speechBuilder.append(it).append(". ") }
            }
            is CardItem.ExampleCard -> {
                speechBuilder.append(card.title).append(". ")
                card.items.forEach { speechBuilder.append(it.text).append(". ").append(it.note).append(". ") }
            }
            is CardItem.IrabCard -> {
                speechBuilder.append(card.sentence).append(". ")
                card.steps.forEach { speechBuilder.append(it.word).append(": ").append(it.irab).append(". ") }
                card.tip?.let { speechBuilder.append(it).append(". ") }
            }
            is CardItem.FactsCard -> {
                speechBuilder.append(card.title).append(". ")
                card.items.forEach { speechBuilder.append(it.label).append(": ").append(it.value).append(". ") }
            }
            is CardItem.VocabCard -> {
                speechBuilder.append(card.title).append(". ")
                card.items.forEach { speechBuilder.append(it.label).append(": ").append(it.value).append(". ") }
            }
            is CardItem.SummaryCard -> {
                speechBuilder.append(card.title).append(". ").append(card.text)
            }
            is CardItem.StepsCard -> {
                speechBuilder.append(card.title).append(". ")
                card.points.forEachIndexed { i, p -> speechBuilder.append("${i + 1}: ").append(p).append(". ") }
            }
            is CardItem.ModelCard -> {
                speechBuilder.append(card.title).append(". ").append(card.text).append(". ")
                card.points.forEach { speechBuilder.append(it).append(". ") }
            }
            is CardItem.MemorizeCard -> {
                speechBuilder.append(card.title).append(". ")
                card.points.forEach { speechBuilder.append(it).append(". ") }
            }
        }
        audioPlayer.speak(speechBuilder.toString(), settings.value.speechRate)
    }

    fun speakText(text: String) {
        audioPlayer.speak(text, settings.value.speechRate)
    }

    fun openExam(examId: String = "exam-s1-homework-1") {
        _selectedExamId.value = examId
        restoreExamSession(examId)
        _activeScreen.value = ActiveScreen.EXAM_VIEW
        // A timed exam whose time ran out while the app was closed is submitted now.
        val ends = examEndsAt[examId] ?: 0L
        if (startedExams[examId] == true && submittedExams[examId] != true &&
            ends > 0L && System.currentTimeMillis() >= ends
        ) {
            submitExam(auto = true)
        }
    }

    private fun answersFor(examId: String): Map<String, String> {
        val prefix = "${examId}_"
        return examAnswers.toMap().filterKeys { it.startsWith(prefix) }
    }

    private fun ratingsFor(examId: String): Map<String, Float> {
        val prefix = "${examId}_"
        return examRatings.toMap().filterKeys { it.startsWith(prefix) }
    }

    private fun clearExamState(examId: String) {
        val prefix = "${examId}_"
        examAnswers.keys.filter { it.startsWith(prefix) }.forEach { examAnswers.remove(it) }
        selectedExamTopics.keys.filter { it.startsWith(prefix) }.forEach { selectedExamTopics.remove(it) }
        examRatings.keys.filter { it.startsWith(prefix) }.forEach { examRatings.remove(it) }
        startedExams.remove(examId)
        examEndsAt.remove(examId)
        examAutoSubmitted.remove(examId)
        submittedExams[examId] = false
        showCorrectionExams[examId] = false
    }

    /** Loads the saved session of [examId] (answers, timer end, submission state), if any. */
    private fun restoreExamSession(examId: String) {
        clearExamState(examId)
        val session = repository.loadExamSession(examId) ?: return
        try {
            startedExams[examId] = session.optBoolean("started", false)
            val ends = session.optLong("endsAt", 0L)
            if (ends > 0L) examEndsAt[examId] = ends
            val submitted = session.optBoolean("submitted", false)
            submittedExams[examId] = submitted
            showCorrectionExams[examId] = submitted
            examAutoSubmitted[examId] = session.optBoolean("auto", false)
            session.optJSONObject("answers")?.let { o ->
                o.keys().forEach { k -> examAnswers[k] = o.optString(k, "") }
            }
            session.optJSONObject("topics")?.let { o ->
                o.keys().forEach { k -> selectedExamTopics[k] = o.optString(k, "") }
            }
            session.optJSONObject("ratings")?.let { o ->
                o.keys().forEach { k -> examRatings[k] = o.optDouble(k, 0.0).toFloat() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            clearExamState(examId)
        }
    }

    private fun persistExamSession(examId: String) {
        val prefix = "${examId}_"
        val session = JSONObject()
        session.put("started", startedExams[examId] == true)
        session.put("endsAt", examEndsAt[examId] ?: 0L)
        session.put("submitted", submittedExams[examId] == true)
        session.put("auto", examAutoSubmitted[examId] == true)
        val answers = JSONObject()
        examAnswers.toMap().forEach { (k, v) -> if (k.startsWith(prefix)) answers.put(k, v) }
        session.put("answers", answers)
        val topics = JSONObject()
        selectedExamTopics.toMap().forEach { (k, v) -> if (k.startsWith(prefix)) topics.put(k, v) }
        session.put("topics", topics)
        val ratings = JSONObject()
        examRatings.toMap().forEach { (k, v) -> if (k.startsWith(prefix)) ratings.put(k, v.toDouble()) }
        session.put("ratings", ratings)
        repository.saveExamSession(examId, session)
    }

    private var persistJob: Job? = null

    private fun schedulePersist() {
        persistJob?.cancel()
        val examId = _selectedExamId.value
        persistJob = viewModelScope.launch {
            delay(500L)
            persistExamSession(examId)
        }
    }

    /** Starts the selected exam: the timer begins and cannot be paused or reset. */
    fun startExam() {
        val examId = _selectedExamId.value
        val exam = repository.getExamById(examId) ?: return
        clearExamState(examId)
        startedExams[examId] = true
        val seconds = ExamGrader.durationSeconds(exam.duration)
        if (seconds != null) examEndsAt[examId] = System.currentTimeMillis() + seconds * 1000L
        persistExamSession(examId)
    }

    /** Number of questions (and the essay) the student left empty. */
    fun unansweredCount(exam: ExamData): Int {
        var count = 0
        for (section in exam.sections) {
            for (q in section.questions) {
                val ans = examAnswers["${exam.id}_${section.id}_q${q.no}"].orEmpty()
                if (ans.replace(ExamGrader.CELL_SEPARATOR, "").isBlank()) count++
            }
            if (section.id == "C" || section.topic != null || section.topics.isNotEmpty()) {
                if (examAnswers["${exam.id}_${section.id}_writing"].orEmpty().isBlank()) count++
            }
        }
        return count
    }

    fun gradeCurrentExam(): ExamGrade? {
        val exam = repository.getExamById(_selectedExamId.value) ?: return null
        return ExamGrader.gradeExam(exam, answersFor(exam.id), ratingsFor(exam.id))
    }

    fun setRubricRating(sectionId: String, index: Int, rating: Float) {
        val examId = _selectedExamId.value
        examRatings["${examId}_${sectionId}_r$index"] = rating
        persistExamSession(examId)
        gradeCurrentExam()?.let { repository.updateLatestExamScore(examId, it.total) }
    }


    fun selectExamTopic(examId: String, sectionId: String, topicId: String) {
        selectedExamTopics["${examId}_${sectionId}"] = topicId
        schedulePersist()
    }

    fun getSelectedExamTopic(examId: String, sectionId: String): String? {
        return selectedExamTopics["${examId}_${sectionId}"]
    }

    fun updateExamAnswer(key: String, text: String) {
        examAnswers[key] = text
        schedulePersist()
    }


    /** Submits the selected exam and grades it. Unanswered questions simply score zero. */
    fun submitExam(auto: Boolean = false): Boolean {
        val examId = _selectedExamId.value
        val exam = repository.getExamById(examId) ?: return false
        if (submittedExams[examId] == true) return true
        persistJob?.cancel()
        startedExams[examId] = true
        submittedExams[examId] = true
        showCorrectionExams[examId] = true
        examAutoSubmitted[examId] = auto
        persistExamSession(examId)
        val grade = ExamGrader.gradeExam(exam, answersFor(examId), ratingsFor(examId))
        repository.recordExamSubmission(examId, exam.title, exam.declaredTotal, grade.total)
        return true
    }

    // Spaced review bindings
    val mistakeReviewItems = repository.mistakeReviewItems
    val examHistory = repository.examHistory

    fun recordMistake(lessonId: String, questionIndex: Int) {
        repository.recordMistake(lessonId, questionIndex)
    }

    fun recordReviewCorrect(lessonId: String, questionIndex: Int) {
        repository.recordReviewCorrect(lessonId, questionIndex)
    }

    fun clearMistakes() {
        repository.clearMistakes()
    }

    fun clearExamHistory() {
        repository.clearExamHistory()
    }

    fun toggleCorrectionModel() {
        val examId = _selectedExamId.value
        if (submittedExams[examId] == true) {
            val current = showCorrectionExams[examId] ?: false
            showCorrectionExams[examId] = !current
        }
    }

    fun resetExam() {
        val examId = _selectedExamId.value
        persistJob?.cancel()
        clearExamState(examId)
        repository.clearExamSession(examId)
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.shutdown()
    }
}
