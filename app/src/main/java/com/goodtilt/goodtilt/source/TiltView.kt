package com.goodtilt.goodtilt.source

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.SensorEvent
import android.util.AttributeSet
import android.view.View

// TiltView 커스텀뷰
// xml을 사용하지 않고 직접 제작해서 만드는 뷰(화면)
// View를 상속받는데 생성자를 오버라이드함 <context를 받을 수 있게>
// context는 어플리케이션 시스템 관리 객체이다
// 코틀린,자바로 뷰를 제작하려면 context를 받는게 약속이다

class TiltView : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    // 그릴 때 붓의 역할하는 객체 만들기
    private val greenPaint: Paint = Paint()
    private val blackPaint: Paint = Paint()

    // 중점 변수 선언
    private var cX: Float = 0f
    private var cY: Float = 0f

    // 센서 x,y값 변수
    private var xCoord: Float = 0f
    private var yCoord: Float = 0f

    private var xDefault = 0f
    private var yDefault = 0f

    init {
        // 색깔을 초록색으로 설정
        greenPaint.color = Color.GREEN
        // 외곽선만 그리게 설정
        blackPaint.style = Paint.Style.STROKE
    }

    // 뷰의 크기가 변경될 때 호출되는 메소드
    // 그런데 처음에 뷰의 크기가 정해지면 어차피 호출됨
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // w: 변경된 뷰 가로 길이, h: 세로 길이
        // oldw: 변경 전 뷰 가로 길이, oldh: 세로 길이
        // 중점 변수 초기화
        cX = w / 2f
        cY = h / 2f
    }

    // 그림 그리기 메소드
    override fun onDraw(canvas: Canvas?) {
        //바깥 검정색 외곽만 그려진 원
        canvas?.drawCircle(cX, cY, 100f, blackPaint)
        // 안쪽 녹색으로 채워진 원
        // [센서의 x값과 y값이 변함에 따라 그려지는 원의 위치도 다름]
        canvas?.drawCircle(xCoord + cX, yCoord + cY, 100f, greenPaint)
        // 가운데 십자가
        canvas?.drawLine(cX - 20, cY, cX + 20, cY, blackPaint)
        canvas?.drawLine(cX, cY - 20, cX, cY + 20, blackPaint)
    }

    // 센서가 변하고 onSensorChanged에서 호출시킴
    fun onSensorEvent(event: SensorEvent) {
        // 화면을 가로로 돌렸으므로 x축과 y축을 서로 바꿈
        // 값이 너무 작으므로 20을 곱함
        xCoord = xDefault + event.values[0] * 40
        yCoord = yDefault + event.values[1] * 40
        // 다시 그리기 메소드
        invalidate()
    }

    fun setDefaultPosition() {
        xDefault = xDefault -xCoord
        yDefault = yDefault -yCoord
        invalidate()
    }
}
//https://shacoding.com/2019/08/25/android-%EC%88%98%ED%8F%89-%EC%B8%A1%EC%A0%95%EA%B8%B0-%EB%A0%88%ED%8D%BC%EB%9F%B0%EC%8A%A4-with-%EC%BD%94%ED%8B%80%EB%A6%B0/