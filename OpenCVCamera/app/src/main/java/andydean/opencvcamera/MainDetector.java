package andydean.opencvcamera;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainDetector extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private static String TAG = "MainDetector";

    //Constantly stream the camera frames to the screen
    private static final String VIDEO_STATE = "video_state";

    //Attempting to detect cube location and colour
    private static final String CAPTURE_STATE = "capture_state";

    //Cube detected, user can correct errors before saving
    private static final String ERROR_CHECK_STATE = "error_check_state";

    private String state = VIDEO_STATE;
    //Main image view
    JavaCameraView javaCameraView;
    //Frames used for still images
    Mat capturedFrame, drawnFrame, capturedFrameWithPixels;
    FloatingActionButton startCaptureButton, saveFaceButton, rejectFaceButton, connetFacesButton;
    CubeDetector houghDetector;
    long time = System.currentTimeMillis();
    //The frames captured during capture_state
    List<List<Pair<Point, Character>>> captured;
    //The colours displayed on the screen during error_check_state
    List<Pair<Point,Character>> onScreenColours;
    //Boolean to register when the user has changed a displayed colour
    boolean onScreenColourChange = false;
    //List view of the face of the cube in the upper left corner
    List<Character>  toSave;
    //The views to store - current face being edited and the collection of faces seen so far
    List<View> sideEditor, selectFace;
    //List of the sides left to see
    List<Character> unseenCentres = new ArrayList<>();
    ArrayList<Character> black = new ArrayList<>(9);
    //Map storing the faces seen so far, with
    ArrayList<ArrayList<Character>> seenFacesList;

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
        startCaptureButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        saveFaceButton = (FloatingActionButton) findViewById(R.id.tickButton);
        rejectFaceButton = (FloatingActionButton) findViewById(R.id.crossButton);
        connetFacesButton = (FloatingActionButton) findViewById(R.id.create_net_button);
        connetFacesButton.setVisibility(View.GONE);

        javaCameraView = (JavaCameraView) findViewById(R.id.java_camera_view_main);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCameraIndex(0);
        javaCameraView.setCvCameraViewListener(this);

        houghDetector = new HoughLinesDetector(this);

        captured = new ArrayList<>();

        sideEditor = new ArrayList<>();
        sideEditor.add(0, findViewById(R.id.cell1));
        sideEditor.add(1, findViewById(R.id.cell2));
        sideEditor.add(2, findViewById(R.id.cell3));
        sideEditor.add(3, findViewById(R.id.cell4));
        sideEditor.add(4, findViewById(R.id.cell5));
        sideEditor.add(5, findViewById(R.id.cell6));
        sideEditor.add(6, findViewById(R.id.cell7));
        sideEditor.add(7, findViewById(R.id.cell8));
        sideEditor.add(8, findViewById(R.id.cell9));

        selectFace = new ArrayList<>(6);
        selectFace.add(0, findViewById(R.id.faceR));
        selectFace.add(1, findViewById(R.id.faceO));
        selectFace.add(2, findViewById(R.id.faceY));
        selectFace.add(3, findViewById(R.id.faceG));
        selectFace.add(4, findViewById(R.id.faceB));
        selectFace.add(5, findViewById(R.id.faceW));

        unseenCentres.add('R');
        unseenCentres.add('O');
        unseenCentres.add('Y');
        unseenCentres.add('G');
        unseenCentres.add('B');
        unseenCentres.add('W');

        toSave = new ArrayList<>(9);
        for (int i = 0; i < 9; i++){
            black.add('X');
            toSave.add('X');
        }

        seenFacesList = new ArrayList<>(6);


        for(int i = 0; i < 6; i++)
            seenFacesList.add(black);

        for(int i = 0; i < 6; i++){
            final View faceStorage = selectFace.get(i);
            final int j = i;
            faceStorage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                toSave.clear();
                for(char c : seenFacesList.get(j))
                    toSave.add(c);
                updateCornerFace(toSave);
                }
            });
        }

        for(int i = 0; i < 9; i++){
            final View cubeSquare = sideEditor.get(i);
            final int j = i;
            cubeSquare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                char currentColour = toSave.get(j);
                char newColour = ColourDetector.nextColour(currentColour);
                toSave.set(j, newColour);
                double[] rgbVals = ColourDetector.getRGB(newColour);
                cubeSquare.setBackgroundColor(Color.rgb((int) rgbVals[0], (int) rgbVals[1], (int) rgbVals[2]));
                }
            });
        }

        startCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state.equals(VIDEO_STATE)){
                    state = CAPTURE_STATE;
                    time = System.currentTimeMillis();
                }
            }
        });

        saveFaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state.equals(ERROR_CHECK_STATE)) {
                    //Reset state of stored frames
                    capturedFrame.release();
                    capturedFrameWithPixels.release();
                    state = VIDEO_STATE;
                    //Update cube in the corner to the one on frame
                    updateCornerFace(toSave);
                }

                storeFace(toSave);
                //If all sides have been added, allow the user to go to the next step
                if(unseenCentres.isEmpty())
                    connetFacesButton.setVisibility(View.VISIBLE);
            }
        });

        rejectFaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state.equals(ERROR_CHECK_STATE)) {
                    //Reset state of stored frames
                    capturedFrame.release();
                    capturedFrameWithPixels.release();
                    state = VIDEO_STATE;
                }
                updateCornerFace(black);
                toSave.clear();
                for(int i = 0; i<9; i++)
                    toSave.add('X');
            }
        });

        javaCameraView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(state.equals(ERROR_CHECK_STATE)){
                    onScreenColourChange = true;
                    //Find touch location and adjust for frame location
                    int eX = (int) event.getX();
                    int eY = (int) event.getY();
                    int x = eX - 150;
                    int y = eY - 100;
                    Point touchLocation = new Point(x,y);
                    //Find the index of the nearest point;
                    int pairNum = getNearestPoint(onScreenColours, touchLocation);
                    //Increment through the list of colours
                    char newC = ColourDetector.nextColour(onScreenColours.get(pairNum).second);
                    Pair<Point, Character> newPC = new Pair<>(onScreenColours.get(pairNum).first, newC);
                    toSave.set(pairNum, newC);
                    onScreenColours.set(pairNum, newPC);
                }
                return false;
            }
        });

        connetFacesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<ArrayList<Character>> net = new ArrayList<>();
                for(List<Character> lc : seenFacesList) {
                    ArrayList<Character> nc = new ArrayList<>();
                    for (int i = 0; i < 8; i++) {
                        nc.add(lc.get(i));
                    }
                    net.add(nc);
                }

                if(checkStickerNum(net)) {
                    Intent i = new Intent(MainDetector.this, CubeNetBuilder.class);
                    i.putExtra("faces", net);
                    startActivity(i);
                }else{
                    Toast.makeText(getBaseContext(), "Invalid number of stickers, check your faces" , Toast.LENGTH_SHORT ).show();
                }
            }
        });
    }

    private boolean checkStickerNum(ArrayList<ArrayList<Character>> net){
        int red = 0;
        int orange = 0;
        int yellow = 0;
        int green = 0;
        int blue = 0;
        int white = 0;
        int black = 0;
        //Simple check to count the number of times each sticker colour appears
        for(ArrayList<Character> lc : net) {
            for (Character c : lc) {
                switch (c) {
                    case 'R':
                        red++;
                        break;
                    case 'O':
                        orange++;
                        break;
                    case 'Y':
                        yellow++;
                        break;
                    case 'G':
                        green++;
                        break;
                    case 'B':
                        blue++;
                        break;
                    case 'W':
                        white++;
                        break;
                    default:
                        black++;
                        break;
                }
            }
        }

        return !(black > 0 || red != 8 || orange != 8 || yellow != 8 || green != 8 || blue != 8 || white != 8);
    }

    private boolean storeFace(List<Character> toSave){
        if(toSave.contains('X'))
            return false;
        Character centre = toSave.get(8);
        int faceIndex = ColourDetector.getColourInt(centre);
        //Update unseenCentres list and centres in bottom left
        if(unseenCentres.contains(centre))
            unseenCentres.remove(centre);

        //Copy over toSave, don't store reference to object
        seenFacesList.set(faceIndex, new ArrayList<>(toSave));

        double[] rgbVals = ColourDetector.getRGB(centre);
        selectFace.get(faceIndex).setBackgroundColor(Color.rgb((int) rgbVals[0], (int) rgbVals[1], (int) rgbVals[2]));
        return true;
    }

    private int getNearestPoint(List<Pair<Point, Character>> points, Point touchLocation){
        double minDist = Double.POSITIVE_INFINITY;
        int pairNum = -1;
        for(int i = 0; i < 9; i++){
            Pair<Point, Character> pc = points.get(i);
            double dist = Line.calcDistBetween(pc.first, touchLocation);
            if(dist < minDist){
                minDist = dist;
                pairNum = i;
            }
        }
        return pairNum;
    }

    private void updateCornerFace(List<Character> face){
        for(int i = 0; i < 9 ; i++){
            Character c = face.get(i);
            View iv = sideEditor.get(i);
            double[] rgbVals = ColourDetector.getRGB(c);
            iv.setBackgroundColor(Color.rgb((int) rgbVals[0], (int) rgbVals[1], (int) rgbVals[2]));
        }
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

        if(state.equals(CAPTURE_STATE)) {
            if (System.currentTimeMillis() < time + 3000) {
                List<Point> corners = houghDetector.detectCubeLocation(drawnFrame);
                List<Pair<Point, Character>> colours;
                if (!corners.isEmpty()) {
                    colours = houghDetector.detectCubeColour(drawnFrame, corners);

                    if (colours != null) {
                        for (Pair<Point, Character> p : colours) {
                            double[] rgbVals = ColourDetector.getRGB(p.second);
                            Imgproc.circle(drawnFrame, p.first, 8, new Scalar(rgbVals[0], rgbVals[1], rgbVals[2], 255), 40);
                        }
                        captured.add(colours);
                    }
                }
            } else {
                if (!captured.isEmpty()) {

                    capturedFrame = drawnFrame.clone();
                    capturedFrameWithPixels = drawnFrame.clone();

                    List<Character> averaged = averageColours(captured);
                    toSave = averaged;
                    onScreenColours = new ArrayList<>();
                    for (int i = 0; i < 9; i++) {
                        Point p = captured.get(captured.size() - 1).get(i).first;
                        Character c = averaged.get(i);
                        onScreenColours.add(new Pair<>(p, c));
                    }
                    captured = new ArrayList<>();
                    state = ERROR_CHECK_STATE;
                    onScreenColourChange = true;
                } else
                    state = VIDEO_STATE;

            }
        }

        if(onScreenColourChange) {
            capturedFrameWithPixels.release();
            capturedFrameWithPixels = HoughLinesDetector.drawPointsColour(onScreenColours, capturedFrame);
            onScreenColourChange = false;
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
        if(javaCameraView!=null)
            javaCameraView.enableView();
    }

    @Override
    protected void onPause(){
        super.onPause();
        drawnFrame.release();
        if(javaCameraView!=null)
            javaCameraView.disableView();
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
