package rinde.sim.core.model.pdp.apis;

import java.util.List;

import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.PdpModel;
import rinde.sim.core.model.pdp.users.Truck;
import rinde.sim.core.model.pdp.users.TruckData;

//TODO
public class TruckGuard implements TruckAPI{

    
    public TruckGuard(Truck<?> user, TruckData data, PdpModel model) {
        
    }

    @Override
    public Parcel findClosestAvailableParcel() {
        return null;
    }
    
    @Override
    public List<Parcel> locateAvailableParcels() {
        return null;
    }
}
