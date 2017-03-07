package andydean.opencvcamera;

import android.app.Activity;
import android.content.Context;
import android.util.Pair;
import android.widget.Toast;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.LineSegmentDetector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by Andy on 14/11/2016.
 */

public class HoughLinesDetector extends CubeDetector{

    private Context context;

    public HoughLinesDetector(Context context){

        this.context  = context;

        //Variables List stores the menu settings and their values
        //Strings.xml in the resources folder also needs to be edited when adding/removing settings
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
        variables.put(R.id.perpendicular_dist_min, new SettingsVariable("perpendicular_dist_min", 50, 800));
        variables.put(R.id.perpendicular_dist_increment, new SettingsVariable("perpendicular_dist_increment", 20, 100));
    }

    /**
     * Takes an image and returns a greyscale copy image, does not release the initial image
     * @param image **The image to be converted**
     * @return grayscaleImage **A greyscale COPY of the initial image**
     */
    private Mat toGrayscale(Mat image){
        Mat grayscaleImage = new Mat(image.height(), image.width(), image.type());
        Imgproc.cvtColor(image, grayscaleImage, Imgproc.COLOR_RGB2GRAY);
        return grayscaleImage;
    }

    /**
     *  Takes an image and returns a blurred copy image, does not release the initial image
     * @param image **The image to be converted**
     * @return blurredImage **A blurred COPY of the initial image**
     */
    private Mat toBlur(Mat image){
        Mat blurredImage = new Mat(image.height(), image.width(), image.type());
        Imgproc.bilateralFilter(image, blurredImage, variables.get(R.id.bilateral_diameter).getVal(), variables.get(R.id.bilateral_sigma).getVal(), variables.get(R.id.bilateral_sigma).getVal());
        return blurredImage;
    }

    /**
     *  Takes an image and returns the canny edges of the input, does not release the initial image
     * @param image **The image to be converted**
     * @return cannyImage **A canny edge COPY of the initial image**
     */
    private Mat toCanny(Mat image){
        Mat cannyImage = new Mat(image.height(), image.width(), image.type());
        Imgproc.Canny(image, cannyImage, variables.get(R.id.canny_threshold_1).getVal(), variables.get(R.id.canny_threshold_2).getVal());
        return cannyImage;
    }

    /**
     *  Takes canny edges and returns the result of a houghLinesP transform, does not release the initial image
     * @param cannyEdge **The image to be converted**
     * @return houghLines **The resulting hough lines**
     */
    private Mat toHoughLines(Mat cannyEdge){
        Mat houghLines = new Mat();
        Imgproc.HoughLinesP(cannyEdge, houghLines, variables.get(R.id.hough_rho).getVal(), variables.get(R.id.hough_theta).getVal() * Math.PI/180, variables.get(R.id.hough_threshold).getVal(), variables.get(R.id.hough_min_line_length).getVal(), variables.get(R.id.hough_max_line_gap).getVal());
        return houghLines;
    }
                                                 
    /**
     * Takes a Mat image of lines, and converts it into a linked list of Line (Custom data type) for
     * later analysis, sorting and filtering
     * @param houghLines **Mat image, has to only contain line vectors**
     * @return listLines **The lines all added into a single linked list**
     */
    private List<Line> matToListVector (Mat houghLines){
        List<Line> listLines = new ArrayList<>();
        for (int x = 0; x < houghLines.rows(); x++) {
            double[] vec = houghLines.get(x, 0);
            double  x1 = vec[0],
                    y1 = vec[1],
                    x2 = vec[2],
                    y2 = vec[3];
            Line line = new Line(new Point(x1, y1), new Point(x2, y2), variables.get(R.id.bin_precision).getVal());
            listLines.add(line);
        }
        return listLines;

    }

