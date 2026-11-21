package com.example.data.model

import java.util.Locale

enum class LanguageGroup(val titleAr: String) {
    ALGERIAN("اللهجات الجزائرية (10)"),
    ARABIC("الدول واللهجات العربية (16)"),
    INTERNATIONAL("اللغات الأجنبية (10)")
}

data class StudioLanguage(
    val code: String,
    val nameAr: String,
    val nameEn: String,
    val group: LanguageGroup,
    val countryOrRegion: String,
    val ttsLocale: Locale,
    val supportBadge: String = "محرك صوت + ذكاء اصطناعي",
    val sampleGreeting: String,
    val isPriority: Boolean = false
) {
    val flagEmoji: String
        get() = when (group) {
            LanguageGroup.ALGERIAN -> "🇩🇿"
            LanguageGroup.ARABIC -> when {
                code.contains("tunisia") -> "🇹🇳"
                code.contains("morocco") -> "🇲🇦"
                code.contains("libya") -> "🇱🇾"
                code.contains("egypt") -> "🇪🇬"
                code.contains("syria") -> "🇸🇾"
                code.contains("lebanon") -> "🇱🇧"
                code.contains("saudi") -> "🇸🇦"
                code.contains("uae") -> "🇦🇪"
                code.contains("iraq") -> "🇮🇶"
                code.contains("jordan") -> "🇯🇴"
                code.contains("palestine") -> "🇵🇸"
                code.contains("yemen") -> "🇾🇪"
                code.contains("sudan") -> "🇸🇩"
                else -> "🇸🇦"
            }
            LanguageGroup.INTERNATIONAL -> when (code) {
                "it" -> "🇮🇹"
                "en" -> "🇬🇧"
                "fr" -> "🇫🇷"
                "es" -> "🇪🇸"
                "de" -> "🇩🇪"
                "pt" -> "🇵🇹"
                "tr" -> "🇹🇷"
                "ru" -> "🇷🇺"
                "zh" -> "🇨🇳"
                "ja" -> "🇯🇵"
                else -> "🌐"
            }
        }

    val nameNative: String
        get() = nameEn

    val regionDescription: String
        get() = countryOrRegion
}

object StudioLanguageRegistry {

