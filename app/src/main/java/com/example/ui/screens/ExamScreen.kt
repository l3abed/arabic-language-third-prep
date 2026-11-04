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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Refresh
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
import com.example.model.ExamData
import com.example.model.ExamQuestion
import com.example.model.ExamSection
import com.example.model.RubricItem
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.BorderLight
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

    // Dynamic total points calculation from sections: 8 + 6 + 6 = 20
    val totalPoints = remember(exam) {
        exam.sections.sumOf { it.points }
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
                                text = exam.subject,
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
            // Exam Header Card with points badge
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
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
                        // Total points badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldLight.copy(alpha = 0.3f))
                                .border(1.dp, GoldAccent, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "المجموع: $totalPoints / 20 نقطة",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PrimaryDarkTeal
                            )
                        }

                        Text(
                            text = "السنة الثالثة إعدادي",
                            fontSize = 13.sp,
                            color = TextSecondaryLight
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "فرض منزلي شامل يغطي مجالات القراءة والفهم، والدرس اللغوي، والتعبير والإنشاء مع نموذج التصحيح وسلم التنقيط.",
                        fontSize = 13.sp,
                        color = TextMutedLight,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Start
                    )
                }
            }

            // 1. Passage Card (نص الانطلاق)
            PassageCard(
                title = exam.passage.title,
                text = exam.passage.text,
                showTashkeel = settings.showTashkeel,
                fontScale = settings.fontScale
            )

            // 2. Sections
            exam.sections.forEachIndexed { sectionIndex, section ->
                SectionCard(
                    section = section,
                    showCorrectionModel = viewModel.showCorrectionModel,
                    examAnswers = viewModel.examAnswers,
                    onAnswerChange = { key, value ->
                        viewModel.updateExamAnswer(key, value)
                    },
                    showTashkeel = settings.showTashkeel,
                    fontScale = settings.fontScale
                )
            }

            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.toggleCorrectionModel()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("toggle_correction_model_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.showCorrectionModel) GoldAccent else PrimaryDarkTeal
                    )
                ) {
                    Icon(
                        imageVector = if (viewModel.showCorrectionModel) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = if (viewModel.showCorrectionModel) PrimaryDarkTeal else Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (viewModel.showCorrectionModel) "إخفاء نموذج التصحيح" else "نموذج التصحيح وعناصر الإجابة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (viewModel.showCorrectionModel) PrimaryDarkTeal else Color.White
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.resetExam() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("reset_exam_answers_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        tint = TextSecondaryLight
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "إعادة تعيين الإجابات",
                        color = TextSecondaryLight,
                        fontSize = 14.sp
                    )
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
    showCorrectionModel: Boolean,
    examAnswers: Map<String, String>,
    onAnswerChange: (String, String) -> Unit,
    showTashkeel: Boolean,
    fontScale: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                // Section Points Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryDarkTeal.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${section.points} نقط",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = PrimaryDarkTeal
                    )
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

            // Section Questions
            section.questions.forEach { question ->
                val answerKey = "${section.id}_q${question.no}"
                val currentAnswer = examAnswers[answerKey] ?: ""

                QuestionItem(
                    sectionId = section.id,
                    question = question,
                    studentAnswer = currentAnswer,
                    onAnswerChange = { onAnswerChange(answerKey, it) },
                    showCorrectionModel = showCorrectionModel,
                    showTashkeel = showTashkeel,
                    fontScale = fontScale
                )
            }

            // If Section C, show rubric and model_text if applicable
            if (section.rubric.isNotEmpty()) {
                RubricSection(
                    rubric = section.rubric,
                    modelText = section.modelText,
                    showCorrectionModel = showCorrectionModel,
                    fontScale = fontScale
                )
            }
        }
    }
}

@Composable
private fun QuestionItem(
    sectionId: String,
    question: ExamQuestion,
    studentAnswer: String,
    onAnswerChange: (String) -> Unit,
    showCorrectionModel: Boolean,
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
        // Question Header: No & Points
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                        text = "${question.no}",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Question Prompt
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

        // Student Answer Input Field
        OutlinedTextField(
            value = studentAnswer,
            onValueChange = onAnswerChange,
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
                unfocusedBorderColor = BorderLight,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = AmiriFontFamily,
                fontSize = (16 * fontScale).sp,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onSurface
            ),
            minLines = if (question.points >= 2.0) 3 else 1
        )

        // Correction Model Display
        AnimatedVisibility(
            visible = showCorrectionModel,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            CorrectionModelCard(
                modelRaw = question.modelRaw,
                grading = question.grading,
                fontScale = fontScale
            )
        }
    }
}

@Composable
private fun CorrectionModelCard(
    modelRaw: Any?,
    grading: String?,
    fontScale: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F3EE)
        ),
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
                    fontSize = 13.sp,
                    color = PrimaryDarkTeal
                )
            }

            // Render model based on type: String -> paragraph; List -> bullet list; Map -> labeled fields
            when (modelRaw) {
                is String -> {
                    Text(
                        text = modelRaw,
                        fontFamily = AmiriFontFamily,
                        fontSize = (16 * fontScale).sp,
                        lineHeight = (25 * fontScale).sp,
                        color = Color(0xFF133E32),
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is List<*> -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        modelRaw.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = item?.toString() ?: "",
                                    fontFamily = AmiriFontFamily,
                                    fontSize = (15 * fontScale).sp,
                                    lineHeight = (24 * fontScale).sp,
                                    color = Color(0xFF133E32),
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "•",
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                is Map<*, *> -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        modelRaw.forEach { (k, v) ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.8f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = v?.toString() ?: "",
                                        fontFamily = AmiriFontFamily,
                                        fontSize = (15 * fontScale).sp,
                                        color = Color(0xFF133E32),
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${k}:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryDarkTeal,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {
                    Text(
                        text = modelRaw?.toString() ?: "",
                        fontFamily = AmiriFontFamily,
                        fontSize = (15 * fontScale).sp,
                        color = Color(0xFF133E32)
                    )
                }
            }

            // Grading criteria / سلم التنقيط
            if (!grading.isNullOrBlank()) {
                HorizontalDivider(color = SuccessGreen.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = grading,
                        fontSize = 12.sp,
                        color = TextSecondaryLight,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "سلم التنقيط:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDarkTeal
                    )
                }
            }
        }
    }
}

@Composable
private fun RubricSection(
    rubric: List<RubricItem>,
    modelText: String?,
    showCorrectionModel: Boolean,
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
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(GoldAccent.copy(alpha = 0.25f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "6 نقط",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDarkTeal
                )
            }

            Text(
                text = "معايير تقويم التعبير والإنشاء",
                fontFamily = AmiriFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = PrimaryDarkTeal
            )
        }

        // Rubric Items Table
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            rubric.forEach { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PrimaryDarkTeal.copy(alpha = 0.1f))
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
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f).padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        // Model Text for Expression / الإنشاء النموذجية
        AnimatedVisibility(
            visible = showCorrectionModel && !modelText.isNullOrBlank(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
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
                        .padding(14.dp),
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
                            text = "نص إنشائي نموذجي (يسترشد به المتعلم):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PrimaryDarkTeal
                        )
                    }

                    Text(
                        text = modelText ?: "",
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
