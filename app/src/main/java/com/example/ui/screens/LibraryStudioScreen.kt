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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.ProjectEntity
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkPanels
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PanelBorder
import com.example.ui.theme.PanelCard
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.SecondaryPanels
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SuccessGreen
import com.example.viewmodel.StudioViewModel

data class StudioShortcut(
    val index: Int,
    val name: String,
    val icon: ImageVector
)

val studioShortcutsList = listOf(
    StudioShortcut(0, "1. التوليد", Icons.Default.AutoAwesome),
    StudioShortcut(1, "2. الشخصيات", Icons.Default.Person),
    StudioShortcut(2, "3. الصوت", Icons.Default.Mic),
    StudioShortcut(3, "4. الموسيقى", Icons.Default.GraphicEq),
    StudioShortcut(4, "5. المونتاج", Icons.Default.ContentCut),
    StudioShortcut(5, "6. المكتبة", Icons.Default.Folder),
    StudioShortcut(6, "7. التصدير", Icons.Default.FileDownload)
)

@Composable
fun LibraryStudioScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.projects.collectAsState()
    val filter by viewModel.libraryFilter.collectAsState()
    val searchQuery by viewModel.librarySearchQuery.collectAsState()

    val scrollState = rememberScrollState()

    val filteredProjects = projects.filter { project ->
        val matchesSearch = project.title.contains(searchQuery, ignoreCase = true) ||
                project.description.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (filter) {
            "الكل", "جميع المشاريع" -> true
            "المفضلة" -> project.isFavorite
            else -> project.category.contains(filter, ignoreCase = true)
        }
        matchesSearch && matchesFilter
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkPanels)
            .verticalScroll(scrollState)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Header & Navigation Shortcuts
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "6. مكتبة المشاريع السينمائية وإدارة الأصول",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Text(
                text = "استعراض المشاهد، إدارة ملفات الفيديو والصوت، وتتبع حالة الحفظ والمزامنة",
                fontSize = 11.sp,
                color = SecondaryText
            )
        }

        // Studios Shortcuts Bar
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(studioShortcutsList) { shortcut ->
                val isCurrent = viewModel.currentSection.collectAsState().value == shortcut.index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isCurrent) ElectricBlue else SecondaryPanels)
                        .border(0.5.dp, if (isCurrent) CyanAccent else PanelBorder, RoundedCornerShape(8.dp))
                        .clickable { viewModel.setSection(shortcut.index) }
                        .padding(horizontal = 9.dp, vertical = 6.dp)
                        .testTag("shortcut_studio_${shortcut.index}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = shortcut.icon,
                            contentDescription = null,
                            tint = if (isCurrent) Color.White else CyanAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = shortcut.name,
                            fontSize = 10.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCurrent) Color.White else PrimaryText
                        )
                    }
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setLibrarySearchQuery(it) },
            placeholder = { Text("بحث في المشاريع والمشاهد...", fontSize = 11.sp, color = SecondaryText) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(18.dp)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("library_search_input"),
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

        // Filters Row
        val filters = listOf("جميع المشاريع", "فيديوهات", "صور", "أصوات", "موسيقى", "المفضلة")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            filters.take(3).forEach { f ->
                val isSelected = filter == f
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) ElectricBlue else SecondaryPanels)
                        .border(0.5.dp, if (isSelected) CyanAccent else PanelBorder, RoundedCornerShape(6.dp))
                        .clickable { viewModel.setLibraryFilter(f) }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = f, fontSize = 9.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) Color.White else SecondaryText)
                }
            }
            filters.drop(3).forEach { f ->
                val isSelected = filter == f
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) ElectricBlue else SecondaryPanels)
                        .border(0.5.dp, if (isSelected) CyanAccent else PanelBorder, RoundedCornerShape(6.dp))
                        .clickable { viewModel.setLibraryFilter(f) }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = f, fontSize = 9.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) Color.White else SecondaryText)
                }
            }
        }

        // ==========================================
        // النصف العلوي: معاينات المشاريع والصور والفيديوهات المنظمة
        // ==========================================
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "معاينات المشاريع والمشاهد الحديثة (${filteredProjects.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
            Text(text = "عرض سينمائي", fontSize = 9.sp, color = CyanAccent)
        }

        // Display projects in clean 2-column rows in the upper section
        val projectChunks = filteredProjects.chunked(2)
        projectChunks.forEach { rowProjects ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowProjects.forEach { project ->
                    val thumbnailRes = when (project.id % 4) {
                        0L -> R.drawable.img_cinematic_hero
                        1L -> R.drawable.img_editing_bay
                        2L -> R.drawable.img_character_ref
                        else -> R.drawable.img_music_banner
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PanelCard)
                            .border(0.5.dp, PanelBorder, RoundedCornerShape(10.dp))
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 10f)
                            ) {
                                Image(
                                    painter = painterResource(id = thumbnailRes),
                                    contentDescription = project.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Duration Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xCC000000))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                        .align(Alignment.BottomEnd)
                                ) {
                                    Text(text = project.durationText, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                                }

                                // Favorite Icon
                                IconButton(
                                    onClick = { viewModel.toggleFavorite(project) },
                                    modifier = Modifier.size(26.dp).align(Alignment.TopEnd)
                                ) {
                                    Icon(
                                        imageVector = if (project.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "المفضلة",
                                        tint = if (project.isFavorite) Color(0xFFE747D9) else Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }

                            // Project info row
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = project.title, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = PrimaryText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(text = "${project.resolution} • ${project.category}", fontSize = 8.5.sp, color = CyanAccent)
                                }

                                IconButton(
                                    onClick = { viewModel.deleteProject(project) },
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = SecondaryText, modifier = Modifier.size(13.dp))
                                }
                            }
                        }
                    }
                }
                if (rowProjects.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // ==========================================
        // النصف السفلي: أدوات عملية ومتطورة لإدارة المشاريع والملفات وحالة الحفظ
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PanelCard)
                .border(0.5.dp, PanelBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Section Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.Storage, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                        Text(text = "لوحة إدارة الأصول والمشاريع وحالة الحفظ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(imageVector = Icons.Default.CloudDone, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                        Text(text = "محفوظ محلياً ومؤمّن", fontSize = 9.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                    }
                }

                // Storage and Project Metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Metric 1: Total Projects
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SecondaryPanels)
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(text = "إجمالي المشاريع", fontSize = 9.sp, color = SecondaryText)
                            Text(text = "${projects.size} مشروع", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        }
                    }

                    // Metric 2: Timeline Status
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SecondaryPanels)
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(text = "مسارات المونتاج", fontSize = 9.sp, color = SecondaryText)
                            Text(text = "نشطة ومزامنة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }
                    }

                    // Metric 3: DB Cache
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SecondaryPanels)
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(text = "سعة التخزين", fontSize = 9.sp, color = SecondaryText)
                            Text(text = "Room SQLite", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PurpleAccent)
                        }
                    }
                }

                // Quick Project Management Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Save Active Timeline
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricBlue)
                            .clickable {
                                viewModel.saveTimelineToCurrentProject("مشروع سينمائي #${projects.size + 1}")
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Text(text = "حفظ المونتاج كـ مشروع", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Go to Export Studio
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SecondaryPanels)
                            .border(0.5.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable { viewModel.setSection(6) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(13.dp))
                            Text(text = "تصدير الأصول (استوديو 7)", fontSize = 10.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
