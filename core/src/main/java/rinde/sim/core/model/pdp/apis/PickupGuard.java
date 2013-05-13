package rinde.sim.core.model.pdp.apis;

import rinde.sim.core.model.Data;
import rinde.sim.core.model.InitUser;
import rinde.sim.core.model.interaction.Receiver;
import rinde.sim.core.model.interaction.apis.InteractionAPI;
import rinde.sim.core.model.interaction.users.InteractionUser;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.PdpModel;
import rinde.sim.core.model.pdp.receivers.PickupReceiver;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.pdp.users.PickupPointData;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.time.TimeLapseHandle;

import com.google.common.collect.Lists;

public class PickupGuard extends PickupPointState implements PickupAPI, InitUser, InteractionUser<Data>{
    
    private final PdpModel pdpModel;
    private final PickupPoint<?> user;
    private InteractionAPI interactionAPI;
    
    private Parcel parcel;
    
    private long lastUpdatedState = -1;
    private long pickedupTime = -1;
    private PickupState state;
    
    private final TimeLapseHandle handle;
    
    public PickupGuard(PickupPoint<?> user, PickupPointData data, PdpModel model, TimeLapseHandle handle) {
        this.parcel = data.getParcel();
        this.pdpModel = model;
        this.user = user;
        this.handle = handle;
    }

    @Override
    public void setInteractionAPi(InteractionAPI api) {
        assert interactionAPI == null;
        this.interactionAPI = api;
    }

    @Override
    public void init() {
        //Advertise a receiver, waiting for the parcel to be picked up
        interactionAPI.advertise(
                new PickupReceiver(parcel.location, Lists.newArrayList(parcel), pdpModel.getPolicy()));
    }

    @Override
    public void notifyDone(Receiver receiver) {
        pickedupTime = handle.getCurrentTime();
        handle.consume(parcel.deliveryDuration);
        pdpModel.notifyParcelPickup(user);
    }
    
    /**
     * State always remains the same during a single tick.
     */
    private void updateState(){
        if(lastUpdatedState == handle.getStartTime()) 
            return; //up to date 
        
        long time = handle.getStartTime();
       
        if(pickedupTime == -1){
            if(time < parcel.pickupTimeWindow.begin)
                state = PickupState.SETTING_UP;
            else if(time < parcel.pickupTimeWindow.end)
                state = PickupState.AVAILABLE;
            else 
                state = PickupState.LATE;
        }
        else {
            if(time < pickedupTime + parcel.pickupDuration)
                state = PickupState.BEING_PICKED_UP;
            else
                state = PickupState.PICKED_UP;
        }
        
        lastUpdatedState = time;
    }
    
    // ----- PICKUP API ----- //
    
    @Override
    public synchronized boolean isPickedUp() {
        return getPickupState() == PickupState.BEING_PICKED_UP 
                || getPickupState() == PickupState.PICKED_UP;
    }
    
    @Override
    public synchronized boolean canBePickedUp(TimeInterval time) {
        return !isPickedUp() && pdpModel.getPolicy().canPickup(
                parcel.pickupTimeWindow, time.getStartTime(), parcel.pickupDuration);
    }

    @Override
    public synchronized PickupPointState getState() {
        return this;
    }
    
    // ----- OVERLAP PICKUP API & PICKUP POINT STATE ----- //

    @Override
    public synchronized PickupState getPickupState() {
        updateState();
        return state;
    }
    
    @Override
    public synchronized Parcel getParcel() {
        return parcel;
    }

}
