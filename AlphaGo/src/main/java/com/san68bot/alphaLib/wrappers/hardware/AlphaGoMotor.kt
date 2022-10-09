package com.san68bot.alphaLib.wrappers.hardware

import com.qualcomm.robotcore.hardware.*
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType
import com.qualcomm.robotcore.util.Range
import com.san68bot.alphaLib.geometry.TAU
import com.san68bot.alphaLib.geometry.toRadians
import com.san68bot.alphaLib.utils.math.difference
import kotlin.math.absoluteValue
import kotlin.reflect.KClass

fun AlphaGoMotor(
    config: String,
    motorType: KClass<*>,
    gearRatio: Double = 1.0,
    hmap: HardwareMap,
    block: AlphaGoMotor.() -> Unit = {}
): AlphaGoMotor = AlphaGoMotor(config, motorType, gearRatio, hmap).apply(block)

class AlphaGoMotor(
    config: String,
    motorType: KClass<*>,
    gearRatio: Double = 1.0,
    hmap: HardwareMap
) {
    val motor by lazy { hmap.get(DcMotorEx::class.java, config) }
    private val type = MotorConfigurationType.getMotorType(motorType.java)
    val encoder by lazy { AGEncoder(motor, type.ticksPerRev, gearRatio) }

    fun setMaxAchievableFraction(): AlphaGoMotor {
        val motorConfigurationType = motor.motorType.clone()
        motorConfigurationType.achieveableMaxRPMFraction = 1.0
        motor.motorType = motorConfigurationType
        return this
    }

    private var direction: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD
        set(value) {
            if (value != field) {
                motor.direction = value
                field = value
            }
        }

    fun currentPosition(): Double {
        return encoder.currentPos
    }

    fun currentRadians(): Double {
        return encoder.radians
    }

    infix fun resetEncoder(newPosition: Double) {
        encoder reset newPosition
    }

    infix fun resetDegrees(degrees: Double) {
        resetRadians(degrees.toRadians)
    }

    infix fun resetRadians(radians: Double) {
        encoder reset (radians / TAU * type.ticksPerRev)
    }

    var veloPIDF: PIDFCoefficients = type.hubVelocityParams.pidfCoefficients
        set(value) {
            motor.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, value)
            field = value
        }

    val velocity get() = motor.velocity

    var power: Double = 0.0
        set(value) {
            val clippedValue = Range.clip(value, -1.0, 1.0)
            if (clippedValue != field && (clippedValue == 0.0 || clippedValue.absoluteValue == 1.0 || clippedValue difference field > 0.005)) {
                field = value
                motor.power = value
            }
        }

    infix fun power(value: Double) {
        power = value
    }

    var targetPosition: Int = 0
        set(value) {
            if (field != value) {
                motor.targetPosition = value
                field = value
            }
        }

    var targetPositionTolerance: Int = 0
        set(value) {
            if (field != value) {
                motor.targetPositionTolerance = value
                field = value
            }
        }

    val reverse: AlphaGoMotor
        get() {
            direction = DcMotorSimple.Direction.REVERSE
            encoder.reverse()
            return this
        }

    val float: AlphaGoMotor
        get() {
            zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
            return this
        }

    val brake: AlphaGoMotor
        get() {
            zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            return this
        }

    val RUN_WITHOUT_ENCODER: AlphaGoMotor
        get() {
            mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            return this
        }

    val RUN_USING_ENCODER: AlphaGoMotor
        get() {
            mode = DcMotor.RunMode.RUN_USING_ENCODER
            return this
        }

    val RUN_TO_POSITION: AlphaGoMotor
        get() {
            mode = DcMotor.RunMode.RUN_TO_POSITION
            return this
        }

    var mode: DcMotor.RunMode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        set(value) {
            if (field != value) {
                motor.mode = value
                field = value
            }
        }

    private var zeroPowerBehavior = DcMotor.ZeroPowerBehavior.UNKNOWN
        set(value) {
            if (value != field) {
                if (value != DcMotor.ZeroPowerBehavior.UNKNOWN)
                    motor.zeroPowerBehavior = value
                field = value
            }
        }
}