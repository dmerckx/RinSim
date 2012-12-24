/** 
 * 
 */
package rinde.sim.core.model.road.users;

import rinde.sim.core.model.User;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.apis.RoadState;

/**
 * Represents a user of the {@link RoadModel}.
 * 
 * @author dmerckx
 *
 * @param <D> The type of initialization data.
 */
public interface RoadUser<D extends RoadData> extends User<D>{
    
    /**
     * Should return the state, regarding the road, of this user.
     * This state can be found in the injected road API.
     * 
     * Note:
     * This method should simply return the state, present in the given API.
     * No side effects should be applied during this call.
     * 
     * @return The road state.
     */
    public RoadState getRoadState();
}
