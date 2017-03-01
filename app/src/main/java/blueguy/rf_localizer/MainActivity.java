package blueguy.rf_localizer;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set Layout
        // Set content view layout
        setContentView(R.layout.activity_main);

        // Get Toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);

        // Set up buttons
        Button trainButton = (Button) findViewById(R.id.main_button_train);
        Button predictButton = (Button) findViewById(R.id.main_button_predict);

        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Training button pressed.", Toast.LENGTH_SHORT).show();
                mShowTrainingPicker();
            }
        });

        predictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Predicting button pressed.", Toast.LENGTH_SHORT).show();
                mShowPredictingPicker();
            }
        });
    }


    private void mShowTrainingPicker() {
        new AlertDialog.Builder(getApplicationContext())
                .setTitle("Which building/location are you in?")
                .show();
    }


    private void mShowPredictingPicker() {

    }
}
