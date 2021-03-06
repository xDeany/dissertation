package andydean.opencvcamera;

import android.util.Pair;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Andy on 13/03/2017.
 */

public class ColourDetector {


    /**
     * The enclosing function for colour detection. It finds the points to check from the corners of
     * the cube. Once found, it reads the rgb values and adjusts the white ballance before finally
     * calculating which colour it is most likely to be
     * @param rgbaImg ** The raw image in bgra colour format **
     * @param corners ** The detected corners of the cube **
     * @param windowSize ** The size of the window to check around the cube **
     * @return colours ** The points detected with their corresponding colour values **
     */
    public static List<Pair<Point, Character>> detectColour(Mat rgbaImg, List<Point> corners, int windowSize){
        double[] rgbWhiteVals = {170, 170, 170};
        ArrayList<Pair<Character, NormalDistribution[]>> rgbDistributions = new ArrayList<>();

        rgbDistributions.add(new Pair<>('R', new NormalDistribution[]{new NormalDistribution(146.12, 4.33), new NormalDistribution(36.78, 6.92), new NormalDistribution(35.85, 7.02)}));
        rgbDistributions.add(new Pair<>('O', new NormalDistribution[]{new NormalDistribution(235.80, 6.87), new NormalDistribution(77.85, 6.31), new NormalDistribution(28.09, 8.60)}));
        rgbDistributions.add(new Pair<>('Y', new NormalDistribution[]{new NormalDistribution(201.55, 5.44), new NormalDistribution(210.01, 5.91), new NormalDistribution(29.93, 8.22)}));
        rgbDistributions.add(new Pair<>('G', new NormalDistribution[]{new NormalDistribution(2.50, 6.49), new NormalDistribution(160.27, 5.50), new NormalDistribution(30.54, 9.03)}));
        rgbDistributions.add(new Pair<>('B', new NormalDistribution[]{new NormalDistribution(19.33, 6.63), new NormalDistribution(43.35, 7.31), new NormalDistribution(87.13, 8.33)}));
        rgbDistributions.add(new Pair<>('W', new NormalDistribution[]{new NormalDistribution(180.86, 4.67), new NormalDistribution(183.87, 4.94), new NormalDistribution(182.62, 4.87)}));
        
        List<Point> squareCentres = getSquareCentres(corners);
        List<Pair<Point, Double[]>> rgbVals = getRGBRaw(squareCentres, rgbaImg, windowSize);
        double[] whiteBalRatio = obtainWhiteBallanceRatio(rgbaImg, rgbWhiteVals);
        List<Pair<Point, Double[]>> rgbValsAdjusted = adjustForWhiteBallance(rgbVals, whiteBalRatio);
        List<Pair<Point, Character>> colours = getColourLabels(rgbValsAdjusted, rgbDistributions);

        return colours;
    }

    /**
     * Checks if a and b refer to opposite colours on the cube
     * @param a **Colour a**
     * @param b **Colour b**
     * @return isOpp(a,b)
     */
    public static boolean isOpposite(char a, char b){
        switch (a){
            case 'R':
                return b == 'O';
            case 'O':
                return b == 'R';
            case 'Y':
                return b == 'W';
            case 'G':
                return b == 'B';
            case 'B':
                return b == 'G';
            case 'W':
                return b == 'Y';
            default:
                return false;
        }
    }

    /**
     * Takes a colour and returns the next colour
     * @param c **Initial colour**
     * @return colour++
     */
    public static char nextColour(char c){
        int i = getColourInt(c);
        i = (i+1)%6;
        return getColourChar(i);
    }

    /**
     * Helper function for translating the sticker colours from char to int
     * @param c ** Character value of colour **
     * @return i ** Int value of colour **
     */
    public static int getColourInt(char c){
        switch (c){
            case 'R':
                return 0;
            case 'O':
                return 1;
            case 'Y':
                return 2;
            case 'G':
                return 3;
            case 'B':
                return 4;
            case 'W':
                return 5;
            default:
                return 6;
        }
    }

    /**
     * Helper function for translating the sticker colours from int to char
     * @param i ** Int value of colour **
     * @return c ** Char value of colour **
     */
    public static char getColourChar(int i){
        switch(i){
            case 0:
                return 'R';
            case 1:
                return 'O';
            case 2:
                return 'Y';
            case 3:
                return 'G';
            case 4:
                return 'B';
            case 5:
                return 'W';
            default:
                return 'X';
        }
    }

