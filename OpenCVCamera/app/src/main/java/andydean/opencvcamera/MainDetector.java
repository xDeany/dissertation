package andydean.opencvcamera;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.media.tv.TvContract;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class MainDetector extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private static String TAG = "MainDetector";

    private static final String VIDEO_STATE = "video_state";
    private static final String CAPTURE_STATE = "capture_state";
    private static final String ERROR_CHECK_STATE = "error_check_state";

    private String state = VIDEO_STATE;
    JavaCameraView javaCameraView;
    Mat capturedFrame, drawnFrame, capturedFrameWithPixels;
    FloatingActionButton captureCubeButton, validDetectionButton;
    CubeDetector houghDetector;
    long time = System.currentTimeMillis();
    List<List<Pair<Point, Character>>> captured;
    List<Pair<Point,Character>> averagedAndLocation;
    List<View> lastSideStored;
    boolean colourChanged = false;

    BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch(status){
                case BaseLoaderCallback.SUCCESS:
                    javaCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }

        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_detector);
        captureCubeButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        validDetectionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton2);

        javaCameraView = (JavaCameraView) findViewById(R.id.java_camera_view_main);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCameraIndex(0);
        javaCameraView.setCvCameraViewListener(this);

        houghDetector = new HoughLinesDetector(this);

        captured = new ArrayList<>();

        lastSideStored = new ArrayList<>();
        lastSideStored.add(0,(View) findViewById(R.id.cell1));
        lastSideStored.add(1,(View) findViewById(R.id.cell2));
        lastSideStored.add(2,(View) findViewById(R.id.cell3));
        lastSideStored.add(3,(View) findViewById(R.id.cell4));
        lastSideStored.add(4,(View) findViewById(R.id.cell5));
        lastSideStored.add(5,(View) findViewById(R.id.cell6));
        lastSideStored.add(6,(View) findViewById(R.id.cell7));
        lastSideStored.add(7,(View) findViewById(R.id.cell8));
        lastSideStored.add(8,(View) findViewById(R.id.cell9));

        captureCubeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!state.equals(ERROR_CHECK_STATE)){
                    state = CAPTURE_STATE;
                    time = System.currentTimeMillis();
                }
            }
        });

        validDetectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state.equals(ERROR_CHECK_STATE)) {
                    capturedFrame.release();
                    capturedFrameWithPixels.release();
                    state = VIDEO_STATE;
                    for(int i = 0; i < 9 ; i++){
                        Character c = averagedAndLocation.get(i).second;
                        View iv = lastSideStored.get(i);
                        double[] rgbVals = ColourDetector.getRGB(c);
                        iv.setBackgroundColor(Color.rgb((int) rgbVals[0], (int) rgbVals[1], (int) rgbVals[2]));
                    }
                }
            }
        });

        javaCameraView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                colourChanged = true;
                if(state.equals(ERROR_CHECK_STATE)){
                    RelativeLayout videoFrame = (RelativeLayout) findViewById(R.id.videoLayout);
                    int[] windowLocation = new int[2];
                    javaCameraView.getLocationOnScreen(windowLocation);
                    int eX = (int) event.getX();
                    int eY = (int) event.getY();
                    int x = eX - 150;
                    int y = eY - 100;
                    Point touchLocation = new Point(x,y);
                    double minDist = Double.POSITIVE_INFINITY;
                    int pairNum = -1;
                    for(int i = 0; i < 9; i++){
                        Pair<Point, Character> pc = averagedAndLocation.get(i);
                        double dist = Line.calcDistBetween(pc.first, touchLocation);
                        if(dist < minDist){
                            minDist = dist;
                            pairNum = i;
                        }
                    }
                    Pair<Point, Character> pc = averagedAndLocation.get(pairNum);
                    int i = ColourDetector.getColourInt(pc.second);
                    i = (i+1)%6;
                    Character newC = ColourDetector.getColourChar(i);
                    Pair<Point, Character> newPC = new Pair<>(pc.first, newC);
                    averagedAndLocation.set(pairNum, newPC);

                }
                return false;
            }
        });
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        drawnFrame = new Mat(height, width, CvType.CV_8SC4);
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        drawnFrame = inputFrame.rgba();

        if(state.equals(CAPTURE_STATE) && System.currentTimeMillis() < time + 3000) {
            List<Point> corners = houghDetector.detectCubeLocation(drawnFrame);
            List<Pair<Point, Character>> colours;
            if (!corners.isEmpty()) {
                colours = houghDetector.detectCubeColour(drawnFrame, corners);

                if (colours != null) {
                    for(Pair<Point, Character> p : colours) {
                        double[] rgbVals = ColourDetector.getRGB(p.second);
                        Imgproc.circle(drawnFrame, p.first, 8, new Scalar(rgbVals[0], rgbVals[1], rgbVals[2], 255), 40);
                    }
                    captured.add(colours);
                }
            }
        }else {
            if(!captured.isEmpty()){

                capturedFrame = drawnFrame.clone();
                capturedFrameWithPixels = drawnFrame.clone();

                List<Character> averaged = averageColours(captured);
                averagedAndLocation = new ArrayList<>();
                for(int i = 0; i < 9; i++){
                    Point p = captured.get(captured.size()-1).get(i).first;
                    Character c = averaged.get(i);
                    averagedAndLocation.add(new Pair<>(p,c));
                }
                captured = new ArrayList<>();
                state = ERROR_CHECK_STATE;
                colourChanged = true;
            }
        }

        if(colourChanged) {
            capturedFrameWithPixels.release();
            capturedFrameWithPixels = HoughLinesDetector.drawPointsColour(averagedAndLocation, capturedFrame);
            colourChanged = false;
            return capturedFrameWithPixels;
        }else if(state.equals(ERROR_CHECK_STATE))
            return capturedFrameWithPixels;

        return drawnFrame;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        drawnFrame.release();
        if(javaCameraView!=null)
            javaCameraView.disableView();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG,"OpenCV failed to load");
            mLoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }else{
            Log.d(TAG,"OpenCV loaded successfully");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallBack);
        }

    }

    private List<Character> averageColours(List<List<Pair<Point,Character>>> totalDetected){
        List<Character> averagedColours = new ArrayList<>(9);
        //Each row corresponds to a colour
        //0 - R
        //1 - O, Y, G, B, W, X (no colour detected)
        //Each column corresponds to a different sticker - see ColourDetector.getSquaresCentres
        int[][] totalMatrix = new int[9][7];
        for(List<Pair<Point, Character>> lpc : totalDetected)
            for(int i = 0; i < 9 ; i++)
                totalMatrix[i][ColourDetector.getColourInt(lpc.get(i).second)]++;

        for(int i = 0; i<9 ; i++){
            int maxVal = 0;
            int maxColour = -1;
            for(int j = 0; j < 7; j++){
                if(totalMatrix[i][j] > maxVal){
                    maxVal = totalMatrix[i][j];
                    maxColour = j;
                }
            }
            averagedColours.add(ColourDetector.getColourChar(maxColour));
        }
        return averagedColours;
    }
}
