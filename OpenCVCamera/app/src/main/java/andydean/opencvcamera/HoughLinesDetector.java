package andydean.opencvcamera;

import android.util.Pair;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Andy on 14/11/2016.
 */

public class HoughLinesDetector extends CubeDetector{

    class Vector{
        //Coords of start and end points
        public Point start;
        public double x1;
        public double x2;
        public double y1;
        public double y2;
        public Point end;
        public int angle;

        Vector(double x1, double y1, double x2, double y2, int angle){
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            start = new Point(x1, y1);
            end = new Point(x2, y2);
            this.angle = angle;
        }
    }

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
        variables.put(R.id.hough_min_line_separation, new SettingsVariable("hough_min_line_separation", 4, 100));
        variables.put(R.id.hough_line_separation_error, new SettingsVariable("hough_line_separation_error", 4, 100));

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

        List<List<Vector>> parallelLinesBin = findParallelLines(houghLines);
        imageWithLines = drawParallelLines(parallelLinesBin, imageWithLines);
        List<Pair<Vector, Vector>> pairsOfLines = findPairsOfParallelLines(parallelLinesBin);
        imageWithLines = drawConnectingPairs(pairsOfLines, imageWithLines);

        return imageWithLines;
    }

    /**
     * Draws the lines connecting valid pairs of parallel lines as found by findPairsOfParallelLines
     * @param pairsOfLines
     * @param image
     * @return image
     */
    private Mat drawConnectingPairs(List<Pair<Vector, Vector>> pairsOfLines, Mat image) {
        Iterator<Pair<Vector, Vector>> pairsItr = pairsOfLines.iterator();
        while (pairsItr.hasNext()){
            Pair<Vector, Vector> pair = pairsItr.next();
            Vector v1 = pair.first;
            Vector v2 = pair.second;
            double x1 = (v1.x1 + v1.x2)/2;
            double y1 = (v1.y1 + v1.y2)/2;
            double x2 = (v2.x1 + v2.x2)/2;
            double y2 = (v2.y1 + v2.y2)/2;
            Point start = new Point(x1, y1);
            Point end = new Point(x2, y2);
            Imgproc.line(image, start, end, new Scalar(255, 0, 0, 255), 5);
        }
        return image;
    }

    /**
     * Pairs together parallel lines where the distance between the centres is ~= length of one of the lines
     * Allows for variation of +- hough_line_separation_error %
     * @param parallelLinesBin
     * @return pairs
     */
    private List<Pair<Vector,Vector>> findPairsOfParallelLines(List<List<Vector>> parallelLinesBin) {
        List<Pair<Vector, Vector>> pairs = new ArrayList<Pair<Vector, Vector>>();
        Iterator<List<Vector>> binItr = parallelLinesBin.iterator();
        while(binItr.hasNext()){
            List<Vector> parallelLines = binItr.next();
            Iterator<Vector> pLinesItr = parallelLines.iterator();
            while (pLinesItr.hasNext()){
                Vector vTemp = pLinesItr.next();
                Vector v1 = new Vector(vTemp.x1, vTemp.y1, vTemp.x2, vTemp.y2, vTemp.angle);
                parallelLines.remove(v1);
                Iterator<Vector> pLinesItr2 = parallelLines.iterator();
                while(pLinesItr2.hasNext()){
                    Vector v2 = pLinesItr2.next();
                    double separation = findDistanceBetweenCentres(v1, v2);
                    double v1Length = Math.sqrt((v1.x1 - v1.x2) * (v1.x1 - v1.x2) + (v1.y1 - v1.y2) * (v1.y1 - v1.y2));
                    double v2Length = Math.sqrt((v2.x1 - v2.x2) * (v2.x1 - v2.x2) + (v2.y1 - v2.y2) * (v2.y1 - v2.y2));
                    //The distance betweent the lines should roughly equal the the length of one of the lines
                    //There is a bound for in case the lines are not completely recognised
                    double v1Upper = v1Length + (v1Length * variables.get(R.id.hough_line_separation_error).getVal() / 100);
                    double v1Lower = v1Length - (v1Length * variables.get(R.id.hough_line_separation_error).getVal() / 100);
                    double v2Upper = v2Length + (v2Length * variables.get(R.id.hough_line_separation_error).getVal() / 100);
                    double v2Lower = v2Length - (v2Length * variables.get(R.id.hough_line_separation_error).getVal() / 100);

                    if(v1Lower <= separation &&  separation <= v1Upper || v2Lower <= separation && separation <= v2Upper){
                        pairs.add(new Pair(v1, v2));
                    }
                }
            }
        }

        return pairs;
    }

    /**
     * Draw the lines in the same gradient bin if the bin has enough lines
     * @param parallelLinesBin
     * @param parallelLinesBin, image
     * @return image
     */
    private Mat drawParallelLines(List<List<Vector>> parallelLinesBin, Mat image) {

        Iterator<List<Vector>> binsItr = parallelLinesBin.iterator();
        while(binsItr.hasNext()){
            List<Vector> parallelLines = binsItr.next();
            if(parallelLines.size() >= variables.get(R.id.hough_min_num_parallel_lines).getVal()){
                Iterator<Vector> linesItr = parallelLines.iterator();
                while(linesItr.hasNext()){
                    Vector line = linesItr.next();
                    Imgproc.line(image, line.start, line.end, new Scalar(0, 255, 0, 255), 5);
                }
            }
        }
        return image;
    }

    /**
     * Group the lines with similar gradients (dependant on hough_angle_precision setting)
     * Also prevents lines from being added to the groups if the centre of the line is too close
     * to any of the other lines (dependant on hough_min_line_separation)
     * @param houghLines
     * @return
     */
    private List<List<Vector>> findParallelLines(Mat houghLines) {
        List<List<Vector>> bin = new ArrayList<List<Vector>>();

        //Grab all lines found from houghlines and place them into gradient bins
        for (int x = 0; x < houghLines.rows(); x++) {
            double[] vec = houghLines.get(x, 0);
            double x1 = vec[0],
                    y1 = vec[1],
                    x2 = vec[2],
                    y2 = vec[3];

            double dx = x1 - x2;
            double dy = y1 - y2;


            double grad = dy / dx;
            double angle = Math.toDegrees(Math.atan(grad)) + 90;
            int precision = variables.get(R.id.hough_angle_precision).getVal();
            double roundedAngle = precision * (Math.round(angle / precision));
            Vector vector = new Vector(x1, y1, x2, y2, (int) roundedAngle);

            if (bin.size() == 0) {
                //Start first list
                ArrayList<Vector> lines = new ArrayList<Vector>();
                lines.add(vector);
                bin.add(lines);
            } else {
                //Try to find a list with the same angle
                Iterator<List<Vector>> binsItr = bin.iterator();
                boolean matchingAngle = false;
                boolean tooClose = false;
                while (binsItr.hasNext() && !matchingAngle) {
                    List<Vector> parallelLines = binsItr.next();
                    Iterator<Vector> lines = parallelLines.iterator();

                    while (lines.hasNext()) {
                        Vector v = lines.next();
                        //If the bin has a vector of the same angle, check it is far enough away from the lines
                        if (v.angle == roundedAngle) {
                            matchingAngle = true;
                            double distance = findDistanceBetweenCentres(v, vector);

                            if (distance < variables.get(R.id.hough_min_line_separation).getVal())
                                tooClose = true;
                        }
                    }
                    if (matchingAngle && !tooClose)
                        parallelLines.add(vector);
                }
                //Start a new list if no matching angles were found
                if (!matchingAngle) {
                    ArrayList<Vector> lines = new ArrayList<Vector>();
                    lines.add(vector);
                    bin.add(lines);
                }
            }
        }
        return bin;
    }

    private double findDistanceBetweenCentres(Vector a, Vector b) {
        double midXA = (a.x1 + a.x2)/2;
        double midYA = (a.y1 + a.y2)/2;
        double midXB = (b.x1 + b.x2)/2;
        double midYB = (b.y1 + b.y2)/2;

        double dist = Math.sqrt((midXB - midXA)*(midXB - midXA) + (midYB - midYA)*(midYB - midYA));

        return dist;
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
