package blueguy.rf_localizer;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
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
import blueguy.rf_localizer.utils.DataPair;

import static blueguy.rf_localizer.BuildConfig.DEBUG;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_PredictingScreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_PredictingScreen extends Fragment {

    private static final String KEY_LOCATION = ScanService.TAG_LOCATION;
    private static final String KEY_TRAIN_CLF = ScanService.TAG_TRAIN_ACTION;

    private Handler mPredictionRequestHandler = new Handler();

    private static final Long predictionTimeoutHistoryMs = 10000L;

    private static final long[] vibrationPattern = new long[] {0L, 500L, 0L};

    private Button yesButton, noButton;
    private RadarChart mChart;

    private int labelIdx = 0;
    private static final String UNKNOWN = "Calculating...";

    private Runnable mPredictionRequest = new Runnable() {
        @Override
        public void run() {
            final Long now = System.currentTimeMillis();
            final Long past = now - predictionTimeoutHistoryMs;
            final DataPair<List<DataPair<DataObject, String>>, Map<String, Double>> distributionsWithData = ((MainActivity)getActivity()).mScanService.predictOnData(false);
            final Map<String, Double> distributions = distributionsWithData.second;

            setRadarData(distributions);

            if(DEBUG)
            {
                for(final String location : distributions.keySet())
                {
                    Log.d("PREDICTIONS", location + " : " + distributions.get(location));
                }
            }

            final String predictedLabel = Collections.max(distributions.entrySet(), Map.Entry.comparingByValue()).getKey();
            updateLabel(predictedLabel);

            yesButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    final String currentLabel = predictLabelTextView.getText().toString();
                    List<DataPair<DataObject, String>> unLabeledData = distributionsWithData.first;
                    for(DataPair singleData : unLabeledData) singleData.second = currentLabel;
                    ((MainActivity)getActivity()).mScanService.updateClassifierData(unLabeledData);
                }
            });

            noButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    final List<String> labels = new ArrayList<>(distributions.keySet());
                    labelIdx = (labelIdx >= labels.size()) ? 0 : labelIdx+1;

                    if(labelIdx < labels.size())
                        updateLabel(labels.get(labelIdx));
                }
            });

            mPredictionRequestHandler.postDelayed(mPredictionRequest, predictionTimeoutHistoryMs);
        }
    };

    private TextView predictLabelTextView;

    private String mCurrLocation;

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
        args.putBoolean(KEY_TRAIN_CLF, false);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrLocation = getArguments().getString(KEY_LOCATION);
        final boolean predict = getArguments().getBoolean(KEY_TRAIN_CLF);
        ((MainActivity)getActivity()).bindScanService(mCurrLocation, predict);
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

        mChart = (RadarChart) rootView.findViewById(R.id.chart1);
        yesButton = (Button) rootView.findViewById(R.id.button_yes);
        noButton = (Button) rootView.findViewById(R.id.button_no);

        initRadarChart();

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
        predictLabelTextView = (TextView) rootView.findViewById(R.id.predicting_label_text);
        

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity)getActivity()).mScanService.resetCurrLabel();
        mPredictionRequestHandler.removeCallbacks(mPredictionRequest);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLabel(UNKNOWN);
        mPredictionRequestHandler.postDelayed(mPredictionRequest, predictionTimeoutHistoryMs);
    }

    private void initRadarChart() {

        //mChart.setBackgroundColor(Color.rgb(60, 65, 82));
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

        ArrayList<RadarEntry> predictVals = new ArrayList<>();
        for(final String label : predictions.keySet())
        {
            predictVals.add(new RadarEntry(predictions.get(label).floatValue(), label));
        }

        RadarDataSet set1 = new RadarDataSet(predictVals, mCurrLocation);
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

//        RadarDataSet set2 = new RadarDataSet(entries2, "This Week");
//        set2.setColor(Color.rgb(121, 162, 175));
//        set2.setFillColor(Color.rgb(121, 162, 175));
//        set2.setDrawFilled(true);
//        set2.setFillAlpha(180);
//        set2.setLineWidth(2f);
//        set2.setDrawHighlightCircleEnabled(true);
//        set2.setDrawHighlightIndicators(false);

        ArrayList<IRadarDataSet> sets = new ArrayList<IRadarDataSet>();
        sets.add(set1);

        RadarData data = new RadarData(sets);
        data.setValueTextSize(16f);
        data.setDrawValues(false);
        data.setValueTextColor(Color.WHITE);

        mChart.setData(data);
        mChart.animateXY(500, 500, Easing.EasingOption.EaseInOutCirc, Easing.EasingOption.EaseInOutCirc);
    }


    private void updateLabel(final String newLabel) {

        final String currentLabel = predictLabelTextView.getText().toString();
        if(!currentLabel.equals(newLabel))
        {
            predictLabelTextView.setText(newLabel);
            Vibrator vibrateOnPredict = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            vibrateOnPredict.vibrate(vibrationPattern, -1);
        }
    }
}
