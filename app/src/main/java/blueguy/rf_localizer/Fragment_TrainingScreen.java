package blueguy.rf_localizer;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment_TrainingScreen extends Fragment {

    private static final String KEY_LOCATION = ScanService.TAG_LOCATION;
    private static final String KEY_TRAIN_CLF = ScanService.TAG_TRAIN_ACTION;

    private String mCurrLocation = "";

    public static Fragment_TrainingScreen newInstance(String location) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_LOCATION, location);
        bundle.putBoolean(KEY_TRAIN_CLF, true);
        Fragment_TrainingScreen fragment_trainingScreen = new Fragment_TrainingScreen();
        fragment_trainingScreen.setArguments(bundle);
        return fragment_trainingScreen;
    }

    public Fragment_TrainingScreen() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrLocation = getArguments().getString(KEY_LOCATION);
        final boolean train = getArguments().getBoolean(KEY_TRAIN_CLF);
        ((MainActivity)getActivity()).bindScanService(mCurrLocation, train);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ((MainActivity)getActivity()).unbindScanService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_training_screen, container, false);


        // TODO: Set up buttons and text views as necessary

        // Location TextView
        TextView locationTextView = (TextView) rootView.findViewById(R.id.training_screen_location_text_view);
        locationTextView.setText(getArguments().getString(KEY_LOCATION));

        final EditText labelText = (EditText) rootView.findViewById(R.id.training_label_text);

        Button updateLabelButton = (Button) rootView.findViewById(R.id.button_update_label);
        updateLabelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String label = labelText.getText().toString();
                if (label.equals("?")) {
                    Toast.makeText(getActivity(), "? is not a valid label", Toast.LENGTH_SHORT).show();
                } else if (!label.isEmpty()) {
                    ((MainActivity)getActivity()).mScanService.setCurrLabel(label);
                    labelText.getText().clear();
                }
            }
        });

        Button clearLabelButton = (Button) rootView.findViewById(R.id.button_clear_label);
        clearLabelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).mScanService.resetCurrLabel();
                labelText.getText().clear();
            }
        });

        Button finishTrainingButton = (Button) rootView.findViewById(R.id.button_finish_training);
        finishTrainingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                labelText.getText().clear();
                getActivity().onBackPressed();
            }
        });

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity)getActivity()).mScanService.trainClassifier();
        ((MainActivity)getActivity()).mScanService.resetCurrLabel();
    }
}
