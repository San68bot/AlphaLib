package com.san68bot.alphaLib.wrappers.hardware

import com.qualcomm.robotcore.hardware.*
import com.san68bot.alphaLib.utils.field.Globals

class AGServo(
    config: String,
    hmap: HardwareMap = Globals.hmap,
    maxAngle: Double = 270.0,
    gearRatio: Double = 1.0
) {
    private val servo = hmap.get(ServoImplEx::class.java, config)
    private val newMaxAngle = maxAngle * gearRatio

    var position = Double.NaN
        set(value) {
            if (!value.isNaN() && value != field) {
                servo.position = value
                field = value
            }
        }

    infix fun position(value: Double) {
        position = value
    }

    var angle = Double.NaN
        set(value) {
            if (!value.isNaN() && value != field) {
                servo.position = value / newMaxAngle
                field = value
            }
        }
        get() {
            return servo.position * newMaxAngle
        }

    infix fun angle(value: Double) {
        angle = value
    }

    private var direction: Servo.Direction = Servo.Direction.FORWARD
    set(value) {
        if (value != field) {
            servo.direction = value
            field = value
        }
    }

    val reverse: AGServo
        get() {
            direction = Servo.Direction.REVERSE
            return this
        }

    val forward: AGServo
        get() {
            direction = Servo.Direction.FORWARD
            return this
        }

    val extend: AGServo
        get() {
            servo.pwmRange = PwmControl.PwmRange(500.0, 2500.0)
            return this
        }
}