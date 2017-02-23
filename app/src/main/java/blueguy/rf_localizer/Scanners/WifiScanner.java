package blueguy.rf_localizer.Scanners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

import blueguy.rf_localizer.RF_Localizer_Application;

/**
 * Created by work on 2/22/17.
 */
public class WifiScanner extends Scanner {

    private WifiManager.WifiLock wifiLock;

    private BroadcastReceiver scanResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            if(wM==null) wM =  (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
//            final List<ScanResult> networks = wM.getScanResults();
//            writeResults(networks);
        }
    };

    public WifiScanner(ScannerCallback scannerCallback) {
        super(scannerCallback);
    }

    @Override
    public boolean StartScan() {
        return false;
    }

    @Override
    public boolean StopScan() {
        return false;
    }

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
