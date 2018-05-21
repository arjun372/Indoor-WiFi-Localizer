package blueguy.rf_localizer;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import blueguy.rf_localizer.Scanners.DataObject;
import blueguy.rf_localizer.Scanners.Scanner;
import blueguy.rf_localizer.Scanners.ScannerCallback;
import blueguy.rf_localizer.Scanners.WifiScanner;
import blueguy.rf_localizer.utils.DataPair;

import static blueguy.rf_localizer.BuildConfig.DEBUG;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Prediction_UI#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Prediction_UI extends Fragment {

    private static final String TAG = "Predicting_Fragment";

    private static final long[] newLocationVibration = new long[]{500L, 0L, 500L};

    private static final Long updatePredictionVibration = 200L;

    /**
     * prediction related
     */
    private static final Long predictionTimeoutHistoryMs = 1000L;
    private Handler mPredictionRequestHandler = new Handler();
    private static volatile boolean ACCUMULATE = false;

    /**
     * GUI related
     **/
    private TextView predictLabelTextView;
    private Button yesButton, noButton;
    private RadarChart mChart;

    /**
     * indoor-map relatedk
     **/
    private List<DataPair<DataObject, String>> mAccumulatedDataAndLabels = new ArrayList<>();
    private IndoorMap mIndoorMap;

    /**
     * scanner related
     **/
    private List<Scanner> mScannerList;
    private ScannerCallback mScannerCallback = new ScannerCallback() {
        @Override
        public void onScanResult(final List<DataObject> dataList)
        {
            for (DataObject dataObject : dataList)
                mAccumulatedDataAndLabels.add(new DataPair<>(dataObject, DataObjectClassifier.CLASS_UNKNOWN));
            //mAccumulatedDataAndLabels.addAll(dataList.stream().map(dataObject -> new DataPair<>(dataObject, DataObjectClassifier.CLASS_UNKNOWN)).collect(Collectors.toList()));
        }
    };

    private int labelIdx = 0;

    private Runnable mPredictionRequest = new Runnable() {
        @Override
        public void run() {

            if(mAccumulatedDataAndLabels.size() > 0)
            {

                final DataPair<List<DataPair<DataObject, String>>, Map<String, Double>> dataWithDistributions = mIndoorMap.predictOnData(mAccumulatedDataAndLabels);

                synchronized (this) {
                    if (!ACCUMULATE) mAccumulatedDataAndLabels.clear();
                }

                final Map<String, Double> distributions = dataWithDistributions.second;

                setRadarData(distributions);

                if (DEBUG) {
                    for (final String location : distributions.keySet()) {
                        Log.d("PREDICTIONS", location + " : " + distributions.get(location));
                    }
                }

                String predictedLabel = "error";
                final Double maxValue = Collections.max(distributions.values());
                for (final String label : distributions.keySet())
                    if (maxValue.equals(distributions.get(label))) predictedLabel = label;

                updatePredictedLabel(predictedLabel);

                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String currentLabel = predictLabelTextView.getText().toString();
                        List<DataPair<DataObject, String>> unLabeledData = dataWithDistributions.first;
                        for (DataPair singleData : unLabeledData) singleData.second = currentLabel;
                        mIndoorMap.retrainWithData(unLabeledData);
                        mAccumulatedDataAndLabels.clear();
                    }
                });

                noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final List<String> labels = new ArrayList<>(distributions.keySet());
                        labelIdx = (labelIdx >= labels.size()) ? 0 : labelIdx + 1;
                        if (labelIdx < labels.size()) updatePredictedLabelSilent(labels.get(labelIdx));
                    }
                });

            }

            mPredictionRequestHandler.postDelayed(mPredictionRequest, predictionTimeoutHistoryMs);
        }
    };

    public Prediction_UI() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Prediction_UI.
     */
    // TODO: Rename and change types and number of parameters
    public static Prediction_UI newInstance(String location) {
        Prediction_UI fragment = new Prediction_UI();
        Bundle args = new Bundle();
        args.putString(IndoorMap.TAG_LOCATION, location);
        args.putBoolean(IndoorMap.TAG_TRAIN_ACTION, false);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_predicting_screen, container, false);

        mChart = (RadarChart) rootView.findViewById(R.id.chart1);
        yesButton = (Button) rootView.findViewById(R.id.button_yes);
        noButton = (Button) rootView.findViewById(R.id.button_no);

        initRadarChart();

        final TextView predictingLocationTextView = (TextView) rootView.findViewById(R.id.predicting_screen_location_text_view);
        predictingLocationTextView.setText(getArguments().getString(IndoorMap.TAG_LOCATION));

        final ToggleButton toggle = (ToggleButton) rootView.findViewById(R.id.accumulateButton);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               ACCUMULATE = toggle.isChecked();
            }
        });

        Button finishPredictingButton = (Button) rootView.findViewById(R.id.button_finish_predicting);
        finishPredictingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Do any necessary finishing ScanService stuff for predictions
                getActivity().onBackPressed();
            }
        });


        // TODO: Update this label as necessary according to the ScanService predictions
        predictLabelTextView = (TextView) rootView.findViewById(R.id.predicting_label_text);

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        mPredictionRequestHandler.removeCallbacks(mPredictionRequest);
        removeScanners();
        resetPredictedLabel();
        mAccumulatedDataAndLabels.clear();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(mAccumulatedDataAndLabels == null)
        {
            mAccumulatedDataAndLabels = new ArrayList<>();
        }
        else
        {
            mAccumulatedDataAndLabels.clear();
        }

        resetPredictedLabel();
        initIndoorMap();
        initScanners();
        mPredictionRequestHandler.postDelayed(mPredictionRequest, predictionTimeoutHistoryMs);
    }

    private void initRadarChart() {
        mChart.getDescription().setEnabled(false);
        mChart.setWebLineWidth(2f);
        mChart.setWebColor(Color.DKGRAY);
        mChart.setWebLineWidthInner(1.5f);
        mChart.setWebColorInner(Color.DKGRAY);
        mChart.setWebAlpha(100);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(8f);
        xAxis.setYOffset(0f);
        xAxis.setXOffset(0f);

        YAxis yAxis = mChart.getYAxis();
        yAxis.setTextSize(8f);
        yAxis.setAxisMinimum(0f);
        yAxis.setDrawLabels(false);
    }

    private void setRadarData(final Map<String, Double> predictions) {

        System.err.println("PREDICTIONS FOR RADAR DATA: " + predictions.toString());

        ArrayList<RadarEntry> predictVals = new ArrayList<>();
        for (final String label : predictions.keySet()) {
            predictVals.add(new RadarEntry(predictions.get(label).floatValue(), label));
        }

        RadarDataSet set1 = new RadarDataSet(predictVals, predictLabelTextView.getText().toString());
        set1.setColor(Color.RED);//.rgb(121, 162, 175));
        set1.setFillColor(Color.RED);//.setFillColor(Color.rgb(103, 110, 129));
        set1.setDrawFilled(true);
        set1.setFillAlpha(180);
        set1.setLineWidth(4f);
        set1.setDrawHighlightCircleEnabled(true);
        set1.setDrawHighlightIndicators(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {

            private final String[] mActivities = predictions.keySet().toArray(new String[predictions.keySet().size()]);

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return mActivities[(int) value % mActivities.length];
            }
        });

        ArrayList<IRadarDataSet> sets = new ArrayList<IRadarDataSet>();
        sets.add(set1);

        RadarData data = new RadarData(sets);
        data.setValueTextSize(16f);
        data.setDrawValues(false);
        data.setValueTextColor(Color.WHITE);

        mChart.setData(data);
        mChart.invalidate();;
        //mChart.animate();
        //mChart.animateY(100, Easing.EasingOption.EaseInCirc);
        //mChart.animateXY(500, 500, Easing.EasingOption.EaseInExpo, Easing.EasingOption.EaseInExpo);
    }

    private void resetPredictedLabel() {
        updatePredictedLabelSilent(DataObjectClassifier.CLASS_UNKNOWN);
    }

    private void updatePredictedLabel(final String newLabel) {
        if (updatePredictedLabelSilent(newLabel)) {
            Vibrator vibrateOnPredict = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            vibrateOnPredict.vibrate(newLocationVibration, -1);
        }
    }

    private boolean updatePredictedLabelSilent(final String newLabel) {
        final String currentLabel = predictLabelTextView.getText().toString();
        if (!currentLabel.equals(newLabel)) {
            predictLabelTextView.setText(newLabel);
            return true;
        }
        return false;
    }

    private void initScanners() {
        if (DEBUG) Log.d(TAG, "initScanners");

        if(mScannerList == null)
        {
            this.mScannerList = new ArrayList<>();
        }
        else
        {
            removeScanners();
        }

        // add wifi scanner
        this.mScannerList.add(new WifiScanner(mScannerCallback));

        // start wifi-scanner
        for (Scanner x : this.mScannerList)
        {
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
