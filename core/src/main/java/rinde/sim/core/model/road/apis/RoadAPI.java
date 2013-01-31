package rinde.sim.core.model.road.apis;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.RoadQueries;

/**
 * The API provided to road users, every road user
 * will get his own personal API assigned.
 * 
 * This API allows to retrieve the current location of this user.
 * 
 * @author dmerckx
 */
public interface RoadAPI extends RoadQueries{
    
    /**
     * Returns the current location of this user.
     * @return The current location.
     */
    Point getCurrentLocation();
    
    /**
     * Returns a presentation of the state of the user of this API. 
     * @return The state of this user.
     */
    RoadState getState();
}