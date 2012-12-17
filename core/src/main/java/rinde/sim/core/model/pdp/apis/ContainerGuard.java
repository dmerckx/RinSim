package rinde.sim.core.model.pdp.apis;

import java.util.ArrayList;
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
import rinde.sim.core.model.pdp.visitors.PickupSpecificVisitor;
import rinde.sim.core.model.pdp.visitors.PickupVisitor;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.simulation.TimeLapse;

public class ContainerGuard extends ContainerState implements ContainerAPI, InteractionUser<Data>{
    
    private PdpModel pdpModel;
    private RoadAPI roadAPI;
    private InteractionAPI interactiveAPI;
    private Container<?> container;
    
    private ContState state = ContState.AVAILABLE;
    private long actionTime = 0;
    private double capacity;
    private List<Parcel> load = new ArrayList<Parcel>();
    
    private Class<? extends Parcel> parcelType;
    
    public ContainerGuard(Container<?> user, ContainerData data, PdpModel model) {
        this.container = user;
        this.capacity = data.getCapacity();
        this.parcelType = data.getParcelType();
    }

    @Override
    public void setInteractionAPi(InteractionAPI api) {
        this.interactiveAPI = api;
    }

    @Override
    public void init(RoadAPI api) {
        assert api != null: "The api can not be null";
        
        this.roadAPI = api;
    }
    
    public void tick(TimeLapse lapse){
        //TODO
        
        doAction(lapse);
    }
    
    @SuppressWarnings("javadoc")
    protected void load(TimeLapse lapse, Parcel parcel){
        state = ContState.PICKING_UP;
        actionTime = parcel.pickupDuration;
        doAction(lapse);
    }
    
    @SuppressWarnings("javadoc")
    protected void doAction(TimeLapse lapse){
        if(state == ContState.ADVERTISING){
            lapse.consumeAll();
            return;
        }
        
        if(actionTime == 0) return;
        
        if( lapse.getTimeLeft() >= actionTime){
            lapse.consume(actionTime);
            actionTime = 0;
            state = ContState.AVAILABLE;
        }
        else {
            actionTime -= lapse.getTimeLeft();
            lapse.consumeAll();
        }
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
        assert roadAPI != null: "init must be called while receiving this container api";
        
        if(state != ContState.AVAILABLE) return null;
        
        PickupVisitor visitor = new PickupVisitor(parcelType,
                roadAPI.getCurrentLocation(), getCapacityLeft());
        Parcel result = interactiveAPI.visit(lapse, visitor);
        if(result != null) load.add(result);
        return result;
    }

    @Override
    public boolean tryPickupOf(TimeLapse lapse, Parcel parcel) {
        assert roadAPI != null: "init must be called while receiving this container api";
        
        if(state != ContState.AVAILABLE) return false;
        
        PickupVisitor visitor = new PickupSpecificVisitor(parcel,
                roadAPI.getCurrentLocation(), getCapacityLeft());
        Parcel result = interactiveAPI.visit(lapse, visitor);
        if(result != null) load.add(result);
        return result != null;
    }

    @Override
    public void acceptAll(TimeLapse lapse){
        assert roadAPI != null: "init must be called while receiving this container api";
        
        DeliveryReceiver rec = new DeliveryReceiver(roadAPI.getCurrentLocation(),
                parcelType, pdpModel.getPolicy());
        accept(lapse, rec);
    }
    
    @Override
    public void accept(TimeLapse lapse, List<Parcel> parcels){
        assert roadAPI != null: "init must be called while receiving this container api";
        
        DeliveryReceiver rec = new DeliverySpecificReceiver(
                roadAPI.getCurrentLocation(), parcels,
                pdpModel.getPolicy());
        accept(lapse, rec);
    }

    @Override
    public void advertiseAll(TimeLapse lapse) {
        assert roadAPI != null: "init must be called while receiving this container api";
        
        advertise(lapse, load);
    }
    
    @Override
    public void advertise(TimeLapse lapse, List<Parcel> parcels) {
        assert roadAPI != null: "init must be called while receiving this container api";
        
        switch(state){
            case ADVERTISING:
                interactiveAPI.removeAll();
                break;
            case ACCEPTING_ADVERTISING:
                interactiveAPI.removeAll(PickupReceiver.class);
                break;
            case ACCEPTING:
                state = ContState.ACCEPTING_ADVERTISING;
                break;
            default:
                return;
        }

        PickupReceiver rec = new PickupReceiver(roadAPI.getCurrentLocation(),
                parcels, pdpModel.getPolicy());
        interactiveAPI.advertise(rec);
        doAction(lapse);
    }

    @Override
    public ContState getContState() {
        return state;
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
                state = ContState.ACCEPTING_ADVERTISING;
                break;
            default:
                return;
        }
        interactiveAPI.advertise(rec);
        doAction(lapse);
    }

    @Override
    public List<Parcel> getLoad() {
        //TODO
        return null;
    }

    @Override
    public ContainerState getState() {
        return this;
    }
}
