package blueguy.rf_localizer.Scanners;

import android.util.Pair;

import java.util.List;

/**
 * Created by Rahul on 2/22/2017.
 */

public class DataObject {

    public DataObject() {}

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
    public DataObject(Long mTimeStamp, String mID, List<Pair<String, Object>> mDataVals) {
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
    public List<Pair<String, Object>> mDataVals;
}
