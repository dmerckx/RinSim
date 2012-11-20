package rinde.sim.core.model.road.guards;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.model.road.users.RoadUser;

public abstract class RoadGuard implements RoadAPI{

    protected RoadModel model;
    
    public RoadGuard(RoadModel model) {
        this.model = model;
    }

    protected abstract boolean isInitialised();
    
    protected abstract RoadUser getUser();
    
    public abstract Point getLocation();
}
