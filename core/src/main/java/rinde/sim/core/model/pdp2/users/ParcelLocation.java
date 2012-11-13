package rinde.sim.core.model.pdp2.users;

import rinde.sim.core.model.pdp2.PdpAPI;
import rinde.sim.core.model.pdp2.PdpUser;
import rinde.sim.core.model.pdp2.objects.Parcel2;
import rinde.sim.core.model.road.apis.FixedRoadAPI;
import rinde.sim.core.model.road.users.FixedRoadUser;

public class ParcelLocation implements FixedRoadUser, PdpUser {

    private FixedRoadAPI roadAPI;
    private PdpAPI pdpAPI;
    
    public final Parcel2 parcelToDeliver;
    private boolean pickedUp = false;
    
    public ParcelLocation(Parcel2 parcel) {
        this.parcelToDeliver = parcel;
    }
    
    @Override
    public void initRoadUser(FixedRoadAPI api) {
        this.roadAPI = api;
    }

    @Override
    public void setPdpAPI(PdpAPI api) {
        this.pdpAPI = api;
    }
    
    
    // ----- METHODS AVAILABLE DURING ACTIONS ------ //
    
    public Parcel2 inspectParcel(){
        return parcelToDeliver.clone();
    }
    
    public boolean isPickedUp(){
        return pickedUp;
    }
    
    public Parcel2 tryPickup(){
        if(canBePickedUp()){
            pickedUp = true;
            return parcelToDeliver;
        }
        return null;
    }
    
    public boolean canBePickedUp(){
        return !pickedUp && pdpAPI.canPickup(parcelToDeliver);
    }
    
}
