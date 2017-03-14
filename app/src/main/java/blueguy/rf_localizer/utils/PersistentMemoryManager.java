package blueguy.rf_localizer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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

    public static final Object loadObjectFile(final String classifierName) throws IOException, ClassNotFoundException {
        final File inputFile = new File(Environment.getExternalStorageDirectory(), classifierName+".dat");
        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(inputFile));
        Object object = objectInputStream.readObject();
        objectInputStream.close();
        return object;
    }

    public static final void saveObjectFile(final String classifierName, Object object) throws IOException {
        final File outputFile = new File(Environment.getExternalStorageDirectory(), classifierName+".dat");
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
        objectOutputStream.writeObject(object);
        objectOutputStream.close();
    }
}