    // 1. اللهجات الجزائرية (10 لهجات)
    val algerianDialects = listOf(
        StudioLanguage(
            code = "dz_general",
            nameAr = "الدارجة الجزائرية العامة",
            nameEn = "General Algerian Darija",
            group = LanguageGroup.ALGERIAN,
            countryOrRegion = "الجزائر",
            ttsLocale = Locale("ar", "DZ"),
            supportBadge = "توليد كامل + نبرة صوتية",
            sampleGreeting = "واش راكم يا جماعة الخير، مرحباً بيكم في الاستوديو السينمائي"
        ),
        StudioLanguage(
            code = "dz_algiers",
            nameAr = "العاصمية (البهجة)",
            nameEn = "Algiers Dialect",
            group = LanguageGroup.ALGERIAN,
            countryOrRegion = "الجزائر العاصمة والوسط",
            ttsLocale = Locale("ar", "DZ"),
            supportBadge = "توليد كامل + شعبي وسينما",
            sampleGreeting = "صباح الخير عليكم يا ولاد البهجة، هنا في قلب القصبة"
        ),
        StudioLanguage(
            code = "dz_oran",
            nameAr = "الوهرانية (الغرب الجزائري)",
            nameEn = "Oran Dialect",
            group = LanguageGroup.ALGERIAN,
            countryOrRegion = "وهران والغرب",
            ttsLocale = Locale("ar", "DZ"),
            supportBadge = "توليد كامل + راي وسينما",
            sampleGreeting = "كيدايرين حبابنا، وهران الباهية ترحب بيكم بنغمات الراي"
        ),
        StudioLanguage(
            code = "dz_constantine",
            nameAr = "القسنطينية (الشرق الجزائري)",
            nameEn = "Constantine Dialect",
            group = LanguageGroup.ALGERIAN,
            countryOrRegion = "قسنطينة ومدينة الجسور",
            ttsLocale = Locale("ar", "DZ"),
            supportBadge = "توليد كامل + مالوف وتراث",
            sampleGreeting = "مساء النور من مدينة الجسور المعلقة قسنطينة العريقة"
        ),
        StudioLanguage(
            code = "dz_setif",
            nameAr = "السطايفية (الهضاب العليا)",
            nameEn = "Setif Dialect",
            group = LanguageGroup.ALGERIAN,
            countryOrRegion = "سطيف وعين الفوارة",
            ttsLocale = Locale("ar", "DZ"),
            supportBadge = "توليد كامل + سطايفي أصيل",
            sampleGreeting = "مرحبابكم عند ناس سطيف العالي ناس النخوة والكرم"
        ),
        StudioLanguage(
            code = "dz_tlemcen",
            nameAr = "التلمسانية (عاصمة الزيانيين)",
            nameEn = "Tlemcen Dialect",
            group = LanguageGroup.ALGERIAN,
            countryOrRegion = "تلمسان وحضارة الأندلس",
            ttsLocale = Locale("ar", "DZ"),
            supportBadge = "توليد سينمائي وتاريخي",
            sampleGreeting = "أهلاً وسهلاً بكم في حاضرة الفن والتاريخ تلمسان"
        ),
        StudioLanguage(
            code = "dz_kabyle",
            nameAr = "القبائلية (Taqbaylit)",
            nameEn = "Kabyle Amazigh",
            group = LanguageGroup.ALGERIAN,
            countryOrRegion = "منطقة القبائل وجبال جرجرة",
            ttsLocale = Locale("ar", "DZ"),
            supportBadge = "صوت أمازيغي + ترجمة",
            sampleGreeting = "Ansuf yis-wen g ustudyu n Kamal DZ Studio 2027"
        ),
        StudioLanguage(
            code = "dz_chaoui",
            nameAr = "الشاوية (Tachawit)",
            nameEn = "Chaoui Amazigh",
            group = LanguageGroup.ALGERIAN,
            countryOrRegion = "الأوراس الأشم وباتنة",
            ttsLocale = Locale("ar", "DZ"),
            supportBadge = "صوت أوراسي + تراث",
            sampleGreeting = "Mrehba s yis-wen deg udrar n L'Awras d Kamal DZ"
        ),
        StudioLanguage(
            code = "dz_tuareg",
            nameAr = "التارقية (Tamahaq)",
            nameEn = "Tuareg Saharan",
            group = LanguageGroup.ALGERIAN,
            countryOrRegion = "تمنراست وطاسيلي ناجر",
            ttsLocale = Locale("ar", "DZ"),
            supportBadge = "صوت صحراوي أصيل",
            sampleGreeting = "Oywan s Kamal DZ Studio g Tassili d Hoggar"
        ),
        StudioLanguage(
            code = "dz_sahara",
            nameAr = "الصحراوية الجزائرية",
            nameEn = "Algerian Saharan",
            group = LanguageGroup.ALGERIAN,
            countryOrRegion = "ورقلة، غرداية، بشار والجنوب",
            ttsLocale = Locale("ar", "DZ"),
            supportBadge = "توليد كامل + وثائقي",
            sampleGreeting = "يا هلا ومسهلا بأهل الجنوب والصحراء الشاسعة والرمال الذهبية"
        )
    )

