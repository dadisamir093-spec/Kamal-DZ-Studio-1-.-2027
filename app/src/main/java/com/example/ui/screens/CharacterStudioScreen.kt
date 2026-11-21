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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MotionPhotosOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.CropLandscape
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkPanels
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MagentaAccent
import com.example.ui.theme.PanelBorder
import com.example.ui.theme.PanelCard
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.SecondaryPanels
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SuccessGreen
import com.example.viewmodel.StudioViewModel

data class CharacterFeatureItem(
    val title: String,
    val icon: ImageVector,
    val description: String
)

val characterFeatureTools = listOf(
    CharacterFeatureItem("مراجعة الوجه والملامح", Icons.Default.Face, "فحص تطابق ملامح الوجه ونسب العيون والأنف"),
    CharacterFeatureItem("نقل الحركة", Icons.Default.MotionPhotosOn, "استنساخ حركة جسدية من فيديو مرجعي"),
    CharacterFeatureItem("عزل وإزالة الخلفية", Icons.Default.PhotoCamera, "استخراج الشخصية بدقة بدون تشويش في الحواف"),
    CharacterFeatureItem("تبديل الأشخاص", Icons.Default.SwapHoriz, "تبديل الممثلين في المشهد مع حفظ الإضاءة"),
    CharacterFeatureItem("توازن الوجه والتعبيرات", Icons.Default.Psychology, "توجيه مشاعر الشخصية (ابتسام، تركيز، غضب)"),
    CharacterFeatureItem("التدريب والمظهر", Icons.Default.Person, "حفظ بصمة الهوية البصرية لاستخدامها في كافة المشاهد"),
    CharacterFeatureItem("تغيير الملابس", Icons.Default.Style, "تعديل الزي السينمائي دون المساس بملامح الوجه"),
    CharacterFeatureItem("استمرارية الشخصية", Icons.Default.Lock, "قفل الهوية لمنع أي تغيير عشوائي أثناء التوليد")
)

data class CharacterCategoryItem(
    val name: String,
    val emoji: String,
    val ageGuide: String
)

val characterCategoriesList = listOf(
    CharacterCategoryItem("طفل", "👦", "8-11 سنة"),
    CharacterCategoryItem("طفلة", "👧", "7-10 سنوات"),
    CharacterCategoryItem("شاب", "🧑", "18-25 سنة"),
    CharacterCategoryItem("شابة", "👩", "18-24 سنة"),
    CharacterCategoryItem("امرأة", "👩‍💼", "28-48 سنة"),
    CharacterCategoryItem("رجل", "👨", "30-50 سنة"),
    CharacterCategoryItem("عجوز", "👵", "60-80 سنة"),
    CharacterCategoryItem("شيخ", "👴", "65-85 سنة")
)

val qualityOptions10 = listOf(
    "360p" to "SD خفيف",
    "480p" to "SD قياسي",
    "720p" to "HD عالي",
    "1080p" to "FHD كامل",
    "2K" to "QHD سينمائي",
    "4K" to "Ultra HD فائق",
    "8K" to "Cinema IMAX",
    "Cinematics RAW" to "خام غير مضغوط",
    "Anamorphic" to "عدسة سينمائية عريضة",
    "Custom 1K" to "أبعاد مخصصة"
)

data class AspectRatioOption(
    val ratio: String,
    val titleAr: String,
    val description: String
)

