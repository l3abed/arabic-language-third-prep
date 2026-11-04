package com.example.model

data class ExamData(
    val id: String,
    val component: String,
    val semester: Int,
    val unit: Int,
    val type: String,
    val order: Int,
    val title: String,
    val subject: String,
    val duration: String,
    val declaredTotal: Int,
    val noteForTeacher: String,
    val passage: ExamPassage,
    val sections: List<ExamSection>
) {
    val totalPoints: Double
        get() = sections.sumOf { it.points }
}

data class ExamPassage(
    val title: String,
    val text: String
)

data class ExamSection(
    val id: String,
    val title: String,
    val points: Double,
    val questions: List<ExamQuestion> = emptyList(),
    val topic: String? = null,
    val rubric: List<RubricItem> = emptyList(),
    val modelText: String? = null,
    val gradingNote: String? = null
)

data class ExamQuestion(
    val no: String,
    val points: Double,
    val prompt: String,
    val modelRaw: Any?,
    val grading: String
)

data class RubricItem(
    val criterion: String,
    val points: Double
)