    // 2. لغات ولهجات الدول العربية (16 لغة ولهجة)
    val arabicDialects = listOf(
        StudioLanguage("ar_msa", "العربية الفصحى المعاصرة", "Modern Standard Arabic", LanguageGroup.ARABIC, "العالم العربي", Locale("ar"), "دعم صوتي قياسي كامل", "مرحباً بكم في الصرح السينمائي الأكبر للذكاء الاصطناعي"),
        StudioLanguage("ar_dz", "الجزائرية", "Algerian Arabic", LanguageGroup.ARABIC, "الجزائر", Locale("ar", "DZ"), "دعم كامل محلي", "مرحبا بيكم في استوديو كمال ديزاد"),
        StudioLanguage("ar_tn", "التونسية", "Tunisian Arabic", LanguageGroup.ARABIC, "تونس", Locale("ar", "TN"), "دعم لهجة + صوت", "عسلامة، نهاركم زين في الاستوديو السينمائي"),
        StudioLanguage("ar_ma", "المغربية", "Moroccan Darija", LanguageGroup.ARABIC, "المغرب", Locale("ar", "MA"), "دعم لهجة + صوت", "السلام عليكم، مرحبا بيكم معنا في الإبداع"),
        StudioLanguage("ar_ly", "الليبية", "Libyan Arabic", LanguageGroup.ARABIC, "ليبيا", Locale("ar", "LY"), "دعم لهجة + صوت", "مرحبتين بيكم وبكل المبدعين والسينمائيين"),
        StudioLanguage("ar_eg", "المصرية", "Egyptian Arabic", LanguageGroup.ARABIC, "مصر", Locale("ar", "EG"), "دعم سينمائي ودرامي كامل", "أهلاً بيكم في عالم السينما والإخراج بالذكاء الاصطناعي"),
        StudioLanguage("ar_sy", "السورية", "Syrian Levantine", LanguageGroup.ARABIC, "سوريا", Locale("ar", "SY"), "دبلجة ودراما كاملة", "يسعد أوقاتكم بكل خير مع أحلى المشاهد السينمائية"),
        StudioLanguage("ar_lb", "اللبنانية", "Lebanese Levantine", LanguageGroup.ARABIC, "لبنان", Locale("ar", "LB"), "دعم درامي وصوتي", "هاي للكل، نورتوا استوديو السينما والموسيقى"),
        StudioLanguage("ar_sa", "السعودية", "Saudi Arabic", LanguageGroup.ARABIC, "المملكة العربية السعودية", Locale("ar", "SA"), "دعم رسمي وإعلامي", "حياكم الله وبياكم في عالم الإنتاج السينمائي المتطور"),
        StudioLanguage("ar_ae", "الإماراتية", "Emirati Arabic", LanguageGroup.ARABIC, "الإمارات العربية المتحدة", Locale("ar", "AE"), "دعم كامل وسريع", "مرحبا بكم ملايين في استوديو المستقبل"),
        StudioLanguage("ar_gulf", "الخليجية المشتركة", "Khaleeji Arabic", LanguageGroup.ARABIC, "دول الخليج العربي", Locale("ar", "KW"), "دعم خليجي شامل", "يا هلا ومسهلا بجميع المبدعين في الاستوديو"),
        StudioLanguage("ar_iq", "العراقية", "Iraqi Arabic", LanguageGroup.ARABIC, "العراق", Locale("ar", "IQ"), "دعم درامي وتاريخي", "هلو عيني شلونكم، نورتوا منصة السينما والإنتاج"),
        StudioLanguage("ar_jo", "الأردنية", "Jordanian Arabic", LanguageGroup.ARABIC, "الأردن", Locale("ar", "JO"), "دعم صوتي وترجمة", "يسعد مساكم جميعاً في الاستوديو الإبداعي"),
        StudioLanguage("ar_ps", "الفلسطينية", "Palestinian Arabic", LanguageGroup.ARABIC, "فلسطين", Locale("ar", "PS"), "دعم وثائقي ودرامي", "يسعد أوقاتكم يا أهلنا وأحبابنا في كل مكان"),
        StudioLanguage("ar_ye", "اليمنية", "Yemeni Arabic", LanguageGroup.ARABIC, "اليمن", Locale("ar", "YE"), "دعم تراثي وأصيل", "حياكم الله ونورتم المنصة السينمائية"),
        StudioLanguage("ar_sd", "السودانية", "Sudanese Arabic", LanguageGroup.ARABIC, "السودان", Locale("ar", "SD"), "دعم صوتي ولهجي", "مرحب بالحبان الكرام في استوديو كمال")
    )

