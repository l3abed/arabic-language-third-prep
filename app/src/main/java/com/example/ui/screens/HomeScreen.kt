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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PrimaryDarkTeal
import com.example.ui.theme.PrimaryDarkTealLight
import com.example.ui.theme.TextMutedLight
import com.example.ui.theme.TextSecondaryLight
import com.example.util.TextHelper
import com.example.viewmodel.ArabicAppViewModel
import com.example.viewmodel.NavTab

@Composable
fun HomeScreen(
    viewModel: ArabicAppViewModel,
    modifier: Modifier = Modifier
) {
    val lastStudiedLesson by viewModel.lastStudiedLesson.collectAsState()
    val componentInfos by viewModel.componentInfos.collectAsState()
    val settings by viewModel.settings.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            // Greeting Header
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "السلام عليكم",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondaryLight,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "العربية • الثالثة إعدادي",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 24.sp
                )
            }
        }

        // Hero Card: "تابع من حيث توقفت"
        item {
            lastStudiedLesson?.let { lesson ->
                val progressStep = viewModel.repository.getLessonProgress(lesson.id)
                val totalSteps = lesson.cards.size.coerceAtLeast(1)
                val fraction = (progressStep.toFloat() / totalSteps).coerceIn(0.2f, 1f)

                HeroResumeCard(
                    lesson = lesson,
                    fraction = fraction,
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

        // 4 Components with progress bar
        items(componentInfos.size) { index ->
            val info = componentInfos[index]
            ComponentProgressCard(
                info = info,
                onClick = {
                    viewModel.setComponentFilter(info.id)
                    viewModel.selectTab(NavTab.LESSONS)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeroResumeCard(
    lesson: Lesson,
    fraction: Float,
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
                text = "تابع من حيث توقفت",
                color = GoldAccent,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Lesson Title in Amiri
            Text(
                text = TextHelper.formatArabic(lesson.title, showTashkeel),
                color = Color.White,
                fontSize = 28.sp,
                fontFamily = AmiriFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Component and semester info
            Text(
                text = "${lesson.componentName} • ${lesson.semesterName}",
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
                    text = "متابعة الدرس",
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

            // Progress bar at bottom of card matching screenshot 3
            val displayProgress = if (info.totalLessons > 0) info.progress else 0.15f
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
