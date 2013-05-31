package rinde.sim.core.model.pdp.apis;

import java.util.List;

import rinde.sim.core.model.Data;
import rinde.sim.core.model.interaction.Receiver;
import rinde.sim.core.model.interaction.apis.InteractionAPI;
import rinde.sim.core.model.interaction.users.InteractionUser;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.PdpModel;
import rinde.sim.core.model.pdp.apis.ContainerAPI.ContState;
import rinde.sim.core.model.pdp.apis.DeliveryAPI.DeliveryState;
import rinde.sim.core.model.pdp.receivers.DeliveryReceiver;
import rinde.sim.core.model.pdp.receivers.DeliverySpecificReceiver;
import rinde.sim.core.model.pdp.receivers.PickupReceiver;
import rinde.sim.core.model.pdp.users.Container;
import rinde.sim.core.model.pdp.users.ContainerData;
import rinde.sim.core.model.pdp.visitors.DeliveryVisitor;
import rinde.sim.core.model.pdp.visitors.PickupSpecificVisitor;
import rinde.sim.core.model.pdp.visitors.PickupVisitor;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.core.simulation.time.TimeLapseHandle;
import rinde.sim.util.concurrency.ListCache;
import rinde.sim.util.concurrency.StateCache;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class ContainerGuard extends ContainerState implements ContainerAPI, InteractionUser<Data>{
    
    private final PdpModel pdpModel;
    private final TimeLapseHandle handle;
    
    private final double capacity;
    private final Class<? extends Parcel> parcelType;

    private final ListCache<Parcel> load;
    private final StateCache<ContState> state;
    
    private RoadAPI roadAPI;
    private InteractionAPI interactiveAPI;
    
    public ContainerGuard(Container<?> user, ContainerData data, PdpModel model, TimeLapseHandle handle) {
        this.pdpModel = model;
        this.handle = handle;
        
        this.capacity = data.getCapacity();
        this.parcelType = data.getParcelType();
        
        this.load = new ListCache<Parcel>(handle);
        this.state = new ContainerStateCache(handle);
    }

    @Override
    public void setInteractionAPi(InteractionAPI api) {
        this.interactiveAPI = api;
    }

    @Override
    public void init(RoadAPI api) {
        assert roadAPI == null: "The roadApi can be set only ones";
        assert api != null: "The api can not be null";
        this.roadAPI = api;
    }
    
    private void load(Parcel parcel){
        assert handle.hasTimeLeft();
        
        handle.consume(parcel.pickupDuration);
        load.addValue(parcel);
    }
    
    private void unload(Parcel parcel){
        assert handle.hasTimeLeft();
        
        handle.consume(parcel.deliveryDuration);
        load.removeValue(parcel);
    }

    @Override
    public void notifyDone(Receiver rec){
        switch(state.getActualValue()){
            case ACCEPTING:
                Parcel pReceived = ((DeliveryReceiver) rec).getReceived();
                if(pReceived != null){
                    handle.consume(pReceived.deliveryDuration);
                    state.setValue(ContState.PICKING_UP, pReceived.deliveryDuration);
                    load.addValue(pReceived);
                }
                break;
            case ADVERTISING:
                Parcel pDelivered = ((PickupReceiver) rec).getPickedup();
                if(pDelivered != null){
                    handle.consume(pDelivered.deliveryDuration);
                    state.setValue(ContState.DELIVERING, pDelivered.pickupDuration);
                    load.removeValue(pDelivered);
                }
                break;
            default: throw new IllegalStateException();
        }
    }
    
    // ----- CONTAINER API ----- //

    @Override
    public ContState getCurrentContState() {
        return state.getActualValue();
    }

    @Override
    public ImmutableList<Parcel> getCurrentLoad() {
        return load.getActualValue();
    }
    
    @Override
    public double getCapacityLeft() {
        double result = capacity;
        for(Parcel parcel:load.getActualValue()) result -= parcel.magnitude;
        return result;
    }

    @Override
    public Parcel tryPickup(TimeLapse lapse) {
        assert handle == lapse;
        if(!handle.hasTimeLeft()) return null;
        
        PickupVisitor visitor = new PickupVisitor(parcelType,
                roadAPI.getCurrentLocation(), getCapacityLeft());
        
        Parcel result = interactiveAPI.visit(lapse, visitor);
        if(result != null) load(result);
        return result;
    }

    @Override
    public boolean tryPickupOf(TimeLapse lapse, Parcel parcel) {
        assert handle == lapse;
        if(!handle.hasTimeLeft()) return false;
        
        PickupVisitor visitor = new PickupSpecificVisitor(parcel,
                roadAPI.getCurrentLocation(), getCapacityLeft());
        Parcel result = interactiveAPI.visit(lapse, visitor);
        if(result != null) load(result);
        return result != null;
    }
    
    @Override
    public Parcel tryDelivery(TimeLapse lapse){
        assert handle == lapse;
        if(!handle.hasTimeLeft()) return null;
        
        DeliveryVisitor visitor =
                new DeliveryVisitor(roadAPI.getCurrentLocation(), load.getActualValue());
        
        Parcel result = interactiveAPI.visit(lapse, visitor);
        if(result != null) unload(result);
        return result;
    }
    
    @Override
    public boolean tryDeliveryOf(TimeLapse lapse, Parcel parcel){
        assert handle == lapse;
        assert load.getActualValue().contains(parcel);
        if(!handle.hasTimeLeft()) return false;
        
        DeliveryVisitor visitor =
                new DeliveryVisitor(roadAPI.getCurrentLocation(), Lists.newArrayList(parcel));
        
        Parcel result = interactiveAPI.visit(lapse, visitor);
        if(result != null) unload(result);
        return result != null;
    }

    @Override
    public void acceptAll(TimeLapse lapse){
        assert handle == lapse;
        if(interactiveAPI.isAdvertising()) return;
        if(!handle.hasTimeLeft()) return;
        
        DeliveryReceiver rec = new DeliveryReceiver(
                roadAPI.getCurrentLocation(), parcelType, pdpModel.getPolicy(), getCapacityLeft());
        interactiveAPI.advertise(rec);
    }
    
    @Override
    public void accept(TimeLapse lapse, List<Parcel> parcels){
        assert handle == lapse;
        if(interactiveAPI.isAdvertising()) return;
        if(!handle.hasTimeLeft()) return;
        
        DeliveryReceiver rec = new DeliverySpecificReceiver(
                roadAPI.getCurrentLocation(), parcels, pdpModel.getPolicy(), getCapacityLeft());
        interactiveAPI.advertise(rec);
    }

    @Override
    public void advertiseAll(TimeLapse lapse) {
        advertise(lapse, load.getActualValue());
    }
    
    @Override
    public void advertise(TimeLapse lapse, List<Parcel> parcels) {
        assert handle == lapse;
        assert load.getActualValue().containsAll(parcels);
        if(interactiveAPI.isAdvertising()) return;
        if(!handle.hasTimeLeft()) return;
        
        PickupReceiver rec = new PickupReceiver(
                roadAPI.getCurrentLocation(), parcels, pdpModel.getPolicy());
        interactiveAPI.advertise(rec);
    }
    
    @Override
    public void stopAdvertisingOrAccepting(){
        interactiveAPI.stopAdvertising();
    }
    
    @Override
    public ContainerState getState() {
        return this;
    }
    
    // ----- CONTAINER STATE ----- //

    @Override
    public ContState getContState() {
        return state.getFrozenValue();
    }

    @Override
    public ImmutableList<Parcel> getLoad() {
        return load.getFrozenValue();
    }
}

class State{
    public final ContState state;
    public final long waitTime;
    
    State(ContState state, long waitTime){
        this.state = state;
        this.waitTime = waitTime;
    }
}

class ContainerStateCache extends StateCache<ContState>{
    
    public ContainerStateCache(TimeLapseHandle handle) {
        super(ContState.AVAILABLE, handle);
    }

    @Override
    public ContState getState(ContState currentState, long time) {
        switch(currentState){
        case PICKING_UP:
            if(time > lastChangedTime + duration) return ContState.AVAILABLE;
            break;
        case DELIVERING:
            if(time > lastChangedTime + duration) return ContState.AVAILABLE;
            break;
        default:
            break;
        }
        return currentState;
    }
};