    // 3. اللغات الأجنبية (10 لغات — الإيطالية بأولوية خاصة)
    val internationalLanguages = listOf(
        StudioLanguage(
            code = "it",
            nameAr = "الإيطالية (Italiano)",
            nameEn = "Italian",
            group = LanguageGroup.INTERNATIONAL,
            countryOrRegion = "إيطاليا (أولوية خاصة)",
            ttsLocale = Locale.ITALIAN,
            supportBadge = "⭐ أولوية سينمائية كاملة + صوت أصلي",
            sampleGreeting = "Benvenuti a KAMAL DZ STUDIO 2027, la piattaforma cinematografica avanzata con intelligenza artificiale.",
            isPriority = true
        ),
        StudioLanguage("en", "الإنجليزية (English)", "English", LanguageGroup.INTERNATIONAL, "العالم / USA / UK", Locale.US, "دعم هوليوودي عالمي كامل", "Welcome to KAMAL DZ STUDIO 2027, your premier AI cinematic platform."),
        StudioLanguage("fr", "الفرنسية (Français)", "French", LanguageGroup.INTERNATIONAL, "فرنسا والمغرب العربي", Locale.FRANCE, "دعم سينمائي وصوتي كامل", "Bienvenue sur KAMAL DZ STUDIO 2027, votre plateforme cinématographique IA."),
        StudioLanguage("es", "الإسبانية (Español)", "Spanish", LanguageGroup.INTERNATIONAL, "إسبانيا وأمريكا اللاتينية", Locale("es", "ES"), "دعم كامل وسريع", "Bienvenidos a KAMAL DZ STUDIO 2027, la plataforma cinematográfica con IA."),
        StudioLanguage("de", "الألمانية (Deutsch)", "German", LanguageGroup.INTERNATIONAL, "ألمانيا وأوروبا", Locale.GERMANY, "دعم صوتي ودبلجة دقيقة", "Willkommen bei KAMAL DZ STUDIO 2027, der fortschrittlichen KI-Kinoplattform."),
        StudioLanguage("pt", "البرتغالية (Português)", "Portuguese", LanguageGroup.INTERNATIONAL, "البرتغال والبرازيل", Locale("pt", "PT"), "دعم درامي وصوتي", "Bem-vindos ao KAMAL DZ STUDIO 2027, a plataforma cinematográfica de IA."),
        StudioLanguage("tr", "التركية (Türkçe)", "Turkish", LanguageGroup.INTERNATIONAL, "تركيا", Locale("tr", "TR"), "دعم مسلسلات وسينما", "KAMAL DZ STUDIO 2027 yapay zeka sinema platformuna hoş geldiniz."),
        StudioLanguage("ru", "الروسية (Русский)", "Russian", LanguageGroup.INTERNATIONAL, "روسيا", Locale("ru", "RU"), "دعم أوركسترالي وسينمائي", "Добро пожаловать в киностудию искусственного интеллекта KAMAL DZ STUDIO."),
        StudioLanguage("zh", "الصينية (中文)", "Chinese (Mandarin)", LanguageGroup.INTERNATIONAL, "الصين", Locale.SIMPLIFIED_CHINESE, "دعم عالمي متقدم", "欢迎来到 KAMAL DZ STUDIO 2027 人工智能电影创作平台。"),
        StudioLanguage("ja", "اليابانية (日本語)", "Japanese", LanguageGroup.INTERNATIONAL, "اليابان", Locale.JAPAN, "دعم أنمي وسينما", "KAMAL DZ STUDIO 2027 AIシネマプラットフォームへようこそ。")
    )

    val allLanguages = algerianDialects + arabicDialects + internationalLanguages

    fun findLanguage(code: String): StudioLanguage {
        return allLanguages.firstOrNull { it.code == code } ?: algerianDialects.first()
    }
}

// Subtitle & Timed Dialogue Cue
data class SubtitleCue(
    val id: Int,
    val startMs: Long,
    val endMs: Long,
    val sourceText: String,
    val translatedText: String,
    val sourceLanguage: String,
    val targetLanguage: String
) {
    fun formatTimecode(ms: Long, isSrt: Boolean = true): String {
        val totalSecs = ms / 1000
        val millis = ms % 1000
        val hours = totalSecs / 3600
        val minutes = (totalSecs % 3600) / 60
        val seconds = totalSecs % 60
        val delimiter = if (isSrt) "," else "."
        return String.format(Locale.US, "%02d:%02d:%02d%s%03d", hours, minutes, seconds, delimiter, millis)
    }

    fun toSrtBlock(): String {
        return "$id\n${formatTimecode(startMs, true)} --> ${formatTimecode(endMs, true)}\n$sourceText\n$translatedText\n"
    }

    fun toVttBlock(): String {
        return "$id\n${formatTimecode(startMs, false)} --> ${formatTimecode(endMs, false)}\n$sourceText\n$translatedText\n"
    }
}
