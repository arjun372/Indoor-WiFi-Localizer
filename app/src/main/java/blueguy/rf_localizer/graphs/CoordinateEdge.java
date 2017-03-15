package blueguy.rf_localizer.graphs;

import org.jgrapht.graph.DefaultEdge;

/**
 * Created by Rahul Malavalli on 3/15/2017.
 */

public class CoordinateEdge extends DefaultEdge {
    private static final long serialVersionUID = 102L;

    private CoordinateNode mNode1;
    private CoordinateNode mNode2;

    public CoordinateEdge(CoordinateNode mNode1, CoordinateNode mNode2) {
        this.mNode1 = mNode1;
        this.mNode2 = mNode2;
    }

    public CoordinateNode getSource() {
        return mNode1;
    }


    public CoordinateNode getTarget() {
        return mNode2;
    }

    @Override
    public String toString() {
        return "CoordinateEdge{" +
                "mNode1=" + mNode1 +
                ", mNode2=" + mNode2 +
                '}';
    }
}
