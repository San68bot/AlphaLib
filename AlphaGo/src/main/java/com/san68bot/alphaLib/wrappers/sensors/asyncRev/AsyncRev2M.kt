package com.san68bot.alphaLib.wrappers.sensors.asyncRev

import com.qualcomm.hardware.rev.Rev2mDistanceSensor
import com.qualcomm.robotcore.hardware.HardwareMap
import com.san68bot.alphaLib.utils.field.Globals
import com.san68bot.alphaLib.wrappers.sensors.asyncRev.AsyncRev2MSensor.AccuracyMode
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit

class AGRevDistanceSensor(
    config: String,
    accuracyMode: AccuracyMode = AccuracyMode.MODE_BALANCED,
    hmap: HardwareMap = Globals.hmap
) {
    private val sensor by lazy { 
        AsyncRev2MSensor(hmap.get(Rev2mDistanceSensor::class.java, config))
    }

    init {
        sensor.setSensorAccuracyMode(accuracyMode)
    }

    val inches get() =
        sensor.getDistance(DistanceUnit.INCH)

    infix fun interval(interval: Long): AGRevDistanceSensor {
        sensor.setMeasurementIntervalMs(interval)
        return this
    }
}