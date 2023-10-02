package com.example.voicenoteapp.interfaces

import com.example.voicenoteapp.databinding.ItemMessegeBinding
import com.example.voicenoteapp.model.AudioRecord

interface MessegeItemInterface {
    abstract fun onPlayClicked(audioRecord: AudioRecord, binding: ItemMessegeBinding)
}