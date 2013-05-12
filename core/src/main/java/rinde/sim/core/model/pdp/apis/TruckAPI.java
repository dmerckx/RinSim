package rinde.sim.core.model.pdp.apis;

import java.util.List;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.simulation.TimeLapse;

public interface TruckAPI{
    
    void init(RoadAPI roadAPI, ContainerAPI containerAPI);
    
    Point findClosestAvailableParcel(TimeLapse time);
    
    List<Parcel> locateAvailableParcels();
}
