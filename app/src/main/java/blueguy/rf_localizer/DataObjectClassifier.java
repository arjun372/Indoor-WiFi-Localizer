package blueguy.rf_localizer;

import android.os.Environment;
import android.util.Log;
import java.io.File;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import blueguy.rf_localizer.Scanners.DataObject;
import blueguy.rf_localizer.utils.DataPair;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

import static blueguy.rf_localizer.BuildConfig.DEBUG;

/**
 * Created by work on 3/6/17.
 */

public class DataObjectClassifier implements Serializable{

    private static final String FS_rootDirectory = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    private static final String TAG = DataObjectClassifier.class.getSimpleName();

    private static final String ATTRIBUTE_CLASS = "class";

    private static final long serialVersionUID = 3L;

    private HashMap<String, Attribute> mFeatureSet = new HashMap<>();

    private List<DataPair<DataObject, String>> mLabeledRawData;

    private HashSet<String> mClassLabels = new HashSet<>();

    private String mClassifierName;

    private Classifier mClassifier;



    public List<DataPair<DataObject, String>> getRawData() {
        return mLabeledRawData;
    }

    public DataObjectClassifier(final List<DataPair<DataObject, String>> dataWithLabels, String locationName) {

        /* save data for future use */
        this.mClassifierName = locationName.trim().replaceAll(" ", "");
        this.mLabeledRawData = dataWithLabels;

        final Instances dataInstances = convertDataObjectToInstances(dataWithLabels);
        this.mClassifier = buildClassifier(dataInstances);

        /* save instances to arff for inspection with weka GUI */
        if(DEBUG) InstancesToArff(dataInstances);
    }

    /**
     *
     * @param data
     * @return
     */
    public Map<String, Double> classify(List<DataPair<DataObject, String>> data) {

        /** create a list of data with unknown labels **/
        data.forEach(single -> single.second = ScanService.CLASS_UNKNOWN);

        final Instances toPredictOn = convertDataObjectToInstances(data);

        /** create an accumulator map with size equal to @mClassLabels, and set it to zero **/
        ArrayList<Double> accumulatedDistributions = new ArrayList<>();
        mClassLabels.forEach(eachLabel -> accumulatedDistributions.add(0.0));

        /** classify with current classifier **/
        for(final Instance singleInstance : toPredictOn)
        {
            try {
                final double[] distributions = mClassifier.distributionForInstance(singleInstance);
                for(int i = 0; i < distributions.length; i++)
                {
                    accumulatedDistributions.set(i, accumulatedDistributions.get(i) + distributions[i]);
                }
            } catch (Exception e) {
                Log.e(TAG, "Unable to classify instance");
                e.printStackTrace();
                continue;
            }

        }

        /** calculate the mean **/
        final int numInstances = toPredictOn.numInstances();
        accumulatedDistributions.forEach(classValue->classValue/=numInstances);

        return zipToMap(new ArrayList<>(mClassLabels), accumulatedDistributions);
    }

    /**
     *
     * @param labeledData
     * @return
     */
    public List<DataPair<String, Double>> evaluate(final List<DataPair<DataObject, String>> labeledData) {
        return new ArrayList<>();
    }

    private static <K, V> Map<K, V> zipToMap(List<K> keys, List<V> values) {
        return IntStream.range(0, keys.size()).boxed().collect(Collectors.toMap(keys::get, values::get));
    }

