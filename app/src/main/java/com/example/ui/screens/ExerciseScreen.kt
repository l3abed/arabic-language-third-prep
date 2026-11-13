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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.model.ExerciseItem
import com.example.model.MistakeReviewItem
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

@OptIn(ExperimentalLayoutApi::class)
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
    val reviewItems by viewModel.reviewSession.collectAsState()

    // Either one lesson's exercises, or a spaced-review session across lessons.
    val review: List<MistakeReviewItem>? = reviewItems
    val lesson = if (review == null) lessons.firstOrNull { it.id == activeLessonId } else null
    val sessionId = if (review != null) "review" else (lesson?.id ?: "none")
    val sessionTitle = if (review != null) "مراجعة المتباعدة" else (lesson?.title ?: "")
    val exercises: List<ExerciseItem> = review?.mapNotNull { it.exercise } ?: lesson?.exercises.orEmpty()
    // Every question knows its lesson, so answers are recorded against the right lesson.
    val lessonIdFor: (Int) -> String = { index ->
        if (review != null) review.getOrNull(index)?.lessonId.orEmpty() else lesson?.id.orEmpty()
    }

    if (exercises.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("لا توجد تمارين لهذا الدرس")
        }
        return
    }

    var questionIndex by remember(sessionId) { mutableIntStateOf(0) }
    var score by remember(sessionId) { mutableIntStateOf(0) }
    var isAnswerSubmitted by remember(sessionId, questionIndex) { mutableStateOf(false) }
    var isAnswerCorrect by remember(sessionId, questionIndex) { mutableStateOf(false) }
    var selectedOptionIndex by remember(sessionId, questionIndex) { mutableStateOf<Int?>(null) }
    var selectedBoolAnswer by remember(sessionId, questionIndex) { mutableStateOf<Boolean?>(null) }
    val orderedWords = remember(sessionId, questionIndex) { mutableStateListOf<String>() }
    var isQuizCompleted by remember(sessionId) { mutableStateOf(false) }
    var fillText by remember(sessionId, questionIndex) { mutableStateOf("") }
    var shortText by remember(sessionId, questionIndex) { mutableStateOf("") }
    var matchSelections by remember(sessionId, questionIndex) { mutableStateOf(mapOf<String, String>()) }
    // Options are shown in a random order (fresh for each question) so the answer's position
    // gives nothing away. Selection and correctness still use the original option indices.
    val optionOrder = remember(sessionId, questionIndex) {
        val count = (exercises.getOrNull(questionIndex) as? ExerciseItem.Mcq)?.options?.size ?: 0
        (0 until count).shuffled()
    }

    val exercise = exercises.getOrNull(questionIndex)
    val totalQuestions = exercises.size

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isQuizCompleted) {
            // Result screen
            QuizResultView(
                score = score,
                total = totalQuestions,
                lessonTitle = sessionTitle,
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

            // Top Bar
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

                // Title
                Text(
                    text = if (review != null) "مراجعة • ${review.size} أسئلة مستحقة" else "تمرين • ${TextHelper.formatArabic(sessionTitle, settings.showTashkeel)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 17.sp,
                    maxLines = 1,
                    modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
                    textAlign = TextAlign.Center
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

            // Segmented progress bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
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
                // Question Card (White)
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
                        val typeLabel = when (exercise) {
                            is ExerciseItem.Mcq -> "اختر الإجابة الصحيحة"
                            is ExerciseItem.TrueFalse -> "حدد صح أم خطأ"
                            is ExerciseItem.OrderWords -> "رتّب الكلمات بالترتيب الصحيح"
                            is ExerciseItem.FillBlank -> "أكمل الفراغ بالكلمة المناسبة"
                            is ExerciseItem.Matching -> "طابق كل عنصر بما يناسبه"
                            is ExerciseItem.ShortAnswer -> "أجب إجابة موجزة ومعلَّلة"
                            else -> ""
                        }
                        Text(
                            text = typeLabel,
                            color = TextSecondaryLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = TextHelper.formatArabic(exercise.prompt, settings.showTashkeel),
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

                // Options / Interaction based on Exercise Type
                when (exercise) {
                    is ExerciseItem.Mcq -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            optionOrder.forEach { optIndex ->
                                val optionText = exercise.options[optIndex]
                                val isSelected = selectedOptionIndex == optIndex
                                val isCorrectOption = optIndex == exercise.answerIndex

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
                                            val correct = optIndex == exercise.answerIndex
                                            isAnswerCorrect = correct
                                            if (correct) {
                                                score++
                                                viewModel.recordReviewCorrect(lessonIdFor(questionIndex), questionIndex)
                                            } else {
                                                viewModel.recordMistake(lessonIdFor(questionIndex), questionIndex)
                                            }
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
                                            textAlign = TextAlign.Start,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    is ExerciseItem.TrueFalse -> {
                        val options = listOf(Pair("صحيح", true), Pair("خطأ", false))
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            options.forEach { (label, boolValue) ->
                                val isSelected = selectedBoolAnswer == boolValue
                                val isCorrectOption = boolValue == exercise.answer

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
                                        .testTag("exercise_tf_$label")
                                        .clip(RoundedCornerShape(14.dp))
                                        .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
                                        .clickable(enabled = !isAnswerSubmitted) {
                                            selectedBoolAnswer = boolValue
                                            isAnswerSubmitted = true
                                            val correct = boolValue == exercise.answer
                                            isAnswerCorrect = correct
                                            if (correct) {
                                                score++
                                                viewModel.recordReviewCorrect(lessonIdFor(questionIndex), questionIndex)
                                            } else {
                                                viewModel.recordMistake(lessonIdFor(questionIndex), questionIndex)
                                            }
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 18.dp, vertical = 16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
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
                                            text = label,
                                            fontFamily = AmiriFontFamily,
                                            fontSize = (22 * settings.fontScale).sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Start
                                        )
                                    }
                                }
                            }
                        }
                    }

                    is ExerciseItem.OrderWords -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Target ordered words container (الجملة المركبة)
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
                                        text = "الجملة المركبة (انقر على الكلمة لإزالتها):",
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
                                        @OptIn(ExperimentalLayoutApi::class)
                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            orderedWords.forEach { word ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(PrimaryDarkTeal)
                                                        .defaultMinSize(minHeight = 44.dp)
                                                        .clickable(enabled = !isAnswerSubmitted) {
                                                            orderedWords.remove(word)
                                                        }
                                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = TextHelper.formatArabic(word, settings.showTashkeel),
                                                        color = Color.White,
                                                        fontFamily = AmiriFontFamily,
                                                        fontSize = 18.sp,
                                                        lineHeight = 28.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Source scattered words (shuffled on display)
                            val shuffledWords = remember(sessionId, questionIndex) {
                                exercise.words.shuffled()
                            }
                            val availableWords = shuffledWords.filter { !orderedWords.contains(it) }
                            if (availableWords.isNotEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    availableWords.chunked(2).forEach { rowWords ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            rowWords.forEach { word ->
                                                Card(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .defaultMinSize(minWidth = 120.dp, minHeight = 56.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .border(1.5.dp, PrimaryDarkTeal, RoundedCornerShape(12.dp))
                                                        .clickable(enabled = !isAnswerSubmitted) {
                                                            orderedWords.add(word)
                                                        },
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = MaterialTheme.colorScheme.surface
                                                    ),
                                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .defaultMinSize(minHeight = 56.dp)
                                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = TextHelper.formatArabic(word, settings.showTashkeel),
                                                            fontFamily = AmiriFontFamily,
                                                            fontSize = 18.sp,
                                                            lineHeight = 28.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = PrimaryDarkTeal,
                                                            textAlign = TextAlign.Center,
                                                            softWrap = true
                                                        )
                                                    }
                                                }
                                            }
                                            if (rowWords.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }

                            if (!isAnswerSubmitted && orderedWords.size == exercise.words.size) {
                                Button(
                                    onClick = {
                                        isAnswerSubmitted = true
                                        val correct = exercise.isCorrect(orderedWords.toList())
                                        isAnswerCorrect = correct
                                        if (correct) {
                                            score++
                                            viewModel.recordReviewCorrect(lessonIdFor(questionIndex), questionIndex)
                                        } else {
                                            viewModel.recordMistake(lessonIdFor(questionIndex), questionIndex)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkTeal)
                                ) {
                                    Text("تأكيد الترتيب", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    else -> {}
                }

                // Shared "check" helper for the three new types
                val checkAndRecord: (Boolean) -> Unit = { correct ->
                    isAnswerSubmitted = true
                    isAnswerCorrect = correct
                    if (correct) {
                        score++
                        viewModel.recordReviewCorrect(lessonIdFor(questionIndex), questionIndex)
                    } else {
                        viewModel.recordMistake(lessonIdFor(questionIndex), questionIndex)
                    }
                }

                when (exercise) {
                    is ExerciseItem.FillBlank -> {
                        val options = remember(sessionId, questionIndex) { exercise.options.shuffled() }
                        val parts = exercise.sentence.split("____", limit = 2)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = TextHelper.formatArabic(exercise.sentence.replace("____", "…"), settings.showTashkeel),
                                    fontFamily = AmiriFontFamily,
                                    fontSize = 20.sp,
                                    lineHeight = 34.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Start
                                )
                                if (exercise.options.isNotEmpty()) {
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        options.forEach { word ->
                                            val picked = fillText == word
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (picked) PrimaryDarkTeal else Color.White)
                                                    .border(1.dp, PrimaryDarkTeal.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                                    .clickable(enabled = !isAnswerSubmitted) { fillText = word }
                                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = TextHelper.formatArabic(word, settings.showTashkeel),
                                                    fontFamily = AmiriFontFamily,
                                                    fontSize = 17.sp,
                                                    color = if (picked) Color.White else PrimaryDarkTeal
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    OutlinedTextField(
                                        value = fillText,
                                        onValueChange = { fillText = it },
                                        enabled = !isAnswerSubmitted,
                                        singleLine = true,
                                        placeholder = { Text(exercise.hint.ifBlank { "اكتب الكلمة" }) },
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            textDirection = androidx.compose.ui.text.style.TextDirection.Rtl,
                                            fontFamily = AmiriFontFamily,
                                            fontSize = 18.sp
                                        )
                                    )
                                }
                                if (!isAnswerSubmitted) {
                                    Button(
                                        onClick = { if (fillText.isNotBlank()) checkAndRecord(exercise.isCorrect(fillText)) },
                                        enabled = fillText.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkTeal)
                                    ) { Text("تأكيد", fontWeight = FontWeight.Bold) }
                                }
                            }
                        }
                    }

                    is ExerciseItem.Matching -> {
                        val lefts = remember(sessionId, questionIndex) { exercise.pairs.map { it.first } }
                        val rights = remember(sessionId, questionIndex) { exercise.pairs.map { it.second }.shuffled() }
                        val allMatched = matchSelections.size == lefts.size
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                lefts.forEach { left ->
                                    val chosen = matchSelections[left]
                                    val leftCorrect = exercise.pairs.first { it.first == left }.second
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = TextHelper.formatArabic(left, settings.showTashkeel),
                                            fontFamily = AmiriFontFamily,
                                            fontSize = 17.sp,
                                            modifier = Modifier.weight(1f),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Start
                                        )
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    when {
                                                        isAnswerSubmitted && chosen == leftCorrect -> Color(0xFFE3F2E8)
                                                        isAnswerSubmitted && chosen != null -> Color(0xFFFDE8E8)
                                                        else -> Color.White
                                                    }
                                                )
                                                .border(1.dp, PrimaryDarkTeal.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                                .padding(10.dp)
                                        ) {
                                            Text(
                                                text = if (chosen == null) "اختر…" else TextHelper.formatArabic(chosen, settings.showTashkeel),
                                                fontFamily = AmiriFontFamily,
                                                fontSize = 16.sp,
                                                color = PrimaryDarkTeal
                                            )
                                        }
                                    }
                                    if (!isAnswerSubmitted) {
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            rights.filter { r -> !matchSelections.containsValue(r) || matchSelections[left] == r }
                                                .forEach { r ->
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .border(1.dp, GoldAccent, RoundedCornerShape(8.dp))
                                                            .clickable { matchSelections = matchSelections + (left to r) }
                                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                                    ) {
                                                        Text(
                                                            text = TextHelper.formatArabic(r, settings.showTashkeel),
                                                            fontFamily = AmiriFontFamily,
                                                            fontSize = 14.sp,
                                                            color = PrimaryDarkTeal
                                                        )
                                                    }
                                                }
                                        }
                                    }
                                }
                                if (!isAnswerSubmitted) {
                                    Button(
                                        onClick = {
                                            val correct = lefts.all { l -> matchSelections[l] == exercise.pairs.first { it.first == l }.second }
                                            checkAndRecord(correct)
                                        },
                                        enabled = allMatched,
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkTeal)
                                    ) { Text("تأكيد المزاوجة", fontWeight = FontWeight.Bold) }
                                }
                            }
                        }
                    }

                    is ExerciseItem.ShortAnswer -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = TextHelper.formatArabic(exercise.question, settings.showTashkeel),
                                    fontFamily = AmiriFontFamily,
                                    fontSize = 19.sp,
                                    lineHeight = 30.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Start
                                )
                                OutlinedTextField(
                                    value = shortText,
                                    onValueChange = { shortText = it },
                                    enabled = !isAnswerSubmitted,
                                    minLines = 2,
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        textDirection = androidx.compose.ui.text.style.TextDirection.Rtl,
                                        fontFamily = AmiriFontFamily,
                                        fontSize = 17.sp
                                    )
                                )
                                if (!isAnswerSubmitted) {
                                    Button(
                                        onClick = {
                                            val fraction = exercise.score(shortText)
                                            checkAndRecord(fraction >= 0.99)
                                        },
                                        enabled = shortText.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkTeal)
                                    ) { Text("تأكيد", fontWeight = FontWeight.Bold) }
                                } else {
                                    Text(
                                        text = "الإجابة النموذجية: ${TextHelper.formatArabic(exercise.answer, settings.showTashkeel)}",
                                        fontFamily = AmiriFontFamily,
                                        fontSize = 16.sp,
                                        lineHeight = 26.sp,
                                        color = SuccessGreen,
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }
                        }
                    }

                    else -> {}
                }

                // Immediate Feedback Card displaying explanation
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
                                text = if (isAnswerCorrect) "إجابة صحيحة! أحسنت" else "إجابة غير صحيحة",
                                color = GoldAccent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            (exercise as? ExerciseItem.OrderWords)?.let { ow ->
                                Text(
                                    text = (if (ow.allValidSentences.size > 1) "الجمل الصحيحة:\n" else "الجملة الصحيحة:\n") +
                                        TextHelper.formatArabic(
                                            ow.allValidSentences.joinToString("\n"),
                                            settings.showTashkeel
                                        ),
                                    color = Color.White,
                                    fontFamily = AmiriFontFamily,
                                    fontSize = 17.sp,
                                    lineHeight = 26.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
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

            // Bottom Action Button: "السؤال التالي"
            if (isAnswerSubmitted) {
                Button(
                    onClick = {
                        if (questionIndex < totalQuestions - 1) {
                            questionIndex++
                            fillText = ""
                            shortText = ""
                            matchSelections = emptyMap()
                        } else {
                            if (review == null) viewModel.repository.saveExerciseScore(lesson?.id.orEmpty(), score, totalQuestions)
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
