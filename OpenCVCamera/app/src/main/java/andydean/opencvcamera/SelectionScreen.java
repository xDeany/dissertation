package andydean.opencvcamera;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class SelectionScreen extends AppCompatActivity {

    private static String TAG = "SelectionScreen";

    BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch(status){
                case BaseLoaderCallback.SUCCESS:
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
        setContentView(R.layout.activity_selection_screen);
        Button houghLinesRT = (Button) findViewById(R.id.hough_lines_real_time_button);
        Button houghLinesImage = (Button) findViewById(R.id.hough_lines_image_button);
        Button testLocationDetector = (Button) findViewById(R.id.test_location_detector);


        testLocationDetector.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                String path = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/DissertationDataSet";
                File dir = new File(path);
                dir.mkdirs();
                for(int i = 1; i<115; i++) {
                    try {
                        int id = SelectionScreen.this.getResources().getIdentifier("cube_" + i, "drawable", SelectionScreen.this.getPackageName());
                        Mat img = Utils.loadResource(SelectionScreen.this, id, Imgcodecs.CV_LOAD_IMAGE_COLOR);
                        CubeDetector detector = new HoughLinesDetector(SelectionScreen.this);
                        ArrayList<Point> corners = detector.testDetectCube(img);
                        String strCorners = corners.toString();
                        File file = new File(path + "/test_corners_1d.txt");
                        FileOutputStream fOut = new FileOutputStream(file, true);
                        byte[] bs = strCorners.getBytes();
                        fOut.write(strCorners.getBytes());
                        String nl = "\n";
                        fOut.write(nl.getBytes());
                        fOut.close();


                        img.release();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        houghLinesRT.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent i = new Intent(SelectionScreen.this, RealTimeAnalysis.class);
                startActivity(i);
            }
        });

        houghLinesImage.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent i = new Intent(SelectionScreen.this, ImageAnalysis.class);
                startActivity(i);
            }
        });
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
