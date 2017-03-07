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

    private PowerManager.WakeLock mPowerLock;

    protected Scanner(ScannerCallback scannerCallback) {
        mScannerCallback = scannerCallback;
    }

    /**
     * This function updateStaleEntries is used by subclasses of Scanner.java to check for stale entries.
     * Pass the list of newly found data in toAdd to find the list of updated entries.
     *
     * @param toAdd     Each element in this List is a Pair that of an Object (which can be
     *                  any type returned by system services) and its timestamp (in a Long).
     * @return          Returns a List of Pair objects that were successfully updated.
     */
    protected List<Object> updateStaleEntries(final List<Pair<Object, Long>> toAdd) {
        List<Object> updated = new ArrayList<>();

        for(Pair<Object, Long> newItem : toAdd) {
            Object newItemKey = newItem.first;
            Long newItemTime = newItem.second;

            Long previousTime = mStaleEntries.get(newItemKey);

            if(previousTime == null || (previousTime < newItemTime))
            {
                mStaleEntries.put(newItemKey, newItemTime);
                updated.add(newItemKey);
            }
        }
        return updated;
    }


    public final void startScan() {
        mSetWakeLock(true);
        mStartScan();
    }

    public final void stopScan() {
        mSetWakeLock(false);
        mStopScan();
    }

    protected abstract boolean mStartScan();
    protected abstract boolean mStopScan();

    private final void mSetWakeLock(final boolean state) {

        final Context mContext = RF_Localizer_Application.getAppContext();
        final PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);

        if(mPowerLock == null) {
            // TODO: Look into depreciated PowerManager.FULL_WAKE_LOCK and possible replacement, if necessary
            mPowerLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "Scanner");
        }

        if(state && !mPowerLock.isHeld()) mPowerLock.acquire();
        if(!state && mPowerLock.isHeld()) mPowerLock.release();
    }
}

