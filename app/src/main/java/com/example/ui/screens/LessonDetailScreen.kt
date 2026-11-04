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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Lightbulb
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
import com.example.model.CardItem
import com.example.model.ExampleEntry
import com.example.model.IrabStep
import com.example.model.KeyValuePair
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.BorderLight
import com.example.ui.theme.ExampleCardBgLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
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

            // Top Bar
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
                            viewModel.speakCard(card)
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
                    fontSize = 18.sp,
                    maxLines = 1,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    textAlign = TextAlign.Center
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

            // Segmented Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
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

            Spacer(modifier = Modifier.height(14.dp))

            // Scrollable Content dynamically rendered by Card Type
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RenderCardContent(
                    card = card,
                    fontScale = settings.fontScale,
                    showTashkeel = settings.showTashkeel
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Bottom Buttons Bar
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

                // "تدرّب الآن" Button (Dark Teal filled) - launches exercises for this lesson!
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

@Composable
private fun RenderCardContent(
    card: CardItem,
    fontScale: Float,
    showTashkeel: Boolean
) {
    when (card) {
        is CardItem.RuleCard -> {
            BaseCardContainer(badge = "القاعدة", title = card.title) {
                card.text?.let { t ->
                    Text(
                        text = TextHelper.formatArabic(t, showTashkeel),
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = (17 * fontScale).sp,
                        lineHeight = (26 * fontScale).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                card.points.forEach { point ->
                    BulletPointRow(text = point, fontScale = fontScale, showTashkeel = showTashkeel)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        is CardItem.ExampleCard -> {
            BaseCardContainer(badge = "أمثلة تطبيقية", title = card.title) {
                card.items.forEach { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = ExampleCardBgLight)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = TextHelper.formatArabic(item.text, showTashkeel),
                                fontFamily = AmiriFontFamily,
                                fontSize = (22 * fontScale).sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDarkTeal,
                                lineHeight = (30 * fontScale).sp
                            )
                            if (item.note.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = TextHelper.formatArabic(item.note, showTashkeel),
                                    fontSize = (13 * fontScale).sp,
                                    color = TextSecondaryLight
                                )
                            }
                        }
                    }
                }
            }
        }

        is CardItem.IrabCard -> {
            BaseCardContainer(badge = "نموذج إعرابي", title = "إعراب الشاهد") {
                // Sentence highlight
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ExampleCardBgLight)
                ) {
                    Text(
                        text = TextHelper.formatArabic(card.sentence, showTashkeel),
                        fontFamily = AmiriFontFamily,
                        fontSize = (24 * fontScale).sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDarkTeal,
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Steps table
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    card.steps.forEach { step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = TextHelper.formatArabic(step.word, showTashkeel),
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDarkTeal,
                                fontFamily = AmiriFontFamily,
                                fontSize = (18 * fontScale).sp,
                                modifier = Modifier.width(90.dp)
                            )
                            Text(
                                text = TextHelper.formatArabic(step.irab, showTashkeel),
                                fontSize = (14 * fontScale).sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                card.tip?.let { tip ->
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = NoteCardBgLight)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lightbulb,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = TextHelper.formatArabic(tip, showTashkeel),
                                fontSize = (13 * fontScale).sp,
                                color = PrimaryDarkTeal,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        is CardItem.FactsCard -> {
            BaseCardContainer(badge = "معطيات وتحليل", title = card.title) {
                card.items.forEach { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Text(
                            text = TextHelper.formatArabic(item.label, showTashkeel),
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDarkTeal,
                            fontSize = (14 * fontScale).sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = TextHelper.formatArabic(item.value, showTashkeel),
                            fontSize = (14 * fontScale).sp,
                            lineHeight = (22 * fontScale).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        is CardItem.VocabCard -> {
            BaseCardContainer(badge = "شرح المفردات", title = card.title) {
                card.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = TextHelper.formatArabic(item.label, showTashkeel),
                            fontFamily = AmiriFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDarkTeal,
                            fontSize = (19 * fontScale).sp
                        )
                        Text(
                            text = TextHelper.formatArabic(item.value, showTashkeel),
                            fontSize = (14 * fontScale).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        is CardItem.SummaryCard -> {
            BaseCardContainer(badge = "خلاصة ومضمون", title = card.title) {
                Text(
                    text = TextHelper.formatArabic(card.text, showTashkeel),
                    fontFamily = AmiriFontFamily,
                    fontSize = (19 * fontScale).sp,
                    lineHeight = (30 * fontScale).sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        is CardItem.StepsCard -> {
            BaseCardContainer(badge = "خطوات منهجية", title = card.title) {
                card.points.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(PrimaryDarkTeal),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = TextHelper.formatArabic(step, showTashkeel),
                            fontSize = (15 * fontScale).sp,
                            lineHeight = (22 * fontScale).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        is CardItem.ModelCard -> {
            BaseCardContainer(badge = "نموذج مُنجَز", title = card.title) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ExampleCardBgLight)
                ) {
                    Text(
                        text = TextHelper.formatArabic(card.text, showTashkeel),
                        fontFamily = AmiriFontFamily,
                        fontSize = (17 * fontScale).sp,
                        lineHeight = (28 * fontScale).sp,
                        color = PrimaryDarkTeal,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                if (card.points.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "ملاحظات على النموذج:",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDarkTeal,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    card.points.forEach { pt ->
                        BulletPointRow(text = pt, fontScale = fontScale, showTashkeel = showTashkeel)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        is CardItem.MemorizeCard -> {
            // Distinctive Memorize Card with Gold border and tinted background
            Card(
                modifier = Modifier.fillMaxWidth().testTag("memorize_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NoteCardBgLight),
                border = androidx.compose.foundation.BorderStroke(1.dp, NoteCardBorderLight)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = card.title,
                            fontFamily = AmiriFontFamily,
                            fontSize = (22 * fontScale).sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDarkTeal
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(GoldAccent)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "احفظ هذا",
                                color = PrimaryDarkTeal,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    card.points.forEach { pt ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryDarkTeal)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = TextHelper.formatArabic(pt, showTashkeel),
                                fontFamily = AmiriFontFamily,
                                fontSize = (18 * fontScale).sp,
                                lineHeight = (28 * fontScale).sp,
                                color = PrimaryDarkTeal,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BaseCardContainer(
    badge: String,
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("lesson_card_container"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            // Header Row: Title & Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(GoldLight)
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = badge,
                        color = PrimaryDarkTeal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}

@Composable
private fun BulletPointRow(
    text: String,
    fontScale: Float,
    showTashkeel: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(GoldAccent)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = TextHelper.formatArabic(text, showTashkeel),
            fontSize = (15 * fontScale).sp,
            lineHeight = (24 * fontScale).sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
