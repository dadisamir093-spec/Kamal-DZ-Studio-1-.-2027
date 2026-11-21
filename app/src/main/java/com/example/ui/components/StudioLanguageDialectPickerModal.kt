package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.LanguageGroup
import com.example.data.model.StudioLanguage
import com.example.data.model.StudioLanguageRegistry
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
import com.example.ui.theme.WarningAmber

@Composable
fun StudioLanguageDialectPickerModal(
    title: String,
    currentLanguageCode: String,
    onLanguageSelected: (StudioLanguage) -> Unit,
    onPreviewAudio: ((StudioLanguage) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var selectedGroup by remember { mutableStateOf(LanguageGroup.ALGERIAN) }
    var searchQuery by remember { mutableStateOf("") }

    val languages = when (selectedGroup) {
        LanguageGroup.ALGERIAN -> StudioLanguageRegistry.algerianDialects
        LanguageGroup.ARABIC -> StudioLanguageRegistry.arabicDialects
        LanguageGroup.INTERNATIONAL -> StudioLanguageRegistry.internationalLanguages
    }.filter {
        searchQuery.isBlank() ||
                it.nameAr.contains(searchQuery, ignoreCase = true) ||
                it.nameEn.contains(searchQuery, ignoreCase = true) ||
                it.countryOrRegion.contains(searchQuery, ignoreCase = true)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 660.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, PanelBorder, RoundedCornerShape(20.dp))
                .testTag("language_picker_modal"),
            colors = CardDefaults.cardColors(containerColor = DarkPanels)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElectricBlue.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                            Text(text = "دعم كامل لجميع اللهجات الجزائرية والعربية واللغات العالمية", fontSize = 10.sp, color = SecondaryText)
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = SecondaryText)
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ابحث عن لهجة أو لغة...", fontSize = 11.sp, color = SecondaryText) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = PanelBorder,
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText,
                        focusedContainerColor = PanelCard,
                        unfocusedContainerColor = PanelCard
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = PrimaryText)
                )

                // 3 Tabs (Algerian Dialects / Arabic Dialects / World Languages)
                TabRow(
                    selectedTabIndex = selectedGroup.ordinal,
                    containerColor = SecondaryPanels,
                    contentColor = CyanAccent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedGroup.ordinal]),
                            color = CyanAccent
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    LanguageGroup.values().forEach { group ->
                        Tab(
                            selected = selectedGroup == group,
                            onClick = { selectedGroup = group },
                            text = {
                                Text(
                                    text = when (group) {
                                        LanguageGroup.ALGERIAN -> "الجزائرية (10)"
                                        LanguageGroup.ARABIC -> "العربية (16)"
                                        LanguageGroup.INTERNATIONAL -> "العالمية (10)"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedGroup == group) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                // Languages & Dialects List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(languages, key = { it.code }) { lang ->
                        val isSelected = currentLanguageCode == lang.code

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) SecondaryPanels else PanelCard)
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) CyanAccent else if (lang.isPriority) WarningAmber else PanelBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    onLanguageSelected(lang)
                                    onDismiss()
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = lang.nameAr,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) CyanAccent else PrimaryText
                                        )

                                        if (lang.isPriority) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(WarningAmber.copy(alpha = 0.2f))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(10.dp))
                                                    Text(text = "أولوية", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = WarningAmber)
                                                }
                                            }
                                        }

                                        Text(
                                            text = "(${lang.nameEn})",
                                            fontSize = 11.sp,
                                            color = SecondaryText
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = "المنطقة: ${lang.countryOrRegion} • ${lang.supportBadge}", fontSize = 10.sp, color = SuccessGreen)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "«${lang.sampleGreeting}»",
                                        fontSize = 10.sp,
                                        color = SecondaryText,
                                        maxLines = 1
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (onPreviewAudio != null) {
                                        IconButton(
                                            onClick = { onPreviewAudio(lang) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "استماع", tint = CyanAccent, modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "محدد", tint = CyanAccent, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
