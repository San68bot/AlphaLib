package com.san68bot.alphaLib.control.motion.drive

import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_deg
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_rad
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_x
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_y
import com.san68bot.alphaLib.geometry.*
import com.san68bot.alphaLib.geometry.Angle.Companion.degrees
import com.san68bot.alphaLib.subsystem.drive.Mecanum
import com.san68bot.alphaLib.utils.field.Alliance
import com.san68bot.alphaLib.utils.field.Globals
import com.san68bot.alphaLib.utils.field.RunData
import com.san68bot.alphaLib.utils.math.angleToEuler
import com.san68bot.alphaLib.utils.math.halfCircleToUnitCircle
import com.san68bot.alphaLib.utils.math.threshold
import com.san68bot.alphaLib.utils.math.unitCircleToHalfCircle
import com.san68bot.alphaLib.wrappers.util.AGps4
import kotlin.math.abs

object DriveMotion {
    var drive_xv = 0.0
    var drive_yv = 0.0
    var drive_omega = 0.0

    fun stop() {
        drive_xv = 0.0
        drive_yv = 0.0
        drive_omega = 0.0
    }

    fun Pose.goTo(): MovementResults {
        return goToPoint(this.x, this.y, this.rad)
    }

    fun goToPose(pose: Pose): MovementResults {
        return goToPoint(pose.x, pose.y, pose.rad)
    }

    fun goToPoint(x: Double, y: Double, theta: Double, external: Boolean = true): MovementResults {
        val xLeft = (x - world_x)
        val yLeft = (y - world_y)
        val turnLeft = angleToEuler(if (external)
            (unitCircleToHalfCircle(theta).deg - world_deg).degrees
        else
            (theta - world_deg).degrees
        )

        val xPID = Mecanum.xPID
        val yPID = Mecanum.yPID
        val thetaPID = Mecanum.turnPID

        val speed = Speedometer.speed
        val xSpeed = xLeft * xPID.kP - speed.x * xPID.kD
        val ySpeed = yLeft * yPID.kP - speed.y * yPID.kD
        val turnSpeed = turnLeft.deg * thetaPID.kP - Speedometer.degPerSec * thetaPID.kD

        moveFieldCentric(xSpeed, ySpeed, turnSpeed)
        clipMovement()

        Globals.telemetryBuilder
            .add("xTarget", x)
            .add("yTarget", y)
            .add("thetaTarget DEG", if (external) theta.toDegrees else theta)
            .add("x-error", xLeft)
            .add("y-error", yLeft)
            .add("theta-error DEG", turnLeft)
            .drawDrivetrain(
                x, y,
                if (external) theta else halfCircleToUnitCircle(theta.toDegrees).deg,
                if (RunData.ALLIANCE == Alliance.RED) "red" else "cyan",
                "black"
            )
        return MovementResults(Pose(xLeft, yLeft, turnLeft.deg))
    }

    fun Double.turnToTheta(): Angle {
        return internalPointAngle(this)
    }

    fun Angle.turnToTheta(): Angle {
        return internalPointAngle(this.rad)
    }

    fun internalPointAngle(theta: Double): Angle {
        val PID = Mecanum.turnPID
        val turnLeft = angleToEuler((unitCircleToHalfCircle(theta).deg - world_deg).degrees)
        drive_omega = turnLeft.deg * PID.kP - Speedometer.degPerSec * PID.kD
        return turnLeft
    }

    fun AGps4.gamepadDrive(xclip: Double = 1.0, yclip: Double = 1.0, thetaClip: Double = 1.0) {
        drive_xv = (this.leftStick.x * xclip) threshold 0.05
        drive_yv= (this.leftStick.y * yclip) threshold 0.05
        drive_omega = (this.rightStick.x * thetaClip) threshold 0.05
    }

    private fun moveFieldCentric(x: Double, y: Double, turn: Double) {
        val pointMove = Point(x, y)
        moveRobotCentricVector(
            pointMove.hypot,
            pointMove.angle - unitCircleToHalfCircle(world_rad),
            turn
        )
    }

    private fun moveRobotCentricVector(vel: Double, direction: Angle, turn: Double) {
        drive_xv = direction.sin * vel
        drive_yv = direction.cos * vel
        drive_omega = turn
    }

    fun robotCentric(x: Double, y: Double, turn: Double) {
        drive_xv = x
        drive_yv = y
        drive_omega = turn
    }

    private fun scaleMovement(scaler: Double) {
        drive_xv *= scaler
        drive_yv *= scaler
    }

    private fun maxMovement() {
        val total = abs(drive_xv) + abs(drive_yv)
        if (total != 0.0) scaleMovement(1.0 / total)
    }

    private fun clipMovement() {
        if (abs(drive_xv) + abs(drive_yv) > 1.0) maxMovement()
    }
}