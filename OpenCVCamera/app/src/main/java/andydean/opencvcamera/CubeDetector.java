package andydean.opencvcamera;

import org.opencv.core.Mat;

/**
 * Created by Andy on 14/11/2016.
 */

public abstract class CubeDetector implements CubeDetectorInterface {
    @Override
    public SettingsVariable getVariable(Integer varName) {
        if(variables.containsKey(varName))
            return variables.get(varName);
        return null;
    }

    @Override
    public SettingsVariable getInitialVar(){
        return variables.get(variables.values().toArray()[0]);
    }
}
