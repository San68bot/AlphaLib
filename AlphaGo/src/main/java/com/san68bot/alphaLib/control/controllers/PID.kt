package com.san68bot.alphaLib.control.controllers

import com.san68bot.alphaLib.wrappers.util.ActionTimer
import kotlin.math.abs

class PID(
    private var kP: Double, private var kI: Double, private var kD: Double, private var kStatic: Double,
    private var leeway: Double = 0.0, private var integralThreshold: Double
) {
    constructor(pid: PIDConstants, kStatic: Double, leeway: Double = 0.0, integralThreshold: Double) : this(
        pid.kP, pid.kI, pid.kD, kStatic, leeway, integralThreshold
    )

    private var target = 0.0

    private var error = 0.0
    private var prev_error = 0.0
    private var delta_error = 0.0

    private val timer = ActionTimer()
    private var prev_time = 0.0

    private var integral = 0.0
    private var derivative = 0.0

    init {
        prev_time = timer.seconds
        integral = 0.0
        derivative = 0.0
    }

    fun setTarget(target: Double): PID { this.target = target; return this }

    fun target() = target

    fun error() = error
    fun delta_error() = delta_error

    fun setConstants(
        kP: Double, kI: Double, kD: Double, kStatic: Double,
        leeway: Double = this.leeway, integralThreshold: Double = this.integralThreshold
    ): PID {
        this.kP = kP; this.kI = kI; this.kD = kD
        this.kStatic = kStatic
        this.leeway = leeway
        this.integralThreshold = integralThreshold
        return this
    }

    fun setConstants(
        pid: PIDConstants, kStatic: Double,
        leeway: Double = this.leeway, integralThreshold: Double = this.integralThreshold
    ): PID {
        this.kP = pid.kP; this.kI = pid.kI; this.kD = pid.kD
        this.kStatic = kStatic
        this.leeway = leeway
        this.integralThreshold = integralThreshold
        return this
    }

    fun update(current: Double): Double {
        return updateUsingError(target - current)
    }

    fun updateUsingError(stateError: Double): Double {
        val current_time = timer.seconds
        val delta_time = (current_time - prev_time)
        prev_time = current_time

        error = stateError
        delta_error = error - prev_error
        prev_error = error

        integral += error * delta_time
        if (abs(error) > integralThreshold) integral = 0.0

        derivative = if (delta_time != 0.0) delta_error / delta_time else 0.0

        return when {
            error > leeway -> (kP * error) + (kI * integral) + (kD * derivative) + kStatic
            error < -leeway -> (kP * error) + (kI * integral) + (kD * derivative) - kStatic
            else -> 0.0
        }
    }
}