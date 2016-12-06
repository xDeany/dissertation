package andydean.opencvcamera;

import android.util.Pair;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.LineSegmentDetector;
import org.opencv.video.Video;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Created by Andy on 14/11/2016.
 */

public class HoughLinesDetector extends CubeDetector{

    class Vector{
        //Coords of start and end points
        private Point start;
        private double x1;
        private double x2;
        private double y1;
        private double y2;
        private Point end;
        private int angle;
        //Equation of line y = mx + c
        private Double m;
        public Double c;

        Vector(double x1, double y1, double x2, double y2, int angle){
            //Make sure the pairs are ordered correctly for later analysis with x1,y1 being the point with the lowest x
            this.x1 = Math.min(x1, x2);
            this.x2 = Math.max(x1, x2);
            if(x1 < x2){
                //x1 is min
            this.y1 = y1;
            this.y2 = y2;}
            else{
                this.y1 = y2;
                this.y2 = y1;
            }
            start = new Point(x1, y1);
            end = new Point(x2, y2);
            this.angle = angle;
            if(Math.abs(angle) == 0){
                this.angle = 0;
                this.m = null;
                this.c = y1;
            }else if(Math.abs(angle) == 90){
                this.angle = 90;
                this.m = null;
                this.c = null;
            }else {
                this.m = Math.tan(Math.toRadians(angle));
                this.c = calcYIntersect(x1, y1, this.m);
                this.angle = angle;
            }
        }

        public double calcYIntersect(double x, double y, double m){
            double c = y - (m*x);
            return c;
        }
    }

    public HoughLinesDetector(){

        variables.put(R.id.downsample_ratio, new SettingsVariable("downsample_ratio", 1, 20));
        variables.put(R.id.bilateral_diameter, new SettingsVariable("bilateral_diameter", 5, 50));
        variables.put(R.id.bilateral_sigma, new SettingsVariable("bilateral_sigma_value", 20, 100));
        variables.put(R.id.canny_threshold_1, new SettingsVariable("canny_threshold1", 50, 400));
        variables.put(R.id.canny_threshold_2, new SettingsVariable("canny_threshold2", 150, 20));
        variables.put(R.id.hough_rho, new SettingsVariable("hough_rho", 1, 20));
        variables.put(R.id.hough_theta, new SettingsVariable("hough_theta", 1, 20));
        variables.put(R.id.hough_threshold, new SettingsVariable("hough_threshold", 50, 200));
        variables.put(R.id.hough_min_line_length, new SettingsVariable("hough_min_line_length", 20, 500));
        variables.put(R.id.hough_max_line_gap, new SettingsVariable("hough_max_line_gap", 10, 20));
        variables.put(R.id.bin_precision, new SettingsVariable("bin_precision", 5, 30));
        variables.put(R.id.min_distance_between_centres, new SettingsVariable("min_distance_between_centres", 4, 100));
        variables.put(R.id.min_dist_error_percent, new SettingsVariable("min_dist_error_percent", 4, 100));
        variables.put(R.id.perpendicular_dist_min, new SettingsVariable("perpendicular_dist_min", 4, 100));
    }

    private Mat toGrayscale(Mat image){
        Mat grayscaleImage = new Mat(image.height(), image.width(), image.type());
        Imgproc.cvtColor(image, grayscaleImage, Imgproc.COLOR_RGB2GRAY);
        return grayscaleImage;
    }

    private Mat toBlur(Mat image){
        Mat blurredImage = new Mat(image.height(), image.width(), image.type());
        Imgproc.bilateralFilter(image, blurredImage, variables.get(R.id.bilateral_diameter).getVal(), variables.get(R.id.bilateral_sigma).getVal(), variables.get(R.id.bilateral_sigma).getVal());
        return blurredImage;
    }

    private Mat toCanny(Mat image){
        Mat cannyImage = new Mat(image.height(), image.width(), image.type());
        Imgproc.Canny(image, cannyImage, variables.get(R.id.canny_threshold_1).getVal(), variables.get(R.id.canny_threshold_2).getVal());
        return cannyImage;
    }