    private Instances convertDataObjectToInstances(final List<DataPair<DataObject, String>> dataWithLabels) {

        int meaningfulFeatureCount = 0;

        for (final DataPair<DataObject, String> dataPair : dataWithLabels) {

            /* Handle first part -> DataObject here */
            final String dataObjectID = dataPair.first.mID;
            final Long dataObjectTime = dataPair.first.mTimeStamp;
            final List<DataPair> dataObjectVals = dataPair.first.mDataVals;

            /* Iterate over DataPair list, concatenating each key with @dataObjectID to create a unique feature name */
            for (final DataPair objectDataPair : dataObjectVals)
            {
                final String keyName = (String) objectDataPair.first;
                final String uniqueFeatureID = DataObject.concatFeatureID(dataObjectID, keyName);

                if(!mFeatureSet.containsKey(uniqueFeatureID))
                {
                    mFeatureSet.put(uniqueFeatureID, new Attribute(uniqueFeatureID));
                    if(DEBUG && !(objectDataPair.second.toString()).equals("0")) meaningfulFeatureCount++;
                }

            }

            /* Handle second part -> Add current class label to set */
            final String classLabel = dataPair.second;
            mClassLabels.add(classLabel);
        }

        /* remove the unknown label from class values, it is meant to be implicit */
        mClassLabels.remove(ScanService.CLASS_UNKNOWN);

        /** By now, we have fully filled out :
         * @classLabels : contains all the label values, which are unique
         * @featureColumnIndex : essentially a list of columns
         */

        if(DEBUG)
        {
            for (String name : mFeatureSet.keySet())
            {
                final String value = mFeatureSet.get(name).toString();
                Log.d(TAG, "feature[" + value + "] : " + name);
            }
            Log.d(TAG, "Classifier contains " + mFeatureSet.size() + " unique features, out of which " + (meaningfulFeatureCount - 1) + " appear meaningful");
        }

        /** Since we already know all the columns, we can create an Instances object, with empty structure to formalize our Classifier structure
         * Step 1: create an attribute list of features
         * Step 2: Prepare class attribute & add it to end of attribute list
         * Step 3: pass attribute list and set instances class index
         * Step 4: print Instances for #sanity
         * */

        /* Step 1 */
//        for (final String uniqueFeatureID : featureColumnIndex.keySet())
//        {
//            final Attribute uniqueAttribute = new Attribute(uniqueFeatureID);
//            featureAttrList.add(uniqueAttribute);
//        }

        /* Step 2 */
        final Attribute classAttribute = new Attribute(ATTRIBUTE_CLASS, new ArrayList<>(mClassLabels));
        if(DEBUG) Log.e(TAG, mClassifierName +":"+ classAttribute.toString());
        mFeatureSet.put(ATTRIBUTE_CLASS, classAttribute);

        //featureAttrList.add(classAttribute);

        /* Step 3 */
        final ArrayList<Attribute> featureAttrList = new ArrayList<> (mFeatureSet.values());
        Instances dataInstances = new Instances(mClassifierName, featureAttrList, 0);
        dataInstances.setClass(classAttribute);

        /* Step 4 */
        if(DEBUG) System.err.println(dataInstances.toSummaryString());


        /** -------------------------------------------------------------------------------------**/


        /** Next, we will populate a row using data timestamp, @featureColumnIndex and @dataWithLabels
         * Step 5: Create a model empty instance (with all missing values) -> '?' and structure set to dataInstances
         * Step 6:
         * Step 7: Iterate over the timestamp array, get all instances, and store it to our @dataInstances set.
         * **/

        /* Step 6 */
        HashMap<Long, Instance> timestampMap = new HashMap<>();

        for(final DataPair<DataObject, String> dataPair : dataWithLabels)
        {
            final String label = (dataPair.second);
            final Long timestamp = dataPair.first.mTimeStamp;
            final List<DataPair> dataObjectVals = dataPair.first.mDataVals;

            /* Step 5 */
            /* gets previously stored instance @ given timestamp : falls back to empty instance if no instance exists @ timestamp */

            Instance previouslyStoredInstance = timestampMap.get(timestamp);

            if(previouslyStoredInstance == null)
            {
                previouslyStoredInstance = new DenseInstance(featureAttrList.size());
                previouslyStoredInstance.setDataset(dataInstances);
            }

            /* update the class attribute value, or set to missing if it's equal to ScanService Unknown label */
            if(label.equals(ScanService.CLASS_UNKNOWN))
            {
                previouslyStoredInstance.setClassMissing();
            }
            else
            {
                previouslyStoredInstance.setClassValue(label);
            }

            /* for every key, value pair here, generate it as an attribute and update it on our @previouslyStoredInstance */
            for(final DataPair dataVal : dataObjectVals)
            {
                final Object value = dataVal.second;
                final String dataAttributeID = DataObject.concatFeatureID(dataPair.first.mID, (String) dataVal.first);
                final Attribute uniqueAttribute = mFeatureSet.get(dataAttributeID);

                if(uniqueAttribute == null)
                {
                    Log.e(TAG, "Error: Cannot find attribute index " + dataAttributeID);
                    continue;
                }

                Log.e(TAG, "Updating attribute :: " + dataAttributeID + " @ column :" + uniqueAttribute.toString());

                if(value instanceof Integer)
                {
                    previouslyStoredInstance.setValue(uniqueAttribute, ((Integer) value).doubleValue());
                }
                else if(value instanceof Float)
                {
                    previouslyStoredInstance.setValue(uniqueAttribute, ((Float) value).doubleValue());
                }
                else if(value instanceof Double)
                {
                    previouslyStoredInstance.setValue(uniqueAttribute, (Double) value);
                }
                else if(value instanceof String)
                {
                    previouslyStoredInstance.setValue(uniqueAttribute, (String) value);
                }
                else
                {
                    Log.e(TAG, "Error: Cannot cast to meaningful type Object :" + value.toString());
                }
            }

            /* store this fucker in the hashmap, this lets it get further updated if there is another timestamp with more attributes */
            timestampMap.put(timestamp, previouslyStoredInstance);
        }

        /* Step 7 */
        dataInstances.addAll(timestampMap.values());
        return dataInstances;
    }

    @Override
    public String toString() {
        return "DataObjectClassifier{" +
                "mLabeledRawData=" + mLabeledRawData +
                ", mClassifier=" + mClassifier +
                '}';
    }

    private static weka.classifiers.Classifier buildClassifier(final Instances structure) {
        NaiveBayesUpdateable naiveBayesUpdateable = new NaiveBayesUpdateable();
        try {

            /* set mClassifier properties */
            naiveBayesUpdateable.setUseSupervisedDiscretization(false);
            naiveBayesUpdateable.setDisplayModelInOldFormat(false);
            naiveBayesUpdateable.setUseKernelEstimator(false);
            //naiveBayesUpdateable.setOptions(options);
            naiveBayesUpdateable.setDebug(DEBUG);

            /* build mClassifier with given instances */
            naiveBayesUpdateable.buildClassifier(structure);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if(DEBUG) System.err.println(naiveBayesUpdateable.toString());
        return naiveBayesUpdateable;
    }

    private boolean InstancesToArff(final Instances dataSet) {
        ArffSaver saver = new ArffSaver();
        saver.setInstances(dataSet);
        try {
            final File outputArff = new File(FS_rootDirectory + "/" + this.mClassifierName + ".arff.gz");
            saver.setCompressOutput(true);
            saver.setFile(outputArff);
            saver.writeBatch();
            Log.i(TAG, "Saved raw-arff data to:" + outputArff.getAbsolutePath());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}

