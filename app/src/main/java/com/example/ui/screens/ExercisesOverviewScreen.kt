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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.PrimaryDarkTeal
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMutedLight
import com.example.ui.theme.TextSecondaryLight
import com.example.util.TextHelper
import com.example.viewmodel.ArabicAppViewModel
import com.example.viewmodel.NavTab

@Composable
fun ExercisesOverviewScreen(
    viewModel: ArabicAppViewModel,
    modifier: Modifier = Modifier
) {
    val lessons by viewModel.lessons.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val allExams = viewModel.allExams

    var selectedUnitFilter by remember { mutableStateOf<Int?>(null) }
    var selectedExamCategory by remember { mutableStateOf<String?>("local") }

    val filteredLessons = remember(lessons, selectedUnitFilter) {
        if (selectedUnitFilter == null) {
            lessons
        } else {
            lessons.filter { it.unit == selectedUnitFilter }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "بنك التمارين والامتحانات",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "فروض شاملة وامتحانات تقويمية لجميع الوحدات (من 1 إلى 6) وتمارين تفاعلية",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryLight,
                fontSize = 14.sp
            )
        }

        // Section 1: Exams / Assessments
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "نماذج الامتحانات والفروض (${allExams.size} نموذجاً)",
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = PrimaryDarkTeal,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = {
                            viewModel.setUnitFilter(null)
                            viewModel.setComponentFilter("exams")
                            viewModel.selectTab(NavTab.LESSONS)
                        }
                    ) {
                        Text(
                            text = "عرض الكل",
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            softWrap = false,
                            maxLines = 1
                        )
                    }
                }

                // 3 Category Chips matching the requested categories
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedExamCategory == "local",
                        onClick = { selectedExamCategory = if (selectedExamCategory == "local") null else "local" },
                        label = { Text("امتحانات محلية (${viewModel.localExams.size})", fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryDarkTeal,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    FilterChip(
                        selected = selectedExamCategory == "assessments",
                        onClick = { selectedExamCategory = if (selectedExamCategory == "assessments") null else "assessments" },
                        label = { Text("تقويمات الوحدات الدراسية (${viewModel.unitAssessments.size})", fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryDarkTeal,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    FilterChip(
                        selected = selectedExamCategory == "homework",
                        onClick = { selectedExamCategory = if (selectedExamCategory == "homework") null else "homework" },
                        label = { Text("الفروض المنزلية (${viewModel.homeworkExams.size})", fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryDarkTeal,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }

        val examsToDisplay = when (selectedExamCategory) {
            "local" -> viewModel.localExams
            "assessments" -> viewModel.unitAssessments
            "homework" -> viewModel.homeworkExams
            else -> allExams
        }

        // Render exams as distinct cards
        items(examsToDisplay) { exam ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("exercise_exam_card_${exam.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${exam.declaredTotal} / 20",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
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
                            color = TextSecondaryLight
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
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (exam.unit) {
                                1 -> "الوحدة الأولى: القيم الإسلامية"
                                2 -> "الوحدة الثانية: القيم الوطنية والإنسانية"
                                3 -> "الوحدة الثالثة: المجال الحضاري"
                                4 -> "الوحدة الرابعة: المجال الاجتماعي والاقتصادي"
                                5 -> "الوحدة الخامسة: المجال السكاني"
                                6 -> "الوحدة السادسة: المجال الفني والثقافي"
                                else -> when {
                                    exam.type == "local_model" -> "امتحان محلي موحد"
                                    exam.semester == 2 -> "الأسدس الثاني: تقويم إجمالي شامل"
                                    else -> "الأسدس الأول: تقويم إجمالي شامل"
                                }
                            },
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

                    Spacer(modifier = Modifier.height(2.dp))

                    Button(
                        onClick = { viewModel.openExam(exam.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("start_exam_btn_${exam.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryDarkTeal,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "بدء الفرض ونموذج التصحيح",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Section 2: Interactive Lesson Quizzes
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "تمارين الدروس التفاعلية (${lessons.size} درساً)",
                    fontFamily = AmiriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = PrimaryDarkTeal
                )

                // Unit Filter Chips for lesson exercises
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedUnitFilter == null,
                        onClick = { selectedUnitFilter = null },
                        label = {
                            Text(
                                text = "كافة الوحدات (${lessons.size})",
                                fontSize = 13.sp,
                                fontWeight = if (selectedUnitFilter == null) FontWeight.Bold else FontWeight.Normal
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

                    FilterChip(
                        selected = selectedUnitFilter == 1,
                        onClick = { selectedUnitFilter = 1 },
                        label = {
                            Text(
                                text = "الوحدة 1: القيم الإسلامية (${lessons.count { it.unit == 1 }})",
                                fontSize = 13.sp,
                                fontWeight = if (selectedUnitFilter == 1) FontWeight.Bold else FontWeight.Normal
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

                    FilterChip(
                        selected = selectedUnitFilter == 2,
                        onClick = { selectedUnitFilter = 2 },
                        label = {
                            Text(
                                text = "الوحدة 2: الوطنية والإنسانية (${lessons.count { it.unit == 2 }})",
                                fontSize = 13.sp,
                                fontWeight = if (selectedUnitFilter == 2) FontWeight.Bold else FontWeight.Normal
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

                    FilterChip(
                        selected = selectedUnitFilter == 3,
                        onClick = { selectedUnitFilter = 3 },
                        label = {
                            Text(
                                text = "الوحدة 3: المجال الحضاري (${lessons.count { it.unit == 3 }})",
                                fontSize = 13.sp,
                                fontWeight = if (selectedUnitFilter == 3) FontWeight.Bold else FontWeight.Normal
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

                    FilterChip(
                        selected = selectedUnitFilter == 4,
                        onClick = { selectedUnitFilter = 4 },
                        label = {
                            Text(
                                text = "الوحدة 4: الاجتماعي والاقتصادي (${lessons.count { it.unit == 4 }})",
                                fontSize = 13.sp,
                                fontWeight = if (selectedUnitFilter == 4) FontWeight.Bold else FontWeight.Normal
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

                    FilterChip(
                        selected = selectedUnitFilter == 5,
                        onClick = { selectedUnitFilter = 5 },
                        label = {
                            Text(
                                text = "الوحدة 5: المجال السكاني (${lessons.count { it.unit == 5 }})",
                                fontSize = 13.sp,
                                fontWeight = if (selectedUnitFilter == 5) FontWeight.Bold else FontWeight.Normal
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

                    FilterChip(
                        selected = selectedUnitFilter == 6,
                        onClick = { selectedUnitFilter = 6 },
                        label = {
                            Text(
                                text = "الوحدة 6: الفني والثقافي (${lessons.count { it.unit == 6 }})",
                                fontSize = 13.sp,
                                fontWeight = if (selectedUnitFilter == 6) FontWeight.Bold else FontWeight.Normal
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
            }
        }

        items(filteredLessons) { lesson ->
            val scoreInfo = viewModel.repository.getExerciseScore(lesson.id)
            ExerciseLessonCard(
                lesson = lesson,
                scoreInfo = scoreInfo,
                showTashkeel = settings.showTashkeel,
                onStartExercise = {
                    viewModel.openExercise(lesson.id)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ExerciseLessonCard(
    lesson: Lesson,
    scoreInfo: Pair<Int, Int>?,
    showTashkeel: Boolean,
    onStartExercise: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("exercise_lesson_card_${lesson.id}")
            .clickable(onClick = onStartExercise),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Component tag & Unit tag
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
                        text = "• ${lesson.unitDisplayName}",
                        color = TextMutedLight,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = TextHelper.formatArabic(lesson.title, showTashkeel),
                    fontFamily = AmiriFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${lesson.exercises.size} أسئلة",
                        fontSize = 12.sp,
                        color = TextSecondaryLight
                    )

                    if (scoreInfo != null) {
                        Text(
                            text = "•",
                            fontSize = 12.sp,
                            color = TextMutedLight
                        )
                        Text(
                            text = "أعلى نتيجة: ${scoreInfo.first}/${scoreInfo.second}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (scoreInfo.first >= scoreInfo.second / 2) SuccessGreen else GoldAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Start Quiz Button
            Button(
                onClick = onStartExercise,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent,
                    contentColor = PrimaryDarkTeal
                ),
                modifier = Modifier
                    .height(44.dp)
                    .testTag("start_quiz_btn_${lesson.id}")
            ) {
                Text(
                    text = if (scoreInfo != null) "إعادة" else "ابدأ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
