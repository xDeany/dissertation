package andydean.opencvcamera;

import android.util.Pair;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;

/**
 * Created by Andy on 13/03/2017.
 */

public class ColourDetector {
    public static ArrayList<Pair<Point, Character>> detectColour(Mat rgbImg, int[] corners){
        ArrayList<Pair<Character, Double[]>> rgbHistograms = new ArrayList<>();
        rgbHistograms.add(new Pair<>('R', new Double[]{171.9473684, 5.109695, 43.029824561, 8.155038, 42.1345, 8.234042}));
        rgbHistograms.add(new Pair<>('O', new Double[]{254.994152, 0.076471911, 91.60233918, 7.467954702, 33.06432749, 10.0821019}));
        rgbHistograms.add(new Pair<>('Y', new Double[]{237.1403509, 6.392381301, 246.497076, 5.903512682, 35.14619883, 9.729683215}));
        rgbHistograms.add(new Pair<>('G', new Double[]{2.964912281, 7.601002052, 188.5614035, 6.440727609, 35.92397661, 10.60078013}));
        rgbHistograms.add(new Pair<>('B', new Double[]{22.77777778, 7.801038979, 50.99415205, 8.597877355, 102.4853801, 9.837118615}));
        rgbHistograms.add(new Pair<>('W', new Double[]{212.6900585, 5.502177697, 216.2748538, 5.865192375, 214.8947368, 5.801675855}));
        
        ArrayList<Point> squareCentres = getSquareCentres(corners);
        ArrayList<Pair<Point, Double[]>> rgbVals = getRGBRaw(squareCentres, rgbImg);
        ArrayList<Pair<Point, Double[]>> rgbValsAdjusted = adjustForWhiteBallance(rgbVals, rgbImg);
        ArrayList<Pair<Point, Character>>colours = getColourLabels(rgbValsAdjusted, rgbHistograms);

        return colours;
    }


}
