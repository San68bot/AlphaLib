package com.san68bot.alphaLib.control.logicControllers

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.util.ElapsedTime
import com.san68bot.alphaLib.control.motion.drive.DriveMotion
import com.san68bot.alphaLib.control.motion.localizer.GlobalPosition
import com.san68bot.alphaLib.control.motion.purepursuit.PurePursuit
import com.san68bot.alphaLib.geometry.Pose
import com.san68bot.alphaLib.subsystem.Robot
import com.san68bot.alphaLib.utils.field.Alliance
import com.san68bot.alphaLib.utils.field.Globals
import com.san68bot.alphaLib.utils.field.RunData
import com.san68bot.alphaLib.utils.math.round
import com.san68bot.alphaLib.wrappers.util.AGps4
import com.san68bot.alphaLib.wrappers.util.ActionTimer
import com.san68bot.alphaLib.wrappers.util.PS4Master
import com.san68bot.alphaLib.wrappers.util.TelemetryBuilder

abstract class AlphaGoInterface(
    val robot: Robot,
    private val alliance: Alliance,
    private val autonomous: Boolean,
    private val startPose: Pose?
): LinearOpMode() {
    /**
     * PS4 Controller objects, to be used in the opmode
     */
    lateinit var driver: AGps4
    lateinit var operator: AGps4

    /**
     * Global telemetry object, can be used when running any opmode and to log subsystem data
     */
    lateinit var telemetryBuilder: TelemetryBuilder

    private var hasStarted = false
    private val loopTimer = ActionTimer()
    private val runTimeTimer = ActionTimer()

    /**
     * The current stage of the opmode
     */
    enum class Status {
        INIT,
        PLAY,
        STOP
    }

    val modeStatus: Status
        get() = when {
            isStopRequested -> Status.STOP
            isStarted       -> Status.PLAY
            else            -> Status.INIT
        }

    final override fun runOpMode() {
        // Set global variables
        Globals.apply {
            agInterface = this@AlphaGoInterface
            isAuto = autonomous; isTeleop = !autonomous
        }

        // Set alliance of current run
        alliance.let { RunData.ALLIANCE = it }

        // Initialize telemetry
        telemetryBuilder = TelemetryBuilder(telemetry)

        // Initialize PS4 controllers
        PS4Master.reset()
        driver = AGps4(gamepad1)
        operator = AGps4(gamepad2)

        // Run pre robot setup
        preRobotSetup()

        // Reset movement
        DriveMotion.stop()
        PurePursuit.reset()

        // Setup robot
        robot.setup()
        startPose?.let { GlobalPosition.setPosition(it) }

        // Run on init
        onInit()

        loopTimer.reset()
        eventLoop@ while (true) {
            // Update PS4 controllers
            PS4Master.update()

            // Recreate telemetry
            telemetryBuilder = TelemetryBuilder(telemetry)

            when (modeStatus) {
                Status.INIT -> {
                    // Run on init loop
                    onInitLoop()
                    telemetryBuilder.add("Ready to start")
                }

                Status.PLAY -> {
                    if (hasStarted) {
                        // Run on main loop, when opmode has started
                        onMainLoop()
                    } else { // Run once on start, when opmode has just started
                        // Reset start position
                        startPose?.let { GlobalPosition.setPosition(it) }

                        // Reset run timer
                        runTimeTimer.reset()

                        // Run once on start
                        onStart()
                        hasStarted = true
                    }
                }

                Status.STOP -> {
                    // Run once on stop
                    DriveMotion.stop()
                    break@eventLoop
                }
            }

            // Update robot
            robot.update()

            // Update telemetry, with some extra data
            telemetryBuilder
                .add("seconds till end", secondsTillEnd)
                .add("seconds into mode", secondsIntoMode)
                .add("loop time", loopTimer.milliseconds)
                .update()

            // Reset loop timer
            loopTimer.reset()
        }
        // Run once on stop
        onStop()
    }

    /**
     * Seconds till end of opmode, as autonomous or teleop
     */
    val secondsTillEnd: Double get() =
        ((if (autonomous) 30.0 else 120.0) - secondsIntoMode).takeIf { it > 0.0 } ?: 0.0

    /**
     * Seconds the opmode has been running
     */
    val secondsIntoMode: Double get() =
        runTimeTimer.seconds

    /**
     * Anything you want to do before the robot gets setup, e.g. setup camera / modify subsystem variables
     */
    abstract fun preRobotSetup()

    /**
     * Called once after the robot is initialized
     */
    abstract fun onInit()

    /**
     * Called every loop while the opmode has not started
     */
    abstract fun onInitLoop()

    /**
     * Called once when the opmode starts, inbetween onInitLoop and onMainLoop
     */
    abstract fun onStart()

    /**
     * Called every loop while the opmode is running, main part of the opmode
     */
    abstract fun onMainLoop()

    /**
     * Called once when the opmode requests a stop
     */
    abstract fun onStop()
}