package blueguy.rf_localizer.Scanners;

import android.content.Context;
import android.os.Handler;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.NeighboringCellInfo;
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

    private static final long pollingInterval = 1000;

    private static final String TAG = "CellScanner";

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

        /** Poll the current API now **/
        final List neighCells = tel.getAllCellInfo();

        /** Poll the deprecated API first, timestamp values are uncertain **/
        final List<?> neighboringCellInfos = tel.getNeighboringCellInfo();

        neighCells.addAll(neighboringCellInfos);

        for(Object singleCellInfo :neighCells) {

            long timestamp = System.currentTimeMillis() - RF_Localizer_Application.timeOfBoot;
            String identity = "cell_tower";

            int dbm = Integer.MIN_VALUE;
            int lvl  = Integer.MIN_VALUE;
            int asu_lvl = Integer.MIN_VALUE;

            int cdma_dbm  = Integer.MIN_VALUE;
            int cdma_lvl  = Integer.MIN_VALUE;
            int cdma_ecio = Integer.MIN_VALUE;

            int evdo_dbm  = Integer.MIN_VALUE;
            int evdo_lvl  = Integer.MIN_VALUE;
            int evdo_ecio = Integer.MIN_VALUE;
            int evdo_snr  = Integer.MIN_VALUE;

            int timing_advance = Integer.MIN_VALUE;

            if (singleCellInfo instanceof NeighboringCellInfo)
            {
                final NeighboringCellInfo info = (NeighboringCellInfo) singleCellInfo;
                identity = String.valueOf(info.getCid());
                asu_lvl = info.getRssi();
                dbm = -113 + (2*asu_lvl);
            }

            if(singleCellInfo instanceof CellInfoWcdma)
            {
                final CellInfoWcdma info = (CellInfoWcdma) singleCellInfo;
                timestamp = (info.getTimeStamp() / 1000000);
                identity = info.getCellIdentity().toString();

                dbm = info.getCellSignalStrength().getDbm();
                lvl = info.getCellSignalStrength().getLevel();
                asu_lvl = info.getCellSignalStrength().getAsuLevel();
            }

            if (singleCellInfo instanceof CellInfoCdma)
            {
                CellInfoCdma info = (CellInfoCdma) singleCellInfo;
                timestamp = (info.getTimeStamp() / 1000000);
                identity = info.getCellIdentity().toString();

                dbm = info.getCellSignalStrength().getDbm();
                lvl = info.getCellSignalStrength().getLevel();
                asu_lvl = info.getCellSignalStrength().getAsuLevel();

                cdma_dbm = info.getCellSignalStrength().getCdmaDbm();
                cdma_lvl = info.getCellSignalStrength().getCdmaLevel();
                cdma_ecio = info.getCellSignalStrength().getCdmaEcio();

                evdo_dbm = info.getCellSignalStrength().getEvdoDbm();
                evdo_lvl = info.getCellSignalStrength().getEvdoLevel();
                evdo_snr = info.getCellSignalStrength().getEvdoSnr();
                evdo_ecio = info.getCellSignalStrength().getEvdoEcio();
            }

            if (singleCellInfo instanceof CellInfoLte)
            {
                CellInfoLte info = (CellInfoLte) singleCellInfo;
                timestamp = (info.getTimeStamp() / 1000000);
                identity = info.getCellIdentity().toString();

                dbm = info.getCellSignalStrength().getDbm();
                lvl = info.getCellSignalStrength().getLevel();
                asu_lvl = info.getCellSignalStrength().getAsuLevel();

                timing_advance = info.getCellSignalStrength().getTimingAdvance();
            }

            if (singleCellInfo instanceof CellInfoGsm)
            {
                CellInfoGsm info = (CellInfoGsm) singleCellInfo;
                timestamp = (info.getTimeStamp() / 1000000);
                identity = info.getCellIdentity().toString();

                dbm = info.getCellSignalStrength().getDbm();
                lvl = info.getCellSignalStrength().getLevel();
                asu_lvl = info.getCellSignalStrength().getAsuLevel();
            }

            List<Pair<String, Object>> dataVector = new ArrayList<>();

            dataVector.add(new Pair<String, Object>("dbm", dbm));
            dataVector.add(new Pair<String, Object>("asu_lvl", asu_lvl));
            dataVector.add(new Pair<String, Object>("lvl", lvl));

            dataVector.add(new Pair<String, Object>("cdma_dbm", cdma_dbm));
            dataVector.add(new Pair<String, Object>("cdma_lvl", cdma_lvl));
            dataVector.add(new Pair<String, Object>("cdma_ecio", cdma_ecio));

            dataVector.add(new Pair<String, Object>("evdo_dbm", evdo_dbm));
            dataVector.add(new Pair<String, Object>("evdo_lvl", evdo_lvl));
            dataVector.add(new Pair<String, Object>("evdo_ecio", evdo_ecio));
            dataVector.add(new Pair<String, Object>("evdo_snr", evdo_snr));

            dataVector.add(new Pair<String, Object>("timing_adv", timing_advance));

            final DataObject newCellTower = new DataObject(timestamp, identity, dataVector);
            newCellTowers.add(newCellTower);
        }

        return newCellTowers;
    }

}
