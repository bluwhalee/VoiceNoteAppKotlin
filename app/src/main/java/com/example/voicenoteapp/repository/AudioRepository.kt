package com.example.voicenoteapp.repository

import com.example.voicenoteapp.DB.AppDatabase
import com.example.voicenoteapp.model.AudioRecord
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRepository @Inject constructor (
    private val appDatabase: AppDatabase
) {
    fun getAudioRecords() = appDatabase.audioRecordDao().getAll()

    suspend fun insert(audioRecord: AudioRecord)
    {
        appDatabase.audioRecordDao().insert(audioRecord)
    }
}