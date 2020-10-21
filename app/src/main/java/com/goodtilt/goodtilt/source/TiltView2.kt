package com.goodtilt.goodtilt.source

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.hardware.SensorEvent
import android.util.AttributeSet
import android.view.View
import com.goodtilt.goodtilt.R
import kotlinx.android.synthetic.main.frag_tilt.view.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
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

    var centerX = 0F
    var centerY = 0F

    private var uCoeff = 0F
    private var dCoeff = 0F
    private var iCoeff = 0F
    private var oCoeff = 0F
    var rad1 = 0F
    var rad2 = 0F
    var rad3 = 0F
    var rad4 = 0F
    private var inner = 0F
    private var outer = 0F

    private var coeff = 0F

    var xCoord = 0F
    var yCoord = 0F
    var rightHand = true

    init {
        greenPaint.color = Color.GREEN
        blackPaint.style = Paint.Style.STROKE
    }

    // x*pos[0]*pos[0]+y*pos[1]*pos[1] = r * r
    private fun graphPath(r : Float) : Path {
        val path = Path()
        val lCoeff = if(rightHand) oCoeff else iCoeff
        val rCoeff = if(rightHand) iCoeff else oCoeff

        val maxX = sqrt(r * r / rCoeff)
        val minX = -sqrt(r * r / lCoeff)
        val maxFx : (Float) -> Float = {x -> if (x<0) sqrt(abs((r * r - x * x * lCoeff)) / dCoeff) else sqrt(abs((r * r - x * x * rCoeff)) / dCoeff) }
        val minFx : (Float) -> Float = {x -> if (x<0) -sqrt(abs((r * r - x * x * lCoeff)) / uCoeff) else -sqrt(abs((r * r - x * x * rCoeff)) / uCoeff) }

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

    fun getCoord() : FloatArray {
        return floatArrayOf(xCoord / coeff, yCoord / coeff)
    }

    fun radPosition(radId : Int): Pair<Int, Int>{
        var rad = 0F
        when(radId){
            1 -> rad = rad1
            2 -> rad = rad2
            3 -> rad = rad3
            4 -> rad = rad4
        }
        val rx = cos(rad) * centerX * 0.75F * if (rightHand) 1F else -1F
        val ry = sin(rad) * centerX * 0.75F
        val offset = resources.getDimension(R.dimen.margin_side) / 2F
        return Pair((centerX + rx - offset).toInt(), (centerY + ry - offset).toInt())
    }

    fun updatePath() {
        val baselen = centerX * 0.75F
        val symmetric = if (rightHand) 1F else -1F
        innerPath = graphPath(inner)
        outerPath = graphPath(outer)
        tanPath = Path()
        tanPath.moveTo(symmetric * baselen * cos(rad1), baselen * sin(rad1))
        tanPath.lineTo(0F, 0F)
        tanPath.moveTo(symmetric *baselen * cos(rad2), baselen * sin(rad2))
        tanPath.lineTo(0F, 0F)
        tanPath.moveTo(symmetric * baselen * cos(rad3), baselen * sin(rad3))
        tanPath.lineTo(0F, 0F)
        tanPath.moveTo(symmetric * baselen * cos(rad4), baselen * sin(rad4))
        tanPath.lineTo(0F, 0F)
        tanPath.offset(centerX, centerY)
    }

    fun updateSetting(u: Float, d: Float, i:Float, o: Float, inn: Float, out: Float, r1: Float, r2: Float, r3: Float, r4: Float) {
        uCoeff = u
        dCoeff = d
        iCoeff = i
        oCoeff = o
        inner = inn
        outer = out
        rad1 = r1
        rad2 = r2
        rad3 = r3
        rad4 = r4
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
