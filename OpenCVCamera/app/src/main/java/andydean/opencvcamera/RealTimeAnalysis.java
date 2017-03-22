package andydean.opencvcamera;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class RealTimeAnalysis extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, AdapterView.OnItemSelectedListener {

    private static String TAG = "RealTimeAnalysis";
    private static int PICK_IMAGE_REQUEST = 1;

    private SeekBar seekbar;
    private TextView seekbar_text;
    private SettingsVariable seekbar_var;
    private Spinner spinner;
    private String imageToReturn;

    private CubeDetector detector;

    JavaCameraView javaCameraView;
    Mat cameraFrameRGB, imageWithLines;

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
        setContentView(R.layout.activity_real_time);


        javaCameraView = (JavaCameraView) findViewById(R.id.java_camera_view);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCameraIndex(0);
        javaCameraView.setCvCameraViewListener(this);

        detector = new HoughLinesDetector(this);
        spinner = (Spinner) findViewById(R.id.real_time_spinner);
        spinner.setOnItemSelectedListener(this);
        seekbar();
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(javaCameraView!=null)
            javaCameraView.disableView();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        cameraFrameRGB.release();
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

    @Override
    public void onCameraViewStarted(int width, int height) {
        cameraFrameRGB = new Mat(height, width, CvType.CV_8SC4);
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        cameraFrameRGB = inputFrame.rgba();
        imageWithLines = detector.detectCubeImageDebug(cameraFrameRGB, imageToReturn);
        return imageWithLines;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        seekbar_var = detector.getVariable(item.getItemId());
        if(seekbar_var!=null)
            seekbar.setProgress(seekbar_var.getVal());

        return super.onOptionsItemSelected(item);
    }

    public void seekbar( ){
        seekbar = (SeekBar)findViewById(R.id.seekbar);
        seekbar_var = detector.getInitialVar();
        seekbar.setProgress(seekbar_var.getVal()); //Set initial values to the first menu option
        seekbar.setMax(seekbar_var.getMax());
        seekbar_text = (TextView)findViewById(R.id.seekbar_text);
        seekbar_text.setText("Current " + seekbar_var.getName() + " = " + seekbar.getProgress() + " / " + seekbar.getMax());


        seekbar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener(){

                    int progress_value;

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        seekBar.setMax(seekbar_var.getMax());
                        progress_value = progress;
                        seekbar_text.setText("Current " + seekbar_var.getName() + " = " + progress + " / " + seekbar.getMax());
                        //Toast.makeText(RealTimeAnalysis.this, "SeekBar in progress", Toast.LENGTH_LONG).show();

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        //Toast.makeText(RealTimeAnalysis.this, "SeekBar start tracking", Toast.LENGTH_LONG).show();

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        seekbar_text.setText("Current " + seekbar_var.getName() + " = " + progress_value + " / " + seekbar.getMax());
                        seekbar_var.setVal(progress_value);
                        //Toast.makeText(RealTimeAnalysis.this, "SeekBar stop tracking", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //On spinner item selected
        imageToReturn = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
