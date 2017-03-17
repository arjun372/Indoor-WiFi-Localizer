package blueguy.rf_localizer.Scanners;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blueguy.rf_localizer.RF_Localizer_Application;

import static blueguy.rf_localizer.BuildConfig.DEBUG;

/**
 * Created by arjun on 2/14/17.
 */

public abstract class Scanner {

    private static final String TAG = "Scanner";
    private Map<Object, Long> mStaleEntries = Collections.synchronizedMap(new HashMap<Object, Long>());
    protected ScannerCallback mScannerCallback;

    private PowerManager.WakeLock mPowerLock;

    private long timestamp_bucketSize_Ms = 1000;

    protected Scanner(ScannerCallback scannerCallback) {
        mScannerCallback = scannerCallback;
    }

    /**
     * This function updateStaleEntries is used by subclasses of Scanner.java to check for stale entries.
     * Pass the list of newly found data in toAdd to find the list of updated entries.
     *
     * @param toAdd     Each element in this List is a Pair that of a DataObject (which can be
     *                  any type returned by system services) and its timestamp (in a Long).
     * @return          Returns a List of DataObjects that were successfully updated, and therefore are not stale.
     */
    protected synchronized List<DataObject> updateStaleEntries(final List<DataObject> toAdd) {

        List<DataObject> updated = new ArrayList<>();

        for(DataObject newItem : toAdd) {

            final Object key = newItem.mID;
            final Long currentTime  = newItem.mTimeStamp + RF_Localizer_Application.timeOfBoot - (newItem.mTimeStamp % timestamp_bucketSize_Ms);
            final Long previousTime = mStaleEntries.get(key);

            if(previousTime == null || (previousTime < currentTime))
            {
                final Long index = mStaleEntries.put(key, currentTime);
                if(index == null || !index.equals(currentTime))
                {
                    if(DEBUG) Log.d(TAG, currentTime + " @ "+key + " -> " + newItem.mDataVals.toString());
                    newItem.mTimeStamp = currentTime;
                    updated.add(newItem);
                }
            }

        }

        return updated;
    }


    public final synchronized void startScan() {
        mSetWakeLock(true);
        mStartScan();
    }

    public final synchronized void stopScan() {
        mSetWakeLock(false);
        mStopScan();
    }

    protected abstract boolean mStartScan();
    protected abstract boolean mStopScan();

    private final synchronized void mSetWakeLock(final boolean state) {

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

