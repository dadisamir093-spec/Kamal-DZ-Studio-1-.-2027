package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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
import com.example.viewmodel.StudioViewModel

@Composable
fun MusicStudioScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val musicTab by viewModel.musicTab.collectAsState()
    val selectedGenre by viewModel.selectedGenre.collectAsState()
    val selectedInstruments by viewModel.selectedInstruments.collectAsState()
    val lyrics by viewModel.musicLyrics.collectAsState()
    val musicLanguage by viewModel.musicLanguage.collectAsState()
    val isPlaying by viewModel.isMusicPlaying.collectAsState()
    val selectedCharCat by viewModel.selectedCharacterCategory.collectAsState()

    var statusMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkPanels)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Header
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "4. استوديو الموسيقى",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Text(
                text = "موسيقى احترافية ومؤثرات صوتية وأغاني مستوحاة من التراث الجزائري والعالمي",
                fontSize = 12.sp,
                color = SecondaryText
            )
        }

        // Studio Desk Banner Image (matches screenshot)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 8.5f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, PanelBorder, RoundedCornerShape(16.dp))
                .background(SecondaryPanels)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_music_banner),
                contentDescription = "استوديو الموسيقى والآلات",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Dark subtle gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x55070B18))
            )
        }

        // Tabs: "المؤثرات الصوتية", "الآلات الموسيقية", "تأليف الموسيقى"
        val tabs = listOf("تأليف الموسيقى", "الآلات الموسيقية", "المؤثرات الصوتية")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEach { tab ->
                val isSelected = musicTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) ElectricBlue else SecondaryPanels)
                        .border(0.5.dp, if (isSelected) CyanAccent else PanelBorder, RoundedCornerShape(8.dp))
                        .clickable { viewModel.setMusicTab(tab) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else SecondaryText
                    )
                }
            }
        }

        // Genre Selector Chips (matches screenshot: راي سينمائي, شعبي, سطايفي, راب, سينمائي, هادئ)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = "اختر النوع الموسيقي", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
            val genres = listOf("راي سينمائي", "شعبي عاصمي", "سطايفي", "راب سينمائي", "ملحمي أوركسترالي", "هادئ")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                genres.take(3).forEach { g ->
                    val isChosen = selectedGenre == g
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isChosen) PurpleAccent else PanelCard)
                            .border(0.5.dp, if (isChosen) CyanAccent else PanelBorder, RoundedCornerShape(8.dp))
                        .clickable { viewModel.setSelectedGenre(g) }
                        .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = g,
                            fontSize = 11.sp,
                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                            color = if (isChosen) Color.White else SecondaryText
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                genres.drop(3).forEach { g ->
                    val isChosen = selectedGenre == g
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isChosen) PurpleAccent else PanelCard)
                            .border(0.5.dp, if (isChosen) CyanAccent else PanelBorder, RoundedCornerShape(8.dp))
                        .clickable { viewModel.setSelectedGenre(g) }
                        .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = g,
                            fontSize = 11.sp,
                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                            color = if (isChosen) Color.White else SecondaryText
                        )
                    }
                }
            }
        }

        // Instruments Multi-Select (مندول, دربوكة, كمان, بيانو, سينثيسايزر, غيتار, ناي)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = "الآلات الموسيقية المستخدمة (توزيع وتزامن)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
            val instruments = listOf("مندول", "دربوكة", "كمان", "بيانو", "سينثيسايزر", "غيتار", "ناي")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                instruments.take(4).forEach { inst ->
                    val active = selectedInstruments.contains(inst)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (active) SecondaryPanels else PanelCard)
                            .border(0.5.dp, if (active) CyanAccent else PanelBorder, RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleInstrument(inst) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = inst,
                            fontSize = 10.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            color = if (active) CyanAccent else SecondaryText
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                instruments.drop(4).forEach { inst ->
                    val active = selectedInstruments.contains(inst)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (active) SecondaryPanels else PanelCard)
                            .border(0.5.dp, if (active) CyanAccent else PanelBorder, RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleInstrument(inst) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = inst,
                            fontSize = 10.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            color = if (active) CyanAccent else SecondaryText
                        )
                    }
                }
            }
        }

        // 8 Character Categories Selector for Singing Voice (طفل، طفلة، شاب، شابة، امرأة، رجل، عجوز، شيخ)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "صوت المغني / فئة الشخصية الصوتية:", fontSize = 11.sp, color = SecondaryText)
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
                            .background(if (isChosen) PurpleAccent else SecondaryPanels)
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
                            .background(if (isChosen) PurpleAccent else SecondaryPanels)
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

        // Waveform & Music Player Preview (matches screenshot)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PanelCard)
                .border(0.5.dp, PanelBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SecondaryPanels)
                        .border(1.dp, CyanAccent, CircleShape)
                        .clickable { viewModel.toggleMusicPlayback() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "تشغيل الموسيقى",
                        tint = CyanAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                WaveformVisualizer(
                    isPlaying = isPlaying,
                    height = 36.dp,
                    barCount = 28,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "02:45",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
            }
        }

        // Lyrics or Mood Description Field with Dialect & Language Selection
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Single Compact Row for Song Language & Lyrics Dialect
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Box 1: لغة ولهجة الأغنية
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SecondaryPanels)
                        .border(0.5.dp, PanelBorder, RoundedCornerShape(8.dp))
                        .clickable { viewModel.openLanguagePicker("music", "اختر لغة ولهجة الأغنية والكلمات") }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("music_language_box")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = musicLanguage.flagEmoji, fontSize = 14.sp)
                            Column {
                                Text(text = "لغة الأغنية:", fontSize = 9.sp, color = SecondaryText)
                                Text(
                                    text = musicLanguage.nameAr,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText,
                                    maxLines = 1
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "اختيار",
                            tint = CyanAccent,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                // Box 2: توليد كلمات أصيلة باللهجة
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PurpleAccent.copy(alpha = 0.25f))
                        .border(0.5.dp, PurpleAccent, RoundedCornerShape(8.dp))
                        .clickable {
                            viewModel.generateMusicLyricsWithDialect()
                            statusMessage = "تم توليد كلمات أصيلة بلهجة: ${musicLanguage.nameAr}"
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("music_lyrics_gen_box")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MagentaAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(text = "لهجة الكلمات:", fontSize = 9.sp, color = MagentaAccent)
                            Text(
                                text = "توليد كلمات أصيلة",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = lyrics,
                onValueChange = { viewModel.setMusicLyrics(it) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = PanelBorder,
                    focusedTextColor = PrimaryText,
                    unfocusedTextColor = PrimaryText,
                    focusedContainerColor = PanelCard,
                    unfocusedContainerColor = PanelCard
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, lineHeight = 17.sp, color = PrimaryText)
            )
        }

        if (statusMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SecondaryPanels)
                    .padding(10.dp)
            ) {
                Text(text = statusMessage!!, fontSize = 11.sp, color = CyanAccent)
            }
        }

        // Action Buttons: "🎵 توليد الموسيقى" and "تجهيز وصف الموسيقى"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    viewModel.generateMusicTrack()
                    statusMessage = "تم توليد المقطوعة الموسيقية بنجاح وإدراجها في خط المونتاج!"
                },
                colors = ButtonDefaults.buttonColors(containerColor = MagentaAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp)
                    .testTag("generate_music_button")
            ) {
                Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("🎵 توليد الموسيقى", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            OutlinedButton(
                onClick = {
                    statusMessage = "تم تجهيز البرومبت الموسيقي المنظم: [${selectedGenre}] مع آلات [${selectedInstruments.joinToString(", ")}] وإيقاع 110 BPM متزامن."
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تجهيز الوصف", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
