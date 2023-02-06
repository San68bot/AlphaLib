package com.san68bot.alphaLib.utils.field

import com.san68bot.alphaLib.control.logicControllers.AlphaGoInterface
import com.san68bot.alphaLib.control.motion.drive.DriveMotion
import com.san68bot.alphaLib.control.motion.drive.Speedometer
import com.san68bot.alphaLib.control.motion.localizer.GlobalPosition
import com.san68bot.alphaLib.control.motion.localizer.method.TwoWheelMath
import com.san68bot.alphaLib.geometry.Pose

object Globals {
    lateinit var agInterface: AlphaGoInterface

    val modeStatus get() = agInterface.modeStatus

    val hmap get() = agInterface.hardwareMap

    val telemetryBuilder get() = agInterface.telemetryBuilder

    fun resetObjects() {
        GlobalPosition.objectReset()
        DriveMotion.objectReset()
        TwoWheelMath.objectReset()
        Speedometer.objectReset()
    }

    var isAuto: Boolean = false
    var isTeleop: Boolean = false
}