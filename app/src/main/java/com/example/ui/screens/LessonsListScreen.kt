package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Lesson
import com.example.ui.theme.AmiriFontFamily
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
    val selectedSemester by viewModel.selectedSemester.collectAsState()
    val selectedComponentId by viewModel.selectedComponentId.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val componentInfos by viewModel.componentInfos.collectAsState()

    val filteredLessons = lessons
        .filter { lesson ->
            val matchesSemester = (selectedSemester == null || lesson.semester == selectedSemester)
            val matchesComponent = (selectedComponentId == null || lesson.component == selectedComponentId)
            matchesSemester && matchesComponent
        }
        .sortedWith(compareBy({ it.component }, { it.semester }, { it.unit }, { it.order }))

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

        // Semester Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipItem(
                label = "كافة الدورات",
                isSelected = selectedSemester == null,
                onClick = { viewModel.setSemesterFilter(null) }
            )
            FilterChipItem(
                label = "الدورة الأولى",
                isSelected = selectedSemester == 1,
                onClick = { viewModel.setSemesterFilter(1) }
            )
            FilterChipItem(
                label = "الدورة الثانية",
                isSelected = selectedSemester == 2,
                onClick = { viewModel.setSemesterFilter(2) }
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

        // Lessons List
        if (filteredLessons.isEmpty()) {
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
                items(filteredLessons) { lesson ->
                    val isCompleted = completedLessons.contains(lesson.id)
                    val scoreInfo = viewModel.repository.getExerciseScore(lesson.id)

                    LessonItemCard(
                        lesson = lesson,
                        isCompleted = isCompleted,
                        scoreInfo = scoreInfo,
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
                if (isCompleted) {
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
