package rinde.sim.core.model.pdp.apis;

import java.util.ArrayList;
import java.util.List;

import rinde.sim.core.model.Data;
import rinde.sim.core.model.interaction.apis.InteractionAPI;
import rinde.sim.core.model.interaction.users.InteractionUser;
import rinde.sim.core.model.pdp.PdpModel;
import rinde.sim.core.model.pdp.receivers.DeliveryReceiver;
import rinde.sim.core.model.pdp.receivers.DeliverySpecificReceiver;
import rinde.sim.core.model.pdp.receivers.PickupReceiver;
import rinde.sim.core.model.pdp.users.Container;
import rinde.sim.core.model.pdp.users.ContainerData;
import rinde.sim.core.model.pdp.users.Parcel;
import rinde.sim.core.model.pdp.visitors.PickupSpecificVisitor;
import rinde.sim.core.model.pdp.visitors.PickupVisitor;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.simulation.TimeLapse;

public class ContainerGuard implements ContainerAPI, InteractionUser<Data>{
    
    private PdpModel pdpModel;
    private RoadAPI roadAPI;
    private InteractionAPI interactiveAPI;
    private Container<?> container;
    
    private ContainerState state = ContainerState.AVAILABLE;
    private long actionTime = 0;
    private double capacity;
    private List<Parcel> load = new ArrayList<Parcel>();
    
    private Class<? extends Parcel> parcelType;
    
    public ContainerGuard(Container<?> user, ContainerData data, PdpModel model, RoadAPI roadAPI) {
        this.container = user;
        this.roadAPI = roadAPI;
        
        this.capacity = data.getCapacity();
        this.parcelType = data.getParcelType();
    }

    @Override
    public void setInteractionAPi(InteractionAPI api) {
        this.interactiveAPI = api;
    }
    
    public void tick(TimeLapse lapse){
        //TODO
        
        doAction(lapse);
    }
    
    @SuppressWarnings("javadoc")
    protected void load(TimeLapse lapse, Parcel parcel){
        state = ContainerState.PICKING_UP;
        actionTime = parcel.pickupDuration;
        doAction(lapse);
    }
    
    @SuppressWarnings("javadoc")
    protected void doAction(TimeLapse lapse){
        if(state == ContainerState.ADVERTISING){
            lapse.consumeAll();
            return;
        }
        
        if(actionTime == 0) return;
        
        if( lapse.getTimeLeft() >= actionTime){
            lapse.consume(actionTime);
            actionTime = 0;
            state = ContainerState.AVAILABLE;
        }
        else {
            actionTime -= lapse.getTimeLeft();
            lapse.consumeAll();
        }
    }

    @Override
    public List<Parcel> getLoad() {
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
        if(state != ContainerState.AVAILABLE) return null;
        
        PickupVisitor visitor = new PickupVisitor(parcelType,
                roadAPI.getLocation(), getCapacityLeft());
        Parcel result = interactiveAPI.visit(lapse, visitor);
        if(result != null) load.add(result);
        return result;
    }

    @Override
    public boolean tryPickupOf(TimeLapse lapse, Parcel parcel) {
        if(state != ContainerState.AVAILABLE) return false;
        
        PickupVisitor visitor = new PickupSpecificVisitor(parcel,
                roadAPI.getLocation(), getCapacityLeft());
        Parcel result = interactiveAPI.visit(lapse, visitor);
        if(result != null) load.add(result);
        return result != null;
    }

    @Override
    public void acceptAll(TimeLapse lapse){
        DeliveryReceiver rec = new DeliveryReceiver(roadAPI.getLocation(),
                parcelType, pdpModel.getPolicy());
        accept(lapse, rec);
    }
    
    @Override
    public void accept(TimeLapse lapse, List<Parcel> parcels){
        DeliveryReceiver rec = new DeliverySpecificReceiver(
                roadAPI.getLocation(), parcels, parcelType,
                pdpModel.getPolicy());
        accept(lapse, rec);
    }
    
    @SuppressWarnings("javadoc")
    protected void accept(TimeLapse lapse, DeliveryReceiver rec){
        switch(state){
            case ACCEPTING:
                interactiveAPI.removeAll();
                break;
            case ACCEPTING_ADVERTISING:
                interactiveAPI.removeAll(DeliveryReceiver.class);
                break;
            case ADVERTISING:
                state = ContainerState.ACCEPTING_ADVERTISING;
                break;
            default:
                return;
        }
        interactiveAPI.advertise(rec);
        doAction(lapse);
    }

    @Override
    public void advertiseAll(TimeLapse lapse) {
        advertise(lapse, load);
    }
    
    @Override
    public void advertise(TimeLapse lapse, List<Parcel> parcels) {
        switch(state){
            case ADVERTISING:
                interactiveAPI.removeAll();
                break;
            case ACCEPTING_ADVERTISING:
                interactiveAPI.removeAll(PickupReceiver.class);
                break;
            case ACCEPTING:
                state = ContainerState.ACCEPTING_ADVERTISING;
                break;
            default:
                return;
        }

        PickupReceiver rec = new PickupReceiver(roadAPI.getLocation(),
                parcels, pdpModel.getPolicy());
        interactiveAPI.advertise(rec);
        doAction(lapse);
    }

    @Override
    public ContainerState getState() {
        return state;
    }
}
