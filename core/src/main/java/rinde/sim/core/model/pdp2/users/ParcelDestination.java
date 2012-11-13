package rinde.sim.core.model.pdp2.users;

import rinde.sim.core.model.pdp2.PdpAPI;
import rinde.sim.core.model.pdp2.PdpUser;
import rinde.sim.core.model.pdp2.objects.Parcel2;
import rinde.sim.core.model.road.apis.FixedRoadAPI;
import rinde.sim.core.model.road.users.FixedRoadUser;
import rinde.sim.util.TimeWindow;

public class ParcelDestination implements FixedRoadUser, PdpUser{

    private FixedRoadAPI roadAPI;
    private PdpAPI pdpAPI;
    
    private final Parcel2 expectedParcel;
    private boolean delivered = false;
    
    public ParcelDestination(Parcel2 parcel) {
        this.expectedParcel = parcel;
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
        return expectedParcel.clone();
    }
    
    public boolean tryAccept(Parcel2 parcel){
        if(canAccept(parcel)){
            delivered = true;
            return true;
        }
        return false;
    }
    
    public boolean canAccept(Parcel2 parcel){
        return parcel == expectedParcel
                && !delivered
                && pdpAPI.canDeliver(expectedParcel);
    }

}
