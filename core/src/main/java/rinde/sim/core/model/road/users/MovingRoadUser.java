package rinde.sim.core.model.road.users;

import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.apis.MovingRoadAPI;
import rinde.sim.core.model.road.apis.RoadAPI;

/**
 * Used to represent road users that want to reposition itself using the
 * {@link RoadModel#followPath(MovingRoadUser, java.util.Queue, rinde.sim.core.TimeLapse)}
 * method.
 * @author Bartosz Michalik <bartosz.michalik@cs.kuleuven.be>
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * @since 2.0
 */
public interface MovingRoadUser extends RoadUser {
   
    public void initRoadUser(MovingRoadAPI api);
}
