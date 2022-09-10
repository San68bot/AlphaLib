package com.san68bot.alphaLib.wrappers

import com.qualcomm.robotcore.hardware.*
import com.san68bot.alphaLib.field.geometry.Point

class AGps4(val gamepad: Gamepad) {
    init {
        PS4Master.add(this)
    }

    val rightStick = ps4Joystick ({ pad -> pad.right_stick_x.toDouble() }, { pad -> pad.right_stick_y.toDouble() })
    val leftStick = ps4Joystick ({ pad -> pad.left_stick_x.toDouble() }, { pad -> pad.left_stick_y.toDouble() })

    val rightStickAngle = Point(rightStick.x, -rightStick.y).angleToUnitCircle(Point.ORIGIN)
    val leftStickAngle = Point(leftStick.x, -leftStick.y).angleToUnitCircle(Point.ORIGIN)

    val leftTriggerButton = ps4Button { pad -> pad.left_trigger > 0.5 }
    val rightTriggerButton = ps4Button { pad -> pad.right_trigger > 0.5 }

    val leftTrigger: Double
        get() = gamepad.left_trigger.toDouble()
    val rightTrigger: Double
        get() = gamepad.right_trigger.toDouble()

    fun setRumblePattern(rumblePattern: Gamepad.RumbleEffect) {
        gamepad.runRumbleEffect(rumblePattern)
    }

    fun setRumblePattern(rumblePattern: () -> Gamepad.RumbleEffect) {
        gamepad.runRumbleEffect(rumblePattern())
    }

    fun rumbleBlips(count: Int) {
        if (!gamepad.isRumbling) gamepad.rumbleBlips(count)
    }

    fun rumble(left: Double, right: Double, delay: Int){
        gamepad.rumble(left, right, delay)
    }

    val isRumbling: Boolean
        get() = gamepad.isRumbling

    fun stopRumble() { gamepad.stopRumble() }

    val touchpad = ps4Button { pad -> pad.touchpad }

    val rightBumper = ps4Button { pad -> pad.right_bumper }
    val leftBumper = ps4Button { pad -> pad.left_bumper }

    val rightStickButton = ps4Button { pad -> pad.right_stick_button }
    val leftStickButton = ps4Button { pad -> pad.left_stick_button }

    val cross = ps4Button { pad -> pad.cross } //a
    val circle = ps4Button { pad -> pad.circle } //b
    val square = ps4Button { pad -> pad.square } //x
    val triangle = ps4Button { pad -> pad.triangle } //y

    val dLeft = ps4Button { pad -> pad.dpad_left }
    val dRight = ps4Button { pad -> pad.dpad_right }
    val dUp = ps4Button { pad -> pad.dpad_up }
    val dDown = ps4Button { pad -> pad.dpad_down }

    private val allParts = arrayOf(
        leftBumper, rightBumper,
        cross, circle, square, triangle,
        dLeft, dRight, dUp, dDown,
        leftStick, rightStick,
        leftTriggerButton, rightTriggerButton,
        leftStickButton, rightStickButton,
        touchpad
    )

    fun update() {
        allParts.forEach { part ->
            part.update(gamepad)
        }
    }
}

class ps4Button(private val getCurrentState: (gamePad: Gamepad) -> Boolean) : PS4part {
    var currentState = false
        private set
    var justPressed = false
        private set
    var justReleased = false
        private set

    override fun update(gamePad: Gamepad) {
        val lastState = currentState
        currentState = getCurrentState(gamePad)
        justPressed = currentState && !lastState
        justReleased = !currentState && lastState
    }
}

class ps4Joystick(private val xInput: (Gamepad) -> Double, private val yInput: (Gamepad) -> Double) : PS4part {
    var x: Double = 0.0
        private set
    var y: Double = 0.0
        private set

    override fun update(gamePad: Gamepad) {
        x = xInput(gamePad)
        y = -yInput(gamePad)
    }
}

object PS4Master {
    val list = ArrayList<AGps4>()

    fun add(controller: AGps4) {
        list.add(controller)
    }

    fun update() {
        for (controller in list)
            controller.update()
    }

    fun reset() {
        list.clear()
    }
}

private interface PS4part {
    fun update(gamePad: Gamepad)
}