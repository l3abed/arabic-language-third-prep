package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
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
import com.example.model.ComponentInfo
import com.example.model.Lesson
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.AmiriQuranFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PrimaryDarkTeal
import com.example.ui.theme.PrimaryDarkTealLight
import com.example.ui.theme.TextMutedLight
import com.example.ui.theme.TextSecondaryLight
import com.example.util.TextHelper
import com.example.viewmodel.ArabicAppViewModel
import com.example.viewmodel.NavTab

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.ui.layout.ContentScale
import com.example.ui.theme.GoldLight
import com.example.ui.theme.SuccessGreen

@Composable
fun HomeScreen(
    viewModel: ArabicAppViewModel,
    modifier: Modifier = Modifier
) {
    val resumeLesson by viewModel.resumeLesson.collectAsState()
    val componentInfos by viewModel.componentInfos.collectAsState()
    val allLessons by viewModel.lessons.collectAsState()
    val completedLessons by viewModel.completedLessons.collectAsState()
    val selectedUnit by viewModel.selectedUnit.collectAsState()
    val settings by viewModel.settings.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            // Basmala Header
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                    fontFamily = AmiriQuranFontFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal,
                    color = PrimaryDarkTeal,
                    lineHeight = 34.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "العربية • الثالثة إعدادي",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 24.sp
                )
            }
        }

        // Unit Filter Chips in Home: Default is Unit 1
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedUnit == 1,
                    onClick = { viewModel.setUnitFilter(1) },
                    label = {
                        Text(
                            text = "الوحدة 1: القيم الإسلامية",
                            fontWeight = if (selectedUnit == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryDarkTeal,
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = selectedUnit == 2,
                    onClick = { viewModel.setUnitFilter(2) },
                    label = {
                        Text(
                            text = "الوحدة 2: الوطنية والإنسانية",
                            fontWeight = if (selectedUnit == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryDarkTeal,
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = selectedUnit == 3,
                    onClick = { viewModel.setUnitFilter(3) },
                    label = {
                        Text(
                            text = "الوحدة 3: المجال الحضاري",
                            fontWeight = if (selectedUnit == 3) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryDarkTeal,
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = selectedUnit == null,
                    onClick = { viewModel.setUnitFilter(null) },
                    label = {
                        Text(
                            text = "كافة الوحدات",
                            fontWeight = if (selectedUnit == null) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryDarkTeal,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Hero Resume Card: shows previous unfinished lesson or first uncompleted lesson
        item {
            resumeLesson?.let { lesson ->
                val progressStep = viewModel.repository.getLessonProgress(lesson.id)
                val totalSteps = lesson.cards.size.coerceAtLeast(1)
                val isPreviouslyStarted = progressStep > 0
                val fraction = if (isPreviouslyStarted) {
                    (progressStep.toFloat() / totalSteps).coerceIn(0.15f, 1f)
                } else {
                    0.05f
                }

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

        // Components Section Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "المكوّنات",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 19.sp
            )
        }

        // Components with progress bars
        items(componentInfos.size) { index ->
            val info = componentInfos[index]
            ComponentProgressCard(
                info = info,
                onClick = {
                    if (info.id == "exams") {
                        viewModel.setUnitFilter(null)
                    }
                    viewModel.setComponentFilter(info.id)
                    viewModel.selectTab(NavTab.LESSONS)
                }
            )
        }

        // Unit Lessons Section Header & Content
        val unitLessons = allLessons
            .filter { selectedUnit == null || it.unit == selectedUnit }
            .sortedWith(compareBy({ it.component }, { it.order }))

        if (unitLessons.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (selectedUnit) {
                            1 -> "دروس الوحدة الأولى: القيم الإسلامية"
                            2 -> "دروس الوحدة الثانية: القيم الوطنية والإنسانية"
                            3 -> "دروس الوحدة الثالثة: المجال الحضاري"
                            else -> "دروس المنهج المقررة"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 17.sp
                    )
                    TextButton(
                        onClick = { viewModel.selectTab(NavTab.LESSONS) }
                    ) {
                        Text(
                            text = "عرض الكل",
                            color = PrimaryDarkTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            items(unitLessons.size) { index ->
                val lesson = unitLessons[index]
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

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

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
                fontSize = 24.sp,
                fontFamily = AmiriFontFamily,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Component and semester info
            Text(
                text = "${lesson.componentDisplayName} • ${lesson.semesterDisplayName} • ${lesson.unitDisplayName}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(18.dp))

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
            Spacer(modifier = Modifier.height(20.dp))

            // Gold Action Button
            Button(
                onClick = onResumeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("resume_lesson_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent,
                    contentColor = PrimaryDarkTeal
                )
            ) {
                Text(
                    text = if (isPreviouslyStarted) "متابعة الدرس" else "بدء الدرس",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ComponentProgressCard(
    info: ComponentInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("component_card_${info.id}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = info.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = info.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight,
                        fontSize = 13.sp
                    )
                }

                if (info.totalLessons > 0) {
                    Text(
                        text = "${info.completedLessons}/${info.totalLessons}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMutedLight,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress bar at bottom of card
            val displayProgress = if (info.totalLessons > 0) info.progress else 0.05f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFDDE6E4))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(displayProgress.coerceIn(0.05f, 1f))
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(PrimaryDarkTeal)
                )
            }
        }
    }
}

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
