package com.example.model

data class LessonsData(
    val lessons: List<Lesson> = emptyList()
)

data class Lesson(
    val id: String,
    val title: String,
    val componentId: String,
    val componentName: String,
    val semester: Int,
    val semesterName: String,
    val shortDescription: String,
    val cards: List<LessonCard> = emptyList(),
    val exercises: List<Exercise> = emptyList()
)

data class LessonCard(
    val id: String,
    val badge: String,
    val rule: String,
    val exampleTitle: String,
    val exampleText: String,
    val exampleBreakdown: String,
    val noteTitle: String,
    val noteText: String
)

enum class ExerciseType {
    SINGLE_CHOICE,
    TRUE_FALSE,
    WORD_ORDER
}

data class Exercise(
    val id: String,
    val type: ExerciseType,
    val title: String,
    val question: String,
    val options: List<String> = emptyList(),
    val correctAnswerIndex: Int = 0,
    val words: List<String> = emptyList(),
    val correctOrder: List<String> = emptyList(),
    val explanation: String
)

data class ComponentInfo(
    val id: String,
    val title: String,
    val subtitle: String,
    val totalLessons: Int = 0,
    val completedLessons: Int = 0
) {
    val progress: Float
        get() = if (totalLessons > 0) completedLessons.toFloat() / totalLessons else 0f
}
