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
import android.os.VibratorManager
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.demoproject1.adapters.MessegeAdapter
import com.example.voicenoteapp.R
import com.example.voicenoteapp.databinding.ActivityMainBinding
import com.example.voicenoteapp.databinding.ItemMessegeBinding
import com.example.voicenoteapp.interfaces.MessegeItemInterface
import com.example.voicenoteapp.interfaces.TimerInterface
import com.example.voicenoteapp.model.AudioRecord
import java.text.SimpleDateFormat
import java.util.Date
import com.example.voicenoteapp.timer.Timer
import com.example.voicenoteapp.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

const val REQUEST_CODE = 200

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), TimerInterface, MessegeItemInterface{

    //Properties
    private lateinit var recorder: MediaRecorder
    private lateinit var player: MediaPlayer
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var recyclerAdapter: MessegeAdapter
    private lateinit var runnable: Runnable
    private lateinit var vibratorManager : VibratorManager
    private lateinit var vibrator: Vibrator

    private var dirPath = ""
    private var fileName = ""
    private var filePath = ""
    private var date = ""
    private var duration = ""
    private var permissionGranted = false
    private var isRecording = false
    private var isPlaying = false
    private var handler = Handler()
    private var permissions = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.VIBRATE)
    private var prevAudioRecord: AudioRecord? = null
    private var prevBinding: ItemMessegeBinding? = null
    private var timer = Timer(this)
    private var job : Job? = null


    //Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        init()
        setContentView(binding.root)
    }

    //Private and Override members
    private fun init()
    {
        vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        setPermissions()
        setMicOnClick()
        setViewModel()
        setRecycler()
        setAllAudioObserver()
    }

    override fun onPlayClicked(audioRecord: AudioRecord, messegeBinding: ItemMessegeBinding) {

        //If nothing playing already
        if(!isPlaying)
        {
            //Play audio
            player = MediaPlayer.create(this, audioRecord.filePath.toUri())
            player.apply {
                player.seekTo(messegeBinding.seekBar.progress)
                player.start()
            }

            //Set icon,Seekbar
            messegeBinding.apply {
                playBtn.setImageResource(R.drawable.baseline_pause_24)
                seekBar.max = player.duration
            }

            //Move Seek Bar, stop moving the prev one
            job?.cancel()
            moveSeekBar(messegeBinding)


            //Set Icons of prev
            prevBinding?.playBtn?.setImageResource(R.drawable.baseline_play_arrow_24)

            //Get ready for the next onClick
            isPlaying = true
            prevAudioRecord = audioRecord
            prevBinding = messegeBinding


        }
        //If something is playing
        else
        {
            if(audioRecord.id == prevAudioRecord?.id)
            {
                //Pause the current audio
                player.pause()

                isPlaying = false
                messegeBinding.playBtn.setImageResource(R.drawable.baseline_play_arrow_24)
                //Stop Seekbar
                job?.cancel()
            }
            else{
                //Play the audio
                player.apply {
                    stop()
                    reset()
                    release()
                }
                player = MediaPlayer.create(this, audioRecord.filePath.toUri())
                player.apply {
                    seekTo(messegeBinding.seekBar.progress)
                    start()
                }

                //Set SeekBar
                messegeBinding.apply {
                    seekBar.max = player.duration
                    playBtn.setImageResource(R.drawable.baseline_pause_24)
                }

                //Move Seek Bar, stop moving the prev one

                job?.cancel()
                moveSeekBar(messegeBinding)


                //Set Icons of prev
                prevBinding?.playBtn?.setImageResource(R.drawable.baseline_play_arrow_24)

                //Get ready for the next onClick
                isPlaying = true
                prevAudioRecord = audioRecord
                prevBinding = messegeBinding

            }
        }
        //If audio is completed
        player.setOnCompletionListener {

            //Reset SeekBar
            handler.removeCallbacks(runnable)
            messegeBinding.apply {
                seekBar.progress =0
                playBtn.setImageResource(R.drawable.baseline_play_arrow_24)
            }
            isPlaying = false
            prevBinding = null
        }
    }

    private fun moveSeekBar(messegeBinding: ItemMessegeBinding)
    {
        job = GlobalScope.launch {
            while (true) {
                messegeBinding.seekBar.progress = player.currentPosition
                delay(100)
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
            vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createOneShot(50,VibrationEffect.DEFAULT_AMPLITUDE))
        }
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
        timer.start()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE)
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
    }
    override fun onTimerTick(duration: String) {
        this.duration = duration
        binding.timerRecord.text = duration
    }
}