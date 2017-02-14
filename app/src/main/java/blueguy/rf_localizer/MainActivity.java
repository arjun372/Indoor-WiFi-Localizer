package blueguy.rf_localizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends WearableActivity {

    private static String current_label = "unknown";
    private Context mContext;
    private logWifiTask wifiLogger;
    private static Handler poller = new Handler();

    private static WifiManager wM = null;
    private static WifiManager.WifiLock  wLock = null;
    private static PowerManager.WakeLock pLock = null;

    private static final String FS_rootDirectory = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();
    }

    public void onRadioButtonClicked(View view) {
        if(!(((RadioButton) view).isChecked()))
            return;
        current_label = ((RadioButton) view).getText().toString();
        Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(50);
    }

    @Override
    protected void onResume(){
        super.onResume();
        mContext = getApplicationContext();
        startScan();
    }

    @Override
    protected void onPause(){
        super.onPause();
        stopScan();
    }

    private void startScan() {
        setWakeLock(true);
        registerReceiver(scanResultReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        poller.post(requestScan);
    }

    private void stopScan() {
        unregisterReceiver(scanResultReceiver);
        poller.removeCallbacks(requestScan);
        setWakeLock(false);
    }

    private static boolean PAUSE;
    private static List<String> lastScanResult  = new ArrayList<>();

    private class logWifiTask extends AsyncTask<Void, Void, Void> {

        protected void onPreExecute() {
            Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(1000);
        }

        protected void onPostExecute() {
            Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(new long[]{1000,1000,1000,1000}, -1);
        }

        @Override
        protected Void doInBackground(Void... params) {


            onPostExecute();
            return null;
        }
    }

    private Runnable requestScan = new Runnable() {
        @Override
        public void run() {
            Log.v("requestScan", "requesting");
            if(wM==null) wM = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            wM.startScan();
            poller.postDelayed(requestScan, 3000);
            final List<ScanResult> networks = wM.getScanResults();
            writeResults(networks);
        }
    };
    private void setWakeLock(final boolean state) {

        if(wM==null) wM =  (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if(wLock == null) wLock = wM.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, "networkLogger");

        final PowerManager pM = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        if(pLock == null) pLock = pM.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "networkLogger");

        if(state) {
            wLock.acquire();
            pLock.acquire();

            while(!wM.isWifiEnabled())
                wM.setWifiEnabled(true);

        } else {
            wLock.release();
            pLock.release();
        }
    }

    private BroadcastReceiver scanResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(wM==null) wM =  (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            final List<ScanResult> networks = wM.getScanResults();
            writeResults(networks);
        }
    };

    private void writeResults(final List<ScanResult> networks) {
        if(!networks.isEmpty()) {
            Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(200);
        }

        for(ScanResult network : networks) {

            final String line = network.timestamp + "," + network.BSSID + "," + network.level + "," + current_label+"\n";

            if(lastScanResult.contains(line)) {
                Log.v("networkLogger", "Removing repeating entry: "+line);
            }
            else {
                lastScanResult.add(line);
                try {
                    Log.d("networkLogger", line);
                    final FileWriter writer = new FileWriter(new File(FS_rootDirectory, current_label+".csv"), true);
                    writer.write(line);
                    writer.flush();
                    writer.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
