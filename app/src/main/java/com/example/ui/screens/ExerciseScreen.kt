package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Exercise
import com.example.model.ExerciseType
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.BorderLight
import com.example.ui.theme.ErrorBgLight
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.PrimaryDarkTeal
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMutedLight
import com.example.ui.theme.TextSecondaryLight
import com.example.util.TextHelper
import com.example.viewmodel.ArabicAppViewModel

@Composable
fun ExerciseScreen(
    viewModel: ArabicAppViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBackToTabs()
    }

    val activeLessonId by viewModel.activeLessonId.collectAsState()
    val lessons by viewModel.lessons.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val lesson = lessons.firstOrNull { it.id == activeLessonId }

    if (lesson == null || lesson.exercises.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("لا توجد تمارين لهذا الدرس")
        }
        return
    }

    var questionIndex by remember(lesson.id) { mutableIntStateOf(0) }
    var score by remember(lesson.id) { mutableIntStateOf(0) }
    var isAnswerSubmitted by remember(lesson.id, questionIndex) { mutableStateOf(false) }
    var isAnswerCorrect by remember(lesson.id, questionIndex) { mutableStateOf(false) }
    var selectedOptionIndex by remember(lesson.id, questionIndex) { mutableStateOf<Int?>(null) }
    val orderedWords = remember(lesson.id, questionIndex) { mutableStateListOf<String>() }
    var isQuizCompleted by remember(lesson.id) { mutableStateOf(false) }

    val exercise = lesson.exercises.getOrNull(questionIndex)
    val totalQuestions = lesson.exercises.size

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isQuizCompleted) {
            // Result screen
            QuizResultView(
                score = score,
                total = totalQuestions,
                lessonTitle = lesson.title,
                onRetry = {
                    questionIndex = 0
                    score = 0
                    isQuizCompleted = false
                },
                onFinish = {
                    viewModel.navigateBackToTabs()
                }
            )
            return@Surface
        }

        if (exercise == null) return@Surface

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Top Bar matching Screenshot 2
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Question index counter, e.g. "2 / 5"
                Text(
                    text = "${totalQuestions} / ${questionIndex + 1}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondaryLight
                )

                // Title: "تمرين • أسلوب الشرط"
                Text(
                    text = "تمرين • ${TextHelper.formatArabic(lesson.title, settings.showTashkeel)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp
                )

                // Close button 'X'
                IconButton(
                    onClick = { viewModel.navigateBackToTabs() },
                    modifier = Modifier.testTag("exercise_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "إغلاق",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Segmented progress bar matching Screenshot 2
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (i in 0 until totalQuestions) {
                    val isPastOrCurrent = i <= questionIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isPastOrCurrent) PrimaryDarkTeal else Color(0xFFD1DCDB))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content Scroll
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Question Card (White) matching Screenshot 2
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exercise_question_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp)
                    ) {
                        Text(
                            text = exercise.title,
                            color = TextSecondaryLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = TextHelper.formatArabic(exercise.question, settings.showTashkeel),
                            fontFamily = AmiriFontFamily,
                            fontSize = (22 * settings.fontScale).sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start,
                            lineHeight = (32 * settings.fontScale).sp
                        )
                    }
                }

                // Options / Interaction
                when (exercise.type) {
                    ExerciseType.SINGLE_CHOICE, ExerciseType.TRUE_FALSE -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            exercise.options.forEachIndexed { optIndex, optionText ->
                                val isSelected = selectedOptionIndex == optIndex
                                val isCorrectOption = optIndex == exercise.correctAnswerIndex

                                val borderColor = when {
                                    isAnswerSubmitted && isSelected && isCorrectOption -> SuccessGreen
                                    isAnswerSubmitted && isSelected && !isCorrectOption -> ErrorRed
                                    isSelected -> PrimaryDarkTeal
                                    else -> BorderLight
                                }

                                val borderWidth = if (isSelected) 2.dp else 1.dp

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("exercise_option_$optIndex")
                                        .clip(RoundedCornerShape(14.dp))
                                        .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
                                        .clickable(enabled = !isAnswerSubmitted) {
                                            selectedOptionIndex = optIndex
                                            isAnswerSubmitted = true
                                            val correct = optIndex == exercise.correctAnswerIndex
                                            isAnswerCorrect = correct
                                            if (correct) score++
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 18.dp, vertical = 16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Checkmark if selected
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = null,
                                                tint = if (isAnswerSubmitted && !isAnswerCorrect) ErrorRed else PrimaryDarkTeal,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.size(22.dp))
                                        }

                                        Text(
                                            text = TextHelper.formatArabic(optionText, settings.showTashkeel),
                                            fontFamily = AmiriFontFamily,
                                            fontSize = (20 * settings.fontScale).sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            }
                        }
                    }

                    ExerciseType.WORD_ORDER -> {
                        // Word Order Exercise: Reorder chips
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Target construction box
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "الجملة المركبة:",
                                        fontSize = 13.sp,
                                        color = TextSecondaryLight
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    if (orderedWords.isEmpty()) {
                                        Text(
                                            text = "انقر على الكلمات بالترتيب لتكوين الجملة",
                                            fontSize = 14.sp,
                                            color = TextMutedLight
                                        )
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            orderedWords.forEach { word ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(PrimaryDarkTeal)
                                                        .clickable(enabled = !isAnswerSubmitted) {
                                                            orderedWords.remove(word)
                                                        }
                                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                                ) {
                                                    Text(
                                                        text = TextHelper.formatArabic(word, settings.showTashkeel),
                                                        color = Color.White,
                                                        fontFamily = AmiriFontFamily,
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Source words
                            val availableWords = exercise.words.filter { !orderedWords.contains(it) }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                availableWords.forEach { word ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .border(1.dp, PrimaryDarkTeal, RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .clickable(enabled = !isAnswerSubmitted) {
                                                orderedWords.add(word)
                                            }
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = TextHelper.formatArabic(word, settings.showTashkeel),
                                            fontFamily = AmiriFontFamily,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryDarkTeal
                                        )
                                    }
                                }
                            }

                            if (!isAnswerSubmitted && orderedWords.size == exercise.words.size) {
                                Button(
                                    onClick = {
                                        isAnswerSubmitted = true
                                        val correct = orderedWords.toList() == exercise.correctOrder
                                        isAnswerCorrect = correct
                                        if (correct) score++
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkTeal)
                                ) {
                                    Text("تأكيد الترتيب", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Immediate Feedback Card matching Screenshot 2
                if (isAnswerSubmitted) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("exercise_feedback_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAnswerCorrect) PrimaryDarkTeal else Color(0xFF6B1D1D)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Text(
                                text = if (isAnswerCorrect) "إجابة صحيحة!" else "إجابة غير صحيحة",
                                color = GoldAccent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = TextHelper.formatArabic(exercise.explanation, settings.showTashkeel),
                                color = Color.White,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Bottom Action Button: "السؤال التالي" in Gold (#F2B84B) matching Screenshot 2
            if (isAnswerSubmitted) {
                Button(
                    onClick = {
                        if (questionIndex < totalQuestions - 1) {
                            questionIndex++
                        } else {
                            viewModel.repository.saveExerciseScore(lesson.id, score, totalQuestions)
                            isQuizCompleted = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(vertical = 4.dp)
                        .testTag("next_question_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldAccent,
                        contentColor = PrimaryDarkTeal
                    )
                ) {
                    Text(
                        text = if (questionIndex < totalQuestions - 1) "السؤال التالي" else "عرض النتيجة النهائية",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun QuizResultView(
    score: Int,
    total: Int,
    lessonTitle: String,
    onRetry: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(if (score >= total / 2) GoldLight else ErrorBgLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = if (score >= total / 2) GoldAccent else ErrorRed,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (score >= total / 2) "أحسنت! تدريب متميز" else "تحتاج لمراجعة سريعة",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "تم إكمال تمارين: $lessonTitle",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryLight
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryDarkTeal)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "النتيجة النهائية",
                    color = GoldAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$score / $total",
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                val percentage = (score.toFloat() / total * 100).toInt()
                Text(
                    text = "نسبة الإتقان: $percentage%",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("retry_quiz_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "إعادة التمرين", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onFinish,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("finish_quiz_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryDarkTeal,
                    contentColor = Color.White
                )
            ) {
                Text(text = "العودة للدروس", fontWeight = FontWeight.Bold)
            }
        }
    }
}
