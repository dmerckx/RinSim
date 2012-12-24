package rinde.sim.core.model.road.users;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Data;

/**
 * Initialization data for {@link RoadUser}s.
 * 
 * @author dmerckx
 */
public interface RoadData extends Data{

    /**
     * The location at which this user will start.
     * @return The starting location.
     */
    Point getStartPosition();
}
