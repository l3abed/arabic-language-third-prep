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
import com.example.util.ArabicAudioPlayer
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
                "exam-s1-general-assessment"
            )
            return list.sortedBy { exam ->
                val idx = assessmentOrder.indexOf(exam.id)
                if (idx >= 0) idx else 99
            }
        }
    val homeworkExams: List<com.example.model.ExamData>
        get() = standardExams.filter { it.id.contains("homework") || it.title.contains("الفرض") }
    val homeworkExam: com.example.model.ExamData? get() = repository.getExamById(_selectedExamId.value) ?: repository.getHomeworkExam()

    var isTeacherMode by androidx.compose.runtime.mutableStateOf(false)

    fun toggleTeacherMode() {
        isTeacherMode = !isTeacherMode
    }

    private val _selectedExamId = MutableStateFlow("exam-s1-homework-1")
    val selectedExamId: StateFlow<String> = _selectedExamId.asStateFlow()

    val currentExam: StateFlow<com.example.model.ExamData?> = combine(
        _selectedExamId,
        repository.lessons
    ) { examId, _ ->
        repository.getExamById(examId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.getHomeworkExam())

    val examAnswers = androidx.compose.runtime.mutableStateMapOf<String, String>()
    val selectedExamTopics = androidx.compose.runtime.mutableStateMapOf<String, String>()
    val submittedExams = androidx.compose.runtime.mutableStateMapOf<String, Boolean>()
    val showCorrectionExams = androidx.compose.runtime.mutableStateMapOf<String, Boolean>()
    var examValidationError by androidx.compose.runtime.mutableStateOf<String?>(null)

    val isExamSubmitted: Boolean
        get() = submittedExams[_selectedExamId.value] == true

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
        _activeLessonId.value = lessonId
        _activeScreen.value = ActiveScreen.EXERCISE_SESSION
    }

    fun navigateBackToTabs() {
        audioPlayer.stop()
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
        examValidationError = null
        _activeScreen.value = ActiveScreen.EXAM_VIEW
    }

    fun selectExamTopic(examId: String, sectionId: String, topicId: String) {
        selectedExamTopics["${examId}_${sectionId}"] = topicId
        if (examValidationError != null) {
            examValidationError = null
        }
    }

    fun getSelectedExamTopic(examId: String, sectionId: String): String? {
        return selectedExamTopics["${examId}_${sectionId}"]
    }

    fun updateExamAnswer(key: String, text: String) {
        examAnswers[key] = text
        if (examValidationError != null) {
            examValidationError = null
        }
    }

    fun validateExamSubmission(): String? {
        val exam = repository.getExamById(_selectedExamId.value) ?: return null
        for (section in exam.sections) {
            for (q in section.questions) {
                val key = "${exam.id}_${section.id}_q${q.no}"
                val ans = examAnswers[key]
                if (ans.isNullOrBlank()) {
                    return "يرجى الإجابة عن السؤال ${q.no} في «${section.title}» قبل تسليم الامتحان."
                }
            }
            if (section.id == "C" || section.topic != null || section.topics.isNotEmpty()) {
                if (section.chooseOne) {
                    val chosen = getSelectedExamTopic(exam.id, section.id)
                    if (chosen.isNullOrBlank()) {
                        return "يرجى اختيار أحد الموضوعين في «${section.title}» قبل تسليم الامتحان."
                    }
                }
                val writingKey = "${exam.id}_${section.id}_writing"
                val ans = examAnswers[writingKey]
                if (ans.isNullOrBlank()) {
                    return "يرجى كتابة موضوع الإنشاء في «${section.title}» قبل تسليم الامتحان."
                }
            }
        }
        return null
    }

    fun submitExam(): Boolean {
        val error = validateExamSubmission()
        val examId = _selectedExamId.value
        if (error != null) {
            examValidationError = error
            submittedExams[examId] = false
            showCorrectionExams[examId] = false
            return false
        }
        examValidationError = null
        submittedExams[examId] = true
        showCorrectionExams[examId] = true

        val examObj = repository.getExamById(examId)
        val title = examObj?.title ?: examId
        val total = examObj?.declaredTotal ?: 20
        repository.recordExamSubmission(examId, title, total)
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
        val prefix = "${examId}_"
        val keysToRemove = examAnswers.keys.filter { it.startsWith(prefix) }
        keysToRemove.forEach { examAnswers.remove(it) }
        val topicKeysToRemove = selectedExamTopics.keys.filter { it.startsWith(prefix) }
        topicKeysToRemove.forEach { selectedExamTopics.remove(it) }
        submittedExams[examId] = false
        showCorrectionExams[examId] = false
        examValidationError = null
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.shutdown()
    }
}
