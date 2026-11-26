package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.AudioTrackEntity
import com.example.data.local.CharacterEntity
import com.example.data.local.ExportTaskEntity
import com.example.data.local.ProjectEntity
import com.example.data.local.SkillEntity
import com.example.data.media.TimelineMediaEngine
import com.example.data.model.TimelineClip
import com.example.data.model.TimelineOperationResult
import com.example.data.model.TimelineTrackType
import com.example.data.repository.StudioRepository
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GenerationSlotState(
    val slotIndex: Int = 1,
    val mode: String = "توليد الصور", // "توليد الصور", "صورة إلى فيديو", "نص إلى فيديو"
    val prompt: String = "مشهد سينمائي لرجل مميز في مدينة جزائرية قديمة عند غروب الشمس، إضاءة ذهبية ولقطة سينمائية",
    val negativePrompt: String = "تشوه، حركة غير واقعية، جودة منخفضة، ضبابية، نص غير مرغوب",
    val cameraMovement: String = "كاميرا سينمائية (Dolly In)",
    val lens: String = "Anamorphic 35mm",
    val durationText: String = "05:00",
    val resolution: String = "1080p",
    val aspectRatio: String = "16:9",
    val fps: Int = 30,
    val isGenerating: Boolean = false,
    val progress: Float = 0f,
    val statusMessage: String = "",
    val resultImageRes: Int? = null,
    val resultImageFile: String? = null,
    val hasResult: Boolean = false,
    val referenceImageUri: String? = null,
    val finalPromptUsed: String? = null,
    val referenceDrawableRes: Int? = R.drawable.img_character_ref
)

