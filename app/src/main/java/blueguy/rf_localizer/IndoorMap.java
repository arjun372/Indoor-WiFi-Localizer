package blueguy.rf_localizer;

import android.util.Log;

import org.jgrapht.ListenableGraph;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import blueguy.rf_localizer.Scanners.DataObject;
import blueguy.rf_localizer.utils.DataPair;
import blueguy.rf_localizer.utils.PersistentMemoryManager;

/**
 * Created by arjun on 3/14/17.
 */

public class IndoorMap implements Serializable{

    private static final String TAG = IndoorMap.class.getSimpleName();

    public static final String TAG_LOCATION     = "location";
    public static final String TAG_TRAIN_ACTION = "train";

    private List<DataPair<DataObject, String>> mRawData;

    private DataObjectClassifier mClassifer;

    private ListenableGraph mVisualGraph;

    private File mImageFile;

    public String getMapName() {
        return mMapName;
    }

    private String mMapName;

    public IndoorMap(String mapName) {
        this.mMapName     = mapName;
      //  this.mVisualGraph = loadGraphFromRawFiles();
      //  this.mImageFile   = loadImageFromRawFile();

        this.mRawData   = loadRawDataFromFile(this.mMapName);
        this.mClassifer = buildClassifier(this.mRawData, this.mMapName);
    }

    public IndoorMap(String mapName, ListenableGraph mVisualGraph, File imageFile) {
        this.mMapName = mapName;
        this.mVisualGraph = mVisualGraph;
        this.mImageFile = imageFile;
    }

    private static DataObjectClassifier buildClassifier(final List<DataPair<DataObject, String>> dataWithLabels, final String clfName) {
        return new DataObjectClassifier(dataWithLabels, clfName);
    }


    public void finishTraining(final List<DataPair<DataObject, String>> dataWithLabels) {
        Log.v(TAG, "Training on :" + dataWithLabels.toString());

        retrainWithData(dataWithLabels);

        saveRawDataToFile(this.mMapName);

       // Log.v(TAG, "Saving to ARFF");
      //  this.mClassifer.InstancesToArff();

        //TODO :: do everything that scanservice.trainclassifier does
        //TODO :: plus save yourself, who knows when you will be back?
    }

    public DataPair<List<DataPair<DataObject, String>>, Map<String, Double>> predictOnData(final List<DataPair<DataObject, String>> dataWithLabels) {
        Log.v(TAG, "Predicting");
        final Map<String, Double> predictions = this.mClassifer.classify(dataWithLabels);
        return new DataPair<> (dataWithLabels, predictions);
    }

    public void retrainWithData(final List<DataPair<DataObject, String>> dataWithLabels) {
        this.mRawData.addAll(dataWithLabels);
        try {
            this.mClassifer.update(dataWithLabels);
            Log.v("retrainWithData", "Was able to update the classifier without rebuilding. Good job bud");
        } catch(Exception e) {
            Log.d("retrainWithData", "Unable to retrain, saw some new attributes, rebuilding");
            this.mClassifer = buildClassifier(this.mRawData, this.mMapName);
        }
        // also save raw data list to file -> you need to make the classifier persist.
        saveRawDataToFile(this.mMapName);
    }

    private static List<DataPair<DataObject, String>> loadRawDataFromFile(final String filename) {
        try {
            return (List<DataPair<DataObject, String>>) PersistentMemoryManager.loadObjectFile(filename + ".raw");
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    private void saveRawDataToFile(final String filename) {
        try {
            PersistentMemoryManager.saveObjectFile(filename + ".raw", this.mRawData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
