package com.example.voicenoteapp.timer

import android.os.Handler
import android.os.Looper
import com.example.voicenoteapp.interfaces.TimerInterface
import java.time.Duration

class Timer(listener: TimerInterface) {

    private var handler = Handler(Looper.getMainLooper())
    private lateinit var runnable : Runnable

    private var duration = 0L
    private var delay = 1000L

    init{

        // this will update time every sec and again call itself
        runnable = Runnable{
            duration += delay
            handler.postDelayed(runnable, delay)
            listener.onTimerTick(format())
        }
    }

    fun start() = handler.postDelayed(runnable, delay)

    fun stop() {
        handler.removeCallbacks(runnable)
        duration = 0L
    }

    fun format(): String {
        val seconds = (duration/1000)
        val minutes = (duration/(1000*60))

        return "%02d:%02d".format(minutes,seconds)
    }

}