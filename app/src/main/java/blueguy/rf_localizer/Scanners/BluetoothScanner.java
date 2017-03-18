package blueguy.rf_localizer.Scanners;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import blueguy.rf_localizer.utils.DataPair;

/**
 * Created by work on 3/6/17.
 */

public class BluetoothScanner extends Scanner {

    private static final String TAG = BluetoothScanner.class.getSimpleName();

    /* Beacon Configuration */
    static final byte[] Beacon_Mask                   = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,   0};
    //static final byte[] Beacon_Mask                   = {127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 0, 0, 0, 0,   0};
    private static final byte[] ESTIMOTE_manufac_data = {  2,  21, -71,  64, 127,  48, -11,  -8,  70, 110, -81, -15,  37,  85, 107,  87,  -2, 109, 0, 1, 0, 3, -74};
    private static final int    iBeacon_manID         = 76;

    private boolean isRegistered = false;

    private BluetoothLeScanner mBLEScanner;
    private static final List<ScanFilter> mScanFilters = Collections.singletonList(new ScanFilter.Builder().setManufacturerData(iBeacon_manID, ESTIMOTE_manufac_data, Beacon_Mask).build());
    private static final ScanSettings mScanSettings = new ScanSettings.Builder()
                                                 .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                                                 .build();

    public BluetoothScanner(ScannerCallback scannerCallback) {
        super(scannerCallback);
        mBLEScanner = getBLEScanner();
    }

    private List<DataObject> updateEntries(final List<ScanResult> iBeacons) {

        /** Make a List of DataObject objects from the WiFi getScanResults **/
        List<DataObject> beaconDataObjects = new ArrayList<>();
        for (final ScanResult iBeacon : iBeacons)
        {
            final long timestampFound = iBeacon.getTimestampNanos() / 1000000;

            final String ID         = iBeacon.getDevice().getAddress();
            final int dbm           = iBeacon.getRssi();
            final int txPowerLevel  = iBeacon.getScanRecord().getTxPowerLevel();

            List<DataPair> beaconDataVals = new ArrayList<>();

            beaconDataVals.add(new DataPair<>("dbm", dbm));
            beaconDataVals.add(new DataPair<>("tx_lvl", txPowerLevel));

            final DataObject newBeacon = new DataObject(timestampFound, ID, beaconDataVals);
            beaconDataObjects.add(newBeacon);
        }

        return updateStaleEntries(beaconDataObjects);
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            final List<ScanResult> results = Collections.singletonList(result);
            mScannerCallback.onScanResult(updateEntries(results));
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            mScannerCallback.onScanResult(updateEntries(results));
        }

        @Override
        public void onScanFailed(int errorCode) {}
    };

    @Override
    protected boolean mStartScan() {
        if (!isRegistered)
        {
            isRegistered = true;
            mBLEScanner.startScan(mScanCallback);
            Log.d(TAG, "started");
        }
        return false;
    }

    @Override
    protected boolean mStopScan() {
        if (isRegistered)
        {
            isRegistered = false;
            mBLEScanner.stopScan(mScanCallback);
            Log.d(TAG, "stopped");
        }
        return false;
    }

    private static BluetoothLeScanner getBLEScanner() {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!bluetoothAdapter.isEnabled()) bluetoothAdapter.enable();
        return bluetoothAdapter.getBluetoothLeScanner();
    }
}
