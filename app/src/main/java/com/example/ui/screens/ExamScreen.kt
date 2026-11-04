package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.model.AnswerItem
import com.example.model.ExamData
import com.example.model.ExamQuestion
import com.example.model.ExamSection
import com.example.model.RubricItem
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.BorderLight
import com.example.ui.theme.ErrorBgLight
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.PrimaryDarkTeal
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMutedLight
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.util.TextHelper
import com.example.viewmodel.ArabicAppViewModel

@Composable
fun ExamScreen(viewModel: ArabicAppViewModel) {
    BackHandler {
        viewModel.navigateBackToTabs()
    }

    val exam = viewModel.homeworkExam
    val settings by viewModel.settings.collectAsState()
    val scrollState = rememberScrollState()

    val isSubmitted = viewModel.isExamSubmitted
    val showCorrection = viewModel.showCorrectionModel
    val validationError = viewModel.examValidationError

    if (exam == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "تعذر تحميل بيانات الفرض المنزلي",
                fontFamily = AmiriFontFamily,
                fontSize = 18.sp,
                color = TextSecondaryLight
            )
        }
        return
    }

    Scaffold(
        topBar = {
            Surface(
                color = PrimaryDarkTeal,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.navigateBackToTabs() },
                        modifier = Modifier.testTag("exam_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع",
                            tint = Color.White
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = exam.title,
                            fontFamily = AmiriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White,
                            textAlign = TextAlign.End
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المدة: ${exam.duration}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Text(
                                text = "•",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "اللغة العربية",
                                fontSize = 12.sp,
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSubmitted) {
                            // Total points badge: displayed only after submission
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GoldLight.copy(alpha = 0.35f))
                                    .border(1.dp, GoldAccent, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "مجموع نقط الفرض: 20 / 20 نقطة",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = PrimaryDarkTeal
                                )
                            }
                        } else {
                            // Pre-submission status badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PrimaryDarkTeal.copy(alpha = 0.1f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "10 أسئلة مطلوبة كاملة",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = PrimaryDarkTeal
                                )
                            }
                        }

                        Text(
                            text = "السنة الثالثة إعدادي",
                            fontSize = 13.sp,
                            color = TextSecondaryLight
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isSubmitted) {
                            "تم تسليم الفرض ومراجعة إجاباتك مقابل عناصر الإجابة وسلم التنقيط."
                        } else {
                            "أجب عن جميع الأسئلة في الخانات المخصصة. لن يظهر نموذج التصحيح وسلم التنقيط إلا بعد الضغط على زر «تسليم الفرض»."
                        },
                        fontSize = 13.sp,
                        color = TextMutedLight,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Start
                    )
                }
            }

            // Validation Error Alert Banner
            if (validationError != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ErrorBgLight),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, ErrorRed)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = validationError,
                            color = ErrorRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Submission Success Banner
            if (isSubmitted) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F3EE)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, SuccessGreen)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "تم تسليم الفرض بنجاح! تم فتح نموذج التصحيح وسلم التنقيط لكل سؤال أدناه للمراجعة والتقويم الذاتي.",
                            color = PrimaryDarkTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 1. Passage Card (نص الانطلاق)
            PassageCard(
                title = exam.passage.title,
                text = exam.passage.text,
                showTashkeel = settings.showTashkeel,
                fontScale = settings.fontScale
            )

            // 2. Sections (A, B, C)
            exam.sections.forEach { section ->
                SectionCard(
                    section = section,
                    isSubmitted = isSubmitted,
                    showCorrection = showCorrection,
                    examAnswers = viewModel.examAnswers,
                    onAnswerChange = { key, value ->
                        viewModel.updateExamAnswer(key, value)
                    },
                    showTashkeel = settings.showTashkeel,
                    fontScale = settings.fontScale
                )
            }

            // Bottom Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!isSubmitted) {
                    // Primary Submit Button (before submission)
                    Button(
                        onClick = {
                            viewModel.submitExam()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("submit_exam_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkTeal)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Send,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تسليم الفرض",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                    }
                } else {
                    // After Submission: Equal-width action buttons in a grid row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.toggleCorrectionModel() },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("toggle_correction_model_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showCorrection) GoldAccent else PrimaryDarkTeal
                            )
                        ) {
                            Text(
                                text = if (showCorrection) "إخفاء التصحيح" else "عرض التصحيح",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (showCorrection) PrimaryDarkTeal else Color.White,
                                textAlign = TextAlign.Center
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.resetExam() },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("reset_exam_answers_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = null,
                                tint = TextSecondaryLight,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إعادة المحاولة",
                                color = TextSecondaryLight,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PassageCard(
    title: String,
    text: String,
    showTashkeel: Boolean,
    fontScale: Float
) {
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "طي النص" else "توسيع النص",
                        tint = PrimaryDarkTeal
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "نص الانطلاق",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PrimaryDarkTeal
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(GoldAccent)
                    )
                }
            }

            Text(
                text = title,
                fontFamily = AmiriFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (20 * fontScale).sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                textAlign = TextAlign.Start
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = BorderLight
                    )
                    Text(
                        text = TextHelper.formatArabic(text, showTashkeel),
                        fontFamily = AmiriFontFamily,
                        fontSize = (17 * fontScale).sp,
                        lineHeight = (29 * fontScale).sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Justify,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    section: ExamSection,
    isSubmitted: Boolean,
    showCorrection: Boolean,
    examAnswers: Map<String, String>,
    onAnswerChange: (String, String) -> Unit,
    showTashkeel: Boolean,
    fontScale: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Points badge displayed only after submission
                if (isSubmitted) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryDarkTeal.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${section.points} نقط",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PrimaryDarkTeal
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = section.title,
                    fontFamily = AmiriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PrimaryDarkTeal,
                    textAlign = TextAlign.End
                )
            }

            HorizontalDivider(color = BorderLight)

            // Questions in Section A and B
            section.questions.forEach { question ->
                val answerKey = "${section.id}_q${question.no}"
                val currentAnswer = examAnswers[answerKey] ?: ""

                QuestionCard(
                    sectionId = section.id,
                    question = question,
                    studentAnswer = currentAnswer,
                    isSubmitted = isSubmitted,
                    showCorrection = showCorrection,
                    onAnswerChange = { onAnswerChange(answerKey, it) },
                    showTashkeel = showTashkeel,
                    fontScale = fontScale
                )
            }

            // Section C (Expression / Writing)
            if (section.id == "C" || section.topic != null) {
                val writingKey = "${section.id}_writing"
                val studentWriting = examAnswers[writingKey] ?: ""

                WritingSectionCard(
                    section = section,
                    studentWriting = studentWriting,
                    isSubmitted = isSubmitted,
                    showCorrection = showCorrection,
                    onWritingChange = { onAnswerChange(writingKey, it) },
                    fontScale = fontScale
                )
            }
        }
    }
}

