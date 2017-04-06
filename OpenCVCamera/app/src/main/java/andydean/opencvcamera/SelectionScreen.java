package andydean.opencvcamera;

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
import java.util.ArrayList;
import java.util.List;

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
        Button mainDetector = (Button) findViewById(R.id.main_detector_button);
        Button cubeBuilder = (Button) findViewById(R.id.test_cube_builder_button);


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
                        List<Point> corners = detector.detectCubeLocation(img);
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

        mainDetector.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent i = new Intent(SelectionScreen.this, MainDetector.class);
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

        cubeBuilder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<ArrayList<Character>> cube = new ArrayList<>();
                /*
                for(int i=0; i<6; i++){
                    ArrayList<Character> lc = new ArrayList<>();
                    Character c = ColourDetector.getColourChar(i);
                    for(int j=0; j<8; j++)
                        lc.add(c);
                    cube.add(lc);
                }*/
                /*ArrayList<Character> r = new ArrayList<Character>(8);
                r.add('B');
                r.add('B');
                r.add('G');
                r.add('Y');
                r.add('O');
                r.add('R');
                r.add('Y');
                r.add('Y');

                ArrayList<Character> o = new ArrayList<Character>(8);
                o.add('B');
                o.add('O');
                o.add('G');
                o.add('G');
                o.add('G');
                o.add('W');
                o.add('W');
                o.add('W');

                ArrayList<Character> y = new ArrayList<Character>(8);
                y.add('R');
                y.add('G');
                y.add('W');
                y.add('O');
                y.add('O');
                y.add('O');
                y.add('O');
                y.add('Y');

                ArrayList<Character> g = new ArrayList<Character>(8);
                g.add('W');
                g.add('W');
                g.add('R');
                g.add('G');
                g.add('G');
                g.add('G');
                g.add('Y');
                g.add('R');

                ArrayList<Character> b = new ArrayList<Character>(8);
                b.add('Y');
                b.add('O');
                b.add('W');
                b.add('B');
                b.add('B');
                b.add('B');
                b.add('Y');
                b.add('Y');

                ArrayList<Character> w = new ArrayList<Character>(8);
                w.add('R');
                w.add('W');
                w.add('B');
                w.add('R');
                w.add('R');
                w.add('B');
                w.add('O');
                w.add('R');



                ArrayList<Character> r = new ArrayList<Character>(8);
                r.add('B');
                r.add('R');
                r.add('R');
                r.add('G');
                r.add('Y');
                r.add('B');
                r.add('W');
                r.add('B');

                ArrayList<Character> o = new ArrayList<Character>(8);
                o.add('O');
                o.add('W');
                o.add('B');
                o.add('G');
                o.add('B');
                o.add('Y');
                o.add('R');
                o.add('O');

                ArrayList<Character> y = new ArrayList<Character>(8);
                y.add('R');
                y.add('Y');
                y.add('W');
                y.add('O');
                y.add('Y');
                y.add('R');
                y.add('G');
                y.add('W');

                ArrayList<Character> g = new ArrayList<Character>(8);
                g.add('W');
                g.add('B');
                g.add('G');
                g.add('Y');
                g.add('G');
                g.add('G');
                g.add('B');
                g.add('W');

                ArrayList<Character> b = new ArrayList<Character>(8);
                b.add('O');
                b.add('G');
                b.add('Y');
                b.add('O');
                b.add('O');
                b.add('B');
                b.add('R');
                b.add('O');

                ArrayList<Character> w = new ArrayList<Character>(8);
                w.add('G');
                w.add('Y');
                w.add('O');
                w.add('R');
                w.add('Y');
                w.add('R');
                w.add('W');
                w.add('W');
                */

                ArrayList<Character> r = new ArrayList<Character>(8);
                r.add('B');
                r.add('W');
                r.add('Y');
                r.add('G');
                r.add('W');
                r.add('Y');
                r.add('R');
                r.add('G');

                ArrayList<Character> o = new ArrayList<Character>(8);
                o.add('O');
                o.add('R');
                o.add('W');
                o.add('O');
                o.add('B');
                o.add('Y');
                o.add('R');
                o.add('O');

                ArrayList<Character> y = new ArrayList<Character>(8);
                y.add('G');
                y.add('Y');
                y.add('Y');
                y.add('G');
                y.add('G');
                y.add('B');
                y.add('G');
                y.add('R');

                ArrayList<Character> g = new ArrayList<Character>(8);
                g.add('O');
                g.add('R');
                g.add('Y');
                g.add('O');
                g.add('R');
                g.add('G');
                g.add('G');
                g.add('R');

                ArrayList<Character> b = new ArrayList<Character>(8);
                b.add('O');
                b.add('O');
                b.add('O');
                b.add('B');
                b.add('B');
                b.add('W');
                b.add('B');
                b.add('B');

                ArrayList<Character> w = new ArrayList<Character>(8);
                w.add('Y');
                w.add('B');
                w.add('R');
                w.add('W');
                w.add('W');
                w.add('Y');
                w.add('W');
                w.add('W');

                cube.add(r);
                cube.add(o);
                cube.add(y);
                cube.add(g);
                cube.add(b);
                cube.add(w);

                Intent i = new Intent(SelectionScreen.this, CubeNetBuilder.class);
                i.putExtra("faces", cube);
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
