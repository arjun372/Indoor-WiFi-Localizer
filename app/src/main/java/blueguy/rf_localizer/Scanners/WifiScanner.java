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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import blueguy.rf_localizer.RF_Localizer_Application;

/**
 * Created by work on 2/22/17.
 */
public class WifiScanner extends Scanner {

    private static final long pollingInterval = 500;

    private static final String TAG = "WifiScanner";
    private static final String KEY_WIFI_RSSI = "rssi";

    private WifiManager.WifiLock wifiLock;

    private Handler mHandler = new Handler();

    private boolean isRegistered = false;

    public WifiScanner(ScannerCallback scannerCallback) {
        super(scannerCallback);
    }

    private BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            /** Get scan results, update entries, and fire callback **/
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final List<ScanResult> networks = wifiManager.getScanResults();
            final List<DataObject> updatedEntries = updateEntries(networks);
            mScannerCallback.onScanResult(updatedEntries);
        }
    };

    private List<DataObject> updateEntries(final List<ScanResult> networks) {

        /** Make a List of DataObject objects from the WiFi getScanResults **/
        List<DataObject> networkDataObjects = new ArrayList<>();
        for (final ScanResult network : networks)
        {
            final long timestampFound = network.timestamp/1000;
            final Pair<String, Object> dataVals = new Pair<String, Object>(KEY_WIFI_RSSI, network.level);
            final List<Pair<String, Object>> networkDataVals = new ArrayList<>(Collections.singletonList(dataVals));
            final DataObject newNetwork = new DataObject(timestampFound, network.BSSID, networkDataVals);
            networkDataObjects.add(newNetwork);
        }

        return updateStaleEntries(networkDataObjects);
    }

    @Override
    protected boolean mStartScan() {
        if (!isRegistered) {
            setWifiLock(true);
            final Context mContext = RF_Localizer_Application.getAppContext();
            mContext.registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            isRegistered = true;
            mHandler.post(requestScan);
            Log.d(TAG, "started scan handler");
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
            setWifiLock(false);
            Log.d(TAG, "stopped scan handler");
        }
        return false;
    }

    private Runnable requestScan = new Runnable() {
        @Override
        public void run() {

            /** Get scan results, update entries, and fire callback **/
            final Context context = RF_Localizer_Application.getAppContext();
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final List<ScanResult> networks = wifiManager.getScanResults();
            final List<DataObject> updatedEntries = updateEntries(networks);
            mScannerCallback.onScanResult(updatedEntries);

            /** Send intent to start scan again **/
            wifiManager.startScan();

            /** poll again in the given interval **/
            mHandler.postDelayed(requestScan, pollingInterval);
        }
    };

    /**
     * @param state
     */
    private void setWifiLock(final boolean state) {

        final Context mContext = RF_Localizer_Application.getAppContext();
        final WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        if (wifiLock == null) {
            wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, TAG);
        }

        if (state && !wifiLock.isHeld()) wifiLock.acquire();
        if (!state && wifiLock.isHeld()) wifiLock.release();
    }


}