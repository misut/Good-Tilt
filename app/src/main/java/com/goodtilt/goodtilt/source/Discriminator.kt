package com.goodtilt.goodtilt.source

import kotlin.math.sqrt

enum class DeviceStatus(val actionIndex: Int) {
    IDLE(-1),
    TILT_LEFT(0),
    TILT_RIGHT(1),
    TILT_UP(2),
    TILT_DOWN(3),
    STOPOVER(4);
}

class Discriminator(var x: Float, var y: Float, var inner: Float, var outer: Float, var tan: Float) {
    constructor() : this(0.5f,0.5f,10.0f,20.0f, 1.3f)

    private var status = DeviceStatus.IDLE

    private fun ellipsify(pos: FloatArray): Float {
        return sqrt(x*pos[0]*pos[0]+y*pos[1]*pos[1]).toFloat()
    }

    fun getStatus(): DeviceStatus {
        return status
    }

    fun updateStatus(pos: FloatArray) {
        var res = ellipsify(pos)
        var cur = pos[1]/pos[0]
        when(status) {
            DeviceStatus.IDLE -> {
                if(res >= outer) {
                    if(cur < tan && cur > -tan) {
                        if(pos[0] < 0)
                            status = DeviceStatus.TILT_LEFT
                        else
                            status = DeviceStatus.TILT_RIGHT
                    }
                    else {
                        if(pos[1] < 0)
                            status = DeviceStatus.TILT_UP
                        else
                            status = DeviceStatus.TILT_DOWN
                    }
                }
                else {
                    status = DeviceStatus.IDLE
                }
            }
            DeviceStatus.TILT_LEFT,
            DeviceStatus.TILT_RIGHT,
            DeviceStatus.TILT_UP,
            DeviceStatus.TILT_DOWN -> {
                if(res < outer) {
                    status = DeviceStatus.STOPOVER
                }
            }
            DeviceStatus.STOPOVER -> {
                if(res < inner) {
                    status = DeviceStatus.IDLE
                }
            }
        }
    }

    fun updateSetting(x: Float, y: Float, inner: Float, outer: Float, tan: Float) {
        this.x = x
        this.y = y
        this.inner = inner
        this.outer = outer
        this.tan = tan
    }
}