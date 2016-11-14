package andydean.opencvcamera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.IOException;

public class ImageAnalysis extends AppCompatActivity{



    private static String TAG = "ImageAnalysis";
    private static int PICK_IMAGE_REQUEST = 1;

    private SeekBar seekbar;
    private TextView seekbar_text;
    private SettingsVariable seekbar_var;

    private CubeDetector detector;

    public Mat imageFromFile, imageWithLines;
    public Bitmap image;



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
        setContentView(R.layout.activity_image);

        detector = new HoughLinesDetector();
        seekbar();

        Button loadImageButton = (Button) findViewById(R.id.load_image_button);

        loadImageButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if(imageFromFile != null)
                    imageFromFile = null;
                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

            }
        });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        imageFromFile.release();
        imageWithLines.release();
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

    public void drawMatToImageView(Mat image){
        Bitmap bm = Bitmap.createBitmap(image.cols(), image.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(image, bm);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(bm);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                image = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));
                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                imageView.setImageBitmap(image);

                imageFromFile = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8SC4);
                Utils.bitmapToMat(image, imageFromFile);
                imageWithLines = detector.detectCube(imageFromFile);
                drawMatToImageView(imageWithLines);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
                        Toast.makeText(ImageAnalysis.this, "Detecting Cube", Toast.LENGTH_LONG).show();
                        seekbar_var.setVal(progress_value);
                        imageWithLines = detector.detectCube(imageFromFile);
                        drawMatToImageView(imageWithLines);
                        Toast.makeText(ImageAnalysis.this, "Cube Detected", Toast.LENGTH_LONG).show();
                        //Toast.makeText(RealTimeAnalysis.this, "SeekBar stop tracking", Toast.LENGTH_LONG).show();

                    }
                }
        );
    }
}
