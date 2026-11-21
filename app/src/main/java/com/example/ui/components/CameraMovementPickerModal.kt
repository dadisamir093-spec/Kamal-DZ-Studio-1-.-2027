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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkPanels
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.PanelBorder
import com.example.ui.theme.PanelCard
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryPanels
import com.example.ui.theme.SecondaryText

data class CameraMovement(
    val id: Int,
    val titleAr: String,
    val titleEn: String,
    val description: String
)

val twentyCameraMovements = listOf(
    CameraMovement(1, "Static Lock", "ثبات الكاميرا", "تثبيت الكاميرا في مكان محدد لمنح المشهد هدوءاً وتركيزاً تاماً"),
    CameraMovement(2, "Pan", "تحريك أفقي", "دوران الكاميرا أفقياً من اليسار لليمين أو العكس لمسح البيئة المحيطة"),
    CameraMovement(3, "Tilt", "تحريك عمودي", "تحريك زاوية العدسة عمودياً للأعلى أو للأسفل لإظهار الارتفاع"),
    CameraMovement(4, "Dolly In", "اقتراب الكاميرا", "تحرك الكاميرا جسدياً نحو الهدف لتعزيز الدراما والمشاعر"),
    CameraMovement(5, "Dolly Out", "ابتعاد الكاميرا", "تحرك الكاميرا مبتعدة للكشف تدريجياً عن المكان المحيط"),
    CameraMovement(6, "Zoom In", "تكبير بصري", "تكبير البعد البؤري للعدسة دون تحريك جسد الكاميرا"),
    CameraMovement(7, "Zoom Out", "تصغير بصري", "تصغير البعد البؤري للعدسة للتوسيع السريع للمشهد"),
    CameraMovement(8, "Tracking Shot", "تتبع جانبي", "مرافقة الشخصية أو المركبة موازية لحركتها الجانبية"),
    CameraMovement(9, "Follow Shot", "تتبع الشخصية", "متابعة الشخصية من الخلف أثناء تقدمها في المشهد"),
    CameraMovement(10, "Orbit Shot", "دوران حول الهدف", "دوران بزاوية 360 أو 180 درجة حول الشخصية لإبراز الأهمية"),
    CameraMovement(11, "Crane Up", "رافعة للأعلى", "حركة رافعة عمودية صاعدة تمنح اللقطة بعداً سينمائياً ملحمياً"),
    CameraMovement(12, "Crane Down", "رافعة للأسفل", "نزول سلس من الأعلى إلى مستوى عيني الشخصية"),
    CameraMovement(13, "Drone Shot", "لقطة جوية درامية", "طيران درامي فوق المدن أو المناظر الطبيعية الشاسعة"),
    CameraMovement(14, "Smart Drone", "درون ذكي وسريع", "مناورات هوائية سريعة ومرنة لمشاهد الأكشن والمطاردات"),
    CameraMovement(15, "Top-Down", "لقطة رأسية مباشرة", "تصوير بزاوية 90 درجة من الأعلى مباشرة لإبراز الهندسة والتكوين"),
    CameraMovement(16, "Low-Angle", "زاوية منخفضة", "تصوير من الأسفل يمنح الشخصية حضوراً وهيمنة قوية"),
    CameraMovement(17, "POV", "منظور عين الشخصية", "رؤية الأحداث مباشرة من عيني البطل لمعايشة واقعية"),
    CameraMovement(18, "Over-the-Shoulder", "عبر الكتف", "لقطة حوارية سينمائية كلاسيكية عبر كتف أحد الممثلين"),
    CameraMovement(19, "Handheld", "كاميرا محمولة باليد", "اهتزاز واقعي طبيعي يضفي وتيرة توتر ومستندات حية"),
    CameraMovement(20, "Gimbal / Steadicam", "حركة انسيابية مثبتة", "حركة بالغة السلاسة والانسيابية خالية من أي اهتزازات")
)

@Composable
fun CameraMovementPickerModal(
    selectedMovement: String,
    onSelectMovement: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 580.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, PanelBorder, RoundedCornerShape(20.dp))
                .testTag("camera_movement_modal"),
            colors = CardDefaults.cardColors(containerColor = DarkPanels)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Modal Header
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
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElectricBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "حركات الكاميرا السينمائية (20)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                            Text(
                                text = "اختر زاوية وحركة الكاميرا لتوجيه نموذج التوليد",
                                fontSize = 11.sp,
                                color = SecondaryText
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = SecondaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // List of 20 movements
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(twentyCameraMovements, key = { it.id }) { movement ->
                        val isChosen = selectedMovement.contains(movement.titleAr) ||
                                selectedMovement.contains(movement.titleEn)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isChosen) SecondaryPanels else PanelCard)
                                .border(
                                    width = if (isChosen) 1.5.dp else 0.5.dp,
                                    color = if (isChosen) CyanAccent else PanelBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    onSelectMovement("${movement.titleAr} — ${movement.titleEn}")
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
                                            text = "${movement.id}. ${movement.titleAr}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isChosen) CyanAccent else PrimaryText
                                        )
                                        Text(
                                            text = "(${movement.titleEn})",
                                            fontSize = 12.sp,
                                            color = SecondaryText
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = movement.description,
                                        fontSize = 11.sp,
                                        color = SecondaryText,
                                        lineHeight = 16.sp
                                    )
                                }

                                if (isChosen) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "محدد",
                                        tint = CyanAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
