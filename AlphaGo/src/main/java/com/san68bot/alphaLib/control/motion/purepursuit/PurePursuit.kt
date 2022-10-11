package com.san68bot.alphaLib.control.motion.purepursuit

import com.san68bot.alphaLib.control.motion.drive.DriveMotion
import com.san68bot.alphaLib.control.motion.drive.DriveMotion.drive_omega
import com.san68bot.alphaLib.control.motion.drive.DriveMotion.drive_xv
import com.san68bot.alphaLib.control.motion.drive.DriveMotion.drive_yv
import com.san68bot.alphaLib.control.motion.drive.DriveMotion.turnToTheta
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_deg
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_point
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_x
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_y
import com.san68bot.alphaLib.geometry.Angle.Companion.degrees
import com.san68bot.alphaLib.geometry.Point
import com.san68bot.alphaLib.geometry.Pose
import com.san68bot.alphaLib.utils.math.angleToEuler
import com.san68bot.alphaLib.utils.math.halfCircleArctan
import com.san68bot.alphaLib.utils.math.halfCircleToUnitCircle
import com.san68bot.alphaLib.utils.math.notNaN
import kotlin.math.*

object PurePursuit {
    private var lastIndex = 0
    private var lastCurvePointIndex = 0
    private var finishingMove = false
    private var followMe: Point? = null
    private var angleError = 0.0
    private const val threshold = 0.003

    fun reset() {
        lastIndex = 0
        lastCurvePointIndex = 0
        finishingMove = false
        followMe = null
        angleError = 0.0
    }

    private fun findCurvePoint(allCurvePoints: ArrayList<CurvePoint>, robot: Point): CurvePoint {
        if (allCurvePoints.size < 2) {
            lastCurvePointIndex = max(0, allCurvePoints.size - 1)
            return allCurvePoints[lastCurvePointIndex]
        }

        if (lastIndex > lastCurvePointIndex) lastCurvePointIndex = lastIndex

        if (allCurvePoints.size > lastCurvePointIndex + 1 && lastCurvePointIndex > 0) {
            val p0 = allCurvePoints[lastCurvePointIndex - 1].point
            val p1 = allCurvePoints[lastCurvePointIndex].point
            val p2 = allCurvePoints[lastCurvePointIndex + 1].point
            val distance0 = projectToLine(p0, p1, robot)
            val distance1 = projectToLine(p1, p2, robot)

            if ((distance1.notNaN()) && (distance0.isNaN() || distance1 < distance0))
                lastCurvePointIndex++
        }
        return allCurvePoints[lastCurvePointIndex]
    }

    private fun distanceBetweenPoints(point: Point, otherPoint: Point): Double =
        hypot(point.x - otherPoint.x, point.y - otherPoint.y)

    private fun goToFollowPoint(targetPoint: Point, robot: Point, followAngle: Double) {
        val angleBetween = halfCircleArctan(robot, targetPoint).deg
        DriveMotion.goToPoint(targetPoint.x, targetPoint.y, angleBetween + followAngle, external = false)
    }

    fun PurePursuitPath.follow(followAngle: Double = 0.0, distanceError: Double = 3.0, angleErrorDEG: Double = 5.0, reverse: Boolean = false): Boolean =
        follow(this, distanceError, angleErrorDEG, reverse, followAngle)

    fun follow(path: PurePursuitPath, distanceError: Double = 3.0, angleErrorDEG: Double = 5.0, reverse: Boolean = false, followAngle: Double = 0.0): Boolean {
        val allPoints = path.curvePoints

        if (allPoints.isEmpty()) throw IllegalArgumentException("pp path needs a point")
        
        val curvePoint = findCurvePoint(allPoints, world_point)

        val followMe = getFollowPoint(allPoints, Pose(world_point, world_deg.degrees), curvePoint.followDistance, followAngle)

        val finalPoint = allPoints.last()

        val distToEndPoint = hypot(finalPoint.point.x - world_x, finalPoint.point.y - world_y)

        if (hypot(finalPoint.point.x - world_x, finalPoint.point.y - world_y) <= curvePoint.followDistance) {
            lastIndex = allPoints.size - 1
            finishingMove = true
        }

        if (!finishingMove) {
            val followMeCurvePoint = if (lastIndex < allPoints.size - 1) allPoints[lastIndex + 1] else allPoints.last()

            goToFollowPoint(followMe, world_point, followAngle)

            if (distanceBetweenPoints(finalPoint.point, world_point) < followMeCurvePoint.followDistance / 2.0)
                drive_omega = 0.0

            (abs(drive_xv) + abs(drive_yv)).takeIf { it != 0.0 }?.let {
                drive_xv /= it
                drive_yv /= it
            }
        } else {
            val angle = when {
                (path.finalAngle).notNaN() -> path.finalAngle
                reverse -> halfCircleArctan(path.curvePoints[path.curvePoints.size - 2].point, finalPoint.point).deg - 180.0
                else -> halfCircleArctan(path.curvePoints[path.curvePoints.size - 2].point, finalPoint.point).deg
            }
            DriveMotion.goToPoint(finalPoint.point.x, finalPoint.point.y, angle, external = false)
            angleError = abs((halfCircleToUnitCircle(angle)).turnToTheta().deg)
        }
        return distToEndPoint < distanceError && angleError <= angleErrorDEG
    }
    
    private fun getFollowPoint(pathPoints: ArrayList<CurvePoint>, robot: Pose, followDistance: Double, followAngle: Double): Point {
        if (followMe == null) followMe = pathPoints[min(1, pathPoints.size - 1)].point

        for (i in 0 until min(lastIndex + 2, pathPoints.size - 1)) {
            val startLine = pathPoints[i]
            val endLine = pathPoints[i + 1]

            val intersections = lineCircleIntersection(robot.point, followDistance, startLine.point.copy(), endLine.point.copy())

            var closestAngle = 1000.0

            if (intersections.isNotEmpty()) lastIndex = i

            for (intersection in intersections) {
                val angle = abs(
                    angleToEuler(
                        (halfCircleArctan(robot.point, intersection).deg - (world_deg + followAngle)).degrees
                    ).deg
                )
                if (angle < closestAngle) {
                    closestAngle = angle
                    followMe = intersection
                }
            }
        }

        val lastPoint = pathPoints.last().point
        val distance = distanceBetweenPoints(robot.point, lastPoint)
        if (distance <= followDistance) followMe = lastPoint
        return followMe!!
    }

    private fun lineCircleIntersection(center: Point, radius: Double, lineP1: Point, lineP2: Point): ArrayList<Point> {
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

    private fun projectToLine(lineP1: Point, lineP2: Point, robot: Point): Double {
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
}