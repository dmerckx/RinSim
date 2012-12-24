package rinde.sim.core.model.road.users;

import rinde.sim.core.model.road.apis.RoadAPI;

/**
 * Represents a simple road user, unable to move, and only able
 * to retrieve his starting location via this API. 
 * 
 * 
 * @author dmerckx
 *
 * @param <D> The type of initialization data.
 */
public interface FixedRoadUser<D extends RoadData> extends RoadUser<D>{

    /**
     * Sets the road API of this user.
     * 
     * Note:
     * This method should simply store the given API.
     * No side effects should be applied during this call.
     * 
     * @param api The road API.
     */
    public void setRoadAPI(RoadAPI api);
}
