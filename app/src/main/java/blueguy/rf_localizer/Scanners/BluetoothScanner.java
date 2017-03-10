package blueguy.rf_localizer.Scanners;

import android.os.Handler;
import android.util.Log;

/**
 * Created by work on 3/6/17.
 */

public class BluetoothScanner extends Scanner {

    public BluetoothScanner(ScannerCallback scannerCallback) {
        super(scannerCallback);
    }

    private static final long pollingInterval = 1000;

    private static final String TAG = "BluetoothScanner";

    private Handler mHandler = new Handler();

    private boolean isRegistered = false;

    @Override
    protected boolean mStartScan() {
        if (!isRegistered)
        {
            isRegistered = true;
            mHandler.post(requestScan);
            Log.d(TAG, "started");
        }
        return false;
    }

    @Override
    protected boolean mStopScan() {
        if (isRegistered)
        {
            isRegistered = false;
            mHandler.removeCallbacks(requestScan);
            Log.d(TAG, "stopped");
        }
        return false;
    }

    private Runnable requestScan = new Runnable() {
        @Override
        public void run() {

            // TODO :: add scanning code for stationary-type devices only

            /** poll again in the given interval **/
            mHandler.postDelayed(requestScan, pollingInterval);
        }
    };
}
