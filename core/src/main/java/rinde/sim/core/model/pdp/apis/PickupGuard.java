package rinde.sim.core.model.pdp.apis;

import rinde.sim.core.model.Data;
import rinde.sim.core.model.InitUser;
import rinde.sim.core.model.interaction.Receiver;
import rinde.sim.core.model.interaction.apis.InteractionAPI;
import rinde.sim.core.model.interaction.users.InteractionUser;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.PdpModel;
import rinde.sim.core.model.pdp.apis.PickupAPI.PickupState;
import rinde.sim.core.model.pdp.receivers.PickupReceiver;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.pdp.users.PickupPointData;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.time.TimeLapseHandle;
import rinde.sim.util.concurrency.StateCache;

import com.google.common.collect.Lists;

public class PickupGuard extends PickupPointState implements PickupAPI, InitUser, InteractionUser<Data>{
    
    private final PdpModel pdpModel;
    private final PickupPoint<?> user;
    private final TimeLapseHandle handle;
    
    private final Parcel parcel;
    private final StateCache<PickupState> state;
    
    private InteractionAPI interactionAPI;

    public PickupGuard(PickupPoint<?> user, PickupPointData data, PdpModel model, TimeLapseHandle handle) {
        this.parcel = data.getParcel();
        this.pdpModel = model;
        this.user = user;
        this.handle = handle;
        this.state = new PickupStateCache(handle, parcel);
    }

    @Override
    public void setInteractionAPi(InteractionAPI api) {
        assert interactionAPI == null;
        this.interactionAPI = api;
    }

    @Override
    public void init() {
        //Advertise a receiver, waiting for the parcel to be picked up
        makeAvailable();
    }

    @Override
    public void notifyDone(Receiver rec) {
        handle.consume(parcel.pickupDuration);
        state.setValue(PickupState.BEING_PICKED_UP);
        pdpModel.notifyParcelPickup(user);
    }
    
    // ----- PICKUP API ----- //
    
    @Override
    public boolean isPickedUp() {
        return state.getActualValue() == PickupState.BEING_PICKED_UP 
                || state.getActualValue() == PickupState.PICKED_UP;
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

    @Override
    public void setCustomReceiver(PickupReceiver rec) {
        interactionAPI.advertise(rec);
    }
    
    @Override
    public void makeAvailable() {
        interactionAPI.advertise(
                new PickupReceiver(parcel.location, Lists.newArrayList(parcel), pdpModel.getPolicy()));
    }

    @Override
    public void makeUnavailable() {
        interactionAPI.stopAdvertising();
    }
    
    // ----- PICKUP POINT STATE ----- //

    @Override
    public PickupState getPickupState() {
        return state.getFrozenValue();
    }
    
    @Override
    public Parcel getParcel() {
        return parcel;
    }
}

class PickupStateCache extends StateCache<PickupState>{
    private final Parcel parcel;
    
    public PickupStateCache(TimeLapseHandle handle, Parcel parcel) {
        super(PickupState.SETTING_UP, handle);
        this.parcel = parcel;
    }

    @Override
    public PickupState getState(PickupState currentState, long time) {
        switch(currentState){
        case SETTING_UP:
            if(time > parcel.pickupTimeWindow.begin) return PickupState.AVAILABLE;
            break;
        case AVAILABLE:
            if(time > parcel.pickupTimeWindow.end) return PickupState.LATE;
            break;
        case BEING_PICKED_UP:
            if(time > lastChangedTime + parcel.pickupDuration) return PickupState.PICKED_UP;
            break;
        default:
            break;
        }
        return currentState;
    }
};
