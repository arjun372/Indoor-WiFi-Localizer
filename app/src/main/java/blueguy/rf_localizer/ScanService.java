package blueguy.rf_localizer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ScanService extends Service {

    private static final String TAG = "ScanService";

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
        // TODO :: Instantiate scanner objects here
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting scan service : start id " + startId + ": " + intent);

        // TODO : run scanners here, permanently.

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Destroying scan service");

        //TODO :: destroy all scanner objects. ensure scanned data gets dumped to file.

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
}
