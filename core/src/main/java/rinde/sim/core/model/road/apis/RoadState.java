package rinde.sim.core.model.road.apis;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.users.RoadUser;

/**
 * The road state of a certain {@link RoadUser}.
 * This object is guaranteed to always return the same results 
 * during a single tick.
 * 
 * @author dmerckx
 */
public abstract class RoadState {

    RoadState() {}
    
    /**
     * Returns the location of this user at the start of this tick.
     * @return The location of this user.
     */
    public abstract Point getLocation();
}
