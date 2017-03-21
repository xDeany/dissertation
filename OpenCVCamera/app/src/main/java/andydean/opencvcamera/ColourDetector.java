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
     * From the corners supplied, it finds the ones that are opposite and then uses ther locations
     * to infer the coordinates of the sticker centres
     * @param corners ** Corners to be analysed **
     * @return centres ** The locations of the centres of the stickers **
     */
    private static List<Point> getSquareCentres(List<Point> corners) {
        List<Pair<Point, Point>> diagonals = getDiagonals(corners);
        Point a = diagonals.get(0).first;
        Point b = diagonals.get(0).second;
        Point c = diagonals.get(1).first;
        Point d = diagonals.get(1).second;
        //Going a -> b and c -> d
        Pair<Double, Double> vecA = new Pair<>((b.x - a.x)/6, (b.y - a.y)/6);
        Pair<Double, Double> vecB = new Pair<>((d.x - c.x)/6, (d.y - c.y)/6);
        //
        // p1  p2  p3
        // p4  p5  p6
        // p7  p8  p9
        // a -> b
        Point p1 = new Point(a.x + vecA.first, a.y + vecA.second);
        Point p5 = new Point(p1.x + vecA.first + vecA.first, p1.y + vecA.second + vecA.second);
        Point p9 = new Point(p5.x + vecA.first + vecA.first, p5.y + vecA.second + vecA.second);

        // c -> d
        Point p3 = new Point(c.x + vecB.first, c.y + vecB.second);
        p5 = new Point(p3.x + vecB.first + vecB.first, p3.y + vecB.second + vecB.second);
        Point p7 = new Point(p5.x + vecB.first + vecB.first, p5.y + vecB.second + vecB.second);

        Point p2 = new Point(((p3.x - p1.x)/2)+p1.x, ((p3.y - p1.y)/2)+p1.y);
        Point p4 = new Point(((p7.x - p1.x)/2)+p1.x, ((p7.y - p1.y)/2)+p1.y);
        Point p6 = new Point(((p3.x - p9.x)/2)+p9.x, ((p3.y - p9.y)/2)+p9.y);
        Point p8 = new Point(((p7.x - p9.x)/2)+p9.x, ((p7.y - p9.y)/2)+p9.y);
        List<Point> centres = new ArrayList<>(Arrays.asList(p1,p2,p3,p4,p5,p6,p7,p8,p9));
        return centres;
    }

    /**
     * Takes the corners in any order, and finds the pair of corners that are furthest apart
     * Idea is so that it creates a diagonal over the cube
     * @param corners ** Corners to be analysed **
     * @return bothDiags ** An arraylist containing both pairs of corners **
     */
    private static List<Pair<Point,Point>> getDiagonals(List<Point> corners){
        Point a = corners.get(0);
        Point b = corners.get(1);
        Point c = corners.get(2);
        Point d = corners.get(3);
        //Take each pair of corners
        List<Pair<Pair<Point, Point>, Double>> distances = new ArrayList<>();
        distances.add(new Pair<>(new Pair<>(a, b), Line.calcDistBetween(a, b))); //ab
        distances.add(new Pair<>(new Pair<>(a, c), Line.calcDistBetween(a, c))); //ac
        distances.add(new Pair<>(new Pair<>(a, d), Line.calcDistBetween(a, d))); //ad
        distances.add(new Pair<>(new Pair<>(b, c), Line.calcDistBetween(b, c))); //bc
        distances.add(new Pair<>(new Pair<>(b, d), Line.calcDistBetween(b, d))); //bd
        distances.add(new Pair<>(new Pair<>(c, d), Line.calcDistBetween(c, d))); //cd

        Pair<Point, Point> diagA = new Pair<>(null, null);
        Pair<Point, Point> diagB;
        //Find the two pairs farthest apart
        double max = 0;
        int pair = 0;
        for(int i = 0; i<6; i++){
            if(distances.get(i).second > max){
                diagA = distances.get(i).first;
                max = distances.get(i).second;
                pair = i;
            }
        }
        diagB = distances.get(5-pair).first;

        List<Pair<Point, Point>> bothDiags = new ArrayList<>();
        bothDiags.add(diagA);
        bothDiags.add(diagB);

        return bothDiags;
    }

}
