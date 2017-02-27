package blueguy.rf_localizer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import blueguy.rf_localizer.Scanners.DataObject;

/**
 * Created by Rahul on 2/27/2017.
 */

public class MainActivity extends AppCompatActivity {

    public static final String TAG_ACTIVITY = "MainActivity";

    private boolean mBounded;
    private ScanService mScanService;

    /**
     * Currently set to 10 second refresh interval.
     */
    private final int DATA_RETRIEVAL_INTERVAL = 1000 * 10;

    // Views
    private ListView mListView;
    private ArrayAdapter mListViewAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set content view layout
        setContentView(R.layout.activity_main);

        // Get Toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);

        // Get ListView
        mListView = (ListView) findViewById(R.id.ListView_data);
        mListViewAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mListViewAdapter.setNotifyOnChange(true);

        mListView.setAdapter(mListViewAdapter);

        Toast.makeText(this, "Testing", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, ScanService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    Handler mHandler = new Handler();

    Runnable mRefreshData = new Runnable() {
        @Override
        public void run() {

            Toast.makeText(MainActivity.this, "getting data", Toast.LENGTH_SHORT).show();

            // Get Data from service
            // TODO: look into if we even need mScanService
            HashMap<String, List<Object>> currData = ScanService.getDataBase();

            Log.d(TAG_ACTIVITY, "HashMap: " + currData.toString());

            List<String> currDataStrings = new ArrayList<>();
            for (String key : currData.keySet()) {
                DataObject dataObject = (DataObject) currData.get(key);
                for (Pair<String, Object> pair : dataObject.mDataVals) {
                    currDataStrings.add(dataObject.mID + pair.first);
                }
            }

            Log.d(TAG_ACTIVITY, "Strings: " + currDataStrings.toString());

            // Clear current list view adapter
            mListViewAdapter.clear();

            // Fill adapter with new list
            mListViewAdapter.addAll(currDataStrings);


            // Recall this soon (10 seconds)
            mHandler.postDelayed(mRefreshData, DATA_RETRIEVAL_INTERVAL);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        // Testing
        mRefreshData.run();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Testing
        mHandler.removeCallbacks(mRefreshData);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    // ScanService binding and connection
    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(MainActivity.this, "Service connected", Toast.LENGTH_SHORT).show();
            ScanService.LocalBinder localBinder = (ScanService.LocalBinder) service;
            mScanService = localBinder.getService();
            mBounded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(MainActivity.this, "Service disconnected", Toast.LENGTH_SHORT).show();
            mBounded = false;
            mScanService = null;
        }
    };
}
