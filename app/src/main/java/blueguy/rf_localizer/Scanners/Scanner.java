package blueguy.rf_localizer.Scanners;

import android.content.Context;
import android.os.PowerManager;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import blueguy.rf_localizer.RF_Localizer_Application;

/**
 * Created by arjun on 2/14/17.
 */

public abstract class Scanner {

    private HashMap<Object, Long> mStaleEntries = new HashMap<>();
    protected ScannerCallback mScannerCallback;

    private PowerManager.WakeLock powerLock;

    protected Scanner(ScannerCallback scannerCallback) {
        mScannerCallback = scannerCallback;
    }

    /**
     *
     * @return
     */
    protected List<Object> updateStaleEntries(final List<Pair<Object, Long>> toAdd) {
        List<Object> updated = new ArrayList<>();

        for(Pair<Object, Long> singleItem : toAdd) {

            Long previousValue = mStaleEntries.get(singleItem.first);

            if(previousValue == null || (previousValue < singleItem.second))
            {
                mStaleEntries.put(singleItem.first, singleItem.second);
                updated.add(singleItem.first);
            }
        }
        return updated;
    }


    public void startScan() {
        setWakeLock(true);
        StartScan();
    }

    public void stopScan() {
        setWakeLock(false);
        StopScan();
    }

    protected abstract boolean StartScan();
    protected abstract boolean StopScan();

    private void setWakeLock(final boolean state) {

        final Context mContext = RF_Localizer_Application.getAppContext();
        final PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);

        if(powerLock == null) {
            powerLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "Scanner");
        }

        if(state && !powerLock.isHeld()) powerLock.acquire();
        if(!state && powerLock.isHeld()) powerLock.release();
    }
}

