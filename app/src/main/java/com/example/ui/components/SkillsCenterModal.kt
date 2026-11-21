package com.example.ui.components

import android.content.Context
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.local.SkillEntity
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

@Composable
fun SkillsCenterModal(
    skills: List<SkillEntity>,
    onToggleSkill: (SkillEntity) -> Unit,
    onAddNewSkill: (name: String, desc: String, category: String, content: String) -> Unit,
    onDeleteSkill: (SkillEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: All Skills, 1: Import SKILL.md
    var showImportSuccess by remember { mutableStateOf(false) }

    // Import form state
    var skillMarkdownInput by remember {
        mutableStateOf(
            """---
name: "حركات الإضاءة الملحمية"
description: "توجيه إضاءة النيون الذهبية والزرقاء لإضفاء طابع سينمائي حربي"
category: "استوديو التوليد"
---
# تعليمات المهارة
- إضافة إضاءة حافة Rim Lighting
- تكثيف الدخان والضباب الجوي Cinematic Haze
- تعزيز التباين في الظلال
"""
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, PanelBorder, RoundedCornerShape(20.dp))
                .testTag("skills_center_modal"),
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
                                .background(PurpleAccent.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Extension,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "مركز المهارات السينمائية",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                            Text(
                                text = "إدارة مهارات الذكاء الاصطناعي واستيراد SKILL.md",
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

                // Tabs: All Skills vs Import SKILL.md
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SecondaryPanels,
                    contentColor = CyanAccent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CyanAccent
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "المهارات المثبتة (${skills.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "استيراد SKILL.md",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    if (com.example.BuildConfig.DEBUG) {
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = {
                                Text(
                                    "App Check Token",
                                    fontSize = 11.sp,
                                    color = CyanAccent,
                                    fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    // List of Skills
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(skills, key = { it.id }) { skill ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(PanelCard)
                                    .border(0.5.dp, if (skill.isEnabled) ElectricBlue.copy(alpha = 0.5f) else PanelBorder, RoundedCornerShape(14.dp))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = skill.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (skill.isEnabled) PrimaryText else SecondaryText
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(SecondaryPanels)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = skill.category,
                                                    fontSize = 9.sp,
                                                    color = CyanAccent
                                                )
                                            }
                                        }

                                        Switch(
                                            checked = skill.isEnabled,
                                            onCheckedChange = { onToggleSkill(skill) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = ElectricBlue,
                                                uncheckedThumbColor = SecondaryText,
                                                uncheckedTrackColor = SecondaryPanels
                                            )
                                        )
                                    }

                                    Text(
                                        text = skill.description,
                                        fontSize = 12.sp,
                                        color = SecondaryText,
                                        lineHeight = 17.sp
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (skill.isBuiltIn) "مهارة مدمجة بالاستوديو" else "مهارة مخصصة مستوردة",
                                            fontSize = 10.sp,
                                            color = if (skill.isBuiltIn) PurpleAccent else SuccessGreen
                                        )

                                        if (!skill.isBuiltIn) {
                                            IconButton(
                                                onClick = { onDeleteSkill(skill) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "حذف",
                                                    tint = ErrorRed,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedTab == 1) {
                    // Import SKILL.md form
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SecondaryPanels)
                                .padding(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "يتم تحليل ملف SKILL.md لاستخراج اسم المهارة وتصنيفها والتعليمات دون تنفيذ أي أوامر نظام غير آمنة.",
                                    fontSize = 11.sp,
                                    color = PrimaryText,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        OutlinedTextField(
                            value = skillMarkdownInput,
                            onValueChange = { skillMarkdownInput = it },
                            label = { Text("محتوى ملف SKILL.md", fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
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

                        Button(
                            onClick = {
                                // Parse simple YAML name & description or use default
                                val lines = skillMarkdownInput.lines()
                                val nameLine = lines.firstOrNull { it.startsWith("name:") }
                                val descLine = lines.firstOrNull { it.startsWith("description:") }
                                val categoryLine = lines.firstOrNull { it.startsWith("category:") }

                                val parsedName = nameLine?.substringAfter("name:")?.replace("\"", "")?.trim()
                                    ?: "مهارة سينمائية مخصصة"
                                val parsedDesc = descLine?.substringAfter("description:")?.replace("\"", "")?.trim()
                                    ?: "مهارة تم استيرادها بواسطة ملف SKILL.md"
                                val parsedCategory = categoryLine?.substringAfter("category:")?.replace("\"", "")?.trim()
                                    ?: "استوديو التوليد"

                                onAddNewSkill(parsedName, parsedDesc, parsedCategory, skillMarkdownInput)
                                selectedTab = 0
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("اعتماد واستيراد المهارة", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (selectedTab == 2 && com.example.BuildConfig.DEBUG) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                    var tokenText by remember { mutableStateOf("جاري قراءة رمز App Check...") }
                    var isCopied by remember { mutableStateOf(false) }

                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        try {
                            val token = com.example.data.remote.GeminiStudioService.getDebugSecretToken(context)
                            if (!token.isNullOrEmpty()) {
                                tokenText = token
                            } else {
                                tokenText = "لم يتم توليد رمز بعد — يرجى فتح التطبيق مرة أخرى"
                            }
                        } catch (e: Exception) {
                            tokenText = "تعذر قراءة الرمز: ${e.message}"
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkPanels),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "🔑 رمز تصحيح الأخطاء (App Check Debug Secret):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    tokenText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SecondaryPanels, RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(tokenText))
                                isCopied = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isCopied) androidx.compose.ui.graphics.Color(0xFF10B981) else ElectricBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (isCopied) "✅ تم نسخ الرمز للحافظة!" else "📋 نسخ الرمز السري",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            "ملاحظة: تظهر هذه الشاشة حصرياً في نسخة Debug المحلية. انسخ الرمز وأضفه إلى Firebase Console -> App Check -> Manage debug tokens.",
                            fontSize = 11.sp,
                            color = SecondaryText,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
