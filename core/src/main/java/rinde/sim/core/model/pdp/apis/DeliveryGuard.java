package rinde.sim.core.model.pdp.apis;

import rinde.sim.core.model.Data;
import rinde.sim.core.model.InitUser;
import rinde.sim.core.model.interaction.Receiver;
import rinde.sim.core.model.interaction.apis.InteractionAPI;
import rinde.sim.core.model.interaction.users.InteractionUser;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.PdpModel;
import rinde.sim.core.model.pdp.apis.DeliveryAPI.DeliveryState;
import rinde.sim.core.model.pdp.receivers.DeliveryReceiver;
import rinde.sim.core.model.pdp.receivers.DeliverySpecificReceiver;
import rinde.sim.core.model.pdp.users.DeliveryPoint;
import rinde.sim.core.model.pdp.users.DeliveryPointData;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.time.TimeLapseHandle;
import rinde.sim.util.concurrency.StateCache;

import com.google.common.collect.Lists;

public class DeliveryGuard extends DeliveryPointState implements DeliveryAPI, InitUser, InteractionUser<Data>{

    private final PdpModel pdpModel;
    private final DeliveryPoint<?> user;
    private final TimeLapseHandle handle;
    
    private final Parcel parcel;
    private final StateCache<DeliveryState> state;
    
    private InteractionAPI interactionAPI;
    
    public DeliveryGuard(DeliveryPoint<?> user, DeliveryPointData data, PdpModel model, TimeLapseHandle handle) {
        this.parcel = data.getParcel();
        this.pdpModel = model;
        this.user = user;
        this.handle = handle;
        this.state = new DeliveryStateCache(handle, parcel);
    }

    @Override
    public void setInteractionAPi(InteractionAPI api) {
        assert interactionAPI == null;
        this.interactionAPI = api;
    }

    @Override
    public void init() {
        //Advertise a receiver, waiting for the parcel to be delivered
        makeAvailable();
    }

    @Override
    public void notifyDone(Receiver rec) {
        handle.consume(parcel.deliveryDuration);
        state.setValue(DeliveryState.BEING_DELIVERED);
        pdpModel.notifyParcelDelivery(user);
    }
    
    // ----- DELIVERY API ----- //
    
    @Override
    public boolean isDelivered() {
        return state.getActualValue() == DeliveryState.BEING_DELIVERED
                || state.getActualValue() == DeliveryState.DELIVERED;
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

    @Override
    public void setCustomReceiver(DeliveryReceiver rec) {
        interactionAPI.advertise(rec);
    }

    @Override
    public void makeAvailable() {
        interactionAPI.advertise(
                new DeliverySpecificReceiver(parcel.destination, Lists.newArrayList(parcel), pdpModel.getPolicy(), parcel.magnitude));
    }

    @Override
    public void makeUnavailable() {
        interactionAPI.stopAdvertising();
    }
    
    // ----- DELIVERY POINT STATE ----- //

    @Override
    public DeliveryState getDeliveryState() {
        return state.getFrozenValue();
    }

    @Override
    public Parcel getParcel() {
        return parcel;
    }
}


class DeliveryStateCache extends StateCache<DeliveryState>{
    private final Parcel parcel;
    
    public DeliveryStateCache(TimeLapseHandle handle, Parcel parcel) {
        super(DeliveryState.SETTING_UP, handle);
        this.parcel = parcel;
    }

    @Override
    public DeliveryState getState(DeliveryState currentState, long time) {
        switch(currentState){
        case SETTING_UP:
            if(time > parcel.deliveryTimeWindow.begin) return DeliveryState.AVAILABLE;
            break;
        case AVAILABLE:
            if(time > parcel.deliveryTimeWindow.end) return DeliveryState.LATE;
            break;
        case BEING_DELIVERED:
            if(time > lastChangedTime + parcel.deliveryDuration) return DeliveryState.DELIVERED;
            break;
        default:
            break;
        }
        return currentState;
    }
};