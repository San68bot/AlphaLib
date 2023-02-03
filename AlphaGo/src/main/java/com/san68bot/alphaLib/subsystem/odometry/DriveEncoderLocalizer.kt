package com.san68bot.alphaLib.subsystem.odometry

import com.acmerobotics.roadrunner.drive.MecanumDrive
import com.acmerobotics.roadrunner.geometry.Pose2d
import com.san68bot.alphaLib.control.motion.drive.Speedometer
import com.san68bot.alphaLib.geometry.Pose
import com.san68bot.alphaLib.control.motion.localizer.method.DriveEncoderMath
import com.san68bot.alphaLib.geometry.Point
import com.san68bot.alphaLib.subsystem.Localizer
import com.san68bot.alphaLib.wrappers.hardware.AGMotor
import com.san68bot.alphaLib.wrappers.imu.IMU
import kotlin.math.PI

class DriveEncoderLocalizer(
    trackWidth: Double,
    wheelBase: Double,
    wheelDiaIN: Double,
    gearRatio: Double,
    motorTicks: Double,
    motors: ArrayList<AGMotor>,
    imu: IMU
): Localizer {
    private val driveEncoders = DriveEncoders(trackWidth, wheelBase, wheelDiaIN, gearRatio, motorTicks, motors, imu)

    override fun update() {
        driveEncoders.updatePoseEstimate()

        DriveEncoderMath.update(
            driveEncoders.poseEstimate
        )

        Speedometer.update(
            DriveEncoderMath.xDelta(),
            DriveEncoderMath.yDelta()
        )
    }

    override fun reset(pose: Pose) {
        driveEncoders.poseEstimate = Pose2d(pose.y, -pose.x, (pose.rad - PI/2.0))
        DriveEncoderMath.reset(
            driveEncoders.poseEstimate
        )
    }

    private class DriveEncoders(
        trackWidth: Double,
        wheelBase: Double,
        private val wheelDiaIN: Double,
        private val gearRatio: Double,
        private val motorTicks: Double,
        private val motors: ArrayList<AGMotor>,
        private val imu: IMU
    ): MecanumDrive(0.0, 0.0, 0.0, trackWidth, wheelBase, 1.0) {
        private fun encoderTicksToInches(ticks: Double) = wheelDiaIN * 2.0 * Math.PI * gearRatio * ticks / motorTicks
        override fun getWheelPositions(): List<Double> = motors.map { encoderTicksToInches(it.currentPosition()) }
        override fun getWheelVelocities(): List<Double> = motors.map { encoderTicksToInches(it.velocity) }
        override fun setMotorPowers(frontLeft: Double, rearLeft: Double, rearRight: Double, frontRight: Double) {}
        override val rawExternalHeading: Double get() = imu.yaw()
        override fun getExternalHeadingVelocity(): Double = imu.zRotationRate()
    }
}