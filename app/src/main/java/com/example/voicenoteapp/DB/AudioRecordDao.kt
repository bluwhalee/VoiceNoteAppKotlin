package com.example.voicenoteapp.DB

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.voicenoteapp.model.AudioRecord
import com.example.voicenoteapp.utils.Constants

@Dao
interface AudioRecordDao {

    @Query("SELECT * FROM ${Constants.DB_NAME}")
    fun getAll() : LiveData<List<AudioRecord>>

    @Insert
    suspend fun insert(vararg audioRecord: AudioRecord)

    @Delete
    suspend fun delete(audioRecord: AudioRecord)
}