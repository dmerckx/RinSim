package rinde.sim.core.model.pdp.apis;

import rinde.sim.core.model.Data;
import rinde.sim.core.model.InitUser;
import rinde.sim.core.model.interaction.apis.InteractionAPI;
import rinde.sim.core.model.interaction.users.InteractionUser;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.PdpModel;
import rinde.sim.core.model.pdp.receivers.PickupReceiver;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.pdp.users.PickupPointData;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.core.simulation.time.TimeLapseHandle;

import com.google.common.collect.Lists;

public class PickupGuard extends PickupPointState implements PickupAPI, InitUser, InteractionUser<Data>{
    
    private Parcel parcel;
    private InteractionAPI interactionAPI;
    private PdpModel pdpModel;
    
    private long lastUpdatedState = -1;
    private PickupState state;
    
    private final TimeLapseHandle handle;
    
    public PickupGuard(PickupPoint<?> user, PickupPointData data, PdpModel model, TimeLapseHandle handle) {
        this.parcel = data.getParcel();
        this.pdpModel = model;
        this.handle = handle;
    }

    @Override
    public void setInteractionAPi(InteractionAPI api) {
        this.interactionAPI = api;
    }

    @Override
    public void init() {
        interactionAPI.advertise(
                new PickupReceiver(parcel.location, Lists.newArrayList(parcel), pdpModel.getPolicy()));
    }
    
    /**
     * State always remains the same during a single tick.
     */
    private void updateState(){
        if(lastUpdatedState == handle.getStartTime()) 
            return; //up to date 
        
        long time = handle.getStartTime();
       
        if(interactionAPI.isAdvertising()){
            if(time < parcel.pickupTimeWindow.begin)
                state = PickupState.SETTING_UP;
            else if(time < parcel.pickupTimeWindow.end)
                state = PickupState.AVAILABLE;
            else 
                state = PickupState.LATE;
        }
        else {
            if(time < handle.getSchedualedUntil())
                state = PickupState.BEING_PICKED_UP;
            else
                state = PickupState.PICKED_UP;
        }
        
        lastUpdatedState = time;
    }
    
    // ----- PICKUP API ----- //
    
    @Override
    public boolean isPickedUp() {
        return getPickupState() == PickupState.BEING_PICKED_UP 
                || getPickupState() == PickupState.PICKED_UP;
    }
    
    @Override
    public boolean canBePickedUp(TimeInterval time) {
        return !isPickedUp() && pdpModel.getPolicy().canPickup(
                parcel.pickupTimeWindow, time.getStartTime(), parcel.pickupDuration);
    }

    @Override
    public PickupPointState getState() {
        return this;
    }
    
    // ----- OVERLAP PICKUP API & PICKUP POINT STATE ----- //

    @Override
    public PickupState getPickupState() {
        updateState();
        return state;
    }
    
    @Override
    public Parcel getParcel() {
        return parcel;
    }

}
