package blueguy.rf_localizer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import blueguy.rf_localizer.utils.PersistentMemoryManager;

/**
 * A simple {@link Fragment} subclass.
 * to handle interaction events.
 */
public class Fragment_TitleScreen extends Fragment{

    public Fragment_TitleScreen() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Set Layout
        // Set content view layout
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Set up buttons
        Button trainButton = (Button) rootView.findViewById(R.id.main_button_train);
        Button predictButton = (Button) rootView.findViewById(R.id.main_button_predict);
        Button navigateButton = (Button) rootView.findViewById(R.id.main_button_navigate);

        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShowTrainingPicker();
            }
        });

        predictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShowPredictingPicker();
            }
        });

        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShowNavigationPicker();
            }
        });

        return rootView;
    }

    private void mChangeFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction
                .replace(R.id.main_fragment_container, fragment)
                .addToBackStack("Fragment_TitleScreen")
                .commit();
    }

    private void mShowTrainingPicker() {

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
                        mChangeFragment(Fragment_TrainingScreen.newInstance(textBox.getText().toString()));
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
                        mChangeFragment(Fragment_PredictingScreen.newInstance((String)checkedItem));
                    }
                })

                .show();
    }

    private void mShowNavigationPicker() {
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
                        mChangeFragment(Fragment_IndoorMap.newInstance((String)checkedItem));
                    }
                })
                .show();
    }
}
