package com.san68bot.alphaLib.control.logicControllers

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
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
    private var prev_loop_speed = 0.0
    var loop_speed_hz = 0.0
        private set
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

        Globals.resetObjects()
        // Run pre robot setup
        preRobotSetup()

        // Reset movement
        DriveMotion.stop()
        PurePursuit.reset()

        // Setup robot
        robot.setup()
        if (startPose != null) GlobalPosition.setPosition(startPose)

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
                    telemetryBuilder.add("Ready")
                }

                Status.PLAY -> {
                    if (hasStarted) {
                        // Run on main loop, when opmode has started
                        onMainLoop()
                    } else { // Run once on start, when opmode has just started
                        // Reset start position
                        if (startPose != null) GlobalPosition.setPosition(startPose)

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

            // Updating anything passed through coreUpdate
            coreUpdate()

            // Update robot
            robot.update()

            // Update telemetry, with some extra data
            val loopTime = loopTimer.milliseconds
            loop_speed_hz = 1000.0 / (loopTime - prev_loop_speed)
            telemetryBuilder
                .add("seconds till end", secondsTillEnd round 2)
                .add("seconds into mode", secondsIntoMode round 2)
                .add("loop time hz", loop_speed_hz round 1)
                .update()
            prev_loop_speed = loopTime
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

    /**
     * Called every loop for core updates to logic controller
     */
    abstract fun coreUpdate()
}