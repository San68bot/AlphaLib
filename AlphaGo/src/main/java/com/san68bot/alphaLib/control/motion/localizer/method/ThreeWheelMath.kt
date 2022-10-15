package com.san68bot.alphaLib.control.motion.localizer.method

import com.san68bot.alphaLib.geometry.Angle
import com.san68bot.alphaLib.geometry.Point
import com.san68bot.alphaLib.geometry.Pose
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_angle
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_pose
import com.san68bot.alphaLib.geometry.Angle.Companion.radians
import com.san68bot.alphaLib.utils.math.epsilonEquals
import kotlin.math.cos
import kotlin.math.sin

object ThreeWheelMath {
    private var last_left = 0.0
    private var last_right = 0.0
    private var last_aux = 0.0

    private var angleOffset = 0.0
    private var lastAngle = 0.0

    private var xInchesTraveled = 0.0
    private var yInchesTraveled = 0.0

    fun update(
        leftTicks: Double, rightTicks: Double, auxTicks: Double,
        inchesPerTick: Double, lateralTrackWidth: Double, auxTrackWidth: Double
    ) {
        val left_delta = (leftTicks - last_left) * inchesPerTick
        val right_delta = (rightTicks - last_right) * inchesPerTick
        val aux_delta = (auxTicks - last_aux) * inchesPerTick

        val angleDelta = (left_delta - right_delta) / lateralTrackWidth

        val leftTotal = leftTicks * inchesPerTick
        val rightTotal = rightTicks * inchesPerTick

        lastAngle = ((leftTotal - rightTotal) / lateralTrackWidth)
        val finalAngle = lastAngle + angleOffset

        val auxRotatePrediction = angleDelta * auxTrackWidth

        val xDelta = aux_delta - auxRotatePrediction
        val yDelta = (left_delta + right_delta) / 2.0

        val dtheta = (angleDelta.radians).rad
        val (sineTerm, cosTerm) = if (dtheta epsilonEquals 0.0) {
            1.0 - dtheta * dtheta / 6.0 to dtheta / 2.0
        } else {
            sin(dtheta) / dtheta to (1.0 - cos(dtheta)) / dtheta
        }
        val x_move = cosTerm * yDelta + sineTerm * xDelta
        val y_move = sineTerm * yDelta - cosTerm * xDelta

        val finalDelta = Point(y_move * world_angle.sin + x_move * world_angle.cos, y_move * world_angle.cos - x_move * world_angle.sin)
        world_pose = Pose(world_pose.point + finalDelta, finalAngle.radians)

        xInchesTraveled += xDelta
        yInchesTraveled += yDelta

        last_left = leftTicks
        last_right = rightTicks
        last_aux = auxTicks
    }

    fun xInchesTraveled() = xInchesTraveled
    fun yInchesTraveled() = yInchesTraveled

    fun reset(pose: Pose) {
        angleOffset = pose.rad - lastAngle
    }
}
