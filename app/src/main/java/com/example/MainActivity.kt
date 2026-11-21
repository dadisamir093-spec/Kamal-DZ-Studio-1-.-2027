package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.CameraMovementPickerModal
import com.example.ui.components.SkillsCenterModal
import com.example.ui.components.StudioBottomNavBar
import com.example.ui.components.StudioTopBar
import com.example.ui.screens.CharacterStudioScreen
import com.example.ui.screens.EditingStudioScreen
import com.example.ui.screens.ExportStudioScreen
import com.example.ui.screens.GenerationStudioScreen
import com.example.ui.screens.LibraryStudioScreen
import com.example.ui.screens.MusicStudioScreen
import com.example.ui.screens.VoiceStudioScreen
import com.example.ui.theme.DarkPanels
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.StudioViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.example.data.remote.GeminiStudioService.initializeAppCheck(applicationContext)
        setContent {
            MyApplicationTheme {
                // RTL LayoutDirection for authentic Arabic cinematic experience
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    StudioAppRoot()
                }
            }
        }
    }
}

@Composable
fun StudioAppRoot(
    viewModel: StudioViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 7 })

    val currentSection by viewModel.currentSection.collectAsState()
    val skills by viewModel.skills.collectAsState()
    val showSkillsModal by viewModel.showSkillsCenter.collectAsState()
    val showCameraModal by viewModel.showCameraPicker.collectAsState()
    val activeSlot by viewModel.activeSlot.collectAsState()
    val slot1 by viewModel.slot1State.collectAsState()
    val slot2 by viewModel.slot2State.collectAsState()
    val currentSlot = if (activeSlot == 1) slot1 else slot2

    // Keep ViewModel currentSection synchronized with pagerState
    LaunchedEffect(currentSection) {
        if (pagerState.currentPage != currentSection) {
            pagerState.animateScrollToPage(currentSection)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (viewModel.currentSection.value != pagerState.currentPage) {
            viewModel.setSection(pagerState.currentPage)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightNavy),
        topBar = {
            StudioTopBar(
                activeSkillsCount = skills.count { it.isEnabled },
                onOpenSkillsCenter = { viewModel.openSkillsCenter() }
            )
        },
        bottomBar = {
            StudioBottomNavBar(
                selectedSectionIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
        },
        containerColor = DarkPanels
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal Pager: Swipe Navigation across all 7 sections
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> GenerationStudioScreen(viewModel = viewModel)
                    1 -> CharacterStudioScreen(viewModel = viewModel)
                    2 -> VoiceStudioScreen(viewModel = viewModel)
                    3 -> MusicStudioScreen(viewModel = viewModel)
                    4 -> EditingStudioScreen(viewModel = viewModel)
                    5 -> LibraryStudioScreen(viewModel = viewModel)
                    6 -> ExportStudioScreen(viewModel = viewModel)
                }
            }

            // Skills Center Modal
            if (showSkillsModal) {
                SkillsCenterModal(
                    skills = skills,
                    onToggleSkill = { viewModel.toggleSkillEnabled(it) },
                    onAddNewSkill = { name, desc, category, content ->
                        viewModel.addNewSkill(name, desc, category, content)
                    },
                    onDeleteSkill = { viewModel.deleteSkill(it) },
                    onDismiss = { viewModel.closeSkillsCenter() }
                )
            }

            // 20 Camera Movements Modal
            if (showCameraModal) {
                CameraMovementPickerModal(
                    selectedMovement = currentSlot.cameraMovement,
                    onSelectMovement = { viewModel.updateCurrentSlotCamera(it) },
                    onDismiss = { viewModel.closeCameraPicker() }
                )
            }

            // Universal Language & Dialect Picker Modal (36 Dialects & Languages)
            val isLanguagePickerOpen by viewModel.isLanguagePickerOpen.collectAsState()
            val languagePickerTitle by viewModel.languagePickerTitle.collectAsState()
            val languagePickerPurpose by viewModel.languagePickerPurpose.collectAsState()
            val scriptLang by viewModel.scriptLanguage.collectAsState()
            val charLang by viewModel.characterLanguage.collectAsState()
            val musicLang by viewModel.musicLanguage.collectAsState()
            val voiceLang by viewModel.voiceLanguage.collectAsState()
            val subSourceLang by viewModel.subtitleSourceLanguage.collectAsState()
            val subTargetLang by viewModel.subtitleTargetLanguage.collectAsState()

            val currentLangCode = when (languagePickerPurpose) {
                "script" -> scriptLang.code
                "character" -> charLang.code
                "music" -> musicLang.code
                "voice" -> voiceLang.code
                "subtitleSource" -> subSourceLang.code
                else -> subTargetLang.code
            }

            if (isLanguagePickerOpen) {
                com.example.ui.components.StudioLanguageDialectPickerModal(
                    title = languagePickerTitle,
                    currentLanguageCode = currentLangCode,
                    onLanguageSelected = { viewModel.selectLanguageForPurpose(it) },
                    onPreviewAudio = { lang ->
                        viewModel.selectLanguageForPurpose(lang)
                        viewModel.previewVoicePlayback()
                    },
                    onDismiss = { viewModel.closeLanguagePicker() }
                )
            }
        }
    }
}
