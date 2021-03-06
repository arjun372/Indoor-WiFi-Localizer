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

public class PressureScanner extends Scanner implements SensorEventListener {

    private static final int samplingFreqHz = 1;

    private static final int samplingDelayUs = 1000000/samplingFreqHz;
    private static final String TAG = "PressureScanner";
    private boolean isRegistered = false;

    public PressureScanner(ScannerCallback scannerCallback) {
        super(scannerCallback);
    }

    @Override
    protected boolean mStartScan() {
        if (!isRegistered) {
            final Context mContext = RF_Localizer_Application.getAppContext();
            final SensorManager sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
            final Sensor gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            sensorManager.registerListener(this, gyroscope, samplingDelayUs);
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

        final String sensorType = "baro";
        final Long timestamp = event.timestamp;

        List<DataPair> features = new ArrayList<>();
        features.add(new DataPair<>("p", event.values[0]));

        final DataObject sensorData = new DataObject(timestamp, sensorType, features);
        final List<DataObject> updatedEntries = updateStaleEntries(Collections.singletonList(sensorData));
        mScannerCallback.onScanResult(updatedEntries);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}