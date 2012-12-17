package rinde.sim.core.model.road.apis;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.users.RoadData;
import rinde.sim.core.model.road.users.RoadUser;

public class RoadGuard extends RoadState implements RoadAPI{

    protected RoadModel model;
    protected RoadUser user;
    protected Point lastLocation;
    
    public RoadGuard(RoadUser<?> user, RoadData data, RoadModel model) {
        this.user = user;
        this.model = model;
       
        this.lastLocation = data.getStartPosition();
    }
    
    @Override
    public Point getCurrentLocation() {
        return lastLocation;
    }

    @Override
    public Point getLocation() {
        return lastLocation;
    }
    
    @Override
    public RoadState getState(){
        return this;
    }

}
