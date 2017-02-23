package blueguy.rf_localizer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import blueguy.rf_localizer.Scanners.DataObject;
import blueguy.rf_localizer.Scanners.Scanner;
import blueguy.rf_localizer.Scanners.ScannerCallback;
import blueguy.rf_localizer.Scanners.WifiScanner;

public class ScanService extends Service {

    private static final String TAG = "ScanService";
    private static final String FS_rootDirectory = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

    // HashMap containing the data to write, whenever we want
    // TODO: A new hash map needs to be created here for each new training/predicting environment
    //          Ex. Boelter Hall vs Engineering 6, BUT not for each room in each building
    //          Thus, this probably needs to be persistent somewhere.
    private HashMap<String, List<Object>> mDataBase = new HashMap<>();

    private static List<Scanner> scannerList;
    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        ScanService getService() {
            return ScanService.this;
        }
    }

    @Override
    public void onCreate() {
        scannerList = initScanners();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Destroying scan service");

        scannerList = removeScanners(scannerList);

        /** TODO : ensure scanned data gets dumped to file. **/
    }

    private static ScannerCallback mScannerCallback = new ScannerCallback() {
        @Override
//        public void onScanResult(List<Pair<Long, List<Object>>> dataList) {
        public void onScanResult(List<DataObject> dataList) {
            Log.d("callback", dataList.toString());

//            List<String> allInfo = getCellTowerInfo();
//            List<String> wifiInfo = getWifiAPInfo(networks);
//            allInfo.addAll(wifiInfo);

            String testFolderName = "testingFolder";

            try {
                final File targetFolder = new File(FS_rootDirectory+"/"+testFolderName);
                targetFolder.mkdirs();
                final FileWriter writer = new FileWriter(new File(targetFolder, "labeled.csv"), true);

                for (DataObject dataObject : dataList) {
                    // TODO: Need to create add to each list in the hash map, mDataBase, based on the concatenated id and dataval id, where the rest empty are question marks
                }

                writer.flush();
                writer.close();

            }  catch (IOException e) {
                Log.e("writeResults", "Unable to write to file!");
            }
        }
    };

    private static List<Scanner> initScanners() {

        List<Scanner> curScanners = new ArrayList<>();

        curScanners.add(new WifiScanner(mScannerCallback));
//        curScanners.add(new CellScanner(mScannerCallback));
//        curScanners.add(new VelocityScanner(mScannerCallback));
//        curScanners.add(new AltitudeScanner(mScannerCallback));
//        curScanners.add(new RotationScanner(mScannerCallback));

        for(Scanner single : curScanners) {
            single.startScan();
        }

        return curScanners;
    }

    private static List<Scanner> removeScanners(List<Scanner> currentScanners) {

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting scan service : start id " + startId + ": " + intent);

        // TODO : run scanners here, permanently.

        return START_NOT_STICKY;
    }



    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
}