val characterAspectRatios10 = listOf(
    AspectRatioOption("16:9", "16:9 عريض سينمائي", "شاشات التلفزيون واليوتيوب (Landscape)"),
    AspectRatioOption("9:16", "9:16 عمودي ريلز", "منصات تيك توك، ريلز، وشورتس (Portrait)"),
    AspectRatioOption("1:1", "1:1 مربع متناسق", "منشورات إنستغرام والوسائط المربعة (Square)"),
    AspectRatioOption("4:5", "4:5 عمودي اجتماعي", "خلاصة إنستغرام وبوسترات السوشيال (Social)"),
    AspectRatioOption("21:9", "21:9 سينما سكوب فائقة", "شاشات العرض السينمائي (Ultrawide)"),
    AspectRatioOption("2.39:1", "2.39:1 عدسة أنامورفيك", "شاشة سينمائية عريضة هوليوودية (Anamorphic)"),
    AspectRatioOption("4:3", "4:3 أكاديمي كلاسيكي", "التلفزيون الكلاسيكي وأفلام الآيماكس (Academy)"),
    AspectRatioOption("3:2", "3:2 فوتوغرافي كلاسيكي", "معيار كاميرات 35mm الاحترافية (Still Photo)"),
    AspectRatioOption("2:3", "2:3 بوستر سينمائي", "الملصقات الدعائية والبوستر العمودي (Poster)"),
    AspectRatioOption("1.85:1", "1.85:1 سينما المسارح", "المعيار السينمائي العالمي للمسارح (Theatrical)")
)

