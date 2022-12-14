package com.san68bot.alphaLib.wrappers.util

import com.acmerobotics.dashboard.*
import com.acmerobotics.dashboard.telemetry.*
import com.acmerobotics.roadrunner.geometry.Pose2d
import com.san68bot.alphaLib.geometry.Pose
import com.san68bot.alphaLib.utils.field.Globals.agInterface
import com.san68bot.alphaLib.utils.math.round
import org.firstinspires.ftc.robotcore.external.*
import kotlin.math.*

class TelemetryBuilder(
    private val telemetry: Telemetry,
    robotWidth : Double = agInterface.robot.ROBOT_WIDTH,
    robotLength : Double = agInterface.robot.ROBOT_LENGTH
) {
    private var packet = TelemetryPacket()
    var ftcDashboard: FtcDashboard = FtcDashboard.getInstance()
        private set

    fun add(line: String?): TelemetryBuilder {
        packet.addLine(line)
        telemetry.addLine(line)
        return this
    }

    fun add(key: String?, value: Any?): TelemetryBuilder {
        packet.put(key, value)
        telemetry.addData(key, if (value is Number) (value.toDouble() round 3) else value)
        return this
    }

    fun telemetryAdd(key: String?, value: Any?): TelemetryBuilder {
        telemetry.addData(key, if (value is Number) (value.toDouble() round 3) else value)
        return this
    }

    fun drawDrivetrain_rr(pose: Pose2d, color: String = "black"): TelemetryBuilder {
        val newPose = Pose2d(pose.y, -pose.x, pose.heading)
        packet.fieldOverlay().setStroke(color).strokeCircle(newPose.x, newPose.y, 9.0)
        val (x, y) = newPose.headingVec().times(9.0)
        val x1 = newPose.x + x / 2.0
        val y1 = newPose.y + y / 2.0
        val x2 = newPose.x + x
        val y2 = newPose.y + y
        packet.fieldOverlay().strokeLine(x1, y1, x2, y2)
        return this
    }

    fun drawDrivetrain(pose: Pose, primaryColor: String? = "black", secondaryColor: String?): TelemetryBuilder {
        return drawDrivetrain(pose.x, pose.y, pose.rad, primaryColor, secondaryColor)
    }

    private val w = (robotWidth/2.0) * sqrt(2.0)
    private val l = (robotLength/2.0) * sqrt(2.0)
    fun drawDrivetrain(robotX: Double, robotY: Double, robotTheta: Double, primaryColor: String? = "black", secondaryColor: String?): TelemetryBuilder {
        val x = robotY - 72.0
        val y = 72.0 - robotX
        val theta = PI / 2.0 + robotTheta
        val xcoords = doubleArrayOf(
            w * cos(PI / 4.0 + theta) + x,
            w * cos(3.0 * PI / 4.0 + theta) + x,
            w * cos(5.0 * PI / 4.0 + theta) + x,
            w * cos(7.0 * PI / 4.0 + theta) + x
        )
        val ycoords = doubleArrayOf(
            l * sin(PI / 4.0 + theta) + y,
            l * sin(3.0 * PI / 4.0 + theta) + y,
            l * sin(5.0 * PI / 4.0 + theta) + y,
            l * sin(7.0 * PI / 4.0 + theta) + y
        )

        val robotPose = Pose(x, y, robotTheta - PI/2.0)
        val (lx, ly) = robotPose.headingVec().scale(9.0, 9.0)
        val x1 = robotPose.x + lx / 2.0
        val y1 = robotPose.y + ly / 2.0
        val x2 = robotPose.x + lx
        val y2 = robotPose.y + ly

        packet.fieldOverlay()
            .setFill(primaryColor).fillPolygon(xcoords, ycoords)
            .setStroke(secondaryColor).strokeLine(x1, y1, x2, y2)
        return this
    }

    fun update(): TelemetryBuilder {
        ftcDashboard = FtcDashboard.getInstance()
        ftcDashboard.sendTelemetryPacket(packet)
        packet = TelemetryPacket()
        telemetry.update()
        return this
    }
}