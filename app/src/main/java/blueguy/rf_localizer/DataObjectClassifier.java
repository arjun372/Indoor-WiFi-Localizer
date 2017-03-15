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
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import blueguy.rf_localizer.Scanners.DataObject;
import blueguy.rf_localizer.utils.DataPair;
import weka.classifiers.Classifier;
import weka.classifiers.UpdateableClassifier;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.supervised.instance.ClassBalancer;

import static blueguy.rf_localizer.BuildConfig.DEBUG;

/**
 * Created by work on 3/6/17.
 */

public class DataObjectClassifier implements Serializable{

    private static final String FS_rootDirectory = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    private static final String TAG = DataObjectClassifier.class.getSimpleName();

    private Instances dataInstances;

    public static final String CLASS_UNKNOWN = "here";

    private static final String ATTRIBUTE_CLASS = "class";

    private static final long serialVersionUID = 3L;

    private HashMap<String, Attribute> mFeatureSet = new HashMap<>();

    private HashSet<String> mClassLabels = new HashSet<>();

    private UpdateableClassifier mClassifier;

    private String mClassifierName;

    public DataObjectClassifier(final List<DataPair<DataObject, String>> dataWithLabels, final String clfName) {
        this.dataInstances = convertDataObjectToInstances(dataWithLabels);
        this.mClassifier   = buildClassifier(dataInstances);
        this.mClassifierName = clfName;
    }

    /**
     *
     * @param data
     * @return
     */
    public Map<String, Double> classify(List<DataPair<DataObject, String>> data) {

        /** create a list of data with unknown labels **/
        for(DataPair singleData : data)
        {
            singleData.second = CLASS_UNKNOWN;
        }

        // convert list of unlabeled data to instances
        final Instances toPredictOn = convertDataObjectToInstances(data);
        if(DEBUG) Log.d(TAG, "Classifying "+toPredictOn.numInstances()+" instances with "+data.size()+" attributes");

        /** create an accumulator map with size equal to @mClassLabels, and set it to zero **/
        ArrayList<Double> accumulatedDistributions = new ArrayList<>();
        for(final String label : mClassLabels) accumulatedDistributions.add(0.0);
//        mClassLabels.forEach(eachLabel -> accumulatedDistributions.add(0.0));

        //if(DEBUG) System.err.println(mClassifier.toString());

        /** classify with current classifier **/
        for(final Instance singleInstance : toPredictOn)
        {
            try {
                final double[] distributions = ((Classifier) mClassifier).distributionForInstance(singleInstance);
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
//
//    private static <K, V> Map<K, V> zipToMap(List<K> keys, List<V> values) {
//
//    }
    private static <K, V> Map<K, V> zipToMap(List<K> keys, List<V> values) {
        return IntStream.range(0, keys.size()).boxed().collect(Collectors.toMap(keys::get, values::get));
    }

    private Instances convertDataObjectToInstances(final List<DataPair<DataObject, String>> dataWithLabels) {

        final boolean updateLabels = (mClassLabels.isEmpty());
        final boolean updateFeatureSet = (mFeatureSet.isEmpty());

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

                if(updateFeatureSet && !mFeatureSet.containsKey(uniqueFeatureID))
                {
                    mFeatureSet.put(uniqueFeatureID, new Attribute(uniqueFeatureID));
                    if(DEBUG && !(objectDataPair.second.toString()).equals("0")) meaningfulFeatureCount++;
                }

            }

            if(updateLabels)
            {
                /* Handle second part -> Add current class label to set */
                final String classLabel = dataPair.second;
                mClassLabels.add(classLabel);
            }
        }

        if(updateLabels) {
            /* remove the unknown label from class values, it is meant to be implicit */
            mClassLabels.remove(CLASS_UNKNOWN);
        }


        /** By now, we have fully filled out :
         * @classLabels : contains all the label values, which are unique
         * @featureColumnIndex : essentially a list of columns
         */

//        if(DEBUG)
//        {
//            for (String name : mFeatureSet.keySet())
//            {
//                final String value = mFeatureSet.get(name).toString();
//                Log.d(TAG, "feature[" + value + "] : " + name);
//            }
//            Log.d(TAG, "Classifier contains " + mFeatureSet.size() + " unique features, out of which " + (meaningfulFeatureCount - 1) + " appear meaningful");
//        }

        /** Since we already know all the columns, we can create an Instances object, with empty structure to formalize our Classifier structure
         * Step 1: create an attribute list of features
         * Step 2: Prepare class attribute & add it to end of attribute list
         * Step 3: pass attribute list and set instances class index
         * Step 4: print Instances for #sanity
         * */

        /* Step 1 */
        final Attribute classAttribute = new Attribute(ATTRIBUTE_CLASS, new ArrayList<>(mClassLabels));
        mFeatureSet.put(ATTRIBUTE_CLASS, classAttribute);

        if (DEBUG) Log.e(TAG, "["+mClassifierName + "] Labels :: " + classAttribute.toString());

        /* Step 2 */
        final ArrayList<Attribute> featureAttrList = new ArrayList<> (mFeatureSet.values());
        Instances dataInstances = new Instances(mClassifierName, featureAttrList, 0);
        dataInstances.setClass(classAttribute);

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
            if(label.equals(CLASS_UNKNOWN))
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

                //Log.e(TAG, "Updating attribute :: " + dataAttributeID + " @ column :" + uniqueAttribute.toString());

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


    public void update(final List<DataPair<DataObject, String>> newData) throws Exception{
        final Instances newInstances = convertDataObjectToInstances(newData);
        if(DEBUG) Log.d("updateClassifier", "Attempting to update CLF with instances :"+newInstances.size());
        for(final Instance instance : newInstances) mClassifier.updateClassifier(instance);
    }

    private static weka.classifiers.UpdateableClassifier buildClassifier(Instances structure) {

        Log.v(TAG, "Builiding classifier using "+structure.numInstances()+" instances...");

        /* Randomize data */
        Log.v(TAG, "Randomizing instances");
        structure.randomize(new Random());

        /* Balance classes */
        Log.v(TAG, "Balancing classes");
        structure = balanceClasses(structure);


        NaiveBayesUpdateable classifier = new NaiveBayesUpdateable();

        try {

            /* set mClassifier properties */
            classifier.setUseSupervisedDiscretization(false);
            classifier.setDisplayModelInOldFormat(false);
            classifier.setUseKernelEstimator(true);
            classifier.setDebug(DEBUG);

            /* build mClassifier with given instances */
            classifier.buildClassifier(structure);

            if(DEBUG) Log.e(TAG, classifier.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return classifier;
    }

    public boolean InstancesToArff() {
        ArffSaver saver = new ArffSaver();
        saver.setInstances(this.dataInstances);
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

    private static Instances balanceClasses(final Instances unbalanced) {
        Instances balanced = unbalanced;
        ClassBalancer classBalancerFilter = new ClassBalancer();
        classBalancerFilter.setDebug(DEBUG);
        if(DEBUG) Log.v(TAG, "Balancer: "+classBalancerFilter.toString());
        try {
            classBalancerFilter.setInputFormat(unbalanced);
            balanced = Filter.useFilter(unbalanced, classBalancerFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return balanced;
    }

//    private void updateRawData(final List<DataPair<DataObject, String>> dataWithLabels) {
//        if(mLabeledRawData == null) mLabeledRawData = new ArrayList<>();
//        mLabeledRawData.addAll(new ArrayList<>(dataWithLabels));
//    }
}