    /**
     * Creates a clone of an input image and draws the lines on to the clone
     * @param lines **List of Lines that are to be drawn**
     * @param image **Image that they are to be drawn on**
     * @return imageWithLines
     */
    private Mat drawLines(List<Line> lines, Mat image){
        Mat imageWithLines = image.clone();
        for (Line vec : lines)
            Imgproc.line(imageWithLines, vec.start, vec.end, new Scalar(0, 255, 0, 255), 5);

        return imageWithLines;
    }

    /**
     * Creates a clone of an input image and draws the points on to the clone
     * @param points **List of Points to draw**
     * @param image **Image to be drawn on to**
     * @return imageWithPoints
     */
    private Mat drawPoints(List<Point> points, Mat image){
        Mat imageWithPoints = image.clone();
        for(Point p : points)
            Imgproc.circle(imageWithPoints, p, 8, new Scalar(255, 0, 0, 255), 40);

        return imageWithPoints;
    }

    /*
     * Draws the lines connecting valid pairs of parallel lines as found by findPairsOfParallelLines
     * @param pairsOfLines
     * @param image
     * @return image
     */
    /*
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
    */


    /*
     * Pairs together parallel lines where the distance between the centres is ~= length of one of the lines
     * Allows for variation of +- hough_line_separation_error %
     * @param parallelLinesBin
     * @return pairs
     */
    /*
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
                    Point midV1 = Line.findMidPoint(v1.start, v1.end);
                    Point midV2 = Line.findMidPoint(v2.start, v2.end);
                    double separation = Line.calcDistBetweenPoints(midV1, midV2);
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
    }*/

