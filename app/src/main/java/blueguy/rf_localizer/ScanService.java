package blueguy.rf_localizer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class ScanService extends Service {

    private static final String TAG = "ScanService";

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
        void onScanResult(List<Pair<Long, List<Object>>> dataList) {
            Log.d("callback", dataList.toString());
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
