package com.example.pomodoro

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.view.View
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoro.databinding.TimerItemBinding

class TimerViewHolder(
    private val binding: TimerItemBinding,
    private val listener: TimerListener,
    private val resources: Resources
) : RecyclerView.ViewHolder(binding.root) {
    private  var _currentTimer: Timer? = null
    private  val currentTimer: Timer get() = _currentTimer!!
    fun bind(timer: Timer) {
        if (_currentTimer != null){
            if(timer != _currentTimer) {
            _currentTimer?.isAttached = false
            _currentTimer = timer
            timer.holder = this
            timer.isAttached = true
            }
        } else _currentTimer = timer

        if (timer.isStarted) {
            startTimer(timer)
        } else {
            stopTimer(timer)
        }

        initButtonsListeners()
        updateFields()
    }

    private fun startTimer(timer: Timer) {
        timer.start()
    }

    private fun initButtonsListeners() {
        binding.startPauseButton.setOnClickListener {
            if (currentTimer.isStarted) {
                stopTimer(currentTimer)
                listener.stop(currentTimer.id, currentTimer.timeLeft)
                binding.startPauseButton.text = resources.getString(R.string.btn_text_start)
            } else {
                listener.start(currentTimer.id)
                binding.startPauseButton.text = resources.getString(R.string.btn_text_stop)
            }
        }
        binding.deleteButton.setOnClickListener {
            listener.delete(currentTimer.id)
        }
    }

    private fun stopTimer(timer: Timer) {
        timer.stop()
    }

    fun updateFields() {
        binding.circleProgressBar.setProgress(currentTimer.timeLeft, currentTimer.initialTime)
        binding.timerTimeLeft.text = currentTimer.timeLeft.displayTime()
        binding.startPauseButton.text =
            if (currentTimer.isStarted) resources.getString(R.string.btn_text_stop) else resources.getString(
                R.string.btn_text_start
            )
        binding.blinkingIndicator.isInvisible = !currentTimer.isStarted

        binding.startPauseButton.visibility =
            if (currentTimer.timeLeft == 0L) View.INVISIBLE else View.VISIBLE
        binding.container.setBackgroundColor(listener.getBGColor(currentTimer.timeLeft == 0L))

        with(binding.blinkingIndicator.background as? AnimationDrawable) {
            if (currentTimer.isStarted) this?.start()
            else this?.stop()
        }
    }

    fun makeFinalNotification(){
        listener.makeFinalNotification()
    }

}