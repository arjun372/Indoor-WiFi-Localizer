package blueguy.rf_localizer.Scanners;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import blueguy.rf_localizer.RF_Localizer_Application;
import blueguy.rf_localizer.utils.DataPair;

/**
 * Created by work on 3/10/17.
 */

public class MagneticFieldScanner extends Scanner implements SensorEventListener {

    private static final int samplingFreqHz = 1;

    private static final int samplingDelayUs = 1000000/samplingFreqHz;
    private static final String TAG = "MagneticFieldScanner";
    private boolean isRegistered = false;

    public MagneticFieldScanner(ScannerCallback scannerCallback) {
        super(scannerCallback);
    }

    @Override
    protected boolean mStartScan() {
        if (!isRegistered) {
            final Context mContext = RF_Localizer_Application.getAppContext();
            final SensorManager sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
            final Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            sensorManager.registerListener(this, sensor, samplingDelayUs);
            isRegistered = true;
            Log.d(TAG, "started");
        }
        return false;
    }

    @Override
    protected boolean mStopScan() {
        if (isRegistered) {
            final Context mContext = RF_Localizer_Application.getAppContext();
            final SensorManager sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
            sensorManager.unregisterListener(this);
            isRegistered = false;
            Log.d(TAG, "stopped");
        }
        return false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        final String sensorType = "magnetic";
        final Long timestamp = event.timestamp;

        List<DataPair> features = new ArrayList<>();
        features.add(new DataPair<>("a", getMagnitude(event.values)));
        features.add(new DataPair<>("x", event.values[0]));
        features.add(new DataPair<>("y", event.values[1]));
        features.add(new DataPair<>("z", event.values[2]));

        final DataObject sensorData = new DataObject(timestamp, sensorType, features);
        final List<DataObject> updatedEntries = updateStaleEntries(Collections.singletonList(sensorData));
        mScannerCallback.onScanResult(updatedEntries);
    }

    private static double getMagnitude(final float[] a) {
        return Math.hypot(Math.hypot(a[0] , a[1]), a[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}