package blueguy.rf_localizer.utils;

import java.io.Serializable;

/**
 * Created by arjun on 3/13/17.
 */

public class LocationVertex implements Serializable {

    private static final long serialVersionUID = 212312L;

    public String mLabel;
    public float mX;
    public float mY;
    public boolean mHere;

    public LocationVertex(final String label, final float x, final float y, final boolean isHere){
        this.mLabel = label;
        this.mHere = isHere;
        this.mX = x;
        this.mY = y;
    }

    public LocationVertex(final String label, final float x, final float y){
        this.mLabel = label;
        this.mHere = false;
        this.mX = x;
        this.mY = y;
    }

    @Override
    public String toString() {
        return "LocationVertex{" +
                "mLabel='" + mLabel + '\'' +
                ", mX=" + mX +
                ", mY=" + mY +
                ", mHere=" + mHere +
                '}';
    }
}