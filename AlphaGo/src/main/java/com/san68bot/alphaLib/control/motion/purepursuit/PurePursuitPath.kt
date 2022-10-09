package com.san68bot.alphaLib.control.motion.purepursuit

import com.san68bot.alphaLib.geometry.Point

class PurePursuitPath(private var followDistance: Double) {
    private val firstFollowDistance = followDistance
    val curvePoints = ArrayList<CurvePoint>()
    var finalAngle = Double.NaN

    val last get() = curvePoints.last().point

    fun reset() {
        followDistance = firstFollowDistance
    }

    private fun add(point: Point) {
        curvePoints.add(CurvePoint(point, followDistance))
    }

    fun add(vararg point: Point) {
        point.forEach { add(it) }
    }

    fun addX(x: Double) {
        add(Point(x, last.y))
    }

    fun addY(y: Double) {
        add(Point(last.x, y))
    }
}