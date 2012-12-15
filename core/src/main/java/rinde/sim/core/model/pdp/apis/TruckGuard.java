package rinde.sim.core.model.pdp.apis;

import java.util.List;

import rinde.sim.core.model.Data;
import rinde.sim.core.model.interaction.apis.InteractionAPI;
import rinde.sim.core.model.interaction.users.InteractionUser;
import rinde.sim.core.model.pdp.users.Parcel;

public class TruckGuard implements TruckAPI{

    
    public TruckGuard() {
        
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
