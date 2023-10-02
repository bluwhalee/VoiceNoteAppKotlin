package com.example.voicenoteapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.voicenoteapp.DB.AppDatabase
import com.example.voicenoteapp.model.AudioRecord
import com.example.voicenoteapp.repository.AudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor (private val audioRepository: AudioRepository) : ViewModel() {

    fun getAudioRecords() = audioRepository.getAudioRecords()
    suspend fun insertAudioRecord(audioRecord: AudioRecord){
        audioRepository.insert(audioRecord)
    }
}