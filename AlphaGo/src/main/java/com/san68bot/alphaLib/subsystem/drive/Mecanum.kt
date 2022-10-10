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
    turnPID: PIDConstants
): Subsystem {
    private var frontLeftPower = 0.0
    private var backLeftPower = 0.0
    private var frontRightPower = 0.0
    private var backRightPower = 0.0

    init {
        Companion.xPID set xPID
        Companion.yPID set yPID
        Companion.turnPID set turnPID
    }

    override fun update() {
        frontLeftPower = drive_yv + drive_omega + drive_xv
        backLeftPower = drive_yv + drive_omega - drive_xv
        frontRightPower = drive_yv - drive_omega - drive_xv
        backRightPower = drive_yv - drive_omega + drive_xv
        val powers = arrayOf(frontLeftPower, backLeftPower, frontRightPower, backRightPower, 1.0)

        val max = powers.maxOf { abs(it) }

        if (max > 1.0) {
            frontLeftPower /= max
            backLeftPower /= max
            frontRightPower /= max
            backRightPower /= max
        }
    }

    override fun log(): String {
        return "FL: ${ frontLeftPower round 3 }, " +
               "BL: ${ backLeftPower round 3 }, " +
               "FR: ${ frontRightPower round 3 }, " +
               "BR: ${ backRightPower round 3 }"
    }

    fun frontLeftPower() = frontLeftPower
    fun backLeftPower() = backLeftPower
    fun frontRightPower() = frontRightPower
    fun backRightPower() = backRightPower

    companion object {
        var xPID = PIDConstants()
        var yPID = PIDConstants()
        var turnPID = PIDConstants()
    }
}