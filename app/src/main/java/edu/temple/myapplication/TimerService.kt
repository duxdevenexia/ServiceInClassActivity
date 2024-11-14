package edu.temple.myapplication

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log

@Suppress("ControlFlowWithEmptyBody")
class TimerService : Service() {

    private var isRunning = false

    private var timerHandler : Handler? = null

    lateinit var t: TimerThread

    private var paused = false

    // TimerBinder inner class, by TimerThread to execute timer logic
    inner class TimerBinder : Binder() {

        // Check if Timer is already running
        var isRunning: Boolean
            get() = this@TimerService.isRunning
            set(value) {this@TimerService.isRunning = value}

        // Start a new timer
        fun start(startValue: Int){

            if (!paused) {
                if (!isRunning) {
                    if (::t.isInitialized) t.interrupt()
                    this@TimerService.start(startValue)
                }
            } else {
                pause()
            }
        }

        // set Handle, which handling UI renew
        fun setHandler(handler: Handler) {
            timerHandler = handler
        }

        // Stop a currently running timer
        fun stop() {
            if (::t.isInitialized || isRunning) {
                t.interrupt()
            }
        }

        // Pause a running timer
        fun pause() {
            this@TimerService.pause()
        }

    }

    override fun onCreate() {
        super.onCreate()

        Log.d("TimerService status", "Created")
    }

    override fun onBind(intent: Intent): IBinder {
        // return TimerBinder, which allow to interact with outside
        return TimerBinder()
    }

    fun start(startValue: Int) {
        t = TimerThread(startValue)
        t.start()           //  start TimerThread
    }

    fun pause () {
        if (::t.isInitialized) {
            paused = !paused        // switch to paused status
            isRunning = !paused
        }
    }


    inner class TimerThread(private val startValue: Int) : Thread() {

        override fun run() {
            isRunning = true
            try {
                for (i in startValue downTo 1)  {
                    Log.d("Countdown", i.toString())

                    //timerHandler?.sendEmptyMessage(i)
                    // use Handler send thread massage to UI
                    timerHandler?.sendMessage(Message.obtain().apply {
                        what = i  // save  number
                    })

                    while (paused); // waiting for paused status canceled
                    sleep(1000) // renew per 1 sec

                }
                isRunning = false
            } catch (e: InterruptedException) {
                Log.d("Timer interrupted", e.toString())
                isRunning = false
                paused = false
            }
        }

    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (::t.isInitialized) {
            t.interrupt()
        }

        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        timerHandler?.sendEmptyMessage(0)
        Log.d("TimerService status", "Destroyed")
    }


}