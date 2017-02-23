package blueguy.rf_localizer;

import android.app.Application;
import android.content.Context;

/**
 * Created by work on 2/22/17.
 */

public class RF_Localizer_Application extends Application {
    private static Context mContext;

    public void onCreate() {
        super.onCreate();
        synchronized (this) {
            mContext = getApplicationContext();
        }
    }

    public static Context getAppContext() {return mContext;}
}
