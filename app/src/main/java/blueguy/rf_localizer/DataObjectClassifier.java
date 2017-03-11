package blueguy.rf_localizer;

import android.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import blueguy.rf_localizer.Scanners.DataObject;
import blueguy.rf_localizer.utils.DataPair;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.classifiers.*;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Created by work on 3/6/17.
 */

import static blueguy.rf_localizer.BuildConfig.DEBUG;

public class DataObjectClassifier implements Serializable{

    private List<DataPair<DataObject, String>> labeled_data;
    private Classifier classifier;


    public DataObjectClassifier(final List<DataPair<DataObject, String>> dataWithLabels) throws Exception{

        final Instances dataInstances = convertDataObjectToInstances(dataWithLabels);
        /* save data for future use */
        this.labeled_data = dataWithLabels;
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

    public static Instances convertDataObjectToInstances(final List<DataPair<DataObject, String>> dataWithLabels) throws Exception{
        //return new Instances();
        return null;
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
//    private static weka.classifiers.UpdateableClassifier createClassifier(final Instances structure, final String[] options) {
//        NaiveBayesUpdateable naiveBayesUpdateable = new NaiveBayesUpdateable();
//        try {
//
//            /* set classifier properties */
//            naiveBayesUpdateable.setUseSupervisedDiscretization(false);
//            naiveBayesUpdateable.setDisplayModelInOldFormat(false);
//            naiveBayesUpdateable.setUseKernelEstimator(false);
//            naiveBayesUpdateable.setOptions(options);
//            naiveBayesUpdateable.setDebug(DEBUG);
//
//            /* build classifier with given instances */
//            naiveBayesUpdateable.buildClassifier(structure);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//        return naiveBayesUpdateable;
//    }

}