    /**
     * Translates the char of a colour to the appropriate rgb value
     * @param c ** Character value of colour **
     * @return rgbVals ** The rgb Values for that colour **
     */
    public static double[] getRGB(char c){
        double[] rgbVals = new double[3];
        switch(c){
            case 'R':
                rgbVals[0] = 200;
                rgbVals[1] = 0;
                rgbVals[2] = 0;
                break;
            case 'O':
                rgbVals[0] = 228;
                rgbVals[1] = 114;
                rgbVals[2] = 0;
                break;
            case 'Y':
                rgbVals[0] = 255;
                rgbVals[1] = 255;
                rgbVals[2] = 0;
                break;
            case 'G':
                rgbVals[0] = 0;
                rgbVals[1] = 125;
                rgbVals[2] = 0;
                break;
            case 'B':
                rgbVals[0] = 0;
                rgbVals[1] = 0;
                rgbVals[2] = 255;
                break;
            case 'W':
                rgbVals[0] = 255;
                rgbVals[1] = 255;
                rgbVals[2] = 255;
                break;
            case 'X':
                rgbVals[0] = 0;
                rgbVals[1] = 0;
                rgbVals[2] = 0;
                break;
        }
        return rgbVals;
    }

    /**
     * From the adjusted rgb values of the points selected, it checks them against the histograms
     * for all 6 colour values and choses the most likely colour it is
     * @param rgbValsAdjusted ** The adjusted rgb point values **
     * @param rgbHistograms ** The histograms to run the colours against **
     * @return labels ** A list of point-colour pairs **
     */
    private static List<Pair<Point,Character>> getColourLabels(List<Pair<Point, Double[]>> rgbValsAdjusted, ArrayList<Pair<Character, NormalDistribution[]>> rgbHistograms) {
        List<Pair<Point,Character>> labels = new ArrayList<>();
        for (Pair<Point,Double[]> point : rgbValsAdjusted){
            Character mostLikelyColour = 'X';
            double colourDensityProbability = 0;
            for(Pair<Character, NormalDistribution[]> colHistograms : rgbHistograms){
                double sumDensity = 0;
                for(int i = 0 ; i < 3; i++){
                    NormalDistribution dist = colHistograms.second[i];
                    sumDensity += dist.density(point.second[i]);
                }
                if(sumDensity > colourDensityProbability){
                    mostLikelyColour = colHistograms.first;
                    colourDensityProbability = sumDensity;
                }
            }
            labels.add(new Pair<>(point.first, mostLikelyColour));
        }
        return labels;
    }

    /**
     * Multiply each detected pixel by the white ballance ratio (more efficient than adjust the entire image)
     * @param rgbVals ** Values to adjust **
     * @param whiteBalRatio ** The ratio to adjust them by **
     * @return rgbVals ** Adjusted values **
     */
    private static List<Pair<Point,Double[]>> adjustForWhiteBallance(List<Pair<Point, Double[]>> rgbVals, double[] whiteBalRatio) {
        for (Pair<Point, Double[]> point : rgbVals) {
            point.second[0] = point.second[0] * whiteBalRatio[0];
            point.second[1] = point.second[1] * whiteBalRatio[1];
            point.second[2] = point.second[2] * whiteBalRatio[2];
        }
        return rgbVals;
    }

    /**
     * From the image, take a sample of a 5x5 window of top left pixels as white (assuming on paper background)
     * Use those pixels to find ratio to adjust all pixels by (white ballance ratio)
     * @param rgbaImg ** Img to adjust **
     * @param rgbWhiteVals ** The value that white should be in the top left **s
     * @return rgbRatio ** The ratio that all pixels should be multiplied by **
     */
    private static double[] obtainWhiteBallanceRatio(Mat rgbaImg, double[] rgbWhiteVals){
        //Obtain sample of upper-left pixels
        double[] rgbaTotal = {0,0,0,0};
        int totalCount = 0;
        for(int i = 1; i < 5; i++){
            for(int j = 1; j < 5; j++){
                double[] rgba = rgbaImg.get(j, i);
                rgbaTotal[0] += rgba[0];
                rgbaTotal[1] += rgba[1];
                rgbaTotal[2] += rgba[2];
                rgbaTotal[3] += rgba[3];
                totalCount++;
            }
        }
        //Average the rgb values
        double[] rgbWhite = {0,0,0};
        rgbWhite[0] = rgbaTotal[0] / totalCount;
        rgbWhite[1] = rgbaTotal[1] / totalCount;
        rgbWhite[2] = rgbaTotal[2] / totalCount;

        //Find ratio for white ballance
        double[] rgbRatio = {0,0,0};
        rgbRatio[0] = rgbWhiteVals[0] / rgbWhite[0];
        rgbRatio[1] = rgbWhiteVals[1] / rgbWhite[1];
        rgbRatio[2] = rgbWhiteVals[2] / rgbWhite[2];

        return rgbRatio;
    }

