package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkPanels
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.PanelBorder
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryPanels
import com.example.ui.theme.SecondaryText

data class StudioNavTab(
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

val studioNavTabs = listOf(
    StudioNavTab("التوليد", Icons.Default.AutoAwesome, "nav_tab_generation"),
    StudioNavTab("الشخصيات", Icons.Default.Group, "nav_tab_characters"),
    StudioNavTab("الصوت", Icons.Default.GraphicEq, "nav_tab_voice"),
    StudioNavTab("الموسيقى", Icons.Default.LibraryMusic, "nav_tab_music"),
    StudioNavTab("المونتاج", Icons.Default.ContentCut, "nav_tab_editing"),
    StudioNavTab("المكتبة", Icons.Default.VideoLibrary, "nav_tab_library"),
    StudioNavTab("التصدير", Icons.Default.FileDownload, "nav_tab_export")
)

@Composable
fun StudioBottomNavBar(
    selectedSectionIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkPanels)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(PanelBorder, DarkPanels)
                ),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .navigationBarsPadding()
            .height(68.dp)
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            studioNavTabs.forEachIndexed { index, tab ->
                val isSelected = selectedSectionIndex == index
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.05f else 1f,
                    animationSpec = spring(),
                    label = "tab_scale"
                )
                val iconTint by animateColorAsState(
                    targetValue = if (isSelected) CyanAccent else SecondaryText,
                    label = "icon_tint"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) PrimaryText else SecondaryText,
                    label = "text_color"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onTabSelected(index) }
                        .padding(vertical = 4.dp)
                        .scale(scale)
                        .testTag(tab.testTag),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Active Pill Background
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .then(
                                if (isSelected) {
                                    Modifier.background(
                                        Brush.horizontalGradient(
                                            listOf(ElectricBlue.copy(alpha = 0.35f), SecondaryPanels)
                                        )
                                    )
                                } else {
                                    Modifier
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = tab.title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
