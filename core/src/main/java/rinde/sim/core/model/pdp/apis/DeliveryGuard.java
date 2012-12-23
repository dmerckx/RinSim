package rinde.sim.core.model.pdp.apis;

import java.util.ArrayList;
import java.util.List;

import rinde.sim.TickGuard;
import rinde.sim.core.model.Data;
import rinde.sim.core.model.InitGuard;
import rinde.sim.core.model.interaction.Notification;
import rinde.sim.core.model.interaction.apis.InteractionAPI;
import rinde.sim.core.model.interaction.users.InteractionUser;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.PdpModel;
import rinde.sim.core.model.pdp.receivers.ContainerNotification;
import rinde.sim.core.model.pdp.receivers.DeliverySpecificReceiver;
import rinde.sim.core.model.pdp.users.DeliveryPoint;
import rinde.sim.core.model.pdp.users.DeliveryPointData;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;

public class DeliveryGuard extends DeliveryPointState implements DeliveryAPI, InitGuard, TickGuard, InteractionUser<Data>{

    private Parcel parcel;
    private InteractionAPI interactionAPI;
    private PdpModel pdpModel;
    private DeliveryPoint<?> user;
    

    private boolean delivered = false;
    private long deliveryTime = 0;
    private DeliveryState state = DeliveryState.SETTING_UP;
    
    public DeliveryGuard(DeliveryPoint<?> user, DeliveryPointData data, PdpModel model) {
        this.user = user;
        this.parcel = data.getParcel();
        this.pdpModel = model;
    }

    @Override
    public void setInteractionAPi(InteractionAPI api) {
        this.interactionAPI = api;
    }
    
    @Override
    public boolean isDelivered() {
        return state == DeliveryState.BEING_DELIVERED
                || state == DeliveryState.DELIVERED;
    }

    @Override
    public Parcel getParcel() {
        return parcel;
    }

    @Override
    public DeliveryState getDeliveryState() {
        return state;
    }
    
    public void setState(TimeInterval interval){
        if(!delivered){
            if(interval.getEndTime() < parcel.deliveryTimeWindow.begin){
                state = DeliveryState.SETTING_UP;
            } 
            else if(interval.getEndTime() < parcel.deliveryTimeWindow.end){
                state = DeliveryState.AVAILABLE;
            }
            else {
                state = DeliveryState.LATE;
            }
        }
        else{
            if(deliveryTime == 0){
                state = DeliveryState.DELIVERED;
            }
            else{
                state = DeliveryState.BEING_DELIVERED;
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
        for(Notification n:interactionAPI.getNotifications()){
            if(n instanceof ContainerNotification){
                assert parcel == ((ContainerNotification) n).getParcel();
                delivered = true;
                pdpModel.notifyParcelDelivery(user);
                deliveryTime = parcel.deliveryDuration;
            }
        }
        setState(time);
        
        switch(state){
            case BEING_DELIVERED:
                if(deliveryTime > time.getTimeLeft()){
                    deliveryTime -= time.getTimeLeft(); 
                    time.consumeAll();
                }
                else{
                    time.consume(deliveryTime);
                    deliveryTime = 0;
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
        
    }

    @Override
    public DeliveryPointState getState() {
        return this;
    }
}
