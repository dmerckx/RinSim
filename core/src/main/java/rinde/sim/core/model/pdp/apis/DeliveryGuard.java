package rinde.sim.core.model.pdp.apis;

import rinde.sim.core.model.Data;
import rinde.sim.core.model.InitUser;
import rinde.sim.core.model.interaction.Receiver;
import rinde.sim.core.model.interaction.apis.InteractionAPI;
import rinde.sim.core.model.interaction.users.InteractionUser;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.PdpModel;
import rinde.sim.core.model.pdp.receivers.DeliverySpecificReceiver;
import rinde.sim.core.model.pdp.users.DeliveryPoint;
import rinde.sim.core.model.pdp.users.DeliveryPointData;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.time.TimeLapseHandle;

import com.google.common.collect.Lists;

public class DeliveryGuard extends DeliveryPointState implements DeliveryAPI, InitUser, InteractionUser<Data>{

    private final PdpModel pdpModel;
    private final DeliveryPoint<?> user;
    private InteractionAPI interactionAPI;
    
    private final Parcel parcel;
    
    private long lastUpdatedState = -1;
    private long deliveredTime = -1;
    private DeliveryState state;
    
    private final TimeLapseHandle handle;
    
    public DeliveryGuard(DeliveryPoint<?> user, DeliveryPointData data, PdpModel model, TimeLapseHandle handle) {
        this.parcel = data.getParcel();
        this.pdpModel = model;
        this.handle = handle;
        this.user = user;
    }

    @Override
    public void setInteractionAPi(InteractionAPI api) {
        assert interactionAPI == null;
        this.interactionAPI = api;
    }

    @Override
    public void init() {
        //Advertise a receiver, waiting for the parcel to be delivered
        interactionAPI.advertise(
            new DeliverySpecificReceiver(parcel.destination, Lists.newArrayList(parcel), pdpModel.getPolicy()));
    }

    @Override
    public void notifyDone(Receiver receiver) {
        deliveredTime = handle.getCurrentTime();
        handle.consume(parcel.deliveryDuration);
        pdpModel.notifyParcelDelivery(user);
    }
    
    /**
     * State always remains the same during a single tick.
     */
    private void updateState(){
        if(lastUpdatedState == handle.getStartTime()) 
            return; //up to date 
        
        long time = handle.getStartTime();
        
        if(deliveredTime == -1){
            if(time < parcel.deliveryTimeWindow.begin)
                state = DeliveryState.SETTING_UP;
            else if(time < parcel.deliveryTimeWindow.end)
                state = DeliveryState.AVAILABLE;
            else 
                state = DeliveryState.LATE;
        }
        else {
            if(time < deliveredTime + parcel.deliveryDuration)
                state = DeliveryState.BEING_DELIVERED;
            else
                state = DeliveryState.DELIVERED;
        }
        
        lastUpdatedState = time;
    }
    
    // ----- DELIVERY API ----- //
    
    @Override
    public boolean isDelivered() {
        updateState();
        return state == DeliveryState.BEING_DELIVERED
                || state == DeliveryState.DELIVERED;
    }

    @Override
    public Parcel getParcel() {
        return parcel;
    }
    
    @Override
    public DeliveryState getDeliveryState() {
        updateState();
        return state;
    }

    @Override
    public boolean canBeDelivered(TimeInterval time) {
        return pdpModel.getPolicy().canDeliver(
                parcel.deliveryTimeWindow, time.getStartTime(), parcel.deliveryDuration);
    }
    
    @Override
    public DeliveryPointState getState() {
        return this;
    }
}
