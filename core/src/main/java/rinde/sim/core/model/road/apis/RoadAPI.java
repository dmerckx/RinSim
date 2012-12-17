package rinde.sim.core.model.road.apis;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Data;
import rinde.sim.core.model.User;

public interface RoadAPI{
    
    Point getCurrentLocation();
    
    RoadState getState();
}