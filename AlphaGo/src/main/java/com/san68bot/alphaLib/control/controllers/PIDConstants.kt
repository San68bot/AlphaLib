package com.san68bot.alphaLib.control.controllers

data class PIDConstants(
    @JvmField var kP: Double = 0.0,
    @JvmField var kI: Double = 0.0,
    @JvmField var kD: Double = 0.0
) {
    infix fun set(pid: PIDConstants) {
        kP = pid.kP
        kI = pid.kI
        kD = pid.kD
    }

    fun set(kP: Double = this.kP, kI: Double = this.kI, kD: Double = this.kD) {
        this.kP = kP
        this.kI = kI
        this.kD = kD
    }
}