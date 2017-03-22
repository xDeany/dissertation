package andydean.opencvcamera;

import android.util.Pair;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Andy on 14/11/2016.
 */

public interface CubeDetectorInterface {
    HashMap<Integer, SettingsVariable> variables = new HashMap<Integer, SettingsVariable>();

    public SettingsVariable getVariable(Integer varId);

    public SettingsVariable getInitialVar();

    public Mat detectCubeImageDebug(Mat image, String imageToReturn);

    public List<Point> detectCubeLocation(Mat image);

    public List<Pair<Point, Character>> detectCubeColour(Mat image, List<Point> corners);

}
