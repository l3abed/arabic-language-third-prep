package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.ExampleCardBgLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NoteCardBgLight
import com.example.ui.theme.NoteCardBorderLight
import com.example.ui.theme.PrimaryDarkTeal
import com.example.ui.theme.TextMutedLight
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.util.TextHelper
import com.example.viewmodel.ArabicAppViewModel

@Composable
fun LessonDetailScreen(
    viewModel: ArabicAppViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBackToTabs()
    }

    val activeLessonId by viewModel.activeLessonId.collectAsState()
    val lessons by viewModel.lessons.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val lesson = lessons.firstOrNull { it.id == activeLessonId }

    if (lesson == null || lesson.cards.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("الدرس غير متوفر")
        }
        return
    }

    var currentCardIndex by remember(lesson.id) {
        mutableIntStateOf(viewModel.repository.getLessonProgress(lesson.id).coerceIn(0, lesson.cards.size - 1))
    }

    LaunchedEffect(currentCardIndex) {
        viewModel.repository.saveLessonProgress(lesson.id, currentCardIndex)
    }

    val card = lesson.cards[currentCardIndex]
    val totalCards = lesson.cards.size

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Top Bar matching Screenshot 1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speaker Button (Yellow circular button)
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(GoldAccent)
                        .testTag("speaker_button")
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            val speechContent = "${card.rule}. ${card.exampleTitle}: ${card.exampleText}. ${card.noteText}"
                            viewModel.speakText(speechContent)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.VolumeUp,
                            contentDescription = "قراءة صوتية",
                            tint = PrimaryDarkTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Lesson Title
                Text(
                    text = TextHelper.formatArabic(lesson.title, settings.showTashkeel),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp
                )

                // Back Arrow Button (in RTL pointing right)
                IconButton(
                    onClick = { viewModel.navigateBackToTabs() },
                    modifier = Modifier.testTag("lesson_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "رجوع",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Segmented Progress Bar matching Screenshot 1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 0 until totalCards) {
                    val isPastOrCurrent = i <= currentCardIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isPastOrCurrent) PrimaryDarkTeal else Color(0xFFD1DCDB))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main White Card: القاعدة + المثال
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("main_lesson_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Badge "القاعدة" in gold
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(GoldAccent)
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = card.badge,
                                    color = PrimaryDarkTeal,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Rule text
                        Text(
                            text = TextHelper.formatArabic(card.rule, settings.showTashkeel),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = (18 * settings.fontScale).sp,
                            lineHeight = (28 * settings.fontScale).sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Example Subcard (#E8F0EE)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = ExampleCardBgLight
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Text(
                                    text = card.exampleTitle,
                                    color = TextSecondaryLight,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Example large text in Amiri
                                Text(
                                    text = TextHelper.formatArabic(card.exampleText, settings.showTashkeel),
                                    fontFamily = AmiriFontFamily,
                                    fontSize = (26 * settings.fontScale).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryDarkTeal,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    lineHeight = (36 * settings.fontScale).sp
                                )

                                if (card.exampleBreakdown.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = TextHelper.formatArabic(card.exampleBreakdown, settings.showTashkeel),
                                        fontSize = (13 * settings.fontScale).sp,
                                        color = TextSecondaryLight,
                                        lineHeight = (20 * settings.fontScale).sp,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // Note Card: "احفظ هذا" in Light Gold (#FEF8EC)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = NoteCardBgLight
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = card.noteTitle,
                            color = PrimaryDarkTeal,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = TextHelper.formatArabic(card.noteText, settings.showTashkeel),
                            fontFamily = AmiriFontFamily,
                            fontSize = (18 * settings.fontScale).sp,
                            color = PrimaryDarkTeal,
                            fontWeight = FontWeight.Medium,
                            lineHeight = (28 * settings.fontScale).sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Bottom Buttons Bar matching Screenshot 1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // "التالي" Button (Outlined)
                OutlinedButton(
                    onClick = {
                        if (currentCardIndex < totalCards - 1) {
                            currentCardIndex++
                        } else {
                            viewModel.openExercise(lesson.id)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("next_step_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryDarkTeal
                    )
                ) {
                    Text(
                        text = if (currentCardIndex < totalCards - 1) "التالي" else "إنهاء الشرح",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // "تدرّب الآن" Button (Dark Teal filled)
                Button(
                    onClick = {
                        viewModel.openExercise(lesson.id)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("train_now_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryDarkTeal,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "تدرّب الآن",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