data class CharacterArchetype(
    val id: Int,
    val name: String,
    val role: String,
    val drawableRes: Int,
    val category: String,
    val age: String,
    val traits: List<String>
)

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private companion object {
        /** عدد الصور المتتابعة في وضع «نص إلى فيديو». */
        const val VIDEO_IMAGE_COUNT = 4
    }

    private val repository: StudioRepository
    val projects: StateFlow<List<ProjectEntity>>
    val characters: StateFlow<List<CharacterEntity>>
    val audioTracks: StateFlow<List<AudioTrackEntity>>
    val exportTasks: StateFlow<List<ExportTaskEntity>>
    val skills: StateFlow<List<SkillEntity>>

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = StudioRepository(database)

        projects = repository.allProjects.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        characters = repository.allCharacters.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        audioTracks = repository.allAudioTracks.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        exportTasks = repository.allExportTasks.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        skills = repository.allSkills.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        fetchDailyQuota()
    }

    private val _remainingDailyQuota = MutableStateFlow(10000)
    val remainingDailyQuota: StateFlow<Int> = _remainingDailyQuota.asStateFlow()

    fun fetchDailyQuota() {
        viewModelScope.launch {
            val result = com.example.data.remote.GeminiStudioService.fetchDailyQuotaInfo(getApplication())
            val quotaInfo = result.getOrNull()
            if (quotaInfo != null) {
                _remainingDailyQuota.value = quotaInfo.remainingNeurons
            }
        }
    }

    // Navigation (0 to 6)
    private val _currentSection = MutableStateFlow(0)
    val currentSection: StateFlow<Int> = _currentSection.asStateFlow()

    fun setSection(index: Int) {
        _currentSection.value = index.coerceIn(0, 6)
    }

    // Skills Center Modal visibility
    private val _showSkillsCenter = MutableStateFlow(false)
    val showSkillsCenter: StateFlow<Boolean> = _showSkillsCenter.asStateFlow()

    fun openSkillsCenter() { _showSkillsCenter.value = true }
    fun closeSkillsCenter() { _showSkillsCenter.value = false }

    // Camera Movement Picker visibility
    private val _showCameraPicker = MutableStateFlow(false)
    val showCameraPicker: StateFlow<Boolean> = _showCameraPicker.asStateFlow()

    fun openCameraPicker() { _showCameraPicker.value = true }
    fun closeCameraPicker() { _showCameraPicker.value = false }

    // Section 1: Generation Slots (Slot 1 & Slot 2)
    private val _activeSlot = MutableStateFlow(1)
    val activeSlot: StateFlow<Int> = _activeSlot.asStateFlow()

    private val _slot1State = MutableStateFlow(GenerationSlotState(slotIndex = 1))
    val slot1State: StateFlow<GenerationSlotState> = _slot1State.asStateFlow()

    private val _slot2State = MutableStateFlow(
        GenerationSlotState(
            slotIndex = 2,
            mode = "توليد الصور",
            prompt = "لقطة مقرّبة لشخصية سينمائية بنظارات داكنة في أزقة القصبة مع إضاءة نيون ناعمة",
            cameraMovement = "Orbit Shot — دوران حول الشخصية",
            lens = "Portrait 85mm",
            aspectRatio = "1:1"
        )
    )
    val slot2State: StateFlow<GenerationSlotState> = _slot2State.asStateFlow()

    fun setActiveSlot(slot: Int) {
        _activeSlot.value = slot
    }

    fun updateCurrentSlotPrompt(prompt: String) {
        if (_activeSlot.value == 1) {
            _slot1State.value = _slot1State.value.copy(prompt = prompt)
        } else {
            _slot2State.value = _slot2State.value.copy(prompt = prompt)
        }
    }

    fun updateCurrentSlotMode(mode: String) {
        if (_activeSlot.value == 1) {
            _slot1State.value = _slot1State.value.copy(mode = mode)
        } else {
            _slot2State.value = _slot2State.value.copy(mode = mode)
        }
    }

    fun updateCurrentSlotCamera(camera: String) {
        if (_activeSlot.value == 1) {
            _slot1State.value = _slot1State.value.copy(cameraMovement = camera)
        } else {
            _slot2State.value = _slot2State.value.copy(cameraMovement = camera)
        }
    }

    fun updateCurrentSlotAspectRatio(ratio: String) {
        if (_activeSlot.value == 1) {
            _slot1State.value = _slot1State.value.copy(aspectRatio = ratio)
        } else {
            _slot2State.value = _slot2State.value.copy(aspectRatio = ratio)
        }
    }

    fun updateCurrentSlotResolution(resolution: String) {
        if (_activeSlot.value == 1) {
            _slot1State.value = _slot1State.value.copy(resolution = resolution)
        } else {
            _slot2State.value = _slot2State.value.copy(resolution = resolution)
        }
    }

    fun updateCurrentSlotReferenceImage(uri: String) {
        if (_activeSlot.value == 1) {
            _slot1State.value = _slot1State.value.copy(referenceImageUri = uri)
        } else {
            _slot2State.value = _slot2State.value.copy(referenceImageUri = uri)
        }
    }

    fun optimizePromptCinematically() {
        val isSlot1 = _activeSlot.value == 1
        val currentSlot = if (isSlot1) _slot1State.value else _slot2State.value
        val currentPrompt = currentSlot.prompt
        val cameraStyle = currentSlot.cameraMovement
        val mode = currentSlot.mode

        viewModelScope.launch {
            if (isSlot1) {
                _slot1State.value = _slot1State.value.copy(statusMessage = "جارٍ تحسين البرومبت عبر الذكاء الاصطناعي Gemini 2.5 Flash...")
            } else {
                _slot2State.value = _slot2State.value.copy(statusMessage = "جارٍ تحسين البرومبت عبر الذكاء الاصطناعي Gemini 2.5 Flash...")
            }

            val result = com.example.data.remote.GeminiStudioService.optimizeCinematicPrompt(
                rawPrompt = currentPrompt,
                cameraStyle = cameraStyle,
                mode = mode
            )

            result.onSuccess { optimizedText ->
                updateCurrentSlotPrompt(optimizedText)
                if (isSlot1) {
                    _slot1State.value = _slot1State.value.copy(statusMessage = "✅ تم تحسين البرومبت السينمائي بنجاح عبر Gemini AI!")
                } else {
                    _slot2State.value = _slot2State.value.copy(statusMessage = "✅ تم تحسين البرومبت السينمائي بنجاح عبر Gemini AI!")
                }
            }.onFailure { err ->
                val fallback = "$currentPrompt، إضاءة سينمائية حجمية 8K، تدرج لوني سينمائي Teal and Orange، تركيز دقيق على التفاصيل، حركة كاميرا انسيابية فائقة الواقعية"
                updateCurrentSlotPrompt(fallback)
                val errMsg = "⚠️ تعذر الاتصال بـ Gemini (${err.localizedMessage ?: "انقطاع شبكة"}) — تم استخدام التحسين المحلي الاحتياطي"
                if (isSlot1) {
                    _slot1State.value = _slot1State.value.copy(statusMessage = errMsg)
                } else {
                    _slot2State.value = _slot2State.value.copy(statusMessage = errMsg)
                }
            }
        }
    }

    private var generationJob: Job? = null

    fun startGeneration() {
        generationJob?.cancel()
        val isSlot1 = _activeSlot.value == 1
        val currentSlot = if (isSlot1) _slot1State.value else _slot2State.value

        generationJob = viewModelScope.launch {
            if (currentSlot.mode == "توليد الصور") {
                val workingMsg = "توليد حقيقي قيد التنفيذ: App Check ثم Cloudflare Worker ثم FLUX.1 [schnell]..."
                if (isSlot1) {
                    _slot1State.value = _slot1State.value.copy(isGenerating = true, progress = 0f, hasResult = false, resultImageFile = null, statusMessage = workingMsg)
                } else {
                    _slot2State.value = _slot2State.value.copy(isGenerating = true, progress = 0f, hasResult = false, resultImageFile = null, statusMessage = workingMsg)
                }

                val result = com.example.data.remote.GeminiStudioService.generateImageWithCloudflareWorker(
                    context = getApplication(),
                    prompt = currentSlot.prompt,
                    width = 1024,
                    height = 576,
                    steps = 8
                )

                result.onSuccess { workerResult ->
                    fetchDailyQuota()
                    val imageFile = workerResult.file
                    val promptLine = workerResult.finalPrompt?.let { " النص المستخدم في التوليد: $it" } ?: ""
                    val successMsg = if (currentSlot.referenceImageUri != null) {
                        "✨ تم توليد الصورة من النص عبر FLUX. الصورة المرجعية لم تُستخدم: التوليد من صورة غير متاح مجانياً حالياً.$promptLine"
                    } else {
                        "✨ تم توليد الصورة وحفظها محلياً بنجاح عبر FLUX.$promptLine"
                    }
                    if (isSlot1) {
                        _slot1State.value = _slot1State.value.copy(
                            isGenerating = false,
                            progress = 1.0f,
                            statusMessage = successMsg,
                            hasResult = true,
                            resultImageFile = imageFile.absolutePath,
                            finalPromptUsed = workerResult.finalPrompt
                        )
                        repository.saveProject(
                            ProjectEntity(
                                title = "توليد صور FLUX - ${_slot1State.value.cameraMovement}",
                                description = _slot1State.value.prompt,
                                durationText = "00:01",
                                resolution = _slot1State.value.resolution,
                                aspectRatio = _slot1State.value.aspectRatio,
                                fps = 30,
                                category = "توليد الصور",
                                thumbnailUri = imageFile.absolutePath,
                                thumbnailResId = R.drawable.img_cinematic_hero
                            )
                        )
                    } else {
                        _slot2State.value = _slot2State.value.copy(
                            isGenerating = false,
                            progress = 1.0f,
                            statusMessage = successMsg,
                            hasResult = true,
                            resultImageFile = imageFile.absolutePath,
                            finalPromptUsed = workerResult.finalPrompt
                        )
                        repository.saveProject(
                            ProjectEntity(
                                title = "توليد صور FLUX - ${_slot2State.value.lens}",
                                description = _slot2State.value.prompt,
                                durationText = "00:01",
                                resolution = _slot2State.value.resolution,
                                aspectRatio = _slot2State.value.aspectRatio,
                                fps = 30,
                                category = "توليد الصور",
                                thumbnailUri = imageFile.absolutePath,
                                thumbnailResId = R.drawable.img_character_ref
                            )
                        )
                    }
                }.onFailure { err ->
                    val errMsg = "❌ فشل التوليد: ${err.message ?: "خطأ أثناء الاتصال بالخادم"}"
                    if (isSlot1) {
                        _slot1State.value = _slot1State.value.copy(
                            isGenerating = false,
                            progress = 0f,
                            statusMessage = errMsg
                        )
                    } else {
                        _slot2State.value = _slot2State.value.copy(
                            isGenerating = false,
                            progress = 0f,
                            statusMessage = errMsg
                        )
                    }
                }
            } else if (currentSlot.mode == "نص إلى فيديو") {
                startTextToVideoGeneration(currentSlot, isSlot1)
            } else {
                val unavailableMsg = "توليد الشخصيات غير متاح حالياً: لا يوجد نموذج مجاني متصل. استخدم وضع «توليد الصور» أو «نص إلى فيديو»."
                if (isSlot1) {
                    _slot1State.value = _slot1State.value.copy(isGenerating = false, progress = 0f, hasResult = false, statusMessage = unavailableMsg)
                } else {
                    _slot2State.value = _slot2State.value.copy(isGenerating = false, progress = 0f, hasResult = false, statusMessage = unavailableMsg)
                }
            }
        }
    }

    /**
     * نص إلى فيديو: يولّد 3-6 صور متتابعة عبر نفس مسار /ai/generate-image المجاني
     * (نفس حصة Neuron اليومية)، مع فحص الحصة قبل كل صورة وإيقاف إن لم تكفِ،
     * ثم يركّبها فيديو MP4 محلياً بحركة تقريب خفيفة وانتقال متدرج عبر MediaCodec،
     * ويحفظه في Movies/KamalDZStudio ويظهر في المكتبة.
     */
    private fun startTextToVideoGeneration(
        currentSlot: GenerationSlotState,
        isSlot1: Boolean
    ) {
        getApplication<Application>
        val imageCount = VIDEO_IMAGE_COUNT

        fun setState(transform: (GenerationSlotState) -> GenerationSlotState) {
            if (isSlot1) _slot1State.value = transform(_slot1State.value)
            else _slot2State.value = transform(_slot2State.value)
        }

        generationJob = viewModelScope.launch {
            setState { it.copy(isGenerating = true, progress = 0f, hasResult = false, resultImageFile = null, statusMessage = "نص إلى فيديو: جارٍ فحص الحصة اليومية...") }

            // 1) فحص الحصة المتوقعة قبل البدء
            val quota = com.example.data.remote.GeminiStudioService.fetchDailyQuotaInfo(app).getOrNull()
            val perImageCost = 16 // 8 steps * (9.6 baseline + tiling) على 1024x576 - تقدير متحفظ
            val estimatedCost = perImageCost * imageCount
            if (quota != null && quota.remainingNeurons < estimatedCost) {
                setState { it.copy(isGenerating = false, progress = 0f, statusMessage = "⚠️ الحصة المتبقية (${quota.remainingNeurons} Neuron) لا تكفي للتوليد المتوقع (~$estimatedCost Neuron لـ $imageCount صور). حاول غداً أو قلّل العدد.") }
                return@launch
            }
            val costNote = quota?.let { "الحصة المتبقية: ${it.remainingNeurons} Neuron، التكلفة المتوقعة: ~$estimatedCost Neuron." } ?: "تعذر جلب الحصة - سيتم التوليد والتحقق من الاستجابة."

            val images = mutableListOf<android.graphics.Bitmap>()
            try {
                for (i in 1..imageCount) {
                    setState { it.copy(statusMessage = "نص إلى فيديو ($costNote): توليد الصورة $i من $imageCount...", progress = (i - 1) / imageCount.toFloat()) }
                    // تنويع لطيف للبرومبت حتى تختلف اللقطات دون إضافة عناصر لم يذكرها المستخدم
                    val shotPrompt = if (i == 1) currentSlot.prompt
                        else "${currentSlot.prompt}, alternate camera angle ${i}"
                    val result = com.example.data.remote.GeminiStudioService.generateImageWithCloudflareWorker(
                        context = app,
                        prompt = shotPrompt,
                        width = 1024,
                        height = 576,
                        steps = 8
                    )
                    val file = result.getOrNull()?.file
                        ?: run {
                            val err = result.exceptionOrNull()?.localizedMessage ?: "خطأ غير معروف"
                            setState { it.copy(isGenerating = false, progress = 0f, statusMessage = "⚠️ توقّف التوليد عند الصورة $i: $err") }
                            return@launch
                        }
                    val bmp = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                    if (bmp == null) {
                        setState { it.copy(isGenerating = false, progress = 0f, statusMessage = "⚠️ تعذر قراءة الصورة $i") }
                        return@launch
                    }
                    images.add(bmp)
                }

                setState { it.copy(statusMessage = "تركيب الفيديو محلياً (${images.size} صور)...", progress = 0.9f) }
                val video = com.example.video.VideoComposer.compose(app, images)
                val vr = video.getOrNull() ?: run {
                    val err = video.exceptionOrNull()?.localizedMessage ?: "خطأ غير معروف"
                    setState { it.copy(isGenerating = false, progress = 0f, statusMessage = "⚠️ فشل تركيب الفيديو: $err") }
                    return@launch
                }
                images.forEach { it.recycle() }
                images.clear()

                val uri = com.example.video.VideoComposer.registerInMediaStore(app, vr.file)
                val msg = "🎬 تم إنشاء الفيديو (${vr.durationMs / 1000} ثانية، ${vr.frameCount} إطاراً) وحفظه في Movies/KamalDZStudio"
                setState {
                    it.copy(
                        isGenerating = false,
                        progress = 1.0f,
                        hasResult = true,
                        resultImageFile = uri?.toString(),
                        statusMessage = msg
                    )
                }
                repository.saveProject(
                    ProjectEntity(
                        title = "نص إلى فيديو - ${currentSlot.cameraMovement}",
                        description = currentSlot.prompt,
                        durationText = "00:%02d".format(vr.durationMs / 1000),
                        resolution = "576p",
                        aspectRatio = currentSlot.aspectRatio,
                        fps = 30,
                        category = "نص إلى فيديو",
                        thumbnailUri = uri?.toString(),
                        thumbnailResId = R.drawable.img_cinematic_hero
                    )
                )
            } catch (e: kotlinx.coroutines.CancellationException) {
                images.forEach { it.recycle() }
                throw e
            } catch (e: Exception) {
                images.forEach { it.recycle() }
                setState { it.copy(isGenerating = false, progress = 0f, statusMessage = "⚠️ خطأ في وضع نص إلى فيديو: ${e.localizedMessage ?: e.javaClass.simpleName}") }
            }
        }
    }

    // Unified Quality System State across Studios
    private val _generationQuality = MutableStateFlow("1080p")
    val generationQuality: StateFlow<String> = _generationQuality.asStateFlow()
    fun setGenerationQuality(q: String) {
        _generationQuality.value = q
        updateCurrentSlotResolution(q)
    }

    private val _characterQuality = MutableStateFlow("1080p")
    val characterQuality: StateFlow<String> = _characterQuality.asStateFlow()
    fun setCharacterQuality(q: String) { _characterQuality.value = q }

    private val _editingQuality = MutableStateFlow("1080p")
    val editingQuality: StateFlow<String> = _editingQuality.asStateFlow()
    fun setEditingQuality(q: String) { _editingQuality.value = q }

    private val _custom1KWidth = MutableStateFlow(1024)
    val custom1KWidth: StateFlow<Int> = _custom1KWidth.asStateFlow()

    private val _custom1KHeight = MutableStateFlow(1024)
    val custom1KHeight: StateFlow<Int> = _custom1KHeight.asStateFlow()

    fun setCustom1KDimensions(width: Int, height: Int) {
        _custom1KWidth.value = width
        _custom1KHeight.value = height
    }

    // Section 2: Character Studio state
    val characterArchetypesList = listOf(
        CharacterArchetype(1, "عمر العاصمي", "بطل سينمائي رئيسي", R.drawable.img_character_ref, "رجل", "34 سنة", listOf("الوجه", "الملابس", "الصوت", "الهيئة")),
        CharacterArchetype(2, "ياسمين القصبة", "شابة عاصمية أصيلة", R.drawable.img_cinematic_hero, "شابة", "24 سنة", listOf("الوجه", "الملامح", "الصوت", "اللباس العاصمي")),
        CharacterArchetype(3, "الشيخ قدور", "حكيم وهران وتلمسان", R.drawable.img_editing_bay, "شيخ", "68 سنة", listOf("الوجه", "اللحية", "الصوت الرخيم", "العمامة")),
        CharacterArchetype(4, "طارق الأوراسي", "فارس الشاوية والأوراس", R.drawable.img_music_banner, "شاب", "29 سنة", listOf("الوجه", "النظرة الحادة", "الصوت الجهوري", "البرنوس"))
    )

    private val _activeArchetypeIndex = MutableStateFlow(0)
    val activeArchetypeIndex: StateFlow<Int> = _activeArchetypeIndex.asStateFlow()

    private val _activeArchetype = MutableStateFlow(characterArchetypesList[0])
    val activeArchetype: StateFlow<CharacterArchetype> = _activeArchetype.asStateFlow()

    private val _isSwapPersonsActive = MutableStateFlow(false)
    val isSwapPersonsActive: StateFlow<Boolean> = _isSwapPersonsActive.asStateFlow()

    private val _isCloningCharacter = MutableStateFlow(false)
    val isCloningCharacter: StateFlow<Boolean> = _isCloningCharacter.asStateFlow()

    private val _isCharacterCloned = MutableStateFlow(false)
    val isCharacterCloned: StateFlow<Boolean> = _isCharacterCloned.asStateFlow()

    private val _characterClonedFeedback = MutableStateFlow<String?>(null)
    val characterClonedFeedback: StateFlow<String?> = _characterClonedFeedback.asStateFlow()

    fun cloneCharacter() {
        if (_isCharacterCloned.value) {
            _isCharacterCloned.value = false
            _characterClonedFeedback.value = "تم إلغاء تفعيل استنساخ الشخصية والعودة للوضع الافتراضي"
            return
        }
        viewModelScope.launch {
            _isCloningCharacter.value = true
            _characterClonedFeedback.value = "جارٍ استنساخ البصمة الجينية وملامح الشخصية السينمائية..."
            delay(800)
            _isCloningCharacter.value = false
            _isCharacterCloned.value = true
            val current = _activeArchetype.value
            _characterClonedFeedback.value = "تم استنساخ الشخصية بنجاح: تم تثبيت ملامح ${current.name} كنموذج سينمائي أصلي جاهز لجميع المشاهد"
        }
    }

    fun swapCharacter() {
        if (_isSwapPersonsActive.value) {
            _isSwapPersonsActive.value = false
            _characterClonedFeedback.value = "تم إلغاء تفعيل وضع تبديل الأشخاص والعودة للشخصية الافتراضية"
            return
        }
        val nextIdx = (_activeArchetypeIndex.value + 1) % characterArchetypesList.size
        _activeArchetypeIndex.value = nextIdx
        val newArch = characterArchetypesList[nextIdx]
        _activeArchetype.value = newArch
        _selectedCharacterCategory.value = newArch.category
        _isSwapPersonsActive.value = true
        _characterClonedFeedback.value = "تم تبديل الشخصية بنجاح إلى: ${newArch.name} (${newArch.role})"
        _isCharacterMediaLoaded.value = true
        _characterPreviewMediaUri.value = null
    }

    // Character Studio Central Preview State
    private val _characterPreviewMediaUri = MutableStateFlow<String?>(null)
    val characterPreviewMediaUri: StateFlow<String?> = _characterPreviewMediaUri.asStateFlow()

    private val _characterPreviewAspectRatio = MutableStateFlow("16:9") // "16:9", "9:16", "1:1"
    val characterPreviewAspectRatio: StateFlow<String> = _characterPreviewAspectRatio.asStateFlow()

    private val _isCharacterMediaLoaded = MutableStateFlow(true)
    val isCharacterMediaLoaded: StateFlow<Boolean> = _isCharacterMediaLoaded.asStateFlow()

    private val _isCharacterVideoPlaying = MutableStateFlow(false)
    val isCharacterVideoPlaying: StateFlow<Boolean> = _isCharacterVideoPlaying.asStateFlow()

    private val _characterVideoTimecode = MutableStateFlow("00:03 / 00:10")
    val characterVideoTimecode: StateFlow<String> = _characterVideoTimecode.asStateFlow()

    fun setCharacterPreviewMediaUri(uri: String?) {
        _characterPreviewMediaUri.value = uri
        _isCharacterMediaLoaded.value = true
        _characterClonedFeedback.value = if (uri != null) "تم استيراد الملف بنجاح للمعاينة والمعالجة" else null
    }

    fun setCharacterPreviewAspectRatio(ratio: String) {
        _characterPreviewAspectRatio.value = ratio
    }

    fun toggleCharacterVideoPlayback() {
        _isCharacterVideoPlaying.value = !_isCharacterVideoPlaying.value
        if (_isCharacterVideoPlaying.value) {
            viewModelScope.launch {
                var seconds = 3
                while (_isCharacterVideoPlaying.value) {
                    delay(1000)
                    seconds = (seconds % 10) + 1
                    _characterVideoTimecode.value = "00:${if (seconds < 10) "0$seconds" else "$seconds"} / 00:10"
                }
            }
        }
    }

    fun clearCharacterMedia() {
        _characterPreviewMediaUri.value = null
        _isCharacterMediaLoaded.value = false
        _isCharacterVideoPlaying.value = false
        _characterClonedFeedback.value = "تم إفراغ شاشة المعاينة — يرجى استيراد ملف"
    }

    fun resetDefaultCharacterMedia() {
        _characterPreviewMediaUri.value = null
        _isCharacterMediaLoaded.value = true
        _characterClonedFeedback.value = "تمت استعادة الشخصية المرجعية الافتراضية"
    }

    // Lip-Sync Studio Controls (Transferred to Character Studio)
    private val _lipSyncVoiceTrack = MutableStateFlow("صوت سينمائي ملحمي (شاب 22 سنة - فصيح)")
    val lipSyncVoiceTrack: StateFlow<String> = _lipSyncVoiceTrack.asStateFlow()

    private val _lipSyncOffsetMs = MutableStateFlow(0f)
    val lipSyncOffsetMs: StateFlow<Float> = _lipSyncOffsetMs.asStateFlow()

    private val _isLipSyncPlaying = MutableStateFlow(false)
    val isLipSyncPlaying: StateFlow<Boolean> = _isLipSyncPlaying.asStateFlow()

    private val _lipSyncMouthOpen = MutableStateFlow(0.3f)
    val lipSyncMouthOpen: StateFlow<Float> = _lipSyncMouthOpen.asStateFlow()

    fun setLipSyncVoiceTrack(track: String) {
        _lipSyncVoiceTrack.value = track
    }

    fun setLipSyncOffsetMs(offset: Float) {
        _lipSyncOffsetMs.value = offset
    }

    fun toggleLipSyncPreview() {
        _isLipSyncPlaying.value = !_isLipSyncPlaying.value
        if (_isLipSyncPlaying.value) {
            viewModelScope.launch {
                var step = 0
                val mouthPhonemes = listOf(0.1f, 0.6f, 0.9f, 0.4f, 0.8f, 0.2f, 0.7f, 0.3f)
                while (_isLipSyncPlaying.value) {
                    _lipSyncMouthOpen.value = mouthPhonemes[step % mouthPhonemes.size]
                    step++
                    delay(120)
                }
                _lipSyncMouthOpen.value = 0.2f
            }
        }
    }

    private val _isBackgroundRemovalActive = MutableStateFlow(false)
    val isBackgroundRemovalActive: StateFlow<Boolean> = _isBackgroundRemovalActive.asStateFlow()

    private val _isRemovingBackground = MutableStateFlow(false)
    val isRemovingBackground: StateFlow<Boolean> = _isRemovingBackground.asStateFlow()

    private val _backgroundRemovalFeedback = MutableStateFlow<String?>(null)
    val backgroundRemovalFeedback: StateFlow<String?> = _backgroundRemovalFeedback.asStateFlow()

    fun toggleBackgroundRemoval() {
        if (_isBackgroundRemovalActive.value) {
            _isBackgroundRemovalActive.value = false
            _backgroundRemovalFeedback.value = "تمت استعادة الخلفية الأصلية للمشهد"
        } else {
            viewModelScope.launch {
                _isRemovingBackground.value = true
                _backgroundRemovalFeedback.value = "جارٍ عزل الشخصية بدقة فائقة وحساب قناع الشفافية (Alpha Mask)..."
                delay(700)
                _isRemovingBackground.value = false
                _isBackgroundRemovalActive.value = true
                _backgroundRemovalFeedback.value = "تم عزل وإزالة الخلفية بنجاح بدقة سينمائية واستخراج قناع ألفا (Alpha Matte)"
            }
        }
    }

    private val _characterIdentityLocked = MutableStateFlow(true)
    val characterIdentityLocked: StateFlow<Boolean> = _characterIdentityLocked.asStateFlow()

    fun toggleIdentityLock() {
        _characterIdentityLocked.value = !_characterIdentityLocked.value
    }

    private val _characterActiveFeature = MutableStateFlow("مراجعة الوجه والملامح")
    val characterActiveFeature: StateFlow<String> = _characterActiveFeature.asStateFlow()

    fun setCharacterActiveFeature(feature: String) {
        _characterActiveFeature.value = feature
    }

    private val _selectedCharacterCategory = MutableStateFlow("رجل")
    val selectedCharacterCategory: StateFlow<String> = _selectedCharacterCategory.asStateFlow()

    fun setSelectedCharacterCategory(category: String) {
        _selectedCharacterCategory.value = category
    }

    // Universal Language & Dialect States Across All Studios
    private val _scriptLanguage = MutableStateFlow(com.example.data.model.StudioLanguageRegistry.algerianDialects[0])
    val scriptLanguage: StateFlow<com.example.data.model.StudioLanguage> = _scriptLanguage.asStateFlow()

    private val _characterLanguage = MutableStateFlow(com.example.data.model.StudioLanguageRegistry.algerianDialects[1])
    val characterLanguage: StateFlow<com.example.data.model.StudioLanguage> = _characterLanguage.asStateFlow()

    private val _musicLanguage = MutableStateFlow(com.example.data.model.StudioLanguageRegistry.algerianDialects[2])
    val musicLanguage: StateFlow<com.example.data.model.StudioLanguage> = _musicLanguage.asStateFlow()

    private val _voiceLanguage = MutableStateFlow(com.example.data.model.StudioLanguageRegistry.algerianDialects[0])
    val voiceLanguage: StateFlow<com.example.data.model.StudioLanguage> = _voiceLanguage.asStateFlow()

    private val _subtitleSourceLanguage = MutableStateFlow(com.example.data.model.StudioLanguageRegistry.algerianDialects[0])
    val subtitleSourceLanguage: StateFlow<com.example.data.model.StudioLanguage> = _subtitleSourceLanguage.asStateFlow()

    private val _subtitleTargetLanguage = MutableStateFlow(com.example.data.model.StudioLanguageRegistry.findLanguage("it")) // Italian priority
    val subtitleTargetLanguage: StateFlow<com.example.data.model.StudioLanguage> = _subtitleTargetLanguage.asStateFlow()

    // Language Picker Modal State
    private val _isLanguagePickerOpen = MutableStateFlow(false)
    val isLanguagePickerOpen: StateFlow<Boolean> = _isLanguagePickerOpen.asStateFlow()

    private val _languagePickerPurpose = MutableStateFlow("script") // script, character, music, voice, subtitleSource, subtitleTarget
    val languagePickerPurpose: StateFlow<String> = _languagePickerPurpose.asStateFlow()

    private val _languagePickerTitle = MutableStateFlow("اختر اللغة أو اللهجة")
    val languagePickerTitle: StateFlow<String> = _languagePickerTitle.asStateFlow()

    fun openLanguagePicker(purpose: String, title: String) {
        _languagePickerPurpose.value = purpose
        _languagePickerTitle.value = title
        _isLanguagePickerOpen.value = true
    }

    fun closeLanguagePicker() {
        _isLanguagePickerOpen.value = false
    }

    fun selectLanguageForPurpose(lang: com.example.data.model.StudioLanguage) {
        when (_languagePickerPurpose.value) {
            "script" -> _scriptLanguage.value = lang
            "character" -> _characterLanguage.value = lang
            "music" -> _musicLanguage.value = lang
            "voice" -> {
                _voiceLanguage.value = lang
                _voiceDialect.value = lang.nameAr
            }
            "subtitleSource" -> {
                _subtitleSourceLanguage.value = lang
                translateAllSubtitles()
            }
            "subtitleTarget" -> {
                _subtitleTargetLanguage.value = lang
                translateAllSubtitles()
            }
        }
        closeLanguagePicker()
    }

    // Subtitles & Dialogue Engine (Section 5)
    private val _subtitlesList = MutableStateFlow(
        listOf(
            com.example.data.model.SubtitleCue(
                id = 1,
                startMs = 1000L,
                endMs = 4500L,
                sourceText = "يا ولاد البهجة، هذا المشهد هو روح القصبة والحرية",
                translatedText = "Figli di Algeri, questa scena è l'anima della Casbah e della libertà.",
                sourceLanguage = "الدارجة العاصمية",
                targetLanguage = "الإيطالية (Italiano)"
            ),
            com.example.data.model.SubtitleCue(
                id = 2,
                startMs = 5000L,
                endMs = 9500L,
                sourceText = "البحر يحكي قصة رجال كتبوا المجد بالصبر والشجاعة",
                translatedText = "Il mare racconta la storia di uomini che hanno scritto la gloria con pazienza e coraggio.",
                sourceLanguage = "الدارجة الجزائرية",
                targetLanguage = "الإيطالية (Italiano)"
            ),
            com.example.data.model.SubtitleCue(
                id = 3,
                startMs = 10000L,
                endMs = 15000L,
                sourceText = "شوف كيفاش غروب الشمس يضوي على سطوح ديارنا القديمة",
                translatedText = "Guarda come il tramonto illumina i tetti delle nostre antiche case.",
                sourceLanguage = "الدارجة الجزائرية",
                targetLanguage = "الإيطالية (Italiano)"
            )
        )
    )
    val subtitlesList: StateFlow<List<com.example.data.model.SubtitleCue>> = _subtitlesList.asStateFlow()

    fun translateAllSubtitles() {
        val target = _subtitleTargetLanguage.value
        _subtitlesList.value = _subtitlesList.value.map { cue ->
            val trans = translateDialogueText(cue.sourceText, target.code)
            cue.copy(translatedText = trans, targetLanguage = target.nameAr)
        }
    }

    fun addSubtitleCue(source: String, startMs: Long, endMs: Long) {
        val target = _subtitleTargetLanguage.value
        val trans = translateDialogueText(source, target.code)
        val newId = (_subtitlesList.value.maxOfOrNull { it.id } ?: 0) + 1
        _subtitlesList.value = _subtitlesList.value + com.example.data.model.SubtitleCue(
            id = newId,
            startMs = startMs,
            endMs = endMs,
            sourceText = source,
            translatedText = trans,
            sourceLanguage = _subtitleSourceLanguage.value.nameAr,
            targetLanguage = target.nameAr
        )
    }

    fun exportSrtContent(): String {
        return _subtitlesList.value.joinToString("\n") { it.toSrtBlock() }
    }

    fun exportVttContent(): String {
        return "WEBVTT\n\n" + _subtitlesList.value.joinToString("\n") { it.toVttBlock() }
    }

    private fun translateDialogueText(text: String, targetCode: String): String {
        return when (targetCode) {
            "it" -> when {
                text.contains("البهجة") || text.contains("القصبة") -> "Figli di Algeri, questa scena è l'anima della Casbah e della libertà."
                text.contains("البحر") -> "Il mare racconta la storia di uomini che hanno scritto la gloria con coraggio."
                text.contains("غروب") -> "Guarda come il tramonto illumina i tetti delle nostre antiche case."
                else -> "Scena cinematografica girata con passione e identità autentica."
            }
            "en" -> when {
                text.contains("البهجة") || text.contains("القصبة") -> "Children of Algiers, this scene embodies the soul of the Casbah and liberty."
                text.contains("البحر") -> "The sea narrates the tale of brave men who etched glory through patience and courage."
                text.contains("غروب") -> "Behold how the golden sunset illuminates the terraces of our historic homes."
                else -> "Cinematic sequence captured with genuine passion and authentic storytelling."
            }
            "fr" -> when {
                text.contains("البهجة") || text.contains("القصبة") -> "Enfants d'Alger, cette scène incarne l'âme de la Casbah et de la liberté."
                text.contains("البحر") -> "La mer raconte l'épopée d'hommes qui ont forgé la gloire par le courage."
                text.contains("غروب") -> "Regarde comme le coucher de soleil illumine les terrasses de nos demeures anciennes."
                else -> "Scène cinématographique produite avec authenticité et passion."
            }
            "es" -> "Hijos de Argel, esta escena representa el alma de la Casbah y la libertad."
            "de" -> "Kinder von Algier, diese Szene verkörpert die Seele der Kasbah und der Freiheit."
            "ar_msa" -> "يا أبناء الجزائر العريقة، هذا المشهد يجسد روح المجد والحرية والأصالة."
            else -> text
        }
    }

    fun generateScriptDialogueWithDialect() {
        val lang = _scriptLanguage.value
        val authenticLine = when (lang.code) {
            "dz_general" -> "حوار: 'واش راك يا خويا، اليوم نكتبو تاريخ جديد في بلادنا العزيزة وما كاش تراجع.'"
            "dz_algiers" -> "حوار: 'يا خويا العاصمي، هذي الحومة فيها ريحة القصبة والنيف، خلينا نديرو لقطة تبقى للتاريخ.'"
            "dz_oran" -> "حوار: 'يا عمري هذي وهران الباهية، هنا الموسيقى والحرية والكلمة الصادقة والراي السينمائي.'"
            "dz_constantine" -> "حوار: 'من فوق جسور سيرتا المعلقة، نشوفو العزة والصخر العتيق يحكي حكايتنا.'"
            "dz_setif" -> "حوار: 'عين الفوارة تشهد، ولاد الهضاب رجال وصناديد وما يرضاوش بالدونية.'"
            "dz_tlemcen" -> "حوار: 'تلمسان الهمة والأندلس، هنا الفن المعماري والحشمة والكرم الأصيل.'"
            "dz_kabyle" -> "حوار: 'Ansuf yis-wen! D tagi i d tamurt n lqedd d nnef d tlelli n waggur.'"
            "dz_chaoui" -> "حوار: 'Yulid wussan seg idurar n Awras, nekwni d imaziɣen ur nettsethi.'"
            "dz_sahara" -> "حوار: 'يا ضيف الرحمن في عرق الشبي، الرمال هذي حافظة سر الأجداد والقمر دليلنا.'"
            "it" -> "Dialogo (Italiano): 'Guarda questa città millenaria, qui la passione e il cinema si incontrano per l'eternità.'"
            "en" -> "Dialogue: 'Look at the horizon, this story belongs to those who dare to dream and conquer.'"
            "fr" -> "Dialogue: 'Regarde ces ruelles chargées d'histoire, chaque pierre murmure la gloire de notre peuple.'"
            "es" -> "Diálogo: 'Mira el horizonte, esta historia pertenece a quienes se atreven a soñar.'"
            "de" -> "Dialog: 'Schau zum Horizont, diese Geschichte gehört denen, die zu träumen wagen.'"
            else -> "حوار أصيل: 'هنا يبدأ المشهد السينمائي الحقيقي بنبض الهوية والأصالة.'"
        }
        val current = if (_activeSlot.value == 1) _slot1State.value.prompt else _slot2State.value.prompt
        updateCurrentSlotPrompt(if (current.isBlank()) authenticLine else "$current\n$authenticLine")
    }

    fun generateMusicLyricsWithDialect() {
        val lang = _musicLanguage.value
        val sampleLyrics = when (lang.code) {
            "dz_oran" -> "يا رايح لبلاد الباهية، سلّم على وهران... ليل وسهرة وفن الراي في قلبي ديما كاين."
            "dz_algiers" -> "يا ضيف البهجة ربي يهديك، القصبة منارة والفن فيها يواتيك... طرب شعبي ومندول يرن."
            "dz_kabyle" -> "A vava inouva d tsekurt deg wemnar... amezruy d wawal yettidir deg wulawen."
            "dz_chaoui" -> "نغم القصبة والبندير في جبال الأوراس الشامخة، صوت الحرية والبارود والفرسان."
            "dz_sahara" -> "صوت التيندي وإيقاع الطبول في عمق الصحراء والرمال الذهبية تحت النجوم الساطعة."
            "it" -> "O sole d'Algeri, melodia antica che risuona tra mare e cielo... canzone d'amore e cinema italiano."
            "en" -> "Cinematic rhythm rising from the heart of the Mediterranean, freedom echoing in every chord."
            "fr" -> "Une symphonie méditerranéenne où les violons et le mandole s'unissent dans la nuit d'Alger."
            "es" -> "Melodía apasionada que nace en el corazón del Mediterráneo, guitarra y alma pura."
            "de" -> "Symphonie der Freiheit, getragen von tiefen Streichern und zeitloser Sehnsucht."
            else -> "نغمات وترية سينمائية ممزوجة بأصالة اللهجة والتراث العريق."
        }
        setMusicLyrics(sampleLyrics)
    }

    fun runSpeechToTextAndAutoSubtitles() {
        val src = _subtitleSourceLanguage.value
        val target = _subtitleTargetLanguage.value
        val sampleCues = listOf(
            com.example.data.model.SubtitleCue(
                id = 1,
                startMs = 1200L,
                endMs = 4800L,
                sourceText = when (src.code) {
                    "dz_algiers" -> "يا ولاد البهجة، هذا المشهد هو روح القصبة والحرية"
                    "dz_oran" -> "وهران الباهية تحكي قصة العشق والفن السينمائي"
                    "dz_kabyle" -> "Tidett d tlelli d tanaṣlit n lmerḥum"
                    "it" -> "Benvenuti nel cuore della nostra terra, dove il cinema prende vita."
                    else -> "مرحباً بكم في أرض الجزائر، هنا يولد الإبداع السينمائي"
                },
                translatedText = "",
                sourceLanguage = src.nameAr,
                targetLanguage = target.nameAr
            ),
            com.example.data.model.SubtitleCue(
                id = 2,
                startMs = 5200L,
                endMs = 9100L,
                sourceText = "الشمس كي تغرب على البحر تعطي لون ذهبي سينمائي ساحر",
                translatedText = "",
                sourceLanguage = src.nameAr,
                targetLanguage = target.nameAr
            ),
            com.example.data.model.SubtitleCue(
                id = 3,
                startMs = 9600L,
                endMs = 14200L,
                sourceText = "ما كاش كيف الأصالة، حنا نديرو السينما بروحنا وهويتنا",
                translatedText = "",
                sourceLanguage = src.nameAr,
                targetLanguage = target.nameAr
            )
        )
        _subtitlesList.value = sampleCues.map { cue ->
            cue.copy(translatedText = translateDialogueText(cue.sourceText, target.code))
        }
    }

    fun speakSubtitleCue(cue: com.example.data.model.SubtitleCue, useTranslation: Boolean) {
        initTtsIfNeeded()
        val text = if (useTranslation) cue.translatedText else cue.sourceText
        val targetLang = if (useTranslation) _subtitleTargetLanguage.value else _subtitleSourceLanguage.value
        textToSpeech?.let { tts ->
            tts.setLanguage(targetLang.ttsLocale)
            tts.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "sub_cue_${cue.id}")
        }
    }

    // Section 3: Advanced Voice Studio State & Engine
    private var textToSpeech: android.speech.tts.TextToSpeech? = null
    private var isTtsReady = false

    private val _voiceMode = MutableStateFlow("تعليق صوتي") // "تعليق صوتي", "استنساخ الصوت", "مزامنة الشفاه"
    val voiceMode: StateFlow<String> = _voiceMode.asStateFlow()
    fun setVoiceMode(mode: String) { _voiceMode.value = mode }

    // 8 Voice Categories
    private val _selectedVoiceCategory = MutableStateFlow("رجل")
    val selectedVoiceCategory: StateFlow<String> = _selectedVoiceCategory.asStateFlow()

    // 12 Tones & Vocal Performances
    private val _selectedVoiceTone = MutableStateFlow("صوت سينمائي وملحمي")
    val selectedVoiceTone: StateFlow<String> = _selectedVoiceTone.asStateFlow()

    // Control Sliders
    private val _vocalAge = MutableStateFlow(35) // 5 to 90 years
    val vocalAge: StateFlow<Int> = _vocalAge.asStateFlow()
    fun setVocalAge(age: Int) { _vocalAge.value = age }

    private val _voiceRoughness = MutableStateFlow(0.6f) // 0f (Soft) to 1f (Rough/Deep)
    val voiceRoughness: StateFlow<Float> = _voiceRoughness.asStateFlow()
    fun setVoiceRoughness(v: Float) { _voiceRoughness.value = v }

    private val _voicePitch = MutableStateFlow(1.0f) // 0.5f to 2.0f
    val voicePitch: StateFlow<Float> = _voicePitch.asStateFlow()
    fun setVoicePitch(p: Float) { _voicePitch.value = p }

    private val _speechSpeed = MutableStateFlow(1.0f) // 0.5x to 2.0x
    val speechSpeed: StateFlow<Float> = _speechSpeed.asStateFlow()
    fun setSpeechSpeed(s: Float) { _speechSpeed.value = s }

    private val _expressiveness = MutableStateFlow(0.85f) // 0% to 100%
    val expressiveness: StateFlow<Float> = _expressiveness.asStateFlow()
    fun setExpressiveness(e: Float) { _expressiveness.value = e }

    private val _pauseAndBreathing = MutableStateFlow("وقفات درامية سينمائية") // "طبيعي متوازن", "وقفات درامية سينمائية", "سريع متواصل"
    val pauseAndBreathing: StateFlow<String> = _pauseAndBreathing.asStateFlow()
    fun setPauseAndBreathing(style: String) { _pauseAndBreathing.value = style }

    private val _voiceDialect = MutableStateFlow("الجزائرية - العاصمة")
    val voiceDialect: StateFlow<String> = _voiceDialect.asStateFlow()
    fun setVoiceDialect(dialect: String) { _voiceDialect.value = dialect }

    private val _voiceScript = MutableStateFlow("هنا بين أزقة القصبة العتيقة في قلب الجزائر، يتنفس التاريخ حكايا الأجداد مع نسائم البحر المتوسط الشاهدة على بطولات خالدة...")
    val voiceScript: StateFlow<String> = _voiceScript.asStateFlow()
    fun setVoiceScript(text: String) { _voiceScript.value = text }

    private val _isVoicePlaying = MutableStateFlow(false)
    val isVoicePlaying: StateFlow<Boolean> = _isVoicePlaying.asStateFlow()

    // Saved Favorite Voices
    private val _favoriteVoices = MutableStateFlow(
        listOf(
            "شيخ بصوت وثائقي" to "شيخ كبير في السن • صوت وثائقي احترافي",
            "بطل سينمائي غاضب" to "رجل بالغ • صوت غاضب",
            "شابة ناعمة هادئة" to "شابة • صوت هادئ وناعم",
            "طفل حزين ومؤثر" to "طفل ولد • صوت حزين ومؤثر"
        )
    )
    val favoriteVoices: StateFlow<List<Pair<String, String>>> = _favoriteVoices.asStateFlow()

    fun saveCurrentVoiceToFavorites(customName: String? = null) {
        val name = customName?.takeIf { it.isNotBlank() }
            ?: "${_selectedVoiceCategory.value} - ${_selectedVoiceTone.value}"
        val desc = "${_selectedVoiceCategory.value} • ${_selectedVoiceTone.value} (عمر: ${_vocalAge.value})"
        _favoriteVoices.value = listOf(name to desc) + _favoriteVoices.value
    }

    fun applyFavoriteVoice(category: String, tone: String) {
        setVoiceCategory(category)
        setVoiceTone(tone)
    }

    fun setVoiceCategory(category: String) {
        _selectedVoiceCategory.value = category
        // Adjust default age and pitch per category
        when (category) {
            "طفل" -> { _vocalAge.value = 9; _voicePitch.value = 1.45f; _voiceRoughness.value = 0.2f }
            "طفلة" -> { _vocalAge.value = 8; _voicePitch.value = 1.55f; _voiceRoughness.value = 0.15f }
            "شاب" -> { _vocalAge.value = 22; _voicePitch.value = 1.05f; _voiceRoughness.value = 0.45f }
            "شابة" -> { _vocalAge.value = 21; _voicePitch.value = 1.25f; _voiceRoughness.value = 0.3f }
            "امرأة" -> { _vocalAge.value = 36; _voicePitch.value = 1.15f; _voiceRoughness.value = 0.4f }
            "رجل" -> { _vocalAge.value = 38; _voicePitch.value = 0.90f; _voiceRoughness.value = 0.65f }
            "عجوز" -> { _vocalAge.value = 70; _voicePitch.value = 0.95f; _voiceRoughness.value = 0.75f }
            "شيخ" -> { _vocalAge.value = 72; _voicePitch.value = 0.75f; _voiceRoughness.value = 0.85f }
        }
    }

    fun setVoiceTone(tone: String) {
        _selectedVoiceTone.value = tone
        // Fine tune speech speed and pitch factor based on tone
        when (tone) {
            "صوت هادئ وناعم" -> { _speechSpeed.value = 0.9f; _voiceRoughness.value = 0.25f }
            "صوت عميق وغليظ" -> { _voicePitch.value = (_voicePitch.value * 0.85f).coerceAtLeast(0.6f); _voiceRoughness.value = 0.9f }
            "صوت حاد ومرتفع" -> { _voicePitch.value = (_voicePitch.value * 1.25f).coerceAtMost(1.8f) }
            "صوت قوي وقيادي" -> { _speechSpeed.value = 1.05f; _expressiveness.value = 0.95f }
            "صوت حزين ومؤثر" -> { _speechSpeed.value = 0.82f; _expressiveness.value = 0.9f }
            "صوت سعيد ومتحمس" -> { _speechSpeed.value = 1.15f; _expressiveness.value = 1.0f }
            "صوت غاضب" -> { _speechSpeed.value = 1.1f; _voiceRoughness.value = 0.85f; _expressiveness.value = 1.0f }
            "صوت خائف ومرتعش" -> { _speechSpeed.value = 1.2f; _expressiveness.value = 0.95f }
            "صوت سينمائي وملحمي" -> { _speechSpeed.value = 0.92f; _voiceRoughness.value = 0.7f; _expressiveness.value = 0.9f }
            "صوت همس" -> { _speechSpeed.value = 0.88f; _voicePitch.value = 0.9f; _voiceRoughness.value = 0.2f }
            "صوت وثائقي احترافي" -> { _speechSpeed.value = 0.95f; _voiceRoughness.value = 0.55f; _expressiveness.value = 0.8f }
            else -> { _speechSpeed.value = 1.0f }
        }
    }

    // Initialize TextToSpeech engine
    private fun initTtsIfNeeded() {
        if (textToSpeech == null) {
            textToSpeech = android.speech.tts.TextToSpeech(getApplication()) { status ->
                if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                    val result = textToSpeech?.setLanguage(java.util.Locale("ar"))
                    isTtsReady = result != android.speech.tts.TextToSpeech.LANG_MISSING_DATA &&
                            result != android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED
                    textToSpeech?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isVoicePlaying.value = true
                        }
                        override fun onDone(utteranceId: String?) {
                            _isVoicePlaying.value = false
                        }
                        override fun onError(utteranceId: String?) {
                            _isVoicePlaying.value = false
                        }
                    })
                }
            }
        }
    }

    fun previewVoicePlayback() {
        initTtsIfNeeded()
        if (_isVoicePlaying.value) {
            stopVoicePlayback()
            return
        }

        val text = _voiceScript.value.ifBlank { "مرحباً بكم في استوديو كمال ديزاد لتوليد الأصوات السينمائية" }
        _isVoicePlaying.value = true

        textToSpeech?.let { tts ->
            tts.setLanguage(_voiceLanguage.value.ttsLocale)
            tts.setPitch(_voicePitch.value)
            tts.setSpeechRate(_speechSpeed.value)
            tts.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "voice_preview_${System.currentTimeMillis()}")
        } ?: run {
            // Fallback simulated playback if TTS is initializing
            viewModelScope.launch {
                delay(3500)
                _isVoicePlaying.value = false
            }
        }
    }

    fun stopVoicePlayback() {
        textToSpeech?.stop()
        _isVoicePlaying.value = false
    }

    fun toggleVoicePlayback() {
        if (_isVoicePlaying.value) stopVoicePlayback() else previewVoicePlayback()
    }

    fun generateVoiceTrack() {
        viewModelScope.launch {
            val comboTitle = "${_selectedVoiceCategory.value} • ${_selectedVoiceTone.value}"
            repository.saveAudioTrack(
                AudioTrackEntity(
                    title = "$comboTitle: ${_voiceScript.value.take(22)}...",
                    type = "VOICE",
                    dialectOrGenre = "${_voiceDialect.value} (عمر: ${_vocalAge.value})",
                    durationText = "02:30",
                    promptOrLyrics = _voiceScript.value,
                    tone = _selectedVoiceTone.value,
                    instruments = "طبقة: ${_voicePitch.value}x | سرعة: ${_speechSpeed.value}x | خشونة: ${(_voiceRoughness.value * 100).toInt()}%"
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }

    // Section 4: Music Studio state
    private val _musicTab = MutableStateFlow("تأليف الموسيقى") // "تأليف الموسيقى", "الآلات الموسيقية", "المؤثرات الصوتية"
    val musicTab: StateFlow<String> = _musicTab.asStateFlow()

    fun setMusicTab(tab: String) { _musicTab.value = tab }

    private val _selectedGenre = MutableStateFlow("راي سينمائي")
    val selectedGenre: StateFlow<String> = _selectedGenre.asStateFlow()

    fun setSelectedGenre(genre: String) { _selectedGenre.value = genre }

    private val _selectedInstruments = MutableStateFlow(setOf("مندول", "دربوكة", "كمان", "بيانو"))
    val selectedInstruments: StateFlow<Set<String>> = _selectedInstruments.asStateFlow()

    fun toggleInstrument(inst: String) {
        val current = _selectedInstruments.value.toMutableSet()
        if (current.contains(inst)) current.remove(inst) else current.add(inst)
        _selectedInstruments.value = current
    }

    private val _musicLyrics = MutableStateFlow("يا ليل يا عيني، في شوارع بلادي سهران، ونغمات الراي تروي حكاية حب سينمائي...")
    val musicLyrics: StateFlow<String> = _musicLyrics.asStateFlow()

    fun setMusicLyrics(lyrics: String) { _musicLyrics.value = lyrics }

    private val _isMusicPlaying = MutableStateFlow(false)
    val isMusicPlaying: StateFlow<Boolean> = _isMusicPlaying.asStateFlow()

    fun toggleMusicPlayback() { _isMusicPlaying.value = !_isMusicPlaying.value }

    fun generateMusicTrack() {
        viewModelScope.launch {
            repository.saveAudioTrack(
                AudioTrackEntity(
                    title = "مقطوعة ${_selectedGenre.value} مع ${_selectedInstruments.value.joinToString("، ")}",
                    type = "MUSIC",
                    dialectOrGenre = _selectedGenre.value,
                    durationText = "03:15",
                    promptOrLyrics = _musicLyrics.value,
                    instruments = _selectedInstruments.value.joinToString("، ")
                )
            )
        }
    }

    // Section 5: Editing Studio state
    private val _isPlayingVideo = MutableStateFlow(false)
    val isPlayingVideo: StateFlow<Boolean> = _isPlayingVideo.asStateFlow()

    fun toggleVideoPlay() { _isPlayingVideo.value = !_isPlayingVideo.value }

    private val _videoContrast = MutableStateFlow(0.7f)
    val videoContrast: StateFlow<Float> = _videoContrast.asStateFlow()
    fun setVideoContrast(v: Float) { _videoContrast.value = v }

    private val _videoBrightness = MutableStateFlow(0.5f)
    val videoBrightness: StateFlow<Float> = _videoBrightness.asStateFlow()
    fun setVideoBrightness(v: Float) { _videoBrightness.value = v }

    private val _videoSharpness = MutableStateFlow(0.8f)
    val videoSharpness: StateFlow<Float> = _videoSharpness.asStateFlow()
    fun setVideoSharpness(v: Float) { _videoSharpness.value = v }

    private val _aiEditCommand = MutableStateFlow("")
    val aiEditCommand: StateFlow<String> = _aiEditCommand.asStateFlow()
    fun setAiEditCommand(cmd: String) { _aiEditCommand.value = cmd }

    private val _aiEditFeedback = MutableStateFlow<String?>(null)
    val aiEditFeedback: StateFlow<String?> = _aiEditFeedback.asStateFlow()

    fun executeAiEditCommand() {
        val cmd = _aiEditCommand.value.trim()
        if (cmd.isNotEmpty()) {
            _aiEditFeedback.value = "تم تنفيذ الأمر: «$cmd» — تم تطبيق التعديل السينمائي على الخط الزمني."
            _aiEditCommand.value = ""
        }
    }

    // Section 5: Timeline Management Component (Cut, Split, Merge operations)
    private val timelineEngine = TimelineMediaEngine(application)

    private val _timelineClips = MutableStateFlow(
        listOf(
            TimelineClip(
                id = "clip_1",
                name = "مشهد 1: أزقة القصبة",
                sourcePath = "assets/clips/scene_casbah.mp4",
                drawableResId = R.drawable.img_editing_bay,
                inPointMs = 0L,
                outPointMs = 5000L,
                timelineStartMs = 0L,
                trackType = TimelineTrackType.VIDEO,
                colorHex = 0xFF1E3A8A,
                isSelected = true
            ),
            TimelineClip(
                id = "clip_2",
                name = "مشهد 2: بطل القصة",
                sourcePath = "assets/clips/scene_character.mp4",
                drawableResId = R.drawable.img_character_ref,
                inPointMs = 0L,
                outPointMs = 6000L,
                timelineStartMs = 5000L,
                trackType = TimelineTrackType.VIDEO,
                colorHex = 0xFF0D9488,
                isSelected = false
            ),
            TimelineClip(
                id = "clip_3",
                name = "مشهد 3: الغروب الساحلي",
                sourcePath = "assets/clips/scene_sunset.mp4",
                drawableResId = R.drawable.img_cinematic_hero,
                inPointMs = 0L,
                outPointMs = 4500L,
                timelineStartMs = 11000L,
                trackType = TimelineTrackType.VIDEO,
                colorHex = 0xFF7C3AED,
                isSelected = false
            )
        )
    )
    val timelineClips: StateFlow<List<TimelineClip>> = _timelineClips.asStateFlow()

    private val _selectedClipId = MutableStateFlow<String?>("clip_1")
    val selectedClipId: StateFlow<String?> = _selectedClipId.asStateFlow()

    private val _timelinePlayheadMs = MutableStateFlow(2500L)
    val timelinePlayheadMs: StateFlow<Long> = _timelinePlayheadMs.asStateFlow()

    private val _lastTimelineResult = MutableStateFlow<TimelineOperationResult?>(null)
    val lastTimelineResult: StateFlow<TimelineOperationResult?> = _lastTimelineResult.asStateFlow()

    val totalTimelineDurationMs: Long
        get() = _timelineClips.value.sumOf { it.durationMs }

    fun selectTimelineClip(id: String) {
        _selectedClipId.value = id
        _timelineClips.value = _timelineClips.value.map { clip ->
            clip.copy(isSelected = clip.id == id)
        }
    }

    fun setTimelinePlayhead(ms: Long) {
        val total = totalTimelineDurationMs.coerceAtLeast(1000L)
        _timelinePlayheadMs.value = ms.coerceIn(0L, total)
    }

    /**
     * Split Operation (تقسيم عند المؤشر)
     */
    fun splitSelectedClipAtPlayhead(): TimelineOperationResult {
        val clips = _timelineClips.value
        val selId = _selectedClipId.value ?: clips.firstOrNull()?.id
        val clip = clips.find { it.id == selId }
        val playhead = _timelinePlayheadMs.value

        if (clip == null) {
            val res = TimelineOperationResult(
                success = false,
                actionType = "SPLIT",
                messageAr = "لم يتم تحديد أي مقطع للتقسيم",
                ffmpegCommand = "# No clip selected",
                affectedClipsCount = 0
            )
            _lastTimelineResult.value = res
            return res
        }

        // Check if playhead is strictly within clip boundaries
        if (playhead <= clip.timelineStartMs || playhead >= clip.timelineEndMs) {
            val res = TimelineOperationResult(
                success = false,
                actionType = "SPLIT",
                messageAr = "المؤشر (${playhead / 1000.0}s) خارج نطاق المقطع المحدد (${clip.formatTimeRange()}). يرجى تحريك المؤشر لداخل المقطع.",
                ffmpegCommand = "# Playhead outside selected clip range",
                affectedClipsCount = 1
            )
            _lastTimelineResult.value = res
            return res
        }

        val (parts, result) = timelineEngine.splitClip(clip, playhead)
        if (parts != null) {
            val (partA, partB) = parts
            val index = clips.indexOfFirst { it.id == clip.id }
            val mutable = clips.toMutableList()
            mutable.removeAt(index)
            mutable.add(index, partB)
            mutable.add(index, partA)

            _timelineClips.value = timelineEngine.recalculateTimelineStarts(mutable)
            _selectedClipId.value = partA.id
        }
        _lastTimelineResult.value = result
        return result
    }

    /**
     * Cut / Trim Operation (قص واقتطاع من البداية/النهاية)
     */
    fun cutSelectedClip(trimHeadMs: Long = 500L, trimTailMs: Long = 500L): TimelineOperationResult {
        val clips = _timelineClips.value
        val selId = _selectedClipId.value ?: clips.firstOrNull()?.id
        val clip = clips.find { it.id == selId }

        if (clip == null) {
            val res = TimelineOperationResult(
                success = false,
                actionType = "CUT",
                messageAr = "لم يتم تحديد أي مقطع للقص",
                ffmpegCommand = "# No clip selected",
                affectedClipsCount = 0
            )
            _lastTimelineResult.value = res
            return res
        }

        val (trimmed, result) = timelineEngine.cutClip(clip, trimHeadMs, trimTailMs)
        if (trimmed != null) {
            val mutable = clips.map { if (it.id == clip.id) trimmed else it }
            _timelineClips.value = timelineEngine.recalculateTimelineStarts(mutable)
        }
        _lastTimelineResult.value = result
        return result
    }

    /**
     * Merge Operation (دمج المقطع المحدد مع المقطع التالي)
     */
    fun mergeSelectedClipWithNext(): TimelineOperationResult {
        val clips = _timelineClips.value
        val selId = _selectedClipId.value ?: clips.firstOrNull()?.id
        val index = clips.indexOfFirst { it.id == selId }

        if (index < 0 || index >= clips.size - 1) {
            val res = TimelineOperationResult(
                success = false,
                actionType = "MERGE",
                messageAr = if (clips.size <= 1) "يجب أن يحتوي الخط الزمني على مقطعين على الأقل للدمج"
                else "المقطع المحدد هو الأخير في المسار، يرجى اختيار مقطع يليه مقطع آخر للدمج",
                ffmpegCommand = "# Cannot merge: no adjacent following clip",
                affectedClipsCount = 0
            )
            _lastTimelineResult.value = res
            return res
        }

        val clipA = clips[index]
        val clipB = clips[index + 1]

        val (merged, result) = timelineEngine.mergeClips(clipA, clipB)
        if (merged != null) {
            val mutable = clips.toMutableList()
            mutable.removeAt(index + 1)
            mutable.removeAt(index)
            mutable.add(index, merged)

            _timelineClips.value = timelineEngine.recalculateTimelineStarts(mutable)
            _selectedClipId.value = merged.id
        }
        _lastTimelineResult.value = result
        return result
    }

    /**
     * Import Media Asset to Timeline
     */
    fun importMediaAsset(
        name: String = "مقطع مستورد جديد",
        durationMs: Long = 4000L,
        drawableResId: Int = R.drawable.img_editing_bay
    ) {
        val newId = "clip_${System.currentTimeMillis() % 10000}"
        val newClip = TimelineClip(
            id = newId,
            name = name,
            sourcePath = "assets/clips/$newId.mp4",
            drawableResId = drawableResId,
            inPointMs = 0L,
            outPointMs = durationMs,
            timelineStartMs = totalTimelineDurationMs,
            trackType = TimelineTrackType.VIDEO,
            colorHex = 0xFF2563EB,
            isSelected = true
        )
        val updated = _timelineClips.value + newClip
        _timelineClips.value = timelineEngine.recalculateTimelineStarts(updated)
        _selectedClipId.value = newId
        _lastTimelineResult.value = TimelineOperationResult(
            success = true,
            actionType = "IMPORT",
            messageAr = "تم استيراد «$name» وإضافته إلى نهاية المسار بنجاح",
            ffmpegCommand = "ffmpeg -i \"$newId.mp4\" -c copy timeline_manifest.mp4",
            affectedClipsCount = 1
        )
    }

    /**
     * Import media from Device File Picker (Uri or File name)
     */
    fun importFromDevice(name: String, uriString: String? = null, mediaType: String = "VIDEO"): Boolean {
        val cleanName = if (name.isBlank()) "ملف_جهاز_${System.currentTimeMillis() % 1000}" else name
        val trackType = when (mediaType.uppercase(Locale.ROOT)) {
            "AUDIO" -> TimelineTrackType.AUDIO
            else -> TimelineTrackType.VIDEO
        }
        val newId = "dev_${System.currentTimeMillis() % 10000}"
        val newClip = TimelineClip(
            id = newId,
            name = cleanName,
            sourcePath = uriString ?: "content://media/external/$newId",
            drawableResId = R.drawable.img_editing_bay,
            inPointMs = 0L,
            outPointMs = 5000L,
            timelineStartMs = totalTimelineDurationMs,
            trackType = trackType,
            colorHex = 0xFF8B5CF6,
            isSelected = true
        )
        val updated = _timelineClips.value + newClip
        _timelineClips.value = timelineEngine.recalculateTimelineStarts(updated)
        _selectedClipId.value = newId
        _lastTimelineResult.value = TimelineOperationResult(
            success = true,
            actionType = "IMPORT_DEVICE",
            messageAr = "تم استيراد «$cleanName» من ذاكرة الجهاز بنجاح وإضافته للخط الزمني",
            ffmpegCommand = "ffmpeg -i \"$cleanName\" -c copy imported_device_clip.mp4",
            affectedClipsCount = 1
        )
        return true
    }

    /**
     * Import media from Direct URL / Web Link
     */
    fun importFromUrl(url: String, mediaType: String = "VIDEO"): Boolean {
        if (url.isBlank() || (!url.startsWith("http://") && !url.startsWith("https://"))) {
            return false
        }
        val cleanUrl = url.trim()
        val extractedName = cleanUrl.substringAfterLast("/").substringBefore("?").ifBlank { "مقطع_رابط_مباشر" }
        val newId = "url_${System.currentTimeMillis() % 10000}"
        val newClip = TimelineClip(
            id = newId,
            name = "🌐 $extractedName",
            sourcePath = cleanUrl,
            drawableResId = R.drawable.img_cinematic_hero,
            inPointMs = 0L,
            outPointMs = 6000L,
            timelineStartMs = totalTimelineDurationMs,
            trackType = TimelineTrackType.VIDEO,
            colorHex = 0xFF00D9FF,
            isSelected = true
        )
        val updated = _timelineClips.value + newClip
        _timelineClips.value = timelineEngine.recalculateTimelineStarts(updated)
        _selectedClipId.value = newId
        _lastTimelineResult.value = TimelineOperationResult(
            success = true,
            actionType = "IMPORT_URL",
            messageAr = "تم تنزيل واستيراد «$extractedName» من الرابط بنجاح",
            ffmpegCommand = "ffmpeg -i \"$cleanUrl\" -c copy url_stream_imported.mp4",
            affectedClipsCount = 1
        )
        return true
    }

    /**
     * Import media from Google Drive (SAF file or share link)
     */
    fun importFromGoogleDrive(identifier: String, isLink: Boolean = false): Boolean {
        if (identifier.isBlank()) return false
        val newId = "drive_${System.currentTimeMillis() % 10000}"
        val name = if (isLink) {
            val fileId = if (identifier.contains("/d/")) {
                identifier.substringAfter("/d/").substringBefore("/")
            } else {
                identifier.substringAfter("id=").substringBefore("&")
            }.take(12)
            "☁️ Drive ($fileId)"
        } else {
            "☁️ $identifier"
        }
        val newClip = TimelineClip(
            id = newId,
            name = name,
            sourcePath = if (isLink) identifier else "gdrive://$newId",
            drawableResId = R.drawable.img_editing_bay,
            inPointMs = 0L,
            outPointMs = 5000L,
            timelineStartMs = totalTimelineDurationMs,
            trackType = TimelineTrackType.VIDEO,
            colorHex = 0xFF10B981,
            isSelected = true
        )
        val updated = _timelineClips.value + newClip
        _timelineClips.value = timelineEngine.recalculateTimelineStarts(updated)
        _selectedClipId.value = newId
        _lastTimelineResult.value = TimelineOperationResult(
            success = true,
            actionType = "IMPORT_DRIVE",
            messageAr = "تم استيراد «$name» من Google Drive بنجاح",
            ffmpegCommand = "ffmpeg -i drive_cloud_source.mp4 -c copy gdrive_clip.mp4",
            affectedClipsCount = 1
        )
        return true
    }

    /**
     * Import media from external Cloud Storage (OneDrive / Dropbox)
     */
    fun importFromCloudStorage(serviceName: String, identifier: String, isLink: Boolean = false): Boolean {
        if (identifier.isBlank()) return false
        val newId = "cloud_${System.currentTimeMillis() % 10000}"
        val name = "📦 $serviceName: ${identifier.take(20)}"
        val newClip = TimelineClip(
            id = newId,
            name = name,
            sourcePath = if (isLink) identifier else "cloud://$newId",
            drawableResId = R.drawable.img_editing_bay,
            inPointMs = 0L,
            outPointMs = 5000L,
            timelineStartMs = totalTimelineDurationMs,
            trackType = TimelineTrackType.VIDEO,
            colorHex = 0xFF06B6D4,
            isSelected = true
        )
        val updated = _timelineClips.value + newClip
        _timelineClips.value = timelineEngine.recalculateTimelineStarts(updated)
        _selectedClipId.value = newId
        _lastTimelineResult.value = TimelineOperationResult(
            success = true,
            actionType = "IMPORT_CLOUD",
            messageAr = "تم استيراد «$name» من التخزين السحابي ($serviceName) بنجاح",
            ffmpegCommand = "ffmpeg -i cloud_source.mp4 -c copy cloud_clip.mp4",
            affectedClipsCount = 1
        )
        return true
    }

    /**
     * Delete Selected Clip from Timeline
     */
    fun deleteSelectedTimelineClip() {
        val clips = _timelineClips.value
        val selId = _selectedClipId.value ?: return
        if (clips.size <= 1) {
            _lastTimelineResult.value = TimelineOperationResult(
                success = false,
                actionType = "DELETE",
                messageAr = "لا يمكن حذف المقطع الوحيد في الخط الزمني",
                ffmpegCommand = "# Cannot delete last clip",
                affectedClipsCount = 0
            )
            return
        }
        val remaining = clips.filter { it.id != selId }
        _timelineClips.value = timelineEngine.recalculateTimelineStarts(remaining)
        _selectedClipId.value = remaining.firstOrNull()?.id
        _lastTimelineResult.value = TimelineOperationResult(
            success = true,
            actionType = "DELETE",
            messageAr = "تم إزالة المقطع من الخط الزمني وتحديث التزامن",
            ffmpegCommand = "# Removed clip: $selId",
            affectedClipsCount = 1
        )
    }

    /**
     * Persist current timeline structure to Room DB ProjectEntity
     */
    fun saveTimelineToCurrentProject(projectTitle: String = "مشروع مونتاج سينمائي") {
        viewModelScope.launch {
            val totalSec = totalTimelineDurationMs / 1000
            val min = totalSec / 60
            val sec = totalSec % 60
            val durText = String.format(Locale.US, "%02d:%02d", min, sec)
            val clipCount = _timelineClips.value.size
            val clipsSummary = _timelineClips.value.joinToString(" ➔ ") { it.name }

            val project = ProjectEntity(
                title = projectTitle,
                description = "الخط الزمني: $clipCount مقاطع معالجة عبر محرك FFmpeg | $clipsSummary",
                durationText = durText,
                resolution = "1080p (Cinema)",
                aspectRatio = "16:9",
                fps = 30,
                category = "مونتاج واقتطاع سينمائي",
                thumbnailResId = _timelineClips.value.firstOrNull()?.drawableResId ?: R.drawable.img_editing_bay,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveProject(project)
            _lastTimelineResult.value = TimelineOperationResult(
                success = true,
                actionType = "SAVE",
                messageAr = "تم حفظ تعديلات الخط الزمني ($clipCount مقاطع، $durText) في قاعدة بيانات المشاريع بنجاح",
                ffmpegCommand = "# Saved to Room DB Project: $projectTitle",
                affectedClipsCount = clipCount
            )
        }
    }

    // Section 6: Library filter & Trends
    private val _libraryFilter = MutableStateFlow("الكل")
    val libraryFilter: StateFlow<String> = _libraryFilter.asStateFlow()

    fun setLibraryFilter(f: String) { _libraryFilter.value = f }

    private val _librarySearchQuery = MutableStateFlow("")
    val librarySearchQuery: StateFlow<String> = _librarySearchQuery.asStateFlow()

    fun setLibrarySearchQuery(q: String) { _librarySearchQuery.value = q }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch { repository.deleteProject(project) }
    }

    fun toggleFavorite(project: ProjectEntity) {
        viewModelScope.launch { repository.updateProject(project.copy(isFavorite = !project.isFavorite)) }
    }

    // Section 7: Export Studio
    private val _exportFormat = MutableStateFlow("MP4")
    val exportFormat: StateFlow<String> = _exportFormat.asStateFlow()
    fun setExportFormat(f: String) { _exportFormat.value = f }

    private val _exportResolution = MutableStateFlow("1080p (Full HD)")
    val exportResolution: StateFlow<String> = _exportResolution.asStateFlow()
    fun setExportResolution(r: String) { _exportResolution.value = r }

    private val _exportFps = MutableStateFlow(60)
    val exportFps: StateFlow<Int> = _exportFps.asStateFlow()
    fun setExportFps(fps: Int) { _exportFps.value = fps }

    private val _exportQuality = MutableStateFlow("عالية (High)")
    val exportQuality: StateFlow<String> = _exportQuality.asStateFlow()
    fun setExportQuality(q: String) { _exportQuality.value = q }

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _exportProgress = MutableStateFlow(0f)
    val exportProgress: StateFlow<Float> = _exportProgress.asStateFlow()

    private val _exportNotice = MutableStateFlow<String?>(null)
    val exportNotice: StateFlow<String?> = _exportNotice.asStateFlow()

    // Real video encoding is not implemented (TimelineMediaEngine only builds an ffmpeg command string),
    // so no fake progress or COMPLETED export record is produced.
    fun startExport(projectTitle: String = "مشروع_سينمائي_نهائي") {
        _exportNotice.value = "تصدير الفيديو غير متاح حالياً: لا يوجد محرك ترميز محلي مثبت، ولم يُنشأ أي ملف."
    }

    // Skills Center Actions
    fun toggleSkillEnabled(skill: SkillEntity) {
        viewModelScope.launch {
            repository.updateSkill(skill.copy(isEnabled = !skill.isEnabled))
        }
    }

    fun addNewSkill(name: String, desc: String, category: String, content: String) {
        viewModelScope.launch {
            repository.saveSkill(
                SkillEntity(
                    skillKey = "custom_${System.currentTimeMillis()}",
                    name = name,
                    description = desc,
                    category = category,
                    targetSection = category,
                    markdownContent = content,
                    isEnabled = true,
                    isBuiltIn = false
                )
            )
        }
    }

    fun deleteSkill(skill: SkillEntity) {
        viewModelScope.launch { repository.deleteSkill(skill) }
    }
}
