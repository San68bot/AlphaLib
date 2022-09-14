package com.san68bot.alphaLib.motion.localizer.method

import com.san68bot.alphaLib.geometry.Angle
import com.san68bot.alphaLib.geometry.Point
import com.san68bot.alphaLib.geometry.Pose
import com.san68bot.alphaLib.motion.localizer.Localizer
import com.san68bot.alphaLib.motion.localizer.WorldPosition.world_angle
import com.san68bot.alphaLib.motion.localizer.WorldPosition.world_pose
import com.san68bot.alphaLib.utils.epsilonEquals
import com.san68bot.alphaLib.wrappers.AGEncoder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class ThreeWheelLocalizer(
    wheelDia: Double, ticksPerRev: Double,
    private val lateralTrackWidth: Double, private val auxTrackWidth: Double,
    private val left: AGEncoder, private val right: AGEncoder, private val aux: AGEncoder
): Localizer() {
    val leftTicks get() = left.currentPos
    val rightTicks get() = right.currentPos
    val auxTicks get() = aux.currentPos

    private var last_left = 0.0
    private var last_right = 0.0
    private var last_aux = 0.0

    private var angleRadBias = 0.0
    private var lastRawAngle = 0.0

    private val inchesPerTick = (wheelDia * PI) / ticksPerRev

    override fun update() {
        val left_delta = (leftTicks - last_left) * inchesPerTick
        val right_delta = (rightTicks - last_right) * inchesPerTick
        val aux_delta = (auxTicks - last_aux) * inchesPerTick

        val angleIncrement = (left_delta - right_delta) / lateralTrackWidth

        val leftTotal = leftTicks * inchesPerTick
        val rightTotal = rightTicks * inchesPerTick
        lastRawAngle = ((leftTotal - rightTotal) / lateralTrackWidth)
        val finalAngleRad = lastRawAngle + angleRadBias

        val aux_prediction = angleIncrement * auxTrackWidth

        val yDelta = (left_delta + right_delta) / 2.0
        val xDelta = aux_delta - aux_prediction

        val dtheta = Angle.createWrappedRad(angleIncrement).rad
        val (sineTerm, cosTerm) = if (dtheta epsilonEquals 0.0) {
            1.0 - dtheta * dtheta / 6.0 to dtheta / 2.0
        } else {
            sin(dtheta) / dtheta to (1.0 - cos(dtheta)) / dtheta
        }
        val strafe = cosTerm * yDelta + sineTerm * xDelta
        val move = sineTerm * yDelta - cosTerm * xDelta

        val finalDelta = Point(move * world_angle.sin + strafe * world_angle.cos, move * world_angle.cos - strafe * world_angle.sin)
        world_pose = Pose(world_pose.point + finalDelta, Angle.createUnwrappedRad(finalAngleRad))

        last_left = leftTicks
        last_right = rightTicks
        last_aux = auxTicks
    }

    override fun reset(pose: Pose) {
        angleRadBias = pose.rad - lastRawAngle
    }
}