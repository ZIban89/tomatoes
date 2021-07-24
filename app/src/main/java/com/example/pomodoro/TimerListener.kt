package com.example.pomodoro

interface TimerListener {
    fun start(id: Int)

    fun stop(id: Int, currentMs: Long)

    fun delete(id: Int)
    fun getBGColor(isFinished: Boolean): Int
    fun makeFinalNotification()}