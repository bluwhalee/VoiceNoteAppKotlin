package com.example.voicenoteapp.activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.SeekBar
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.demoproject1.adapters.MessegeAdapter
import com.example.voicenoteapp.DB.AppDatabase
import com.example.voicenoteapp.R
import com.example.voicenoteapp.databinding.ActivityMainBinding
import com.example.voicenoteapp.databinding.ItemMessegeBinding
import com.example.voicenoteapp.interfaces.MessegeItemInterface
import com.example.voicenoteapp.model.AudioRecord
import java.text.SimpleDateFormat
import java.util.Date
import com.example.voicenoteapp.timer.Timer
import com.example.voicenoteapp.utils.Constants
import com.example.voicenoteapp.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

const val REQUEST_CODE = 200

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), Timer.OnTimerTickListener, MessegeItemInterface{

    private var handler = Handler()
    private lateinit var binding: ActivityMainBinding
    private var permissions = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.VIBRATE)
    private var permissionGranted = false
    private lateinit var recorder: MediaRecorder
    private lateinit var player: MediaPlayer
    private var dirPath = ""
    private var fileName = ""
    private var filePath =""
    private lateinit var duration : String
    private lateinit var date : String
    private var isRecording = false
    private var isPlaying = false
    private lateinit var db : AppDatabase
    private lateinit var viewModel: MainViewModel
    private lateinit var recyclerAdapter: MessegeAdapter
    private var currentAudioRecord: AudioRecord? = null
    private var currentBinding: ItemMessegeBinding? = null
    private lateinit var runnable: Runnable
    private lateinit var currentRunnable: Runnable

    private lateinit var vibrator : Vibrator
    private var timer = Timer(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        init()
        setContentView(binding.root)
    }

    private fun init()
    {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        setPermissions()
        setMicOnClick()
        setViewModel()
        setRecycler()
        setAllAudioObserver()
    }

    override fun onPlayClicked(audioRecord: AudioRecord, binding: ItemMessegeBinding) {

        if(!isPlaying)
        {
                player = MediaPlayer.create(this, audioRecord.filePath.toUri())
                player.seekTo(binding.seekBar.progress)
                player.start()
                binding.playBtn.setImageResource(R.drawable.baseline_pause_24)
                currentBinding?.playBtn?.setImageResource(R.drawable.baseline_play_arrow_24)
                currentAudioRecord = audioRecord
                isPlaying = true
                currentBinding = binding
                binding.seekBar.max = player.duration
                handler.removeCallbacksAndMessages(null)
                runnable = Runnable {
                    binding.seekBar.progress = player.currentPosition
                    println(player.currentPosition)
                    handler.postDelayed(runnable, 100) // Update every 1 second
                }
                handler.postDelayed(runnable,100)
        }
        else
        {
            if(audioRecord.filePath == currentAudioRecord?.filePath)
            {
                player.pause()
                isPlaying = false
                binding.playBtn.setImageResource(R.drawable.baseline_play_arrow_24)
                handler.removeCallbacks(runnable)
                audioRecord.duration = player.currentPosition.toString()
            }
            else{
                player.apply {
                    stop()
                    reset()
                    release()
                }
                player = MediaPlayer.create(this, audioRecord.filePath.toUri())
                player.seekTo(binding.seekBar.progress)
                player.start()
                handler.removeCallbacks(runnable)

                binding.seekBar.max = player.duration

                handler.removeCallbacks(runnable)
                currentAudioRecord?.duration = player.currentPosition.toString()
                runnable = Runnable {
                    binding.seekBar.progress = player.currentPosition
                    handler.postDelayed(runnable, 100) // Update every 1 second
                }
                handler.postDelayed(runnable,100)

                isPlaying = true
                binding.playBtn.setImageResource(R.drawable.baseline_pause_24)
//                currentBinding?.seekBar?.progress = 0
                currentBinding?.playBtn?.setImageResource(R.drawable.baseline_play_arrow_24)
                currentAudioRecord = audioRecord
                currentBinding = binding
            }
        }
        player.setOnCompletionListener {
            binding.seekBar.progress =0
            handler.removeCallbacks(runnable)
            binding.playBtn.setImageResource(R.drawable.baseline_play_arrow_24)
        }
    }

    private fun setViewModel() {
        viewModel = ViewModelProvider(
            this,
        )[MainViewModel::class.java]
    }

    private fun setRecycler() {
        recyclerAdapter = MessegeAdapter(this)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recyclerAdapter
        }
    }

    private fun setAllAudioObserver() {
        viewModel.getAudioRecords().observe(this) { list ->
            list?.let {
                recyclerAdapter.updateList(list)
            }
        }
    }


    private fun setPermissions(){

        permissionGranted = ActivityCompat.checkSelfPermission(this,permissions[0]) == PackageManager.PERMISSION_GRANTED
        if(!permissionGranted)
            ActivityCompat.requestPermissions(this,permissions, REQUEST_CODE)
    }
    private fun setMicOnClick() {
        binding.micIcParent.setOnClickListener{
            if(isRecording) stopRecording()
            else startRecording()
            vibrator.vibrate(VibrationEffect.createOneShot(50,VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun stopRecording() {
        if(isRecording){
            recorder.apply {
                stop()
                reset()
                release()
            }
            timer.stop()
            binding.apply {
                micIcon.setImageResource(R.drawable.baseline_mic_24)
                timerRecord.text = "00:00"
                releaseTextView.isVisible = false
            }
            isRecording=false
            save()
        }

    }

    private fun save() {
        val audioRecord = AudioRecord(fileName, filePath, 0.toString(), Date().time)
        GlobalScope.launch {
            viewModel.insertAudioRecord(audioRecord)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE)
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    private fun startRecording(){
        if(!permissionGranted){
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
        }

        recorder = MediaRecorder(this)
        dirPath = "${filesDir}/"

        date = SimpleDateFormat("yyyy.MM.DD_hh.mm.ss").format(Date())
        fileName = "voice_note_$date"

        val outputFile = File(getExternalFilesDir(null),"$fileName.mp3")
        filePath = outputFile.absolutePath


        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(outputFile).fd)
            prepare()
            start()
        }
        isRecording = true
        binding.micIcon.setImageResource(R.drawable.baseline_stop_24)
        binding.releaseTextView.isVisible = true
        timer.start()
    }
    override fun onTimerTick(duration: String) {
        this.duration = duration
        binding.timerRecord.text = duration
    }
}