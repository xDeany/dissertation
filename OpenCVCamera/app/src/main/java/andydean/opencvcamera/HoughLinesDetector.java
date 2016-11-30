package andydean.opencvcamera;

import android.util.Pair;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Andy on 14/11/2016.
 */

public class HoughLinesDetector extends CubeDetector{

    class Line {
        //Coords of start and end points
        private Point start;
        private Point end;
        private double dy;
        private double dx;
        private int angle;
        //Equation of line y = mx + c
        private Double m;
        public Double c;

        Line(Point p1, Point p2, int angle){
            //Make sure the pairs are ordered correctly for later analysis with x1,y1 being the point with the lowest x
            double x1 = Math.min(p1.x, p2.x);
            double x2 = Math.max(p1.x, p2.x);
            this.start = p1.x == x1 ? p1 : p2;
            this.end = this.start == p1 ? p2 : p1;
            this.dy = end.y - start.y;
            this.dx = end.x - start.x;

            this.angle = angle;
            if(Math.abs(angle) == 0){
                //Line is horizontal
                this.angle = 0;
                this.m = 0.0;
                this.c = start.y;
            }else if(Math.abs(angle) == 90){
                //Line is vertical
                this.angle = 90;
                this.m = null;
                this.c = null;
            }else {
                this.m = Math.tan(Math.toRadians(angle));
                this.c = calcYIntersect(start.x, start.y, this.m);
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


    private List<Line> matToListVector (Mat houghLines){
        List<Line> vLines = new ArrayList<Line>();
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
            Line line = new Line(new Point(x1, y1), new Point(x2, y2), (int) roundedAngle);
            vLines.add(line);
        }
        return vLines;

    }


    private Mat drawLines(List<Line> lines, Mat image){
        Mat imageWithLines = image.clone();
        Iterator<Line> linesItr = lines.iterator();
        while(linesItr.hasNext()){
            Line vec = linesItr.next();
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
    private Mat drawConnectingPairs(List<Pair<Line, Line>> pairsOfLines, Mat image) {
        Iterator<Pair<Line, Line>> pairsItr = pairsOfLines.iterator();
        while (pairsItr.hasNext()){
            Pair<Line, Line> pair = pairsItr.next();
            Line v1 = pair.first;
            Line v2 = pair.second;
            double x1 = (v1.start.x + v1.end.x)/2;
            double y1 = (v1.start.y + v1.end.y)/2;
            double x2 = (v2.start.x + v2.end.x)/2;
            double y2 = (v2.start.y + v2.end.y)/2;
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
    private List<Pair<Line,Line>> findPairsOfParallelLines(List<List<Line>> parallelLinesBin) {
        List<Pair<Line, Line>> pairs = new ArrayList<Pair<Line, Line>>();
        Iterator<List<Line>> binItr = parallelLinesBin.iterator();
        while(binItr.hasNext()){
            List<Line> parallelLines = binItr.next();
            Iterator<Line> pLinesItr = parallelLines.iterator();
            while (pLinesItr.hasNext()){
                Line vTemp = pLinesItr.next();
                //Clone vector
                Line v1 = new Line(vTemp.start, vTemp.end, vTemp.angle);
                parallelLines.remove(v1);
                Iterator<Line> pLinesItr2 = parallelLines.iterator();
                while(pLinesItr2.hasNext()){
                    Line v2 = pLinesItr2.next();
                    Point midV1 = findMidPoint(v1.start, v1.end);
                    Point midV2 = findMidPoint(v2.start, v2.end);
                    double separation = calcDistBetweenPoints(midV1, midV2);
                    double v1Length = Math.sqrt((v1.dx) * (v1.dx) + (v1.dy) * (v1.dy));
                    double v2Length = Math.sqrt((v2.dx) * (v2.dx) + (v2.dy) * (v2.dy));
                    //The distance betweent the lines should roughly equal the the length of one of the lines
                    //There is a bound for in case the lines are not completely recognised
                    double v1Upper = v1Length + (v1Length * variables.get(R.id.min_dist_error_percent).getVal() / 100);
                    double v1Lower = v1Length - (v1Length * variables.get(R.id.min_dist_error_percent).getVal() / 100);
                    double v2Upper = v2Length + (v2Length * variables.get(R.id.min_dist_error_percent).getVal() / 100);
                    double v2Lower = v2Length - (v2Length * variables.get(R.id.min_dist_error_percent).getVal() / 100);

                    if(v1Lower <= separation &&  separation <= v1Upper || v2Lower <= separation && separation <= v2Upper){
                        pairs.add(new Pair<Line,Line>(v1, v2));
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
    private Mat drawOnlyParallelLines(List<List<Line>> parallelLinesBin, Mat image) {

        Iterator<List<Line>> binsItr = parallelLinesBin.iterator();
        while(binsItr.hasNext()){
            List<Line> parallelLines = binsItr.next();
            Iterator<Line> linesItr = parallelLines.iterator();
            while(linesItr.hasNext()){
                Line line = linesItr.next();
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
    private List<List<Line>> findParallelLines(List<Line> vLines) {
        List<List<Line>> bin = new ArrayList<List<Line>>();
        Iterator<Line> vLinesItr = vLines.iterator();
        //Grab all lines found from houghlines and place them into gradient bins
        while(vLinesItr.hasNext()){
            Line line = vLinesItr.next();
            //Add first item to bin
            if (bin.size() == 0) {
                //Start first list
                ArrayList<Line> lines = new ArrayList<Line>();
                lines.add(line);
                bin.add(lines);
            } else {
                //Iterate through each list
                //Compare the angle of the first item in each list to the line
                //If they are equal, add the line to that list
                //Else start a new list with that line
                Iterator<List<Line>> binsItr = bin.iterator();
                boolean matchedAngle = false;
                //boolean tooClose = false;
                while (binsItr.hasNext() && !matchedAngle) {
                    List<Line> parallelLines = binsItr.next();
                    Iterator<Line> lines = parallelLines.iterator();
                    Line v = lines.next();
                    //while (lines.hasNext()) {
                      //  Line v = lines.next();
                        //If the bin already has a line of the same angle, check it is far enough away from the other lines
                        //if (v.angle == line.angle) {
                          //  matchingAngle = true;
                            //double distance = findDistanceBetweenCentres(v, line);

                            //if (distance < variables.get(R.id.min_distance_between_centres).getVal())
                            //    tooClose = true;
                        //}
                    //}
                    if (v.angle == line.angle) {
                        parallelLines.add(line);
                        matchedAngle = true;
                    }
                }
                //Start a new list if no matching angles were found
                if (!matchedAngle) {
                    ArrayList<Line> lines = new ArrayList<Line>();
                    lines.add(line);
                    bin.add(lines);
                }
            }
        }
        //Only need to return two largest bins
        List<List<Line>> bestBin = getTwoLargestBins(bin);
        return bestBin;
    }

    private double calcDistBetweenPoints(Point a, Point b){
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        double dist = Math.sqrt((dx * dx) + (dy * dy));
        return dist;
    }

    private Point findMidPoint(Point a, Point b){
        double midX = (a.x + b.x)/2;
        double midY = (a.y + b.y)/2;
        return new Point(midX, midY);
    }

    /**
     * Finds the perpendicular distance between each pair of parallel lines
     * If this distance is too small i.e. the lines too close together two be classed as distinct lines
     * It will connect them into a single line
     * The lines must also be reasonably close (distances between centres < threshold)
     * @param pLinesBin
     * @return
     */

    private List<List<Line>> joinCloseLines(List<List<Line>> pLinesBin){
        Iterator<List<Line>> binItr = pLinesBin.iterator();
        while(binItr.hasNext()){
            List<Line> lines = binItr.next();
            Pair<Line, Line> toJoin = findLinesToJoin(lines);
            while(toJoin != null){
                //Join lines together by finding the start and end points that are farthest from the midpoint between the lines
                //Finding the normal at these points and then connecting the mid points of them
                Line v1 = toJoin.first;
                Line v2 = toJoin.second;

                Point midV1 = findMidPoint(v1.start, v1.end);
                Point midV2 = findMidPoint(v1.start, v1.end);
                Point mid = findMidPoint(midV1, midV2);
                double distStartV1ToMid = calcDistBetweenPoints(v1.start, mid);
                double distStartV2ToMid = calcDistBetweenPoints(v2.start, mid);
                double distEndV1ToMid = calcDistBetweenPoints(v1.end, mid);
                double distEndV2ToMid = calcDistBetweenPoints(v2.end, mid);

                //Set farthest vector points
                Point startV1 = distStartV1ToMid > distStartV2ToMid ? v1.start : v2.start;
                Point endV1 = distEndV1ToMid > distEndV2ToMid ? v1.end : v2.end;

                //Find eqns of normals at these points along the line v1
                Pair<Double, Double> eqnNormS = findEqnOfNormal(startV1, v1.m, v1.c);
                Pair<Double, Double> eqnNormE = findEqnOfNormal(endV1, v1.m, v1.c);
                //Find the coords where these lines intersect the other line
                Point startV2 = startV1 == v1.start ? findIntersect(eqnNormS.first, eqnNormS.second, startV1, v2.m, v2.c, v2.start) : findIntersect(eqnNormS.first, eqnNormS.second, startV1, v1.m, v1.c, v1.start);
                Point endV2 = endV1 == v1.end ? findIntersect(eqnNormE.first, eqnNormE.second, endV1, v2.m, v2.c, v2.start) : findIntersect(eqnNormE.first, eqnNormE.second, endV1, v1.m, v1.c, v1.start);

                //With these start and end points, find the midpoints
                Point midStart = findMidPoint(startV1, startV2);
                Point midEnd = findMidPoint(endV1, endV2);

                Line v3 = new Line( midStart, midEnd, v1.angle);
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
    private Pair<Line, Line> findLinesToJoin(List<Line> lines){
        Iterator<Line> linesItr1 = lines.iterator();
        Iterator<Line> linesItr2 = lines.iterator();
        while(linesItr1.hasNext()){
            Line v1 = linesItr1.next();
            while(linesItr2.hasNext()){
                Line v2 = linesItr2.next();
                if(v1 != v2){
                    double pDist = findPerpendicularDistance(v1, v2);
                    if(pDist <= variables.get(R.id.perpendicular_dist_min).getVal())
                        return new Pair<Line, Line>(v1, v2);
                }
            }
        }
        return null;
    }

    private Pair<Double, Double> findEqnOfNormal(Point xy, Double m, Double c){
        if(m == null){
            //Line is vertical
            return new Pair<Double, Double> (0.0, xy.y);
        }else if(m == 0){
            //Line is horizontal
            return new Pair<Double, Double>(null, xy.x);
        }
        double mNorm = -1/m;
        double cNorm = xy.y - (mNorm*xy.x);
        return new Pair<Double, Double>(mNorm, cNorm);

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
    private double findPerpendicularDistance(Line v1, Line v2) {
        //If line is vertical (i.e. m is inf because it is at 90deg to horizontal
        if(Math.abs(v1.angle) == 90 ){
            return Math.abs(v1.start.x-v2.start.x);
        }else if(Math.abs(v1.angle) == 0){
            //If line is horizontal i.e. m = +- 0
            return Math.abs(v1.start.y - v2.start.y);
        }
        Pair<Double, Double> normEq = findEqnOfNormal(v1.start, v1.m, v1.c);
        Point norm2IntersectV2 = findIntersect(v2.m, v2.c, v2.start, normEq.first, normEq.second, v1.start);
        //The perpendicular line has points v1.x1, v1.y1 and the pair of points returned by findIntersect (with v2)
        double dx = norm2IntersectV2.x - v1.start.x;
        double dy = norm2IntersectV2.y - v1.start.y;
        return Math.sqrt((dx * dx) + (dy * dy));
    }

    /**
     * Finds the intersect using the gradient, intersect and a point of each line
     * @param m1
     * @param c1
     * @param a1
     * @param m2
     * @param c2
     * @param a2
     * @return
     */
    private Point findIntersect(Double m1, Double c1, Point a1, Double m2, Double c2, Point a2){
        if(m1 == null){
            //line 1 is vertical
            return new Point(a1.x, c2);
        }else if(m1 == 0){
            //line 1 is horizontal
            return new Point(a2.x, c1);
        }
        Double x = (c2-c1)/(m1-m2);
        Double y = (m1*x) + c1;
        return new Point(x,y);
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
        List<Line> vLines = matToListVector(houghLines);
        Mat dHoughOverlayRaw = drawLines(vLines, downscaled);

        //Place them into bins of parallel lines
        List<List<Line>> parallelLinesBin = findParallelLines(vLines);

        //Try to join all similar lines together
        List<List<Line>> linesAfterJoinging = joinCloseLines(parallelLinesBin);

        //Only draw the line in bins where bin.size() > hough_min_num_parallel_lines
        Mat dHoughOverlayParallel = drawOnlyParallelLines(linesAfterJoinging, downscaled);

        //Find the most likely pairs of lines
        //List<Pair<Line, Line>> pairsOfLines = findPairsOfParallelLines(parallelLinesBin);

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
                break;
            case "hough_lines_overlay":
                toReturn = upscale(dHoughOverlayRaw.clone(), ratio);
                break;
            case "hough_lines_grouped":
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
    private List<List<Line>> getTwoLargestBins(List<List<Line>> bin) {
        Iterator<List<Line>> binItr = bin.iterator();
        List<Line> largestBin = new ArrayList<Line>();
        List<Line> secondLargestBin = new ArrayList<Line>();
        int max = 0;
        int secondMax = 0;
        while(binItr.hasNext()){
            List<Line> list = binItr.next();
            if(list.size() > max){
                max = list.size();
                largestBin = list;
            }else if(list.size() > secondMax){
                secondMax = list.size();
                secondLargestBin = list;
            }
        }
        List<List<Line>> newBin = new ArrayList<List<Line>>();
        newBin.add(largestBin);
        newBin.add(secondLargestBin);
        return newBin;
    }

    @Override
    public SettingsVariable getInitialVar(){
        return variables.get(R.id.bilateral_diameter);
    }
}
