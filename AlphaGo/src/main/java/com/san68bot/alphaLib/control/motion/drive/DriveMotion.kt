package com.san68bot.alphaLib.control.motion.drive

import com.acmerobotics.roadrunner.profile.MotionProfile
import com.acmerobotics.roadrunner.profile.MotionProfileGenerator
import com.acmerobotics.roadrunner.profile.MotionState
import com.san68bot.alphaLib.control.motion.drive.Speedometer.degPerSec
import com.san68bot.alphaLib.control.motion.drive.Speedometer.speed
import com.san68bot.alphaLib.control.motion.localizer.GlobalPosition.global_angle
import com.san68bot.alphaLib.control.motion.localizer.GlobalPosition.global_angle_bisectedArc
import com.san68bot.alphaLib.control.motion.localizer.GlobalPosition.global_point
import com.san68bot.alphaLib.control.motion.localizer.GlobalPosition.global_rad
import com.san68bot.alphaLib.control.motion.localizer.GlobalPosition.global_x
import com.san68bot.alphaLib.control.motion.localizer.GlobalPosition.global_y
import com.san68bot.alphaLib.geometry.*
import com.san68bot.alphaLib.geometry.Angle.Companion.degrees
import com.san68bot.alphaLib.geometry.Angle.Companion.radians
import com.san68bot.alphaLib.subsystem.drive.Mecanum
import com.san68bot.alphaLib.subsystem.drive.Mecanum.Companion.turnPID
import com.san68bot.alphaLib.subsystem.drive.Mecanum.Companion.xPID
import com.san68bot.alphaLib.subsystem.drive.Mecanum.Companion.yPID
import com.san68bot.alphaLib.utils.OneTime
import com.san68bot.alphaLib.utils.field.Globals.telemetryBuilder
import com.san68bot.alphaLib.utils.field.RunData.ALLIANCE
import com.san68bot.alphaLib.utils.math.*
import com.san68bot.alphaLib.wrappers.util.*
import kotlin.math.abs
import kotlin.math.atan2

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
     * X motion profile
     */
    private var x_mp: MotionProfile? = null

    /**
     * Y motion profile
     */
    private var y_mp: MotionProfile? = null

    /**
     * Theta motion profile
     */
    private var theta_mp: MotionProfile? = null

    /**
     * Initial theta for theta mp
     */
    private var theta_initial = 0.0

    /**
     * Motion profile one time to set the motion profile
     */
    private val mpOneTime = OneTime()

    /**
     * Motion profile timer
     */
    private val mpTimer = ActionTimer()

    /**
     * Stops movement in all axis
     */
    fun stop() {
        drive_xv = 0.0
        drive_yv = 0.0
        drive_omega = 0.0
    }

    fun objectReset() {
        stop()
        x_mp = null
        y_mp = null
        theta_mp = null
        theta_initial = 0.0
        mpOneTime.reset()
        mpTimer.reset()
    }

    /**
     * Resets the motion profile
     */
    fun resetMotionProfile() {
        objectReset()
    }

    /**
     * GoToPose implementation that uses PID to move to a specific point
     * Must be a unit cirlce angle
     */
    fun goToPose(x: Double, y: Double, theta: Angle): MovementResults {
        /**
         * Pose errors
         */
        val xError = (x - global_x)
        val yError = (y - global_y)
        val angleError = fullCircleToBisectedArc(theta - global_angle)

        /**
         * Pose speeds using PD controller calculations and robot speed
         */
        val xSpeed = xError * xPID.kP - speed.x * xPID.kD
        val ySpeed = yError * yPID.kP - speed.y * yPID.kD
        val turnSpeed = angleError.deg * turnPID.kP - degPerSec * turnPID.kD

        /**
         * Set movement
         */
        fieldCentric(xSpeed, ySpeed, turnSpeed)
        maximizeMovement()

        /**
         * Log results
         */
        logData(Pose(x, y, theta), Pose(xError, yError, angleError))
        return MovementResults(Pose(xError, yError, angleError))
    }

    /**
     * Pose wrapper implementation of goToPose
     * Must be a unit cirlce angle
     */
    fun Pose.goToPose(): MovementResults {
        return goToPose(this.x, this.y, this.angle)
    }

    /**
     * Motion profile implementation of goToPose
     */
    fun goToPose_mp(x: Double, y: Double, theta: Angle): MovementResults {
        mpOneTime.once {
            x_mp = MotionProfileGenerator.generateSimpleMotionProfile(
                MotionState(global_x, 0.0, 0.0),
                MotionState(x, 0.0, 0.0), Mecanum.max_velo, Mecanum.max_accel
            )
            y_mp = MotionProfileGenerator.generateSimpleMotionProfile(
                MotionState(global_y, 0.0, 0.0),
                MotionState(y, 0.0, 0.0), Mecanum.max_velo, Mecanum.max_accel
            )
            mpTimer.reset()
        }

        /**
         * Motion Profile target positions
         */
        val mpXTarget = x_mp!![mpTimer.seconds].x
        val mpYTarget = y_mp!![mpTimer.seconds].x

        /**
         * Pose errors
         */
        val xError = (mpXTarget - global_x)
        val yError = (mpYTarget - global_y)
        val angleError = fullCircleToBisectedArc(theta - global_angle)

        /**
         * Pose speeds using PD controller calculations and robot speed
         */
        val xSpeed = xError * xPID.kP - speed.x * xPID.kD
        val ySpeed = yError * yPID.kP - speed.y * yPID.kD
        val turnSpeed = angleError.deg * turnPID.kP - degPerSec * turnPID.kD

        /**
         * Set movement
         */
        fieldCentric(xSpeed, ySpeed, turnSpeed)

        /**
         * Log results
         */
        logData(Pose(mpXTarget, mpYTarget, theta), Pose(xError, yError, angleError))
        return MovementResults(Pose(x - global_x, y - global_y, angleError))
    }

    /**
     * Pose wrapper implementation of goToPose_mp
     * Must be a unit cirlce angle
     */
    fun Pose.goToPose_mp(): MovementResults {
        return goToPose_mp(this.x, this.y, this.angle)
    }

    /**
     * PointAngle implementation that uses PID to turn to a specific angle
     * Must be a unit cirlce angle
     */
    fun pointAngle(theta: Angle): Angle {
        /**
         * Angle errors
         */
        val angleError = fullCircleToBisectedArc(theta - global_angle)

        /**
         * Angle speed using PID calculations and robot speed
         */
        drive_omega = angleError.deg * turnPID.kP - degPerSec * turnPID.kD

        /**
         * Log results
         */
        logData(Pose(Double.NaN, Double.NaN, theta), Pose(global_point, angleError))
        return angleError
    }

    fun pointAngle_mp(theta: Angle): Angle {
        /**
         * Angle errors
         */
        val angleError = fullCircleToBisectedArc(theta - global_angle)

        /**
         * Generating the motion profile
         */
        mpOneTime.once {
            theta_initial = global_rad
            theta_mp = MotionProfileGenerator.generateSimpleMotionProfile(
                MotionState(0.0, 0.0, 0.0),
                MotionState(angleError.rad, 0.0, 0.0), Mecanum.max_omega, Mecanum.max_alpha
            )
            mpTimer.reset()
        }

        /**
         * Offseting the mp value
         */
        val mp_theta_target = (theta_initial - (theta_mp!![mpTimer.seconds].x * angleError.sign())).radians

        /**
         * Angle speed using PID calculations and robot speed
         */
        drive_omega = mp_theta_target.deg * turnPID.kP - degPerSec * turnPID.kD

        /**
         * Log results
         */
        logData(Pose(Double.NaN, Double.NaN, theta), Pose(global_point, mp_theta_target))
        return mp_theta_target
    }

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
            atan2(pointMove.x, pointMove.y).radians - global_angle_bisectedArc,
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
                scaleMovement(1.0/total)
            }
        }
    }

    /**
     * Scales the robot's x and y movement
     */
    private fun scaleMovement(scaler: Double) {
        drive_xv *= scaler
        drive_yv *= scaler
    }
}