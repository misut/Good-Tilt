package com.goodtilt.goodtilt.source

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.hardware.SensorEvent
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs
import kotlin.math.sqrt

class TiltView2 : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val greenPaint: Paint = Paint()
    private val blackPaint: Paint = Paint()
    private val sampling = 1000
    private var outerPath = Path()
    private var innerPath = Path()
    private var tanPath = Path()
    private var targetPath = Path()

    private var centerX = 0F
    private var centerY = 0F

    private var xCoeff = 0F
    private var yCoeff = 0F
    private var tan = 0F
    private var inner = 0F
    private var outer = 0F

    private var coeff = 0F

    private var xCoord = 0F
    private var yCoord = 0F

    init {
        greenPaint.color = Color.GREEN
        blackPaint.style = Paint.Style.STROKE
    }

    // x*pos[0]*pos[0]+y*pos[1]*pos[1] = r * r
    private fun graphPath(xCoeff : Float, yCoeff : Float, r : Float) : Path {
        val path = Path()

        val maxX = sqrt(r * r / xCoeff)
        val minX = -maxX
        val Fx : (Float) -> Float = {x -> sqrt(abs((r * r - x * x * xCoeff)) / yCoeff)}

        val sample = (maxX - minX) / sampling.toFloat()
        path.moveTo(  minX * coeff, 0F)
        for (i in 1..sampling) {
            val newX = minX + i * sample
            path.lineTo( newX * coeff,Fx(newX) * coeff)
        }
        path.lineTo( maxX * coeff, 0F)
        for (i in 1..sampling) {
            val newX = maxX - i * sample
            path.lineTo( newX * coeff,-Fx(newX) * coeff)
        }
        path.lineTo( minX * coeff, 0F)
        //path.close()
        path.offset(centerX, centerY)
        return path
    }

    fun updatePath() {
        innerPath = graphPath(xCoeff, yCoeff, inner)
        outerPath = graphPath(xCoeff, yCoeff, outer)
        tanPath = Path()
        tanPath.moveTo(- centerX * 0.5F, - centerY * 0.5F)
        tanPath.lineTo(centerX * 0.5F, centerY * 0.5F)
        tanPath.moveTo(- centerX * 0.5F, centerY * 0.5F)
        tanPath.lineTo(centerX * 0.5F, - centerY * 0.5F)
        tanPath.offset(centerX, centerY)
    }


    fun updateSetting(x: Float, y: Float, i: Float, o: Float, t: Float) {
        xCoeff = x
        yCoeff = y
        inner = i
        outer = o
        tan = t
        updatePath()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        centerX = w / 2f
        centerY = h / 2f
        coeff = centerX / 45F
        updatePath()
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        //canvas?.drawCircle(cX, cY, 100f, blackPaint)
        canvas?.drawCircle(xCoord + centerX, yCoord + centerY, 10f, greenPaint)
        //canvas?.drawLine(centerX - 100, centerY, centerX + 100, centerY, blackPaint)
        //canvas?.drawLine(centerX , centerY - 100, centerX , centerY + 100, blackPaint)

        canvas?.drawPath(outerPath, blackPaint)
        canvas?.drawPath(innerPath, blackPaint)
        canvas?.drawPath(tanPath, blackPaint)
    }

    fun initPosition(){
        xCoord = 0F
        yCoord = 0F
        invalidate()
    }

    fun onSensorEvent(event: SensorEvent) {
        xCoord = event.values[0] * coeff
        yCoord = event.values[1] * coeff
        invalidate()
    }

}
