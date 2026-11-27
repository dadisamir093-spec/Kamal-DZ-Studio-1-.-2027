package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.util.ImageExport
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.UnifiedQualitySelector
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkPanels
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.PanelBorder
import com.example.ui.theme.PanelCard
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.SecondaryPanels
import com.example.ui.theme.SecondaryText
import com.example.viewmodel.StudioViewModel

@Composable
fun GenerationStudioScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val activeSlot by viewModel.activeSlot.collectAsState()
    val slot1 by viewModel.slot1State.collectAsState()
    val slot2 by viewModel.slot2State.collectAsState()
    val currentSlot = if (activeSlot == 1) slot1 else slot2
    val scriptLanguage by viewModel.scriptLanguage.collectAsState()
    val selectedCharCat by viewModel.selectedCharacterCategory.collectAsState()
    val genQuality by viewModel.generationQuality.collectAsState()
    val custom1KWidth by viewModel.custom1KWidth.collectAsState()
    val custom1KHeight by viewModel.custom1KHeight.collectAsState()
    val remainingDailyQuota by viewModel.remainingDailyQuota.collectAsState()

    val scrollState = rememberScrollState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.updateCurrentSlotReferenceImage(uri.toString())
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    var exportMessage by remember { mutableStateOf<String?>(null) }
    fun saveNow() {
        val path = currentSlot.resultImageFile ?: return
        exportMessage = ImageExport.saveToGallery(context, java.io.File(path)).fold(
            onSuccess = { "✅ تم الحفظ في المعرض: $it" },
            onFailure = { "❌ فشل الحفظ: ${it.message ?: it.javaClass.simpleName}" }
        )
    }
    val storagePermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) saveNow()
        else exportMessage = "❌ فشل الحفظ: لم تُمنح صلاحية التخزين المطلوبة على Android 9 وما قبله"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkPanels)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Single Horizontal Rectangle Bar with 5 Independent Slots (Explicit RTL Layout Direction):
        // [ الصورة المولدة ] [ توليد الصورة ] [ نص إلى فيديو ] [ شاشة 1 ] [ شاشة 2 ]
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SecondaryPanels)
                .border(0.5.dp, PanelBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .testTag("generation_reference_image_bar")
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Slot 1 (Far Right in RTL): [ الصورة المولدة ] - Independent Slot
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(0.5.dp, CyanAccent.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .background(DarkPanels)
                            .testTag("slot_generated_image"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentSlot.resultImageFile != null) {
                            AsyncImage(
                                model = currentSlot.resultImageFile,
                                contentDescription = "الصورة المولدة",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "الصورة المولدة",
                                tint = CyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Slot 2: [ توليد الصورة ] - Independent Slot
                    val isImageGen = currentSlot.mode == "توليد الصور"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isImageGen) ElectricBlue else DarkPanels)
                            .border(0.5.dp, if (isImageGen) CyanAccent else PanelBorder, RoundedCornerShape(6.dp))
                            .clickable { viewModel.updateCurrentSlotMode("توليد الصور") }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("slot_image_gen"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "توليد الصورة",
                            fontSize = 11.sp,
                            fontWeight = if (isImageGen) FontWeight.Bold else FontWeight.Normal,
                            color = if (isImageGen) Color.White else SecondaryText
                        )
                    }

                    // Slot 3: [ نص إلى فيديو ] - Independent Slot
                    val isTextToVideo = currentSlot.mode == "نص إلى فيديو" || currentSlot.mode == "صورة إلى فيديو"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isTextToVideo) ElectricBlue else DarkPanels)
                            .border(0.5.dp, if (isTextToVideo) CyanAccent else PanelBorder, RoundedCornerShape(6.dp))
                            .clickable { viewModel.updateCurrentSlotMode("نص إلى فيديو") }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("slot_text_to_video"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "نص إلى فيديو",
                            fontSize = 11.sp,
                            fontWeight = if (isTextToVideo) FontWeight.Bold else FontWeight.Normal,
                            color = if (isTextToVideo) Color.White else SecondaryText
                        )
                    }

                    // Slot 4: [ شاشة 1 ] - Independent Slot
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (activeSlot == 1) ElectricBlue else DarkPanels)
                            .border(0.5.dp, if (activeSlot == 1) CyanAccent else PanelBorder, RoundedCornerShape(6.dp))
                            .clickable { viewModel.setActiveSlot(1) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("slot_screen_1"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "شاشة 1",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeSlot == 1) Color.White else SecondaryText
                        )
                    }

                    // Slot 5 (Far Left in RTL): [ شاشة 2 ] - Independent Slot
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (activeSlot == 2) ElectricBlue else DarkPanels)
                            .border(0.5.dp, if (activeSlot == 2) CyanAccent else PanelBorder, RoundedCornerShape(6.dp))
                            .clickable { viewModel.setActiveSlot(2) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("slot_screen_2"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "شاشة 2",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeSlot == 2) Color.White else SecondaryText
                        )
                    }
                }
            }
        }

        // Hero Preview Player Card (matches screenshot)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9.5f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, PanelBorder, RoundedCornerShape(16.dp))
                .background(SecondaryPanels)
                .testTag("generation_hero_preview")
        ) {
            if (currentSlot.resultImageFile != null) {
                AsyncImage(
                    model = currentSlot.resultImageFile,
                    contentDescription = "معاينة التوليد السينمائي",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (currentSlot.resultImageRes != null) {
                Image(
                    painter = painterResource(id = currentSlot.resultImageRes),
                    contentDescription = "معاينة التوليد السينمائي",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "لا توجد صورة مولّدة بعد. اضغط «إنشاء الآن» لتوليد صورة حقيقية.",
                        fontSize = 12.sp,
                        color = SecondaryText,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Dark Vignette Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0x99070B18))
                        )
                    )
            )

            if (currentSlot.mode != "توليد الصور") {
            // Play Button Overlay
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xCC0D1427))
                    .border(1.dp, CyanAccent.copy(alpha = 0.8f), CircleShape)
                    .align(Alignment.Center)
                    .clickable { /* Toggle preview play */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "تشغيل",
                    tint = PrimaryText,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Top Badges (Duration & Resolution)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xBB000000))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = currentSlot.durationText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xBB000000))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = currentSlot.resolution,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                }
            }
            }
        }

        if (currentSlot.hasResult && currentSlot.resultImageFile != null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED
                        ) storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        else saveNow()
                    },
                    modifier = Modifier.weight(1f).testTag("save_to_gallery_button")
                ) { Text("💾 حفظ في المعرض", fontSize = 12.sp) }
                Button(
                    onClick = {
                        ImageExport.share(context, java.io.File(currentSlot.resultImageFile!!)).onFailure {
                            exportMessage = "❌ فشلت المشاركة: ${it.message ?: it.javaClass.simpleName}"
                        }
                    },
                    modifier = Modifier.weight(1f).testTag("share_image_button")
                ) { Text("📤 مشاركة", fontSize = 12.sp) }
            }
            exportMessage?.let { Text(it, fontSize = 11.sp, color = PrimaryText) }
        }

        // Mode Selector Chips ("توليد الصور", "صورة إلى فيديو", "نص إلى فيديو")
        val modes = listOf("توليد الصور", "صورة إلى فيديو", "نص إلى فيديو")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            modes.forEach { mode ->
                val isSelected = currentSlot.mode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) ElectricBlue else SecondaryPanels)
                        .border(0.5.dp, if (isSelected) CyanAccent else PanelBorder, RoundedCornerShape(8.dp))
                        .clickable { viewModel.updateCurrentSlotMode(mode) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else SecondaryText
                    )
                }
            }
        }

        if (currentSlot.mode == "نص إلى فيديو") {
            Text(
                "🎬 نص إلى فيديو: سيولّد 4 صور متتابعة عبر مسار توليد الصور المجاني (نفس الحصة اليومية، التكلفة تُعرض قبل البدء)، ثم تُركَّب فيديو قصيراً محلياً بحركة تقريب وانتقال متدرج، ويُحفظ في Movies/KamalDZStudio.",
                fontSize = 11.sp,
                color = SecondaryText
            )
        } else if (currentSlot.mode != "توليد الصور") {
            Text(
                "⚠️ هذا الوضع غير متاح حاليًا: لا يوجد نموذج مجاني متصل. استخدم «توليد الصور» أو «نص إلى فيديو».",
                fontSize = 11.sp,
                color = SecondaryText
            )
        }
        Text(
            "الصورة المرجعية وتوليد صورة من صورة غير متاحين حاليًا؛ التوليد من النص فقط.",
            fontSize = 10.sp,
            color = SecondaryText
        )

        // Prompt Input Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PanelCard)
                .border(0.5.dp, PanelBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "أدخل البرومبت السينمائي",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )

                    // Cinematic Optimizer Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SecondaryPanels)
                            .border(0.5.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .clickable { viewModel.optimizePromptCinematically() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "تحسين سينمائي",
                                fontSize = 10.sp,
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Universal Dialect & Language Selector Bar (Compact & Elegant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SecondaryPanels)
                            .border(0.5.dp, PanelBorder, RoundedCornerShape(8.dp))
                            .clickable { viewModel.openLanguagePicker("script", "اختر لغة ولهجة السيناريو والحوار") }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("generation_dialect_compact_chip")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = scriptLanguage.flagEmoji, fontSize = 13.sp)
                            Text(
                                text = "اللهجة: ${scriptLanguage.nameAr}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "تغيير اللهجة",
                                tint = CyanAccent,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    // Button to inject authentic dialogue
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricBlue.copy(alpha = 0.25f))
                            .border(0.5.dp, ElectricBlue, RoundedCornerShape(8.dp))
                            .clickable { viewModel.generateScriptDialogueWithDialect() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "توليد حوار أصيل",
                                fontSize = 10.sp,
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = currentSlot.prompt,
                    onValueChange = { viewModel.updateCurrentSlotPrompt(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prompt_input_field"),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = PanelBorder,
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText,
                        focusedContainerColor = DarkPanels,
                        unfocusedContainerColor = DarkPanels
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, lineHeight = 18.sp, color = PrimaryText)
                )

                // 8 Character Categories Selector (طفل، طفلة، شاب، شابة، امرأة، رجل، عجوز، شيخ)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "فئة الشخصية للمشهد:", fontSize = 11.sp, color = SecondaryText)
                        Text(text = selectedCharCat, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                    }
                    val cats = listOf("طفل", "طفلة", "شاب", "شابة", "امرأة", "رجل", "عجوز", "شيخ")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        cats.take(4).forEach { cat ->
                            val isChosen = selectedCharCat == cat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isChosen) ElectricBlue else SecondaryPanels)
                                    .clickable { viewModel.setSelectedCharacterCategory(cat) }
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 10.sp,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isChosen) Color.White else SecondaryText
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        cats.drop(4).forEach { cat ->
                            val isChosen = selectedCharCat == cat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isChosen) ElectricBlue else SecondaryPanels)
                                    .clickable { viewModel.setSelectedCharacterCategory(cat) }
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 10.sp,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isChosen) Color.White else SecondaryText
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Camera & Aspect Ratio Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Camera Movement Button (opens 20 camera movements modal)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PanelCard)
                    .border(0.5.dp, PanelBorder, RoundedCornerShape(10.dp))
                    .clickable { viewModel.openCameraPicker() }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .testTag("camera_picker_button")
            ) {
                Column {
                    Text(text = "حركة الكاميرا السينمائية", fontSize = 10.sp, color = SecondaryText)
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                        Text(
                            text = currentSlot.cameraMovement.take(18) + "...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText,
                            maxLines = 1
                        )
                    }
                }
            }

            // Aspect Ratio Selector
            val ratios = listOf("16:9", "9:16", "1:1", "4:5", "21:9")
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PanelCard)
                    .border(0.5.dp, PanelBorder, RoundedCornerShape(10.dp))
                    .clickable {
                        val nextIndex = (ratios.indexOf(currentSlot.aspectRatio) + 1) % ratios.size
                        viewModel.updateCurrentSlotAspectRatio(ratios[nextIndex])
                    }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(text = "نسبة الأبعاد", fontSize = 10.sp, color = SecondaryText)
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AspectRatio, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                        Text(
                            text = currentSlot.aspectRatio,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                    }
                }
            }
        }


        // Progress Bar (if generating)
        if (currentSlot.isGenerating) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SecondaryPanels)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = currentSlot.statusMessage, fontSize = 11.sp, color = PrimaryText)
                }
                if (!currentSlot.isGenerating && currentSlot.hasResult && !currentSlot.finalPromptUsed.isNullOrEmpty()) {
                    Text(
                        text = "النص النهائي: ${currentSlot.finalPromptUsed}",
                        fontSize = 10.sp,
                        color = PrimaryText,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = ElectricBlue,
                    trackColor = DarkPanels
                )
            }
        }

        if (!currentSlot.isGenerating && currentSlot.statusMessage.startsWith("❌")) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SecondaryPanels)
                    .padding(12.dp)
            ) {
                Text(text = currentSlot.statusMessage, fontSize = 11.sp, color = PrimaryText)
            }
        }

        // Cloudflare Workers AI Daily Quota Badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SecondaryPanels)
                .border(0.5.dp, PanelBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "الحصة اليومية المجانية المتبقية: $remainingDailyQuota / 10000 Neurons (Cloudflare FLUX)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyanAccent
            )
        }

        var diagClicks by remember { mutableStateOf(0) }
        Text(
            text = "DIAG mode=${currentSlot.mode} gen=${currentSlot.isGenerating} clicks=$diagClicks",
            fontSize = 11.sp,
            color = CyanAccent
        )

        // Primary Action Button: "✨ إنشاء الآن" (matches screenshot)
        Button(
            onClick = {
                diagClicks++
                viewModel.startGeneration()
            },
            enabled = !currentSlot.isGenerating && (currentSlot.mode == "توليد الصور" || currentSlot.mode == "نص إلى فيديو"),
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricBlue,
                disabledContainerColor = ElectricBlue.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("generate_now_button")
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (currentSlot.isGenerating) "جارٍ المعالجة السينمائية..." else "✨ إنشاء الآن",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
