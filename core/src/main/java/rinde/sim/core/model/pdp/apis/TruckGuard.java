package rinde.sim.core.model.pdp.apis;

import java.util.Iterator;
import java.util.List;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.PdpModel;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.pdp.users.Truck;
import rinde.sim.core.model.pdp.users.TruckData;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.simulation.TimeLapse;

//TODO
public class TruckGuard implements TruckAPI{

    private PdpModel pdpModel;
    private RoadAPI roadAPI;
    
    public TruckGuard(Truck<?> user, TruckData data, PdpModel model) {
        this.pdpModel = model;
    }

    @Override
    public void init(RoadAPI roadAPI, ContainerAPI containerAPI) {
        this.roadAPI = roadAPI;
    }
    
    @Override
    public Parcel findClosestAvailableParcel(TimeLapse time) {
        assert roadAPI != null: "Init has to be called first";
        
        double minDist = Double.POSITIVE_INFINITY;
        Parcel closestParcel = null;
        
        Point pos = roadAPI.getCurrentLocation();
        Iterator<PickupPoint<?>> it = pdpModel.queryPickups();
        while(it.hasNext()){
            PickupPoint<?> p = it.next();
            
            switch(p.getPickupPointState().getPickupState()){
                case BEING_PICKED_UP: continue;
                case PICKED_UP: continue;
            }
            
            Parcel parcel = p.getPickupPointState().getParcel();
            if(!pdpModel.getPolicy().canPickup(parcel.pickupTimeWindow, time.getTimeLeft(), parcel.pickupDuration))
                continue;
            
            double dist = Point.distance(pos, parcel.location);
            if(dist < minDist){
                minDist = dist;
                closestParcel = parcel;
            }
        }
        
        return closestParcel;
    }
    
    @Override
    public List<Parcel> locateAvailableParcels() {
        return null;
    }

}
