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

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class HoughLinesImage extends AppCompatActivity{



    private static String TAG = "HoughLinesImage";
    private static int PICK_IMAGE_REQUEST = 1;

    private SeekBar seekbar;
    private TextView seekbar_text;
    private String seekBarSetting = "bilateral_sigma_value";
    private String seekBarVariable = "Sigma Value";
    public int bilateral_sigma_value = 20;
    public int canny_threshold1 = 50;
    public int canny_threshold2 = 150;
    public int hough_rho = 1;
    public int hough_theta = 1;
    public int hough_threshold = 50;
    public int hough_min_line_length = 20;
    public int hough_max_line_gap = 10;
    public int height;
    public int width;

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
        setContentView(R.layout.activity_hough_image);


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
                Mat haughLines = getHaughLines(imageFromFile);
                drawHaughLines(haughLines, imageFromFile);
                drawMatToImageView(imageWithLines);
                haughLines.release();

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

        switch(item.getItemId()){
            case R.id.bilateral_sigma:
                seekBarSetting = "bilateral_sigma_value";
                seekBarVariable = "Sigma Value";
                seekbar.setProgress(bilateral_sigma_value);
                break;
            case R.id.canny_threshold_1:
                seekBarSetting = "canny_threshold1";
                seekBarVariable = "Canny Threshold 1";
                seekbar.setProgress(canny_threshold1);
                break;
            case R.id.canny_threshold_2:
                seekBarSetting = "canny_threshold2";
                seekBarVariable = "Canny Threshold 2";
                seekbar.setProgress(canny_threshold2);
                break;
            case R.id.hough_rho:
                seekBarSetting = "hough_rho";
                seekBarVariable = "Hough Rho";
                seekbar.setProgress(hough_rho);
                break;
            case R.id.hough_theta:
                seekBarSetting = "hough_theta";
                seekBarVariable = "Hough Theta";
                seekbar.setProgress(hough_theta);
                break;
            case R.id.hough_threshold:
                seekBarSetting = "hough_threshold";
                seekBarVariable = "Hough Threshold";
                seekbar.setProgress(hough_threshold);
                break;
            case R.id.hough_line_length:
                seekBarSetting = "hough_min_line_length";
                seekBarVariable = "Hough Min Line Length";
                seekbar.setProgress(hough_min_line_length);
                break;
            case R.id.hough_line_gap:
                seekBarSetting = "hough_max_line_gap";
                seekBarVariable = "Hough Max Line Gap";
                seekbar.setProgress(hough_max_line_gap);
                break;
            default:
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    public void seekbar( ){
        seekbar = (SeekBar)findViewById(R.id.seekbar);
        seekbar.setProgress(bilateral_sigma_value); //Set initial values to the first menu option
        seekbar_text = (TextView)findViewById(R.id.seekbar_text);
        seekbar_text.setText("Current " + seekBarVariable + " = " + seekbar.getProgress() + " / " + seekbar.getMax());


        seekbar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener(){

                    int progress_value;

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        switch(seekBarSetting) {
                            case "bilateral_sigma_value":
                                bilateral_sigma_value = progress;
                                seekBar.setMax(100);
                                break;
                            case "canny_threshold1":
                                canny_threshold1 = progress;
                                seekBar.setMax(400);
                                break;
                            case "canny_threshold2":
                                canny_threshold2 = progress;
                                seekBar.setMax(400);
                                break;
                            case "hough_rho":
                                hough_rho = progress;
                                seekBar.setMax(20);
                                break;
                            case "hough_theta":
                                hough_theta = progress;
                                seekBar.setMax(20);
                                break;
                            case "hough_threshold":
                                hough_threshold = progress;
                                seekBar.setMax(200);
                                break;
                            case "hough_min_line_length":
                                hough_min_line_length = progress;
                                seekBar.setMax(50);
                                break;
                            case "hough_max_line_gap":
                                hough_max_line_gap = progress;
                                seekBar.setMax(20);
                                break;
                            default:
                                break;
                        }

                        progress_value = progress;
                        seekbar_text.setText("Current " + seekBarVariable + " = " + progress + " / " + seekbar.getMax());



                        //Toast.makeText(HoughLinesRealTime.this, "SeekBar in progress", Toast.LENGTH_LONG).show();

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        //Toast.makeText(HoughLinesRealTime.this, "SeekBar start tracking", Toast.LENGTH_LONG).show();

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        seekbar_text.setText("Current " + seekBarVariable + " = " + progress_value + " / " + seekbar.getMax());
                        Mat haughLines = getHaughLines(imageFromFile);
                        drawHaughLines(haughLines, imageFromFile);
                        drawMatToImageView(imageWithLines);
                        haughLines.release();
                        //Toast.makeText(HoughLinesRealTime.this, "SeekBar stop tracking", Toast.LENGTH_LONG).show();

                    }
                }
        );
    }
    public Mat getHaughLines(Mat image){
        Mat grayscaleImage = new Mat(image.height(), image.width(), CvType.CV_8SC4);
        Mat mBlur = new Mat(height, width, CvType.CV_8SC4);
        Mat mCanny = new Mat(height, width, CvType.CV_8SC4);
        Mat haughLines = new Mat();

        Imgproc.cvtColor(image, grayscaleImage, Imgproc.COLOR_RGB2GRAY);
        Imgproc.bilateralFilter(grayscaleImage, mBlur, 5, bilateral_sigma_value, bilateral_sigma_value);
        Imgproc.Canny(mBlur, mCanny, canny_threshold1, canny_threshold1);
        Imgproc.HoughLinesP(mCanny, haughLines, hough_rho, hough_theta * Math.PI/180, hough_threshold, hough_min_line_length, hough_max_line_gap);

        mBlur.release();
        mCanny.release();
        return haughLines;
    }

    public void drawHaughLines(Mat haughLines, Mat image){
        imageWithLines = image.clone();
        //Grab all lines found from haughlines
        for (int x = 0; x < haughLines.rows(); x++) {
            double[] vec = haughLines.get(x, 0);
            double x1 = vec[0],
                    y1 = vec[1],
                    x2 = vec[2],
                    y2 = vec[3];
            Point start = new Point(x1, y1);
            Point end = new Point(x2, y2);
            double dx = x1 - x2;
            double dy = y1 - y2;

            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist > 100.d)  // show those lines that have length greater than 300
                Imgproc.line(imageWithLines, start, end, new Scalar(0, 255, 0, 255), 5);
        }
    }
}
