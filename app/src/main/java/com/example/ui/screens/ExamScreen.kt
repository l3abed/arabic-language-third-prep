package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Timer
import kotlinx.coroutines.delay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.model.ExamTable
import com.example.model.ExamData
import com.example.model.ExamGrade
import com.example.model.GradeMode
import com.example.model.QuestionGrade
import com.example.model.SectionGrade
import com.example.util.ExamGrader
import com.example.model.ExamQuestion
import com.example.model.ExamSection
import com.example.model.ExamTopic
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
import com.example.ui.theme.TextSecondaryLight
import com.example.util.TextHelper
import com.example.viewmodel.ArabicAppViewModel

@Composable
fun ExamScreen(viewModel: ArabicAppViewModel) {
    val exam by viewModel.currentExam.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val scrollState = rememberScrollState()

    val isSubmitted = viewModel.isExamSubmitted
    val isStarted = viewModel.isExamStarted
    val showCorrection = viewModel.showCorrectionModel
    val inProgress = isStarted && !isSubmitted

    var showLeaveDialog by remember { mutableStateOf(false) }
    var showSubmitDialog by remember { mutableStateOf(false) }

    // While an exam is running, leaving it means submitting it.
    BackHandler {
        if (inProgress) showLeaveDialog = true else viewModel.navigateBackToTabs()
    }

    if (exam == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "تعذر تحميل بيانات الفرض والامتحان",
                fontFamily = AmiriFontFamily,
                fontSize = 18.sp,
                color = TextSecondaryLight
            )
        }
        return
    }

    val currentExam = exam!!

    // The timer is derived from a saved end time, so it survives rotation and app restarts
    // and cannot be paused or reset.
    val endsAt = viewModel.examEndsAt[currentExam.id] ?: 0L
    val timed = endsAt > 0L
    val totalDurationSeconds = remember(currentExam.id, currentExam.duration) {
        ExamGrader.durationSeconds(currentExam.duration)
    }

    var nowMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(currentExam.id, inProgress, endsAt) {
        while (inProgress) {
            nowMs = System.currentTimeMillis()
            if (timed && nowMs >= endsAt) {
                viewModel.submitExam(auto = true)
                break
            }
            delay(1000L)
        }
    }
    val remainingSeconds = if (timed) ((endsAt - nowMs).coerceAtLeast(0L) / 1000L).toInt() else 0

    val grade: ExamGrade? = if (isSubmitted) viewModel.gradeCurrentExam() else null

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("الخروج يعني تسليم الفرض", fontWeight = FontWeight.Bold) },
            text = {
                Text("لا يمكنك مغادرة الامتحان أثناء المدة. إن خرجتَ الآن سيُسلَّم الفرض بما كتبتَه ولن تستطيع تعديله. هل تريد التسليم الآن؟")
            },
            confirmButton = {
                TextButton(onClick = {
                    showLeaveDialog = false
                    viewModel.submitExam()
                }) { Text("تسليم الآن", color = ErrorRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) { Text("متابعة الامتحان") }
            }
        )
    }

    if (showSubmitDialog) {
        val unanswered = viewModel.unansweredCount(currentExam)
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text("تسليم الفرض", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    if (unanswered > 0) {
                        "لم تجب عن $unanswered من الأسئلة، وستُحتسب لها نقطة الصفر. هل تريد التسليم الآن؟ لن تستطيع تعديل إجاباتك بعد ذلك."
                    } else {
                        "هل أنت متأكد من تسليم الفرض؟ لن تستطيع تعديل إجاباتك بعد ذلك."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSubmitDialog = false
                    viewModel.submitExam()
                }) { Text("تسليم", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) { Text("متابعة الكتابة") }
            }
        )
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (inProgress) {
                        // No navigation while the exam is running.
                        Spacer(modifier = Modifier.width(12.dp))
                    } else {
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

                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = currentExam.title,
                            fontFamily = AmiriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White,
                            textAlign = TextAlign.Start,
                            maxLines = 1
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المدة: ${currentExam.duration}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Text(
                                text = "•",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "${currentExam.declaredTotal} نقطة",
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
            if (!isStarted && !isSubmitted) {
                ExamIntroCard(
                    exam = currentExam,
                    durationSeconds = totalDurationSeconds,
                    onStart = { viewModel.startExam() },
                    onBack = { viewModel.navigateBackToTabs() }
                )
            } else {
            if (inProgress) {
                if (timed) {
                    val hours = remainingSeconds / 3600
                    val minutes = (remainingSeconds % 3600) / 60
                    val seconds = remainingSeconds % 60
                    val timeString = if (hours > 0) {
                        String.format("%02d:%02d:%02d", hours, minutes, seconds)
                    } else {
                        String.format("%02d:%02d", minutes, seconds)
                    }
                    val isUrgent = remainingSeconds in 1..300
                    val timerColor = if (isUrgent) Color(0xFFD84315) else MaterialTheme.colorScheme.primary

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUrgent) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(timerColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Timer,
                                    contentDescription = "المؤقت",
                                    tint = timerColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (isUrgent) "الوقت المتبقي قارب على الانتهاء" else "الوقت المتبقي (${currentExam.duration})",
                                    fontSize = 12.sp,
                                    color = if (isUrgent) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = timeString,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = AmiriFontFamily,
                                    color = timerColor
                                )
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ErrorBgLight),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed)
                ) {
                    Text(
                        text = if (timed) {
                            "تنبيه: يُمنع التنقل أو مغادرة الامتحان أثناء المدة، والمؤقت لا يتوقف ولو خرجتَ من الشاشة. عند انتهاء الوقت يُسلَّم الفرض تلقائياً بما كتبتَه."
                        } else {
                            "تنبيه: يُمنع التنقل أو مغادرة الامتحان قبل التسليم؛ الخروج منه يعني تسليم الفرض."
                        },
                        color = ErrorRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (isSubmitted && grade != null) {
                ExamResultCard(
                    grade = grade,
                    auto = viewModel.examAutoSubmitted[currentExam.id] == true
                )
            }

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
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryDarkTeal.copy(alpha = 0.1f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "المجموع: ${currentExam.declaredTotal} نقطة",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = PrimaryDarkTeal
                            )
                        }

                        Text(
                            text = when (currentExam.unit) {
                                1 -> "الوحدة الأولى: القيم الإسلامية"
                                2 -> "الوحدة الثانية: الوطنية والإنسانية"
                                3 -> "الوحدة الثالثة: المجال الحضاري"
                                4 -> "الوحدة الرابعة: المجال الاجتماعي والاقتصادي"
                                5 -> "الوحدة الخامسة: المجال السكاني"
                                6 -> "الوحدة السادسة: المجال الفني والثقافي"
                                else -> if (currentExam.semester == 2) "الأسدس الثاني: شامل" else "الأسدس الأول: شامل"
                            },
                            fontSize = 13.sp,
                            color = TextSecondaryLight
                        )
                    }

                    if (currentExam.source.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "المصدر: ${currentExam.source}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondaryLight,
                            textAlign = TextAlign.Start
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isSubmitted) {
                            "تم التسليم. قارن إجابتك بالإجابة النموذجية أسفل كل سؤال؛ التصحيح الآلي تقديري يعتمد مطابقة الكلمات."
                        } else {
                            "أجب عن الأسئلة في الخانات المخصصة. لن تظهر الإجابات النموذجية ونقطتك إلا بعد التسليم."
                        },
                        fontSize = 13.sp,
                        color = TextMutedLight,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Start
                    )
                }
            }

            // 1. Passage Card (نص الانطلاق)
            if (currentExam.passage.text.isNotBlank()) {
                PassageCard(
                    title = currentExam.passage.title,
                    text = currentExam.passage.text,
                    showTashkeel = settings.showTashkeel,
                    fontScale = settings.fontScale
                )
            }

            // 2. Sections (A, B, C) in order
            currentExam.sections.forEach { section ->
                SectionCard(
                    examId = currentExam.id,
                    section = section,
                    isSubmitted = isSubmitted,
                    showCorrection = showCorrection,
                    grade = grade,
                    ratings = viewModel.examRatings,
                    onRate = { index, rating -> viewModel.setRubricRating(section.id, index, rating) },
                    examAnswers = viewModel.examAnswers,
                    selectedTopicId = viewModel.getSelectedExamTopic(currentExam.id, section.id),
                    onSelectTopic = { topicId ->
                        viewModel.selectExamTopic(currentExam.id, section.id, topicId)
                    },
                    onAnswerChange = { key, value ->
                        viewModel.updateExamAnswer(key, value)
                    },
                    showTashkeel = settings.showTashkeel,
                    fontScale = settings.fontScale
                )
            }

            // Bottom Actions (Equal-width, keep-all typography)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!isSubmitted) {
                    Button(
                        onClick = { showSubmitDialog = true },
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
                            color = Color.White,
                            softWrap = false,
                            maxLines = 1
                        )
                    }
                } else {
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
                                textAlign = TextAlign.Center,
                                softWrap = false,
                                maxLines = 1
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
                                textAlign = TextAlign.Center,
                                softWrap = false,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            } // end of started / submitted content
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
                    val cleanTitle = title
                        .removePrefix("نص الانطلاق")
                        .trimStart(':', ' ', '،', '-', '–')
                        .trim()
                    if (cleanTitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldLight.copy(alpha = 0.35f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "عنوان النص: «$cleanTitle»",
                                fontFamily = AmiriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = (16 * fontScale).sp,
                                color = PrimaryDarkTeal,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    examId: String,
    section: ExamSection,
    isSubmitted: Boolean,
    showCorrection: Boolean,
    grade: ExamGrade?,
    ratings: Map<String, Float>,
    onRate: (Int, Float) -> Unit,
    examAnswers: Map<String, String>,
    selectedTopicId: String?,
    onSelectTopic: (String) -> Unit,
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
                            text = grade?.sections?.firstOrNull { it.sectionId == section.id }
                                ?.let { "${ExamGrader.formatScore(it.earned)} / ${ExamGrader.formatScore(it.max)} نقط" }
                                ?: "${section.points} نقط",
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
                    textAlign = TextAlign.Start
                )
            }

            HorizontalDivider(color = BorderLight)

            // Questions in Sections A and B
            section.questions.forEach { question ->
                val answerKey = "${examId}_${section.id}_q${question.no}"
                val currentAnswer = examAnswers[answerKey] ?: ""

                QuestionCard(
                    sectionId = section.id,
                    question = question,
                    studentAnswer = currentAnswer,
                    grade = grade?.questions?.get("${section.id}_${question.no}"),
                    isSubmitted = isSubmitted,
                    showCorrection = showCorrection,
                    onAnswerChange = { onAnswerChange(answerKey, it) },
                    showTashkeel = showTashkeel,
                    fontScale = fontScale
                )
            }

            // Section C (Expression / Writing)
            val hasWritingContent = section.id == "C" || section.topic != null || section.topics.isNotEmpty()
            if (hasWritingContent) {
                val writingKey = "${examId}_${section.id}_writing"
                val studentWriting = examAnswers[writingKey] ?: ""

                WritingSectionCard(
                    examId = examId,
                    sectionGrade = grade?.sections?.firstOrNull { it.sectionId == section.id },
                    ratings = ratings,
                    onRate = onRate,
                    section = section,
                    selectedTopicId = selectedTopicId,
                    onSelectTopic = onSelectTopic,
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
    grade: QuestionGrade?,
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSubmitted) {
                val badgeColor = when {
                    grade == null -> GoldAccent
                    grade.isFull -> SuccessGreen
                    grade.isEmpty -> ErrorRed
                    else -> GoldAccent
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.25f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (grade != null) {
                            "${ExamGrader.formatScore(grade.earned)} / ${ExamGrader.formatScore(grade.max)}"
                        } else {
                            "${question.points} ن"
                        },
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

        // Student answer: a fill-in table, or a free-text field
        if (question.table != null) {
            if (isSubmitted && grade != null) {
                GradedTableView(
                    table = question.table,
                    studentAnswer = studentAnswer,
                    grade = grade,
                    fontScale = fontScale
                )
            } else {
                ExamTableView(
                    table = question.table,
                    studentAnswer = studentAnswer,
                    onAnswerChange = onAnswerChange,
                    enabled = !isSubmitted,
                    fontScale = fontScale
                )
            }
        } else {
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
                    textDirection = androidx.compose.ui.text.style.TextDirection.Rtl,
                    fontFamily = AmiriFontFamily,
                    fontSize = (16 * fontScale).sp,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                minLines = if (question.points >= 2.0) 3 else 1
            )

        }

        if (isSubmitted && grade != null) {
            QuestionResultBlock(
                grade = grade,
                answerBlank = studentAnswer.replace(ExamGrader.CELL_SEPARATOR, "").isBlank(),
                isTable = question.table != null
            )
        }

        // Model Answer strictly hidden until submission
        AnimatedVisibility(
            visible = isSubmitted && showCorrection,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            QuestionCorrectionCard(
                answerGroups = question.answerGroups,
                table = question.table,
                grading = question.grading,
                fontScale = fontScale
            )
        }
    }
}

