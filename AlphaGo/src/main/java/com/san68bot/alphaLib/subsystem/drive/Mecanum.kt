package com.san68bot.alphaLib.subsystem.drive

import com.san68bot.alphaLib.control.controllers.PIDConstants
import com.san68bot.alphaLib.control.motion.drive.DriveMotion.drive_xv
import com.san68bot.alphaLib.control.motion.drive.DriveMotion.drive_yv
import com.san68bot.alphaLib.control.motion.drive.DriveMotion.drive_omega
import com.san68bot.alphaLib.subsystem.Subsystem
import com.san68bot.alphaLib.utils.math.round
import kotlin.math.abs

open class Mecanum(
    xPID: PIDConstants,
    yPID: PIDConstants,
    turnPID: PIDConstants,
    max_velo: Double,
    max_accel: Double
): Subsystem {
    /**
     * Powers for each wheel
     */
    private var frontLeftPower = 0.0
    private var backLeftPower = 0.0
    private var frontRightPower = 0.0
    private var backRightPower = 0.0

    /**
     * Set PID controllers for each axis
     */
    init {
        Companion.xPID set xPID
        Companion.yPID set yPID
        Companion.turnPID set turnPID
        Companion.max_velo = max_velo
        Companion.max_accel = max_accel
    }

    /**
     * For live updating PID constants
     */
    fun updatePID(xPID: PIDConstants, yPID: PIDConstants, turnPID: PIDConstants) {
        Companion.xPID set xPID
        Companion.yPID set yPID
        Companion.turnPID set turnPID
    }

    /**
     * Calculate powers for each wheel
     * Settings powers should be done in a child class
     */
    override fun update() {
        val x = drive_xv
        val y = drive_yv
        val omega = drive_omega

        frontLeftPower = y + omega + x
        backLeftPower = y + omega - x
        frontRightPower = y - omega - x
        backRightPower = y - omega + x

        val powers = arrayOf(
            abs(frontLeftPower),
            abs(backLeftPower),
            abs(frontRightPower),
            abs(backRightPower),
            1.0
        )

        val max = powers.max()
        if (max > 1.0) {
            frontLeftPower /= max
            backLeftPower /= max
            frontRightPower /= max
            backRightPower /= max
        }
    }

    /**
     * Logs each wheel power
     */
    override fun log(): String {
        return "DriveTrain Data:\n" +
               "FL: ${ frontLeftPower round 3 }, \n" +
               "BL: ${ backLeftPower round 3 }, \n" +
               "FR: ${ frontRightPower round 3 }, \n" +
               "BR: ${ backRightPower round 3 }\n"
    }

    /**
     * Getters for each wheel power
     */
    fun frontLeftPower() = frontLeftPower
    fun backLeftPower() = backLeftPower
    fun frontRightPower() = frontRightPower
    fun backRightPower() = backRightPower

    /**
     * Access to PID controllers for Drive controllers
     */
    companion object {
        var xPID = PIDConstants()
        var yPID = PIDConstants()
        var turnPID = PIDConstants()
        var max_velo = 0.0
        var max_accel = 0.0
    }
}