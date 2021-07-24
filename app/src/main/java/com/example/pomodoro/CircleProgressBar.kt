package com.example.pomodoro

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View


class CircleProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defaultAttrs: Int = 0
): View(context, attrs, defaultAttrs) {

    private var angle = 0
    private val paint = Paint()
    private val gradientPaint = Paint()
    private var arcWidth = 0f


    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar, defaultAttrs, 0)
        arcWidth = ta.getDimension(R.styleable.CircleProgressBar_arcWidth, 2f)
    }

    override fun onDraw(canvas: Canvas?) {


        super.onDraw(canvas)

        val center = width/2
        var radius = center.toFloat() - 20
        var oval = RectF(center- radius, center-radius, center+radius, center+ radius)

        paint.apply {
            color = Color.parseColor("#FFFFFF")
            strokeWidth = arcWidth
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        gradientPaint.apply {
            style = Paint.Style.FILL
            color = Color.argb(100 + 155 * angle / 360,100 + 155 * angle / 360,0,0)
            strokeWidth = arcWidth
            isAntiAlias = true
        }

        canvas?.drawArc(oval,-90f, angle.toFloat(), true, gradientPaint)
        radius -= arcWidth/2
        oval = RectF(center- radius, center-radius, center+radius, center+ radius)
        canvas?.drawArc(oval,0f, 360f, true, paint)
    }

    fun setProgress(timeLeft: Long, initTime: Long) {
        if(initTime != 0L) angle = (360 - 360 * timeLeft / initTime).toInt()
        else angle = 360
        invalidate()
    }


}