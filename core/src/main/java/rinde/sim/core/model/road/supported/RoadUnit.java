package rinde.sim.core.model.road.supported;

import rinde.sim.core.model.Unit;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.model.road.users.RoadUser;

public interface RoadUnit extends Unit {

    public RoadAPI getRoadAPI();
    
    public void setRoadAPI(RoadAPI api);
    
    @Override
    public RoadUser getElement();
}
