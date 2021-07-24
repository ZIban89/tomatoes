package com.example.pomodoro

import android.os.CountDownTimer

data class Timer(
    val id: Int,
    var timeLeft: Long,
    val initialTime: Long,
    var isStarted: Boolean
) {
    private val timer by lazy { getCountDownTimer() }
    var holder: TimerViewHolder? = null
    var isAttached = false

    private var isCountDownTimerStarted = false
    private fun getCountDownTimer(): CountDownTimer {


        return object : CountDownTimer(timeLeft, INTERVAL_MS) {

            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished
                if(isAttached) holder?.updateFields()
            }

            override fun onFinish() {
                isStarted = false
                timeLeft = 0
                if(isAttached) holder?.updateFields()
                holder?.makeFinalNotification()
            }
        }
    }

    fun stop() {

        isStarted = false
        isCountDownTimerStarted = false
        timer.cancel()
        holder?.updateFields()
    }

    fun start() {
        if (!isCountDownTimerStarted) {
            isCountDownTimerStarted = true
            timer.start()
        }
        holder?.updateFields()

    }

    companion object {
        private const val INTERVAL_MS = 50L
    }

}