    /**
     * From the detected points, it averages the rgb values of the pixels surrounding it
     * @param squareCentres ** The pixel locations to be analysed **
     * @param rgbaImg ** The image to read from **
     * @return rgbValus ** The average rgb value for each point
     */
    private static List<Pair<Point,Double[]>> getRGBRaw(List<Point> squareCentres, Mat rgbaImg, int windowSize) {
        Iterator<Point> coordsItr = squareCentres.iterator();
        List<Pair<Point, Double[]>> rgbValues = new ArrayList<>();
        while(coordsItr.hasNext()){
            Point coord = coordsItr.next();
            double[] rgbaTotal = {0,0,0,0};
            int totalCount = 0;
            for(int i = -1 * windowSize; i < windowSize; i++){
                for(int j = -1 * windowSize; j < windowSize; j++){
                    int x = (int)coord.x + i;
                    int y = (int)coord.y + j;
                    if(x >= 0 && y >=0) {
                        double[] rgba = rgbaImg.get(y, x);
                        rgbaTotal[0] += rgba[0];
                        rgbaTotal[1] += rgba[1];
                        rgbaTotal[2] += rgba[2];
                        rgbaTotal[3] += rgba[3];
                        totalCount++;
                    }
                }
            }
            Double[] rgb = new Double[3];
            rgb[0] = rgbaTotal[0] / totalCount;
            rgb[1] = rgbaTotal[1] / totalCount;
            rgb[2] = rgbaTotal[2] / totalCount;
            rgbValues.add(new Pair<>(coord,rgb));
        }
        return rgbValues;
    }

    /**
     * Assuming the corners are given in a clockwise order, calculate where the stickers should be
     * @param corners ** Corners to be analysed **
     * @return centres ** The locations of the centres of the stickers **
     */
    private static List<Point> getSquareCentres(List<Point> corners) {
        //a              b
        //   p1  p2  p3
        //   p8  p9  p4
        //d  p7  p6  p5  c

        Point a = corners.get(0);
        Point b = corners.get(1);
        Point c = corners.get(2);
        Point d = corners.get(3);
        //Pair<Double, Double> vecA = new Pair<>((c.x - a.x)/6, (c.y - a.y)/6);
        //Pair<Double, Double> vecB = new Pair<>((d.x - b.x)/6, (d.y - b.y)/6);
        Line ac = new Line(a, c, 1);
        Line bd = new Line(b, d, 1);
        Point p9 = Line.findIntersect(ac.m, ac.c, a, bd.m, bd.c, b);
        Point p1 = new Point(((p9.x - a.x) / 3) + a.x, ((p9.y - a.y)/3) + a.y);
        Point p3 = new Point(((p9.x - b.x) / 3) + b.x, ((p9.y - b.y)/3) + b.y);
        Point p5 = new Point(((p9.x - c.x) / 3) + c.x, ((p9.y - c.y)/3) + c.y);
        Point p7 = new Point(((p9.x - d.x) / 3) + d.x, ((p9.y - d.y)/3) + d.y);

        Point p2 = new Point(((p3.x - p1.x)/2)+p1.x, ((p3.y - p1.y)/2)+p1.y);
        Point p4 = new Point(((p5.x - p3.x)/2)+p3.x, ((p5.y - p3.y)/2)+p3.y);
        Point p6 = new Point(((p7.x - p5.x)/2)+p5.x, ((p7.y - p5.y)/2)+p5.y);
        Point p8 = new Point(((p1.x - p7.x)/2)+p7.x, ((p1.y - p7.y)/2)+p7.y);

        List<Point> centres = new ArrayList<>(Arrays.asList(p1,p2,p3,p4,p5,p6,p7,p8,p9));
        return centres;
    }
}
