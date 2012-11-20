package rinde.sim.core.model.road.supported;

import rinde.sim.core.model.road.guards.FixedRoadGuard;
import rinde.sim.core.model.road.users.FixedRoadUser;

public interface FixedRoadHolder extends RoadHolder{

    @Override
    public FixedRoadGuard getRoadGuard();
    
    @Override
    public FixedRoadUser getElement();
}
