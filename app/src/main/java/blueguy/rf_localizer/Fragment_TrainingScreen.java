package blueguy.rf_localizer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import blueguy.rf_localizer.Scanners.DataObject;
import blueguy.rf_localizer.Scanners.Scanner;
import blueguy.rf_localizer.Scanners.ScannerCallback;
import blueguy.rf_localizer.Scanners.WifiScanner;
import blueguy.rf_localizer.utils.DataPair;

import static blueguy.rf_localizer.BuildConfig.DEBUG;

/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment_TrainingScreen extends Fragment {

    private static final String TAG = "Training_Activity";

    /**
     * indoor-map related
     **/
    private List<DataPair<DataObject, String>> mAccumulatedDataAndLabels;
    private String mGroundTruthLabel;
    private IndoorMap mIndoorMap;

    /**
     * scanner related
     **/
    private List<Scanner> mScannerList;

    /**
     * GUI related
     **/

    private ScannerCallback mScannerCallback = new ScannerCallback() {
        @Override
        public void onScanResult(final List<DataObject> dataList) {
            if (mAccumulatedDataAndLabels == null) mAccumulatedDataAndLabels = new ArrayList<>();
            for (DataObject dataObject : dataList) {
                mAccumulatedDataAndLabels.add(new DataPair<>(dataObject, mGroundTruthLabel));
            }
//            mAccumulatedDataAndLabels.addAll(dataList.stream().map(dataObject -> new DataPair<>(dataObject, mGroundTruthLabel)).collect(Collectors.toList()));
        }
    };

    public static Fragment_TrainingScreen newInstance(String location) {
        Bundle bundle = new Bundle();
        bundle.putString(IndoorMap.TAG_LOCATION, location);
        bundle.putBoolean(IndoorMap.TAG_TRAIN_ACTION, true);
        Fragment_TrainingScreen fragment_trainingScreen = new Fragment_TrainingScreen();
        fragment_trainingScreen.setArguments(bundle);
        return fragment_trainingScreen;
    }

    public Fragment_TrainingScreen() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_training_screen, container, false);

        /* IndoorMap Name TextView */
        TextView locationTextView = (TextView) rootView.findViewById(R.id.training_screen_location_text_view);
        locationTextView.setText(getArguments().getString(IndoorMap.TAG_LOCATION));

        /* Save reference to GroundTruth label */
        EditText labelText = (EditText) rootView.findViewById(R.id.training_label_text);

        Button updateLabelButton = (Button) rootView.findViewById(R.id.button_update_label);
        updateLabelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String label = labelText.getText().toString();
                if (label.equals("?") || label.equals(DataObjectClassifier.CLASS_UNKNOWN)) {
                    Toast.makeText(getActivity(), label + " is not a valid label", Toast.LENGTH_SHORT).show();
                } else if (!label.isEmpty()) {
                    setGroundTruthLabel(label);
                    labelText.getText().clear();
                }
            }
        });

        Button clearLabelButton = (Button) rootView.findViewById(R.id.button_clear_label);
        clearLabelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetGroundTruthLabel();
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
        removeScanners();
        resetGroundTruthLabel();
        this.mIndoorMap.finishTraining(mAccumulatedDataAndLabels);
        mAccumulatedDataAndLabels.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAccumulatedDataAndLabels = new ArrayList<>();
        resetGroundTruthLabel();
        initIndoorMap();
        initScanners();
    }

    private void resetGroundTruthLabel() {
        setGroundTruthLabel(DataObjectClassifier.CLASS_UNKNOWN);
    }

    private void setGroundTruthLabel(final String newLabel) {
        mGroundTruthLabel = newLabel;
        Toast.makeText(getActivity(), "new label: " + mGroundTruthLabel, Toast.LENGTH_SHORT).show();
    }

    private void initScanners() {
        if (DEBUG) Log.d(TAG, "initScanners");
        this.mScannerList = new ArrayList<>();
        this.mScannerList.add(new WifiScanner(mScannerCallback));
        //curScanners.add(new CellScanner(mScannerCallback));
        //curScanners.add(new BluetoothScanner(mScannerCallback));
        //curScanners.add(new VelocityScanner(mScannerCallback));
        //curScanners.add(new RotationScanner(mScannerCallback));
        //curScanners.add(new MagneticFieldScanner(mScannerCallback));
        //curScanners.add(new PressureScanner(mScannerCallback));
        for (Scanner x : this.mScannerList) {
            x.startScan();
        }
    }

    private void removeScanners() {
        if (DEBUG) Log.d(TAG, "removeScanners");
        for (Scanner x : this.mScannerList) {
            x.stopScan();
        }
        this.mScannerList.clear();
    }

    private void initIndoorMap() {
        final String indoorMapName = getArguments().getString(IndoorMap.TAG_LOCATION);
        this.mIndoorMap = new IndoorMap(indoorMapName);
    }
}