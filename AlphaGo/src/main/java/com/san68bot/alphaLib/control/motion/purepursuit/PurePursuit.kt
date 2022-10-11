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
import com.san68bot.alphaLib.control.motion.purepursuit.PurePursuitMath.lineCircleIntersection
import com.san68bot.alphaLib.control.motion.purepursuit.PurePursuitMath.projectToLine
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

    fun reset() {
        lastIndex = 0
        lastCurvePointIndex = 0
        finishingMove = false
        followMe = null
        angleError = 0.0
    }

    fun PurePursuitPath.followPath(
        distanceError: Double = 3.0,
        angleErrorDEG: Double = 5.0,
        reverse: Boolean = false,
        followAngle: Double = 0.0
    ): Boolean = follow(this, distanceError, angleErrorDEG, reverse, followAngle)

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

            val angleBetween = halfCircleArctan(world_point, followMe).deg
            DriveMotion.goToPoint(followMe.x, followMe.y, angleBetween + followAngle, external = false)

            if ((finalPoint.point - world_point).hypot < followMeCurvePoint.followDistance / 2.0)
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
        val distance = (robot.point - lastPoint).hypot
        if (distance <= followDistance) followMe = lastPoint
        return followMe!!
    }
}