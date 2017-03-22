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
    ImageView singleFrameView;
    Mat cameraFrameStream, capturedFrame;
    FloatingActionButton captureCubeButton;
    CubeDetector houghDetector;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_detector);
        captureCubeButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);

        javaCameraView = (JavaCameraView) findViewById(R.id.java_camera_view_main);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCameraIndex(0);
        javaCameraView.setCvCameraViewListener(this);

        singleFrameView = (ImageView) findViewById(R.id.imageView2);
        singleFrameView.setVisibility(SurfaceView.INVISIBLE);

        houghDetector = new HoughLinesDetector(this);

        captureCubeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capturedFrame = cameraFrameStream.clone();
                List<Point> corners = houghDetector.detectCubeLocation(capturedFrame);
                List<Pair<Point, Character>> colours;
                if(!corners.isEmpty()) {
                    colours = houghDetector.detectCubeColour(capturedFrame, corners);
                    List<Character> cols = new ArrayList<Character>();
                    for(Pair<Point, Character> p : colours)
                        cols.add(p.second);
                    String str = cols.toString();
                    Toast.makeText(MainDetector.this, str, Toast.LENGTH_LONG).show();
                }
                capturedFrame.release();
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
}
