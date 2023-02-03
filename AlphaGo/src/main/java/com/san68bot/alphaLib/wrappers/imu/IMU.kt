package com.san68bot.alphaLib.wrappers.imu

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.hardware.IMU
import com.san68bot.alphaLib.utils.field.Globals
import com.san68bot.alphaLib.wrappers.util.ActionTimer
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles

class IMU(private val frequencyMS: Double = 0.0, orientation: RevHubOrientationOnRobot, hmap: HardwareMap = Globals.hmap) {
    val imu: IMU = hmap.get(IMU::class.java, "imu")
    private val frequencyTimer = ActionTimer()

    lateinit var robotYawPitchRollAngles: YawPitchRollAngles
    lateinit var angularVelocity: AngularVelocity

    init {
        imu.initialize(IMU.Parameters(orientation))
        frequencyTimer.reset()
    }

    fun update() {
        if (!frequencyMS.isNaN() && frequencyTimer.milliseconds <= frequencyMS) return
        robotYawPitchRollAngles = imu.robotYawPitchRollAngles
        angularVelocity = imu.getRobotAngularVelocity(AngleUnit.RADIANS)
        frequencyTimer.reset()
    }

    fun yaw() = robotYawPitchRollAngles.getYaw(AngleUnit.RADIANS)
    fun pitch() = robotYawPitchRollAngles.getPitch(AngleUnit.RADIANS)
    fun roll() = robotYawPitchRollAngles.getRoll(AngleUnit.RADIANS)

    fun xRotationRate() = angularVelocity.xRotationRate
    fun yRotationRate() = angularVelocity.yRotationRate
    fun zRotationRate() = angularVelocity.zRotationRate
}