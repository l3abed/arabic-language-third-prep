package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.model.Lesson
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.BorderLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.PrimaryDarkTeal
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMutedLight
import com.example.ui.theme.TextSecondaryLight
import com.example.util.TextHelper
import com.example.viewmodel.ArabicAppViewModel

@Composable
fun LessonsListScreen(
    viewModel: ArabicAppViewModel,
    modifier: Modifier = Modifier
) {
    val lessons by viewModel.lessons.collectAsState()
    val completedLessons by viewModel.completedLessons.collectAsState()
    val selectedUnit by viewModel.selectedUnit.collectAsState()
    val selectedComponentId by viewModel.selectedComponentId.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val componentInfos by viewModel.componentInfos.collectAsState()

    val filteredLessons = remember(lessons, selectedUnit, selectedComponentId) {
        lessons
            .filter { lesson ->
                val matchesUnit = (selectedUnit == null || lesson.unit == selectedUnit)
                val matchesComponent = (selectedComponentId == null || lesson.component == selectedComponentId)
                matchesUnit && matchesComponent
            }
            .sortedWith(compareBy({ it.unit }, { it.component }, { it.order }))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = "فهرس الدروس المقررة",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Unit Filter Chips: Default is Unit 1 (الوحدة الأولى)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipItem(
                label = "الوحدة 1: القيم الإسلامية",
                isSelected = selectedUnit == 1,
                onClick = { viewModel.setUnitFilter(1) }
            )
            FilterChipItem(
                label = "الوحدة 2: الوطنية والإنسانية",
                isSelected = selectedUnit == 2,
                onClick = { viewModel.setUnitFilter(2) }
            )
            FilterChipItem(
                label = "الوحدة 3: المجال الحضاري",
                isSelected = selectedUnit == 3,
                onClick = { viewModel.setUnitFilter(3) }
            )
            FilterChipItem(
                label = "الوحدة 4: الاجتماعي والاقتصادي",
                isSelected = selectedUnit == 4,
                onClick = { viewModel.setUnitFilter(4) }
            )
            FilterChipItem(
                label = "الوحدة 5: المجال السكاني",
                isSelected = selectedUnit == 5,
                onClick = { viewModel.setUnitFilter(5) }
            )
            FilterChipItem(
                label = "الوحدة 6: الفني والثقافي",
                isSelected = selectedUnit == 6,
                onClick = { viewModel.setUnitFilter(6) }
            )
            FilterChipItem(
                label = "كافة الوحدات",
                isSelected = selectedUnit == null,
                onClick = { viewModel.setUnitFilter(null) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Component Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipItem(
                label = "كافة المكوّنات",
                isSelected = selectedComponentId == null,
                onClick = { viewModel.setComponentFilter(null) }
            )
            FilterChipItem(
                label = "النصوص القرائية",
                isSelected = selectedComponentId == "texts",
                onClick = { viewModel.setComponentFilter("texts") }
            )
            FilterChipItem(
                label = "الدرس اللغوي",
                isSelected = selectedComponentId == "language",
                onClick = { viewModel.setComponentFilter("language") }
            )
            FilterChipItem(
                label = "التعبير والإنشاء",
                isSelected = selectedComponentId == "writing",
                onClick = { viewModel.setComponentFilter("writing") }
            )
            FilterChipItem(
                label = "نماذج الامتحانات",
                isSelected = selectedComponentId == "exams",
                onClick = { viewModel.setComponentFilter("exams") }
            )
        }

        // Active component progress indicator
        selectedComponentId?.let { compId ->
            val compInfo = componentInfos.firstOrNull { it.id == compId }
            if (compInfo != null && compInfo.totalLessons > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "تقدّم ${compInfo.title}:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDarkTeal
                            )
                            Text(
                                text = "${compInfo.completedLessons} من ${compInfo.totalLessons} دروس",
                                fontSize = 12.sp,
                                color = TextSecondaryLight
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFFDDE6E4))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(compInfo.progress.coerceIn(0.05f, 1f))
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(PrimaryDarkTeal)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Lessons List or Exam Section
        if (selectedComponentId == "exams") {
            var isLocalExpanded by remember { mutableStateOf(false) }
            var isAssessmentsExpanded by remember { mutableStateOf(false) }
            var isHomeworkExpanded by remember { mutableStateOf(false) }

            val localExams = viewModel.localExams
            val unitAssessments = viewModel.unitAssessments
            val homeworkExams = viewModel.homeworkExams

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "نماذج الامتحانات والفروض",
                            fontFamily = AmiriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = PrimaryDarkTeal
                        )

                        // 3 Quick Action Category Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChipItem(
                                label = "امتحانات محلية (${localExams.size})",
                                isSelected = isLocalExpanded,
                                onClick = { isLocalExpanded = !isLocalExpanded }
                            )
                            FilterChipItem(
                                label = "تقويمات الوحدات الدراسية (${unitAssessments.size})",
                                isSelected = isAssessmentsExpanded,
                                onClick = { isAssessmentsExpanded = !isAssessmentsExpanded }
                            )
                            FilterChipItem(
                                label = "الفروض المنزلية (${homeworkExams.size})",
                                isSelected = isHomeworkExpanded,
                                onClick = { isHomeworkExpanded = !isHomeworkExpanded }
                            )
                        }
                    }
                }

                // Initial instruction card when all are collapsed
                if (!isLocalExpanded && !isAssessmentsExpanded && !isHomeworkExpanded) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = GoldLight.copy(alpha = 0.3f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "اضغط على زر الفئة المطلوبة أدناه (امتحانات محلية، أو تقويمات الوحدات الدراسية، أو الفروض المنزلية) لعرض نماذجها مع التصحيح الكامل.",
                                fontSize = 13.sp,
                                color = PrimaryDarkTeal,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(14.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // 1. زر «امتحانات محلية»
                item {
                    ExamCategoryHeaderButton(
                        title = "امتحانات محلية",
                        subtitle = "مرتبة زمنياً حسب تاريخ الوضع (من 2018 إلى 2025)",
                        count = localExams.size,
                        isExpanded = isLocalExpanded,
                        onClick = { isLocalExpanded = !isLocalExpanded },
                        testTag = "btn_category_local_exams"
                    )
                }

                if (isLocalExpanded) {
                    items(localExams) { exam ->
                        ExamItemCard(
                            exam = exam,
                            onOpenExam = { viewModel.openExam(exam.id) }
                        )
                    }
                }

                // 2. زر «تقويمات الوحدات الدراسية»
                item {
                    ExamCategoryHeaderButton(
                        title = "تقويمات الوحدات الدراسية",
                        subtitle = "تقويمات الوحدات 1 و2 و3 والتقويم الإجمالي للأسدس",
                        count = unitAssessments.size,
                        isExpanded = isAssessmentsExpanded,
                        onClick = { isAssessmentsExpanded = !isAssessmentsExpanded },
                        testTag = "btn_category_unit_assessments"
                    )
                }

                if (isAssessmentsExpanded) {
                    items(unitAssessments) { exam ->
                        ExamItemCard(
                            exam = exam,
                            onOpenExam = { viewModel.openExam(exam.id) }
                        )
                    }
                }

                // 3. زر «الفروض المنزلية»
                item {
                    ExamCategoryHeaderButton(
                        title = "الفروض المنزلية",
                        subtitle = "الفرض المنزلي الأول للأسدس الأول مع عناصر الإجابة",
                        count = homeworkExams.size,
                        isExpanded = isHomeworkExpanded,
                        onClick = { isHomeworkExpanded = !isHomeworkExpanded },
                        testTag = "btn_category_homework"
                    )
                }

                if (isHomeworkExpanded) {
                    items(homeworkExams) { exam ->
                        ExamItemCard(
                            exam = exam,
                            onOpenExam = { viewModel.openExam(exam.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        } else if (filteredLessons.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لا توجد دروس مطابقة لهذا الفلتر",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondaryLight
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredLessons.size) { index ->
                    val lesson = filteredLessons[index]
                    val isCompleted = completedLessons.contains(lesson.id)
                    val scoreInfo = viewModel.repository.getExerciseScore(lesson.id)
                    val progress = viewModel.repository.getLessonCombinedProgress(lesson)

                    LessonItemCard(
                        lesson = lesson,
                        isCompleted = isCompleted,
                        scoreInfo = scoreInfo,
                        progress = progress,
                        showTashkeel = settings.showTashkeel,
                        onOpenLesson = { viewModel.openLesson(lesson.id) },
                        onOpenExercise = { viewModel.openExercise(lesson.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PrimaryDarkTeal,
            selectedLabelColor = Color.White,
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun LessonItemCard(
    lesson: Lesson,
    isCompleted: Boolean,
    scoreInfo: Pair<Int, Int>?,
    progress: Float,
    showTashkeel: Boolean,
    onOpenLesson: () -> Unit,
    onOpenExercise: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lesson_card_${lesson.id}")
            .clickable(onClick = onOpenLesson),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Badge: Component & Semester & Tag
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldLight)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = lesson.componentDisplayName,
                                color = PrimaryDarkTeal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        lesson.tag?.let { tagText ->
                            Box(
                                modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PrimaryDarkTeal.copy(alpha = 0.08f))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = tagText,
                                    color = PrimaryDarkTeal,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Text(
                            text = "• ${lesson.unitDisplayName} - الترتيب ${lesson.order}",
                            color = TextMutedLight,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Lesson Title
                    Text(
                        text = TextHelper.formatArabic(lesson.title, showTashkeel),
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 19.sp,
                        lineHeight = 26.sp
                    )

                    if (lesson.summary.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = TextHelper.formatArabic(lesson.summary, showTashkeel),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryLight,
                            fontSize = 13.sp
                        )
                    }
                }

                // Completion status icon
                if (isCompleted || progress >= 1f) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "مكتمل",
                            tint = SuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details and stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${lesson.cards.size} محطات شرح",
                    fontSize = 12.sp,
                    color = TextSecondaryLight
                )
                Text(
                    text = "•",
                    fontSize = 12.sp,
                    color = TextMutedLight
                )
                Text(
                    text = "${lesson.exercises.size} أسئلة تدريبية",
                    fontSize = 12.sp,
                    color = TextSecondaryLight
                )

                if (scoreInfo != null) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "النتيجة: ${scoreInfo.first}/${scoreInfo.second}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (scoreInfo.first >= scoreInfo.second / 2) SuccessGreen else GoldAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar on each lesson card
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "نسبة الإنجاز:",
                        fontSize = 11.sp,
                        color = TextSecondaryLight
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (progress >= 1f) SuccessGreen else PrimaryDarkTeal
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFE2ECE9))
                ) {
                    if (progress > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.coerceIn(0.04f, 1f))
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (progress >= 1f) SuccessGreen else PrimaryDarkTeal)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenLesson,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("read_lesson_btn_${lesson.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryDarkTeal,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "مراجعة الدرس", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onOpenExercise,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("exercise_btn_${lesson.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryDarkTeal
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "التدريب", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ExamItemCard(
    exam: com.example.model.ExamData,
    onOpenExam: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("exam_card_${exam.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = exam.title,
                    fontFamily = AmiriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    lineHeight = 28.sp,
                    color = PrimaryDarkTeal,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldLight.copy(alpha = 0.35f))
                        .border(1.dp, GoldAccent, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${exam.declaredTotal} / 20",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = PrimaryDarkTeal,
                        softWrap = false,
                        maxLines = 1
                    )
                }
            }

            if (exam.source.isNotBlank()) {
                Text(
                    text = "المصدر: ${exam.source}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondaryLight,
                    lineHeight = 17.sp
                )
            }

            val passageTitle = exam.passage.title
                .removePrefix("نص الانطلاق")
                .trimStart(':', ' ', '،', '-', '–')
                .trim()
            if (passageTitle.isNotBlank()) {
                Text(
                    text = "نص الانطلاق: «$passageTitle»",
                    fontFamily = AmiriFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (exam.unit != null) "الوحدة ${exam.unit}" else "امتحان محلي موحد",
                    fontSize = 12.sp,
                    color = TextSecondaryLight
                )
                Text(text = "•", fontSize = 12.sp, color = TextMutedLight)
                Text(
                    text = "المدة: ${exam.duration}",
                    fontSize = 12.sp,
                    color = TextSecondaryLight
                )
            }

            Text(
                text = "المجالات: القراءة والفهم (8 ن) • الدرس اللغوي (6 ن) • التعبير والإنشاء (6 ن) مع التصحيح وسلم التنقيط.",
                fontSize = 13.sp,
                color = TextSecondaryLight,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onOpenExam,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("start_exam_btn_${exam.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkTeal)
            ) {
                Text(
                    text = "بدء الامتحان ونموذج التصحيح",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    softWrap = false,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ExamCategoryHeaderButton(
    title: String,
    subtitle: String,
    count: Int,
    isExpanded: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) PrimaryDarkTeal else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isExpanded) 1.5.dp else 1.dp,
            color = if (isExpanded) GoldAccent else BorderLight
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 3.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isExpanded) GoldAccent else PrimaryDarkTeal.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "طي" else "عرض",
                        tint = if (isExpanded) PrimaryDarkTeal else PrimaryDarkTeal,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isExpanded) GoldAccent else GoldLight)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "$count نماذج",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = PrimaryDarkTeal
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = title,
                    fontFamily = AmiriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isExpanded) Color.White else PrimaryDarkTeal,
                    textAlign = TextAlign.Start
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = if (isExpanded) Color.White.copy(alpha = 0.85f) else TextSecondaryLight,
                    textAlign = TextAlign.Start,
                    maxLines = 1
                )
            }
        }
    }
}


