package blueguy.rf_localizer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blueguy.rf_localizer.Scanners.CellScanner;
import blueguy.rf_localizer.Scanners.DataObject;
import blueguy.rf_localizer.Scanners.Scanner;
import blueguy.rf_localizer.Scanners.ScannerCallback;
import blueguy.rf_localizer.Scanners.WifiScanner;
import blueguy.rf_localizer.utils.DataPair;
import blueguy.rf_localizer.utils.PersistentMemoryManager;

public class ScanService extends Service {

    private static final String TAG = "ScanService";
    private static final String CALLBACK = "ScanCallback";

    public static final String TAG_LOCATION = "location";
    public static final String TAG_TRAIN_ACTION= "train";

    private static final String KEY_TIMESTAMP = "timestamp";

    private static final String MAP_UNKNOWN = "nowhere";
    public static final String CLASS_UNKNOWN = "here";


    // TODO: Remove mDataBase HashMap
    /**
     * HashMap containing the data to write, whenever we want to use it.
     *
     * TODO: A new hash map needs to be created here for each new training/predicting environment
     *          Ex. Boelter Hall vs Engineering 6, BUT not for each room in each building
     *          Thus, this probably needs to be persistent somewhere.
     *
     */
    private static HashMap<String, List<Object>> mDataBase;// = new HashMap<>();

    public static HashMap<String, List<Object>> getDataBase() {
        return mDataBase;
    }

    //


    private DataObjectClassifier mCurrDataObjectClassifier = null;

    private List<DataPair<DataObject, String>> mAccumulatedDataAndLabels;
    private String mCurrLabel = CLASS_UNKNOWN;

    private String mLocation = MAP_UNKNOWN;

    public void setCurrLabel(String newCurrLabel) {
        mCurrLabel = newCurrLabel;
        Toast.makeText(this, "new label: " + mCurrLabel, Toast.LENGTH_SHORT).show();
    }

    public void resetCurrLabel() {
        setCurrLabel(CLASS_UNKNOWN);
    }

    private void loadClassifier(final String location, final boolean train) {
        mLocation = location;
        try {
            mCurrDataObjectClassifier = (DataObjectClassifier) PersistentMemoryManager.loadObjectFile(this, location);
            mAccumulatedDataAndLabels = train ? mCurrDataObjectClassifier.getRawData() : new ArrayList<>();
            Log.e(TAG, "successfully loaded mAccumlated labels [size] : " + mAccumulatedDataAndLabels.size());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            mCurrDataObjectClassifier = null;
            mAccumulatedDataAndLabels = new ArrayList<>();
        }
    }


    public void trainClassifier() {
        try {
            mCurrDataObjectClassifier = new DataObjectClassifier(mAccumulatedDataAndLabels, mLocation);
            PersistentMemoryManager.saveObjectFile(this, mLocation, mCurrDataObjectClassifier);
            Log.e(TAG, "successfully saved mAccumlated labels [size] : " + mAccumulatedDataAndLabels.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

////        Toast.makeText(this, "new location: " + mLocation, Toast.LENGTH_SHORT).show();
//        // TODO: Also need to load correct classifier from memory, or create a new one if necessary
//        mCurrDataObjectClassifier = null;
//        try {
//            mCurrDataObjectClassifier = (DataObjectClassifier) PersistentMemoryManager.loadObjectFile(this, mLocation);
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }


    /**
     * This List contains all the active scanners being polled for data.
     */
    private static List<Scanner> mScannerList;

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

        final String location = intent.getStringExtra(TAG_LOCATION);
        final Boolean train = intent.getBooleanExtra(TAG_TRAIN_ACTION, false);

        if ((location == null) || (location.isEmpty()))
        {
            throw new IllegalArgumentException("Valid location must be passed in Intent String extra with key: " + TAG_LOCATION);
        }
        else
        {
           loadClassifier(location, train);
        }

        resetCurrLabel();

        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();


    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        Toast.makeText(this, "onCreate: scanService", Toast.LENGTH_SHORT).show();
        mScannerList = mInitScanners();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        mScannerList = mRemoveScanners(mScannerList);

        Toast.makeText(this, "onDestroy: scanService", Toast.LENGTH_SHORT).show();
        /** TODO : ensure scanned data gets dumped to file. **/
    }

    private ScannerCallback mScannerCallback = new ScannerCallback() {
        @Override
        public void onScanResult(final List<DataObject> dataList) {
            if (mAccumulatedDataAndLabels == null)
            {
                mAccumulatedDataAndLabels = new ArrayList<>();
            }
            for (final DataObject dataObject : dataList)
            {
                final String current_label = String.valueOf(mCurrLabel);
                mAccumulatedDataAndLabels.add(new DataPair<>(dataObject, current_label));
            }
        }
    };

    private List<Scanner> mInitScanners() {
        Log.d(TAG, "initScanners");
        List<Scanner> curScanners = new ArrayList<>();

        curScanners.add(new WifiScanner(mScannerCallback));
        //curScanners.add(new CellScanner(mScannerCallback));
        //curScanners.add(new BluetoothScanner(mScannerCallback));
        //curScanners.add(new VelocityScanner(mScannerCallback));
        //curScanners.add(new RotationScanner(mScannerCallback));
        //curScanners.add(new MagneticFieldScanner(mScannerCallback));
        //curScanners.add(new PressureScanner(mScannerCallback));

        for(final Scanner scanner : curScanners)
        {
            scanner.startScan();
        }

        return curScanners;
    }

    private List<Scanner> mRemoveScanners(List<Scanner> currentScanners) {
        Log.d(TAG, "removeScanners");
        // TODO: Make sure this works

        // Stop scanning for each scanner and clear context to prevent memory leaks
        for (Scanner scanner : currentScanners) {
            // Stop scan
            scanner.stopScan();
        }

        mScannerList.removeAll(currentScanners);
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStart: " + startId + ":" + intent);

        // TODO : run scanners here, permanently.
        for (Scanner scanner : mScannerList) {
            scanner.startScan();
        }

        return START_NOT_STICKY;
    }

//    public Map<String, Double> predictOnData(long timeStart, long timeEnd) {
//        return n
//    }

    public Map<String, Double> predictOnData(final boolean accumulate) {
        final Map<String, Double> predictions = predict();
        if(!accumulate) mAccumulatedDataAndLabels = new ArrayList<>();
        return predictions;
    }

    private Map<String, Double> predict() {
        if(mCurrDataObjectClassifier == null) loadClassifier(mLocation, false);
        return mCurrDataObjectClassifier.classify(mAccumulatedDataAndLabels);
    }
}