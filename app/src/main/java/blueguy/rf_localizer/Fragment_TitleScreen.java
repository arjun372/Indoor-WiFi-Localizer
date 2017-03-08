package blueguy.rf_localizer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import blueguy.rf_localizer.utils.PersistentMemoryManager;

/**
 * A simple {@link Fragment} subclass.
 * to handle interaction events.
 */
public class Fragment_TitleScreen extends Fragment{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        TextView textView = new TextView(getActivity());
//        textView.setText(R.string.hello_blank_fragment);

        // Set Layout
        // Set content view layout
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Set up buttons
        Button trainButton = (Button) rootView.findViewById(R.id.main_button_train);
        Button predictButton = (Button) rootView.findViewById(R.id.main_button_predict);

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

        return rootView;
    }


    private void mShowTrainingPicker() {
        Toast.makeText(getActivity(), "Training button", Toast.LENGTH_SHORT).show();

        final EditText textBox = new EditText(getActivity());
        textBox.setSingleLine(true);

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.main_location_prompt)
                .setView(textBox)
                .setPositiveButton(R.string.start_training, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        Toast.makeText(getActivity(), "got it " + textBox.getText().toString(), Toast.LENGTH_SHORT).show();
                        PersistentMemoryManager.updateLocationsList(getActivity(), textBox.getText().toString());
                    }
                })
                .show()
        ;
    }

    private void mShowPredictingPicker() {
        final ArrayList<String> listItems = new ArrayList<>(PersistentMemoryManager.getLocationsList(getActivity()));

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.main_location_prompt)

                .setSingleChoiceItems(listItems.toArray(new CharSequence[listItems.size()]), 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                })

                .setPositiveButton(R.string.start_predicting, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ListView listView = ((AlertDialog)dialog).getListView();
                        Object checkedItem = listView.getAdapter().getItem(listView.getCheckedItemPosition());
                        Toast.makeText(getActivity(), "sup " + checkedItem, Toast.LENGTH_SHORT).show();
                    }
                })

                .show();
    }
}
