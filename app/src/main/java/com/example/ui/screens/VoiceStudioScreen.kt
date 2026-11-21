package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.WaveformVisualizer
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
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.ErrorRed
import com.example.viewmodel.StudioViewModel

data class VoiceCategoryData(
    val name: String,
    val emoji: String,
    val ageGuide: String
)

val voiceCategoriesList = listOf(
    VoiceCategoryData("طفل", "👦", "8-11 سنة"),
    VoiceCategoryData("طفلة", "👧", "7-10 سنوات"),
    VoiceCategoryData("شاب", "🧑", "18-25 سنة"),
    VoiceCategoryData("شابة", "👩", "18-24 سنة"),
    VoiceCategoryData("امرأة", "👩‍💼", "28-48 سنة"),
    VoiceCategoryData("رجل", "👨", "30-50 سنة"),
    VoiceCategoryData("عجوز", "👵", "60-80 سنة"),
    VoiceCategoryData("شيخ", "👴", "65-85 سنة")
)

val voiceTonesList = listOf(
    "صوت طبيعي" to "طبيعي وواقعي",
    "صوت هادئ وناعم" to "همس ناعم وسكينة",
    "صوت عميق وغليظ" to "قرار جهوري قوي",
    "صوت حاد ومرتفع" to "نبرة عالية واضحة",
    "صوت قوي وقيادي" to "حضور ملحمي صارم",
    "صوت حزين ومؤثر" to "شجن وعاطفة دافئة",
    "صوت سعيد ومتحمس" to "طاقة وحيوية عالية",
    "صوت غاضب" to "انفعال سينمائي حاد",
    "صوت خائف ومرتعش" to "تردد ورعب درامي",
    "صوت سينمائي وملحمي" to "أفلام وتريلرات عالمية",
    "صوت همس" to "غموض وإثارة قريبة",
    "صوت وثائقي احترافي" to "سرد ناشيونال جيوغرافيك"
)

