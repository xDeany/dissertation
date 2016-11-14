package andydean.opencvcamera;

/**
 * Created by Andy on 14/11/2016.
 */

public class SettingsVariable {
    private String name;
    private int val;
    private int max;

    public SettingsVariable(String name, int val, int max) {
        this.name = name;
        this.val = val;
        this.max = max;
    }

    public String getName(){
        return name;
    }

    public void setVal(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public int getMax() {
        return max;
    }
}
