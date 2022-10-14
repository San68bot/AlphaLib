package com.san68bot.alphaLib.control.motion.drive

import com.san68bot.alphaLib.control.motion.drive.Speedometer.degPerSec
import com.san68bot.alphaLib.control.motion.drive.Speedometer.speed
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_deg
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_point
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_rad
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_x
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_y
import com.san68bot.alphaLib.geometry.*
import com.san68bot.alphaLib.geometry.Angle.Companion.degrees
import com.san68bot.alphaLib.subsystem.drive.Mecanum.Companion.turnPID
import com.san68bot.alphaLib.subsystem.drive.Mecanum.Companion.xPID
import com.san68bot.alphaLib.subsystem.drive.Mecanum.Companion.yPID
import com.san68bot.alphaLib.utils.field.Globals.telemetryBuilder
import com.san68bot.alphaLib.utils.field.RunData.ALLIANCE
import com.san68bot.alphaLib.utils.math.*
import com.san68bot.alphaLib.wrappers.util.*
import kotlin.math.abs

object DriveMotion {
    /**
     * X velocity
     */
    var drive_xv = 0.0

    /**
     * Y velocity
     */
    var drive_yv = 0.0

    /**
     * Angular velocity
     */
    var drive_omega = 0.0

    /**
     * Stops movement in all axis
     */
    fun stop() {
        drive_xv = 0.0
        drive_yv = 0.0
        drive_omega = 0.0
    }

    /**
     * GoToPose implementation that uses PID to move to a specific point
     */
    fun goToPose(x: Double, y: Double, theta: Double, external: Boolean = true): MovementResults {
        /**
         * Pose errors
         */
        val xLeft = (x - world_x)
        val yLeft = (y - world_y)
        val turnLeft = angleToEuler(if (external)
            (unitCircleToHalfCircle(theta).deg - world_deg).degrees
        else
            (theta - world_deg).degrees
        )

        /**
         * Pose speeds using PID calculations and robot speed
         */
        val xSpeed = xLeft * xPID.kP - speed.x * xPID.kD
        val ySpeed = yLeft * yPID.kP - speed.y * yPID.kD
        val turnSpeed = turnLeft.deg * turnPID.kP - degPerSec * turnPID.kD

        /**
         * Set movement
         */
        fieldCentric(xSpeed, ySpeed, turnSpeed)
        maximizeMovement()

        /**
         * Log results
         */
        logData(Pose(xLeft, yLeft, turnLeft.deg), Pose(x, y, theta))
        return MovementResults(Pose(xLeft, yLeft, turnLeft.deg))
    }

    /**
     * Pose wrapper implementation of goToPose
     */
    fun Pose.goToPose(): MovementResults {
        return goToPose(this.x, this.y, this.rad)
    }

    /**
     * PointAngle implementation that uses PID to turn to a specific angle
     */
    fun pointAngle(theta: Double): Angle {
        /**
         * Angle errors
         */
        val turnLeft = angleToEuler((unitCircleToHalfCircle(theta).deg - world_deg).degrees)

        /**
         * Angle speed using PID calculations and robot speed
         */
        drive_omega = turnLeft.deg * turnPID.kP - degPerSec * turnPID.kD

        /**
         * Log results
         */
        logData(Pose(world_point, turnLeft), Pose(Double.NaN, Double.NaN, theta))
        return turnLeft
    }

    /**
     * Double implementation of pointAngle
     */
    fun Double.turnToTheta(): Angle = pointAngle(this)

    /**
     * Angle implementation of pointAngle
     */
    fun Angle.turnToTheta(): Angle = pointAngle(this.rad)

    /**
     * Log data to telemetry
     */
    private fun logData(target: Pose, error: Pose) {
        if (!error.x.isNaN() && !error.y.isNaN())
            telemetryBuilder.add("Target Point", target.point)

        telemetryBuilder.add("Theta Target(deg)", target.deg)

        if (!error.x.isNaN() && !error.y.isNaN())
            telemetryBuilder.add("Target Point Error", error.point)

        telemetryBuilder
            .add("Target Theta Error(deg)", error.deg)
            .drawDrivetrain(
                target.x, target.y, target.rad,
                if (ALLIANCE.isRed()) "red" else "cyan", "black"
            )
    }

    /**
     * Gamepad drive implementation for TeleOp
     */
    fun AGps4.gamepadDrive(robotCentric: Boolean, xclip: Double = 1.0, yclip: Double = 1.0, thetaClip: Double = 1.0) {
        val x = (this.leftStick.x * xclip) threshold 0.05
        val y = (this.leftStick.y * yclip) threshold 0.05
        val turn = (this.rightStick.x * thetaClip) threshold 0.05
        if (robotCentric) robotCentric(x, y, turn) else fieldCentric(x, y, turn)
    }

    /**
     * Robot centric drive
     */
    fun robotCentric(x: Double, y: Double, turn: Double) {
        drive_xv = x
        drive_yv = y
        drive_omega = turn
    }

    /**
     * Field centric drive
     */
    fun fieldCentric(x: Double, y: Double, turn: Double) {
        val pointMove = Point(x, y)
        movementVector(
            pointMove.hypot,
            pointMove.angle - unitCircleToHalfCircle(world_rad),
            turn
        )
    }

    /**
     * Sets movement vector
     */
    private fun movementVector(vel: Double, direction: Angle, turn: Double) {
        robotCentric(
            direction.sin * vel,
            direction.cos * vel,
            turn
        )
    }

    /**
     * Maximize movement
     */
    private fun maximizeMovement() {
        if (abs(drive_xv) + abs(drive_yv) > 1.0) {
            val total = abs(drive_xv) + abs(drive_yv)
            if (total != 0.0) {
                drive_xv *= (1.0/total)
                drive_yv *= (1.0/total)
            }
        }
    }
}