package andydean.opencvcamera;

import org.opencv.core.Mat;

import java.util.HashMap;

/**
 * Created by Andy on 14/11/2016.
 */

public interface CubeDetectorInterface {
    HashMap<Integer, SettingsVariable> variables = new HashMap<Integer, SettingsVariable>();

    public SettingsVariable getVariable(Integer varId);

    public SettingsVariable getInitialVar();

    public Mat detectCube(Mat image, String imageToReturn);
}