    private Mat toHoughLines(Mat image){
        Mat houghLines = new Mat();
        Imgproc.HoughLinesP(image, houghLines, variables.get(R.id.hough_rho).getVal(), variables.get(R.id.hough_theta).getVal() * Math.PI/180, variables.get(R.id.hough_threshold).getVal(), variables.get(R.id.hough_min_line_length).getVal(), variables.get(R.id.hough_max_line_gap).getVal());
        return houghLines;
    }


    private List<Vector> matToListVector (Mat houghLines){
        List<Vector> vLines = new ArrayList<Vector>();
        for (int x = 0; x < houghLines.rows(); x++) {
            double[] vec = houghLines.get(x, 0);
            double  x1 = vec[0],
                    y1 = vec[1],
                    x2 = vec[2],
                    y2 = vec[3];

            double dx = x1 - x2;
            double dy = y1 - y2;


            double grad = dy / dx;
            double angle = Math.toDegrees(Math.atan(grad));
            int precision = variables.get(R.id.bin_precision).getVal();
            double roundedAngle = precision * (Math.round(angle / precision));
            Vector vector = new Vector(x1, y1, x2, y2, (int) roundedAngle);
            vLines.add(vector);
        }
        return vLines;

    }


    private Mat drawLines(List<Vector> lines, Mat image){
        Mat imageWithLines = image.clone();
        Iterator<Vector> linesItr = lines.iterator();
        while(linesItr.hasNext()){
            Vector vec = linesItr.next();
            Imgproc.line(imageWithLines, vec.start, vec.end, new Scalar(0, 255, 0, 255), 5);
        }
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
                //Clone vector
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
                    double v1Upper = v1Length + (v1Length * variables.get(R.id.min_dist_error_percent).getVal() / 100);
                    double v1Lower = v1Length - (v1Length * variables.get(R.id.min_dist_error_percent).getVal() / 100);
                    double v2Upper = v2Length + (v2Length * variables.get(R.id.min_dist_error_percent).getVal() / 100);
                    double v2Lower = v2Length - (v2Length * variables.get(R.id.min_dist_error_percent).getVal() / 100);

                    if(v1Lower <= separation &&  separation <= v1Upper || v2Lower <= separation && separation <= v2Upper){
                        pairs.add(new Pair<Vector,Vector>(v1, v2));
                    }
                }
            }
        }

        return pairs;
    }

    /**
     * Draw the lines in the same gradient bin
     * @param parallelLinesBin
     * @param parallelLinesBin, image
     * @return image
     */
    private Mat drawOnlyParallelLines(List<List<Vector>> parallelLinesBin, Mat image) {

        Iterator<List<Vector>> binsItr = parallelLinesBin.iterator();
        while(binsItr.hasNext()){
            List<Vector> parallelLines = binsItr.next();
            Iterator<Vector> linesItr = parallelLines.iterator();
            while(linesItr.hasNext()){
                Vector line = linesItr.next();
                Imgproc.line(image, line.start, line.end, new Scalar(0, 255, 0, 255), 5);
            }
        }
        return image;
    }

    /**
     * Group the lines with similar gradients (dependant on hough_angle_precision setting)
     * Also prevents lines from being added to the groups if the centre of the line is too close
     * to any of the other lines (dependant on hough_min_line_separation)
     * @param vLines
     * @return
     */
    private List<List<Vector>> findParallelLines(List<Vector> vLines) {
        List<List<Vector>> bin = new ArrayList<List<Vector>>();
        Iterator<Vector> vLinesItr = vLines.iterator();
        //Grab all lines found from houghlines and place them into gradient bins
        while(vLinesItr.hasNext()){
            Vector vector = vLinesItr.next();
            //Add first item to bin
            if (bin.size() == 0) {
                //Start first list
                ArrayList<Vector> lines = new ArrayList<Vector>();
                lines.add(vector);
                bin.add(lines);
            } else {
                //Iterate through each list
                //Compare the angle of the first item in each list to the vector
                //If they are equal, add the vector to that list
                //Else start a new list with that vector
                Iterator<List<Vector>> binsItr = bin.iterator();
                boolean matchedAngle = false;
                //boolean tooClose = false;
                while (binsItr.hasNext() && !matchedAngle) {
                    List<Vector> parallelLines = binsItr.next();
                    Iterator<Vector> lines = parallelLines.iterator();
                    Vector v = lines.next();
                    //while (lines.hasNext()) {
                      //  Vector v = lines.next();
                        //If the bin already has a line of the same angle, check it is far enough away from the other lines
                        //if (v.angle == vector.angle) {
                          //  matchingAngle = true;
                            //double distance = findDistanceBetweenCentres(v, vector);

                            //if (distance < variables.get(R.id.min_distance_between_centres).getVal())
                            //    tooClose = true;
                        //}
                    //}
                    if (v.angle == vector.angle) {
                        parallelLines.add(vector);
                        matchedAngle = true;
                    }
                }
                //Start a new list if no matching angles were found
                if (!matchedAngle) {
                    ArrayList<Vector> lines = new ArrayList<Vector>();
                    lines.add(vector);
                    bin.add(lines);
                }
            }
        }
        //Only need to return two largest bins
        List<List<Vector>> bestBin = getTwoLargestBins(bin);
        return bestBin;
    }

    private double findDistanceBetweenCentres(Vector a, Vector b) {
        double midXA = (a.x1 + a.x2)/2;
        double midYA = (a.y1 + a.y2)/2;
        double midXB = (b.x1 + b.x2)/2;
        double midYB = (b.y1 + b.y2)/2;

        double dist = Math.sqrt((midXB - midXA)*(midXB - midXA) + (midYB - midYA)*(midYB - midYA));

        return dist;
    }

    private double calcDistBetweenPoints(Point a, Point b){
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        double dist = Math.sqrt((dx * dx) + (dy * dy));
        return dist;
    }

    /**
     * Finds the perpendicular distance between each pair of parallel lines
     * If this distance is too small i.e. the lines too close together two be classed as distinct lines
     * It will connect them into a single line
     * The lines must also be reasonably close (distances between centres < threshold)
     * @param pLinesBin
     * @return
     */

    private List<List<Vector>> joinCloseLines(List<List<Vector>> pLinesBin){
        Iterator<List<Vector>> binItr = pLinesBin.iterator();
        while(binItr.hasNext()){
            List<Vector> lines = binItr.next();
            Pair<Vector, Vector> toJoin = findLinesToJoin(lines);
            while(toJoin != null){
                //Join lines together by finding the start and end points that ars farthest from the midpoint between the lines
                Vector v1 = toJoin.first;
                Vector v2 = toJoin.second;
                double midV1X = (v1.x1 + v1.x2) / 2;
                double midV1Y = (v1.y1 + v1.y2) / 2;
                double midV2X = (v2.x1 + v2.x2) / 2;
                double midV2Y = (v2.y1 + v2.y2) / 2;
                double midX = (midV1X + midV2X) / 2;
                double midY = (midV1Y + midV2Y) /2;
                Point mid = new Point(midX,midY);
                double startV1Mid = calcDistBetweenPoints(v1.start, mid);
                double startV2Mid = calcDistBetweenPoints(v2.start, mid);
                double endV1Mid = calcDistBetweenPoints(v1.end, mid);
                double endV2Mid = calcDistBetweenPoints(v2.end, mid);

                double x1,x2,y1,y2;
                if(startV1Mid > startV2Mid){
                    x1 = v1.start.x;
                    y1 = v1.start.y;
                }else{
                    x1 = v2.start.x;
                    y1 = v2.start.y;
                }

                if(endV1Mid > endV2Mid){
                    x2 = v1.end.x;
                    y2 = v1.end.y;
                }else{
                    x2 = v2.end.x;
                    y2 = v2.end.y;
                }

                Vector v3 = new Vector(x1, y1, x2, y2, v1.angle);
                lines.remove(v1);
                lines.remove(v2);
                lines.add(v3);
                toJoin = findLinesToJoin(lines);
            }
        }
        return pLinesBin;
    }

    /**
     * Finds the perpendicular distance between each pair of parallel lines
     * If this distance is too small i.e. the lines too close together two be classed as distinct lines
     * The lines must also be reasonably close (distances between centres < threshold)
     */
    private Pair<Vector, Vector> findLinesToJoin(List<Vector> lines){
        Iterator<Vector> linesItr1 = lines.iterator();
        Iterator<Vector> linesItr2 = lines.iterator();
        while(linesItr1.hasNext()){
            Vector v1 = linesItr1.next();
            while(linesItr2.hasNext()){
                Vector v2 = linesItr2.next();
                if(v1 != v2){
                    double pDist = findPerpendicularDistance(v1, v2);
                    if(pDist <= variables.get(R.id.perpendicular_dist_min).getVal())
                        return new Pair<Vector, Vector>(v1, v2);
                }
            }
        }
        return null;
    }

    /**
     * To find the perpendicular distance between two vectors
     * Need to find the equation for the normal of vector 1
     * Find the intersect between this normal and the second vector
     * Calculate distance between the points where the normal crosses each vector
     * @param v1
     * @param v2
     * @return distance
     */
    private double findPerpendicularDistance(Vector v1, Vector v2) {
        //If line is vertical (i.e. m is inf because it is at 90deg to horizontal
        if(Math.abs(v1.angle) == 90 ){
            return Math.abs(v1.x1-v2.x1);
        }else if(Math.abs(v1.angle) == 0){
            //If line is horizontal i.e. m = +- 0
            return Math.abs(v1.y1 - v2.y1);
        }
        double norm1M = -1/v1.m;
        double norm1C = v1.calcYIntersect(v1.x1, v1.y1, norm1M);
        Pair<Double, Double> norm2IntersectV2 = findIntersect(v2.m, v2.c, norm1M, norm1C);
        //The perpendicular line has points v1.x1, v1.y1 and the pair of points returned by findIntersect (with v2)
        double dx = norm2IntersectV2.first - v1.x1;
        double dy = norm2IntersectV2.second - v1.y1;
        return Math.sqrt((dx * dx) + (dy * dy));
    }

    private Pair<Double, Double> findIntersect(double m1, double c1, double m2, double c2){
        Double x = (c2-c1)/(m1-m2);
        Double y = (m1*x) + c1;
        return new Pair<Double, Double>(x,y);
    }

    private Mat downscale(Mat image, int ratio){
        if(ratio > 1) {
            int divisor = (int) Math.pow(2,ratio);
            Mat downscaled = new Mat(image.rows() / divisor, image.cols() / divisor, image.type());
            Mat toDownscale = downscale(image, ratio-1);
            Imgproc.pyrDown(toDownscale, downscaled, new Size(downscaled.cols(), downscaled.rows()));
            toDownscale.release();
            return downscaled;
        }else if(ratio == 1) {
            Mat temp = new Mat(image.rows()/2, image.cols()/2, image.type());
            Imgproc.pyrDown(image, temp, new Size(image.cols()/2, image.rows()/2));
            return temp;
        }

        return image.clone();
    }

    private Mat upscale(Mat image, int ratio){
        if(ratio > 1) {
            int multiplier = (int) Math.pow(2, ratio);
            Mat upscaled = new Mat(image.rows() * multiplier, image.cols() * multiplier, image.type());
            Mat temp = upscale(image, ratio-1);
            Imgproc.pyrUp(temp, upscaled, new Size(upscaled.cols(), upscaled.rows()));
            temp.release();
            return upscaled;
        }else if(ratio == 1) {
            Mat temp = new Mat(image.rows()*2, image.cols()*2, image.type());
            Imgproc.pyrUp(image, temp, new Size(image.cols()*2, image.rows()*2));
            return temp;
        }

        return image.clone();
    }

    @Override
    public Mat detectCube(Mat image, String imageToReturn) {
        int ratio = variables.get(R.id.downsample_ratio).getVal() - 1;

        Mat downscaled = downscale(image, ratio);
        Mat grayscaleImage = toGrayscale(downscaled);
        Mat mBlur = toBlur(grayscaleImage);
        Mat mCanny = toCanny(mBlur);
        Mat houghLines = toHoughLines(mCanny);
        List<Vector> vLines = matToListVector(houghLines);
        Mat dHoughOverlayRaw = drawLines(vLines, downscaled);

        //Place them into bins of parallel lines
        List<List<Vector>> parallelLinesBin = findParallelLines(vLines);

        //Try to join all similar lines together
        List<List<Vector>> linesAfterJoinging = joinCloseLines(parallelLinesBin);

        //Only draw the line in bins where bin.size() > hough_min_num_parallel_lines
        Mat dHoughOverlayParallel = drawOnlyParallelLines(linesAfterJoinging, downscaled);

        //Find the most likely pairs of lines
        //List<Pair<Vector, Vector>> pairsOfLines = findPairsOfParallelLines(parallelLinesBin);

        //Draw these connections
        //Mat connectionsOverlay = drawConnectingPairs(pairsOfLines, downscaled);
        //Mat connectionsAndLinesOverlay = drawConnectingPairs(pairsOfLines, dHoughOverlayParallel);

        //Mat imageWithOverlay = upscale(dHoughOverlayRaw, ratio);
        Mat toReturn;


        switch(imageToReturn){
            case "original_image":
                toReturn = image.clone();
                break;
            case "grayscale_image":
                toReturn = upscale(grayscaleImage.clone(), ratio);
                break;
            case "blurred_image":
                toReturn = upscale(mBlur.clone(), ratio);
                break;
            case "canny_edges":
                toReturn = upscale(mCanny.clone(), ratio);
                break;
            case "hough_lines_only":
                Mat blankCanvas = new Mat(downscaled.rows(), downscaled.cols(), CvType.CV_8UC4, new Scalar(0,0,0,255));
                dHoughOverlayRaw = drawLines(vLines, blankCanvas);
                toReturn = upscale(dHoughOverlayRaw.clone(), ratio);
                blankCanvas.release();
                break;
            case "hough_lines_overlay":
                toReturn = upscale(dHoughOverlayRaw.clone(), ratio);
                break;
            case "hough_lines_grouped_only":
                Mat blank = new Mat(downscaled.rows(), downscaled.cols(), CvType.CV_8UC4, new Scalar(0,0,0,255));
                List<Vector> lines = new ArrayList<Vector>();
                for(List<Vector> l : linesAfterJoinging){
                    lines.addAll(l);
                }
                Mat groupedLinesOnly = drawLines(lines, blank);
                toReturn = upscale(groupedLinesOnly.clone(), ratio);
                blank.release();
                groupedLinesOnly.release();
                break;
            case "hough_lines_grouped_overlay":
                toReturn = upscale(dHoughOverlayParallel.clone(), ratio);
                break;
            /*case "connections":
                toReturn = upscale(connectionsOverlay.clone(), ratio);
                break;
            case "connections_and_lines":
                toReturn = upscale(connectionsAndLinesOverlay.clone(), ratio);
                break;*/
            default:
                toReturn = image.clone();
                break;
        }

        mBlur.release();
        mCanny.release();
        grayscaleImage.release();
        houghLines.release();
        downscaled.release();
        dHoughOverlayRaw.release();
        dHoughOverlayParallel.release();
        //connectionsOverlay.release();
        //connectionsAndLinesOverlay.release();

        return toReturn;
    }

    /**
     * Find the two bins that have the most vectors
     * @param bin
     * @return newBin
     */
    private List<List<Vector>> getTwoLargestBins(List<List<Vector>> bin) {
        Iterator<List<Vector>> binItr = bin.iterator();
        List<Vector> largestBin = new ArrayList<Vector>();
        List<Vector> secondLargestBin = new ArrayList<Vector>();
        int max = 0;
        int secondMax = 0;
        while(binItr.hasNext()){
            List<Vector> list = binItr.next();
            if(list.size() > max){
                max = list.size();
                largestBin = list;
            }else if(list.size() > secondMax){
                secondMax = list.size();
                secondLargestBin = list;
            }
        }
        List<List<Vector>> newBin = new ArrayList<List<Vector>>();
        newBin.add(largestBin);
        newBin.add(secondLargestBin);
        return newBin;
    }

    @Override
    public SettingsVariable getInitialVar(){
        return variables.get(R.id.bilateral_diameter);
    }
}
