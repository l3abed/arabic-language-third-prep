package com.example.model

data class ExamData(
    val id: String,
    val component: String = "exams",
    val semester: Int = 1,
    val unit: Int? = null,
    val type: String = "exam",
    val order: Int = 1,
    val title: String,
    val source: String = "",
    val subject: String = "",
    val duration: String = "",
    val declaredTotal: Int = 20,
    val passage: ExamPassage,
    val sections: List<ExamSection> = emptyList()
) {
    val totalPoints: Double
        get() = sections.sumOf { it.points }

    val isTotalValid: Boolean
        get() = Math.abs(totalPoints - declaredTotal.toDouble()) < 0.01
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
    val chooseOne: Boolean = false,
    val topics: List<ExamTopic> = emptyList(),
    val topic: String? = null,
    val rubric: List<RubricItem> = emptyList(),
    val modelText: String? = null,
    val modelPoints: List<String> = emptyList(),
    val gradingNote: String? = null
)

data class ExamTopic(
    val id: String = "",
    val title: String = "",
    val prompt: String = "",
    val modelPoints: List<String> = emptyList(),
    val modelText: String? = null
)

data class ExamQuestion(
    val no: String,
    val points: Double,
    val prompt: String,
    val grading: String = "",
    val answerGroups: List<List<AnswerItem>> = emptyList(),
    val table: ExamTable? = null
)

// A question whose answer is given in a table. Blank cells ("") in [rows] are filled by the student;
// [answerRows] holds the complete model answer.
data class ExamTable(
    val headers: List<String>,
    val rows: List<List<String>>,
    val answerRows: List<List<String>> = emptyList()
)

data class AnswerItem(
    val label: String = "",
    val value: String = ""
)

data class RubricItem(
    val criterion: String,
    val points: Double
)
