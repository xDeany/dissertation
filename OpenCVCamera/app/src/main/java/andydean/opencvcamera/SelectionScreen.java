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
        Button button = (Button) findViewById(R.id.hough_lines_button);

        button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent i = new Intent(SelectionScreen.this, HoughLines.class);
                startActivity(i);
            }
        });
    }
}