@Composable
private fun QuestionCard(
    sectionId: String,
    question: ExamQuestion,
    studentAnswer: String,
    isSubmitted: Boolean,
    showCorrection: Boolean,
    onAnswerChange: (String) -> Unit,
    showTashkeel: Boolean,
    fontScale: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Question Header: Number and points (points visible after submission)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSubmitted) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GoldAccent.copy(alpha = 0.25f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${question.points} ن",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDarkTeal
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(4.dp))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "السؤال ${question.no}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDarkTeal
                )
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(PrimaryDarkTeal),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = question.no,
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Question Prompt in Arabic
        Text(
            text = TextHelper.formatArabic(question.prompt, showTashkeel),
            fontFamily = AmiriFontFamily,
            fontSize = (17 * fontScale).sp,
            lineHeight = (26 * fontScale).sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )

        // Student Answer Field
        OutlinedTextField(
            value = studentAnswer,
            onValueChange = onAnswerChange,
            enabled = !isSubmitted,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("exam_answer_${sectionId}_${question.no}"),
            placeholder = {
                Text(
                    text = "اكتب إجابتك هنا...",
                    fontFamily = AmiriFontFamily,
                    fontSize = 15.sp,
                    color = TextMutedLight
                )
            },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryDarkTeal,
                unfocusedBorderColor = if (studentAnswer.isNotBlank()) PrimaryDarkTeal.copy(alpha = 0.5f) else BorderLight,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                disabledTextColor = MaterialTheme.colorScheme.onSurface
            ),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = AmiriFontFamily,
                fontSize = (16 * fontScale).sp,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onSurface
            ),
            minLines = if (question.points >= 2.0) 3 else 1
        )

        // Model Answer displayed ONLY after submission
        AnimatedVisibility(
            visible = isSubmitted && showCorrection,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            QuestionCorrectionCard(
                answerGroups = question.answerGroups,
                grading = question.grading,
                fontScale = fontScale
            )
        }
    }
}

