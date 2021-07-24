package com.example.pomodoro

import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoro.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), TimerListener, LifecycleObserver {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding get() = _binding!!
    private val timers = mutableListOf<Timer>()
    private lateinit var notification: Ringtone
    private val timerAdapter = TimerAdapter(this)
    private var startedTimer: Timer? = null

    private var nextId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = timerAdapter
        }

        binding.addNewTimerButton.setOnClickListener {
            val time =
                (binding.inputMinutesField.text.toString().toIntOrNull() ?: 0) * 60L * 1000 +
                        (binding.inputSecondsField.text.toString().toLongOrNull() ?: 0) * 1000
            if(time != 0L) {
                timers.add(Timer(nextId++, time, time, false))
                timerAdapter.submitList(timers.toList())
            } else Toast.makeText(this, getString(R.string.empty_or_nullable_fields_message), Toast.LENGTH_LONG).show()
        }
        notification =
            RingtoneManager.getRingtone(this, Uri.parse("android.resource://$packageName/raw/tudu"))
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                moveTaskToBack(false)
            }
        })
    }


    override fun start(id: Int) {
        if (startedTimer != null) {
            changeStopwatch(startedTimer!!.id, null, false)
            Thread.sleep(100L)
            changeStopwatch(id, null, true)
        } else {
            changeStopwatch(id, null, true)
        }
    }

    override fun stop(id: Int, currentMs: Long) {
        startedTimer = null
        changeStopwatch(id, currentMs, false)
    }


    override fun delete(id: Int) {
        if(startedTimer?.id == id){
            startedTimer?.stop()
            startedTimer = null
        }
        timers.remove(timers.find { it.id == id })
        timerAdapter.submitList(timers.toList())
    }

    override fun getBGColor(isFinished: Boolean): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(getColorByIsFinished(isFinished), theme)
        } else {
            resources.getColor(getColorByIsFinished(isFinished))
        }
    }

    override fun makeFinalNotification() {
        notification.stop()
        notification.play()
        Toast.makeText(this, getString(R.string.toast_message), Toast.LENGTH_LONG).show()
    }

    private fun getColorByIsFinished(isFinished: Boolean): Int {
        return if (isFinished) R.color.alarm else R.color.transparent
    }

    private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean) {
        val newTimers = mutableListOf<Timer>()
        timers.forEach {
            if (it.id == id) {
                newTimers.add(Timer(it.id, currentMs ?: it.timeLeft, it.initialTime, isStarted))
                startedTimer =
                    if (newTimers[newTimers.lastIndex].isStarted) newTimers[newTimers.lastIndex]
                    else {
                        startedTimer?.stop()
                        null
                    }
            } else {
                newTimers.add(it)
            }
        }
        timerAdapter.submitList(newTimers)
        timers.clear()
        timers.addAll(newTimers)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        if (startedTimer == null || startedTimer?.timeLeft == 0L) return
        val startIntent = Intent(this, ForegroundService::class.java)
        startIntent.putExtra(COMMAND_ID, COMMAND_START)
        startIntent.putExtra(STARTED_TIMER_TIME_MS, startedTimer?.timeLeft)
        startIntent.putExtra(TIMER_ID, startedTimer?.id)
        startService(startIntent)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }
}