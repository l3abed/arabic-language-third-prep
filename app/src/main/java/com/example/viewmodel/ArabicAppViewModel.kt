package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LessonRepository
import com.example.data.UserSettings
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
    EXERCISE_SESSION
}

class ArabicAppViewModel(application: Application) : AndroidViewModel(application) {
    val repository = LessonRepository(application)
    val audioPlayer = ArabicAudioPlayer(application)

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

    val lastStudiedLesson: StateFlow<Lesson?> = combine(
        lessons,
        _activeLessonId
    ) { allLessons, _ ->
        val lastId = repository.getLastStudiedLessonId()
        allLessons.firstOrNull { it.id == lastId } ?: allLessons.firstOrNull()
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

    fun speakText(text: String) {
        audioPlayer.speak(text, settings.value.speechRate)
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.shutdown()
    }
}
