package blueguy.rf_localizer;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
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
 * Activities that contain this fragment must implement the
 * {@link IndoorMap.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link IndoorMap#newInstance} factory method to
 * create an instance of this fragment.
 */

/**
 * An activity that displays a Google map with polylines to represent paths or routes,
 * and polygons to represent areas.
 */

public class Fragment_IndoorMap extends Fragment implements SensorEventListener{

    private static final String KEY_LOCATION = ScanService.TAG_LOCATION;

    private Handler mPredictionRequestHandler = new Handler();
    private static final Long predictionTimeoutHistoryMs = 3000L;

    private static final long[] vibrationPattern = new long[] {0L, 500L, 0L};

    private Button yesButton, noButton;
    private ScatterChart mChart;

    private int labelIdx = 0;
    private static final String UNKNOWN = "Calculating...";

    private TextView predictLabelTextView;
    private String mCurrLocation;

    public Fragment_IndoorMap() {
        // Required empty public constructor
    }

    public static Fragment_IndoorMap newInstance(String location) {
        Fragment_IndoorMap fragment = new Fragment_IndoorMap();
        Bundle args = new Bundle();
        args.putString(KEY_LOCATION, location);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrLocation = getArguments().getString(KEY_LOCATION);
        ((MainActivity)getActivity()).bindScanService(mCurrLocation, true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((MainActivity)getActivity()).unbindScanService();
    }

//    @Override
//    public void onPause() {
//        super.onPause();
//        // TODO :: What happens when you exit navigation?
//        ((MainActivity)getActivity()).mScanService.resetCurrLabel();
//        mPredictionRequestHandler.removeCallbacks(mPredictionRequest);
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        // TODO :: What happens when you start navigation?
//        //updateLabel(UNKNOWN);
//        mPredictionRequestHandler.postDelayed(mPredictionRequest, predictionTimeoutHistoryMs);
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_indoor_map, container, false);
        mChart = (ScatterChart) rootView.findViewById(R.id.chart1);
        initMapChart();

        //TextView predictingLocationTextView = (TextView) rootView.findViewById(R.id.predicting_screen_location_text_view);
        //predictingLocationTextView.setText(getArguments().getString(KEY_LOCATION));

//        Button finishPredictingButton = (Button) rootView.findViewById(R.id.button_finish_predicting);
//        finishPredictingButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO: Do any necessary finishing ScanService stuff for predictions
//                getActivity().onBackPressed();
//            }
//        });


        // TODO: Update this label as necessary according to the ScanService predictions
//        predictLabelTextView = (TextView) rootView.findViewById(R.id.predicting_label_text);


        return rootView;
    }



//    private Runnable mPredictionRequest = new Runnable() {
//        @Override
//        public void run() {
//            final Long now = System.currentTimeMillis();
//            final Long past = now - predictionTimeoutHistoryMs;
//            //final DataPair<List<DataPair<DataObject, String>>, Map<String, Double>> distributionsWithData = ((MainActivity)getActivity()).mScanService.predictOnData(false);
//            final Map<String, Double> distributions = distributionsWithData.second;
//
//            setRadarData(distributions);
//
//            if(DEBUG)
//            {
//                for(final String location : distributions.keySet())
//                {
//                    Log.d("PREDICTIONS", location + " : " + distributions.get(location));
//                }
//            }
//
//            final String predictedLabel = Collections.max(distributions.entrySet(), Map.Entry.comparingByValue()).getKey();
//            updateLabel(predictedLabel);
//
//            yesButton.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                    final String currentLabel = predictLabelTextView.getText().toString();
//                    List<DataPair<DataObject, String>> unLabeledData = distributionsWithData.first;
//                    for(DataPair singleData : unLabeledData) singleData.second = currentLabel;
//                    ((MainActivity)getActivity()).mScanService.updateClassifierData(unLabeledData);
//                }
//            });
//
//            noButton.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                    final List<String> labels = new ArrayList<>(distributions.keySet());
//                    labelIdx = (labelIdx >= labels.size()) ? 0 : labelIdx+1;
//
//                    if(labelIdx < labels.size())
//                        updateLabel(labels.get(labelIdx));
//                }
//            });
//
//            mPredictionRequestHandler.postDelayed(mPredictionRequest, predictionTimeoutHistoryMs);
//        }
//    };

    private void initMapChart() {

        //mChart.setBackgroundColor(Color.rgb(60, 65, 82));
        mChart.getDescription().setEnabled(false);
//        mChart.setWebLineWidth(2f);
//        mChart.setWebColor(Color.DKGRAY);
//        mChart.setWebLineWidthInner(1.5f);
//        mChart.setWebColorInner(Color.DKGRAY);
//        mChart.setWebAlpha(100);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(8f);
        xAxis.setYOffset(0f);
        xAxis.setXOffset(0f);

        //YAxis yAxis = mChart.getYAxis();
//        yAxis.setTextSize(8f);
//        yAxis.setAxisMinimum(0f);
//        yAxis.setDrawLabels(false);

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

        //mChart.setData(data);
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

    private void syncBearing(final boolean state) {
        final SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        final Sensor orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        if(state)
            sensorManager.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_GAME);
        else
            sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.sensor.getType() == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
        {
            float[] rotMatrix = new float[9];
            float[] orientation = new float[3];
            SensorManager.getRotationMatrixFromVector(rotMatrix, event.values);
            SensorManager.getOrientation(rotMatrix, orientation);
            final Double bearing = Math.toDegrees(orientation[0]);
            final Double bearingChange = getPercentageDifference(bearing, currentBearing);
            currentBearing = bearing;
            Log.v("setbearing", "Bearing changed by : "+bearingChange+", Setting bearing to :"+bearing);
            updateCamera(bearing.floatValue());
        }
    }

    private static final double BEARING_CHANGE_REDRAW_THRESHOLD = 0.0005;
    private boolean bearingUpdating = false;
    private Double currentBearing = 0.0;

    private static Double getPercentageDifference(final double first, final double second) {
        return 100.0*Math.abs(first-second);///(first > second ? first : second);
    }

    private synchronized void updateCamera(final float bearing) {

        if(bearingUpdating)
            return;

        bearingUpdating = true;
        //final CameraPosition oldPos = mGoogleMap.getCameraPosition();
        //final CameraPosition newPos = CameraPosition.builder(oldPos).bearing(bearing).build();
        //mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(newPos));
//        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(newPos), 5, new GoogleMap.CancelableCallback() {
//            @Override
//            public void onFinish() {
//                bearingUpdating = false;
//            }
//
//            @Override
//            public void onCancel() {
//                bearingUpdating = false;
//            }
//        });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
