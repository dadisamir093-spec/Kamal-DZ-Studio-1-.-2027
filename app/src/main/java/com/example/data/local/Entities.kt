package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val durationText: String = "02:15",
    val resolution: String = "1080p",
    val aspectRatio: String = "16:9",
    val fps: Int = 30,
    val category: String = "فيديو سينمائي",
    val thumbnailResId: Int? = null,
    val thumbnailUri: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val role: String = "بطل القصة",
    val description: String = "",
    val facialFeatures: String = "ملامح محددة، نظارات داكنة، لحية مشذبة",
    val costumeStyle: String = "سترة جلدية سوداء أنيقة",
    val voiceDialect: String = "الجزائرية - العاصمة",
    val imageResId: Int? = null,
    val imageUri: String? = null,
    val isIdentityLocked: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audio_tracks")
data class AudioTrackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String, // VOICE, MUSIC, SFX
    val dialectOrGenre: String,
    val durationText: String = "02:30",
    val promptOrLyrics: String = "",
    val tone: String = "عادي",
    val instruments: String = "بيانو، دربوكة، مندول",
    val audioUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "export_tasks")
data class ExportTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val projectTitle: String,
    val format: String = "MP4",
    val resolution: String = "1080p",
    val fps: Int = 60,
    val quality: String = "عالية (High)",
    val fileSizeMb: Float = 320.0f,
    val progress: Int = 100, // 0-100
    val status: String = "COMPLETED", // PENDING, PROCESSING, COMPLETED, FAILED
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "skills")
data class SkillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val skillKey: String,
    val name: String,
    val description: String,
    val category: String,
    val targetSection: String,
    val markdownContent: String,
    val isEnabled: Boolean = true,
    val isBuiltIn: Boolean = true,
    val version: String = "1.0.0"
)
