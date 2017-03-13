package andydean.opencvcamera;

import android.util.Pair;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Andy on 13/03/2017.
 */

public class ColourDetector {
    /*
    public static ArrayList<Pair<Point, Character>> detectColour(Mat rgbImg, ArrayList<Point> corners){
        ArrayList<Pair<Character, Double[]>> rgbHistograms = new ArrayList<>();
        rgbHistograms.add(new Pair<>('R', new Double[]{171.9473684, 5.109695, 43.029824561, 8.155038, 42.1345, 8.234042}));
        rgbHistograms.add(new Pair<>('O', new Double[]{254.994152, 0.076471911, 91.60233918, 7.467954702, 33.06432749, 10.0821019}));
        rgbHistograms.add(new Pair<>('Y', new Double[]{237.1403509, 6.392381301, 246.497076, 5.903512682, 35.14619883, 9.729683215}));
        rgbHistograms.add(new Pair<>('G', new Double[]{2.964912281, 7.601002052, 188.5614035, 6.440727609, 35.92397661, 10.60078013}));
        rgbHistograms.add(new Pair<>('B', new Double[]{22.77777778, 7.801038979, 50.99415205, 8.597877355, 102.4853801, 9.837118615}));
        rgbHistograms.add(new Pair<>('W', new Double[]{212.6900585, 5.502177697, 216.2748538, 5.865192375, 214.8947368, 5.801675855}));
        
        List<Point> squareCentres = getSquareCentres(corners);
        //List<Pair<Point, Double[]>> rgbVals = getRGBRaw(squareCentres, rgbImg);
        //List<Pair<Point, Double[]>> rgbValsAdjusted = adjustForWhiteBallance(rgbVals, rgbImg);
        //List<Pair<Point, Character>>colours = getColourLabels(rgbValsAdjusted, rgbHistograms);

        return colours;
    }*/

    public static List<Point> getSquareCentres(ArrayList<Point> corners) {
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
    private static List<Pair<Point,Point>> getDiagonals(ArrayList<Point> corners){
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
        Pair<Point, Point> diagB = new Pair<>(null, null);
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
