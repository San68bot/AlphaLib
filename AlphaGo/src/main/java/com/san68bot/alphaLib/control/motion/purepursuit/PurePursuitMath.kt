package com.san68bot.alphaLib.control.motion.purepursuit

import com.san68bot.alphaLib.geometry.Point
import com.san68bot.alphaLib.utils.math.notNaN
import kotlin.math.*

object PurePursuitMath {
    private const val threshold = 0.003

    fun projectToLine(p1: Point, p2: Point, robot: Point): Double {
        val minX = min(p1.x, p2.x)
        val maxX = max(p1.x, p2.x)

        if (abs(p1.x - p2.x) < threshold)
            return if (robot.y < max(p1.y, p2.y) && robot.y > min(p1.y, p2.y)) abs(robot.x - p1.x) else Double.NaN
        if (abs(p1.y - p2.y) < threshold)
            return if (robot.x < maxX && robot.x > minX) abs(robot.y - p1.y) else Double.NaN

        val m1 = (p2.y - p1.y) / (p2.x - p1.x)
        val b1 = p1.y - m1 * p1.x

        val x = (m1 * (robot.y + (1.0 / m1) * robot.x) - m1 * b1) / (m1 * m1 + 1)

        if (x > minX && x < maxX)
            return hypot(x - robot.x, (x * m1 + b1) - robot.y)

        return Double.NaN
    }

    fun lineCircleIntersection(center: Point, radius: Double, p1: Point, p2: Point): ArrayList<Point> {
        val points = ArrayList<Point>()

        if (abs(p1.y - p2.y) < threshold)
            p1.y = p2.y + threshold
        if (abs(p1.x - p2.x) < threshold)
            p1.x = p2.x + threshold

        val m1 = (p2.y - p1.y) / (p2.x - p1.x)
        val x1 = (p1.x - center.x)
        val y1 = (p1.y - center.y)

        val quadA = 1.0 + m1.pow(2.0)
        val quadB = (2.0 * m1 * y1) - (2.0 * m1.pow(2.0) * x1)
        val quadC = (m1.pow(2.0) * x1.pow(2.0)) - (2.0 * y1 * m1 * x1) + y1.pow(2.0) - radius.pow(2.0)

        var xRoot1 = (-quadB + sqrt(quadB.pow(2.0) - (4.0 * quadA * quadC))) / (2.0 * quadA)
        val yRoot1 = (m1 * (xRoot1 - x1) + y1) + center.y
        xRoot1 += center.x

        val minX = min(p1.x, p2.x)
        val maxX = max(p1.x, p2.x)

        if ((xRoot1.notNaN() && yRoot1.notNaN()) && (xRoot1 > minX && xRoot1 < maxX))
            points.add(Point(xRoot1, yRoot1))

        var xRoot2 = (-quadB - sqrt(quadB.pow(2.0) - (4.0 * quadA * quadC))) / (2.0 * quadA)
        val yRoot2 = (m1 * (xRoot2 - x1) + y1) + center.y
        xRoot2 += center.x

        if ((xRoot2.notNaN() && yRoot2.notNaN()) && (xRoot2 > minX && xRoot2 < maxX))
            points.add(Point(xRoot2, yRoot2))
        return points
    }
}