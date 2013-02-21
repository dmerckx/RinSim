package rinde.sim.core.model.pdp.apis;

import java.util.List;

import rinde.sim.core.model.SafeIterator;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.PdpModel;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.pdp.users.Truck;
import rinde.sim.core.model.pdp.users.TruckData;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.util.positions.Filter;

import com.google.common.collect.Lists;

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
    public Parcel findClosestAvailableParcel(final TimeLapse time) {
        assert roadAPI != null: "Init has to be called first";
        
        PickupPoint<?> p = pdpModel.queryClosestPickup(roadAPI.getCurrentLocation(), new Filter<PickupPoint<?>>() {
            @Override
            public boolean matches(PickupPoint<?> p) {
                switch(p.getPickupPointState().getPickupState()){
                    case BEING_PICKED_UP: return true;
                    case PICKED_UP: return true;
                }

                Parcel parcel = p.getPickupPointState().getParcel();
                if(!pdpModel.getPolicy().canPickup(parcel.pickupTimeWindow, time.getTimeLeft(), parcel.pickupDuration))
                    return true;
                
                return false;
            }
        });
        
        if(p == null) return null;
        
        return p.getPickupPointState().getParcel();
    }
    
    @Override
    public List<Parcel> locateAvailableParcels() {
        List<Parcel> result = Lists.newArrayList();
    
        SafeIterator<PickupPoint<?>> it = pdpModel.queryPickups();
        while(it.hasNext()){
            PickupPoint<?> p = it.next();
            
            switch(p.getPickupPointState().getPickupState()){
                case BEING_PICKED_UP: continue;
                case PICKED_UP: continue;
            }
            
            result.add(p.getPickupPointState().getParcel());
        }
        
        return result;
    }
}
