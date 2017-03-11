package blueguy.rf_localizer;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import blueguy.rf_localizer.Scanners.DataObject;
import blueguy.rf_localizer.utils.DataPair;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import static blueguy.rf_localizer.BuildConfig.DEBUG;

/**
 * Created by work on 3/6/17.
 */

public class DataObjectClassifier implements Serializable{

    private static final long serialVersionUID = 3L;

    private List<DataPair<DataObject, String>> labeled_data;
    private Classifier classifier;
    private String mClassifierName;


    private static final String ATTRIBUTE_TIMESTAMP = "timestamp";
    private static final String ATTRIBUTE_CLASS = "class";

    public List<DataPair<DataObject, String>> getLabeled_data() {
        return labeled_data;
    }

    public DataObjectClassifier(final List<DataPair<DataObject, String>> dataWithLabels, String locationName) throws Exception{

        /* save data for future use */
        final Instances dataInstances = convertDataObjectToInstances(dataWithLabels, locationName);
//        this.classifier = buildClassifier(dataInstances);
        this.labeled_data = dataWithLabels;
        mClassifierName = locationName;
    }

    /**
     *
     * @param data
     * @return
     */
    public List<DataPair<String, Double>> classify(final List<DataObject> data) {

        return new ArrayList<>();
    }

    /**
     *
     * @param labeledData
     * @return
     */
    public List<DataPair<String, Double>> evaluate(final List<DataPair<DataObject, String>> labeledData) {
        return new ArrayList<>();
    }

    private static void addToInstance(Instance instance, int attributeIndex, Long value) {

    }

    public static Instances convertDataObjectToInstances(final List<DataPair<DataObject, String>> dataWithLabels, String classifierName) throws Exception{
        //return new Instances();
       // create hashmap wherer the key is the name of the feature and value is an integer which is an index of the column number of that feature

        HashMap<String, Integer> featureColumnIndex = new HashMap<>();
        HashSet<String> classLabels = new HashSet<>();

        int indexCount = 0;
        featureColumnIndex.put(ATTRIBUTE_TIMESTAMP, indexCount++);
        featureColumnIndex.put(ATTRIBUTE_CLASS, indexCount++);

        for (DataPair<DataObject, String> dataPair : dataWithLabels) {

            // Add current class label to set
            classLabels.add(dataPair.second);

            for (DataPair<String, Object> dataValPair : dataPair.first.mDataVals) {
                String key = DataObject.concatFeatureID(dataPair.first.mID, dataValPair.first);
                if (!featureColumnIndex.containsKey(key)) {
                    featureColumnIndex.put(key, indexCount++);
                }
            }
        }

//        Log.e("supdawg", "convertDataObjectToInstances: " + featureColumnIndex.size());

        List<Instance> instanceList = new ArrayList<>();
        for (DataPair<DataObject, String> dataPair : dataWithLabels) {
            final String currLabel = dataPair.second;
            final Long timestamp = dataPair.first.mTimeStamp;
            final String feat_prefix = dataPair.first.mID;
            Collection<String> keys = new HashSet<>(featureColumnIndex.keySet());
            final int numColumns = keys.size();
            Instance instance = new DenseInstance(numColumns);

            addToInstance(instance, featureColumnIndex.get(ATTRIBUTE_TIMESTAMP), timestamp);
            keys.remove(ATTRIBUTE_TIMESTAMP);

//            addToInstance(instance, featureColumnIndex.get(ATTRIBUTE_CLASS), currLabel);
//            keys.remove(ATTRIBUTE_CLASS);

            for (DataPair<String, Object> dataValPair : dataPair.first.mDataVals) {

            }

        }

        // TODO: populate instance list using featureColumnIndex HashMap

        ArrayList<Attribute> attributeList = new ArrayList<>();
        for (String featureName : featureColumnIndex.keySet()) {
            attributeList.add(new Attribute(featureName));
        }


        Instances dataInstances = new Instances(classifierName, attributeList, 0);
        dataInstances.addAll(instanceList);

        dataInstances.setClassIndex(featureColumnIndex.get(ATTRIBUTE_CLASS));


        return null;
    }

    @Override
    public String toString() {
        return "DataObjectClassifier{" +
                "labeled_data=" + labeled_data +
                ", classifier=" + classifier +
                '}';
    }
//    public void updateClassifier(final Instances newInstances) {
//        for(final Instance instance : newInstances) {
//            try {
//                this.classifier.updateClassifier(instance);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
////        ((weka.classifiers.Classifier) this.classifier).distributionForInstance();
//    }
//
    private static weka.classifiers.Classifier buildClassifier(final Instances structure) {
        NaiveBayesUpdateable naiveBayesUpdateable = new NaiveBayesUpdateable();
        try {

            /* set classifier properties */
            naiveBayesUpdateable.setUseSupervisedDiscretization(false);
            naiveBayesUpdateable.setDisplayModelInOldFormat(false);
            naiveBayesUpdateable.setUseKernelEstimator(false);
            //naiveBayesUpdateable.setOptions(options);
            naiveBayesUpdateable.setDebug(DEBUG);

            /* build classifier with given instances */
            naiveBayesUpdateable.buildClassifier(structure);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return naiveBayesUpdateable;
    }

}

