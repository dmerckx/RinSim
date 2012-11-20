package rinde.sim.core.model.road.supported;

import rinde.sim.core.model.road.guards.MovingRoadGuard;
import rinde.sim.core.model.road.users.MovingRoadUser;

public interface MovingRoadHolder extends RoadHolder{

    @Override
    public MovingRoadGuard getRoadGuard();
    
    @Override
    public MovingRoadUser getElement();
}
