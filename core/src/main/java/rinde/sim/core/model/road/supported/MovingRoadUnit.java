package rinde.sim.core.model.road.supported;

import rinde.sim.core.model.road.apis.MovingRoadAPI;
import rinde.sim.core.model.road.users.MovingRoadUser;

public interface MovingRoadUnit extends RoadUnit{

    @Override
    public MovingRoadAPI getRoadAPI();
    
    @Override
    public MovingRoadUser getElement();
    
    @Override
    public MovingRoadData getInitData();
    
    public interface MovingRoadData extends RoadData{
        public Double getInitialSpeed();
    }
}