@Composable
fun CharacterStudioScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val isLocked by viewModel.characterIdentityLocked.collectAsState()
    val activeFeature by viewModel.characterActiveFeature.collectAsState()
    val characterLanguage by viewModel.characterLanguage.collectAsState()
    val selectedCategory by viewModel.selectedCharacterCategory.collectAsState()
    val activeArchetype by viewModel.activeArchetype.collectAsState()
    val isSwapActive by viewModel.isSwapPersonsActive.collectAsState()
    val isCloning by viewModel.isCloningCharacter.collectAsState()
    val isCharacterCloned by viewModel.isCharacterCloned.collectAsState()
    val clonedFeedback by viewModel.characterClonedFeedback.collectAsState()
    val isBgRemovalActive by viewModel.isBackgroundRemovalActive.collectAsState()
    val isRemovingBg by viewModel.isRemovingBackground.collectAsState()
    val bgRemovalFeedback by viewModel.backgroundRemovalFeedback.collectAsState()
    val characterQuality by viewModel.characterQuality.collectAsState()

    // Lip Sync States from ViewModel
    val lipSyncVoice by viewModel.lipSyncVoiceTrack.collectAsState()
    val lipSyncOffset by viewModel.lipSyncOffsetMs.collectAsState()
    val isLipSyncPlaying by viewModel.isLipSyncPlaying.collectAsState()
    val lipSyncMouthOpen by viewModel.lipSyncMouthOpen.collectAsState()

    // Character Central Preview States from ViewModel
    val previewMediaUri by viewModel.characterPreviewMediaUri.collectAsState()
    val previewAspectRatio by viewModel.characterPreviewAspectRatio.collectAsState()
    val isMediaLoaded by viewModel.isCharacterMediaLoaded.collectAsState()
    val isVideoPlaying by viewModel.isCharacterVideoPlaying.collectAsState()
    val videoTimecode by viewModel.characterVideoTimecode.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.setCharacterPreviewMediaUri(uri.toString())
        }
    }

    var showQualityDropdown by remember { mutableStateOf(false) }
    var showLanguageDropdown by remember { mutableStateOf(false) }
    var showVoiceTrackDropdown by remember { mutableStateOf(false) }
    var showAspectRatioDropdown by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkPanels)
            .verticalScroll(scrollState)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Header
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "2. استوديو استنساخ الشخصيات والتحكم السينمائي",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Text(
                text = "اصنع شخصياتك تحكم في مظهرها وحركتها ومزامنة شفتيها داخل الفيديوهات",
                fontSize = 11.sp,
                color = SecondaryText
            )
        }

        // 2. Top Action Bar: Exactly equal buttons (weight 1f each) that turn SuccessGreen upon activation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // استنساخ الشخصية - Equal weight 1f, dark grey inactive (#2B3553 / SecondaryPanels), turns SuccessGreen when active
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isCharacterCloned) SuccessGreen else SecondaryPanels)
                    .border(0.5.dp, if (isCharacterCloned) SuccessGreen else PanelBorder, RoundedCornerShape(8.dp))
                    .clickable {
                        if (!isCloning) {
                            viewModel.cloneCharacter()
                        }
                    }
                    .padding(vertical = 9.dp)
                    .testTag("btn_clone_character"),
                contentAlignment = Alignment.Center
            ) {
                if (isCloning) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(13.dp), color = Color.White, strokeWidth = 2.dp)
                        Text(text = "جارٍ الاستنساخ...", fontSize = 10.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isCharacterCloned) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isCharacterCloned) "تم الاستنساخ ✓" else "+ استنساخ الشخصية",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // تبديل الأشخاص - Equal weight 1f, dark grey inactive (#2B3553 / SecondaryPanels), turns SuccessGreen when active
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSwapActive) SuccessGreen else SecondaryPanels)
                    .border(0.5.dp, if (isSwapActive) SuccessGreen else PanelBorder, RoundedCornerShape(8.dp))
                    .clickable {
                        viewModel.swapCharacter()
                    }
                    .padding(vertical = 9.dp)
                    .testTag("btn_swap_persons"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isSwapActive) Icons.Default.Check else Icons.Default.SwapHoriz,
                        contentDescription = "تبديل",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isSwapActive) "تم التبديل ✓" else "تبديل الأشخاص",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // عزل وإزالة الخلفية - Equal weight 1f, turns SuccessGreen when active
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isBgRemovalActive) SuccessGreen else SecondaryPanels)
                    .border(0.5.dp, if (isBgRemovalActive) SuccessGreen else PanelBorder, RoundedCornerShape(8.dp))
                    .clickable {
                        if (!isRemovingBg) {
                            viewModel.toggleBackgroundRemoval()
                        }
                    }
                    .padding(vertical = 9.dp)
                    .testTag("btn_remove_background"),
                contentAlignment = Alignment.Center
            ) {
                if (isRemovingBg) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), color = CyanAccent, strokeWidth = 2.dp)
                        Text(text = "معالجة العزل...", fontSize = 10.sp, color = CyanAccent)
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "عزل",
                            tint = if (isBgRemovalActive) Color.White else PrimaryText,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (isBgRemovalActive) "الخلفية معزولة ✓" else "عزل وإزالة الخلفية",
                            fontSize = 10.5.sp,
                            fontWeight = if (isBgRemovalActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isBgRemovalActive) Color.White else PrimaryText
                        )
                    }
                }
            }
        }

        // Action Feedback Notification (Cloning or background removal status)
        val feedback = clonedFeedback ?: bgRemovalFeedback
        if (feedback != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SecondaryPanels)
                    .border(0.5.dp, CyanAccent, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                    Text(text = feedback, fontSize = 10.5.sp, color = PrimaryText)
                }
            }
        }

        // =========================================================================
        // 🎬 شاشة المعاينة المركزية للشخصية (Character Preview Monitor)
        // تعرض النتائج الفعلية للاستيراد، الاستنساخ، عزل الخلفية، تبديل الممثلين، وحماية الملامح، ومزامنة الشفاه
        // =========================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PanelCard)
                .border(1.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(10.dp)
                .testTag("character_preview_screen")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Monitor Header Bar: Title, Live indicator, Aspect Ratio toggles, and Clear/Reset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CenterFocusStrong,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = "شاشة المعاينة المركزية",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isMediaLoaded) SuccessGreen.copy(alpha = 0.2f) else SecondaryPanels)
                                .border(0.5.dp, if (isMediaLoaded) SuccessGreen else PanelBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isMediaLoaded) SuccessGreen else Color.Gray)
                                )
                                Text(
                                    text = if (isMediaLoaded) "معاينة حية" else "شاشة فارغة",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMediaLoaded) SuccessGreen else SecondaryText
                                )
                            }
                        }
                    }

                    // Media Control Buttons (Import & Reset/Clear)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {

                        // Import Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(ElectricBlue)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                    )
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Icon(imageVector = Icons.Default.Upload, contentDescription = "استيراد", tint = Color.White, modifier = Modifier.size(11.dp))
                                Text(text = "استيراد", fontSize = 8.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Clear or Reset Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SecondaryPanels)
                                .clickable {
                                    if (isMediaLoaded) {
                                        viewModel.clearCharacterMedia()
                                    } else {
                                        viewModel.resetDefaultCharacterMedia()
                                    }
                                }
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isMediaLoaded) "إفراغ" else "استعادة",
                                fontSize = 8.5.sp,
                                color = if (isMediaLoaded) SecondaryText else CyanAccent
                            )
                        }
                    }
                }

                // Monitor Screen Content
                if (!isMediaLoaded) {
                    // مساحة فارغة منظمة عند عدم استيراد أي ملف (Empty State)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkPanels)
                            .border(0.8.dp, PanelBorder, RoundedCornerShape(8.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = SecondaryText,
                                modifier = Modifier.size(34.dp)
                            )
                            Text(
                                text = "لا يوجد ملف مستورد حالياً للمعاينة",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                            Text(
                                text = "اضغط على زر الاستيراد لاختيار صورة شخصية أو فيديو للمعاينة وتطبيق الاستنساخ وعزل الخلفية",
                                fontSize = 9.5.sp,
                                color = SecondaryText,
                                textAlign = TextAlign.Center
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Upload, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "استيراد صورة أو فيديو", fontSize = 10.sp, color = Color.White)
                                }
                                Button(
                                    onClick = { viewModel.resetDefaultCharacterMedia() },
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryPanels),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text(text = "استعادة الشخصية المرجعية", fontSize = 10.sp, color = CyanAccent)
                                }
                            }
                        }
                    }
                } else {
                    // شاشة العرض الحية المجهزة بجميع المؤشرات والأبعاد (Display Monitor Frame)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                when (previewAspectRatio) {
                                    "9:16" -> Modifier.height(240.dp)
                                    "2:3" -> Modifier.height(230.dp)
                                    "4:5" -> Modifier.height(220.dp)
                                    "1:1" -> Modifier.aspectRatio(1.1f)
                                    "4:3" -> Modifier.aspectRatio(4f / 3f)
                                    "3:2" -> Modifier.aspectRatio(3f / 2f)
                                    "1.85:1" -> Modifier.aspectRatio(1.85f)
                                    "21:9" -> Modifier.aspectRatio(21f / 9f)
                                    "2.39:1" -> Modifier.aspectRatio(2.39f)
                                    else -> Modifier.aspectRatio(16f / 9.5f)
                                }
                            )
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isBgRemovalActive) Color(0xFF0F172A) else Color(0xFF060913))
                            .border(0.8.dp, PanelBorder, RoundedCornerShape(8.dp))
                    ) {
                        // 1. الصورة أو الفيديو الفعلي (مع Fit لمنع تمدد الصورة وتشويه الأبعاد)
                        if (previewMediaUri != null) {
                            AsyncImage(
                                model = previewMediaUri,
                                contentDescription = "معاينة الشخصية المستوردة",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Image(
                                painter = painterResource(id = activeArchetype.drawableRes),
                                contentDescription = "معاينة الممثل: ${activeArchetype.name}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }

                        // 2. تراكب عزل الخلفية (Alpha Matte Indicator)
                        if (isBgRemovalActive) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xD9064E3B))
                                    .border(0.5.dp, Color(0xFF34D399), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(10.dp))
                                    Text(text = "الخلفية معزولة (Alpha Matte) ✓", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                                }
                            }
                        }

                        // 3. تراكب الممثل الحالي / بعد تبديل الأشخاص (Swap Persons live overlay)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xCC0D1427))
                                .border(0.5.dp, CyanAccent, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(11.dp))
                                Text(
                                    text = "${activeArchetype.name} • ${activeArchetype.role}",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText
                                )
                            }
                        }

                        // 4. تراكب نتائج الاستنساخ والبصمة الجينية (After Cloning status)
                        if (isCloning) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0x73000000)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = CyanAccent, strokeWidth = 2.5.dp)
                                    Text(text = "جارٍ مسح وتثبيت البصمة الجينية للملامح...", fontSize = 10.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else if (isCharacterCloned) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SuccessGreen.copy(alpha = 0.85f))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                                    Text(text = "البصمة مستنسخة ومثبتة ✓ (99.8%)", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        // 5. تراكب حماية ملامح الوجه والهوية (Protected Identity Shield)
                        if (isLocked) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xCC0D1427))
                                    .border(0.5.dp, PurpleAccent, RoundedCornerShape(4.dp))
                                    .clickable { viewModel.toggleIdentityLock() }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(10.dp))
                                    Text(text = "الملامح محمية 🔒", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        // 6. معاينة مزامنة الشفاه السينمائية الحية (Lip-Sync Real-time Preview)
                        if (isLipSyncPlaying) {
                            // Mouth Phoneme Circle Indicator over the center of the character
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x99000000))
                                    .border(1.dp, MagentaAccent, CircleShape)
                                    .align(Alignment.Center),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(18.dp)
                                        .height((14 * lipSyncMouthOpen).dp.coerceAtLeast(3.dp))
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MagentaAccent)
                                )
                            }

                            // Bottom Subtitle/Lip-Sync speech wave banner
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(Color(0xD90D1427))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(12.dp))
                                        Text(text = "مزامنة الشفاه نشطة: $lipSyncVoice", fontSize = 8.5.sp, color = CyanAccent, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                    Text(text = "${lipSyncOffset.toInt()} ms", fontSize = 8.5.sp, color = SecondaryText)
                                }
                            }
                        }

                        // 7. زر تشغيل وإيقاف الفيديو ومؤشر التوقيت (Video Playback & Timecode controls)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xCC000000))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                // Timecode
                                Text(
                                    text = videoTimecode,
                                    fontSize = 8.5.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )

                                // Play / Pause Button
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(CyanAccent)
                                        .clickable {
                                            viewModel.toggleCharacterVideoPlayback()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isVideoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "تشغيل أو إيقاف",
                                        tint = DarkPanels,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2.5. قائمة أبعاد الفيديو المنسدلة (مستطيل صغير وأنيق ومنظم مشابه لقائمة اللغات)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SecondaryPanels)
                .border(0.5.dp, PanelBorder, RoundedCornerShape(8.dp))
                .clickable { showAspectRatioDropdown = true }
                .padding(horizontal = 10.dp, vertical = 7.dp)
                .testTag("btn_aspect_ratio_dropdown")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AspectRatio,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "نسبة أبعاد الفيديو:",
                        fontSize = 10.sp,
                        color = SecondaryText
                    )
                    Text(
                        text = "$previewAspectRatio (${characterAspectRatios10.firstOrNull { it.ratio == previewAspectRatio }?.titleAr ?: previewAspectRatio})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(PanelCard)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "تغيير (10 أبعاد)",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            DropdownMenu(
                expanded = showAspectRatioDropdown,
                onDismissRequest = { showAspectRatioDropdown = false },
                modifier = Modifier
                    .background(PanelCard)
                    .border(1.dp, PanelBorder, RoundedCornerShape(8.dp))
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "📐 نسب أبعاد المشهد (10 خيارات):",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryText
                        )
                    },
                    onClick = { },
                    enabled = false
                )

                characterAspectRatios10.forEach { opt ->
                    val isSelected = previewAspectRatio == opt.ratio
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = opt.ratio,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) CyanAccent else PrimaryText
                                        )
                                        Text(
                                            text = "• ${opt.titleAr}",
                                            fontSize = 10.sp,
                                            color = if (isSelected) CyanAccent else PrimaryText
                                        )
                                    }
                                    Text(
                                        text = opt.description,
                                        fontSize = 8.5.sp,
                                        color = SecondaryText
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = CyanAccent,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            viewModel.setCharacterPreviewAspectRatio(opt.ratio)
                            showAspectRatioDropdown = false
                        }
                    )
                }
            }
        }

        // 3. مساحة جودة استنساخ الشخصيات (بطاقة صغيرة وأنيقة ومنظمة دون إهدار مساحة)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(PanelCard)
                .border(0.5.dp, PanelBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                    Text(text = "جودة الاستنساخ:", fontSize = 11.sp, color = SecondaryText)
                    Text(text = characterQuality, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                }

                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SecondaryPanels)
                            .border(0.5.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .clickable { showQualityDropdown = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "تغيير (10 خيارات)", fontSize = 10.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                        }
                    }

                    DropdownMenu(
                        expanded = showQualityDropdown,
                        onDismissRequest = { showQualityDropdown = false },
                        modifier = Modifier.background(SecondaryPanels).border(0.5.dp, PanelBorder, RoundedCornerShape(8.dp))
                    ) {
                        qualityOptions10.forEach { (q, desc) ->
                            DropdownMenuItem(
                                text = {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = q, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (q == characterQuality) CyanAccent else PrimaryText)
                                        Text(text = " • $desc", fontSize = 9.sp, color = SecondaryText)
                                    }
                                },
                                onClick = {
                                    viewModel.setCharacterQuality(q)
                                    showQualityDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // 4. مساحة إضافة الصور والفيديو والملامح المحمية (منظمة ومتناسقة وموزونة)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(PanelCard)
                .border(0.5.dp, PanelBorder, RoundedCornerShape(10.dp))
                .padding(9.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Header inside the card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = activeArchetype.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                        Text(text = "(${activeArchetype.role} • ${activeArchetype.age})", fontSize = 10.sp, color = CyanAccent)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SecondaryPanels)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(11.dp))
                        Text(text = "الملامح المحمية", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                    }
                }

                // Content Row: Portrait + Upload Box + Traits
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Character Portrait Image
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isBgRemovalActive) Color(0xFF1E293B) else SecondaryPanels)
                            .border(1.dp, if (isBgRemovalActive) SuccessGreen else CyanAccent, RoundedCornerShape(8.dp))
                    ) {
                        if (previewMediaUri != null) {
                            AsyncImage(
                                model = previewMediaUri,
                                contentDescription = "صورة المرجع المستورد",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = activeArchetype.drawableRes),
                                contentDescription = "صورة المرجع: ${activeArchetype.name}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        if (isBgRemovalActive) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(Color(0xD9064E3B))
                                    .padding(vertical = 1.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "معزول (Alpha)", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                            }
                        }
                    }

                    // Add Reference Photo/Video Box (Connected directly to system Photo Picker)
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SecondaryPanels)
                            .border(0.5.dp, PanelBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(imageVector = Icons.Default.Upload, contentDescription = "رفع", tint = CyanAccent, modifier = Modifier.size(17.dp))
                            Text(text = "إضافة صورة/فيديو", fontSize = 8.sp, color = PrimaryText, textAlign = TextAlign.Center)
                            Text(text = "JPG / MP4", fontSize = 7.sp, color = SecondaryText)
                        }
                    }

                    // Protected Traits details list
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        activeArchetype.traits.take(3).forEach { trait ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(11.dp))
                                Text(text = trait, fontSize = 9.sp, color = PrimaryText, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        // 5. مساحة مزامنة الشفاه السينمائية المدمجة (Lip-Sync Engine Compact - مقلصة ومناسبة لشاشة الهاتف)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(PanelCard)
                .border(0.8.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Header Row: Title + Compact Preview Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(15.dp))
                        Text(text = "مزامنة الشفاه السينمائية (Lip-Sync)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                    }

                    // Compact Preview Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isLipSyncPlaying) PurpleAccent else ElectricBlue)
                            .clickable { viewModel.toggleLipSyncPreview() }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(
                                imageVector = if (isLipSyncPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (isLipSyncPlaying) "إيقاف" else "معاينة",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Middle Row: Mouth Indicator + Audio Track Dropdown
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkPanels)
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mouth Movement Indicator Circle
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(SecondaryPanels)
                            .border(0.8.dp, CyanAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height((12 * lipSyncMouthOpen).dp.coerceAtLeast(3.dp))
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isLipSyncPlaying) MagentaAccent else CyanAccent)
                        )
                    }

                    // Voice Track Selector Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(5.dp))
                                .background(SecondaryPanels)
                                .clickable { showVoiceTrackDropdown = true }
                                .padding(horizontal = 7.dp, vertical = 4.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = lipSyncVoice, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CyanAccent, maxLines = 1)
                                Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(13.dp))
                            }

                            DropdownMenu(
                                expanded = showVoiceTrackDropdown,
                                onDismissRequest = { showVoiceTrackDropdown = false },
                                modifier = Modifier.background(SecondaryPanels).border(0.5.dp, PanelBorder, RoundedCornerShape(8.dp))
                            ) {
                                listOf(
                                    "صوت سينمائي ملحمي (شاب 22 سنة - فصيح)",
                                    "صوت وثائقي عميق (رجل 45 سنة - عاصمي)",
                                    "صوت هادئ ومؤثر (شابة 24 سنة - قبائلي)",
                                    "صوت شيخ حكيم (شيخ 70 سنة - صحراوي)"
                                ).forEach { trk ->
                                    DropdownMenuItem(
                                        text = { Text(text = trk, fontSize = 10.sp, color = PrimaryText) },
                                        onClick = {
                                            viewModel.setLipSyncVoiceTrack(trk)
                                            showVoiceTrackDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Compact Offset Slider Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "إزاحة التوقيت:", fontSize = 9.sp, color = SecondaryText)
                    Slider(
                        value = lipSyncOffset,
                        onValueChange = { viewModel.setLipSyncOffsetMs(it) },
                        valueRange = -200f..200f,
                        modifier = Modifier.weight(1f).height(18.dp),
                        colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                    )
                    Text(text = "${lipSyncOffset.toInt()} ms", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                }
            }
        }

        // 6. مساحة الترجمة واللغات المصغرة (لوحة صغيرة منسدلة دون استهلاك مساحة)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SecondaryPanels)
                .border(0.5.dp, PanelBorder, RoundedCornerShape(8.dp))
                .clickable { viewModel.openLanguagePicker("character", "اختر لغة ولهجة الشخصية") }
                .padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                    Text(text = "لهجة الشخصية:", fontSize = 10.sp, color = SecondaryText)
                    Text(text = "${characterLanguage.flagEmoji} ${characterLanguage.nameAr}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(PanelCard)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "تغيير", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                }
            }
        }

        // 7. 8 Character Categories Grid (طفل، طفلة، شاب، شابة، امرأة، رجل، عجوز، شيخ)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "فئات الشخصيات (8 فئات):", fontSize = 11.sp, color = SecondaryText)
                Text(text = "المحددة: $selectedCategory", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
            }

            val chunkedCats = characterCategoriesList.chunked(4)
            chunkedCats.forEach { rowCats ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    rowCats.forEach { cat ->
                        val isChosen = selectedCategory == cat.name
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isChosen) ElectricBlue else PanelCard)
                                .border(
                                    width = if (isChosen) 1.dp else 0.5.dp,
                                    color = if (isChosen) CyanAccent else PanelBorder,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    viewModel.setSelectedCharacterCategory(cat.name)
                                }
                                .padding(vertical = 6.dp, horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = cat.emoji, fontSize = 14.sp)
                                Text(
                                    text = cat.name,
                                    fontSize = 10.sp,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isChosen) Color.White else PrimaryText
                                )
                            }
                        }
                    }
                }
            }
        }

        // 8. أدوات التحكم بالهوية والحركة السينمائية
        Text(text = "أدوات التحكم بالهوية والحركة السينمائية", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
        val chunkedTools = characterFeatureTools.chunked(4)
        chunkedTools.forEach { rowTools ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowTools.forEach { tool ->
                    val isCurrent = activeFeature == tool.title
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCurrent) SecondaryPanels else PanelCard)
                            .border(
                                width = if (isCurrent) 1.5.dp else 0.5.dp,
                                color = if (isCurrent) CyanAccent else PanelBorder,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.setCharacterActiveFeature(tool.title) }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(imageVector = tool.icon, contentDescription = tool.title, tint = if (isCurrent) CyanAccent else PurpleAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(text = tool.title, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = PrimaryText, textAlign = TextAlign.Center, lineHeight = 10.sp, maxLines = 2)
                        }
                    }
                }
            }
        }
    }
}
