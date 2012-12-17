package rinde.sim.core.model.road.apis;

import rinde.sim.core.graph.Point;

public abstract class RoadState {

    RoadState() {}
    
    public abstract Point getLocation();
}
