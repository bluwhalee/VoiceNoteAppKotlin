package com.example.demoproject1.viewHolders

import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.voicenoteapp.R
import com.example.voicenoteapp.databinding.ItemMessegeBinding
import com.example.voicenoteapp.interfaces.MessegeItemInterface
import com.example.voicenoteapp.model.AudioRecord

class MessegeViewHolder (val binding: ItemMessegeBinding) : RecyclerView.ViewHolder(binding.root) {

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var currentPlayingPosition = -1


    companion object{
        fun fromParent(parent: ViewGroup,):MessegeViewHolder{
            return MessegeViewHolder(ItemMessegeBinding.inflate(LayoutInflater.from(parent.context),parent,false))
        }
    }

    fun bind(audioRecord: AudioRecord, listener: MessegeItemInterface)
    {
        binding.playBtn.setOnClickListener {
            listener.onPlayClicked(audioRecord, binding)
        }
    }


}
