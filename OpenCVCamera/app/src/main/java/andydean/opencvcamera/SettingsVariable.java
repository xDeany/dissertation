package andydean.opencvcamera;
@SuppressWarnings("WeakerAccess")

/**
 * Created by Andy on 14/11/2016.
 */

public class SettingsVariable {
    private String name;
    private int val;
    private int initialVal;
    private int max;

    public SettingsVariable(String name, int val, int max) {
        this.name = name;
        this.val = val;
        this.initialVal = val;
        this.max = max;
    }

    public String getName(){
        return name;
    }

    public void setVal(int val) {
        this.val = val;
    }

    public void adjustVal(int incr) { this.val += incr;}

    public int getVal() {
        return val;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {this.max = max;}

    public int getInitialVal() { return initialVal;}
}
