package com.san68bot.alphaLib.control.motion.purepursuit

import com.san68bot.alphaLib.geometry.Point
import com.san68bot.alphaLib.utils.math.notNaN
import com.san68bot.alphaLib.utils.math.withinThreshold
import kotlin.math.*

object PurePursuit {
    private var lastIndex = 0
    private var lastCurvePoint = 0
    var finishingMove = false
    private var followMe: Point? = null
    private var angleError = 0.0

    fun reset() {
        lastIndex = 0
        lastCurvePoint = 0
        finishingMove = false
        followMe = null
        angleError = 0.0
    }

    private fun findCurvePoint(curvePoints: ArrayList<CurvePoint>, robot: Point): CurvePoint {
        if (curvePoints.size < 2) {
            lastCurvePoint = max(0, curvePoints.size - 1)
            return curvePoints[lastCurvePoint]
        }

        if (lastIndex > lastCurvePoint)
            lastCurvePoint = lastIndex

        if (curvePoints.size > lastCurvePoint + 1 && lastCurvePoint > 0) {
            val p0 = curvePoints[lastCurvePoint - 1].point
            val p1 = curvePoints[lastCurvePoint].point
            val p2 = curvePoints[lastCurvePoint + 1].point
            val distance0 = projectToLine(p0, p1, robot)
            val distance1 = projectToLine(p1, p2, robot)

            if (distance1.notNaN() && (distance0.isNaN() || distance1 < distance0))
                lastCurvePoint++
        }

        return curvePoints[lastCurvePoint]
    }

    private fun projectToLine(p1: Point, p2: Point, robot: Point): Double {
        var distance = Double.NaN

        val minX = min(p1.x, p2.x)
        val maxX = max(p1.x, p2.x)

        val minY = min(p1.y, p2.y)
        val maxY = max(p1.y, p2.y)

        return when {
            (p1.x - p2.x) withinThreshold 0.003 -> if (robot.y < maxY && robot.y > minY) abs(robot.x - p1.x) else Double.NaN
            (p1.y - p2.y) withinThreshold 0.003 -> if (robot.x < maxX && robot.x > minX) abs(robot.y - p1.y) else Double.NaN
            else -> {
                val m1 = (p2.y - p1.y) / (p2.x - p1.x)
                val m2 = -(1.0 / m1)

                val b1 = p1.y - m1 * p1.x
                val b2 = robot.y - m2 * robot.x

                val x = (m1 * b2 - m1 * b1) / (m1.pow(2) + 1)

                if (x > minX && x < maxX) {
                    val y = x * m1 + b1
                    distance = hypot(x - robot.x, y - robot.y)
                }
                distance
            }
        }
    }
}