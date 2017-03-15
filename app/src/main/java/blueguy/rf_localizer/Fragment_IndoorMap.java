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

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;

import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import blueguy.rf_localizer.Scanners.DataObject;
import blueguy.rf_localizer.Scanners.Scanner;
import blueguy.rf_localizer.Scanners.ScannerCallback;
import blueguy.rf_localizer.Scanners.WifiScanner;
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


    private static final String TAG = "Predicting_Fragment";

    private static final long[] newLocationVibration = new long[] {500L, 0L, 500L};

    private static final Long updatePredictionVibration = 200L;

    /** prediction related */
    private static final Long predictionTimeoutHistoryMs = 10000L;
    private Handler mPredictionRequestHandler = new Handler();
    private static final boolean ACCUMULATE = false;

    /** GUI related **/
    private ScatterChart mChart;

    /** indoor-map related **/
    private List<DataPair<DataObject, String>> mAccumulatedDataAndLabels;
    private IndoorMap mIndoorMap;

    /** scanner related **/
    private List<Scanner> mScannerList;
    private ScannerCallback mScannerCallback = new ScannerCallback() {
        @Override
        public void onScanResult(final List<DataObject> dataList) {
            if (mAccumulatedDataAndLabels == null) mAccumulatedDataAndLabels = new ArrayList<>();
            mAccumulatedDataAndLabels.addAll(dataList.stream().map(dataObject -> new DataPair<>(dataObject, DataObjectClassifier.CLASS_UNKNOWN)).collect(Collectors.toList()));
        }
    };

    private int labelIdx = 0;

    private Runnable mPredictionRequest = new Runnable() {
        @Override
        public void run() {
            final Long now = System.currentTimeMillis();
            final Long past = now - predictionTimeoutHistoryMs;
            final DataPair<List<DataPair<DataObject, String>>, Map<String, Double>> distributionsWithData = mIndoorMap.predictOnData(mAccumulatedDataAndLabels);
            final Map<String, Double> distributions = distributionsWithData.second;
            if(!ACCUMULATE) mAccumulatedDataAndLabels.clear();

            if(DEBUG)
            {
                for(final String location : distributions.keySet())
                {
                    Log.d("PREDICTIONS", location + " : " + distributions.get(location));
                }
            }

//            String predictedLabel = "error";
//            final Double maxValue = Collections.max(distributions.values());
//            for(final String label : distributions.keySet()) if(maxValue.equals(distributions.get(label))) predictedLabel = label;

            int index = 0;
            Double max = 0.0;

            final Double[] values = (Double[]) distributions.values().toArray();

            for(int i = 0; i < values.length; i++)
            {
               if(values[i] > max)
               {
                   index = i;
               }
            }

            updateLocation(index);
            mPredictionRequestHandler.postDelayed(mPredictionRequest, predictionTimeoutHistoryMs);
        }
    };

    public Fragment_IndoorMap() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Fragment_PredictingScreen.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_IndoorMap newInstance(String location) {
        Fragment_IndoorMap fragment = new Fragment_IndoorMap();
        Bundle args = new Bundle();
        args.putString(IndoorMap.TAG_LOCATION, location);
        args.putBoolean(IndoorMap.TAG_TRAIN_ACTION, false);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.activity_indoor_map, container, false);

        mChart = (ScatterChart) rootView.findViewById(R.id.chart1);

        initRadarChart();

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        syncBearing(false);
        mPredictionRequestHandler.removeCallbacks(mPredictionRequest);
        removeScanners();
        resetPredictedLabel();
        mAccumulatedDataAndLabels.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAccumulatedDataAndLabels = new ArrayList<>();
        resetPredictedLabel();
        initIndoorMap();
        initScanners();
        syncBearing(true);
        mPredictionRequestHandler.postDelayed(mPredictionRequest, predictionTimeoutHistoryMs);
    }

    private void initRadarChart() {

        mChart.getDescription().setEnabled(false);
        mChart.setBackgroundColor(Color.TRANSPARENT); //set whatever color you prefer
        mChart.setDrawGridBackground(false);

//        mChart.setWebLineWidth(2f);
//        mChart.setWebColor(Color.DKGRAY);
//        mChart.setWebLineWidthInner(1.5f);
//        mChart.setWebColorInner(Color.DKGRAY);
//        mChart.setWebAlpha(100);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(0f);
        xAxis.setYOffset(0f);
        xAxis.setXOffset(0f);

//        YAxis yAxis = mChart.getYAxis();
//        yAxis.setTextSize(8f);
//        yAxis.setAxisMinimum(0f);
//        yAxis.setDrawLabels(false);

        ArrayList<Entry> locations = new ArrayList<>();
        locations.add(new Entry(0.15F, 0.15F));
        locations.add(new Entry(0.25F, 0.15F));
        locations.add(new Entry(0.45F, 0.95F));
        locations.add(new Entry(0.55F, 0.95F));
        locations.add(new Entry(0.35F, 0.95F));
        locations.add(new Entry(0.0F, 0.95F));
//        locations.add(new Entry(0.0F, 0.95F));
//        locations.add(new Entry(0.0F, 0.95F));

        ScatterDataSet set1 = new ScatterDataSet(locations, "");
        set1.setColor(Color.RED, 180);//.rgb(121, 162, 175));
        set1.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        set1.setScatterShapeSize(64f);
        set1.setScatterShapeHoleColor(Color.BLUE);
        set1.setScatterShapeHoleRadius(1f);
        set1.setHighlightEnabled(true);

        //.setFillColor(Color.rgb(103, 110, 129));)
        set1.setDrawHighlightIndicators(false);

        ArrayList<IScatterDataSet> sets = new ArrayList<>();
        sets.add(set1);

        ScatterData data = new ScatterData(sets);
        data.setValueTextSize(16f);
        data.setDrawValues(false);
        data.setValueTextColor(Color.WHITE);

        mChart.setData(data);
        mChart.animateXY(500, 500, Easing.EasingOption.EaseInOutCirc, Easing.EasingOption.EaseInOutCirc);

    }

    private void updateLocation(final int index) {
        Log.e("onLocation", "UPDATING LOCATION MOFO :: "+index);
    }

    private void setRadarData(final Map<String, Double> predictions) {

//        ArrayList<Entry> predictVals = new ArrayList<>();
//
//        for(final String label : predictions.keySet())
//        {
//            predictVals.add(new RadarEntry(predictions.get(label).floatValue(), label));
//        }
//
//        ScatterDataSet set1 = new ScatterDataSet(predictVals, "LALALA");
//        set1.setColor(Color.RED, 180);//.rgb(121, 162, 175));
//        set1.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
//        set1.setScatterShapeSize(64f);
//        set1.setScatterShapeHoleColor(Color.BLUE);
//        set1.setScatterShapeHoleRadius(18f);
//        set1.setHighlightEnabled(true);
//
//       //.setFillColor(Color.rgb(103, 110, 129));)
//        set1.setDrawHighlightIndicators(false);
//
//        XAxis xAxis = mChart.getXAxis();
//        xAxis.setValueFormatter(new IAxisValueFormatter() {
//
//            private final String[] mActivities = predictions.keySet().toArray(new String[predictions.keySet().size()]);
//
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                return mActivities[(int) value % mActivities.length];
//            }
//        });
//
//        ArrayList<IScatterDataSet> sets = new ArrayList<>();
//        sets.add(set1);
//
//        ScatterData data = new ScatterData(sets);
//        data.setValueTextSize(16f);
//        data.setDrawValues(false);
//        data.setValueTextColor(Color.WHITE);
//
//        mChart.setData(data);
//        mChart.animateXY(500, 500, Easing.EasingOption.EaseInOutCirc, Easing.EasingOption.EaseInOutCirc);
    }

    private void resetPredictedLabel() {updatePredictedLabelSilent(DataObjectClassifier.CLASS_UNKNOWN);}
    private void updatePredictedLabel(final String newLabel) {
        if(updatePredictedLabelSilent(newLabel)) {
            Vibrator vibrateOnPredict = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            vibrateOnPredict.vibrate(newLocationVibration, -1);
        }
    }
    private boolean updatePredictedLabelSilent(final String newLabel) {

//        final String currentLabel = predictLabelTextView.getText().toString();
//        if(!currentLabel.equals(newLabel))
//        {
//            predictLabelTextView.setText(newLabel);
//            return true;
//        }
        return false;
    }

    private void initScanners() {
        if(DEBUG) Log.d(TAG, "initScanners");
        this.mScannerList = new ArrayList<>();
        this.mScannerList.add(new WifiScanner(mScannerCallback));
        //curScanners.add(new CellScanner(mScannerCallback));
        //curScanners.add(new BluetoothScanner(mScannerCallback));
        //curScanners.add(new VelocityScanner(mScannerCallback));
        //curScanners.add(new RotationScanner(mScannerCallback));
        //curScanners.add(new MagneticFieldScanner(mScannerCallback));
        //curScanners.add(new PressureScanner(mScannerCallback));
        for(Scanner x : this.mScannerList)
        {
            x.startScan();
        }
    }
    private void removeScanners() {
        if(DEBUG) Log.d(TAG, "removeScanners");
        for(Scanner x : this.mScannerList)
        {
            x.stopScan();
        }
        this.mScannerList.clear();
    }
    private void initIndoorMap() {
        final String indoorMapName = getArguments().getString(IndoorMap.TAG_LOCATION);
        this.mIndoorMap = new IndoorMap(indoorMapName);
    }
    private void syncBearing(final boolean state) {
        final SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        final Sensor orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        final Sensor orientationSensor2 = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if(state) {
            sensorManager.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this, orientationSensor2, SensorManager.SENSOR_DELAY_FASTEST);
        }
        else
            sensorManager.unregisterListener(this);
    }

    float gyroBearing = 0.0F;

    @Override
    public synchronized void onSensorChanged(SensorEvent event) {

        float[] rotMatrix = new float[9];
        float[] orientation = new float[3];
        SensorManager.getRotationMatrixFromVector(rotMatrix, event.values);
        SensorManager.getOrientation(rotMatrix, orientation);
        final Double bearing = Math.toDegrees(orientation[0]);

        if(event.sensor.getType() == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
        {
            final float calibratedBearing = (bearing.floatValue() + gyroBearing)/2;
            mChart.setRotation(calibratedBearing);
        }

        if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR)
        {
            gyroBearing = bearing.floatValue();
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}