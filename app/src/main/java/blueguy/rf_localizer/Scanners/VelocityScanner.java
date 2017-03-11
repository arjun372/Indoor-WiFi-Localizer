package blueguy.rf_localizer.Scanners;

/**
 * Created by work on 3/10/17.
 */

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.Collections;
import java.util.List;

import blueguy.rf_localizer.RF_Localizer_Application;
import blueguy.rf_localizer.utils.DataPair;

public class VelocityScanner extends Scanner implements SensorEventListener {

    private static final int samplingFreqHz = 1;

    private static final int samplingDelayUs = 1000000/samplingFreqHz;
    private static final String TAG = "VelocityScanner";
    private boolean isRegistered = false;

    public VelocityScanner(ScannerCallback scannerCallback) {
        super(scannerCallback);
    }

    @Override
    protected boolean mStartScan() {
        if (!isRegistered) {
            final Context mContext = RF_Localizer_Application.getAppContext();
            final SensorManager sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
            final Sensor gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
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

        final String sensorType = "gyro";
        final Long timestamp = event.timestamp;
        final double velocity = getMagnitude(event.values);

//        Log.d(TAG, timestamp + ", " + sensorType + ", " + velocity);

        DataPair<String, Object> gyro_a = new DataPair<String, Object>("a", velocity);
        final DataObject sensorData = new DataObject(timestamp, sensorType, Collections.singletonList(gyro_a));
        final List<DataObject> updatedEntries = updateStaleEntries(Collections.singletonList(sensorData));
        mScannerCallback.onScanResult(updatedEntries);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private static double getMagnitude(final float[] a) {
        return Math.hypot(Math.hypot(a[0] , a[1]), a[2]);
    }
}