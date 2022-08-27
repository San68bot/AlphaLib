package com.san68bot.alphaLib.controllers

import com.qualcomm.robotcore.util.ElapsedTime
import kotlin.math.abs

class PID(
    private var kP: Double, private var kI: Double, private var kD: Double, private var kStatic: Double,
    private var leeway: Double = 0.0, private var integralThreshold: Double, private val time: ElapsedTime
) {
    private var targetTheta = 0.0

    private var error = 0.0
    private var prev_error = 0.0

    private var prev_time = time.seconds()

    private var integral = 0.0
    private var derivative = 0.0

    fun target(targetTheta: Double): PID { this.targetTheta = targetTheta; return this }

    fun error() = error

    fun setConstants(
        kP: Double, kI: Double, kD: Double, kStatic: Double,
        leeway: Double = this.leeway, integralThreshold: Double = this.integralThreshold
    ): PID {
        this.kP = kP
        this.kI = kI
        this.kD = kD
        this.kStatic = kStatic
        this.leeway = leeway
        this.integralThreshold = integralThreshold
        return this
    }

    fun update(currentTheta: Double): Double {
        val current_time = time.seconds()
        val delta_time = (current_time - prev_time)
        prev_time = current_time

        error = targetTheta - currentTheta
        val delta_error = error - prev_error
        prev_error = error

        integral += error * delta_time
        if (abs(error) > integralThreshold) integral = 0.0

        derivative = if (delta_time != 0.0) delta_error / delta_time else 0.0

        return when {
            error() > leeway -> (kP * error()) + (kI * integral) + (kD * derivative) + kStatic
            error() < -leeway -> (kP * error()) + (kI * integral) + (kD * derivative) - kStatic
            else -> 0.0
        }
    }
}