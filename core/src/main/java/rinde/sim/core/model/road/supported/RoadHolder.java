package rinde.sim.core.model.road.supported;

import rinde.sim.core.model.Holder;
import rinde.sim.core.model.road.guards.RoadGuard;
import rinde.sim.core.model.road.users.RoadUser;

public interface RoadHolder extends Holder {

    public RoadGuard getRoadGuard();
    
    @Override
    public RoadUser getElement();
}
