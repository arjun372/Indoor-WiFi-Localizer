package blueguy.rf_localizer.utils;

import java.io.Serializable;

/**
 * Created by arjun on 3/10/17.
 */

public class DataPair<First, Second> implements Serializable{

    private static final long serialVersionUID = 2L;
    public First first;
    public Second second;

    /**
     * Constructor for a Pair.
     *
     * @param first  the first object in the Pair
     * @param second the second object in the pair
     */
    public DataPair(First first, Second second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return "DataPair{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }
}
