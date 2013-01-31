package rinde.sim.core.model.pdp.apis;

import java.util.List;

import rinde.sim.core.model.Data;
import rinde.sim.core.model.interaction.apis.InteractionAPI;
import rinde.sim.core.model.interaction.users.InteractionUser;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.PdpModel;
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

import com.google.common.collect.Lists;

public class ContainerGuard extends ContainerState implements ContainerAPI, InteractionUser<Data>{
    
    private PdpModel pdpModel;
    private RoadAPI roadAPI;
    private InteractionAPI interactiveAPI;
    
    private final double capacity;
    private final Class<? extends Parcel> parcelType;
    
    private List<Parcel> load = Lists.newArrayList();
    
    private long backupTime;
    private List<Parcel> backupLoad = Lists.newArrayList();
    private ContState backupState;
    
    private Action lastAction;
    private long lastActionEnd;
    
    private final TimeLapseHandle handle;
    
    public ContainerGuard(Container<?> user, ContainerData data, PdpModel model, TimeLapseHandle handle) {
        this.capacity = data.getCapacity();
        this.parcelType = data.getParcelType();
        this.handle = handle;
        updateBackup();
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
        load.add(parcel);
        lastAction = Action.LOAD;
        lastActionEnd = handle.getSchedualedUntil();
    }
    
    private void unload(Parcel parcel){
        assert handle.hasTimeLeft();
        
        handle.consume(parcel.deliveryDuration);
        load.remove(parcel);
        lastAction = Action.UNLOAD;
        lastActionEnd = handle.getSchedualedUntil();
    }
    
    private void updateBackup(){
        if(backupTime == handle.getStartTime())
            return;
        
        backupTime = handle.getStartTime();
        backupState = getCurrentContState(handle.getStartTime());
        backupLoad = Lists.newArrayList(load);
    }
    
    private ContState getCurrentContState(long time){
        if(interactiveAPI.isAdvertising()){
            switch(lastAction){
                case ACCEPT: return ContState.ACCEPTING;
                case ADVERTISE: return ContState.ADVERTISING;
                default: throw new IllegalStateException();
            }
        }
        
        if(lastAction == null)
            return ContState.AVAILABLE;
        
        switch (lastAction) {
            case LOAD:
                if(time < lastActionEnd)
                    return ContState.PICKING_UP;
                return ContState.AVAILABLE;
            case UNLOAD:
                if(time < lastActionEnd)
                    return ContState.DELIVERING;
                return ContState.AVAILABLE;
            default: throw new IllegalStateException();
        }
    }
    
    // ----- CONTAINER API ----- //

    @Override
    public ContState getCurrentContState() {
        updateBackup();
        return getCurrentContState(handle.getCurrentTime());
    }

    @Override
    public List<Parcel> getCurrentLoad() {
        return load;
    }
    
    @Override
    public double getCapacityLeft() {
        double result = capacity;
        for(Parcel parcel:load) result -= parcel.magnitude;
        return result;
    }

    @Override
    public Parcel tryPickup(TimeLapse lapse) {
        assert handle == lapse;
        if(!handle.hasTimeLeft()) return null;
        updateBackup();
        
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
        updateBackup();
        
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
        updateBackup();
        
        DeliveryVisitor visitor =
                new DeliveryVisitor(roadAPI.getCurrentLocation(), load);
        
        Parcel result = interactiveAPI.visit(lapse, visitor);
        if(result != null) unload(result);
        return result;
    }
    
    @Override
    public boolean tryDeliveryOf(TimeLapse lapse, Parcel parcel){
        assert handle == lapse;
        assert load.contains(parcel);
        if(!handle.hasTimeLeft()) return false;
        updateBackup();
        
        DeliveryVisitor visitor =
                new DeliveryVisitor(roadAPI.getCurrentLocation(), Lists.newArrayList(parcel));
        
        Parcel result = interactiveAPI.visit(lapse, visitor);
        if(result != null) unload(result);
        return result != null;
    }

    @Override
    public void acceptAll(TimeLapse lapse){
        if(interactiveAPI.isAdvertising()) return;
        if(!handle.hasTimeLeft()) return;
        updateBackup();
        
        DeliveryReceiver rec = new DeliveryReceiver(
                roadAPI.getCurrentLocation(), parcelType, pdpModel.getPolicy());
        interactiveAPI.advertise(rec);
        lastAction = Action.ACCEPT;
    }
    
    @Override
    public void accept(TimeLapse lapse, List<Parcel> parcels){
        if(interactiveAPI.isAdvertising()) return;
        if(!handle.hasTimeLeft()) return;
        updateBackup();
        
        DeliveryReceiver rec = new DeliverySpecificReceiver(
                roadAPI.getCurrentLocation(), parcels, pdpModel.getPolicy());
        interactiveAPI.advertise(rec);
        lastAction = Action.ACCEPT;
    }

    @Override
    public void advertiseAll(TimeLapse lapse) {
        advertise(lapse, load);
    }
    
    @Override
    public void advertise(TimeLapse lapse, List<Parcel> parcels) {
        if(interactiveAPI.isAdvertising()) return;
        if(!handle.hasTimeLeft()) return;
        updateBackup();
        
        PickupReceiver rec = new PickupReceiver(
                roadAPI.getCurrentLocation(), parcels, pdpModel.getPolicy());
        interactiveAPI.advertise(rec);
        lastAction = Action.ADVERTISE;
    }
    
    @Override
    public void stopAdvertisingOrAccepting(){
        updateBackup();
        interactiveAPI.stopAdvertising();
    }
    
    @Override
    public ContainerState getState() {
        return this;
    }
    
    // ----- CONTAINER STATE ----- //

    @Override
    public ContState getContState() {
        updateBackup();
        return backupState;
    }

    @Override
    public List<Parcel> getLoad() {
        updateBackup();
        return backupLoad;
    }

}

enum Action{
    LOAD,
    UNLOAD,
    ADVERTISE,
    ACCEPT
}