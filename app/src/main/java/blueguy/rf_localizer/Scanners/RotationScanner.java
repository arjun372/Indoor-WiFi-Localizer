package blueguy.rf_localizer.Scanners;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import blueguy.rf_localizer.RF_Localizer_Application;
import blueguy.rf_localizer.utils.DataPair;

/**
 * Created by work on 3/10/17.
 */

public class RotationScanner extends Scanner implements SensorEventListener {

    private static final int samplingFreqHz = 1;

    private static final int samplingDelayUs = 1000000/samplingFreqHz;
    private static final String TAG = "RotationScanner";
    private boolean isRegistered = false;

    public RotationScanner(ScannerCallback scannerCallback) {
        super(scannerCallback);
    }

    @Override
    protected boolean mStartScan() {
        if (!isRegistered) {
            final Context mContext = RF_Localizer_Application.getAppContext();
            final SensorManager sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
            final Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
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

        final String sensorType = "rot_vect";
        final Long timestamp = event.timestamp;

        List<DataPair<String, Object>> features = new ArrayList<>();
        features.add(new DataPair<String, Object>("x", event.values[0]));
        features.add(new DataPair<String, Object>("y", event.values[1]));
        features.add(new DataPair<String, Object>("z", event.values[2]));

        final DataObject sensorData = new DataObject(timestamp, sensorType, features);
        final List<DataObject> updatedEntries = updateStaleEntries(Collections.singletonList(sensorData));
        mScannerCallback.onScanResult(updatedEntries);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}