package andydean.opencvcamera;

import android.content.Intent;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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
        Button netTest = (Button) findViewById(R.id.test_net_builders);


        testLocationDetector.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                String path = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/DissertationDataSet";
                File dir = new File(path);
                CubeDetector detector = new HoughLinesDetector(SelectionScreen.this);
                dir.mkdirs();

                List<Pair<Long, Boolean>> times = new ArrayList<>();
                for(int i = 1; i<115; i++) {
                    try {
                        int id = SelectionScreen.this.getResources().getIdentifier("cube_" + i, "drawable", SelectionScreen.this.getPackageName());
                        Mat img = Utils.loadResource(SelectionScreen.this, id, Imgcodecs.CV_LOAD_IMAGE_COLOR);


                        long t1 = SystemClock.currentThreadTimeMillis();
                        List<Point> corners = detector.detectCubeLocation(img);
                        long t2 = SystemClock.currentThreadTimeMillis();

                        long tDiff = t2-t1;
                        boolean foundCorners = corners.isEmpty();
                        times.add(new Pair<>(tDiff, foundCorners));

                        img.release();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    File file = new File(path + "/test_corners_time_test.txt");
                    FileOutputStream fOut = new FileOutputStream(file, true);
                    for(Pair<Long, Boolean> tB : times){
                        String str = String.valueOf(tB.first) + "," + String.valueOf(tB.second) + "\n";
                        fOut.write(str.getBytes());
                    }
                    fOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
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
                Intent i = new Intent(SelectionScreen.this, UserActivity.class);
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

        netTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/DissertationDataSet";
                File dir = new File(path);
                dir.mkdirs();

                final ArrayList<Character> BLANK_ROW = new ArrayList<>(Arrays.asList('X','X','X','X','X','X','X','X'));
                final ArrayList<ArrayList<Character>> BLANK_NET = new ArrayList<>(Arrays.asList(BLANK_ROW, BLANK_ROW, BLANK_ROW, BLANK_ROW, BLANK_ROW, BLANK_ROW));

                List<CubePiece> vp = CubeNetBuilder.generateAllValidPieces();
                List<CubePiece> corners = new ArrayList<>();
                List<CubePiece> centres = new ArrayList<>();
                List<Boolean> isCentre = new ArrayList<>(Arrays.asList(false,true,false,true,false,true,false,true,true,true,true,true,false,true,false,true,false,true,false,true));

                List<Pair<Long, Long>> results = new ArrayList<>();
                for(int num = 0; num<200; num++) {
                    for (CubePiece cp : vp)
                        cp.randomise();

                    for (int i = 0; i < 20; i++)
                        if (isCentre.get(i))
                            centres.add(vp.get(i));
                        else
                            corners.add(vp.get(i));


                    Collections.shuffle(corners);
                    Collections.shuffle(centres);

                    List<CubePiece> cube = new ArrayList<>();
                    for (int i = 0; i < 20; i++)
                        if (isCentre.get(i))
                            cube.add(centres.remove(0));
                        else
                            cube.add(corners.remove(0));

                    ArrayList<ArrayList<Character>> net = CubeNetBuilder.cubeToNet(cube);

                    for (int i = 0; i < 6; i++) {
                        int randomNum = ThreadLocalRandom.current().nextInt(0, 4);
                        Collections.rotate(net.get(i), randomNum * 2);
                    }

                    Pair<ArrayList<ArrayList<Character>>, ArrayList<Integer>> result = CubeNetBuilder.improvedSearch(CubeNetBuilder.netToCube(BLANK_NET), new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0)), new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, -1)), net);
                    for(ArrayList<Character> face : result.first)
                        Collections.rotate(face,2);

                    long t1 = SystemClock.currentThreadTimeMillis();
                    CubeNetBuilder.improvedSearch(CubeNetBuilder.netToCube(BLANK_NET), new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0)), new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, -1)), net);
                    long t2 = SystemClock.currentThreadTimeMillis();

                    long t3 = SystemClock.currentThreadTimeMillis();
                    CubeNetBuilder.improvedSearch(CubeNetBuilder.netToCube(BLANK_NET), new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0)), new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, -1)), result.first);
                    long t4 = SystemClock.currentThreadTimeMillis();
                    //ArrayList<Character> f = net.remove(1);
                    //net.add(f);
                    //long t3 = SystemClock.currentThreadTimeMillis();
                    //CubeNetBuilder.improvedSearch(BLANK_NET, new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0)), new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, -1)), net);
                    //long t4 = SystemClock.currentThreadTimeMillis();

                    long improved = t2 - t1;
                    long slow = t4 - t3;
                    results.add(new Pair<>(improved, slow));
                }

                try {
                    File file = new File(path + "/net_builders_a_and_worst.txt");
                    FileOutputStream fOut = new FileOutputStream(file, true);
                    for(Pair<Long, Long> pll : results){
                        long fast = pll.first;
                        long slow = pll.second;
                        String str = String.valueOf(fast)+ "," + String.valueOf(slow) + "\n";
                        fOut.write(str.getBytes());
                    }
                    fOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        cubeBuilder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<ArrayList<Character>> net = new ArrayList<>();
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

*/
                ArrayList<Character> r = new ArrayList<Character>(8);
                r.add('R');
                r.add('Y');
                r.add('R');
                r.add('G');
                r.add('R');
                r.add('W');
                r.add('R');
                r.add('B');

                ArrayList<Character> o = new ArrayList<Character>(8);
                o.add('O');
                o.add('Y');
                o.add('O');
                o.add('B');
                o.add('O');
                o.add('W');
                o.add('O');
                o.add('G');

                ArrayList<Character> y = new ArrayList<Character>(8);
                y.add('Y');
                y.add('O');
                y.add('Y');
                y.add('G');
                y.add('Y');
                y.add('R');
                y.add('Y');
                y.add('B');

                ArrayList<Character> g = new ArrayList<Character>(8);
                g.add('G');
                g.add('Y');
                g.add('G');
                g.add('O');
                g.add('G');
                g.add('W');
                g.add('G');
                g.add('R');

                ArrayList<Character> b = new ArrayList<Character>(8);
                b.add('B');
                b.add('Y');
                b.add('B');
                b.add('R');
                b.add('B');
                b.add('W');
                b.add('B');
                b.add('O');

                ArrayList<Character> w = new ArrayList<Character>(8);
                w.add('W');
                w.add('R');
                w.add('W');
                w.add('G');
                w.add('W');
                w.add('O');
                w.add('W');
                w.add('B');
                /*

                ArrayList<Character> r = new ArrayList<Character>(8);
                r.add('Y');
                r.add('R');
                r.add('R');
                r.add('R');
                r.add('W');
                r.add('W');
                r.add('W');
                r.add('G');

                ArrayList<Character> o = new ArrayList<Character>(8);
                o.add('B');
                o.add('O');
                o.add('O');
                o.add('O');
                o.add('W');
                o.add('B');
                o.add('Y');
                o.add('Y');

                ArrayList<Character> y = new ArrayList<Character>(8);
                y.add('B');
                y.add('Y');
                y.add('R');
                y.add('B');
                y.add('B');
                y.add('R');
                y.add('G');
                y.add('B');

                ArrayList<Character> g = new ArrayList<Character>(8);
                g.add('O');
                g.add('O');
                g.add('O');
                g.add('Y');
                g.add('Y');
                g.add('Y');
                g.add('R');
                g.add('G');

                ArrayList<Character> b = new ArrayList<Character>(8);
                b.add('O');
                b.add('W');
                b.add('G');
                b.add('B');
                b.add('W');
                b.add('W');
                b.add('R');
                b.add('R');

                ArrayList<Character> w = new ArrayList<Character>(8);
                w.add('G');
                w.add('G');
                w.add('G');
                w.add('W');
                w.add('B');
                w.add('O');
                w.add('Y');
                w.add('G');*/

                net.add(r);
                net.add(o);
                net.add(y);
                net.add(g);
                net.add(b);
                net.add(w);

                Intent i = new Intent(SelectionScreen.this, CubeNetBuilder.class);
                i.putExtra("faces", net);
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
