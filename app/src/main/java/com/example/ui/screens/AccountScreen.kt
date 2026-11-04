package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Spellcheck
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PrimaryDarkTeal
import com.example.ui.theme.TextMutedLight
import com.example.ui.theme.TextSecondaryLight
import com.example.viewmodel.ArabicAppViewModel

@Composable
fun AccountScreen(
    viewModel: ArabicAppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val completedLessons by viewModel.completedLessons.collectAsState()
    val lessons by viewModel.lessons.collectAsState()

    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showJsonEditorDialog by remember { mutableStateOf(false) }
    var customJsonText by remember { mutableStateOf("") }
    var showGuideDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "حسابي وإعداداتي",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp
            )
        }

        // Progress Stats Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("account_progress_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryDarkTeal),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "مستوى التقدّم العام",
                        color = GoldAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(
                            number = "${completedLessons.size} / ${lessons.size}",
                            label = "الدروس المكتملة"
                        )
                        val totalExercises = lessons.sumOf { it.exercises.size }
                        val solvedCount = lessons.count { viewModel.repository.getExerciseScore(it.id) != null }
                        StatItem(
                            number = "$solvedCount / ${lessons.size}",
                            label = "الامتحانات المنجزة"
                        )
                        val percentage = if (lessons.isNotEmpty()) {
                            (completedLessons.size.toFloat() / lessons.size * 100).toInt()
                        } else 0
                        StatItem(
                            number = "$percentage%",
                            label = "نسبة الإنجاز"
                        )
                    }
                }
            }
        }

        // Section: Appearance & Reading Settings
        item {
            Text(
                text = "إعدادات القراءة والمظهر",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Night Mode Toggle
        item {
            SettingRowCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.DarkMode,
                            contentDescription = null,
                            tint = PrimaryDarkTeal,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "الوضع الليلي (Dark Mode)",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "مريح للعينين أثناء المراجعة المسائية",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryLight
                            )
                        }
                    }

                    Switch(
                        checked = settings.isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GoldAccent,
                            checkedTrackColor = PrimaryDarkTeal
                        ),
                        modifier = Modifier.testTag("dark_mode_switch")
                    )
                }
            }
        }

        // Optional Tashkeel Toggle
        item {
            SettingRowCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Spellcheck,
                            contentDescription = null,
                            tint = PrimaryDarkTeal,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "التشكيل الاختياري",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (settings.showTashkeel) "حركات الإعراب ظاهرة" else "بدون حركات تشكيلية",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryLight
                            )
                        }
                    }

                    Switch(
                        checked = settings.showTashkeel,
                        onCheckedChange = { viewModel.toggleTashkeel() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GoldAccent,
                            checkedTrackColor = PrimaryDarkTeal
                        ),
                        modifier = Modifier.testTag("tashkeel_switch")
                    )
                }
            }
        }

        // Font Size Options
        item {
            SettingRowCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.FormatSize,
                            contentDescription = null,
                            tint = PrimaryDarkTeal,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "حجم الخط",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FontSizeChip(
                            label = "صغير",
                            isSelected = settings.fontScale == 0.9f,
                            onClick = { viewModel.setFontScale(0.9f) },
                            modifier = Modifier.weight(1f)
                        )
                        FontSizeChip(
                            label = "متوسط",
                            isSelected = settings.fontScale == 1.0f,
                            onClick = { viewModel.setFontScale(1.0f) },
                            modifier = Modifier.weight(1f)
                        )
                        FontSizeChip(
                            label = "كبير",
                            isSelected = settings.fontScale == 1.2f,
                            onClick = { viewModel.setFontScale(1.2f) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Section: Lessons JSON Management & Guide
        item {
            Text(
                text = "إدارة محتوى الدروس (lessons.json)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Guide Button
        item {
            SettingRowCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showGuideDialog = true }
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = PrimaryDarkTeal,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "كيف أضيف درساً جديداً في lessons.json؟",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "شرح الهيكل البرمجي والقوالب الجاهزة",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryLight
                            )
                        }
                    }
                }
            }
        }

        // JSON Viewer / Editor Button
        item {
            SettingRowCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            customJsonText = viewModel.repository.getActiveJsonString()
                            showJsonEditorDialog = true
                        }
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Code,
                            contentDescription = null,
                            tint = PrimaryDarkTeal,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "معاينة وتعديل ملف lessons.json",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "استعراض أو لصق دروسك الخاصة لتجربتها مباشرة",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryLight
                            )
                        }
                    }
                }
            }
        }

        // Reset Progress
        item {
            OutlinedButton(
                onClick = { showResetConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("reset_progress_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "إعادة تعيين التقدّم الدراسي", fontWeight = FontWeight.Bold)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Reset Progress Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text(text = "تأكيد إعادة التعيين", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من مسح جميع الدروس المكتملة ونتائج التمارين؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetAllProgress()
                        showResetConfirmDialog = false
                        Toast.makeText(context, "تمت إعادة تعيين التقدّم", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("نعم، مسح", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Guide Dialog explaining how to add a lesson
    if (showGuideDialog) {
        AlertDialog(
            onDismissRequest = { showGuideDialog = false },
            title = {
                Text("دليل إضافة درس في lessons.json", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "ملف `lessons.json` الموجود في `app/src/main/assets/lessons.json` يحتوي على قائمة كائنات `lessons`.",
                        fontSize = 13.sp
                    )
                    Text(
                        "لإضافة درس جديد، ضع كائناً داخل مصفوفة `lessons` بالهيكل التالي:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = """{
  "id": "farid_lesson",
  "title": "عنوان الدرس",
  "componentId": "linguistic", 
  "componentName": "الدرس اللغوي",
  "semester": 1,
  "semesterName": "الدورة الأولى",
  "shortDescription": "قاعدة + أمثلة",
  "cards": [
    {
      "badge": "القاعدة",
      "rule": "نص القاعدة هنا...",
      "exampleTitle": "مثال",
      "exampleText": "جملة الشاهد بالشكل التام",
      "exampleBreakdown": "تفكيك عناصر الجملة",
      "noteTitle": "احفظ هذا",
      "noteText": "فائدة إعرابية سريعة"
    }
  ],
  "exercises": [
    {
      "type": "single_choice",
      "title": "اختر الإجابة",
      "question": "نص السؤال؟",
      "options": ["أ", "ب", "ج"],
      "correctAnswerIndex": 0,
      "explanation": "تعليل الإجابة..."
    }
  ]
}""",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Text(
                        "المكوّنات المتاحة هي:\n" +
                                "• reading (النصوص القرائية)\n" +
                                "• linguistic (الدرس اللغوي)\n" +
                                "• composition (التعبير والإنشاء)\n" +
                                "• exams (نماذج الامتحانات)",
                        fontSize = 12.sp,
                        color = PrimaryDarkTeal
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showGuideDialog = false }) {
                    Text("فهمت، شكراً", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // JSON Viewer / Editor Dialog
    if (showJsonEditorDialog) {
        AlertDialog(
            onDismissRequest = { showJsonEditorDialog = false },
            title = {
                Text("محرّر محتوى الدروس (JSON)", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "يمكنك تعديل المحتوى هنا أو استرجاع النسخة الأصلية:",
                        fontSize = 12.sp,
                        color = TextSecondaryLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customJsonText,
                        onValueChange = { customJsonText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = viewModel.repository.saveCustomLessonsJson(customJsonText)
                        if (success) {
                            Toast.makeText(context, "تم حفظ الدروس الجديدة بنجاح!", Toast.LENGTH_SHORT).show()
                            showJsonEditorDialog = false
                        } else {
                            Toast.makeText(context, "خطأ في بنية الـ JSON! تأكد من صحة الأقواس", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkTeal)
                ) {
                    Text("حفظ وتطبيق", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.repository.restoreDefaultLessons()
                        showJsonEditorDialog = false
                        Toast.makeText(context, "تم استرجاع النسخة الافتراضية", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("استرجاع الافتراضي")
                }
            }
        )
    }
}

@Composable
private fun StatItem(
    number: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = number,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SettingRowCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        content()
    }
}

@Composable
private fun FontSizeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) PrimaryDarkTeal else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
