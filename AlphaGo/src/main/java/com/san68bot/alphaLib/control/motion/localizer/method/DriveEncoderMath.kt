package com.san68bot.alphaLib.control.motion.localizer.method

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.san68bot.alphaLib.control.motion.localizer.GlobalPosition
import com.san68bot.alphaLib.geometry.Pose
import com.san68bot.alphaLib.geometry.TAU
import kotlin.math.PI

object DriveEncoderMath {
    private var rawPose = Pose2d()
    private var x = 0.0; private var y = 0.0; private var theta = 0.0

    private var deltaX = 0.0; private var deltaY = 0.0; private var deltaTheta = 0.0
    private var prevX = 0.0; private var prevY = 0.0; private var prevTheta = 0.0

    private var xInchesTraveled = 0.0
    private var yInchesTraveled = 0.0

    fun update(rawPose: Pose2d) {
        this.rawPose = rawPose
        deltaY = (rawPose.x - prevX)
        deltaX = (rawPose.y - prevY)
        deltaTheta = (rawPose.heading - prevTheta)

        x += -deltaX; y += deltaY; theta += deltaTheta

        if (theta >= TAU) theta -= TAU
        if (theta < 0.0) theta += TAU

        GlobalPosition.global_pose = Pose(x, y, theta)

        xInchesTraveled += deltaX
        yInchesTraveled += deltaY

        prevX = rawPose.x
        prevY = rawPose.y
        prevTheta = rawPose.heading
    }

    fun xDelta() = deltaX
    fun yDelta() = deltaY

    fun reset(rawPose: Pose2d) {
        this.rawPose = rawPose
        x = -rawPose.y
        y = rawPose.x
        theta = rawPose.heading + PI /2.0

        GlobalPosition.global_pose = Pose(x, y, theta)

        prevX = rawPose.x
        prevY = rawPose.y
        prevTheta = rawPose.heading
    }
}