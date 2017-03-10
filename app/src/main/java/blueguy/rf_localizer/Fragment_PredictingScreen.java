package blueguy.rf_localizer;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_PredictingScreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_PredictingScreen extends Fragment {
    private static final String KEY_LOCATION = "location";

    private String mCurrLocation = "";

    public Fragment_PredictingScreen() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Fragment_PredictingScreen.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_PredictingScreen newInstance(String location) {
        Fragment_PredictingScreen fragment = new Fragment_PredictingScreen();
        Bundle args = new Bundle();
        args.putString(KEY_LOCATION, location);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrLocation = getArguments().getString(KEY_LOCATION);
        ((MainActivity)getActivity()).bindScanService(mCurrLocation);
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
        View rootView = inflater.inflate(R.layout.fragment_predicting_screen, container, false);


        TextView predictingLocationTextView = (TextView) rootView.findViewById(R.id.predicting_screen_location_text_view);
        predictingLocationTextView.setText(getArguments().getString(KEY_LOCATION));

        Button finishPredictingButton = (Button) rootView.findViewById(R.id.button_finish_predicting);
        finishPredictingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Do any necessary finishing ScanService stuff for predictions
                getActivity().onBackPressed();
            }
        });


        // TODO: Update this label as necessary according to the ScanService predictions
        TextView predictLabelTextView = (TextView) rootView.findViewById(R.id.predicting_label_text);
        

        return rootView;
    }

}
