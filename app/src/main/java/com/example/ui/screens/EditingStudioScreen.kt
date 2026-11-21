package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.StudioLanguage
import com.example.data.model.StudioLanguageRegistry
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.PanelBorder
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryText
import com.example.viewmodel.StudioViewModel

@Composable
fun EditingStudioScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val isPlaying by viewModel.isPlayingVideo.collectAsState()
    val contrast by viewModel.videoContrast.collectAsState()
    val brightness by viewModel.videoBrightness.collectAsState()
    val sharpness by viewModel.videoSharpness.collectAsState()
    val editingQuality by viewModel.editingQuality.collectAsState()

    val subtitles by viewModel.subtitlesList.collectAsState()
    val targetLang by viewModel.subtitleTargetLanguage.collectAsState()
    val timelineClips by viewModel.timelineClips.collectAsState()
    val selectedClipId by viewModel.selectedClipId.collectAsState()
    val playheadMs by viewModel.timelinePlayheadMs.collectAsState()
    val timelineResult by viewModel.lastTimelineResult.collectAsState()

    var showCutTrimDialog by remember { mutableStateOf(false) }
    var showAddSubtitleDialog by remember { mutableStateOf(false) }
    var newSubtitleText by remember { mutableStateOf("") }
    var trimHeadMsInput by remember { mutableStateOf("1:23.00") }
    var trimTailMsInput by remember { mutableStateOf("1:30.00") }
    var textEditPrompt by remember { mutableStateOf("أزل الشخص الموجود في الخلفية") }
    var exportDialogContent by remember { mutableStateOf<Pair<String, String>?>(null) }

    var showLanguageModal by remember { mutableStateOf(false) }
    var showLanguageDropdown by remember { mutableStateOf(false) }
    var showQualityDropdown by remember { mutableStateOf(false) }
    var showAdvancedSettings by remember { mutableStateOf(false) }
    var showSubtitlesOnVideo by remember { mutableStateOf(true) }
    var colorMode by remember { mutableStateOf("سينمائي") } // "سينمائي", "واقعي", "حي"

    // Advanced settings toggles
    var isStabilizationOn by remember { mutableStateOf(true) }
    var isNoiseReductionOn by remember { mutableStateOf(true) }
    var isSlowMotionOn by remember { mutableStateOf(false) }
    var isFastMotionOn by remember { mutableStateOf(false) }
    var isAutoSubtitleOn by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()

    val activeCue = subtitles.firstOrNull() ?: com.example.data.model.SubtitleCue(
        id = 1,
        startMs = 1000L,
        endMs = 4000L,
        sourceText = "أنت أجمل حلم في حياتي",
        translatedText = "You are my most beautiful dream",
        sourceLanguage = "الجزائرية العامة",
        targetLanguage = "English"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .verticalScroll(scrollState)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ==========================================
        // 1. TOP HEADER & NAVIGATION BAR
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF0A192F), Color(0xFF162A45), Color(0xFF0A192F))
                    )
                )
                .border(1.5.dp, Color(0xFF00E5FF), RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Back Arrow
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F2B4A))
                    .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(8.dp))
                    .clickable { viewModel.setSection(0) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "الرجوع",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Center: App Title & Subtitle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF1E3A8A), Color(0xFF7C3AED))
                            )
                        )
                        .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "KAMAL DZ STUDIO 2027",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD700)
                    )
                    Text(
                        text = "Editing & Timeline — المونتاج والتحرير الزمني",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF)
                    )
                }
            }

            // Right: Crown VIP & Settings Icons
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF3B2D06))
                        .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "👑", fontSize = 18.sp)
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F2B4A))
                        .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(8.dp))
                        .clickable { showAdvancedSettings = !showAdvancedSettings },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "الإعدادات",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // ==========================================
        // 2. TOP ACTION BUTTONS ROW (6 items)
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val topActions = listOf(
                "مشروع جديد" to Icons.Default.Add,
                "فتح مشروع" to Icons.Default.Folder,
                "حفظ" to Icons.Default.Save,
                "تراجع" to Icons.Default.Undo,
                "إعادة" to Icons.Default.Redo,
                "المزيد" to Icons.Default.MoreHoriz
            )

            topActions.forEach { (label, icon) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F2B4A))
                        .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .clickable {
                            when (label) {
                                "مشروع جديد" -> viewModel.importMediaAsset("مشروع جديد", 5000L, R.drawable.img_editing_bay)
                                "حفظ" -> viewModel.saveTimelineToCurrentProject()
                                "تراجع" -> viewModel.cutSelectedClip(0L, 0L)
                                else -> {}
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = label,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // ==========================================
        // 3. MAIN PREVIEW & AI SIDEBAR CONTAINER
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // LEFT: VIDEO PREVIEW BOX (Larger central screen)
            Box(
                modifier = Modifier
                    .weight(1.8f)
                    .height(250.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black)
                    .border(1.5.dp, Color(0xFF00E5FF), RoundedCornerShape(10.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_editing_bay),
                    contentDescription = "معاينة الفيديو",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Top Left: Quality Dropdown & 4K Gold Badge
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xCC000000))
                            .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(6.dp))
                            .clickable { showQualityDropdown = true }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "$editingQuality ▾",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        DropdownMenu(
                            expanded = showQualityDropdown,
                            onDismissRequest = { showQualityDropdown = false },
                            modifier = Modifier.background(Color(0xFF0A192F))
                        ) {
                            listOf("360p", "480p", "720p", "1080p", "1K", "2K", "4K", "8K").forEach { q ->
                                DropdownMenuItem(
                                    text = { Text(q, color = Color.White, fontSize = 11.sp) },
                                    onClick = {
                                        viewModel.setEditingQuality(q)
                                        showQualityDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFFFD700), Color(0xFFB8860B))
                                )
                            )
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "4K ULTRA HD",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }

                // Top Right: Fullscreen Button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xCC000000))
                        .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "ملء الشاشة",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Bottom Left Overlay: Magic Reel + Magic Wand (✨ الريشة السحرية inside video)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xCC0A192F))
                            .border(1.5.dp, Color(0xFF00E5FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // ✨ الريشة السحرية Magic Wand button inside video
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFFFD700), Color(0xFFFF8C00))
                                )
                            )
                            .border(2.dp, Color.White, CircleShape)
                            .clickable { showAddSubtitleDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "الريشة السحرية",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Subtitle Overlay (Dual Subtitle on video)
                if (showSubtitlesOnVideo) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xCC000000))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = activeCue.sourceText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = activeCue.translatedText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                    }
                }
            }

            // RIGHT: AI TOOLS SIDEBAR (🤖 أدوات الذكاء الاصطناعي)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(250.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0A192F))
                    .border(1.5.dp, Color(0xFF00E5FF), RoundedCornerShape(10.dp))
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🤖 أدوات الذكاء الاصطناعي",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF)
                    )
                }

                // Box 1: Delete Clip (حذف مقطع)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF162A45))
                        .border(1.dp, Color(0xFFE53E3E), RoundedCornerShape(8.dp))
                        .padding(6.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color(0xFFE53E3E), modifier = Modifier.size(14.dp))
                            Text(text = "حذف مقطع", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "من:", fontSize = 9.sp, color = Color.Gray)
                            Text(text = trimHeadMsInput, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "إلى:", fontSize = 9.sp, color = Color.Gray)
                            Text(text = trimTailMsInput, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFE53E3E), Color(0xFF991B1B))
                                    )
                                )
                                .clickable { viewModel.cutSelectedClip(500L, 500L) },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Text(text = "حذف اللقطة", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                // Box 2: Text Editing (التعديل بالنص)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF162A45))
                        .border(1.dp, Color(0xFF7C3AED), RoundedCornerShape(8.dp))
                        .padding(6.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                            Text(text = "التعديل بالنص", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Text(
                            text = textEditPrompt,
                            fontSize = 8.5.sp,
                            color = Color(0xFF00E5FF),
                            maxLines = 2
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF7C3AED), Color(0xFF3B82F6))
                                    )
                                )
                                .clickable { viewModel.executeAiEditCommand() },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Text(text = "تطبيق", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 4. SUBTITLE & LANGUAGE CONTROL ROW
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 1. AI Auto Subtitles
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF7C3AED), Color(0xFF2563EB))
                        )
                    )
                    .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(8.dp))
                    .clickable { viewModel.runSpeechToTextAndAutoSubtitles() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "الترجمة التلقائية بذكاء اصطناعي", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                }
            }

            // 2. Algerian Dialects & Languages Modal Trigger
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F2B4A))
                    .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(8.dp))
                    .clickable { showLanguageModal = true },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "🇩🇿 الترجمة الجزائرية واللهجات", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // 3. Target Language Selector
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F2B4A))
                    .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(8.dp))
                    .clickable { showLanguageDropdown = true },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "لغات الترجمة", fontSize = 7.5.sp, color = Color.Gray)
                    Text(text = "${targetLang.flagEmoji} ${targetLang.nameAr} ▾", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                }

                DropdownMenu(
                    expanded = showLanguageDropdown,
                    onDismissRequest = { showLanguageDropdown = false },
                    modifier = Modifier.background(Color(0xFF0A192F))
                ) {
                    listOf(
                        StudioLanguageRegistry.findLanguage("en"),
                        StudioLanguageRegistry.findLanguage("it"),
                        StudioLanguageRegistry.findLanguage("tr"),
                        StudioLanguageRegistry.findLanguage("fr"),
                        StudioLanguageRegistry.findLanguage("es"),
                        StudioLanguageRegistry.findLanguage("de"),
                        StudioLanguageRegistry.findLanguage("ar_dz")
                    ).forEach { lang ->
                        DropdownMenuItem(
                            text = { Text("${lang.flagEmoji} ${lang.nameAr}", color = Color.White, fontSize = 11.sp) },
                            onClick = {
                                viewModel.selectLanguageForPurpose(lang)
                                showLanguageDropdown = false
                            }
                        )
                    }
                }
            }

            // 4. Show Subtitles Toggle Switch Box
            Box(
                modifier = Modifier
                    .weight(1.1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F2B4A))
                    .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(8.dp))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(text = "إعدادات الترجمة", fontSize = 7.5.sp, color = Color.Gray)
                        Text(text = "إظهار الترجمة", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Switch(
                        checked = showSubtitlesOnVideo,
                        onCheckedChange = { showSubtitlesOnVideo = it },
                        modifier = Modifier.size(24.dp),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B981)
                        )
                    )
                }
            }
        }

        // ==========================================
        // 5. PLAYBACK & VOLUME CONTROLS BAR
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF0A192F))
                .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(10.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute / Volume Icon Button
            IconButton(onClick = { }, modifier = Modifier.size(34.dp)) {
                Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "الصوت", tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
            }

            // Step Rewind
            IconButton(onClick = { viewModel.setTimelinePlayhead((playheadMs - 2000L).coerceAtLeast(0L)) }, modifier = Modifier.size(34.dp)) {
                Icon(imageVector = Icons.Default.FastRewind, contentDescription = "ترجيع", tint = Color.White, modifier = Modifier.size(22.dp))
            }

            // Central Play/Pause Button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF2563EB), Color(0xFF00E5FF))
                        )
                    )
                    .border(2.dp, Color.White, CircleShape)
                    .clickable { viewModel.toggleVideoPlay() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "تشغيل",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Step Forward
            IconButton(onClick = { viewModel.setTimelinePlayhead(playheadMs + 2000L) }, modifier = Modifier.size(34.dp)) {
                Icon(imageVector = Icons.Default.FastForward, contentDescription = "تقديم", tint = Color.White, modifier = Modifier.size(22.dp))
            }

            // Expand / Aspect Ratio Button
            IconButton(onClick = { }, modifier = Modifier.size(34.dp)) {
                Icon(imageVector = Icons.Default.Fullscreen, contentDescription = "تكبير", tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
            }

            // Volume Slider & Percentage
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(14.dp))
                Slider(
                    value = 1f,
                    onValueChange = { },
                    modifier = Modifier.width(60.dp).height(16.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF00E5FF),
                        activeTrackColor = Color(0xFF00E5FF)
                    )
                )
                Text(text = "100%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        // ==========================================
        // 6. TIMELINE SECTION
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF0A192F))
                .border(1.5.dp, Color(0xFF00E5FF), RoundedCornerShape(10.dp))
                .padding(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Time Code Ruler (00:00 00:05 00:10 00:15 00:20 00:25 00:30)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 60.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("00:00", "00:05", "00:10", "00:15", "00:20", "00:25", "00:30").forEach { time ->
                        Text(text = time, fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = Color.Gray)
                    }
                }

                // Interactive Scrubber Slider with Red Playhead Line
                val maxDur = viewModel.totalTimelineDurationMs.toFloat().coerceAtLeast(1000f)
                Slider(
                    value = playheadMs.toFloat().coerceIn(0f, maxDur),
                    onValueChange = { viewModel.setTimelinePlayhead(it.toLong()) },
                    valueRange = 0f..maxDur,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .testTag("timeline_playhead_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFE53E3E),
                        activeTrackColor = Color(0xFF00E5FF),
                        inactiveTrackColor = Color(0xFF162A45)
                    )
                )

                // TRACK 1: VIDEO TRACK
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .fillMaxSize()
                            .background(Color(0xFF0F2B4A))
                            .border(0.5.dp, Color(0xFF00E5FF), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Icon(imageVector = Icons.Default.Movie, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(12.dp))
                            Text(text = "الفيديو", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF162A45)),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        timelineClips.forEach { clip ->
                            Box(
                                modifier = Modifier
                                    .weight(clip.durationMs.toFloat().coerceAtLeast(500f))
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(clip.colorHex))
                                    .clickable { viewModel.selectTimelineClip(clip.id) }
                            ) {
                                Image(
                                    painter = painterResource(id = clip.drawableResId ?: R.drawable.img_editing_bay),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    alpha = 0.7f
                                )
                            }
                        }
                    }
                }

                // TRACK 2: AUDIO TRACK (Green Waveform Graph)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .fillMaxSize()
                            .background(Color(0xFF0F2B4A))
                            .border(0.5.dp, Color(0xFF00FF88), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFF00FF88), modifier = Modifier.size(12.dp))
                            Text(text = "الصوت", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF062D1B))
                            .border(0.5.dp, Color(0xFF00FF88), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp)
                    ) {
                        WaveformVisualizer(
                            isPlaying = isPlaying,
                            height = 20.dp,
                            barCount = 42,
                            barColor = Color(0xFF00FF88)
                        )
                    }
                }

                // TRACK 3: SUBTITLE TRACK (Purple Subtitle Blocks)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .fillMaxSize()
                            .background(Color(0xFF0F2B4A))
                            .border(0.5.dp, Color(0xFFD800FF), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Icon(imageVector = Icons.Default.Subtitles, contentDescription = null, tint = Color(0xFFD800FF), modifier = Modifier.size(12.dp))
                            Text(text = "الترجمة", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF2A0845)),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("مرحبا بكم في KAMAL DZ STUDIO", "مونتاج احترافي بكل سهولة").forEach { cueText ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF7C3AED))
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cueText,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 7. VIDEO ENHANCEMENT & COLOR CONTROLS ROW
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Left Card (70%): ✨ تحسين جودة الفيديو
            Box(
                modifier = Modifier
                    .weight(2f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0A192F))
                    .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(10.dp))
                    .padding(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                        Text(text = "تحسين جودة الفيديو", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "السطوع ☀️", fontSize = 8.sp, color = Color.White)
                        Slider(
                            value = brightness,
                            onValueChange = { viewModel.setVideoBrightness(it) },
                            modifier = Modifier.weight(1f).height(12.dp),
                            colors = SliderDefaults.colors(thumbColor = Color(0xFFFFD700), activeTrackColor = Color(0xFFFFD700))
                        )

                        Text(text = "التباين 🌓", fontSize = 8.sp, color = Color.White)
                        Slider(
                            value = contrast,
                            onValueChange = { viewModel.setVideoContrast(it) },
                            modifier = Modifier.weight(1f).height(12.dp),
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF00E5FF), activeTrackColor = Color(0xFF00E5FF))
                        )

                        Text(text = "الألوان 🎨", fontSize = 8.sp, color = Color.White)
                        Slider(
                            value = 0.6f,
                            onValueChange = { },
                            modifier = Modifier.weight(1f).height(12.dp),
                            colors = SliderDefaults.colors(thumbColor = Color(0xFFD800FF), activeTrackColor = Color(0xFFD800FF))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "الحدة 🔍", fontSize = 8.sp, color = Color.White)
                        Slider(
                            value = sharpness,
                            onValueChange = { viewModel.setVideoSharpness(it) },
                            modifier = Modifier.weight(1f).height(12.dp),
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF00FF88), activeTrackColor = Color(0xFF00FF88))
                        )

                        Text(text = "الوضوح السينمائي 📺 (HD)", fontSize = 8.sp, color = Color.White)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF0F2B4A))
                                .border(0.5.dp, Color(0xFF00E5FF), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "HD", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                        }
                    }
                }
            }

            // Right Card (30%): 🎨 التحكم في الألوان
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0A192F))
                    .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(10.dp))
                    .padding(8.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🎨 التحكم في الألوان", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        listOf("سينمائي", "واقعي", "حي").forEach { mode ->
                            val isSel = colorMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSel) Color(0xFF0F2B4A) else Color(0xFF162A45))
                                    .border(
                                        width = if (isSel) 1.dp else 0.dp,
                                        color = if (isSel) Color(0xFF00E5FF) else Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .clickable { colorMode = mode },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode,
                                    fontSize = 8.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color(0xFF00E5FF) else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 8. EDITING TOOLS GRID (8 Styled Action Buttons)
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tools = listOf(
                "قص" to Icons.Default.ContentCut,
                "تقسيم" to Icons.Default.Crop,
                "حذف" to Icons.Default.Delete,
                "السرعة" to Icons.Default.Speed,
                "الفلاتر" to Icons.Default.Filter,
                "المؤثرات" to Icons.Default.AutoAwesome,
                "نص" to Icons.Default.Title,
                "ملصقات" to Icons.Default.Star
            )

            tools.forEach { (label, icon) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F2B4A))
                        .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .clickable {
                            when (label) {
                                "قص" -> showCutTrimDialog = true
                                "تقسيم" -> viewModel.splitSelectedClipAtPlayhead()
                                "حذف" -> viewModel.cutSelectedClip(500L, 0L)
                                else -> {}
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = label,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // ==========================================
        // 9. BOTTOM ACTION BAR (Preview, Export & Advanced Settings Toggle)
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Button 1: 👁️ معاينة
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
                        )
                    )
                    .border(1.5.dp, Color(0xFF00E5FF), RoundedCornerShape(10.dp))
                    .clickable { viewModel.toggleVideoPlay() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "👁️", fontSize = 16.sp)
                    Text(text = "معاينة", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Button 2 (CENTER): 🎬 تصدير الفيديو (Large Gold Button)
            Box(
                modifier = Modifier
                    .weight(2f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                        )
                    )
                    .border(2.dp, Color.White, RoundedCornerShape(10.dp))
                    .clickable { viewModel.saveTimelineToCurrentProject() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Movie, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                    Text(text = "تصدير الفيديو", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color.Black)
                }
            }

            // Button 3: ⚙️ إعدادات متقدمة ⤓ (Purple Neon Toggle)
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF7C3AED), Color(0xFF4C1D95))
                        )
                    )
                    .border(1.5.dp, Color(0xFFD800FF), RoundedCornerShape(10.dp))
                    .clickable { showAdvancedSettings = !showAdvancedSettings },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "إعدادات متقدمة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Icon(
                        imageVector = if (showAdvancedSettings) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // ==========================================
        // 10. ADVANCED SETTINGS PANEL (EXPANDED - Image 2 Design)
        // ==========================================
        AnimatedVisibility(
            visible = showAdvancedSettings,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0A192F))
                    .border(1.5.dp, Color(0xFFD800FF), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "⚙️ الإعدادات المتقدمة الكاملة",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )

                // 4 Grid Cards matching Image 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Card 1: تحسين الجودة
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F2B4A))
                            .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "تحسين الجودة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "الحدة", fontSize = 9.sp, color = Color.White)
                                Text(text = "+50", fontSize = 9.sp, color = Color(0xFF00E5FF))
                            }
                            Slider(value = 0.75f, onValueChange = {}, modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "التباين", fontSize = 9.sp, color = Color.White)
                                Text(text = "+50", fontSize = 9.sp, color = Color(0xFF00E5FF))
                            }
                            Slider(value = 0.75f, onValueChange = {}, modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "السطوع", fontSize = 9.sp, color = Color.White)
                                Text(text = "+0", fontSize = 9.sp, color = Color(0xFF00E5FF))
                            }
                            Slider(value = 0.5f, onValueChange = {}, modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "التشبع اللوني", fontSize = 9.sp, color = Color.White)
                                Text(text = "+50", fontSize = 9.sp, color = Color(0xFF00E5FF))
                            }
                            Slider(value = 0.75f, onValueChange = {}, modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(26.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF162A45))
                                    .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(6.dp))
                                    .clickable {
                                        viewModel.setVideoBrightness(0.5f)
                                        viewModel.setVideoContrast(0.5f)
                                        viewModel.setVideoSharpness(0.5f)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(12.dp))
                                    Text(text = "إعادة تعيين", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    // Card 2: تأثيرات بصرية
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F2B4A))
                            .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "تأثيرات بصرية", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "تثبيت الفيديو", fontSize = 9.sp, color = Color.White)
                                Switch(checked = isStabilizationOn, onCheckedChange = { isStabilizationOn = it }, modifier = Modifier.size(20.dp))
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "تقليل الضوضاء", fontSize = 9.sp, color = Color.White)
                                Switch(checked = isNoiseReductionOn, onCheckedChange = { isNoiseReductionOn = it }, modifier = Modifier.size(20.dp))
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "الحركة البطئية", fontSize = 9.sp, color = Color.White)
                                Switch(checked = isSlowMotionOn, onCheckedChange = { isSlowMotionOn = it }, modifier = Modifier.size(20.dp))
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "الحركة السريعة", fontSize = 9.sp, color = Color.White)
                                Switch(checked = isFastMotionOn, onCheckedChange = { isFastMotionOn = it }, modifier = Modifier.size(20.dp))
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(26.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF162A45))
                                    .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                                    Text(text = "المزيد من التأثيرات", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    // Card 3: الترجمة الذكية
                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F2B4A))
                            .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "الترجمة الذكية", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))

                            Text(text = "اللغة الأساسية", fontSize = 8.5.sp, color = Color.Gray)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF162A45))
                                    .border(0.5.dp, Color(0xFF00E5FF), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(text = "🇬🇧 English ▾", fontSize = 9.sp, color = Color.White)
                            }

                            Text(text = "لهجة الترجمة", fontSize = 8.5.sp, color = Color.Gray)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF162A45))
                                    .border(0.5.dp, Color(0xFF00E5FF), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(text = "British ▾", fontSize = 9.sp, color = Color.White)
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "ترجمة تلقائية", fontSize = 8.5.sp, color = Color.White)
                                Switch(checked = isAutoSubtitleOn, onCheckedChange = { isAutoSubtitleOn = it }, modifier = Modifier.size(20.dp))
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "إضافة ترجمة على الفيديو", fontSize = 8.5.sp, color = Color.White)
                                Switch(checked = showSubtitlesOnVideo, onCheckedChange = { showSubtitlesOnVideo = it }, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    // Card 4: أدوات التعديل الذكي
                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F2B4A))
                            .border(1.dp, Color(0xFFE53E3E), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "أدوات التعديل الذكي", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color(0xFFE53E3E), modifier = Modifier.size(14.dp))
                                Text(text = "حذف اللقطة", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "من:", fontSize = 9.sp, color = Color.Gray)
                                Text(text = trimHeadMsInput, fontSize = 9.sp, color = Color.White)
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "إلى:", fontSize = 9.sp, color = Color.Gray)
                                Text(text = trimTailMsInput, fontSize = 9.sp, color = Color.White)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFFE53E3E), Color(0xFF991B1B))
                                        )
                                    )
                                    .clickable { viewModel.cutSelectedClip(500L, 500L) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🗑️ حذف المقطع", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                // Advanced Export Banner (Yellow/Gold Banner)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                            )
                        )
                        .border(1.5.dp, Color.White, RoundedCornerShape(8.dp))
                        .clickable { viewModel.saveTimelineToCurrentProject() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎬 تصدير الفيديو بدقة عالية (MP4 - 4K)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }
        }

        // ==========================================
        // 11. ALGERIAN DIALECTS & LANGUAGES MODAL
        // ==========================================
        if (showLanguageModal) {
            AlertDialog(
                onDismissRequest = { showLanguageModal = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🇩🇿 اختيار اللهجات واللغات والترجمة", fontWeight = FontWeight.Bold, color = Color(0xFFFFD700), fontSize = 14.sp)
                        IconButton(onClick = { showLanguageModal = false }) {
                            Text(text = "✕", color = Color.White, fontSize = 16.sp)
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "اللهجات الجزائرية المدعومة:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))

                        val dzDialects = listOf(
                            "🇩🇿 الجزائرية العامة / الدارجة الجزائرية" to "ar_dz",
                            "🟢 الوهرانية (وهرانية)" to "ar_dz",
                            "🟢 السطايفية (سطايفية)" to "ar_dz",
                            "🟢 القبائلية (قبائلية)" to "ar_dz",
                            "🟢 الشاوية (شاوية)" to "ar_dz",
                            "🟢 الصحراوية (صحراوية)" to "ar_dz"
                        )

                        dzDialects.forEach { (name, code) ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0F2B4A))
                                    .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .clickable {
                                        val l = StudioLanguageRegistry.findLanguage(code)
                                        viewModel.selectLanguageForPurpose(l)
                                        showLanguageModal = false
                                    }
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(text = "تحديد ✓", fontSize = 10.sp, color = Color(0xFF00E5FF))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "اللغات العالمية المستهدفة:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))

                        val globalLangs = listOf(
                            StudioLanguageRegistry.findLanguage("en"),
                            StudioLanguageRegistry.findLanguage("it"),
                            StudioLanguageRegistry.findLanguage("tr"),
                            StudioLanguageRegistry.findLanguage("fr"),
                            StudioLanguageRegistry.findLanguage("es"),
                            StudioLanguageRegistry.findLanguage("de")
                        )

                        globalLangs.forEach { lang ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF162A45))
                                    .clickable {
                                        viewModel.selectLanguageForPurpose(lang)
                                        showLanguageModal = false
                                    }
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "${lang.flagEmoji} ${lang.nameAr} (${lang.nameEn})", fontSize = 11.sp, color = Color.White)
                                    Text(text = "ترجمة", fontSize = 10.sp, color = Color(0xFFFFD700))
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLanguageModal = false }) {
                        Text("تم الإغلاق", color = Color(0xFF00E5FF))
                    }
                },
                containerColor = Color(0xFF0A192F)
            )
        }

        // ==========================================
        // 12. DIALOGS (Trim & Export)
        // ==========================================
        if (showCutTrimDialog) {
            val currentSelected = timelineClips.find { it.id == selectedClipId }
            AlertDialog(
                onDismissRequest = { showCutTrimDialog = false },
                title = {
                    Text(text = "اقتطاع وقص المقطع السينمائي", fontWeight = FontWeight.Bold, color = Color(0xFFFFD700), fontSize = 13.sp)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "المقطع: ${currentSelected?.name ?: "مشهد 1"} (${currentSelected?.formatDuration() ?: ""})",
                            fontSize = 10.5.sp,
                            color = Color(0xFF00E5FF)
                        )
                        OutlinedTextField(
                            value = trimHeadMsInput,
                            onValueChange = { trimHeadMsInput = it },
                            label = { Text("اقتطاع من البداية (من)", fontSize = 9.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = PrimaryText)
                        )
                        OutlinedTextField(
                            value = trimTailMsInput,
                            onValueChange = { trimTailMsInput = it },
                            label = { Text("اقتطاع إلى النهاية (إلى)", fontSize = 9.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = PrimaryText)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val head = 500L
                            val tail = 500L
                            viewModel.cutSelectedClip(head, tail)
                            showCutTrimDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53E3E))
                    ) {
                        Text("حذف اللقطة", color = Color.White, fontSize = 10.sp)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCutTrimDialog = false }) {
                        Text("إلغاء", color = SecondaryText, fontSize = 10.sp)
                    }
                },
                containerColor = Color(0xFF0A192F)
            )
        }

        if (showAddSubtitleDialog) {
            AlertDialog(
                onDismissRequest = { showAddSubtitleDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                        Text(text = "الريشة السحرية - إضافة ترجمة تلقائية", fontWeight = FontWeight.Bold, color = Color(0xFFFFD700), fontSize = 13.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("أدخل نص الترجمة الذكية للظهور فوق المشهد:", fontSize = 11.sp, color = Color.White)
                        OutlinedTextField(
                            value = newSubtitleText,
                            onValueChange = { newSubtitleText = it },
                            label = { Text("نص الترجمة", fontSize = 10.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = PrimaryText)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newSubtitleText.isNotBlank()) {
                                viewModel.addSubtitleCue(newSubtitleText, 5000L, 15000L)
                                newSubtitleText = ""
                            }
                            showAddSubtitleDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                    ) {
                        Text("إضافة النص", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddSubtitleDialog = false }) {
                        Text("إلغاء", color = SecondaryText, fontSize = 10.sp)
                    }
                },
                containerColor = Color(0xFF0A192F)
            )
        }
    }
}
