package com.goodtilt.goodtilt.source

import kotlin.math.sqrt

enum class DeviceStatus(val actionIndex: Int) {
    IDLE(-1),
    TILT_LEFT(0),
    TILT_RIGHT(1),
    TILT_UP(2),
    TILT_DOWN(3),
    STOPOVER(8);
}

class Discriminator(var u: Float, var d: Float, var l: Float, var r: Float, var inner: Float, var outer: Float, var t1: Float, var t2: Float, var t3: Float, var t4: Float) {
    constructor() : this(0.5f,0.5f, 0.5f, 0.5f, 10.0f,20.0f, 1.0f, -1.0f, 1.0f, -1.0f)

    private var status = DeviceStatus.IDLE

    private fun ellipsify(pos: FloatArray): Float {
        if(pos[0]>=0 && pos[1]>=0) {
            return sqrt(r*pos[0]*pos[0]+u*pos[1]*pos[1]).toFloat()
        }
        else if(pos[0]<0 && pos[1]>=0) {
            return sqrt(l*pos[0]*pos[0]+u*pos[1]*pos[1]).toFloat()
        }
        else if(pos[0]<0 && pos[1]<0) {
            return sqrt(l*pos[0]*pos[0]+d*pos[1]*pos[1]).toFloat()
        }
        else {
            return sqrt(r*pos[0]*pos[0]+d*pos[1]*pos[1]).toFloat()
        }
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
                    if(pos[0] > 0) {
                        if(cur in t4..t1)
                            status = DeviceStatus.TILT_RIGHT
                        else if(cur > t1)
                            status = DeviceStatus.TILT_DOWN
                        else if(cur < t4)
                            status = DeviceStatus.TILT_UP
                    }
                    else {
                        if(cur in t2..t3)
                            status = DeviceStatus.TILT_LEFT
                        else if(cur > t3)
                            status = DeviceStatus.TILT_UP
                        else if(cur < t2)
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

    fun updateSetting(u: Float, d: Float, l: Float, r: Float, inner: Float, outer: Float, t1: Float, t2: Float, t3: Float, t4: Float) {
        this.u = u
        this.d = d
        this.l = l
        this.r = r
        this.inner = inner
        this.outer = outer
        this.t1 = t1
        this.t2 = t2
        this.t3 = t3
        this.t4 = t4
    }
}