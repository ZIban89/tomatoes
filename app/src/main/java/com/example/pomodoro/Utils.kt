package com.example.pomodoro

import kotlin.math.ceil

const val INVALID = "INVALID"
const val COMMAND_START = "COMMAND_START"
const val COMMAND_STOP = "COMMAND_STOP"
const val COMMAND_ID = "COMMAND_ID"
const val STARTED_TIMER_TIME_MS = "STARTED_TIMER_TIME"
const val TIMER_ID = "TIMER_ID"

fun Long.displayTime(): String {

    var temp = ceil(this / 1000.0 % 60).toLong()
    val s = if(temp == 60L) 0 else temp
    temp = (ceil((this - s * 1000).toDouble() / 1000 % 3600 / 60)).toLong()
    val m = if(temp == 60L) 0 else temp
    val h = (ceil((this - s * 1000 - m * 1000 * 60).toDouble() /1000 / 3600)).toLong()

    return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}"
}

private fun displaySlot(count: Long): String {
    return if (count / 10L > 0) {
        "$count"
    } else {
        "0$count"
    }
}