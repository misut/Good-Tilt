package com.goodtilt.goodtilt.source

import android.util.Log

class Quaternion (var x : Double, var y : Double, var z : Double, var w : Double) {
    constructor () : this(0.0, 0.0, 0.0, 0.0)

    constructor (array : FloatArray) : this(
        array[0].toDouble(),
        array[1].toDouble(),
        array[2].toDouble(),
        array[3].toDouble()
    )

    fun conjugate() = Quaternion(-x, -y, -z, w)

    fun isInvalid() = (x == 0.0) && (y == 0.0) && (z == 0.0)

    operator fun times(other: Quaternion) = Quaternion(
        this.w * other.x + this.x * other.w + this.y * other.z - this.z * other.y,
        this.w * other.y - this.x * other.z + this.y * other.w + this.z * other.x,
        this.w * other.z + this.x * other.y - this.y * other.x + this.z * other.w,
        this.w * other.w - this.x * other.x - this.y * other.y - this.z * other.z
    )

    fun eulerAngle() = floatArrayOf(
        Math.toDegrees(Math.asin((2 * (this.w * this.y - this.z * this.x)))).toFloat(),
        Math.toDegrees(
            Math.atan2(
                ((2 * (this.w * this.x + this.y * this.z))),
                (1.0f - 2.0f * (this.x * this.x + this.y * this.y))
            )
        ).toFloat()
        /*  roll 인데 필요 없어보임
        Math.toDegrees(
            Math.atan2(
                (2 * (this.w * this.z + this.x * this.y)),
                (1 - 2 * (this.y * this.y + this.z * this.z))
            )
        ).toFloat() */
    )
}