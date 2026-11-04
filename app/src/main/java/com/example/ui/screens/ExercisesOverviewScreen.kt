package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
fun ExercisesOverviewScreen(
    viewModel: ArabicAppViewModel,
    modifier: Modifier = Modifier
) {
    val lessons by viewModel.lessons.collectAsState()
    val settings by viewModel.settings.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "بنك التمارين التفاعلية",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "اختبارات تفاعلية: اختيار من متعدد، صح/خطأ، ترتيب كلمات",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryLight,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Homework Exam Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("exercise_homework_exam_card")
                    .clickable { viewModel.openExam() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryDarkTeal),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldAccent)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "20 / 20 نقطة",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = PrimaryDarkTeal
                            )
                        }

                        Text(
                            text = "الفرض المنزلي الأول",
                            color = Color.White,
                            fontFamily = AmiriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    Text(
                        text = "نص الانطلاق والأسئلة الشاملة مع نموذج التصحيح وسلم التنقيط لجميع المجالات",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { viewModel.openExam() },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = PrimaryDarkTeal
                        )
                    ) {
                        Text("بدء الفرض المنزلي ونموذج الإجابة", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        items(lessons) { lesson ->
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
                // Component tag
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
