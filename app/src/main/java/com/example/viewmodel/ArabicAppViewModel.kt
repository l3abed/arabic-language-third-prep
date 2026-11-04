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
    val homeworkExam = repository.getHomeworkExam()
    val examAnswers = androidx.compose.runtime.mutableStateMapOf<String, String>()
    var isExamSubmitted by androidx.compose.runtime.mutableStateOf(false)
    var showCorrectionModel by androidx.compose.runtime.mutableStateOf(false)
    var examValidationError by androidx.compose.runtime.mutableStateOf<String?>(null)

    private val _currentTab = MutableStateFlow(NavTab.HOME)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    private val _activeScreen = MutableStateFlow(ActiveScreen.TABS)
    val activeScreen: StateFlow<ActiveScreen> = _activeScreen.asStateFlow()

    private val _activeLessonId = MutableStateFlow<String?>(null)
    val activeLessonId: StateFlow<String?> = _activeLessonId.asStateFlow()

    // Filter states for lessons screen
    private val _selectedSemester = MutableStateFlow<Int?>(null) // null = all
    val selectedSemester: StateFlow<Int?> = _selectedSemester.asStateFlow()

    private val _selectedComponentId = MutableStateFlow<String?>(null) // null = all
    val selectedComponentId: StateFlow<String?> = _selectedComponentId.asStateFlow()

    val settings: StateFlow<UserSettings> = repository.settings
    val lessons: StateFlow<List<Lesson>> = repository.lessons
    val completedLessons: StateFlow<Set<String>> = repository.completedLessons

    val componentInfos: StateFlow<List<ComponentInfo>> = combine(
        lessons,
        completedLessons
    ) { _, _ ->
        repository.getComponentInfos()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val resumeLesson: StateFlow<Lesson?> = combine(
        lessons,
        completedLessons,
        _activeLessonId
    ) { _, _, _ ->
        repository.getResumeLesson()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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

    fun openExam() {
        if (!isExamSubmitted) {
            showCorrectionModel = false
        }
        examValidationError = null
        _activeScreen.value = ActiveScreen.EXAM_VIEW
    }

    fun updateExamAnswer(key: String, text: String) {
        examAnswers[key] = text
        if (examValidationError != null) {
            examValidationError = null
        }
    }

    fun validateExamSubmission(): String? {
        val exam = homeworkExam ?: return null
        for (section in exam.sections) {
            for (q in section.questions) {
                val key = "${section.id}_q${q.no}"
                val ans = examAnswers[key]
                if (ans.isNullOrBlank()) {
                    return "يرجى الإجابة عن السؤال ${q.no} في «${section.title}» قبل تسليم الفرض."
                }
            }
            if (section.id == "C" || section.topic != null) {
                val key = "${section.id}_writing"
                val ans = examAnswers[key]
                if (ans.isNullOrBlank()) {
                    return "يرجى كتابة موضوع الإنشاء في «${section.title}» قبل تسليم الفرض."
                }
            }
        }
        return null
    }

    fun submitExam(): Boolean {
        val error = validateExamSubmission()
        if (error != null) {
            examValidationError = error
            isExamSubmitted = false
            showCorrectionModel = false
            return false
        }
        examValidationError = null
        isExamSubmitted = true
        showCorrectionModel = true
        return true
    }

    fun toggleCorrectionModel() {
        if (isExamSubmitted) {
            showCorrectionModel = !showCorrectionModel
        }
    }

    fun resetExam() {
        examAnswers.clear()
        isExamSubmitted = false
        showCorrectionModel = false
        examValidationError = null
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.shutdown()
    }
}
