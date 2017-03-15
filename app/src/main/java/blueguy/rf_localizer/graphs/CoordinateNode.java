package blueguy.rf_localizer.graphs;

import java.io.Serializable;

/**
 * Created by Rahul Malavalli on 3/15/2017.
 */

public class CoordinateNode implements Serializable {
    private static final Long serialVersionUID = 101L;

    private static int CURR_ID = 0;

    private int mID;
    private String mLabel;
    private float mXCoord;
    private float mYCoord;

    public CoordinateNode(String mLabel, float mXCoord, float mYCoord) {
        this.mLabel = mLabel;
        this.mXCoord = mXCoord;
        this.mYCoord = mYCoord;
    }


    public int getID() {
        return mID;
    }

    public String getLabel() {
        return mLabel;
    }

    public float getXCoord() {
        return mXCoord;
    }

    public float getYCoord() {
        return mYCoord;
    }

    @Override
    public String toString() {
        return "CoordinateNode{" +
                "mID=" + mID +
                ", mLabel='" + mLabel + '\'' +
                ", mXCoord=" + mXCoord +
                ", mYCoord=" + mYCoord +
                '}';
    }
}
