package org.firstinspires.ftc.teamcode.ours;

import com.qualcomm.robotcore.eventloop.opmode.*;
import com.qualcomm.robotcore.hardware.*;

import org.firstinspires.ftc.robotcore.external.Telemetry;

@ com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "TeleOp")
public class OpMode extends LinearOpMode {
    static DcMotor FL, BL, FR, BR;
    static double speed = 1;

    @Override
    public void runOpMode() throws InterruptedException {
        FL = hardwareMap.get(DcMotor.class, "leftFront");
        BL = hardwareMap.get(DcMotor.class, "leftRear");
        FR = hardwareMap.get(DcMotor.class, "rightFront");
        BR = hardwareMap.get(DcMotor.class, "rightRear");

        FL.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        FR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        BL.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        BR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        FL.setDirection(DcMotor.Direction.REVERSE);
        BL.setDirection(DcMotor.Direction.REVERSE);

        waitForStart();
        
        while (opModeIsActive()){
            telemetry.addData("X", -gamepad1.left_stick_y);
            telemetry.addData("Y", gamepad1.left_stick_x);
            telemetry.update();

            move(gamepad1.left_stick_x, -gamepad1.left_stick_y, gamepad1.right_stick_x, speed);
        }
    }

    private static void move(double x, double y, double r, double speed){
        double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(r), 1);
        double frontLeftPower = (y + x + r) * speed / denominator;
        double backLeftPower = (y - x + r) * speed / denominator;
        double frontRightPower = (y - x - r) * speed / denominator;
        double backRightPower = (y + x - r) * speed / denominator;

        FL.setPower(frontLeftPower);
        BL.setPower(backLeftPower);
        FR.setPower(frontRightPower);
        BR.setPower(backRightPower);
    }
}