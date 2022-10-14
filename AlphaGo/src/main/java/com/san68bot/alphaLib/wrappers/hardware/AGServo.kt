package com.san68bot.alphaLib.wrappers.hardware

import com.qualcomm.robotcore.hardware.*
import com.san68bot.alphaLib.geometry.Angle
import com.san68bot.alphaLib.utils.field.Globals

fun AGServo(
    config: String,
    maxAngle: Double = 270.0,
    gearRatio: Double = 1.0,
    hmap: HardwareMap = Globals.hmap,
    block: AGServo.() -> Unit = {}
): AGServo = AGServo(config, maxAngle, gearRatio, hmap).apply(block)

class AGServo(
    config: String,
    maxAngle: Double = 270.0,
    gearRatio: Double = 1.0,
    hmap: HardwareMap = Globals.hmap
) {
    private val servo = hmap.get(ServoImplEx::class.java, config)
    private val maxAngleCorrected = maxAngle * gearRatio

    val extend: AGServo
        get() {
            servo.pwmRange = PwmControl.PwmRange(500.0, 2500.0)
            return this
        }

    infix fun position(value: Double) {
        position = value
    }

    var position = Double.NaN
        set(value) {
            if (!value.isNaN() && value != field) {
                servo.position = value
                field = value
            }
        }

    infix fun angle(angle: Angle) {
        this.angle = angle.deg
    }

    var angle = Double.NaN
        set(value) {
            if (!value.isNaN() && value != field) {
                servo.position = value / maxAngleCorrected
                field = value
            }
        }
        get() {
            return servo.position * maxAngleCorrected
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

    private var direction: Servo.Direction = Servo.Direction.FORWARD
        set(value) {
            if (value != field) {
                servo.direction = value
                field = value
            }
        }
}