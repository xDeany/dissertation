package andydean.opencvcamera;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SelectionScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection_screen);
        Button houghLinesRT = (Button) findViewById(R.id.hough_lines_real_time_button);
        Button houghLinesImage = (Button) findViewById(R.id.hough_lines_image_button);

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
}
