package rinde.sim.core.model.pdp.apis;

import java.util.List;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Parcel;

public interface TruckAPI<P extends Parcel>{
    
    public List<Parcel> scanLocation();
    
    public P findClosestAvailableParcel();
    
    public List<Point> locateTrucks();
    
    public List<Point> locateDepots();
    
    public List<P> locateAvailableParcels();
}
