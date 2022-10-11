package com.san68bot.alphaLib.control.motion.purepursuit

import com.san68bot.alphaLib.geometry.Point
import com.san68bot.alphaLib.utils.math.notNaN
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sqrt

object PurePursuitMath {
    private const val threshold = 0.003

    fun projectToLine(lineP1: Point, lineP2: Point, robot: Point): Double {
        var distance = Double.NaN

        val minX = if (lineP1.x < lineP2.x) lineP1.x else lineP2.x
        val maxX = if (lineP1.x < lineP2.x) lineP2.x else lineP1.x

        val minY = if (lineP1.y < lineP2.y) lineP1.y else lineP2.y
        val maxY = if (lineP1.y < lineP2.y) lineP2.y else lineP1.y

        if (abs(lineP1.x - lineP2.x) < threshold)
            return if (robot.y < maxY && robot.y > minY) abs(robot.x - lineP1.x) else Double.NaN
        if (abs(lineP1.y - lineP2.y) < threshold)
            return if (robot.x < maxX && robot.x > minX) abs(robot.y - lineP1.y) else Double.NaN

        val m1 = (lineP2.y - lineP1.y) / (lineP2.x - lineP1.x)
        val m2 = -(1.0 / m1)

        val b1 = lineP1.y - m1 * lineP1.x
        val b2 = robot.y - m2 * robot.x

        val x = (m1 * b2 - m1 * b1) / (m1.pow(2.0) + 1.0)

        if (x > minX && x < maxX) {
            val y = x * m1 + b1
            distance = hypot(x - robot.x, y - robot.y)
        }
        return distance
    }

    fun lineCircleIntersection(center: Point, radius: Double, lineP1: Point, lineP2: Point): ArrayList<Point> {
        if (abs(lineP1.y - lineP2.y) < threshold) lineP1.y = lineP2.y + threshold
        if (abs(lineP1.x - lineP2.x) < threshold) lineP1.x = lineP2.x + threshold
        val points = ArrayList<Point>()

        val m1 = (lineP2.y - lineP1.y) / (lineP2.x - lineP1.x)

        val x1 = lineP1.x - center.x
        val y1 = lineP1.y - center.y

        val quadA = 1.0 + m1.pow(2.0)
        val quadB = (2.0 * m1 * y1) - (2.0 * m1.pow(2.0) * x1)
        val quadC = (m1.pow(2.0) * x1.pow(2.0)) - (2.0 * y1 * m1 * x1) + y1.pow(2.0) - radius.pow(2.0)

        var xRoot1 = (-quadB + sqrt(quadB.pow(2.0) - (4.0 * quadA * quadC))) / (2.0 * quadA)
        var yRoot1 = m1 * (xRoot1 - x1) + y1

        xRoot1 += center.x
        yRoot1 += center.y

        val minX = if (lineP1.x < lineP2.x) lineP1.x else lineP2.x
        val maxX = if (lineP1.x > lineP2.x) lineP1.x else lineP2.x

        if ((xRoot1.notNaN() && yRoot1.notNaN()) && (xRoot1 > minX && xRoot1 < maxX))
            points.add(Point(xRoot1, yRoot1))

        var xRoot2 = (-quadB - sqrt(quadB.pow(2.0) - (4.0 * quadA * quadC))) / (2.0 * quadA)
        var yRoot2 = m1 * (xRoot2 - x1) + y1

        xRoot2 += center.x
        yRoot2 += center.y

        if ((xRoot2.notNaN() && yRoot2.notNaN()) && (xRoot2 > minX && xRoot2 < maxX))
            points.add(Point(xRoot2, yRoot2))
        return points
    }
}