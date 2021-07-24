package com.example.pomodoro

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

@RequiresApi(Build.VERSION_CODES.M)
class ForegroundService : Service() {

    private var isServiceStarted = false
    private var notificationManager: NotificationManager? = null
    private var job: Job? = null

    private val builder by lazy {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setGroup(getString(R.string.timer))
            .setGroupSummary(false)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getPendingIntent())
            .setSilent(true)
            .setSmallIcon(R.drawable.ic_baseline_access_alarm_24)


    }

    override fun onCreate() {
        super.onCreate()
        notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        processCommand(intent)
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun processCommand(intent: Intent?) {


        when (intent?.extras?.getString(COMMAND_ID) ?: INVALID) {
            COMMAND_START -> {
                builder.setContentTitle(
                    getString(R.string.notification_label_timer_id) +
                        " ${intent?.extras?.getInt(TIMER_ID) ?: getString(R.string.unknow_id)}")
                val timeLeft = intent?.extras?.getLong(STARTED_TIMER_TIME_MS) ?: return
                commandStart(timeLeft)
            }
            COMMAND_STOP -> commandStop()
            INVALID -> return
        }
    }

    private fun commandStart(timeLeft: Long) {
        if (isServiceStarted) {
            return
        }
        try {
            moveToStartedState()
            startForegroundAndShowNotification()
            continueTimer(timeLeft)
        } finally {
            isServiceStarted = true
        }
    }

    private fun continueTimer(timeLeft: Long) {
        job = GlobalScope.launch(Dispatchers.Main) {
            val endTime = timeLeft/1000 * 1000 + SystemClock.elapsedRealtime()
            var message: String

            while (true) {
                message = (endTime - SystemClock.elapsedRealtime()).displayTime()

                notificationManager?.notify(
                    NOTIFICATION_ID,
                    if(message == getString(R.string.time_is_out)) getNotification(message, true)
                    else getNotification(message, false)
                )
                if(message == getString(R.string.time_is_out)) break
                delay(INTERVAL)
            }
        }
    }

    private fun commandStop() {
        if (!isServiceStarted) {
            return
        }
        Log.i("TAG", "commandStop()")
        try {
            job?.cancel()
            stopForeground(true)
            stopSelf()
        } finally {
            isServiceStarted = false
        }
    }

    private fun moveToStartedState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("TAG", "moveToStartedState(): Running on Android O or higher")
            startForegroundService(Intent(this, ForegroundService::class.java))
        } else {
            Log.d("TAG", "moveToStartedState(): Running on Android N or lower")
            startService(Intent(this, ForegroundService::class.java))
        }
    }

    private fun startForegroundAndShowNotification() {
        createChannel()
        val notification = getNotification("content", false)
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun getNotification(content: String, isFinished: Boolean): Notification {
        with(builder){
            if(isFinished){
                color = getColor(R.color.alarm)
                setColorized(true)
            }
            setContentText(content).build()
        }
        return builder.build()
    }


    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "pomodoro"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(
                CHANNEL_ID, channelName, importance
            )
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }

    private fun getPendingIntent(): PendingIntent? {
        val resultIntent = Intent(this, MainActivity::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_ONE_SHOT)
    }

    private companion object {
        private const val CHANNEL_ID = "Channel_ID"
        private const val NOTIFICATION_ID = 777
        private const val INTERVAL = 1000L
    }
}