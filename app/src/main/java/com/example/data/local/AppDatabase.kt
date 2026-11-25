package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProjectEntity::class,
        CharacterEntity::class,
        AudioTrackEntity::class,
        ExportTaskEntity::class,
        SkillEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun characterDao(): CharacterDao
    abstract fun audioTrackDao(): AudioTrackDao
    abstract fun exportTaskDao(): ExportTaskDao
    abstract fun skillDao(): SkillDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kamal_dz_studio.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(db: AppDatabase) {
            // Seed sample Projects matching the reference image
            db.projectDao().insertProject(
                ProjectEntity(
                    title = "رحلة الجزائر",
                    description = "مشهد سينمائي شاطئي عند غروب الشمس فوق قصبة الجزائر",
                    durationText = "04:32",
                    resolution = "1080p",
                    aspectRatio = "16:9",
                    fps = 30,
                    category = "فيديو سينمائي",
                    isFavorite = true
                )
            )
            db.projectDao().insertProject(
                ProjectEntity(
                    title = "مغامرة في الصحراء",
                    description = "لقطات جوية مذهلة لكثبان تاسيلي وطاسيلي ناجر",
                    durationText = "02:15",
                    resolution = "4K UHD",
                    aspectRatio = "16:9",
                    fps = 60,
                    category = "فيديو سينمائي",
                    isFavorite = true
                )
            )
            db.projectDao().insertProject(
                ProjectEntity(
                    title = "مدينة الجزائر العتيقة",
                    description = "حركة كاميرا Pan و Dolly In عبر أزقة القصبة التاريخية",
                    durationText = "02:48",
                    resolution = "1080p",
                    aspectRatio = "16:9",
                    fps = 30,
                    category = "فيديو سينمائي"
                )
            )
            db.projectDao().insertProject(
                ProjectEntity(
                    title = "مشروع تجريبي",
                    description = "اختبار نقل الحركة واستمرارية الشخصيات",
                    durationText = "05:12",
                    resolution = "2K",
                    aspectRatio = "9:16",
                    fps = 24,
                    category = "استنساخ شخصية"
                )
            )
            db.projectDao().insertProject(
                ProjectEntity(
                    title = "شخصية رقمية سينمائية",
                    description = "استوديو استنساخ الشخصيات وقفل الهوية البصرية",
                    durationText = "01:36",
                    resolution = "1080p",
                    aspectRatio = "1:1",
                    fps = 30,
                    category = "شخصيات"
                )
            )

            // Seed sample Character Profile
            db.characterDao().insertCharacter(
                CharacterEntity(
                    name = "كمال - البطل السينمائي",
                    role = "الشخصية الرئيسية",
                    description = "رجل ذو ملامح واثقة، لحية مشذبة، نظارات شمسية داكنة، وسترة جلدية",
                    facialFeatures = "ملامح محددة، نظارات داكنة، لحية مشذبة",
                    costumeStyle = "سترة جلدية سوداء أنيقة وقميص داكن",
                    voiceDialect = "الجزائرية - العاصمة",
                    isIdentityLocked = true
                )
            )

            // Seed Audio Tracks
            db.audioTrackDao().insertTrack(
                AudioTrackEntity(
                    title = "تعليق سينمائي - وثائقي القصبة",
                    type = "VOICE",
                    dialectOrGenre = "الجزائرية - العاصمة",
                    durationText = "02:30",
                    promptOrLyrics = "هنا بين أزقة القصبة العتيقة، يتنفس التاريخ حكايا الأجداد وأصوات البحر...",
                    tone = "هادئ وسينمائي"
                )
            )
            db.audioTrackDao().insertTrack(
                AudioTrackEntity(
                    title = "موسيقى راي سينمائية حديثة",
                    type = "MUSIC",
                    dialectOrGenre = "راي سينمائي (Cinematic Rai)",
                    durationText = "03:15",
                    promptOrLyrics = "مزيج ملحمي من المندول الجزائري والدربوكة مع مؤثرات سينمائية إلكترونية",
                    instruments = "مندول، دربوكة، كمان، سينثيسايزر"
                )
            )

            // Seed Export Task
            db.exportTaskDao().insertTask(
                ExportTaskEntity(
                    fileName = "مشهد_نهائي_4k.mp4",
                    projectTitle = "رحلة الجزائر",
                    format = "MP4",
                    resolution = "1080p (Full HD)",
                    fps = 60,
                    quality = "عالية (High)",
                    fileSizeMb = 320.0f,
                    progress = 100,
                    status = "COMPLETED"
                )
            )

            // Seed Skills
            val initialSkills = listOf(
                SkillEntity(
                    skillKey = "cinematic_prompting",
                    name = "هندسة البرومبتات السينمائية",
                    description = "توليد أوصاف سينمائية غنية بالإضاءة والحركة وزوايا الكاميرا العالمية",
                    category = "استوديو التوليد",
                    targetSection = "التوليد",
                    markdownContent = """
                        # مهارة هندسة البرومبتات السينمائية
                        تهدف هذه المهارة إلى تحويل الوصف البسيط إلى مشهد هوليوودي متكامل.
                        - إضافة حركة الكاميرا (Dolly In, Orbit, Drone)
                        - ضبط نوع العدسة (Anamorphic, 35mm, 85mm)
                        - ضبط الإضاءة (Golden hour, Cyberpunk neon, Rim light)
                        - تعزيز الواقعية ومنع التشويه عبر Negative Prompts
                    """.trimIndent(),
                    isEnabled = true,
                    isBuiltIn = true
                ),
                SkillEntity(
                    skillKey = "algerian_cinema_dialect",
                    name = "اللهجة والسينما الجزائرية",
                    description = "ضبط الحوارات والمصطلحات المحلية الجزائرية (العاصمة، وهران، الشرق) بدقة",
                    category = "الصوت والتعليق",
                    targetSection = "الصوت",
                    markdownContent = """
                        # مهارة اللهجة والسينما الجزائرية
                        - دعم التراكيب الجزائرية الأصيلة دون تحويل قسري للفصحى
                        - ضبط النبرة العاطفية الجزائرية (حنين، هيبة، عزة)
                        - التوافق مع أصوات التعليق الصوتي والدبلجة
                    """.trimIndent(),
                    isEnabled = true,
                    isBuiltIn = true
                ),
                SkillEntity(
                    skillKey = "character_identity_lock",
                    name = "قفل استمرارية الشخصيات",
                    description = "الحفاظ على ملامح الوجه والشعر والملابس والنسب الجسدية عبر المشاهد",
                    category = "استوديو الشخصيات",
                    targetSection = "الشخصيات",
                    markdownContent = """
                        # مهارة قفل استمرارية الشخصيات
                        - فحص نقاط التطابق الملامحي (Face Landmarks)
                        - استخراج قناع الملامح (Identity Mask)
                        - تنبيه المستخدم في حال حدوث انحراف في مظهر الشخصية
                    """.trimIndent(),
                    isEnabled = true,
                    isBuiltIn = true
                ),
                SkillEntity(
                    skillKey = "rai_chaabi_music_engine",
                    name = "محرر موسيقى الراي والشعبي",
                    description = "هيكلة مقاطع الراي والشعبي وتوزيع آلات المندول والكمان والدربوكة",
                    category = "استوديو الموسيقى",
                    targetSection = "الموسيقى",
                    markdownContent = """
                        # مهارة محرر موسيقى الراي والشعبي
                        - توزيع الإيقاع (BPM 85 إلى 135)
                        - تنسيق مقاطع الأغنية (المقدمة، المقطع، اللازمة، الاستخبار)
                        - توليد وصف آلات احترافي لنموذج الصوت
                    """.trimIndent(),
                    isEnabled = true,
                    isBuiltIn = true
                ),
                SkillEntity(
                    skillKey = "ai_timeline_director",
                    name = "مخرج المونتاج الذكي",
                    description = "تنفيذ أوامر المونتاج باللغة الطبيعية وتلوين المشاهد وقص الإطارات",
                    category = "المونتاج السينمائي",
                    targetSection = "المونتاج",
                    markdownContent = """
                        # مهارة مخرج المونتاج الذكي
                        - تفسير الأوامر الطبيعية (احذف أول ثانيتين، أضف تدرج أزرق سينمائي)
                        - تنظيم المسارات وتزامن الصوت مع حركة الفيديو
                        - تصدير الترجمة SRT تلقائياً
                    """.trimIndent(),
                    isEnabled = true,
                    isBuiltIn = true
                )
            )
            db.skillDao().insertAll(initialSkills)
        }
    }
}
