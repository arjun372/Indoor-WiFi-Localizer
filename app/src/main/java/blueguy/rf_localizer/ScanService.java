package blueguy.rf_localizer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import blueguy.rf_localizer.Scanners.DataObject;
import blueguy.rf_localizer.Scanners.Scanner;
import blueguy.rf_localizer.Scanners.ScannerCallback;
import blueguy.rf_localizer.Scanners.WifiScanner;

public class ScanService extends Service {

    private static final String TAG = "ScanService";
//    private static final String FS_rootDirectory = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

    /**
     * HashMap containing the data to write, whenever we want to use it.
     *
     * TODO: A new hash map needs to be created here for each new training/predicting environment
     *          Ex. Boelter Hall vs Engineering 6, BUT not for each room in each building
     *          Thus, this probably needs to be persistent somewhere.
     *
     */
    private static HashMap<String, List<Object>> mDataBase;// = new HashMap<>();
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String VAL_UNKNOWN = "?";

    public static HashMap<String, List<Object>> getDataBase() {
        return mDataBase;
    }

    /**
     * This List contains all the active scanners being polled for data.
     */
    private static List<Scanner> mScannerList;

    /**
     * mAddToDataBase is a helper function that takes into account the structure of the HashMap,
     *      which links String keys to lists of any Object values (with toString implemented).
     *      Specifically:
     *          • Creates a new HashMap if mDataBase is not initialized
     *          • Then adds the value to the list corresponding to the given key in the HashMap if
     *              it already exists in mDataBase.
     *              • Otherwise, a new list is created with the given value and put in mDataBase
     *
     * @param key       The String denoting the key to be used when putting into the mDataBase.
     * @param value     The Object value to put into the mDataBase with given key.
     */
    private static synchronized void mAddToDataBase(String key, Object value) {
        // If the mDataBase is null, create a new empty one for it
        if (mDataBase == null) {
            mDataBase = new HashMap<>();
        }

        // If the mDataBase already contains the list for this key, then add this value to it
        if (mDataBase.containsKey(key)) {
            mDataBase.get(key).add(value);
        }
        // Otherwise, create a new list initialized with the given value and input it in the hashmap
        else {
            mDataBase.put(key, new ArrayList<Object>(Collections.singletonList(value)));
        }
    }

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        public ScanService getService() {
            return ScanService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();


    @Override
    public void onCreate() {
        mScannerList = mInitScanners();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Destroying scan service");

        mScannerList = mRemoveScanners(mScannerList);

        /** TODO : ensure scanned data gets dumped to file. **/
    }

    private static ScannerCallback mScannerCallback = new ScannerCallback() {
        @Override
        public void onScanResult(List<DataObject> dataList) {

            for (DataObject dataObject : dataList) {
                // TODO: Check: Need to add to each list in the hash map, mDataBase, based on the concatenated id and dataval id, where the rest empty are question marks

                // Start keeping track of feature names that were updated so the rest can be filled with unknowns
                Set<String> unUpdatedKeys = (mDataBase == null) ? (new HashSet<String>()) : (new HashSet<>(mDataBase.keySet()));

                // First, push the timestamp on to the HashMap for this new data row
                ScanService.mAddToDataBase(KEY_TIMESTAMP, dataObject.mTimeStamp);
                unUpdatedKeys.remove(KEY_TIMESTAMP);


                Log.d("callback", dataObject.mDataVals.toString());

                // Add each data value to the mDataBase HashMap
                for (Pair<String, Object> dataPair : dataObject.mDataVals) {

                    ScanService.mAddToDataBase(dataObject.mID + "_" + dataPair.first, dataPair.second);

                    // Remove this feature name from unUpdatedKeys
                    unUpdatedKeys.remove(dataObject.mID + "_" + dataPair.first);
                }

                // For each unUpdatedKey, fill in with unknown value, '?'
                for (String key : unUpdatedKeys) {
                    ScanService.mAddToDataBase(key, VAL_UNKNOWN);
                }
            }
        }
    };

    private List<Scanner> mInitScanners() {

        List<Scanner> curScanners = new ArrayList<>();

        curScanners.add(new WifiScanner(mScannerCallback));
//        curScanners.add(new CellScanner(mScannerCallback));
//        curScanners.add(new VelocityScanner(mScannerCallback));
//        curScanners.add(new AltitudeScanner(mScannerCallback));
//        curScanners.add(new RotationScanner(mScannerCallback));

        for(Scanner scanner : curScanners) {
            scanner.startScan();
        }

        return curScanners;
    }

    private List<Scanner> mRemoveScanners(List<Scanner> currentScanners) {
        // TODO: Make sure this works

        // Stop scanning for each scanner and clear context to prevent memory leaks
        for (Scanner scanner : currentScanners) {
            // Stop scan
            scanner.stopScan();

            // Clear context
//            scanner.clearContext();
        }

        mScannerList.removeAll(currentScanners);
        //
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting scan service : start id " + startId + ": " + intent);

        // TODO : run scanners here, permanently.
//        for (Scanner scanner : mScannerList) {
//            scanner.startScan();
//        }

        return START_NOT_STICKY;
    }
}
