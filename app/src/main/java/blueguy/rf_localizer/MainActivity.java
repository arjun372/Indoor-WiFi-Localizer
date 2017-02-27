package blueguy.rf_localizer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements SensorEventListener{

    private static long count = 0;
    private static String fileTime="";
    private Context mContext;
    private static String current_label = "unknown";
    private static Handler poller = new Handler();

    private static WifiManager wM = null;
    private static WifiManager.WifiLock  wLock = null;
    private static PowerManager.WakeLock pLock = null;

    private static HashMap<String, Long> staleNetworks = new HashMap<>();

    private static List<String> lastScanResult_Wifi = new ArrayList<>();
    private static List<String> lastScanResult_Cell = new ArrayList<>();

    private static final String FS_rootDirectory = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

//    class WriterCallback {
//        public void performWrite(long timestamp, String featureID, String value) {
//
//        }
//    }
//
//    onCallBack(asdasd) {
//        write
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setAmbientEnabled();
    }

    public void onButtonClicked(View view) {

        String new_label = ((EditText) findViewById(R.id.label_value)).getText().toString();

        if(new_label.equals(current_label))
            return;

        if(new_label.equals(""))
            new_label = "unknown";

        current_label = new_label;
        Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(50);
        count = -1;
        updateCount();
    }

    @Override
        protected void onResume(){
            super.onResume();

            Log.v("onResume", "Resumed activity!");

            /** reset all counters **/
            fileTime = new SimpleDateFormat("HH:mm", Locale.US).format(new Date());
            count = 0;

            lastScanResult_Wifi  = new ArrayList<>();
            lastScanResult_Cell  = new ArrayList<>();

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
        setGyroscope(true);
        poller.post(requestScan);
    }

    private void stopScan() {
        setGyroscope(false);
        unregisterReceiver(scanResultReceiver);
        poller.removeCallbacks(requestScan);
        setWakeLock(false);
    }

    private void setGyroscope(final boolean state) {
        final SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        final Sensor gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        try {
            if(state) {
                mSensorManager.registerListener(this, gyroscope, 1000000, 0);
            } else {
                mSensorManager.unregisterListener(this);
            }
        } catch (Exception e) {
            Log.e("setGyro", "Unable to perform gyroscope operation!"+e);
        }
    }

    private static void writeGyro(final String line) {
        try {
            final File targetFolder = new File(FS_rootDirectory+"/"+fileTime);
            targetFolder.mkdirs();
            final FileWriter writer = new FileWriter(new File(targetFolder, "gyro.csv"), true);
            writer.write(line);
            writer.flush();
            writer.close();
        }  catch (IOException e) {
            Log.e("writeGyro", "Unable to write to file:gyro");
        }
    }

    private Runnable requestScan = new Runnable() {
        @Override
        public void run() {
            Log.v("requestScan", "requesting");
            if(wM==null) wM = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            wM.startScan();
            final List<ScanResult> networks = wM.getScanResults();
            writeResults(networks);
            poller.postDelayed(requestScan, 100);
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


    private synchronized void writeResults(final List<ScanResult> networks) {

        List<String> allInfo = getCellTowerInfo();
        List<String> wifiInfo = getWifiAPInfo(networks);
        allInfo.addAll(wifiInfo);

        try {
            final File targetFolder = new File(FS_rootDirectory+"/"+fileTime);
            targetFolder.mkdirs();
            final FileWriter writer = new FileWriter(new File(targetFolder, "labeled.csv"), true);

            for(String singleLine : allInfo) {
                writer.write(singleLine);
                updateCount();
            }

            writer.flush();
            writer.close();

        }  catch (IOException e) {
            Log.e("writeResults", "Unable to write to file!");
        }
    }

    private static List<String> getWifiAPInfo(final List<ScanResult> networks) {
        List<String> newWiFiInfo = new ArrayList<>();

        for(ScanResult network: networks) {
            final String line = (network.timestamp/1000)+ "," + network.BSSID + "," + network.level + "," + current_label+"\n";
            if(!lastScanResult_Wifi.contains(line)) {
                lastScanResult_Wifi.add(line);
                newWiFiInfo.add(line);
            }
        }

        return newWiFiInfo;
    }

    private synchronized void updateCount() {
        TextView view = (TextView) findViewById(R.id.count);
        view.setText(current_label+":"+(++count));
    };

    private List<String> getCellTowerInfo() {

        TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        List<String> newCellTowers = new ArrayList<>();

        final List<CellInfo> neighCells = tel.getAllCellInfo();

        for(CellInfo singleCellInfo : neighCells) {
            int identity = 0;
            long timestamp = 0;
            int rssi = 0;

            if(singleCellInfo instanceof CellInfoWcdma) {
                CellInfoWcdma info = (CellInfoWcdma) singleCellInfo;
                identity = info.getCellIdentity().getCid();
                timestamp = (info.getTimeStamp() / 1000000);
                rssi = info.getCellSignalStrength().getDbm();
            }

            else if (singleCellInfo instanceof CellInfoCdma) {
                CellInfoCdma info = (CellInfoCdma) singleCellInfo;
                identity  = info.getCellIdentity().getNetworkId();
                timestamp = info.getTimeStamp();
                rssi      = info.getCellSignalStrength().getDbm();
            }

            else {
                CellInfoLte info = (CellInfoLte) singleCellInfo;
                identity  = info.getCellIdentity().getCi();
                timestamp = info.getTimeStamp();
                rssi      = info.getCellSignalStrength().getDbm();
            }

            String line = timestamp + "," + identity + "," + rssi + "," + current_label+"\n";
            if(!lastScanResult_Cell.contains(line)) {
                lastScanResult_Cell.add(line);
                newCellTowers.add(line);
            }
        }

        return newCellTowers;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final double x2 = event.values[0]*event.values[0];
        final double y2 = event.values[1]*event.values[1];
        final double z2 = event.values[2]*event.values[2];
        final double gyroMag = Math.sqrt(x2+y2+z2);
        final String velocity = String.format(Locale.US, "%.4f", gyroMag);
        final String line = (event.timestamp/1000000) +"," + velocity + "\n";
        writeGyro(line);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
//for(ScanResult network : networks) {
//final String line = network.timestamp + "," + network.BSSID + "," + network.level + "," + current_label+"\n";
//
//        if(lastScanResult_Wifi.contains(line)) {
//        //Log.v("networkLogger", "Removing repeating entry: "+line);
//        }
//        else {
//
//        try {
////Log.d("networkLogger", line);
//final File targetFolder = new File(FS_rootDirectory+"/"+fileTime);
//        targetFolder.mkdirs();
//final FileWriter writer = new FileWriter(new File(targetFolder, "labeled.csv"), true);
//        writer.write(line);
//        writer.flush();
//        writer.close();
//        lastScanResult_Wifi.add(line);
//        updateCount();
//        }
//        catch (IOException e) {
//        e.printStackTrace();
//        }
//
//        }
//        }