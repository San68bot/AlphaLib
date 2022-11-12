package com.san68bot.alphaLib.wrappers.imu

import com.qualcomm.hardware.bosch.BNO055IMU
import com.qualcomm.robotcore.hardware.HardwareMap
import com.san68bot.alphaLib.utils.field.Globals
import com.san68bot.alphaLib.wrappers.util.ActionTimer
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder
import org.firstinspires.ftc.robotcore.external.navigation.Orientation

class IMU(private val frequencyMS: Double = 0.0, hmap: HardwareMap = Globals.hmap) {
    private val imu: BNO055IMU = hmap.get(BNO055IMU::class.java, "imu")
    private val frequencyTimer = ActionTimer()

    init {
        val params = BNO055IMU.Parameters()
        params.angleUnit = BNO055IMU.AngleUnit.RADIANS
        imu.initialize(params)
        BNO055IMUUtil.remapAxes(imu, AxesOrder.XYZ, AxesSigns.NPN)//first = z, second = x, third = y
        frequencyTimer.reset()
    }

    private var angularOrientation: Orientation = Orientation()
    private var angularVelocity: AngularVelocity = AngularVelocity()

    fun update() {
        if(frequencyTimer.milliseconds >= frequencyMS) {
            angularOrientation = imu.angularOrientation
            angularVelocity = imu.angularVelocity
            frequencyTimer.reset()
        }
    }

    val firstAngle get() = angularOrientation.firstAngle.toDouble()
    val secondAngle get() = angularOrientation.secondAngle.toDouble()
    val thirdAngle get() = angularOrientation.thirdAngle.toDouble()

    val xRotationRate get() = angularVelocity.xRotationRate.toDouble()
    val yRotationRate get() = angularVelocity.yRotationRate.toDouble()
    val zRotationRate get() = angularVelocity.zRotationRate.toDouble()

    fun disable() = imu.close()
}