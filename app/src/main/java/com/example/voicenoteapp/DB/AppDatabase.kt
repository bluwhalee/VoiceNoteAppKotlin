package com.example.voicenoteapp.DB

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.voicenoteapp.model.AudioRecord

@Database(entities = [AudioRecord::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun audioRecordDao() : AudioRecordDao
}