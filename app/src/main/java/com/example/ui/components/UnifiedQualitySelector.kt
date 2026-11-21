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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

/**
 * النظام الموحد للجودة - 10 خيارات مرتبة بالضبط:
 * 360p, 480p, 720p, 720p60, 1080p, 1080p60, 1K, 2K, 4K, 8K
 */
val UNIFIED_STUDIO_QUALITIES = listOf(
    "360p",
    "480p",
    "720p",
    "720p60",
    "1080p",
    "1080p60",
    "1K",
    "2K",
    "4K",
    "8K"
)

@Composable
fun UnifiedQualitySelector(
    selectedQuality: String,
    onQualitySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    custom1KWidth: Int = 1024,
    custom1KHeight: Int = 1024,
    onCustom1KDimensionsChanged: (Int, Int) -> Unit = { _, _ -> },
    studioTitle: String = "جودة المعالجة والتوليد"
) {
    var widthInput by remember(custom1KWidth) { mutableStateOf(custom1KWidth.toString()) }
    var heightInput by remember(custom1KHeight) { mutableStateOf(custom1KHeight.toString()) }
    var dimensionError by remember { mutableStateOf<String?>(null) }

    // Engine classification
    val isLocalNative = selectedQuality in listOf("360p", "480p", "720p", "720p60", "1080p", "1080p60")
    val isCustom1K = selectedQuality == "1K"
    val isNeuralUpscale = selectedQuality in listOf("2K", "4K", "8K")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PanelCard)
            .border(0.5.dp, PanelBorder, RoundedCornerShape(12.dp))
            .padding(10.dp)
            .testTag("unified_quality_selector")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header Row
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
                        imageVector = Icons.Default.HighQuality,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = studioTitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                    Text(
                        text = "($selectedQuality)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                }

                // Realistic Engine Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when {
                                isNeuralUpscale -> PurpleAccent.copy(alpha = 0.25f)
                                isCustom1K -> CyanAccent.copy(alpha = 0.2f)
                                else -> SuccessGreen.copy(alpha = 0.2f)
                            }
                        )
                        .border(
                            0.5.dp,
                            when {
                                isNeuralUpscale -> PurpleAccent
                                isCustom1K -> CyanAccent
                                else -> SuccessGreen
                            },
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                isNeuralUpscale -> Icons.Default.AutoAwesome
                                isCustom1K -> Icons.Default.Settings
                                else -> Icons.Default.Memory
                            },
                            contentDescription = null,
                            tint = when {
                                isNeuralUpscale -> PurpleAccent
                                isCustom1K -> CyanAccent
                                else -> SuccessGreen
                            },
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = when {
                                isNeuralUpscale -> "سحابي / Neural Upscale"
                                isCustom1K -> "أبعاد مخصصة 1K"
                                else -> "معالجة محلية سريعة"
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isNeuralUpscale -> PurpleAccent
                                isCustom1K -> CyanAccent
                                else -> SuccessGreen
                            }
                        )
                    }
                }
            }

            // 10 Quality Options Horizontal Scroll Bar
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(UNIFIED_STUDIO_QUALITIES) { quality ->
                    val isSelected = selectedQuality == quality
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) ElectricBlue else SecondaryPanels)
                            .border(
                                0.5.dp,
                                if (isSelected) CyanAccent else PanelBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onQualitySelected(quality) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("quality_option_$quality"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = quality,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else PrimaryText
                            )
                        }
                    }
                }
            }

            // Custom 1K Dimension Inputs (Shown only when 1K is selected)
            if (isCustom1K) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SecondaryPanels)
                        .border(0.5.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تحديد أبعاد 1K مخصصة (Custom Dimensions):",
                                fontSize = 10.sp,
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold
                            )
                            val w = widthInput.toIntOrNull() ?: 1024
                            val h = heightInput.toIntOrNull() ?: 1024
                            val ratioText = when {
                                w == h -> "1:1 مربع"
                                w > h -> "${String.format(java.util.Locale.US, "%.2f", w.toFloat() / h)}:1 عريض"
                                else -> "1:${String.format(java.util.Locale.US, "%.2f", h.toFloat() / w)} طولي"
                            }
                            Text(
                                text = "النسبة: $ratioText",
                                fontSize = 9.sp,
                                color = SecondaryText
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = widthInput,
                                onValueChange = {
                                    widthInput = it.filter { ch -> ch.isDigit() }.take(4)
                                    val wVal = widthInput.toIntOrNull() ?: 1024
                                    val hVal = heightInput.toIntOrNull() ?: 1024
                                    if (wVal in 256..2048 && hVal in 256..2048) {
                                        dimensionError = null
                                        onCustom1KDimensionsChanged(wVal, hVal)
                                    } else {
                                        dimensionError = "الأبعاد يجب أن تتراوح بين 256 و 2048 بكسل"
                                    }
                                },
                                label = { Text("العرض (Width)", fontSize = 9.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_1k_width"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanAccent,
                                    unfocusedBorderColor = PanelBorder,
                                    focusedTextColor = PrimaryText,
                                    unfocusedTextColor = PrimaryText,
                                    focusedContainerColor = DarkPanels,
                                    unfocusedContainerColor = DarkPanels
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = PrimaryText)
                            )

                            Text(text = "×", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SecondaryText)

                            OutlinedTextField(
                                value = heightInput,
                                onValueChange = {
                                    heightInput = it.filter { ch -> ch.isDigit() }.take(4)
                                    val wVal = widthInput.toIntOrNull() ?: 1024
                                    val hVal = heightInput.toIntOrNull() ?: 1024
                                    if (wVal in 256..2048 && hVal in 256..2048) {
                                        dimensionError = null
                                        onCustom1KDimensionsChanged(wVal, hVal)
                                    } else {
                                        dimensionError = "الأبعاد يجب أن تتراوح بين 256 و 2048 بكسل"
                                    }
                                },
                                label = { Text("الارتفاع (Height)", fontSize = 9.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_1k_height"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanAccent,
                                    unfocusedBorderColor = PanelBorder,
                                    focusedTextColor = PrimaryText,
                                    unfocusedTextColor = PrimaryText,
                                    focusedContainerColor = DarkPanels,
                                    unfocusedContainerColor = DarkPanels
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = PrimaryText)
                            )
                        }

                        if (dimensionError != null) {
                            Text(
                                text = dimensionError!!,
                                fontSize = 8.5.sp,
                                color = Color(0xFFF43F5E)
                            )
                        }
                    }
                }
            }
        }
    }
}
