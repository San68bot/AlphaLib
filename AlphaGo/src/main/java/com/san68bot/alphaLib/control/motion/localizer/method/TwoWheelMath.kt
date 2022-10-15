package com.san68bot.alphaLib.control.motion.localizer.method

import com.san68bot.alphaLib.control.motion.localizer.WorldPosition
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_angle
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_pose
import com.san68bot.alphaLib.geometry.Angle
import com.san68bot.alphaLib.geometry.Angle.Companion.radians
import com.san68bot.alphaLib.geometry.Point
import com.san68bot.alphaLib.geometry.Pose
import com.san68bot.alphaLib.geometry.TAU
import com.san68bot.alphaLib.utils.math.fullCircleToBisectedArc
import com.san68bot.alphaLib.utils.math.epsilonEquals
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object TwoWheelMath {
    private var last_horizonal = 0.0
    private var last_vertical = 0.0

    private var angleOffset = 0.0
    private var lastAngle = 0.0

    private var xInchesTraveled = 0.0
    private var yInchesTraveled = 0.0

    fun update(
        horizontalTicks: Double, verticalTicks: Double, imuAngle: Double,
        inchesPerTick: Double, xTrackWidth: Double, yTrackWidth: Double
    ) {
        val x_delta = (horizontalTicks - last_horizonal) * inchesPerTick
        val y_delta = (verticalTicks - last_vertical) * inchesPerTick

        val angleDelta = (imuAngle - lastAngle).angleWrapRad

        val finalAngle = imuAngle + angleOffset

        val xPrediction = angleDelta * xTrackWidth
        val yPrediction = angleDelta * yTrackWidth

        val r_x = x_delta - xPrediction
        val r_y = y_delta - yPrediction

        val dtheta = (fullCircleToBisectedArc(angleDelta.radians)).rad
        val (sineTerm, cosTerm) = if (dtheta epsilonEquals 0.0) {
            1.0 - dtheta * dtheta / 6.0 to dtheta / 2.0
        } else {
            sin(dtheta) / dtheta to (1.0 - cos(dtheta)) / dtheta
        }
        val x_move = cosTerm * r_y + sineTerm * r_x
        val y_move = sineTerm * r_y - cosTerm * r_x

        val finalDelta = Point(y_move * world_angle.sin + x_move * world_angle.cos, y_move * world_angle.cos - x_move * world_angle.sin)
        world_pose = Pose(world_pose.point + finalDelta, finalAngle.radians)

        xInchesTraveled += r_x
        yInchesTraveled += r_y

        last_horizonal = horizontalTicks
        last_vertical = verticalTicks
        lastAngle = imuAngle
    }

    private val Double.angleWrapRad: Double
        get() {
            var angle = this
            while (angle > PI)
                angle -= TAU
            while (angle < -PI)
                angle += TAU
            return angle
        }

    fun xInchesTraveled() = xInchesTraveled
    fun yInchesTraveled() = yInchesTraveled

    fun reset(pose: Pose) {
        angleOffset = pose.rad - lastAngle
    }
}