@Composable
fun VoiceStudioScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val voiceMode by viewModel.voiceMode.collectAsState()
    val selectedCategory by viewModel.selectedVoiceCategory.collectAsState()
    val selectedTone by viewModel.selectedVoiceTone.collectAsState()
    val vocalAge by viewModel.vocalAge.collectAsState()
    val voiceRoughness by viewModel.voiceRoughness.collectAsState()
    val voicePitch by viewModel.voicePitch.collectAsState()
    val speechSpeed by viewModel.speechSpeed.collectAsState()
    val expressiveness by viewModel.expressiveness.collectAsState()
    val pauseAndBreathing by viewModel.pauseAndBreathing.collectAsState()
    val dialect by viewModel.voiceDialect.collectAsState()
    val voiceLanguage by viewModel.voiceLanguage.collectAsState()
    val script by viewModel.voiceScript.collectAsState()
    val isPlaying by viewModel.isVoicePlaying.collectAsState()
    val favorites by viewModel.favoriteVoices.collectAsState()

    var statusAlert by remember { mutableStateOf<String?>(null) }
    var showAdvancedControls by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkPanels)
            .verticalScroll(scrollState)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "3. استوديو الصوت والتعليق ومزامنة الشفاه",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Text(
                text = "نظام متطور لتوليد وهندسة الأصوات البشرية، النبرات العاطفية، واللهجات السينمائية",
                fontSize = 11.sp,
                color = SecondaryText
            )
        }

        // Sub Modes: "تعليق صوتي", "استنساخ الصوت", "مزامنة الشفاه"
        val modes = listOf("تعليق صوتي", "استنساخ الصوت", "مزامنة الشفاه")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            modes.forEach { mode ->
                val isSelected = voiceMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) ElectricBlue else SecondaryPanels)
                        .border(0.5.dp, if (isSelected) CyanAccent else PanelBorder, RoundedCornerShape(8.dp))
                        .clickable { viewModel.setVoiceMode(mode) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else SecondaryText
                    )
                }
            }
        }

        // 2. Waveform Visualizer & Live Monitor Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PanelCard)
                .border(1.dp, PanelBorder, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Combined Hybrid Voice Persona Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(ElectricBlue.copy(alpha = 0.25f), PurpleAccent.copy(alpha = 0.25f))
                            )
                        )
                        .border(0.5.dp, CyanAccent.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "الصوت النشط: $selectedCategory • $selectedTone ($vocalAge سنة)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                }

                // Interactive Mic / Playback Button
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) ElectricBlue else SecondaryPanels)
                        .border(1.5.dp, if (isPlaying) Color.White else CyanAccent, CircleShape)
                        .clickable { viewModel.toggleVoicePlayback() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.Mic,
                        contentDescription = "استماع مباشر",
                        tint = if (isPlaying) Color.White else CyanAccent,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Animated Waveform Bars
                WaveformVisualizer(
                    isPlaying = isPlaying,
                    height = 46.dp,
                    barCount = 36
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isPlaying) "جارٍ نطق التعليق الصوتي..." else "00:00 / 02:30",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPlaying) SuccessGreen else PrimaryText
                    )
                    Text(
                        text = "طبقة: ${"%.1f".format(voicePitch)}x | سرعة: ${"%.1f".format(speechSpeed)}x",
                        fontSize = 10.sp,
                        color = SecondaryText
                    )
                }
            }
        }

        // Action Buttons Row: [ 🔊 تجربة الصوت ] + [ ⭐ حفظ للمفضلة ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.previewVoicePlayback() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPlaying) ErrorRed else ElectricBlue
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1.2f)
                    .height(44.dp)
                    .testTag("preview_voice_button")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isPlaying) "إيقاف الاستماع" else "🔊 تجربة الصوت الآن",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            OutlinedButton(
                onClick = {
                    viewModel.saveCurrentVoiceToFavorites()
                    statusAlert = "تم حفظ توليفة «$selectedCategory • $selectedTone» في الأصوات المفضلة!"
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningAmber),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("save_favorite_voice_button")
            ) {
                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("⭐ حفظ للمفضلة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 3. فئات الأصوات البشرية (8 فئات)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "1. فئات الأصوات البشرية (8 فئات)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
                Text(
                    text = selectedCategory,
                    fontSize = 11.sp,
                    color = CyanAccent,
                    fontWeight = FontWeight.Bold
                )
            }

            // 2-row grid / chip list of 8 categories
            val chunkedCategories = voiceCategoriesList.chunked(4)
            chunkedCategories.forEach { rowCategories ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowCategories.forEach { cat ->
                        val isChosen = selectedCategory == cat.name
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isChosen) ElectricBlue else PanelCard)
                                .border(
                                    0.5.dp,
                                    if (isChosen) CyanAccent else PanelBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    viewModel.setVoiceCategory(cat.name)
                                    statusAlert = "تم اختيار فئة: ${cat.name} (${cat.ageGuide})"
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = cat.emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = cat.name,
                                    fontSize = 10.sp,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isChosen) Color.White else PrimaryText,
                                    maxLines = 1
                                )
                                Text(
                                    text = cat.ageGuide,
                                    fontSize = 8.sp,
                                    color = if (isChosen) Color.White.copy(alpha = 0.8f) else SecondaryText
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. تغيير النبرات والأداء الصوتي (12 نبرة)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "2. النبرات والأداء الصوتي (12 نبرة)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
                Text(
                    text = selectedTone,
                    fontSize = 11.sp,
                    color = MagentaAccent,
                    fontWeight = FontWeight.Bold
                )
            }

            // Grid of 12 tones (3 rows of 4)
            val chunkedTones = voiceTonesList.chunked(3)
            chunkedTones.forEach { rowTones ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowTones.forEach { (toneName, toneDesc) ->
                        val isChosen = selectedTone == toneName
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isChosen) PurpleAccent else PanelCard)
                                .border(
                                    0.5.dp,
                                    if (isChosen) CyanAccent else PanelBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    viewModel.setVoiceTone(toneName)
                                    statusAlert = "تم ضبط نبرة الصوت: $toneName ($toneDesc)"
                                }
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = toneName,
                                    fontSize = 10.sp,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isChosen) Color.White else PrimaryText,
                                    maxLines = 1
                                )
                                Text(
                                    text = toneDesc,
                                    fontSize = 7.5.sp,
                                    color = if (isChosen) Color.White.copy(alpha = 0.8f) else SecondaryText,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. إعدادات التحكم المتقدمة (Sliders)
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                        Text(text = "إعدادات التحكم الدقيق في الصوت", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                    }

                    Text(
                        text = if (showAdvancedControls) "إخفاء" else "إظهار",
                        fontSize = 11.sp,
                        color = CyanAccent,
                        modifier = Modifier.clickable { showAdvancedControls = !showAdvancedControls }
                    )
                }

                AnimatedVisibility(visible = showAdvancedControls) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // العمر الصوتي (5 إلى 90 سنة)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "العمر الصوتي", fontSize = 11.sp, color = SecondaryText)
                            Text(text = "$vocalAge سنة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        }
                        Slider(
                            value = vocalAge.toFloat(),
                            onValueChange = { viewModel.setVocalAge(it.toInt()) },
                            valueRange = 5f..90f,
                            modifier = Modifier.height(20.dp),
                            colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                        )

                        // درجة خشونة ونعومة الصوت
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "درجة خشونة / نعومة الصوت", fontSize = 11.sp, color = SecondaryText)
                            Text(text = if (voiceRoughness > 0.5f) "خشن وجش (${(voiceRoughness * 100).toInt()}%)" else "ناعم وسلس (${(voiceRoughness * 100).toInt()}%)", fontSize = 10.sp, color = PurpleAccent)
                        }
                        Slider(
                            value = voiceRoughness,
                            onValueChange = { viewModel.setVoiceRoughness(it) },
                            valueRange = 0f..1f,
                            modifier = Modifier.height(20.dp),
                            colors = SliderDefaults.colors(thumbColor = PurpleAccent, activeTrackColor = PurpleAccent)
                        )

                        // طبقة الصوت وارتفاعها (Pitch)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "طبقة الصوت (Pitch)", fontSize = 11.sp, color = SecondaryText)
                            Text(text = "${"%.2f".format(voicePitch)}x", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
                        }
                        Slider(
                            value = voicePitch,
                            onValueChange = { viewModel.setVoicePitch(it) },
                            valueRange = 0.5f..2.0f,
                            modifier = Modifier.height(20.dp),
                            colors = SliderDefaults.colors(thumbColor = ElectricBlue, activeTrackColor = ElectricBlue)
                        )

                        // سرعة الكلام
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "سرعة الكلام (Speech Rate)", fontSize = 11.sp, color = SecondaryText)
                            Text(text = "${"%.2f".format(speechSpeed)}x", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }
                        Slider(
                            value = speechSpeed,
                            onValueChange = { viewModel.setSpeechSpeed(it) },
                            valueRange = 0.5f..2.0f,
                            modifier = Modifier.height(20.dp),
                            colors = SliderDefaults.colors(thumbColor = SuccessGreen, activeTrackColor = SuccessGreen)
                        )

                        // قوة التعبير والانفعال
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "قوة التعبير والانفعال الدرامي", fontSize = 11.sp, color = SecondaryText)
                            Text(text = "${(expressiveness * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MagentaAccent)
                        }
                        Slider(
                            value = expressiveness,
                            onValueChange = { viewModel.setExpressiveness(it) },
                            valueRange = 0f..1f,
                            modifier = Modifier.height(20.dp),
                            colors = SliderDefaults.colors(thumbColor = MagentaAccent, activeTrackColor = MagentaAccent)
                        )

                        // الوقفات والتنفس
                        Text(text = "التحكم في الوقفات والتنفس", fontSize = 11.sp, color = SecondaryText)
                        val pauseStyles = listOf("طبيعي متوازن", "وقفات درامية سينمائية", "سريع متواصل")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            pauseStyles.forEach { style ->
                                val isSelected = pauseAndBreathing == style
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) SecondaryPanels else DarkPanels)
                                        .border(0.5.dp, if (isSelected) CyanAccent else PanelBorder, RoundedCornerShape(6.dp))
                                        .clickable { viewModel.setPauseAndBreathing(style) }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = style,
                                        fontSize = 9.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) CyanAccent else SecondaryText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. اللهجة واللغة السينمائية (المدمجة والأنيقة)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SecondaryPanels)
                .border(0.5.dp, PanelBorder, RoundedCornerShape(8.dp))
                .clickable {
                    viewModel.openLanguagePicker("voice", "اختر لغة ولهجة التعليق الصوتي")
                }
                .padding(horizontal = 10.dp, vertical = 7.dp)
                .testTag("voice_dialect_compact_bar")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = voiceLanguage.flagEmoji, fontSize = 18.sp)
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "اللهجة الصوتية:",
                                fontSize = 10.sp,
                                color = SecondaryText
                            )
                            Text(
                                text = voiceLanguage.nameAr,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                            if (voiceLanguage.isPriority) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CyanAccent)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "أولوية",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkPanels
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${voiceLanguage.nameNative} • ${voiceLanguage.regionDescription}",
                            fontSize = 8.5.sp,
                            color = SecondaryText,
                            maxLines = 1
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PanelCard)
                        .border(0.5.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "تغيير",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                }
            }
        }

        // 7. الأصوات المفضلة المحفوظة
        if (favorites.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "الأصوات المفضلة المحفوظة (اضغط للتطبيق الفوري)", fontSize = 11.sp, color = SecondaryText)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(favorites) { (name, desc) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SecondaryPanels)
                                .border(0.5.dp, WarningAmber.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable {
                                    val parts = desc.split("•").map { it.trim() }
                                    if (parts.size >= 2) {
                                        val cat = parts[0]
                                        val tone = parts[1].substringBefore("(").trim()
                                        viewModel.applyFavoriteVoice(cat, tone)
                                        statusAlert = "تم تطبيق الصوت المفضل: $name"
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(12.dp))
                                Text(text = name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                            }
                        }
                    }
                }
            }
        }

        // 8. Script Input & Quick Prompts
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "نص التعليق الصوتي والحوار", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryText)

                // Quick sample phrase button
                Text(
                    text = "مثال درامي",
                    fontSize = 10.sp,
                    color = CyanAccent,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        viewModel.setVoiceScript("يا ليل الصحراء الطويل، هبت رياح التاسيلي لتحكي عن فرسان كتبوا المجد بالصبر والكرامة...")
                    }
                )
            }

            OutlinedTextField(
                value = script,
                onValueChange = { viewModel.setVoiceScript(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("voice_script_input"),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = PanelBorder,
                    focusedTextColor = PrimaryText,
                    unfocusedTextColor = PrimaryText,
                    focusedContainerColor = PanelCard,
                    unfocusedContainerColor = PanelCard
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, lineHeight = 18.sp, color = PrimaryText)
            )
        }

        // Status alert box
        if (statusAlert != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SecondaryPanels)
                    .padding(10.dp)
            ) {
                Text(text = statusAlert!!, fontSize = 11.sp, color = CyanAccent)
            }
        }

        // 9. Primary Action Button: "🎙️ إنشاء التعليق الصوتي"
        Button(
            onClick = {
                viewModel.generateVoiceTrack()
                statusAlert = "تم توليد المسار الصوتي بنجاح وحفظه في مكتبة الوسائط وخط المونتاج!"
            },
            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("generate_voice_button")
        ) {
            Icon(
                imageVector = Icons.Default.RecordVoiceOver,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "🎙️ إنشاء التعليق الصوتي ($selectedCategory • $selectedTone)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
