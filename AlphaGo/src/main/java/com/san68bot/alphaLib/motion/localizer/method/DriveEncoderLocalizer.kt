package com.san68bot.alphaLib.motion.localizer.method

import com.acmerobotics.roadrunner.drive.MecanumDrive
import com.acmerobotics.roadrunner.geometry.Pose2d
import com.san68bot.alphaLib.geometry.Pose
import com.san68bot.alphaLib.geometry.TAU
import com.san68bot.alphaLib.motion.localizer.Localizer
import com.san68bot.alphaLib.motion.localizer.WorldPosition.world_pose
import com.san68bot.alphaLib.wrappers.AlphaGoMotor
import com.san68bot.alphaLib.wrappers.imu.IMU
import kotlin.math.PI

class DriveEncoderLocalizer(trackWidth: Double, wheelBase: Double, wheelDiaIN: Double, gearRatio: Double, motorTicks: Double, motors: ArrayList<AlphaGoMotor>, imu: IMU): Localizer() {
    private val driveEncoders = DriveEncoders(trackWidth, wheelBase, wheelDiaIN, gearRatio, motorTicks, motors, imu)

    private var rawPose = Pose2d()
    private var x = 0.0; private var y = 0.0; private var theta = 0.0

    private var deltaX = 0.0; private var deltaY = 0.0; private var deltaTheta = 0.0
    private var prevX = 0.0; private var prevY = 0.0; private var prevTheta = 0.0

    override fun update() {
        driveEncoders.updatePoseEstimate()
        rawPose = driveEncoders.poseEstimate

        deltaY = (rawPose.x - prevX)
        deltaX = (rawPose.y - prevY)
        deltaTheta = (rawPose.heading - prevTheta)

        x += -deltaX; y += deltaY; theta += deltaTheta

        if (theta >= TAU) theta -= TAU
        if (theta < 0.0) theta += TAU

        world_pose = Pose(x, y, theta)

        prevX = rawPose.x
        prevY = rawPose.y
        prevTheta = rawPose.heading
    }

    override fun reset(pose: Pose) {
        driveEncoders.poseEstimate = Pose2d(pose.y, -pose.x, (pose.rad - PI/2.0))
        rawPose = driveEncoders.poseEstimate

        x = -rawPose.y
        y = rawPose.x
        theta = rawPose.heading + PI/2.0

        world_pose = Pose(x, y, theta)

        prevX = rawPose.x
        prevY = rawPose.y
        prevTheta = rawPose.heading
    }

    private class DriveEncoders(trackWidth: Double, wheelBase: Double, private val wheelDiaIN: Double, private val gearRatio: Double, private val motorTicks: Double, private val motors: ArrayList<AlphaGoMotor>, private val imu: IMU): MecanumDrive(0.0, 0.0, 0.0, trackWidth, wheelBase, 1.0) {
        private fun encoderTicksToInches(ticks: Double) = wheelDiaIN * 2.0 * Math.PI * gearRatio * ticks / motorTicks
        override fun getWheelPositions(): List<Double> = motors.map { encoderTicksToInches(it.currentPosition()) }
        override fun getWheelVelocities(): List<Double> = motors.map { encoderTicksToInches(it.velocity) }

        override fun setMotorPowers(frontLeft: Double, rearLeft: Double, rearRight: Double, frontRight: Double) {}
        override val rawExternalHeading: Double get() = imu.firstAngle
        override fun getExternalHeadingVelocity(): Double = imu.zRotationRate
    }
}