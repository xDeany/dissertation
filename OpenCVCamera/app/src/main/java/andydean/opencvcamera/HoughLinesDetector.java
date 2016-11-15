package andydean.opencvcamera;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Andy on 14/11/2016.
 */

public class HoughLinesDetector extends CubeDetector{

    public HoughLinesDetector(){

        variables.put(R.id.bilateral_diameter, new SettingsVariable("bilateral_diameter", 5, 50));
        variables.put(R.id.bilateral_sigma, new SettingsVariable("bilateral_sigma_value", 20, 100));
        variables.put(R.id.canny_threshold_1, new SettingsVariable("canny_threshold1", 50, 400));
        variables.put(R.id.canny_threshold_2, new SettingsVariable("canny_threshold2", 150, 20));
        variables.put(R.id.hough_rho, new SettingsVariable("hough_rho", 1, 20));
        variables.put(R.id.hough_theta, new SettingsVariable("hough_theta", 1, 20));
        variables.put(R.id.hough_threshold, new SettingsVariable("hough_threshold", 50, 200));
        variables.put(R.id.hough_min_line_length, new SettingsVariable("hough_min_line_length", 20, 500));
        variables.put(R.id.hough_max_line_gap, new SettingsVariable("hough_max_line_gap", 10, 20));
        variables.put(R.id.hough_angle_precision, new SettingsVariable("hough_angle_precision", 5, 30));
        variables.put(R.id.hough_min_num_parallel_lines, new SettingsVariable("hough_min_num_parallel_lines", 4, 30));


    }

    private Mat getHoughLines(Mat image){
        int height = image.height();
        int width = image.width();
        Mat grayscaleImage = new Mat(height, width, CvType.CV_8SC4);
        Mat mBlur = new Mat(height, width, CvType.CV_8SC4);
        Mat mCanny = new Mat(height, width, CvType.CV_8SC4);
        Mat houghLines = new Mat();

        Imgproc.cvtColor(image, grayscaleImage, Imgproc.COLOR_RGB2GRAY);
        Imgproc.bilateralFilter(grayscaleImage, mBlur, variables.get(R.id.bilateral_diameter).getVal(), variables.get(R.id.bilateral_sigma).getVal(), variables.get(R.id.bilateral_sigma).getVal());
        Imgproc.Canny(mBlur, mCanny, variables.get(R.id.canny_threshold_1).getVal(), variables.get(R.id.canny_threshold_2).getVal());
        Imgproc.HoughLinesP(mCanny, houghLines, variables.get(R.id.hough_rho).getVal(), variables.get(R.id.hough_theta).getVal() * Math.PI/180, variables.get(R.id.hough_threshold).getVal(), variables.get(R.id.hough_min_line_length).getVal(), variables.get(R.id.hough_max_line_gap).getVal());

        mBlur.release();
        mCanny.release();
        grayscaleImage.release();
        return houghLines;
    }

    private Mat drawHoughLines(Mat houghLines, Mat image){
        Mat imageWithLines = image.clone();

        class Vector{
            //Coords of start and end points
            public Point start;
            public Point end;
            public int angle;

            Vector(Point start, Point end, int angle){
                this.start = start;
                this.end = end;
                this.angle = angle;
            }
        }

        List<List<Vector>> bin = new ArrayList<List<Vector>>();

        //Grab all lines found from houghlines and place them into gradient bins
        for (int x = 0; x < houghLines.rows(); x++) {
            double[] vec = houghLines.get(x, 0);
            double x1 = vec[0],
                    y1 = vec[1],
                    x2 = vec[2],
                    y2 = vec[3];
            Point start = new Point(x1, y1);
            Point end = new Point(x2, y2);

            double dx = x1 - x2;
            double dy = y1 - y2;

            double dist = Math.sqrt(dx * dx + dy * dy);
            double grad = dy/dx;
            double angle = Math.toDegrees(Math.atan(grad)) + 90;
            int precision = variables.get(R.id.hough_angle_precision).getVal();
            double roundedAngle = precision * (Math.round(angle/precision));
            Vector vector = new Vector(start, end, (int) roundedAngle);

            if(bin.size() == 0){
                //Start first list
                ArrayList<Vector> lines = new ArrayList<Vector>();
                lines.add(vector);
                bin.add(lines);
            }else {
                //Try to find a list with the same angle
                Iterator<List<Vector>> binsItr = bin.iterator();
                boolean noMatch = true;
                while (binsItr.hasNext() && noMatch) {
                    List<Vector> parallelLines = binsItr.next();
                    Iterator<Vector> lines = parallelLines.iterator();
                    if (lines.next().angle == roundedAngle) {
                        parallelLines.add(vector);
                        noMatch = false;
                    }
                }
                //Add new list if no matching angles were found
                if(noMatch){
                    ArrayList<Vector> lines = new ArrayList<Vector>();
                    lines.add(vector);
                    bin.add(lines);
                }
            }
        }

        Iterator<List<Vector>> binsItr = bin.iterator();

        while(binsItr.hasNext()){
            List<Vector> parallelLines = binsItr.next();
            if(parallelLines.size() >= variables.get(R.id.hough_min_num_parallel_lines).getVal()){
                Iterator<Vector> linesItr = parallelLines.iterator();
                while(linesItr.hasNext()){
                    Vector line = linesItr.next();
                    Imgproc.line(imageWithLines, line.start, line.end, new Scalar(255-line.angle, line.angle, 0, 255), 5);
                }
            }

        }

        return imageWithLines;
    }

    @Override
    public Mat detectCube(Mat image) {
        Mat houghLines = getHoughLines(image);
        image = drawHoughLines(houghLines, image);
        houghLines.release();
        return image;
    }

    @Override
    public SettingsVariable getInitialVar(){
        return variables.get(R.id.bilateral_diameter);
    }
}
