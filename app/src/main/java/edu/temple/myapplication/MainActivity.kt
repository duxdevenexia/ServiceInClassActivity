/*
* Nov 14, 2024, Class Activity Working with Bound Services
*
* */


package edu.temple.myapplication

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private var isTimerRunning = false  // to trace timer status

    private var timerService: TimerService? = null
    private var isServiceBound = false  // mark service whether is bound


    private lateinit var timerBinder: TimerService.TimerBinder

    private val handler = Handler(Looper.getMainLooper()) { msg ->
        // handle message sent from TimerService，renew TextView
        if (msg.what >= 0) {
            findViewById<TextView>(R.id.textView).text = msg.what.toString()
        }
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set activity_main as current Content View
        setContentView(R.layout.activity_main)


        // startButton listener
        findViewById<Button>(R.id.startButton).setOnClickListener {
            // if timer already run
            if (isTimerRunning) {
                timerBinder.pause() // call pause()
                isTimerRunning = false
            } else {
                // just start timerBinder
                timerBinder.start(100)  // that number sec as start value
                isTimerRunning = true
            }

        }

        // stopButton listener
        findViewById<Button>(R.id.stopButton).setOnClickListener {

            if (isServiceBound) {
                // stop timer and service
                timerBinder.stop()
                unbindService(serviceConnection)
                isServiceBound = false
                stopService(Intent(this, TimerService::class.java))  // stop service

            }

        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // obtain Binder object of service
            timerBinder = service as TimerService.TimerBinder
            timerBinder.setHandler(handler)  // set Handler
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceBound = false
        }
    }

        override fun onStart() {
            super.onStart()
            // bound TimerService
            val serviceIntent = Intent(this, TimerService::class.java)
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        override fun onStop() {
            super.onStop()
            if (isServiceBound) {
                // unbind service
                unbindService(serviceConnection)
                isServiceBound = false
            }
        }

}