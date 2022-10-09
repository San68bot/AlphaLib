package com.san68bot.alphaLib.control.controllers

class PIDConstants(var kP: Double = 0.0, var kI: Double = 0.0, var kD: Double = 0.0) {
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