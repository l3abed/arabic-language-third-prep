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
import com.example.ui.theme.GoldLight
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

    val mistakeReviewItems by viewModel.mistakeReviewItems.collectAsState()
    val examHistory by viewModel.examHistory.collectAsState()
    var showClearMistakesDialog by remember { mutableStateOf(false) }
    var showClearExamHistoryDialog by remember { mutableStateOf(false) }

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



        // Section: Exam History Records (سجل الامتحانات)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سجل الامتحانات المنجزة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (examHistory.isNotEmpty()) {
                            TextButton(onClick = { showClearExamHistoryDialog = true }) {
                                Text("مسح السجل", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                            }
                        }
                    }

                    if (examHistory.isEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "لا توجد امتحانات مسلّمة حتى الآن.",
                            fontSize = 13.sp,
                            color = TextSecondaryLight
                        )
                    } else {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            examHistory.take(5).forEach { record ->
                                val dateStr = java.text.SimpleDateFormat("yyyy/MM/dd - HH:mm", java.util.Locale.getDefault())
                                    .format(java.util.Date(record.timestamp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = record.examTitle,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = dateStr,
                                            fontSize = 11.sp,
                                            color = TextSecondaryLight
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(GoldLight)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${record.declaredTotal} نقطة",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryDarkTeal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Clear Mistakes (مسح قائمة الأخطاء والمراجعة)
        if (mistakeReviewItems.isNotEmpty()) {
            item {
                OutlinedButton(
                    onClick = { showClearMistakesDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "مسح قائمة أخطاء المراجعة (${mistakeReviewItems.size} سؤال)", fontWeight = FontWeight.Bold)
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

    // Clear Mistakes Confirmation Dialog
    if (showClearMistakesDialog) {
        AlertDialog(
            onDismissRequest = { showClearMistakesDialog = false },
            title = { Text(text = "تأكيد مسح قائمة المراجعة", fontWeight = FontWeight.Bold) },
            text = { Text("هل تريد مسح جميع الأسئلة المسجلة في قائمة المراجعة التكرارية؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearMistakes()
                        showClearMistakesDialog = false
                        Toast.makeText(context, "تم مسح قائمة المراجعة", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("نعم، مسح", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearMistakesDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Clear Exam History Confirmation Dialog
    if (showClearExamHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearExamHistoryDialog = false },
            title = { Text(text = "تأكيد مسح سجل الامتحانات", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من مسح جميع سجلات الامتحانات المسلّمة؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearExamHistory()
                        showClearExamHistoryDialog = false
                        Toast.makeText(context, "تم مسح سجل الامتحانات", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("نعم، مسح", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearExamHistoryDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
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
