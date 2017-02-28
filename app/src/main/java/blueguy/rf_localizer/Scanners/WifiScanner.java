package blueguy.rf_localizer.Scanners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import blueguy.rf_localizer.RF_Localizer_Application;

/**
 * Created by work on 2/22/17.
 */
public class WifiScanner extends Scanner {

    public static final String TAG = "WifiScanner";

    public static final String KEY_WIFI_RSSI = "rssi";

    private WifiManager.WifiLock wifiLock;
    private WifiManager mWifiManager;

    private Handler mHandler = new Handler();

    private boolean isRegistered = false;

    private BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "Reached wifiScanReceiver");

            final Context localContext = RF_Localizer_Application.getAppContext();
            final WifiManager wifiManager = (WifiManager) localContext.getSystemService(Context.WIFI_SERVICE);
            final List<ScanResult> networks = wifiManager.getScanResults();

            Log.d(TAG, "wifiScanReceiver getScanResults: " + networks.toString());

            // TODO: Make sure this works
            // Make a List of DataObject objects from the WiFi getScanResults
            List<Pair<DataObject, Long>> networkDataObjects = new ArrayList<>();
            for (ScanResult network : networks) {
                networkDataObjects.add(
                        new Pair<>(
                                new DataObject(
                                        network.timestamp,
                                        network.BSSID,
                                        new ArrayList<>(
                                                Arrays.asList(new Pair<String, Object>(KEY_WIFI_RSSI, network.level))
                                        )
                                ),
                                network.timestamp
                        )
                );
            }

            // Try to update the stale entries, and get updated list to send to scan callback
            // NOTE: Doing a lot of unchecked casting here
            List<DataObject> updatedEntries = (List<DataObject>) (List<?>) updateStaleEntries((List<Pair<Object, Long>>) (List<?>) networkDataObjects);

            Log.d(TAG, "wifiScanReceiver updatedEntries: " + updatedEntries.toString());

            // Now send updated data to scanner callback for processing
            mScannerCallback.onScanResult(updatedEntries);

            //
        }
    };


    public WifiScanner(ScannerCallback scannerCallback) {
        super(scannerCallback);
    }

    @Override
    protected boolean mStartScan() {
        Log.d("WifiScanner", "starting scan");
        if (!isRegistered) {
            final Context mContext = RF_Localizer_Application.getAppContext();
            mContext.registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            isRegistered = true;
            mHandler.post(requestScan);
        }
        return false;
    }

    @Override
    protected boolean mStopScan() {
        if (isRegistered) {
            final Context mContext = RF_Localizer_Application.getAppContext();
            mContext.unregisterReceiver(wifiScanReceiver);
            isRegistered = false;
            mHandler.removeCallbacks(requestScan);
        }
        return false;
    }

    private Runnable requestScan = new Runnable() {
        @Override
        public void run() {
            Log.v("requestScan", "requesting");
            if(mWifiManager==null) {
                mWifiManager = (WifiManager) RF_Localizer_Application.getAppContext().getSystemService(Context.WIFI_SERVICE);
            }
            mWifiManager.startScan();

            final List<ScanResult> networks = mWifiManager.getScanResults();
            Log.d(TAG, "Got scan results in runnable: " + networks.toString());
//            writeResults(networks);
            mHandler.postDelayed(requestScan, 100);
        }
    };

    /**
     * @param state
     */
    private void setWifiLock(final boolean state) {

        final Context mContext = RF_Localizer_Application.getAppContext();
        final WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        if (wifiLock == null) {
            wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "WifiScanner");
        }

        if (state && !wifiLock.isHeld()) wifiLock.acquire();
        if (!state && wifiLock.isHeld()) wifiLock.release();
    }


}