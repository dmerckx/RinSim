package rinde.sim.core.model.road.dummies;

import rinde.sim.core.model.road.apis.MovingRoadAPI;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.model.road.apis.RoadState;
import rinde.sim.core.model.road.users.MovingRoadUser;

public class TrivialRoadUser implements MovingRoadUser{

    public RoadAPI roadAPI;
    
    public TrivialRoadUser() {
        
    }

    @Override
    public void setRoadAPI(MovingRoadAPI api) {
        this.roadAPI = api;
    }

    @Override
    public RoadState getRoadState() {
        return roadAPI.getState();
    }
}
