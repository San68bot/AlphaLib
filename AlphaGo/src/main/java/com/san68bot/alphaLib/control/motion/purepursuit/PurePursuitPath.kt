package com.san68bot.alphaLib.control.motion.purepursuit

import com.san68bot.alphaLib.geometry.Point

data class CurvePoint (val point: Point, val followDistance: Double)

class PurePursuitPath(private val followDistance: Double) {
    val curvePoints = ArrayList<CurvePoint>()
    var finalAngle = Double.NaN

    private val last get() = curvePoints.last().point

    fun add(point: Point) {
        curvePoints.add(CurvePoint(point, followDistance))
    }

    fun add(vararg point: Point) {
        point.forEach { add(it) }
    }

    fun append_x(x: Double) {
        add(Point(x, last.y))
    }

    fun append_y(y: Double) {
        add(Point(last.x, y))
    }

    infix fun finalAngle(angle: Double): PurePursuitPath {
        finalAngle = angle
        return this
    }
}

fun PurePursuitPath(
    followDistance: Double,
    block: PurePursuitPath.() -> Unit
): PurePursuitPath = PurePursuitPath(followDistance).apply(block)