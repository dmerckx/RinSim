package rinde.sim.core.model.road;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.users.RoadUser;
import rinde.sim.util.positions.Query;

/**
 * @author merckx
 */
public interface RoadQueries {
    /**
     * 
     * @param pos
     * @param range
     * @param q
     */
    <T extends RoadUser<?>> void queryAround(Point pos, double range, Query q);
}
        