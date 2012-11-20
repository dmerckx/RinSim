package rinde.sim.core.model.pdp.supported;

import java.util.ArrayList;
import java.util.List;

import rinde.sim.core.model.ModelManager;
import rinde.sim.core.model.interaction.InteractionModel;
import rinde.sim.core.model.interaction.guards.InteractiveGuard;
import rinde.sim.core.model.interaction.supported.InteractiveHolder;
import rinde.sim.core.model.pdp.PdpModel;
import rinde.sim.core.model.pdp.apis.ContainerAPI;
import rinde.sim.core.model.pdp.receivers.ContainerNotification;
import rinde.sim.core.model.pdp.receivers.DeliveryReceiver;
import rinde.sim.core.model.pdp.receivers.DeliverySpecificReceiver;
import rinde.sim.core.model.pdp.receivers.PickupReceiver;
import rinde.sim.core.model.pdp.users.Container;
import rinde.sim.core.model.pdp.visitors.PickupSpecificVisitor;
import rinde.sim.core.model.pdp.visitors.PickupVisitor;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.guards.RoadGuard;
import rinde.sim.core.model.road.supported.RoadHolder;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;

public class ContainerHolder<P extends Parcel> implements RoadHolder, InteractiveHolder, ContainerAPI<P>{
    
    private PdpModel pdpModel;
    private RoadGuard roadGuard;
    private InteractiveGuard<ContainerNotification> interactiveGuard;
    private Container<P> container;
    
    private ContainerState state = ContainerState.AVAILABLE;
    private long actionTime = 0;
    private double capacity;
    private List<P> load = new ArrayList<P>();
    
    private Class<P> parcelType;
    
    
    public ContainerHolder(Container<P> container, ModelManager manager) {
        this.container = container;
        
        pdpModel = manager.getModel(PdpModel.class);
        roadGuard = manager.getModel(RoadModel.class).makeGuard(container);
        interactiveGuard = manager.getModel(InteractionModel.class).makeGuard(container);
    }
    
    @SuppressWarnings("javadoc")
    protected void load(TimeLapse lapse, P parcel){
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

    @SuppressWarnings("hiding")
    @Override
    public void init(double capacity, Class<P> parcelType) {
        this.capacity = capacity;
        this.parcelType = parcelType;
    }
    
    @Override
    public List<P> getLoad() {
        return load;
    }
    
    @Override
    public double getCapacityLeft() {
        double result = capacity;
        for(P parcel:load) result -= parcel.magnitude;
        return result;
    }

    @Override
    public P tryPickup(TimeLapse lapse) {
        if(state != ContainerState.AVAILABLE) return null;
        
        PickupVisitor<P> visitor = new PickupVisitor<P>(parcelType, roadGuard.getLocation(), getCapacityLeft());
        P result = interactiveGuard.visit(lapse, visitor);
        if(result != null) load.add(result);
        return result;
    }

    @Override
    public boolean tryPickupOf(TimeLapse lapse, P parcel) {
        if(state != ContainerState.AVAILABLE) return false;
        
        PickupVisitor<P> visitor = new PickupSpecificVisitor<P>(parcel, roadGuard.getLocation(), getCapacityLeft());
        P result = interactiveGuard.visit(lapse, visitor);
        if(result != null) load.add(result);
        return result != null;
    }

    @Override
    public List<Parcel> scanLocation() {
        List<Parcel> result = new ArrayList<Parcel>();
        //TODO
        return result;
    }
    
    @Override
    public void acceptAll(TimeLapse lapse){
        DeliveryReceiver rec = 
                new DeliveryReceiver(roadGuard.getLocation(), parcelType, pdpModel, interactiveGuard);
        accept(lapse, rec);
    }
    
    @Override
    public void accept(TimeLapse lapse, List<P> parcels){
        DeliveryReceiver rec = 
                new DeliverySpecificReceiver(roadGuard.getLocation(), parcels, parcelType, pdpModel, interactiveGuard);
        accept(lapse, rec);
    }
    
    @SuppressWarnings("javadoc")
    protected void accept(TimeLapse lapse, DeliveryReceiver rec){
        switch(state){
            case ACCEPTING:
                interactiveGuard.removeAll();
                break;
            case ACCEPTING_ADVERTISING:
                interactiveGuard.removeAll(DeliveryReceiver.class);
                break;
            case ADVERTISING:
                state = ContainerState.ACCEPTING_ADVERTISING;
                break;
            default:
                return;
        }
        interactiveGuard.advertise(rec);
        doAction(lapse);
    }

    @Override
    public void advertiseAll(TimeLapse lapse) {
        advertise(lapse, load);
    }
    
    @Override
    public void advertise(TimeLapse lapse, List<P> parcels) {
        switch(state){
            case ADVERTISING:
                interactiveGuard.removeAll();
                break;
            case ACCEPTING_ADVERTISING:
                interactiveGuard.removeAll(PickupReceiver.class);
                break;
            case ACCEPTING:
                state = ContainerState.ACCEPTING_ADVERTISING;
                break;
            default:
                return;
        }
        
        PickupReceiver rec = new PickupReceiver(roadGuard.getLocation(), parcels, pdpModel, interactiveGuard);
        interactiveGuard.advertise(rec);
        doAction(lapse);
    }

    @Override
    public ContainerState getState() {
        return state;
    }
    
    
    // ----- HOLDER APIS ----- //
    
    @Override
    public InteractiveGuard<ContainerNotification> getInteractiveGuard() {
        return interactiveGuard;
    }

    @Override
    public RoadGuard getRoadGuard() {
        return roadGuard;
    }
    
    @Override
    public Container<P> getElement(){
        return container;
    }

    @Override
    public void tick(TimeLapse lapse) {
        doAction(lapse);
    }

    @Override
    public void afterTick(TimeInterval time) {
        interactiveGuard.afterTick(time);
    }
}
