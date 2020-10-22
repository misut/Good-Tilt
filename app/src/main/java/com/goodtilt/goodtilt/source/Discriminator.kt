package com.goodtilt.goodtilt.source

import android.util.Log
import kotlin.math.*

enum class DeviceStatus(val actionIndex: Int) {
    IDLE(-1),
    TILT_IN(0),
    TILT_OUT(1),
    TILT_UP(2),
    TILT_DOWN(3),
    STOPOVER(8);
}

class Discriminator(var u: Float, var d: Float, var i: Float, var o: Float, var inner: Float, var outer: Float, var r1: Float, var r2: Float, var r3: Float, var r4: Float) {
    constructor() : this(0.7f,0.4f, 0.7f, 0.4f, 10.0f,15.0f, (PI/4f).toFloat(), (3*PI/4f).toFloat(), (5*PI/4f).toFloat(), (7*PI/4f).toFloat()) {
        this.t1 = tan(r1).toFloat()
        this.t2 = tan(r2).toFloat()
        this.t3 = tan(r3).toFloat()
        this.t4 = tan(r4).toFloat()
    }
    private val gradient = 0.025f
    private val mDelta = 0.04f

    private var t1 = 1.0f
    private var t2 = -1.0f
    private var t3 = 1.0f
    private var t4 = -1.0f
    private var status = DeviceStatus.IDLE

    private fun ellipsify(pos: FloatArray, rightHand: Boolean): Float {
        if(rightHand) {
            if(pos[0]>=0 && pos[1]>=0)
                return sqrt(i*pos[0]*pos[0]+d*pos[1]*pos[1]).toFloat()
            else if(pos[0]<0 && pos[1]>=0)
                return sqrt(o*pos[0]*pos[0]+d*pos[1]*pos[1]).toFloat()
            else if(pos[0]<0 && pos[1]<0)
                return sqrt(o*pos[0]*pos[0]+u*pos[1]*pos[1]).toFloat()
            else
                return sqrt(i*pos[0]*pos[0]+u*pos[1]*pos[1]).toFloat()
        }
        else {
            if(pos[0]>=0 && pos[1]>=0)
                return sqrt(o*pos[0]*pos[0]+d*pos[1]*pos[1]).toFloat()
            else if(pos[0]<0 && pos[1]>=0)
                return sqrt(i*pos[0]*pos[0]+d*pos[1]*pos[1]).toFloat()
            else if(pos[0]<0 && pos[1]<0)
                return sqrt(i*pos[0]*pos[0]+u*pos[1]*pos[1]).toFloat()
            else
                return sqrt(o*pos[0]*pos[0]+u*pos[1]*pos[1]).toFloat()
        }

    }

    fun getStatus(): DeviceStatus {
        return status
    }

    fun updateStatus(pos: FloatArray, rightHand: Boolean) {
        var res = ellipsify(pos, rightHand)
        var cur = pos[1]/pos[0] * (if (rightHand) 1F else -1F)
        when(status) {
            DeviceStatus.IDLE -> {
                if(res >= outer) {
                    if(pos[0] * (if (rightHand) 1F else -1F) > 0) {
                        if(cur in t4..t1)
                            status = DeviceStatus.TILT_IN
                        else if(cur > t1)
                            status = DeviceStatus.TILT_DOWN
                        else if(cur < t4)
                            status = DeviceStatus.TILT_UP
                    }
                    else {
                        if(cur in t2..t3)
                            status = DeviceStatus.TILT_OUT
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
            DeviceStatus.TILT_IN,
            DeviceStatus.TILT_OUT,
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

    fun updateSetting(u: Float, d: Float, i: Float, o: Float, inner: Float, outer: Float, r1: Float, r2: Float, r3: Float, r4: Float) {
        this.u = u
        this.d = d
        this.i = i
        this.o = o
        this.inner = inner
        this.outer = outer
        this.r1 = r1
        this.r2 = r2
        this.r3 = r3
        this.r4 = r4
        this.t1 = tan(r1).toFloat()
        this.t2 = tan(r2).toFloat()
        this.t3 = tan(r3).toFloat()
        this.t4 = tan(r4).toFloat()
    }

    fun feed(pos: FloatArray, status: DeviceStatus, rightHand: Boolean) {
        var res = ellipsify(pos, rightHand)
        var safezone = (outer-inner)*0.5f
        when(status) {
            DeviceStatus.TILT_IN -> {
                if(res < outer+safezone)
                    this.i = min(max(this.i + min(ln(outer+safezone-res)*gradient, mDelta), 0.2f), 1.0f)
                else
                    this.i = min(max(this.i - min(ln(res-outer-safezone)*gradient, mDelta), 0.2f), 1.0f)
            }
            DeviceStatus.TILT_OUT -> {
                if(res < outer+safezone)
                    this.o = min(max(this.o + min(ln(outer+safezone-res)*gradient, mDelta), 0.2f), 1.0f)
                else
                    this.o = min(max(this.o - min(ln(res-outer-safezone)*gradient, mDelta), 0.2f), 1.0f)
            }
            DeviceStatus.TILT_UP -> {
                if(res < outer+safezone)
                    this.u = min(max(this.u + min(ln(outer+safezone-res)*gradient, mDelta), 0.2f), 1.0f)
                else
                    this.u = min(max(this.u - min(ln(res-outer-safezone)*gradient, mDelta), 0.2f), 1.0f)
            }
            DeviceStatus.TILT_DOWN -> {
                if(res < outer+safezone)
                    this.d = min(max(this.d + min(ln(outer+safezone-res)*gradient, mDelta), 0.3f), 1.0f)
                else
                    this.d = min(max(this.d - min(ln(res-outer-safezone)*gradient, mDelta), 0.2f), 1.0f)
            }
            else -> {
            }
        }
    }
}