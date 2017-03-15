package blueguy.rf_localizer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public ScanService mScanService;
    private boolean mBounded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set Layout
        // Set content view layout
        setContentView(R.layout.activity_main);

        // Get Toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);

        // Create fragment and inflate
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.main_fragment_container, new Fragment_TitleScreen())
                .commit();
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

    public void bindScanService(final String location, final boolean train) {
        Intent intent = new Intent(this, ScanService.class);
        intent.putExtra(ScanService.TAG_LOCATION, location);
        intent.putExtra(ScanService.TAG_TRAIN_ACTION, train);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    public void unbindScanService() {
        unbindService(mConnection);
        Intent intent = new Intent(this, ScanService.class);
        stopService(intent);
    }
}