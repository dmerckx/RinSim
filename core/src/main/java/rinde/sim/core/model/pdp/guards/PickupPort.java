package rinde.sim.core.model.pdp.guards;

import rinde.sim.core.model.Guard;
import rinde.sim.core.model.pdp.PdpModel;
import rinde.sim.core.model.pdp.apis.PickupAPI;
import rinde.sim.core.model.pdp.supported.Parcel;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.core.simulation.types.Agent;

public class PickupPort implements Guard, PickupAPI{

    private PdpModel pdpModel;
    private PickupPoint pickupPoint;
    
    private Parcel parcel;
    private boolean pickedUp = false;
    private long actionTime;

    private TimeLapse lapse;
    
    public PickupPort(PdpModel pdpModel, PickupPoint pickup) {
        this.pdpModel = pdpModel;
        this.pickupPoint = pickup;
    }
    
    private void doAction(TimeLapse lapse){
        if(actionTime == 0) return;
        
        if( lapse.getTimeLeft() >= actionTime){
            lapse.consume(actionTime);
            actionTime = 0;
        }
        else {
            actionTime -= lapse.getTimeLeft();
            lapse.consumeAll();
        }
    }
    
    
    // ----- AGENT PORT ----- //
    
    public void tick(TimeLapse lapse) {
        this.lapse = lapse;
        doAction(lapse);
    }

    @Override
    public Agent getAgent() {
        return pickupPoint;
    }
    
    // ----- PICKUP API ----- //
    
    @Override
    public void init(Parcel parcelToDeliver) {
        this.parcel = parcelToDeliver;
    }

    @Override
    public boolean isPickedUp() {
        return false;
    }

    @Override
    public Parcel getParcel() {
        return parcel;
    }

    @Override
    public PickupState getState() {
        if(pickedUp){
            return actionTime == 0? PickupState.PICKED_UP: PickupState.BEING_PICKED_UP;
        }
        
        if(lapse.getTime() < parcel.pickupTimeWindow.begin){
            return PickupState.SETTING_UP;
        }
        if(lapse.getTime() < parcel.pickupTimeWindow.end)
            return PickupState.AVAILABLE;
        
        return PickupState.LATE;
    }

    @Override
    public boolean canBePickedUp() {
        return !pickedUp && pdpModel.canPickup(lapse, parcel);
    }
}
