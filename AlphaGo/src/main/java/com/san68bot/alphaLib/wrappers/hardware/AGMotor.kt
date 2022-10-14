package com.san68bot.alphaLib.wrappers.hardware

import com.qualcomm.robotcore.hardware.*
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType
import com.qualcomm.robotcore.util.Range
import com.san68bot.alphaLib.geometry.Angle
import com.san68bot.alphaLib.geometry.TAU
import com.san68bot.alphaLib.geometry.toRadians
import com.san68bot.alphaLib.utils.field.Globals
import com.san68bot.alphaLib.utils.math.difference
import kotlin.math.absoluteValue
import kotlin.reflect.KClass

fun AGMotor(
    config: String,
    motorType: KClass<*>,
    gearRatio: Double = 1.0,
    hmap: HardwareMap = Globals.hmap,
    block: AGMotor.() -> Unit = {}
): AGMotor = AGMotor(config, motorType, gearRatio, hmap).apply(block)

class AGMotor(
    config: String,
    motorType: KClass<*>,
    gearRatio: Double = 1.0,
    hmap: HardwareMap = Globals.hmap
) {
    val motor by lazy { hmap.get(DcMotorEx::class.java, config) }
    private val type = MotorConfigurationType.getMotorType(motorType.java)
    val encoder by lazy { AGEncoder(motor, type.ticksPerRev, gearRatio) }

    fun setMaxAchievableFraction(): AGMotor {
        val motorConfigurationType = motor.motorType.clone()
        motorConfigurationType.achieveableMaxRPMFraction = 1.0
        motor.motorType = motorConfigurationType
        return this
    }

    fun currentPosition(): Double {
        return encoder.currentPos
    }

    fun currentAngle(): Angle {
        return encoder.angle
    }

    infix fun resetEncoder(newPosition: Double) {
        encoder reset newPosition
    }

    infix fun resetEncoder(newAngle: Angle) {
        encoder reset (newAngle.rad / TAU * type.ticksPerRev)
    }

    val velocity get() = motor.velocity

    infix fun power(value: Double) {
        power = value
    }

    var power: Double = 0.0
        set(value) {
            val clippedValue = Range.clip(value, -1.0, 1.0)
            if (clippedValue != field && (clippedValue == 0.0 || clippedValue.absoluteValue == 1.0 || clippedValue difference field > 0.005)) {
                field = value
                motor.power = value
            }
        }

    val reverse: AGMotor
        get() {
            direction = DcMotorSimple.Direction.REVERSE
            encoder.reverse()
            return this
        }

    private var direction: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD
        set(value) {
            if (value != field) {
                motor.direction = value
                field = value
            }
        }

    val float: AGMotor
        get() {
            zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
            return this
        }

    val brake: AGMotor
        get() {
            zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            return this
        }

    private var zeroPowerBehavior = DcMotor.ZeroPowerBehavior.UNKNOWN
        set(value) {
            if (value != field) {
                if (value != DcMotor.ZeroPowerBehavior.UNKNOWN)
                    motor.zeroPowerBehavior = value
                field = value
            }
        }

    val RUN_WITHOUT_ENCODER: AGMotor
        get() {
            mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            return this
        }

    val RUN_USING_ENCODER: AGMotor
        get() {
            mode = DcMotor.RunMode.RUN_USING_ENCODER
            return this
        }

    private var mode: DcMotor.RunMode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        set(value) {
            if (field != value) {
                motor.mode = value
                field = value
            }
        }
}