@Composable
private fun QuestionCorrectionCard(
    answerGroups: List<List<AnswerItem>>,
    grading: String,
    fontScale: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F3EE)),
        border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "عناصر الإجابة النموذجية:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PrimaryDarkTeal
                )
            }

            // Render each answer group as a clean card without any English keys or symbols
            answerGroups.forEach { group ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        group.forEach { item ->
                            val hasLabel = item.label.isNotBlank()
                            val hasValue = item.value.isNotBlank()

                            when {
                                !hasLabel && hasValue -> {
                                    // Empty label: show value only
                                    Text(
                                        text = item.value,
                                        fontFamily = AmiriFontFamily,
                                        fontSize = (15 * fontScale).sp,
                                        lineHeight = (23 * fontScale).sp,
                                        color = Color(0xFF133E32),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                hasLabel && !hasValue -> {
                                    // Empty value: show label only as heading
                                    Text(
                                        text = item.label,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = PrimaryDarkTeal,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                hasLabel && hasValue -> {
                                    // Both exist: show label then value
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = item.label,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = PrimaryDarkTeal
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = item.value,
                                            fontFamily = AmiriFontFamily,
                                            fontSize = (15 * fontScale).sp,
                                            lineHeight = (23 * fontScale).sp,
                                            color = Color(0xFF133E32),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Grading criteria
            if (grading.isNotBlank()) {
                HorizontalDivider(color = SuccessGreen.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "سلم التنقيط:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDarkTeal
                    )
                    Text(
                        text = grading,
                        fontSize = 12.sp,
                        color = TextSecondaryLight,
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WritingSectionCard(
    section: ExamSection,
    studentWriting: String,
    isSubmitted: Boolean,
    showCorrection: Boolean,
    onWritingChange: (String) -> Unit,
    fontScale: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSubmitted) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GoldAccent.copy(alpha = 0.25f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${section.points} ن",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDarkTeal
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = "موضوع الإنشاء المطلوب",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryDarkTeal
            )
        }

        // Topic Prompt
        section.topic?.let { topic ->
            Text(
                text = topic,
                fontFamily = AmiriFontFamily,
                fontSize = (17 * fontScale).sp,
                lineHeight = (26 * fontScale).sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Student Writing Textarea
        OutlinedTextField(
            value = studentWriting,
            onValueChange = onWritingChange,
            enabled = !isSubmitted,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("exam_answer_C_writing"),
            placeholder = {
                Text(
                    text = "اكتب نص اليومية هنا (من 8 إلى 10 أسطر)...",
                    fontFamily = AmiriFontFamily,
                    fontSize = 15.sp,
                    color = TextMutedLight
                )
            },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryDarkTeal,
                unfocusedBorderColor = if (studentWriting.isNotBlank()) PrimaryDarkTeal.copy(alpha = 0.5f) else BorderLight,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                disabledTextColor = MaterialTheme.colorScheme.onSurface
            ),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = AmiriFontFamily,
                fontSize = (16 * fontScale).sp,
                lineHeight = (26 * fontScale).sp,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onSurface
            ),
            minLines = 7
        )

        // Rubric & Model Text: strictly shown only AFTER submission
        AnimatedVisibility(
            visible = isSubmitted && showCorrection,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Rubric Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F3EE)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "معايير تقويم التعبير والإنشاء (سلم التنقيط - 6 نقط):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PrimaryDarkTeal
                        )

                        section.rubric.forEach { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(PrimaryDarkTeal.copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${item.points} ن",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = PrimaryDarkTeal
                                        )
                                    }

                                    Text(
                                        text = item.criterion,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }

                        section.gradingNote?.let { note ->
                            HorizontalDivider(color = SuccessGreen.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
                            Text(
                                text = "ملاحظة: $note",
                                fontSize = 12.sp,
                                color = TextSecondaryLight,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // Model Text
                section.modelText?.let { model ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F3EE)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "نص إنشائي نموذجي (للاسترشاد):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = PrimaryDarkTeal
                                )
                            }

                            Text(
                                text = model,
                                fontFamily = AmiriFontFamily,
                                fontSize = (16 * fontScale).sp,
                                lineHeight = (27 * fontScale).sp,
                                color = Color(0xFF133E32),
                                textAlign = TextAlign.Justify,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
