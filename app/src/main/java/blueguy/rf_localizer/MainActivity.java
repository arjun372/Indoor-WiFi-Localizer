package blueguy.rf_localizer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import blueguy.rf_localizer.utils.PersistentMemoryManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO : Start scan service here
        final Intent startServiceIntent = new Intent(this, ScanService.class);
        startService(startServiceIntent);


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
//                Toast.makeText(MainActivity.this, "Training button pressed.", Toast.LENGTH_SHORT).show();
                mShowTrainingPicker();
            }
        });

        predictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(MainActivity.this, "Predicting button pressed.", Toast.LENGTH_SHORT).show();
                mShowPredictingPicker();
            }
        });


    }

    private void mShowTrainingPicker() {
        Toast.makeText(this, "Training button", Toast.LENGTH_SHORT).show();

        final EditText textBox = new EditText(this);
        textBox.setSingleLine(true);

        new AlertDialog.Builder(this)
                .setTitle(R.string.main_location_prompt)
                .setView(textBox)
                .setPositiveButton(R.string.start_training, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "got it " + textBox.getText().toString(), Toast.LENGTH_SHORT).show();
                        PersistentMemoryManager.updateLocationsList(MainActivity.this, textBox.getText().toString());
                    }
                })
                .show()
                ;
    }

    private void mShowPredictingPicker() {
        final ArrayList<String> listItems = new ArrayList<>(PersistentMemoryManager.getLocationsList(this));

        new AlertDialog.Builder(this)
                .setTitle(R.string.main_location_prompt)

                .setSingleChoiceItems(listItems.toArray(new CharSequence[listItems.size()]), 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        Toast.makeText(MainActivity.this, "sup " + listItems.get(which), Toast.LENGTH_SHORT).show();
                    }
                })

                .setPositiveButton(R.string.start_predicting, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ListView listView = ((AlertDialog)dialog).getListView();
                        Object checkedItem = listView.getAdapter().getItem(listView.getCheckedItemPosition());
                        Toast.makeText(MainActivity.this, "sup " + checkedItem, Toast.LENGTH_SHORT).show();
                    }
                })

                .show();
    }
}
