package com.example.demoproject1.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.demoproject1.viewHolders.MessegeViewHolder
import com.example.voicenoteapp.interfaces.MessegeItemInterface
import com.example.voicenoteapp.model.AudioRecord

class MessegeAdapter (private val listener:MessegeItemInterface): RecyclerView.Adapter<MessegeViewHolder>(){

    // region properties
    private val allMessage = ArrayList<AudioRecord>()

    // region lifecycle
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessegeViewHolder {
        return MessegeViewHolder.fromParent(parent)
    }
    override fun onBindViewHolder(holder: MessegeViewHolder, position: Int) {
        holder.bind(allMessage[position], listener)
    }
    override fun getItemCount(): Int  = allMessage.size

    // region private methods
    fun updateList(newList: List<AudioRecord>){
        allMessage.apply {
            clear()
            addAll(newList)
        }
        notifyDataSetChanged()
    }

}

