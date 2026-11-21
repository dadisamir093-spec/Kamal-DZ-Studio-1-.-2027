package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.AudioTrackEntity
import com.example.data.local.CharacterEntity
import com.example.data.local.ExportTaskEntity
import com.example.data.local.ProjectEntity
import com.example.data.local.SkillEntity
import kotlinx.coroutines.flow.Flow

class StudioRepository(private val database: AppDatabase) {

    // Projects
    val allProjects: Flow<List<ProjectEntity>> = database.projectDao().getAllProjects()
    val favoriteProjects: Flow<List<ProjectEntity>> = database.projectDao().getFavoriteProjects()

    suspend fun saveProject(project: ProjectEntity): Long =
        database.projectDao().insertProject(project)

    suspend fun updateProject(project: ProjectEntity) =
        database.projectDao().updateProject(project)

    suspend fun deleteProject(project: ProjectEntity) =
        database.projectDao().deleteProject(project)

    // Characters
    val allCharacters: Flow<List<CharacterEntity>> = database.characterDao().getAllCharacters()

    suspend fun saveCharacter(character: CharacterEntity): Long =
        database.characterDao().insertCharacter(character)

    suspend fun updateCharacter(character: CharacterEntity) =
        database.characterDao().updateCharacter(character)

    suspend fun deleteCharacter(character: CharacterEntity) =
        database.characterDao().deleteCharacter(character)

    // Audio
    val allAudioTracks: Flow<List<AudioTrackEntity>> = database.audioTrackDao().getAllTracks()

    suspend fun saveAudioTrack(track: AudioTrackEntity): Long =
        database.audioTrackDao().insertTrack(track)

    suspend fun deleteAudioTrack(track: AudioTrackEntity) =
        database.audioTrackDao().deleteTrack(track)

    // Export Tasks
    val allExportTasks: Flow<List<ExportTaskEntity>> = database.exportTaskDao().getAllExportTasks()

    suspend fun saveExportTask(task: ExportTaskEntity): Long =
        database.exportTaskDao().insertTask(task)

    suspend fun updateExportTask(task: ExportTaskEntity) =
        database.exportTaskDao().updateTask(task)

    suspend fun deleteExportTask(task: ExportTaskEntity) =
        database.exportTaskDao().deleteTask(task)

    // Skills
    val allSkills: Flow<List<SkillEntity>> = database.skillDao().getAllSkills()

    suspend fun saveSkill(skill: SkillEntity): Long =
        database.skillDao().insertSkill(skill)

    suspend fun updateSkill(skill: SkillEntity) =
        database.skillDao().updateSkill(skill)

    suspend fun deleteSkill(skill: SkillEntity) =
        database.skillDao().deleteSkill(skill)
}
