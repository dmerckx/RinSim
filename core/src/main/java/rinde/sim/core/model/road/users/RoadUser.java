/** 
 * 
 */
package rinde.sim.core.model.road.users;

import rinde.sim.core.model.User;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.apis.RoadAPI;

/**
 * A RoadUser is an object living on the {@link RoadModel}.
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public interface RoadUser<D extends RoadData> extends User<D>{

    public void setRoadAPI(RoadAPI api);
}