    /*
     * Draw all lines on to image, where they are already grouped into lists of lines with similar gradients
     * @param parallelLinesBin **A List of lists, where each List contains lines of the same gradien**
     * @param image **Image to be drawn on to**
     * @return imageClone **Clone of the initial image with **
     */
    /*
    private Mat drawParallelLines(List<List<Line>> parallelLinesBin, Mat image) {
        Mat imageClone = image.clone();
        for (List<Line> parallelLines : parallelLinesBin) {
            for (Line line : parallelLines)
                Imgproc.line(imageClone, line.start, line.end, new Scalar(0, 255, 0, 255), 5);
        }
        return imageClone;
    }
*/
    /**
     * Group the lines with similar gradients (dependant on hough_angle_precision setting)
     * Also prevents lines from being added to the groups if the centre of the line is too close
     * to any of the other lines (dependant on hough_min_line_separation)
     * @param vLines **Lines that are to be grouped**
     * @return bin **All of the lines in their groups**
     */
    private List<List<Line>> findParallelLines(List<Line> vLines) {
        List<List<Line>> bin = new ArrayList<>();
        //Grab all lines found from houghlines and place them into gradient bins
        for (Line line : vLines) {
            //Add first item to bin
            if (bin.size() == 0) {
                //Start first list
                ArrayList<Line> lines = new ArrayList<>();
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
                    ArrayList<Line> lines = new ArrayList<>();
                    lines.add(line);
                    bin.add(lines);
                }
            }
        }
        return bin;
    }

    /**
     * Finds the perpendicular distance between each pair of parallel lines
     * If this distance is too small i.e. the lines too close together two be classed as distinct lines
     * It will connect them into a single line
     * The lines must also be reasonably close (distances between centres < threshold)
     * @param pLinesBin **Lines grouped by their gradients**
     * @return pLinesBinClone **Lines grouped by their gradients, after lines are grouped if they are close to each other**
     */

    private List<List<Line>> joinCloseLines(List<List<Line>> pLinesBin){
        List<List<Line>> pLinesBinClone = new ArrayList<>();
        for(List<Line> lines : pLinesBin){
            pLinesBinClone.add( new ArrayList<Line>());
        }
        for(int i = 0; i < pLinesBin.size(); i++){
            List<Line> lines = pLinesBin.get(i);
            for(Line l : lines){
                pLinesBinClone.get(i).add(l.clone());
            }
        }
        for (List<Line> lines : pLinesBinClone) {
            Pair<Line, Line> toJoin = findLinesToJoin(lines);
            while (toJoin != null) {
                //Join lines together by finding the start and end points that are farthest from the midpoint between the lines
                //Finding the normal at these points and then connecting the mid points of them
                Line v1 = toJoin.first;
                Line v2 = toJoin.second;

                Point midV1 = Line.findMidPoint(v1.start, v1.end);
                Point midV2 = Line.findMidPoint(v2.start, v2.end);
                Point mid = Line.findMidPoint(midV1, midV2);
                //Calc distance from midpoint of both lines to all points
                double distStartV1ToMid = Line.calcDistBetweenPoints(v1.start, mid);
                double distStartV2ToMid = Line.calcDistBetweenPoints(v2.start, mid);
                double distEndV1ToMid = Line.calcDistBetweenPoints(v1.end, mid);
                double distEndV2ToMid = Line.calcDistBetweenPoints(v2.end, mid);

                //Find most extreme points
                Point startV1 = distStartV1ToMid > distStartV2ToMid ? v1.start : v2.start;
                Point endV1 = distEndV1ToMid > distEndV2ToMid ? v1.end : v2.end;

                //Find eqns of normals at these points along the line v1
                Pair<Double, Double> eqnNormS = Line.findEqnOfNormal(startV1, v1.m);
                Pair<Double, Double> eqnNormE = Line.findEqnOfNormal(endV1, v1.m);

                //Find the coords where these lines intersect v2
                Point startV2 = startV1 == v1.start ? Line.findIntersect(eqnNormS.first, eqnNormS.second, startV1, v2.m, v2.c, v2.start) : Line.findIntersect(eqnNormS.first, eqnNormS.second, startV1, v1.m, v1.c, v1.start);
                Point endV2 = endV1 == v1.end ? Line.findIntersect(eqnNormE.first, eqnNormE.second, endV1, v2.m, v2.c, v2.start) : Line.findIntersect(eqnNormE.first, eqnNormE.second, endV1, v1.m, v1.c, v1.start);

                //With these start and end points, find the midpoints
                Point midStart = Line.findMidPoint(startV1, startV2);
                Point midEnd = Line.findMidPoint(endV1, endV2);

                Line v3 = new Line(midStart, midEnd, v1.angle);
                lines.remove(v1);
                lines.remove(v2);
                lines.add(v3);
                toJoin = findLinesToJoin(lines);
            }
        }
        return pLinesBinClone;
    }

    /**
     * Finds the perpendicular distance between each pair of parallel lines
     * If this distance is too small i.e. the lines too close together two be classed as distinct lines
     * The lines must also be reasonably close (distances between centres < threshold)
     * @param lines **List of lines that are to be grouped, assumed to all be roughly parallel**
     * @return toJoin **Pair of lines that are deemed close enough to be grouped**
     */
    private Pair<Line, Line> findLinesToJoin(List<Line> lines){
        for (Line v1 : lines) {
            for (Line v2 : lines) {
                if (v1 != v2) {
                    double pDist = Line.findPerpendicularDistance(v1, v2);
                    if (pDist <= variables.get(R.id.perpendicular_dist_min).getVal()) {
                        Pair<Line, Line> toJoin = new Pair<>(v1, v2);
                        return toJoin;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Given that the two lines provided are intersecting between their start and end points,
     * The four corners surrounding these lines are found
     * E.g. If the lines given where a cross-hair over the centre of a square, the containing corners
     * would correspond to the corners of the square.
     * @param v1 **First line**
     * @param v2 **Second line**
     * @return allPoints **An arrayList containing the coordinates of the corners around the lines**
     */
    private ArrayList<Point> findContainingCorners(Line v1, Line v2){
        if(!Line.areIntersecting(v1, v2))
            return null;

        //For both start and end points of each line
        //Calc eqn of normal
        //Find intersections between these
        Pair<Double, Double> v1StartNorm = Line.findEqnOfNormal(v1.start, v1.m);
        Pair<Double, Double> v1EndNorm = Line.findEqnOfNormal(v1.end, v1.m);
        Pair<Double, Double> v2StartNorm = Line.findEqnOfNormal(v2.start, v2.m);
        Pair<Double, Double> v2EndNorm = Line.findEqnOfNormal(v2.end, v2.m);

        //Find where these normal lines intersect i.e. the containing corners
        Point v1Sv2S = Line.findIntersect(v1StartNorm.first, v1StartNorm.second, v1.start, v2StartNorm.first, v2StartNorm.second, v2.start);
        Point v1Sv2E = Line.findIntersect(v1StartNorm.first, v1StartNorm.second, v1.start, v2EndNorm.first, v2EndNorm.second, v2.end);
        Point v1Ev2S = Line.findIntersect(v1EndNorm.first, v1EndNorm.second, v1.end, v2StartNorm.first, v2StartNorm.second, v2.start);
        Point v1Ev2E = Line.findIntersect(v1EndNorm.first, v1EndNorm.second, v1.end, v2EndNorm.first, v2EndNorm.second, v2.end);

        //Group all the corners into a containing array
        ArrayList<Point> allPoints = new ArrayList<>(4);
        allPoints.add(v1Sv2S);
        allPoints.add(v1Sv2E);
        allPoints.add(v1Ev2S);
        allPoints.add(v1Ev2E);
        return allPoints;
    }

    /*private Mat downscale(Mat image, int ratio){
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
    }*/

    /*private Mat upscale(Mat image, int ratio){
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
    }*/

    /**
     * Adjust the end point of the vertical line so that the two lines are of equal length
     * @param lines **The pair of lines to adjust**
     * @return pair **The pair of adjusted lines**
     */
    private Pair<Line,Line> adjustVertLine(Pair<Line, Line> lines){
        //Check gradients of lines, if they aren't vert and horizontal then don't adjust
        if(lines.first.m != Double.POSITIVE_INFINITY && lines.second.m != Double.POSITIVE_INFINITY)
            return lines;
        if(lines.first.m != 0 && lines.second.m != 0)
            return lines;


        //Find which line is vertical, and which is horizontal
        Line vertL = lines.first.m == Double.POSITIVE_INFINITY ? lines.first : lines.second;
        Line horzL = lines.first.m == 0 ? lines.first : lines.second;
        Double v1L = vertL.getLength();
        Double v2L = horzL.getLength();
        Double horzLen = horzL.getLength();
        Double vertLen = vertL.getLength();
        vertL.start.y += 1 * (horzLen - vertLen);
        return lines;

    }

    /**
     * Find the two bins that have the most vectors
     * @param bin **Lines that have been grouped into Lists of parallel lines**
     * @return newBin **The two largest groups of parallel lines**
     */
    private List<List<Line>> getTwoLargestBins(List<List<Line>> bin) {
        Iterator<List<Line>> binItr = bin.iterator();
        List<Line> largestBin = new ArrayList<>();
        List<Line> secondLargestBin = new ArrayList<>();
        int max = 0;
        int secondMax = 0;
        while(binItr.hasNext()){
            List<Line> list = binItr.next();
            if(list.size() > max){
                if(max > secondMax){
                    secondMax = max;
                    secondLargestBin = largestBin;
                }
                max = list.size();
                largestBin = list;
            }else if(list.size() > secondMax){
                secondMax = list.size();
                secondLargestBin = list;
            }
        }
        List<List<Line>> newBin = new ArrayList<>();
        newBin.add(largestBin);
        newBin.add(secondLargestBin);
        return newBin;
    }

    /**
     * Abstract function for returning the first variable in the variables array (For the Analysis class)
     * @return The first variable in the list
     */
    @Override
    public SettingsVariable getInitialVar(){
        return variables.get(R.id.bilateral_diameter);
    }

    /**
     * Runs the full Computer Vision process on the given image, returning the specified stage of analysis
     * @param image **The raw image to be analysed**
     * @param imageToReturn **The stage of analysis to be returned (e.g. HoughLines, or detectedCube**
     * @return toReturn **The image corresponding to imageToReturn, returns the inital image if there is an error**
     */
    @Override
    public Mat detectCube(Mat image, String imageToReturn) {
        variables.get(R.id.perpendicular_dist_min).setMax(image.cols()>image.rows()?image.cols():image.rows());
        Mat blankCanvas = new Mat(image.rows(), image.cols(), CvType.CV_8UC4, new Scalar(0,0,0,255));

        Mat grayscaleImage = toGrayscale(image);
        Mat blurredImage = toBlur(grayscaleImage);
        Mat onlyCanny = toCanny(blurredImage);
        Mat houghLines = toHoughLines(onlyCanny);
        //Convert Mat of the houghlines (not drawable) to list of lines
        List<Line> listHoughLines = matToListVector(houghLines);
        Mat onlyHoughLines = drawLines(listHoughLines, blankCanvas);
        Mat overlayHoughLines = drawLines(listHoughLines, image);

        //Place them into bins of parallel lines
        List<List<Line>> parallelLinesBin = findParallelLines(listHoughLines);

        //Select the two largest bins
        List<List<Line>> bestParallelLines = getTwoLargestBins(parallelLinesBin);

        //Draw the best parallel lines
        Mat overlayBestParallelLines = drawLines(Line.foldList(bestParallelLines), image);

        //Try to join all similar lines together
        List<List<Line>> linesAfterJoining= joinCloseLines(bestParallelLines);

        boolean cornersFound = false;
        ArrayList<Point> corners = new ArrayList<>(4);

        //Keep grouping lines until left with a single pair of intersecting lines
        do {
            linesAfterJoining = joinCloseLines(bestParallelLines);

            ArrayList<Pair<Line, Line>> intersectingLines = Line.findAllIntersectingLines(Line.foldList(linesAfterJoining));
            if (intersectingLines.size() == 2) {

                //Shorten length of longest line to the length of the shorter one
                //Decrease the largest y coords
                adjustVertLine(intersectingLines.get(0));
                corners = findContainingCorners(intersectingLines.get(0).first, intersectingLines.get(0).second);
                //if(corners != null)
                    //Toast.makeText(context, corners.toString(), Toast.LENGTH_LONG).show();
                cornersFound = true;
            } else {
                //int before = variables.get(R.id.perpendicular_dist_min).getVal();
                variables.get(R.id.perpendicular_dist_min).adjustVal(variables.get(R.id.perpendicular_dist_increment).getVal());
                //int after = variables.get(R.id.perpendicular_dist_min).getVal();

            }
        }while(!cornersFound && variables.get(R.id.perpendicular_dist_min).getVal() <= (variables.get(R.id.perpendicular_dist_min).getMax() - variables.get(R.id.perpendicular_dist_increment).getVal()));


        //Draw grouped lines
        Mat overlayGrouped = drawLines(Line.foldList(linesAfterJoining), image);
        Mat onlyGrouped = drawLines(Line.foldList(linesAfterJoining), blankCanvas);

        Mat onlyCorners = cornersFound ?
                drawPoints(corners, blankCanvas) :
                image.clone();

        Mat overlayCorners = cornersFound ?
                drawPoints(corners, image) :
                image.clone();

        Mat onlyCornersAndLines = cornersFound ?
                drawLines(Line.foldList(linesAfterJoining), onlyCorners) :
                image.clone();

        Mat overlayCornersAndLines = cornersFound ?
                drawLines(Line.foldList(linesAfterJoining), overlayCorners) :
                image.clone();

        Mat toReturn;

        switch(imageToReturn){
            case "original_image":
                toReturn = image.clone();
                break;
            case "grayscale_image":
                toReturn = grayscaleImage.clone();
                break;
            case "blurred_image":
                toReturn = blurredImage.clone();
                break;
            case "canny_edges":
                toReturn = onlyCanny.clone();
                break;
            case "hough_lines_only":
                toReturn = overlayHoughLines.clone();
                break;
            case "hough_lines_overlay":
                toReturn = overlayHoughLines.clone();
                break;
            case "best_parallel_lines_overlay":
                toReturn = overlayBestParallelLines.clone();
                break;
            case "grouped_only":
                toReturn = onlyGrouped.clone();
                break;
            case "grouped_overlay":
                toReturn = overlayGrouped.clone();
                break;
            case "corners_only":
                toReturn = onlyCorners.clone();
                break;
            case "corners_overlay":
                toReturn = overlayCorners.clone();
                break;
            case "corners_and_lines_only":
                toReturn = onlyCornersAndLines.clone();
                break;
            case "corners_and_lines_overlay":
                toReturn = overlayCornersAndLines.clone();
                break;
            default:
                toReturn = blankCanvas.clone();
                break;
        }

        grayscaleImage.release();
        blurredImage.release();
        onlyCanny.release();
        houghLines.release();
        onlyHoughLines.release();
        overlayHoughLines.release();
        overlayBestParallelLines.release();
        onlyGrouped.release();
        overlayGrouped.release();
        onlyCorners.release();
        overlayCorners.release();
        onlyCornersAndLines.release();
        overlayCornersAndLines.release();

        return toReturn;
    }

    /**
     * Attempts to detect the cube, returning the location of the corners (if they are found)
     * @param image ** The image to be analysed **
     * @return corners **The corners of the cube the detector finds **
     */
    public ArrayList<Point> testDetectCube(Mat image){
        variables.get(R.id.perpendicular_dist_min).setMax(image.cols()>image.rows()?image.cols():image.rows());

        Mat grayscaleImage = toGrayscale(image);
        Mat blurredImage = toBlur(grayscaleImage);
        Mat onlyCanny = toCanny(blurredImage);
        Mat houghLines = toHoughLines(onlyCanny);

        //Convert Mat of the houghlines (not drawable) to list of lines
        List<Line> listHoughLines = matToListVector(houghLines);

        //Place them into bins of parallel lines
        List<List<Line>> parallelLinesBin = findParallelLines(listHoughLines);

        //Select the two largest bins
        List<List<Line>> bestParallelLines = getTwoLargestBins(parallelLinesBin);

        //Try to join all similar lines together
        List<List<Line>> linesAfterJoining = joinCloseLines(bestParallelLines);

        boolean cornersFound = false;
        ArrayList<Point> corners = new ArrayList<>(4);
        int before = 0;
        int after = 0;
        //Keep grouping lines until left with a single pair of intersecting lines
        do {
            linesAfterJoining = joinCloseLines(bestParallelLines);

            ArrayList<Pair<Line, Line>> intersectingLines = Line.findAllIntersectingLines(Line.foldList(linesAfterJoining));
            if (intersectingLines.size() == 2) {
                adjustVertLine(intersectingLines.get(0));
                corners = findContainingCorners(intersectingLines.get(0).first, intersectingLines.get(0).second);
                //Toast.makeText(context, corners.toString(), Toast.LENGTH_LONG).show();
                cornersFound = true;
            } else {
                before = variables.get(R.id.perpendicular_dist_min).getVal();
                variables.get(R.id.perpendicular_dist_min).adjustVal(variables.get(R.id.perpendicular_dist_increment).getVal());
                after = variables.get(R.id.perpendicular_dist_min).getVal();

            }
        }while(!cornersFound && variables.get(R.id.perpendicular_dist_min).getVal() <= (variables.get(R.id.perpendicular_dist_min).getMax() - variables.get(R.id.perpendicular_dist_increment).getVal()));



        grayscaleImage.release();
        blurredImage.release();
        onlyCanny.release();
        houghLines.release();
        before = after;

        return corners;
    }
}