@Composable
private fun QuestionCorrectionCard(
    answerGroups: List<List<AnswerItem>>,
    table: ExamTable?,
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

            // Table questions show the completed table; others show answer cards
            if (table != null && table.answerRows.isNotEmpty()) {
                ExamAnswerTable(table = table, fontScale = fontScale)
            }

            // Render each group as a clean card without any English keys, symbols or '='
            (if (table != null && table.answerRows.isNotEmpty()) emptyList() else answerGroups).forEach { group ->
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
                                    Text(
                                        text = item.label,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = PrimaryDarkTeal,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                hasLabel && hasValue -> {
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
    examId: String,
    sectionGrade: SectionGrade?,
    ratings: Map<String, Float>,
    onRate: (Int, Float) -> Unit,
    section: ExamSection,
    selectedTopicId: String?,
    onSelectTopic: (String) -> Unit,
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
                        text = if (sectionGrade != null) {
                            "${ExamGrader.formatScore(sectionGrade.earned)} / ${ExamGrader.formatScore(sectionGrade.max)}"
                        } else {
                            "${section.points} ن"
                        },
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

        // Handle choose_one topics
        var activeTopic: ExamTopic? = null
        if (section.chooseOne && section.topics.isNotEmpty()) {
            Text(
                text = when (section.topics.size) {
                    2 -> "اختر أحد الموضوعين الآتيين للإنجاز:"
                    3 -> "اختر أحد المواضيع الثلاثة الآتية للإنجاز:"
                    else -> "اختر أحد المواضيع الآتية للإنجاز:"
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryDarkTeal
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                section.topics.forEach { topic ->
                    val isSelected = selectedTopicId == topic.id
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = !isSubmitted) { onSelectTopic(topic.id) }
                            .testTag("topic_choice_${topic.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) PrimaryDarkTeal else MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) PrimaryDarkTeal else BorderLight
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = topic.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            activeTopic = section.topics.firstOrNull { it.id == selectedTopicId }
            if (activeTopic != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "نص الموضوع المختار: «${activeTopic.title}»",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PrimaryDarkTeal
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeTopic.prompt,
                            fontFamily = AmiriFontFamily,
                            fontSize = (16 * fontScale).sp,
                            lineHeight = (25 * fontScale).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        } else if (section.topics.isNotEmpty()) {
            val singleTopic = section.topics[0]
            activeTopic = singleTopic
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (singleTopic.title.isNotBlank()) {
                        Text(
                            text = singleTopic.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PrimaryDarkTeal
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(
                        text = singleTopic.prompt,
                        fontFamily = AmiriFontFamily,
                        fontSize = (16 * fontScale).sp,
                        lineHeight = (25 * fontScale).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else if (section.topic != null) {
            Text(
                text = section.topic,
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
                    text = "اكتب نص الموضوع هنا (من 8 إلى 10 أسطر)...",
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
                textDirection = androidx.compose.ui.text.style.TextDirection.Rtl,
                fontFamily = AmiriFontFamily,
                fontSize = (16 * fontScale).sp,
                lineHeight = (26 * fontScale).sp,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onSurface
            ),
            minLines = 7
        )

        // Rubric & Model Text/Points: strictly shown only AFTER submission
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
                if (section.rubric.isNotEmpty()) {
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

                            Text(
                                text = if (studentWriting.isBlank()) {
                                    "لم تكتب موضوع الإنشاء، فنقطته صفر."
                                } else {
                                    "قدّر إنشاءك بصدق على كل معيار؛ تُحتسب نقطتك من تقديرك."
                                },
                                fontSize = 12.sp,
                                color = TextSecondaryLight
                            )

                            section.rubric.forEachIndexed { index, item ->
                                val rating = ratings["${examId}_${section.id}_r$index"]
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
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
                                                    text = "${ExamGrader.formatScore(item.points)} ن",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = PrimaryDarkTeal
                                                )
                                            }

                                            Text(
                                                text = item.criterion,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(start = 8.dp),
                                                textAlign = TextAlign.Start
                                            )
                                        }

                                        if (studentWriting.isNotBlank()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                listOf(1f to "أتقنتُ", 0.5f to "جزئياً", 0f to "لم أتقن").forEach { option ->
                                                    val selected = rating == option.first
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(if (selected) PrimaryDarkTeal else Color.White)
                                                            .border(
                                                                1.dp,
                                                                if (selected) PrimaryDarkTeal else BorderLight,
                                                                RoundedCornerShape(8.dp)
                                                            )
                                                            .clickable { onRate(index, option.first) }
                                                            .padding(vertical = 7.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = option.second,
                                                            color = if (selected) Color.White else PrimaryDarkTeal,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            softWrap = false,
                                                            maxLines = 1
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}


private const val TABLE_CELL_SEPARATOR = "\u001F"
private val TABLE_GIVEN_CELL_COLOR = Color(0xFFF4F1E8)

@Composable
private fun RowScope.ExamTableCell(
    scrollable: Boolean,
    background: Color,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .then(if (scrollable) Modifier.width(130.dp) else Modifier.weight(1f))
            .fillMaxHeight()
            .background(background)
            .border(0.5.dp, BorderLight)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart,
        content = content
    )
}

@Composable
private fun ExamTableView(
    table: ExamTable,
    studentAnswer: String,
    onAnswerChange: (String) -> Unit,
    enabled: Boolean,
    fontScale: Float
) {
    val columnCount = table.headers.size.coerceAtLeast(1)
    val scrollable = columnCount > 3
    val blankIndex = remember(table) {
        val map = mutableMapOf<Pair<Int, Int>, Int>()
        var n = 0
        table.rows.forEachIndexed { r, row ->
            for (c in 0 until columnCount) {
                if (row.getOrElse(c) { "" }.isBlank()) { map[r to c] = n; n++ }
            }
        }
        map
    }
    val typed = remember(studentAnswer, blankIndex.size) {
        val parts = studentAnswer.split(TABLE_CELL_SEPARATOR)
        List(blankIndex.size) { parts.getOrElse(it) { "" } }
    }

    val tableContent: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, PrimaryDarkTeal.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                for (c in 0 until columnCount) {
                    ExamTableCell(scrollable, PrimaryDarkTeal) {
                        Text(
                            text = table.headers.getOrElse(c) { "" },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
            table.rows.forEachIndexed { r, row ->
                Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                    for (c in 0 until columnCount) {
                        val cell = row.getOrElse(c) { "" }
                        val idx = blankIndex[r to c]
                        if (idx == null) {
                            ExamTableCell(scrollable, TABLE_GIVEN_CELL_COLOR) {
                                Text(
                                    text = cell,
                                    fontFamily = AmiriFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (15 * fontScale).sp,
                                    lineHeight = (22 * fontScale).sp,
                                    color = PrimaryDarkTeal
                                )
                            }
                        } else {
                            ExamTableCell(scrollable, Color.White) {
                                val value = typed.getOrElse(idx) { "" }
                                BasicTextField(
                                    value = value,
                                    onValueChange = { v ->
                                        val list = typed.toMutableList()
                                        list[idx] = v.replace(TABLE_CELL_SEPARATOR, "")
                                        onAnswerChange(list.joinToString(TABLE_CELL_SEPARATOR))
                                    },
                                    enabled = enabled,
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        textDirection = androidx.compose.ui.text.style.TextDirection.Rtl,
                                        fontFamily = AmiriFontFamily,
                                        fontSize = (15 * fontScale).sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { inner ->
                                        Box {
                                            if (value.isEmpty()) {
                                                Text(text = "…", color = TextMutedLight, fontSize = 15.sp)
                                            }
                                            inner()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (scrollable) {
        Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) { tableContent() }
    } else {
        tableContent()
    }
}

@Composable
private fun ExamAnswerTable(table: ExamTable, fontScale: Float) {
    val columnCount = table.headers.size.coerceAtLeast(1)
    val scrollable = columnCount > 3
    val tableContent: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, SuccessGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                for (c in 0 until columnCount) {
                    ExamTableCell(scrollable, PrimaryDarkTeal) {
                        Text(
                            text = table.headers.getOrElse(c) { "" },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
            table.answerRows.forEachIndexed { r, row ->
                Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                    for (c in 0 until columnCount) {
                        val wasGiven = table.rows.getOrNull(r)?.getOrElse(c) { "" }?.isNotBlank() == true
                        ExamTableCell(scrollable, if (wasGiven) TABLE_GIVEN_CELL_COLOR else Color.White) {
                            Text(
                                text = row.getOrElse(c) { "" },
                                fontFamily = AmiriFontFamily,
                                fontWeight = if (wasGiven) FontWeight.Bold else FontWeight.Normal,
                                fontSize = (15 * fontScale).sp,
                                lineHeight = (22 * fontScale).sp,
                                color = if (wasGiven) PrimaryDarkTeal else Color(0xFF133E32)
                            )
                        }
                    }
                }
            }
        }
    }
    if (scrollable) {
        Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) { tableContent() }
    } else {
        tableContent()
    }
}


@Composable
private fun ExamIntroCard(
    exam: ExamData,
    durationSeconds: Int?,
    onStart: () -> Unit,
    onBack: () -> Unit
) {
    val hasWriting = exam.sections.any { it.id == "C" || it.topic != null || it.topics.isNotEmpty() }
    val questionCount = exam.sections.sumOf { it.questions.size } + if (hasWriting) 1 else 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "قبل أن تبدأ",
                fontFamily = AmiriFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = PrimaryDarkTeal
            )
            Text(
                text = exam.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextSecondaryLight,
                textAlign = TextAlign.Start
            )
            HorizontalDivider(color = BorderLight)
            val lines = buildList {
                if (durationSeconds != null) {
                    add("مدة الامتحان: ${exam.duration}. يبدأ المؤقت فور الضغط على «ابدأ الامتحان»، ولا يمكن إيقافه ولا إعادة ضبطه.")
                } else {
                    add("هذا الامتحان بلا مؤقت، لكن الخروج منه يعني تسليمه.")
                }
                add("عدد الأسئلة: $questionCount${if (hasWriting) " (منها موضوع الإنشاء)" else ""}.")
                add("تُحفظ إجاباتك تلقائياً، ويستمر الوقت حتى لو أُغلق التطبيق.")
                if (durationSeconds != null) add("عند انتهاء الوقت يُسلَّم الفرض تلقائياً بما كتبتَه، وتظهر نقطتك مع الإجابات النموذجية.")
            }
            lines.forEach { line ->
                Text(
                    text = "• $line",
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start
                )
            }
            Text(
                text = "تنبيه: بعد البدء يُمنع التنقل أو مغادرة الامتحان؛ زر الرجوع يعني تسليم الفرض.",
                color = ErrorRed,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Start
            )
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_exam_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkTeal)
            ) {
                Text(
                    text = "ابدأ الامتحان",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color.White,
                    softWrap = false,
                    maxLines = 1
                )
            }
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "رجوع", color = TextSecondaryLight, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ExamResultCard(grade: ExamGrade, auto: Boolean) {
    val ratio = if (grade.max > 0.0) grade.total / grade.max else 0.0
    val verdict = when {
        ratio >= 0.9 -> "ممتاز"
        ratio >= 0.75 -> "جيد جداً"
        ratio >= 0.6 -> "جيد"
        ratio >= 0.5 -> "مقبول"
        else -> "تحتاج إلى مراجعة"
    }
    val color = when {
        ratio >= 0.6 -> SuccessGreen
        ratio >= 0.5 -> GoldAccent
        else -> ErrorRed
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "نقطتك التقديرية",
                fontSize = 14.sp,
                color = TextSecondaryLight,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${ExamGrader.formatScore(grade.total)} / ${ExamGrader.formatScore(grade.max)}",
                fontFamily = AmiriFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 38.sp,
                color = color,
                modifier = Modifier.testTag("exam_score_text")
            )
            Text(text = verdict, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
            if (auto) {
                Text(
                    text = "سُلِّم الفرض تلقائياً عند انتهاء الوقت.",
                    fontSize = 12.sp,
                    color = ErrorRed,
                    fontWeight = FontWeight.Medium
                )
            }
            HorizontalDivider(color = BorderLight)
            grade.sections.forEach { sec ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sec.title,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Start
                        )
                        Text(
                            text = "${ExamGrader.formatScore(sec.earned)} / ${ExamGrader.formatScore(sec.max)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDarkTeal,
                            softWrap = false,
                            maxLines = 1
                        )
                    }
                    if (sec.needsSelfRating) {
                        Text(
                            text = "قدّر إنشاءك بالمعايير في أسفل الصفحة لتكتمل نقطتك.",
                            fontSize = 12.sp,
                            color = Color(0xFFE65100),
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
            HorizontalDivider(color = BorderLight)
            Text(
                text = "النقطة تقديرية: يُصحَّح الجواب بمطابقة الكلمات الأساسية، فقد تُظلَم عبارة صحيحة صيغت بكلمات مختلفة. قارن دائماً بالإجابة النموذجية.",
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = TextMutedLight,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun QuestionResultBlock(grade: QuestionGrade, answerBlank: Boolean, isTable: Boolean) {
    val partialColor = Color(0xFFE65100)
    val status = when {
        answerBlank -> Triple("✘", "لم تجب عن هذا السؤال", ErrorRed)
        grade.isFull -> Triple("✔", "إجابة صحيحة", SuccessGreen)
        grade.isEmpty -> Triple("✘", "إجابة غير صحيحة", ErrorRed)
        else -> Triple("◐", "إجابة جزئية", partialColor)
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = status.first, color = status.third, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                text = "${status.second}: ${ExamGrader.formatScore(grade.earned)} من ${ExamGrader.formatScore(grade.max)}",
                color = status.third,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        if (grade.mode == GradeMode.OPINION) {
            Text(
                text = "سؤال رأي: تُقبل كل إجابة معلَّلة، ويُقدَّر الجواب بحسب اكتماله.",
                fontSize = 12.sp,
                color = TextSecondaryLight
            )
        } else if (!isTable && grade.items.size > 1 && !answerBlank) {
            grade.items.forEach { item ->
                val f = item.fraction
                val symbol = if (f >= 0.99) "✔" else if (f >= 0.25) "◐" else "✘"
                val col = if (f >= 0.99) SuccessGreen else if (f >= 0.25) partialColor else ErrorRed
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = symbol, color = col, fontSize = 13.sp)
                    Text(
                        text = item.label.ifBlank { "عنصر من الإجابة" },
                        fontSize = 12.sp,
                        color = TextSecondaryLight
                    )
                }
            }
        }
    }
}

private fun tableBlankIndex(table: ExamTable): Map<Pair<Int, Int>, Int> {
    val map = mutableMapOf<Pair<Int, Int>, Int>()
    ExamGrader.blankCells(table).forEachIndexed { index, cell -> map[cell] = index }
    return map
}

@Composable
private fun GradedTableView(
    table: ExamTable,
    studentAnswer: String,
    grade: QuestionGrade,
    fontScale: Float
) {
    val columnCount = table.headers.size.coerceAtLeast(1)
    val scrollable = columnCount > 3
    val blankIndex = remember(table) { tableBlankIndex(table) }
    val tableContent: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, PrimaryDarkTeal.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                for (c in 0 until columnCount) {
                    ExamTableCell(scrollable, PrimaryDarkTeal) {
                        Text(
                            text = table.headers.getOrElse(c) { "" },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
            table.rows.forEachIndexed { r, row ->
                Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                    for (c in 0 until columnCount) {
                        val cell = row.getOrElse(c) { "" }
                        val idx = blankIndex[r to c]
                        if (idx == null) {
                            ExamTableCell(scrollable, TABLE_GIVEN_CELL_COLOR) {
                                Text(
                                    text = cell,
                                    fontFamily = AmiriFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (15 * fontScale).sp,
                                    lineHeight = (22 * fontScale).sp,
                                    color = PrimaryDarkTeal
                                )
                            }
                        } else {
                            val item = grade.items.getOrNull(idx)
                            val f = item?.fraction ?: 0.0
                            val background = when {
                                f >= 0.99 -> Color(0xFFE3F2E8)
                                f >= 0.25 -> Color(0xFFFFF3E0)
                                else -> Color(0xFFFDE8E8)
                            }
                            ExamTableCell(scrollable, background) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = item?.student?.takeIf { it.isNotBlank() } ?: "—",
                                        fontFamily = AmiriFontFamily,
                                        fontSize = (15 * fontScale).sp,
                                        lineHeight = (22 * fontScale).sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (item != null && f < 0.99) {
                                        Text(
                                            text = "الصحيح: ${item.expected}",
                                            fontSize = 12.sp,
                                            lineHeight = 17.sp,
                                            color = SuccessGreen,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (scrollable) {
        Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) { tableContent() }
    } else {
        tableContent()
    }
}
