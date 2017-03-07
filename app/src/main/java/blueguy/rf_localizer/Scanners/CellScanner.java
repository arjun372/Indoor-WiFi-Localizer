package blueguy.rf_localizer.Scanners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import blueguy.rf_localizer.RF_Localizer_Application;

/**
 * Created by work on 2/22/17.
 */
public class CellScanner extends Scanner {

    private static final long pollingInterval = 500;

    private static final String TAG = "CellScanner";
    private static final String KEY_CELL_RSSI = "cell_rssi";

    private Handler mHandler = new Handler();

    private boolean isRegistered = false;

    public CellScanner(ScannerCallback scannerCallback) {
        super(scannerCallback);
    }

    @Override
    protected boolean mStartScan() {
        if (!isRegistered)
        {
            isRegistered = true;
            mHandler.post(requestScan);
            Log.d(TAG, "started scan handler");
        }
        return false;
    }

    @Override
    protected boolean mStopScan() {
        if (isRegistered)
        {
            isRegistered = false;
            mHandler.removeCallbacks(requestScan);
            Log.d(TAG, "stopped scan handler");
        }
        return false;
    }

    private Runnable requestScan = new Runnable() {
        @Override
        public void run() {

            /** Get scan results, remove stale entries, and fire callback **/
            final List<DataObject> cellTowers = getCellTowerInfo();
            final List<DataObject> updatedEntries = updateStaleEntries(cellTowers);

            mScannerCallback.onScanResult(updatedEntries);

            /** poll again in the given interval **/
            mHandler.postDelayed(requestScan, pollingInterval);
        }
    };

    private static List<DataObject> getCellTowerInfo() {

        List<DataObject> newCellTowers = new ArrayList<>();

        final Context context = RF_Localizer_Application.getAppContext();
        TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        final List<CellInfo> neighCells = tel.getAllCellInfo();

        for(final CellInfo singleCellInfo : neighCells) {

            final long timestamp = (singleCellInfo.getTimeStamp() / 1000000);
            final int identity;
            final int rssi;

            if(singleCellInfo instanceof CellInfoWcdma) {
                CellInfoWcdma info = (CellInfoWcdma) singleCellInfo;
                identity = info.getCellIdentity().getCid();
                rssi = info.getCellSignalStrength().getDbm();
            }

            else if (singleCellInfo instanceof CellInfoCdma) {
                CellInfoCdma info = (CellInfoCdma) singleCellInfo;
                identity  = info.getCellIdentity().getNetworkId();
                rssi      = info.getCellSignalStrength().getDbm();
            }

            else if (singleCellInfo instanceof CellInfoLte) {
                CellInfoLte info = (CellInfoLte) singleCellInfo;
                identity  = info.getCellIdentity().getCi();
                rssi      = info.getCellSignalStrength().getDbm();
            }

            else {
                CellInfoGsm info = (CellInfoGsm) singleCellInfo;
                identity  = info.getCellIdentity().getCid();
                rssi      = info.getCellSignalStrength().getDbm();
            }

            final Pair<String, Object> dataVals = new Pair<String, Object>(KEY_CELL_RSSI, rssi);
            final List<Pair<String, Object>> networkDataVals = new ArrayList<>(Collections.singletonList(dataVals));
            final DataObject newCellTower = new DataObject(timestamp, String.valueOf(identity), networkDataVals);
            newCellTowers.add(newCellTower);
        }

        return newCellTowers;
    }

}
