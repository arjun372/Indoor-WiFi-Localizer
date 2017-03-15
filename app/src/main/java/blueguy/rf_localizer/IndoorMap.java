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

    private List<DataPair<DataObject, String>> mCurrentRawData;

    private DataObjectClassifier mClassifer;

    private ListenableGraph mVisualGraph;

    private File mImageFile;

    public String getMapName() {
        return mMapName;
    }

    private String mMapName;

    private String mLastPrediction;

    public String getCurrentLocation() {
        return mLastPrediction;
    }

    public IndoorMap(String mapName) {
        this.mMapName = mapName;
        this.mClassifer = loadClassifierFromFile(this.mMapName);
    }

    public IndoorMap(String mapName, ListenableGraph mVisualGraph, File imageFile) {
        this.mMapName = mapName;
        this.mVisualGraph = mVisualGraph;
        this.mImageFile = imageFile;
        //this.mClassifer =
    }

    private void buildClassifier(final List<DataPair<DataObject, String>> dataWithLabels) {
        this.mClassifer = new DataObjectClassifier(dataWithLabels, this.mMapName);
    }

    private static DataObjectClassifier loadClassifierFromFile(final String mapName) {
        try {
             return (DataObjectClassifier) PersistentMemoryManager.loadObjectFile(mapName+".clf");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void finishTraining(final List<DataPair<DataObject, String>> dataWithLabels) {
        Log.v(TAG, "Training on :" + dataWithLabels.toString());
        //TODO :: do everything that scanservice.trainclassifier does
        //TODO :: plus save yourself, who knows when you will be back?
    }

    public DataPair<List<DataPair<DataObject, String>>, Map<String, Double>> predictOnData(final List<DataPair<DataObject, String>> dataWithLabels) {
        Log.v(TAG, "Predicting on :" + dataWithLabels.toString());
        return null;
    }

    public void retrainWithData(final List<DataPair<DataObject, String>> dataWithLabels) {
       // TODO :: Implement this !
        // ((MainActivity)getActivity()).mScanService.updateClassifierData(unLabeledData);
    }


}
