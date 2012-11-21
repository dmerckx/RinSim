package rinde.sim.core.model.road.apis;

import rinde.sim.core.graph.Point;

public interface RoadAPI {
    
    public Point getLocation();
    
    public Point getLastLocation();
}
