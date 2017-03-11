package blueguy.rf_localizer.Scanners;

import java.io.Serializable;
import java.util.List;

import blueguy.rf_localizer.utils.DataPair;

/**
 * Created by Rahul on 2/22/2017.
 */

public class DataObject implements Serializable{

    public DataObject() {}

    static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "DataObject{" +
                "mTimeStamp=" + mTimeStamp +
                ", mID='" + mID + '\'' +
                ", mDataVals=" + mDataVals +
                '}';
    }

    /**
     * DataObject is a container used to pass data among Scanner objects and ScannerCallback
     * and ScannerService, and more.
     *
     * @param mTimeStamp        the Long timestamp corresponding to this data
     * @param mID               the general feature name of the DataObject
     * @param mDataVals         the List of Pair objects that has:
     *                                  .first  = the String denoting the feature name of this value
     *                                  .second = the actual value of the data
     */
    public DataObject(Long mTimeStamp, String mID, List<DataPair<String, Object>> mDataVals) {
        this.mTimeStamp = mTimeStamp;
        this.mID = mID;
        this.mDataVals = mDataVals;
    }

    /**
     * This mTimeStamp contains the timestamp associated with this DataObject
     */
    public Long mTimeStamp;

    /**
     * This mID String is associated with the general name of this DataObject's
     * sensor type, etc.
     */
    public String mID;

    /**
     * Each element of this mDataVals List is a Pair that contains:
     *  .first  = a String indicating the feature name of the specific value given.
     *  .second = this Object is the actual value (with a .toString function)
     */
    public List<DataPair<String, Object>> mDataVals;



    public static final String concatFeatureID (String dataObjectID, String dataFeatureID) {
        return dataObjectID + "_" + dataFeatureID;
    }

}
