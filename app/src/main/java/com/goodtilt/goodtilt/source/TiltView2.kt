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

    private var uCoeff = 0F
    private var dCoeff = 0F
    private var lCoeff = 0F
    private var rCoeff = 0F
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
    private fun graphPath(r : Float) : Path {
        val path = Path()

        val maxX = sqrt(r * r / rCoeff)
        val minX = -sqrt(r * r / lCoeff)
        val maxFx : (Float) -> Float = {x -> if (x<0) sqrt(abs((r * r - x * x * lCoeff)) / uCoeff) else sqrt(abs((r * r - x * x * rCoeff)) / uCoeff) }
        val minFx : (Float) -> Float = {x -> if (x<0) -sqrt(abs((r * r - x * x * lCoeff)) / dCoeff) else -sqrt(abs((r * r - x * x * rCoeff)) / dCoeff) }

        val sample = (maxX - minX) / sampling.toFloat()
        path.moveTo(  minX * coeff, 0F)
        for (i in 1..sampling) {
            val newX = minX + i * sample
            path.lineTo( newX * coeff,maxFx(newX) * coeff)
        }
        path.lineTo( maxX * coeff, 0F)
        for (i in 1..sampling) {
            val newX = maxX - i * sample
            path.lineTo( newX * coeff,minFx(newX) * coeff)
        }
        path.lineTo( minX * coeff, 0F)
        //path.close()
        path.offset(centerX, centerY)
        return path
    }

    fun updatePath() {
        innerPath = graphPath(inner)
        outerPath = graphPath(outer)
        tanPath = Path()
        tanPath.moveTo(- centerX * 0.5F, - centerY * 0.5F)
        tanPath.lineTo(centerX * 0.5F, centerY * 0.5F)
        tanPath.moveTo(- centerX * 0.5F, centerY * 0.5F)
        tanPath.lineTo(centerX * 0.5F, - centerY * 0.5F)
        tanPath.offset(centerX, centerY)
    }


    fun updateSetting(u: Float, d: Float, l:Float, r: Float, i: Float, o: Float, t: Float) {
        uCoeff = u
        dCoeff = d
        lCoeff = l
        rCoeff = r
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
