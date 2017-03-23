package andydean.opencvcamera;

import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
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

import java.util.ArrayList;
import java.util.List;

public class MainDetector extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private static String TAG = "MainDetector";
    JavaCameraView javaCameraView;
    Mat cameraFrameStream;
    FloatingActionButton captureCubeButton;
    CubeDetector houghDetector;
    long time = System.currentTimeMillis();
    boolean videoMode = true;
    List<List<Pair<Point, Character>>> captured;
    List<Character> averaged;

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

        javaCameraView = (JavaCameraView) findViewById(R.id.java_camera_view_main);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCameraIndex(0);
        javaCameraView.setCvCameraViewListener(this);

        houghDetector = new HoughLinesDetector(this);

        captured = new ArrayList<>();

        captureCubeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoMode = false;
                time = System.currentTimeMillis();
            }
        });
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        cameraFrameStream = new Mat(height, width, CvType.CV_8SC4);
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        cameraFrameStream = inputFrame.rgba();
        if(!videoMode && System.currentTimeMillis() < time + 3000) {
            List<Point> corners = houghDetector.detectCubeLocation(cameraFrameStream);
            List<Pair<Point, Character>> colours;
            if (!corners.isEmpty()) {
                colours = houghDetector.detectCubeColour(cameraFrameStream, corners);

                if (colours != null) {
                    cameraFrameStream = HoughLinesDetector.drawPointsColour(colours, cameraFrameStream);
                    captured.add(colours);
                }
            }
        }else {
            if(!captured.isEmpty()){
                averaged = averageColours(captured);
                captured = new ArrayList<>();
            }
            videoMode = true;
        }
        return cameraFrameStream;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        cameraFrameStream.release();
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
        for(List<Pair<Point, Character>> lpc : totalDetected){
            for(int i = 0; i < 9 ; i++){
                Pair<Point,Character> pc = lpc.get(i);
                switch (pc.second){
                    case 'R':
                        totalMatrix[i][0]++;
                        break;
                    case 'O':
                        totalMatrix[i][1]++;
                        break;
                    case 'Y':
                        totalMatrix[i][2]++;
                        break;
                    case 'G':
                        totalMatrix[i][3]++;
                        break;
                    case 'B':
                        totalMatrix[i][4]++;
                        break;
                    case 'W':
                        totalMatrix[i][5]++;
                        break;
                    default:
                        totalMatrix[i][6]++;
                        break;
                }
            }
        }

        for(int i = 0; i<9 ; i++){
            int maxVal = 0;
            int maxColour = -1;
            for(int j = 0; j < 7; j++){
                if(totalMatrix[i][j] > maxVal){
                    maxVal = totalMatrix[i][j];
                    maxColour = j;
                }
            }
            switch(maxColour){
                case 0:
                    averagedColours.add('R');
                    break;
                case 1:
                    averagedColours.add('O');
                    break;
                case 2:
                    averagedColours.add('Y');
                    break;
                case 3:
                    averagedColours.add('G');
                    break;
                case 4:
                    averagedColours.add('B');
                    break;
                case 5:
                    averagedColours.add('W');
                    break;
                default:
                    averagedColours.add('X');
                    break;
            }
        }
        return averagedColours;
    }
}
