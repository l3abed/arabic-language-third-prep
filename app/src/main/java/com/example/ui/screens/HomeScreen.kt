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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ComponentInfo
import com.example.model.ExamData
import com.example.model.Lesson
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.AmiriQuranFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.PrimaryDarkTeal
import com.example.ui.theme.PrimaryDarkTealLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMutedLight
import com.example.ui.theme.TextSecondaryLight
import com.example.util.TextHelper
import com.example.viewmodel.ArabicAppViewModel

/**
 * Data class representing a Unit option within a component.
 */
data class UnitItem(
    val unitNumber: Int,
    val title: String,
    val subtitle: String
)

@Composable
fun HomeScreen(
    viewModel: ArabicAppViewModel,
    modifier: Modifier = Modifier
) {
    val componentInfos by viewModel.componentInfos.collectAsState()
    val allLessons by viewModel.lessons.collectAsState()
    val completedLessons by viewModel.completedLessons.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val resumeLesson by viewModel.resumeLesson.collectAsState()
    val mistakeReviewItems by viewModel.mistakeReviewItems.collectAsState()

    // 3-Level drill down states:
    // Level 1: selectedComponentId == null (Show ONLY Components)
    // Level 2: selectedComponentId != null && selectedUnitNumber == null (Show ONLY Units / Exam Categories)
    // Level 3: selectedComponentId != null && selectedUnitNumber != null (Show Lessons / Exams of that Unit)
    var selectedComponentId by remember { mutableStateOf<String?>(null) }
    var selectedUnitNumber by remember { mutableStateOf<Int?>(null) }
    var selectedExamCategory by remember { mutableStateOf<String?>(null) } // "local", "assessments", "homework"

    // Back handling for smooth step-back
    BackHandler(enabled = selectedUnitNumber != null || selectedExamCategory != null) {
        selectedUnitNumber = null
        selectedExamCategory = null
    }

    BackHandler(enabled = selectedComponentId != null && selectedUnitNumber == null && selectedExamCategory == null) {
        selectedComponentId = null
    }

    val unitsList = listOf(
        UnitItem(1, "الوحدة الأولى", "مجال القيم الإسلامية"),
        UnitItem(2, "الوحدة الثانية", "مجال القيم الوطنية والإنسانية"),
        UnitItem(3, "الوحدة الثالثة", "المجال الحضاري"),
        UnitItem(4, "الوحدة الرابعة", "المجال الاجتماعي والاقتصادي"),
        UnitItem(5, "الوحدة الخامسة", "المجال السكاني"),
        UnitItem(6, "الوحدة السادسة", "المجال الفني والثقافي")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Basmala Header and App Identity with Dark Mode Toggle at top-left
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                        fontFamily = AmiriQuranFontFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 34.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "مادة اللغة العربية - الثالثة إعدادي",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 23.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "الأستاذ محمد العابد",
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GoldAccent
                    )
                }

                // Dark Mode Toggle Button (Top-Left of Screen in RTL layout)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { viewModel.toggleDarkMode() }
                        .testTag("dark_mode_toggle_home"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (settings.isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                        contentDescription = if (settings.isDarkMode) "تفعيل الوضع النهاري" else "تفعيل الوضع الليلي",
                        tint = if (settings.isDarkMode) GoldAccent else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // =========================================================================
        // LEVEL 1: HOME (Resume card + Components)
        // =========================================================================
        if (selectedComponentId == null) {
            // Hero Resume Card: زر الاستكمال من حيث توقفت
            resumeLesson?.let { lesson ->
                val progressStep = viewModel.repository.getLessonProgress(lesson.id)
                val totalSteps = lesson.cards.size.coerceAtLeast(1)
                val isPreviouslyStarted = progressStep > 0
                val fraction = if (isPreviouslyStarted) {
                    (progressStep.toFloat() / totalSteps).coerceIn(0.15f, 1f)
                } else {
                    0.05f
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    HeroResumeCard(
                        lesson = lesson,
                        fraction = fraction,
                        isPreviouslyStarted = isPreviouslyStarted,
                        showTashkeel = settings.showTashkeel,
                        onResumeClick = {
                            viewModel.openLesson(lesson.id)
                        }
                    )
                }
            }

            // بطاقة «للمراجعة» - المراجعة المتباعدة للأخطاء
            if (mistakeReviewItems.isNotEmpty()) {
                val reviewBatch = mistakeReviewItems.take(10)
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("spaced_review_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(GoldAccent.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Refresh,
                                            contentDescription = null,
                                            tint = GoldAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "للمراجعة (تكرار الأخطاء)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "أسئلة أخطأت فيها مرتبة حسب الأحدث",
                                            fontSize = 11.sp,
                                            color = TextSecondaryLight
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GoldLight)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${reviewBatch.size} سؤال",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = PrimaryDarkTeal
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Show the top mistake questions up to 10
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                reviewBatch.forEachIndexed { idx, item ->
                                    val ex = item.exercise
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.openExercise(item.lessonId)
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${idx + 1}. ${item.lessonTitle}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                if (ex != null) {
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = TextHelper.formatArabic(ex.prompt, settings.showTashkeel),
                                                        fontFamily = AmiriFontFamily,
                                                        fontSize = 14.sp,
                                                        maxLines = 1,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFFFFEBEE))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "${item.errorCount} أخطاء",
                                                    color = Color(0xFFC62828),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
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

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "المكوّنات الدراسية",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 19.sp
                    )
                    Text(
                        text = "اختر مكوّناً للمتابعة",
                        fontSize = 12.sp,
                        color = TextSecondaryLight
                    )
                }
            }

            items(componentInfos.size) { index ->
                val info = componentInfos[index]
                ComponentSelectionCard(
                    info = info,
                    onClick = {
                        selectedComponentId = info.id
                        selectedUnitNumber = null
                        selectedExamCategory = null
                    }
                )
            }
        }

        // =========================================================================
        // LEVEL 2: UNITS SELECTION (After clicking a component)
        // =========================================================================
        else if (selectedUnitNumber == null && selectedExamCategory == null) {
            val currentComp = componentInfos.firstOrNull { it.id == selectedComponentId }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryDarkTeal),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = { selectedComponentId = null },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "العودة للمكوّنات",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = currentComp?.title ?: "المكوّن",
                                    fontFamily = AmiriFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "اختر الوحدة الدراسية لعرض دروسها",
                                    fontSize = 12.sp,
                                    color = GoldAccent
                                )
                            }
                        }
                    }
                }
            }

            // If the selected component is "exams", show the 3 exam categories
            if (selectedComponentId == "exams") {
                item {
                    ExamCategoryCard(
                        title = "امتحانات محلية موحدة",
                        subtitle = "6 نماذج امتحانات موحدة لدورات سابقة مع عناصر الإجابة وسلم التنقيط",
                        countText = "6 امتحانات",
                        icon = Icons.Filled.HistoryEdu,
                        onClick = { selectedExamCategory = "local" }
                    )
                }
                item {
                    ExamCategoryCard(
                        title = "تقويمات الوحدات الدراسية",
                        subtitle = "تقويمات الوحدات من 1 إلى 6، والتقويمان الإجماليان للأسدسين",
                        countText = "${viewModel.unitAssessments.size} تقويمات",
                        icon = Icons.Filled.Assignment,
                        onClick = { selectedExamCategory = "assessments" }
                    )
                }
                item {
                    ExamCategoryCard(
                        title = "الفروض المنزلية",
                        subtitle = "فروض منزلية تطبيقية شاملة مع نماذج التصحيح وسلم التنقيط",
                        countText = "فرض منزلي",
                        icon = Icons.Filled.EditNote,
                        onClick = { selectedExamCategory = "homework" }
                    )
                }
            } else {
                // Regular Lesson Components: Display the 3 Units
                items(unitsList.size) { index ->
                    val unit = unitsList[index]
                    val lessonsInUnit = allLessons.filter {
                        it.component == selectedComponentId && it.unit == unit.unitNumber
                    }
                    val completedCount = lessonsInUnit.count { completedLessons.contains(it.id) }

                    UnitSelectionCard(
                        unit = unit,
                        totalLessons = lessonsInUnit.size,
                        completedCount = completedCount,
                        onClick = {
                            selectedUnitNumber = unit.unitNumber
                        }
                    )
                }
            }
        }

        // =========================================================================
        // LEVEL 3: LESSONS OR EXAMS LIST (After clicking a unit)
        // =========================================================================
        else {
            val currentComp = componentInfos.firstOrNull { it.id == selectedComponentId }

            // Header for Level 3
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryDarkTeal),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (selectedExamCategory != null) {
                                    selectedExamCategory = null
                                } else {
                                    selectedUnitNumber = null
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "العودة للوحدات",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            val categoryTitle = when {
                                selectedExamCategory == "local" -> "امتحانات محلية موحدة"
                                selectedExamCategory == "assessments" -> "تقويمات الوحدات الدراسية"
                                selectedExamCategory == "homework" -> "الفروض المنزلية"
                                selectedUnitNumber == 1 -> "الوحدة الأولى: القيم الإسلامية"
                                selectedUnitNumber == 2 -> "الوحدة الثانية: القيم الوطنية والإنسانية"
                                selectedUnitNumber == 3 -> "الوحدة الثالثة: المجال الحضاري"
                                selectedUnitNumber == 4 -> "الوحدة الرابعة: المجال الاجتماعي والاقتصادي"
                                selectedUnitNumber == 5 -> "الوحدة الخامسة: المجال السكاني"
                                selectedUnitNumber == 6 -> "الوحدة السادسة: المجال الفني والثقافي"
                                else -> "الدروس"
                            }
                            Text(
                                text = categoryTitle,
                                fontFamily = AmiriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                text = currentComp?.title ?: "مكوّن المادة",
                                fontSize = 12.sp,
                                color = GoldAccent
                            )
                        }
                    }
                }
            }

            // If displaying Exams of selectedExamCategory
            if (selectedComponentId == "exams") {
                val examsToShow = when (selectedExamCategory) {
                    "local" -> viewModel.localExams
                    "assessments" -> viewModel.unitAssessments
                    "homework" -> viewModel.homeworkExams
                    else -> emptyList()
                }

                items(examsToShow) { exam ->
                    ExamItemCard(
                        exam = exam,
                        onOpenExam = {
                            viewModel.openExam(exam.id)
                        }
                    )
                }
            } else {
                // Displaying lessons of (selectedComponentId, selectedUnitNumber)
                val targetLessons = allLessons
                    .filter { it.component == selectedComponentId && it.unit == selectedUnitNumber }
                    .sortedBy { it.order }

                if (targetLessons.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Text(
                                text = "لا توجد دروس مضافة لهذه الوحدة حالياً.",
                                modifier = Modifier.padding(24.dp),
                                textAlign = TextAlign.Center,
                                color = TextSecondaryLight
                            )
                        }
                    }
                } else {
                    items(targetLessons) { lesson ->
                        val isCompleted = completedLessons.contains(lesson.id)
                        val scoreInfo = viewModel.repository.getExerciseScore(lesson.id)
                        val progress = viewModel.repository.getLessonCombinedProgress(lesson)

                        HomeLessonCard(
                            lesson = lesson,
                            isCompleted = isCompleted,
                            scoreInfo = scoreInfo,
                            progress = progress,
                            showTashkeel = settings.showTashkeel,
                            onOpenLesson = { viewModel.openLesson(lesson.id) },
                            onOpenExercise = { viewModel.openExercise(lesson.id) }
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

/**
 * Level 1 Component Card
 */
@Composable
private fun ComponentSelectionCard(
    info: ComponentInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("component_card_${info.id}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val iconVector = when (info.id) {
                        "texts" -> Icons.Filled.AutoStories
                        "language" -> Icons.Filled.School
                        "writing" -> Icons.Filled.EditNote
                        "exams" -> Icons.Filled.Assignment
                        else -> Icons.Filled.MenuBook
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryDarkTeal.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = PrimaryDarkTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = info.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = info.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryLight,
                            fontSize = 13.sp
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "عرض الوحدات",
                    tint = GoldAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Hero Resume Card: زر الاستكمال من حيث توقفت
 */
@Composable
private fun HeroResumeCard(
    lesson: Lesson,
    fraction: Float,
    isPreviouslyStarted: Boolean,
    showTashkeel: Boolean,
    onResumeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_resume_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryDarkTeal),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Gold Subtitle
            Text(
                text = if (isPreviouslyStarted) "تابع من حيث توقفت" else "الدرس التالي للمراجعة",
                color = GoldAccent,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Lesson Title in Amiri
            Text(
                text = TextHelper.formatArabic(lesson.title, showTashkeel),
                color = Color.White,
                fontSize = 23.sp,
                fontFamily = AmiriFontFamily,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Component and unit info
            Text(
                text = "${lesson.componentDisplayName} • ${lesson.unitDisplayName}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(PrimaryDarkTealLight)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(GoldAccent)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))

            // Gold Action Button
            Button(
                onClick = onResumeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("resume_lesson_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent,
                    contentColor = PrimaryDarkTeal
                )
            ) {
                Text(
                    text = if (isPreviouslyStarted) "متابعة من حيث توقفت" else "بدء الدرس",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Level 2 Unit Selection Card
 */
@Composable
private fun UnitSelectionCard(
    unit: UnitItem,
    totalLessons: Int,
    completedCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(GoldLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${unit.unitNumber}",
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = PrimaryDarkTeal
                    )
                }

                Column {
                    Text(
                        text = unit.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = unit.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight,
                        fontSize = 13.sp
                    )
                    if (totalLessons > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$totalLessons دروس مقررة • $completedCount منجزة",
                            fontSize = 11.sp,
                            color = PrimaryDarkTeal,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "عرض الدروس",
                tint = PrimaryDarkTeal,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Level 2 Exam Category Card
 */
@Composable
private fun ExamCategoryCard(
    title: String,
    subtitle: String,
    countText: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryDarkTeal.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PrimaryDarkTeal,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = countText,
                        fontSize = 11.sp,
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "عرض النماذج",
                tint = PrimaryDarkTeal,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Level 3 Exam Item Card
 */
@Composable
private fun ExamItemCard(
    exam: ExamData,
    onOpenExam: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenExam),
        shape = RoundedCornerShape(14.dp),
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exam.title,
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = PrimaryDarkTeal
                    )
                    if (exam.source.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "المصدر: ${exam.source}",
                            fontSize = 12.sp,
                            color = TextSecondaryLight
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GoldLight)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${exam.declaredTotal} نقطة",
                        color = PrimaryDarkTeal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onOpenExam,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkTeal)
            ) {
                Text(
                    text = "فتح الامتحان والإجابة",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Level 3 Lesson Card
 */
@Composable
private fun HomeLessonCard(
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
            .testTag("home_lesson_card_${lesson.id}")
            .clickable(onClick = onOpenLesson),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldLight)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = lesson.componentDisplayName,
                                color = PrimaryDarkTeal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "• ${lesson.unitDisplayName} - الترتيب ${lesson.order}",
                            color = TextMutedLight,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = TextHelper.formatArabic(lesson.title, showTashkeel),
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp
                    )
                }

                if (isCompleted || progress >= 1f) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "مكتمل",
                            tint = SuccessGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
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
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFE2ECE9))
            ) {
                if (progress > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0.04f, 1f))
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (progress >= 1f) SuccessGreen else PrimaryDarkTeal)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenLesson,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("home_read_btn_${lesson.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkTeal)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("مراجعة", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onOpenExercise,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("home_exercise_btn_${lesson.id}"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Quiz,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = PrimaryDarkTeal
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("تدرّب الآن", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryDarkTeal)
                }
            }
        }
    }
}
