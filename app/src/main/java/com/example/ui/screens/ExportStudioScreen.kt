package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoFile
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.provider.OpenableColumns
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkPanels
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.PanelBorder
import com.example.ui.theme.PanelCard
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.SecondaryPanels
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SuccessGreen
import com.example.viewmodel.StudioViewModel

data class ImportSource(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun ExportStudioScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val format by viewModel.exportFormat.collectAsState()
    val resolution by viewModel.exportResolution.collectAsState()
    val fps by viewModel.exportFps.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()
    val exportNotice by viewModel.exportNotice.collectAsState()
    val progress by viewModel.exportProgress.collectAsState()
    val tasks by viewModel.exportTasks.collectAsState()
    val editingQuality by viewModel.editingQuality.collectAsState()
    val custom1KWidth by viewModel.custom1KWidth.collectAsState()
    val custom1KHeight by viewModel.custom1KHeight.collectAsState()

    val context = LocalContext.current
    var showAdvancedQuality by remember { mutableStateOf(false) }
    var statusAlert by remember { mutableStateOf<String?>(null) }
    var isAlertSuccess by remember { mutableStateOf(true) }

    var showUrlDialog by remember { mutableStateOf(false) }
    var showGoogleDriveDialog by remember { mutableStateOf(false) }
    var showCloudDialog by remember { mutableStateOf(false) }

    var urlInput by remember { mutableStateOf("") }
    var urlError by remember { mutableStateOf<String?>(null) }

    var driveLinkInput by remember { mutableStateOf("") }
    var driveError by remember { mutableStateOf<String?>(null) }

    var cloudLinkInput by remember { mutableStateOf("") }
    var cloudError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    // SAF Document Picker: Device Files
    val deviceFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            var displayName = "ملف_جهاز"
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        displayName = cursor.getString(nameIndex) ?: displayName
                    }
                }
            } catch (_: Exception) {}
            viewModel.importFromDevice(name = displayName, uriString = uri.toString())
            statusAlert = "✅ تم استيراد «$displayName» بنجاح من ذاكرة الهاتف وإضافته للمشروع!"
            isAlertSuccess = true
        } else {
            statusAlert = "ℹ️ تم إلغاء اختيار الملف من الجهاز"
            isAlertSuccess = false
        }
    }

    // SAF Document Picker: Google Drive
    val driveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            var displayName = "ملف_Drive"
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        displayName = cursor.getString(nameIndex) ?: displayName
                    }
                }
            } catch (_: Exception) {}
            viewModel.importFromGoogleDrive(identifier = displayName, isLink = false)
            statusAlert = "✅ تم استيراد «$displayName» من Google Drive بنجاح!"
            isAlertSuccess = true
        } else {
            statusAlert = "ℹ️ تم إلغاء اختيار الملف من Google Drive"
            isAlertSuccess = false
        }
    }

    // SAF Document Picker: External Cloud Storage (OneDrive / Dropbox)
    val cloudFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            var displayName = "ملف_سحابي"
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        displayName = cursor.getString(nameIndex) ?: displayName
                    }
                }
            } catch (_: Exception) {}
            viewModel.importFromCloudStorage(serviceName = "Dropbox/OneDrive", identifier = displayName, isLink = false)
            statusAlert = "✅ تم استيراد «$displayName» من التخزين السحابي بنجاح!"
            isAlertSuccess = true
        } else {
            statusAlert = "ℹ️ تم إلغاء اختيار الملف السحابي"
            isAlertSuccess = false
        }
    }

    val qualityList = listOf(
        "360p", "480p", "720p", "1080p", "2K", "4K", "8K", "Cinematics RAW", "Anamorphic", "Custom 1K"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkPanels)
            .verticalScroll(scrollState)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "7. الاستيراد والتصدير السينمائي",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Text(
                text = "ارفع ملفاتك وصدر أعمالك السينمائية بأعلى دقة وترميز متقدم",
                fontSize = 11.sp,
                color = SecondaryText
            )
        }

        // Section 1: استيراد الملفات (Import Sources) - الأزرار الأربعة التفاعلية بالكامل
        Text(text = "استيراد الملفات والوسائط والمشاريع", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryText)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 1. زر رفع من الجهاز (Local Device Storage)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PanelCard)
                    .border(0.5.dp, PurpleAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable {
                        deviceFileLauncher.launch(arrayOf("video/*", "audio/*", "image/*", "*/*"))
                    }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .testTag("btn_import_device"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(imageVector = Icons.Default.FolderOpen, contentDescription = "رفع من الجهاز", tint = PurpleAccent, modifier = Modifier.size(18.dp))
                    Text(text = "رفع من الجهاز", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PrimaryText, maxLines = 1)
                }
            }

            // 2. زر Google Drive
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PanelCard)
                    .border(0.5.dp, SuccessGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable {
                        showGoogleDriveDialog = true
                    }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .testTag("btn_import_google_drive"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(imageVector = Icons.Default.Cloud, contentDescription = "Google Drive", tint = SuccessGreen, modifier = Modifier.size(18.dp))
                    Text(text = "Google Drive", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PrimaryText, maxLines = 1)
                }
            }

            // 3. زر الخيار الآخر المشابه لـ Drive (Dropbox / OneDrive)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PanelCard)
                    .border(0.5.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable {
                        showCloudDialog = true
                    }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .testTag("btn_import_cloud"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = "Dropbox / OneDrive", tint = CyanAccent, modifier = Modifier.size(18.dp))
                    Text(text = "Dropbox/Drive", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PrimaryText, maxLines = 1)
                }
            }

            // 4. زر استيراد عبر رابط مباشر
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PanelCard)
                    .border(0.5.dp, ElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable {
                        showUrlDialog = true
                    }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .testTag("btn_import_url"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(imageVector = Icons.Default.AddLink, contentDescription = "استيراد عبر رابط", tint = ElectricBlue, modifier = Modifier.size(18.dp))
                    Text(text = "استيراد عبر رابط", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PrimaryText, maxLines = 1)
                }
            }
        }

        // بطاقة التنبيه الديناميكية المباشرة عند النجاح أو الفشل
        AnimatedVisibility(visible = statusAlert != null) {
            statusAlert?.let { alert ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isAlertSuccess) SuccessGreen.copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f))
                        .border(1.dp, if (isAlertSuccess) SuccessGreen else Color(0xFFEF4444), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isAlertSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isAlertSuccess) SuccessGreen else Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = alert,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                        }
                        IconButton(
                            onClick = { statusAlert = null },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = SecondaryText, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        // Section 2: إعدادات التصدير السينمائي المنظمة (بطاقة صغيرة وأنيقة)
        Text(text = "إعدادات التصدير السينمائي", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PanelCard)
                .border(0.5.dp, PanelBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Formats: MP4, WebM, MOV
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("MP4", "WebM", "MOV").forEach { f ->
                        val isChosen = format == f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isChosen) ElectricBlue else SecondaryPanels)
                                .border(0.5.dp, if (isChosen) CyanAccent else PanelBorder, RoundedCornerShape(6.dp))
                            .clickable { viewModel.setExportFormat(f) }
                            .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = f, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isChosen) Color.White else SecondaryText)
                        }
                    }
                }

                // FPS Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(24, 30, 60).forEach { f ->
                        val isChosen = fps == f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isChosen) SecondaryPanels else DarkPanels)
                                .border(0.5.dp, if (isChosen) CyanAccent else PanelBorder, RoundedCornerShape(6.dp))
                                .clickable { viewModel.setExportFps(f) }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "$f FPS", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = if (isChosen) CyanAccent else SecondaryText)
                        }
                    }
                }

                // Compact Quality Bar with Expandable 10 Options
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SecondaryPanels)
                        .border(0.5.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable { showAdvancedQuality = !showAdvancedQuality }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                            Text(text = "دقة وجودة التصدير:", fontSize = 11.sp, color = SecondaryText)
                            Text(text = editingQuality, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(text = if (showAdvancedQuality) "إغلاق" else "خيارات الجودة (10)", fontSize = 9.5.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                            Icon(
                                imageVector = if (showAdvancedQuality) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Expandable Options (All 10 Quality Options kept completely!)
                AnimatedVisibility(visible = showAdvancedQuality) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val chunks = qualityList.chunked(3)
                        chunks.forEach { rowQ ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                rowQ.forEach { q ->
                                    val isCurrent = editingQuality == q
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isCurrent) ElectricBlue else DarkPanels)
                                            .border(0.5.dp, if (isCurrent) CyanAccent else PanelBorder, RoundedCornerShape(6.dp))
                                            .clickable {
                                                viewModel.setEditingQuality(q)
                                                viewModel.setExportResolution(q)
                                            }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = q, fontSize = 9.sp, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal, color = if (isCurrent) Color.White else PrimaryText)
                                    }
                                }
                                if (rowQ.size < 3) {
                                    Spacer(modifier = Modifier.weight((3 - rowQ.size).toFloat()))
                                }
                            }
                        }

                        // Custom 1K dimensions inputs if chosen
                        if (editingQuality == "Custom 1K") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "الأبعاد المخصصة:", fontSize = 10.sp, color = CyanAccent)
                                OutlinedTextField(
                                    value = custom1KWidth.toString(),
                                    onValueChange = { viewModel.setCustom1KDimensions(it.toIntOrNull() ?: 1024, custom1KHeight) },
                                    label = { Text("العرض", fontSize = 8.sp) },
                                    modifier = Modifier.weight(1f).height(44.dp)
                                )
                                OutlinedTextField(
                                    value = custom1KHeight.toString(),
                                    onValueChange = { viewModel.setCustom1KDimensions(custom1KWidth, it.toIntOrNull() ?: 1024) },
                                    label = { Text("الارتفاع", fontSize = 8.sp) },
                                    modifier = Modifier.weight(1f).height(44.dp)
                                )
                            }
                        }
                    }
                }

                // Export Metrics & Processing Information
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkPanels)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "معلومات التشفير:", fontSize = 9.sp, color = SecondaryText)
                        Text(text = "H.264 / AAC • معدل البت: 18 Mbps", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                    }
                }

                exportNotice?.let { notice ->
                    Text(text = notice, fontSize = 10.sp, color = SecondaryText)
                }

                // Export Progress (if running)
                if (isExporting) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "جارٍ ترميز وتصدير الفيديو...", fontSize = 10.sp, color = PrimaryText)
                            Text(text = "${(progress * 100).toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = SuccessGreen,
                            trackColor = DarkPanels
                        )
                    }
                }

                // Primary Action Button: "📥 تصدير الآن"
                Button(
                    onClick = { viewModel.startExport() },
                    enabled = !isExporting,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("export_now_button")
                ) {
                    Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = DarkPanels, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isExporting) "جارٍ التصدير..." else "📥 تصدير الآن",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkPanels
                    )
                }
            }
        }

        // Section 3: قائمة الملفات المصدرة المكتملة
        Text(text = "الملفات والوسائط المصدرة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            tasks.forEach { task ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PanelCard)
                        .border(0.5.dp, PanelBorder, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SuccessGreen.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.VideoFile, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text(text = task.projectTitle, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                                Text(text = "${task.resolution} • ${task.format} • ${task.fileSizeMb.toInt()} MB", fontSize = 8.5.sp, color = SecondaryText)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SuccessGreen)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "مكتمل ✓", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = DarkPanels)
                        }
                    }
                }
            }
        }

        // ==========================================
        // 1. نافذة استيراد الوسائط والمشاريع عبر رابط مباشر
        // ==========================================
        if (showUrlDialog) {
            AlertDialog(
                onDismissRequest = {
                    showUrlDialog = false
                    urlError = null
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.AddLink, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
                        Text(text = "استيراد عبر رابط مباشر", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "أدخل رابط فيديو أو صوت أو مشروع سينمائي (HTTP/HTTPS):", fontSize = 11.sp, color = SecondaryText)
                        OutlinedTextField(
                            value = urlInput,
                            onValueChange = {
                                urlInput = it
                                urlError = null
                            },
                            placeholder = { Text("https://example.com/video.mp4", fontSize = 11.sp, color = SecondaryText) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dialog_url_input"),
                            singleLine = true,
                            isError = urlError != null
                        )
                        if (urlError != null) {
                            Text(text = urlError ?: "", fontSize = 10.sp, color = Color(0xFFEF4444))
                        }
                        Text(text = "روابط سريعة تجريبية سينمائية:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { urlInput = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4" },
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryPanels),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("فيديو تجريبي", fontSize = 9.sp, color = PrimaryText)
                            }
                            Button(
                                onClick = { urlInput = "https://actions.google.com/sounds/v1/ambiences/outdoor_ambience.ogg" },
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryPanels),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("صوت سينمائي", fontSize = 9.sp, color = PrimaryText)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (urlInput.isBlank() || (!urlInput.startsWith("http://") && !urlInput.startsWith("https://"))) {
                                urlError = "⚠️ يرجى إدخال رابط صالح يبدأ بـ https:// أو http://"
                            } else {
                                val success = viewModel.importFromUrl(urlInput)
                                if (success) {
                                    statusAlert = "✅ تم تنزيل واستيراد الوسائط من الرابط بنجاح وإضافتها للخط الزمني والمشروع!"
                                    isAlertSuccess = true
                                    showUrlDialog = false
                                    urlInput = ""
                                    urlError = null
                                } else {
                                    urlError = "فشل في معالجة الرابط المحدد"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        modifier = Modifier.testTag("dialog_url_submit")
                    ) {
                        Text("بدء التنزيل والاستيراد", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showUrlDialog = false
                        urlError = null
                    }) {
                        Text("إلغاء", fontSize = 11.sp, color = SecondaryText)
                    }
                },
                containerColor = PanelCard
            )
        }

        // ==========================================
        // 2. نافذة استيراد من Google Drive
        // ==========================================
        if (showGoogleDriveDialog) {
            AlertDialog(
                onDismissRequest = {
                    showGoogleDriveDialog = false
                    driveError = null
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.Cloud, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                        Text(text = "استيراد من Google Drive", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = "اختر طريقة استيراد الأصول والمشاريع من حساب Google Drive الخاص بك:", fontSize = 11.sp, color = SecondaryText)

                        // Option 1: SAF Document Picker (Google Drive Provider)
                        Button(
                            onClick = {
                                showGoogleDriveDialog = false
                                driveFileLauncher.launch(arrayOf("*/*"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_drive_browse_native")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, tint = DarkPanels, modifier = Modifier.size(16.dp))
                                Text("تصفح ملفات Google Drive في الهاتف (SAF)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = DarkPanels)
                            }
                        }

                        Text(text = "أو أدخل رابط مشاركة مباشر من Google Drive:", fontSize = 10.5.sp, color = SecondaryText)
                        OutlinedTextField(
                            value = driveLinkInput,
                            onValueChange = {
                                driveLinkInput = it
                                driveError = null
                            },
                            placeholder = { Text("https://drive.google.com/file/d/...", fontSize = 11.sp, color = SecondaryText) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dialog_drive_link_input"),
                            singleLine = true,
                            isError = driveError != null
                        )
                        if (driveError != null) {
                            Text(text = driveError ?: "", fontSize = 10.sp, color = Color(0xFFEF4444))
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (driveLinkInput.isBlank()) {
                                driveError = "⚠️ يرجى إدخال رابط ملف Google Drive"
                            } else if (!driveLinkInput.contains("drive.google.com") && !driveLinkInput.startsWith("http")) {
                                driveError = "⚠️ الرابط يجب أن يكون رابط Google Drive صالح"
                            } else {
                                viewModel.importFromGoogleDrive(driveLinkInput, isLink = true)
                                statusAlert = "✅ تم استيراد الملف من Google Drive بنجاح وإضافته للمشروع!"
                                isAlertSuccess = true
                                showGoogleDriveDialog = false
                                driveLinkInput = ""
                                driveError = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        modifier = Modifier.testTag("dialog_drive_submit")
                    ) {
                        Text("استيراد الرابط", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkPanels)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showGoogleDriveDialog = false
                        driveError = null
                    }) {
                        Text("إلغاء", fontSize = 11.sp, color = SecondaryText)
                    }
                },
                containerColor = PanelCard
            )
        }

        // ==========================================
        // 3. نافذة استيراد من Dropbox / OneDrive
        // ==========================================
        if (showCloudDialog) {
            AlertDialog(
                onDismissRequest = {
                    showCloudDialog = false
                    cloudError = null
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                        Text(text = "استيراد من Dropbox / OneDrive", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = "اختر طريقة استيراد الأصول السحابية الخارجية والمشاريع:", fontSize = 11.sp, color = SecondaryText)

                        Button(
                            onClick = {
                                showCloudDialog = false
                                cloudFileLauncher.launch(arrayOf("*/*"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_cloud_browse_native")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, tint = DarkPanels, modifier = Modifier.size(16.dp))
                                Text("تصفح التخزين السحابي الخارجي (SAF)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = DarkPanels)
                            }
                        }

                        Text(text = "أو أدخل رابط مشاركة Dropbox أو OneDrive:", fontSize = 10.5.sp, color = SecondaryText)
                        OutlinedTextField(
                            value = cloudLinkInput,
                            onValueChange = {
                                cloudLinkInput = it
                                cloudError = null
                            },
                            placeholder = { Text("https://www.dropbox.com/s/... أو https://1drv.ms/...", fontSize = 11.sp, color = SecondaryText) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dialog_cloud_link_input"),
                            singleLine = true,
                            isError = cloudError != null
                        )
                        if (cloudError != null) {
                            Text(text = cloudError ?: "", fontSize = 10.sp, color = Color(0xFFEF4444))
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (cloudLinkInput.isBlank()) {
                                cloudError = "⚠️ يرجى إدخال رابط مشاركة سحابي"
                            } else if (!cloudLinkInput.startsWith("http://") && !cloudLinkInput.startsWith("https://")) {
                                cloudError = "⚠️ الرابط يجب أن يبدأ بـ https://"
                            } else {
                                val service = if (cloudLinkInput.contains("dropbox")) "Dropbox" else "OneDrive"
                                viewModel.importFromCloudStorage(service, cloudLinkInput, isLink = true)
                                statusAlert = "✅ تم استيراد الملف السحابي من $service بنجاح وإضافته للمشروع!"
                                isAlertSuccess = true
                                showCloudDialog = false
                                cloudLinkInput = ""
                                cloudError = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        modifier = Modifier.testTag("dialog_cloud_submit")
                    ) {
                        Text("استيراد الرابط", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkPanels)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showCloudDialog = false
                        cloudError = null
                    }) {
                        Text("إلغاء", fontSize = 11.sp, color = SecondaryText)
                    }
                },
                containerColor = PanelCard
            )
        }
    }
}
