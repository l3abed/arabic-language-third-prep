package com.example.model

data class LessonsData(
    val components: List<ComponentDefinition> = emptyList(),
    val lessons: List<Lesson> = emptyList()
)

data class ComponentDefinition(
    val id: String,
    val title: String
)

data class Lesson(
    val id: String,
    val component: String,
    val semester: Int,
    val unit: Int,
    val order: Int,
    val tag: String? = null,
    val title: String,
    val summary: String,
    val cards: List<CardItem> = emptyList(),
    val exercises: List<ExerciseItem> = emptyList()
) {
    val componentDisplayName: String
        get() = when (component) {
            "texts" -> "النصوص القرائية والشعرية"
            "language" -> "الدرس اللغوي"
            "writing" -> "التعبير والإنشاء"
            "exams" -> "نماذج الامتحانات"
            else -> component
        }

    val semesterDisplayName: String
        get() = when (semester) {
            1 -> "الدورة الأولى"
            2 -> "الدورة الثانية"
            else -> "الدورة $semester"
        }

    val unitDisplayName: String
        get() = when (unit) {
            1 -> "الوحدة الأولى"
            2 -> "الوحدة الثانية"
            3 -> "الوحدة الثالثة"
            4 -> "الوحدة الرابعة"
            5 -> "الوحدة الخامسة"
            6 -> "الوحدة السادسة"
            else -> "الوحدة $unit"
        }
}

sealed class CardItem {
    abstract val type: String
    abstract val title: String

    // rule: title, text (اختياري), points[]
    data class RuleCard(
        override val title: String,
        val text: String? = null,
        val points: List<String> = emptyList()
    ) : CardItem() {
        override val type: String = "rule"
    }

    // example: title, items[{text, note}]
    data class ExampleCard(
        override val title: String,
        val items: List<ExampleEntry> = emptyList()
    ) : CardItem() {
        override val type: String = "example"
    }

    // irab: sentence, steps[{word, irab}], tip
    data class IrabCard(
        val sentence: String,
        val steps: List<IrabStep> = emptyList(),
        val tip: String? = null
    ) : CardItem() {
        override val type: String = "irab"
        override val title: String = "نموذج إعرابي"
    }

    // facts: title, items[{label, value}]
    data class FactsCard(
        override val title: String,
        val items: List<KeyValuePair> = emptyList()
    ) : CardItem() {
        override val type: String = "facts"
    }

    // vocab: title, items[{label, value}]
    data class VocabCard(
        override val title: String,
        val items: List<KeyValuePair> = emptyList()
    ) : CardItem() {
        override val type: String = "vocab"
    }

    // summary: title, text
    data class SummaryCard(
        override val title: String,
        val text: String
    ) : CardItem() {
        override val type: String = "summary"
    }

    // steps: title, points[]
    data class StepsCard(
        override val title: String,
        val points: List<String> = emptyList()
    ) : CardItem() {
        override val type: String = "steps"
    }

    // model: title, text, points[]
    data class ModelCard(
        override val title: String,
        val text: String,
        val points: List<String> = emptyList()
    ) : CardItem() {
        override val type: String = "model"
    }

    // memorize: title, points[]
    data class MemorizeCard(
        override val title: String,
        val points: List<String> = emptyList()
    ) : CardItem() {
        override val type: String = "memorize"
    }
}

data class ExampleEntry(
    val text: String,
    val note: String
)

data class IrabStep(
    val word: String,
    val irab: String
)

data class KeyValuePair(
    val label: String,
    val value: String
)

sealed class ExerciseItem {
    abstract val type: String
    abstract val prompt: String
    abstract val explanation: String

    data class Mcq(
        val question: String,
        val options: List<String>,
        val answerIndex: Int,
        override val explanation: String
    ) : ExerciseItem() {
        override val type: String = "mcq"
        override val prompt: String = question
    }

    data class TrueFalse(
        val statement: String,
        val answer: Boolean,
        override val explanation: String
    ) : ExerciseItem() {
        override val type: String = "true_false"
        override val prompt: String = statement
    }

    data class OrderWords(
        val words: List<String>,
        val answer: List<String>,
        override val explanation: String
    ) : ExerciseItem() {
        override val type: String = "order_words"
        override val prompt: String = "رتّب الكلمات لتكوين جملة سليمة:"
    }
}

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

/**
 * Represents a mistaken question in exercises for spaced review.
 */
data class MistakeReviewItem(
    val id: String, // lessonId:questionIndex
    val lessonId: String,
    val questionIndex: Int,
    val errorCount: Int,
    val lastErrorTimestamp: Long,
    val lessonTitle: String = "",
    val exercise: ExerciseItem? = null
)

/**
 * Record of a completed exam submission.
 */
data class ExamHistoryRecord(
    val examId: String,
    val examTitle: String,
    val timestamp: Long,
    val declaredTotal: Int
)
