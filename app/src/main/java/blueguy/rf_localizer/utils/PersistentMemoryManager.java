package blueguy.rf_localizer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Rahul on 3/7/2017.
 */

public class PersistentMemoryManager {

    private static final String KEY_LOCATIONS_LIST = "locations_list";


    private static final void putStringCollection(Context context, String key, Collection<String > vals) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Set<String> stringSet = new HashSet<String>(vals);
        editor.putStringSet(key, stringSet);
        editor.commit();
    }

    private static final Set<String> getStringList(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return  sharedPreferences.getStringSet(key, new HashSet<String>());
    }

    public static final Set<String> getLocationsList(Context context) {
        return getStringList(context, KEY_LOCATIONS_LIST);
    }

    public static final void updateLocationsList(Context context, String newLocation) {
        Set<String> prevSet = getLocationsList(context);
        prevSet.add(newLocation);
        putStringCollection(context, KEY_LOCATIONS_LIST, prevSet);
    }
}
