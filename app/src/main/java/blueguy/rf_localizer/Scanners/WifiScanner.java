package blueguy.rf_localizer.Scanners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import blueguy.rf_localizer.RF_Localizer_Application;

/**
 * Created by work on 2/22/17.
 */
public class WifiScanner extends Scanner {

    public static final String KEY_WIFI_RSSI = "rssi";

    private WifiManager.WifiLock wifiLock;

    private boolean isRegistered = false;

    private BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Context mContext = RF_Localizer_Application.getAppContext();
            final WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            final List<ScanResult> networks = wifiManager.getScanResults();

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

            // Now send updated data to scanner callback for processing
            mScannerCallback.onScanResult(updatedEntries);

            //
        }
    };

    /**
     * @param scannerCallback
     */
    public WifiScanner(ScannerCallback scannerCallback) {
        super(scannerCallback);
    }

    @Override
    protected boolean mStartScan() {
        if (!isRegistered) {
            final Context mContext = RF_Localizer_Application.getAppContext();
            mContext.registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            isRegistered = true;
        }
        return false;
    }

    @Override
    protected boolean mStopScan() {
        if (isRegistered) {
            final Context mContext = RF_Localizer_Application.getAppContext();
            mContext.unregisterReceiver(wifiScanReceiver);
            isRegistered = false;
        }
        return false;
    }

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