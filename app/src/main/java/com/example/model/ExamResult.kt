package com.example.model

/** How a question was graded. */
enum class GradeMode { AUTO, OPINION }

/** One expected element of a model answer and how well the student's answer matched it (0.0 .. 1.0). */
data class GradedItem(
    val label: String,
    val expected: String,
    val fraction: Double,
    val student: String
)

data class QuestionGrade(
    val earned: Double,
    val max: Double,
    val mode: GradeMode,
    val items: List<GradedItem>
) {
    val fraction: Double get() = if (max <= 0.0) 0.0 else earned / max
    val isFull: Boolean get() = fraction >= 0.999
    val isEmpty: Boolean get() = earned <= 0.0
}

data class SectionGrade(
    val sectionId: String,
    val title: String,
    val earned: Double,
    val max: Double,
    /** True when the section has an essay whose rubric the student has not fully self-rated yet. */
    val needsSelfRating: Boolean
)

data class ExamGrade(
    val total: Double,
    val max: Double,
    val sections: List<SectionGrade>,
    /** Key: "<sectionId>_<questionNo>". */
    val questions: Map<String, QuestionGrade>
)
