package blueguy.rf_localizer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import blueguy.rf_localizer.DataObjectClassifier;

/**
 * Created by Rahul on 3/7/2017.
 */

public class PersistentMemoryManager {

    private static final String KEY_LOCATIONS_LIST = "locations_list";


    private static final void putStringCollection(Context context, String key, Collection<String > vals) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Set<String> stringSet = new HashSet<>(vals);
        editor.putStringSet(key, stringSet);
        editor.commit();
    }

    private static final Set<String> getStringSet(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return new HashSet<>(sharedPreferences.getStringSet(key, new HashSet<String>()));
    }

    public static final Set<String> getLocationsList(Context context) {
        return getStringSet(context, KEY_LOCATIONS_LIST);
    }

    public static final void updateLocationsList(Context context, String newLocation) {
        Set<String> prevSet = getLocationsList(context);
        prevSet.add(newLocation);
        putStringCollection(context, KEY_LOCATIONS_LIST, prevSet);
    }

    public static final DataObjectClassifier loadClassifier(Context context, String classifierName) throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = context.openFileInput(classifierName);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        DataObjectClassifier dataObjectClassifier = (DataObjectClassifier) objectInputStream.readObject();
        objectInputStream.close();
        fileInputStream.close();
        return dataObjectClassifier;
    }

    public static final void saveClassifier(Context context, String classifierName, DataObjectClassifier dataObjectClassifier) throws IOException {
        FileOutputStream fileOutputStream = context.openFileOutput(classifierName, Context.MODE_PRIVATE);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(dataObjectClassifier);
        objectOutputStream.close();
        fileOutputStream.close();
    }
}
