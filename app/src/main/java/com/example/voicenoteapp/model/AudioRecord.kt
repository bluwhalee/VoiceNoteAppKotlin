package com.example.voicenoteapp.model

import android.icu.text.Transliterator.Position
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.voicenoteapp.utils.Constants

@Entity(tableName = Constants.DB_NAME)
data class AudioRecord(
    var fileName :String,
    var filePath :String,
    var duration :String,
    var timeStamp : Long
) {
    @PrimaryKey(autoGenerate = true)
    var id=0

}