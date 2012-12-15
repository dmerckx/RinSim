package rinde.sim.core.model.pdp.apis;

import java.util.List;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Data;
import rinde.sim.core.model.User;
import rinde.sim.core.model.pdp.users.Parcel;

public interface TruckAPI extends User<Data>{
    
    //public List<Parcel> scanLocation();
    
    public Parcel findClosestAvailableParcel();
    
    public List<Parcel> locateAvailableParcels();
}
