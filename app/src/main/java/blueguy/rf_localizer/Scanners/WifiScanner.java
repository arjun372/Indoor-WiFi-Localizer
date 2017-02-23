package blueguy.rf_localizer.Scanners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

import blueguy.rf_localizer.RF_Localizer_Application;

/**
 * Created by work on 2/22/17.
 */
public class WifiScanner extends Scanner {

    private WifiManager.WifiLock wifiLock;

    private boolean isRegistered = false;

    private BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Context mContext = RF_Localizer_Application.getAppContext();
            final WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            final List<ScanResult> networks = wifiManager.getScanResults();
            //mScannerCallback.onScanResult();
        }
    };

    /**
     *
     * @param scannerCallback
     */
    public WifiScanner(ScannerCallback scannerCallback) {
        super(scannerCallback);
    }

    @Override
    public boolean StartScan() {
        if(!isRegistered) {
            final Context mContext = RF_Localizer_Application.getAppContext();
            mContext.registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            isRegistered = true;
        }
        return false;
    }

    @Override
    public boolean StopScan() {
        if(isRegistered) {
            final Context mContext = RF_Localizer_Application.getAppContext();
            mContext.unregisterReceiver(wifiScanReceiver);
            isRegistered = false;
        }
        return false;
    }

    /**
     *
     * @param state
     */
    private void setWifiLock(final boolean state) {

        final Context mContext = RF_Localizer_Application.getAppContext();
        final WifiManager wifiManager =  (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        if(wifiLock == null) {
            wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "WifiScanner");
        }

        if(state && !wifiLock.isHeld()) wifiLock.acquire();
        if(!state && wifiLock.isHeld()) wifiLock.release();
    }


}

class WifiScanReceiver extends BroadcastReceiver {

    private boolean registered = false;

    @Override
    public void onReceive(Context context, Intent intent) {
//            if(wM==null) wM =  (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
//            final List<ScanResult> networks = wM.getScanResults();
//            writeResults(networks);
    }


    public boolean isRegistered() {return registered;}
}