package rinde.sim.core.model.pdp.apis;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.apis.RoadAPI;

public interface TruckAPI{
    
    void init(RoadAPI roadAPI);
    
    Point findClosestAvailableParcel();
}
