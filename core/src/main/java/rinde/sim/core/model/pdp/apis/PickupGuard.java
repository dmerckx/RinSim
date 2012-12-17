package rinde.sim.core.model.pdp.apis;

import java.util.ArrayList;
import java.util.List;

import rinde.sim.FullGuard;
import rinde.sim.core.model.Data;
import rinde.sim.core.model.InitGuard;
import rinde.sim.core.model.interaction.Notification;
import rinde.sim.core.model.interaction.apis.InteractionAPI;
import rinde.sim.core.model.interaction.users.InteractionUser;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.PdpModel;
import rinde.sim.core.model.pdp.receivers.ContainerNotification;
import rinde.sim.core.model.pdp.receivers.DeliverySpecificReceiver;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.pdp.users.PickupPointData;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;

public class PickupGuard extends PickupPointState implements PickupAPI, InitGuard, FullGuard, InteractionUser<Data>{
    
    private Parcel parcel;
    private InteractionAPI interactionAPI;
    private PdpModel pdpModel;
    

    private boolean pickedUp = false;
    private long pickupTime = 0;
    private PickupState state = PickupState.SETTING_UP;
    
    public PickupGuard(PickupPoint<?> user, PickupPointData data, PdpModel model) {
        this.parcel = data.getParcel();
        this.pdpModel = model;
    }

    @Override
    public void setInteractionAPi(InteractionAPI api) {
        this.interactionAPI = api;
    }
    
    @Override
    public boolean isPickedUp() {
        return state == PickupState.BEING_PICKED_UP 
                || state == PickupState.PICKED_UP;
    }

    @Override
    public Parcel getParcel() {
        return parcel;
    }

    @Override
    public PickupState getPickupState() {
        return state;
    }
    
    public void setState(TimeInterval interval){
        if(!pickedUp){
            if(interval.getEndTime() < parcel.pickupTimeWindow.begin){
                state = PickupState.SETTING_UP;
            } 
            else if(interval.getEndTime() < parcel.pickupTimeWindow.end){
                state = PickupState.AVAILABLE;
            }
            else {
                state = PickupState.LATE;
            }
        }
        else{
            if(pickupTime == 0){
                state = PickupState.PICKED_UP;
            }
            else{
                state = PickupState.BEING_PICKED_UP;
            }
        }
    }

    // ----- INIT GUARD & AFTER-TICK GUARD ----- //


    @Override
    public void init() {
        List<Parcel> targets = new ArrayList<Parcel>();
        targets.add(parcel);
        
        interactionAPI.advertise(
                new DeliverySpecificReceiver(parcel.destination, targets, pdpModel.getPolicy()));
    }

    /**
     * State does not change during a tick.
     * Enforce time consistency.
     */
    @Override
    public void tick(TimeLapse time) {
        switch(state){
            case BEING_PICKED_UP:
                if(pickupTime > time.getTimeLeft()){
                    pickupTime -= time.getTimeLeft(); 
                    time.consumeAll();
                }
                else{
                    time.consume(pickupTime);
                    pickupTime = 0;
                }
                break;
            default:
                break;
        }
    }
    
    /**
     * Change state if necessary.
     */
    @Override
    public void afterTick(TimeInterval interval) {
        for(Notification n:interactionAPI.getNotifications()){
            if(n instanceof ContainerNotification){
                assert parcel == ((ContainerNotification) n).getParcel();
                pickedUp = true;
                pickupTime = parcel.deliveryDuration;
            }
        }
        setState(interval);
    }

    @Override
    public boolean canBePickedUp(TimeInterval time) {
        return pdpModel.getPolicy().canPickup(
                parcel.pickupTimeWindow, time.getStartTime(), parcel.pickupDuration);
    }

    @Override
    public PickupPointState getState() {
        return this;
    }
}
