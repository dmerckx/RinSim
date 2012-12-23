package rinde.sim.core.model.pdp.apis;

import java.util.List;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Data;
import rinde.sim.core.model.User;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.simulation.TimeLapse;

public interface TruckAPI extends User<Data>{
    
    void init(RoadAPI roadAPI, ContainerAPI containerAPI);
    
    //public List<Parcel> scanLocation();
    
    Parcel findClosestAvailableParcel(TimeLapse time);
    
    List<Parcel> locateAvailableParcels();
}
