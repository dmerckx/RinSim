package rinde.sim.core.model.road.users;

import rinde.sim.core.model.road.supported.MovingRoadUnit;

public interface MovingRoadUser extends RoadUser {
   
    @Override
    public MovingRoadUnit buildUnit();
}
