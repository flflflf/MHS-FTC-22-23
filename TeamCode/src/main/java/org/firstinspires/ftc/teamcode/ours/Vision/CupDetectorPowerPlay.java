package org.firstinspires.ftc.teamcode.ours.Vision;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

import java.util.ArrayList;
import java.util.List;

public class CupDetectorPowerPlay extends OpenCvPipeline {

    Mat HSVMat = new Mat();

    public Scalar lowerHSV = new Scalar(0, 42.5, 172.8); // the lower limit for the detection (tune this for camera)
    public Scalar upperHSV = new Scalar(120.4, 255, 255); // upper limit also tune this with the camera

    public double blurConstant = 1; // change this to change the Gaussian blur amount

    public double dilationConstant = 2; // tune

    public int cupSide = -1;

    private final Object sync = new Object();

    Telemetry telemetryOpenCV = null;

    public int topX = 220;
    public int topY = 52;
    public int bottomX = 512;

    public int cupWidth = 60;
    public int cupHeight = 120;

    public int yellowLow = 55;
    public int yellowHigh = 75;

    public int blueLow = 195;
    public int blueHigh = 215;

    public int greenLow = 115;
    public int greenHigh = 145;

    // constructor
    public CupDetectorPowerPlay(Telemetry OpModeTelemetry) {
        telemetryOpenCV = OpModeTelemetry;
    }

    public int getCupPosition() {
        synchronized (sync) {
            return cupSide;
        }
    }

    public int getColor(int x, int y, Mat mat, int scale) {
        Mat cropped = new Mat(mat, new Rect(x, y, scale, scale));

        MatOfDouble average = new MatOfDouble();
        MatOfDouble std = new MatOfDouble();
        Core.meanStdDev(cropped, average, std);
        double averageDouble = average.get(0,0)[0];

        if((averageDouble >= yellowLow) && (averageDouble <= yellowHigh)){
            return 0;
        }
        if((averageDouble >= blueLow) && (averageDouble <= blueHigh)){
            return 1;
        }
        if((averageDouble >= greenLow) && (averageDouble <= greenHigh)){
            return 2;
        }
        return -1;
    }

    @Override
    public Mat processFrame(Mat input) {
        Imgproc.cvtColor(input, HSVMat, Imgproc.COLOR_RGB2HSV_FULL);
        Core.inRange(HSVMat, lowerHSV, upperHSV, HSVMat);

        Size kernelSize = new Size(blurConstant, blurConstant);

        // adds blur effect to the image to help image processing
        Imgproc.GaussianBlur(HSVMat, HSVMat, kernelSize, 0);
        Size kernelRectangleSize = new Size(2 * dilationConstant + 1, 2 * dilationConstant + 1);

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, kernelRectangleSize); // dialution
        Imgproc.dilate(HSVMat, HSVMat, kernel);

        int xLength = (bottomX-topX)/(cupWidth/2)+1;

        int[] colors = new int[xLength*2];

        synchronized (sync) {
            for(int yindex=1; yindex<=2; yindex++){
                for(int xindex=1; xindex<xLength; xindex++){
                    int xabsolute = xindex*(cupWidth/2)+topX;
                    int yabsolute = yindex*(cupHeight/3)+topY;
                    int color = getColor(xabsolute, yabsolute, HSVMat, 3);
                    Imgproc.circle(input, new Point(xabsolute, yabsolute), 2, new Scalar((color+1)*(255/3), 0, 0), 2);
                    colors[xindex*yindex] = getColor(xabsolute, yabsolute, HSVMat, 3);
                }
            }

            for(int i=1; i<=colors.length/2; i++){
                if(colors[i-1] == colors[i]){
                    if(colors[i-1] == colors[i]){
                        if(colors[i-1+xLength] == colors[i]){
                            if(colors[i+xLength] == colors[i]){
                                cupSide = colors[i];
                                break;
                            }
                        }
                    }
                }
            }
        }
        Imgproc.rectangle(input, new Point(topX, topY), new Point(bottomX, topY+cupHeight), new Scalar(255, 0, 0), 2);
        telemetryOpenCV.addData("Cup Color:", cupSide);
        telemetryOpenCV.update();
        return input;
    }
}