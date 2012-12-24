package rinde.sim.core.model.road.users;

import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.apis.MovingRoadAPI;

/**
 * Represents a moving road user, able to move across the
 * road infrastructure defined in the {@link RoadModel}.
 * 
 * The user of this API is able to drive towards a given target,
 * adjust his speed and retrieve a random location on the map.
 * 
 * 
 * @author dmerckx
 *
 * @param <D> The type of initialization data.
 */
public interface MovingRoadUser<D extends MovingRoadData> extends RoadUser<D> {

    /**
     * Sets the road API of this user.
     * 
     * Note:
     * This method should simply store the given API.
     * No side effects should be applied during this call.
     * 
     * @param api The road API.
     */
    public void setRoadAPI(MovingRoadAPI api);
}
