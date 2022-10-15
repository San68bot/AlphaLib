package com.san68bot.alphaLib.control.motion.purepursuit

import com.san68bot.alphaLib.control.motion.drive.DriveMotion
import com.san68bot.alphaLib.control.motion.drive.DriveMotion.drive_omega
import com.san68bot.alphaLib.control.motion.drive.DriveMotion.drive_xv
import com.san68bot.alphaLib.control.motion.drive.DriveMotion.drive_yv
import com.san68bot.alphaLib.control.motion.drive.DriveMotion.pointAngle
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_deg
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_point
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_pose
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_x
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_y
import com.san68bot.alphaLib.control.motion.purepursuit.PurePursuitMath.lineCircleIntersection
import com.san68bot.alphaLib.control.motion.purepursuit.PurePursuitMath.projectToLine
import com.san68bot.alphaLib.geometry.Angle
import com.san68bot.alphaLib.geometry.Angle.Companion.degrees
import com.san68bot.alphaLib.geometry.Point
import com.san68bot.alphaLib.geometry.Pose
import com.san68bot.alphaLib.utils.math.fullCircleToBisectedArc
import com.san68bot.alphaLib.utils.math.bisectedArcArctan
import com.san68bot.alphaLib.utils.math.bisectedArcToUnitCircle
import com.san68bot.alphaLib.utils.math.notNaN
import kotlin.math.*

object PurePursuit {
    private var lastIndex = 0
    private var lastCurvePointIndex = 0
    private var finishingMove = false
    private var followMe: Point? = null
    private var currentAngleError = (0.0).degrees

    fun reset() {
        lastIndex = 0
        lastCurvePointIndex = 0
        finishingMove = false
        followMe = null
        currentAngleError = (0.0).degrees
    }

    fun PurePursuitPath.followPath(
        distanceError: Double = 3.0,
        angleError: Angle = (5.0).degrees,
        reverse: Boolean = false,
        followAngle: Double = 0.0
    ): Boolean = follow(this, distanceError, angleError, reverse, followAngle)

    fun follow(path: PurePursuitPath, distanceError: Double = 3.0, angleError: Angle = (5.0).degrees, reverse: Boolean = false, followAngle: Double = 0.0): Boolean {
        val allPoints = path.curvePoints

        if (allPoints.isEmpty()) throw IllegalArgumentException("path cannot be empty")
        
        val curvePoint = findCurvePoint(allPoints, world_point)

        val followMe = getFollowPoint(allPoints, world_pose, curvePoint.followDistance, followAngle)

        val finalPoint = allPoints.last()

        val distToEndPoint = hypot(finalPoint.point.x - world_x, finalPoint.point.y - world_y)

        if (hypot(finalPoint.point.x - world_x, finalPoint.point.y - world_y) <= curvePoint.followDistance) {
            lastIndex = allPoints.size - 1
            finishingMove = true
        }

        if (!finishingMove) {
            val followMeCurvePoint = if (lastIndex < allPoints.size - 1) allPoints[lastIndex + 1] else allPoints.last()

            DriveMotion.goToPose(followMe.x, followMe.y, bisectedArcArctan(world_point, followMe) + followAngle.degrees)

            if ((finalPoint.point - world_point).hypot < followMeCurvePoint.followDistance / 2.0)
                drive_omega = 0.0

            (abs(drive_xv) + abs(drive_yv)).takeIf { it != 0.0 }?.let {
                drive_xv /= it
                drive_yv /= it
            }
        } else {
            val angle = when {
                (path.finalAngle).notNaN() -> path.finalAngle
                reverse -> bisectedArcArctan(path.curvePoints[path.curvePoints.size - 2].point, finalPoint.point).deg - 180.0
                else -> bisectedArcArctan(path.curvePoints[path.curvePoints.size - 2].point, finalPoint.point).deg
            }
            DriveMotion.goToPose(finalPoint.point.x, finalPoint.point.y, angle.degrees)
            currentAngleError = pointAngle(bisectedArcToUnitCircle(angle.degrees))
        }
        return distToEndPoint < distanceError && abs(currentAngleError.deg) <= angleError.deg
    }

    private fun findCurvePoint(allCurvePoints: ArrayList<CurvePoint>, robot: Point): CurvePoint {
        if (allCurvePoints.size < 2) {
            lastCurvePointIndex = max(0, allCurvePoints.size - 1)
            return allCurvePoints[lastCurvePointIndex]
        }

        if (lastIndex > lastCurvePointIndex) lastCurvePointIndex = lastIndex

        if (allCurvePoints.size > lastCurvePointIndex + 1 && lastCurvePointIndex > 0) {
            val p1 = allCurvePoints[lastCurvePointIndex].point
            val distance0 = projectToLine(allCurvePoints[lastCurvePointIndex - 1].point, p1, robot)
            val distance1 = projectToLine(p1, allCurvePoints[lastCurvePointIndex + 1].point, robot)

            if ((distance1.notNaN()) && (distance0.isNaN() || distance1 < distance0))
                lastCurvePointIndex++
        }
        return allCurvePoints[lastCurvePointIndex]
    }
    
    private fun getFollowPoint(path: ArrayList<CurvePoint>, robot: Pose, followDistance: Double, followAngle: Double): Point {
        if (followMe == null) followMe = path[min(1, path.size - 1)].point

        for (i in 0 until min(lastIndex + 2, path.size - 1)) {
            val startLine = path[i]
            val endLine = path[i + 1]

            val intersections = lineCircleIntersection(robot.point, followDistance, startLine.point.copy(), endLine.point.copy())

            var closestAngle = 1000.0

            if (intersections.isNotEmpty()) lastIndex = i

            for (intersection in intersections) {
                val angle = abs(
                    fullCircleToBisectedArc(
                        (bisectedArcArctan(robot.point, intersection).deg - (world_deg + followAngle)).degrees
                    ).deg
                )
                if (angle < closestAngle) {
                    closestAngle = angle
                    followMe = intersection
                }
            }
        }

        val lastPoint = (path.last()).point
        val distance = (robot.point - lastPoint).hypot
        if (distance <= followDistance) followMe = lastPoint
        return followMe!!
    }
}