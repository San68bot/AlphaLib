package com.san68bot.alphaLib.motion.drive

import com.san68bot.alphaLib.controllers.PIDConstants
import com.san68bot.alphaLib.motion.drive.DriveMotion.drive_xv
import com.san68bot.alphaLib.motion.drive.DriveMotion.drive_yv
import com.san68bot.alphaLib.motion.drive.DriveMotion.drive_omega
import kotlin.math.abs

class Mecanum(xPID: PIDConstants, yPID: PIDConstants, turnPID: PIDConstants) {
    var frontLeftPower = 0.0
    var backLeftPower = 0.0
    var frontRightPower = 0.0
    var backRightPower = 0.0

    init {
        Mecanum.xPID set xPID
        Mecanum.yPID set yPID
        Mecanum.turnPID set turnPID
    }

    fun update() {
        frontLeftPower = drive_yv + drive_omega + drive_xv
        backLeftPower = drive_yv + drive_omega - drive_xv
        frontRightPower = drive_yv - drive_omega - drive_xv
        backRightPower = drive_yv - drive_omega + drive_xv

        maxOf(
            abs(frontLeftPower),
            abs(backLeftPower),
            abs(frontRightPower),
            abs(backRightPower),
            1.0
        ).also {
            if (it > 1.0) {
                frontLeftPower /= it
                backLeftPower /= it
                frontRightPower /= it
                backRightPower /= it
            }
        }
    }

    companion object {
        var xPID = PIDConstants()
        var yPID = PIDConstants()
        var turnPID = PIDConstants()
    }
}