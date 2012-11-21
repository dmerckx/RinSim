package rinde.sim.core.model.pdp.guards;

import java.util.ArrayList;
import java.util.List;

import rinde.sim.core.model.interaction.apis.InteractiveAPI;
import rinde.sim.core.model.interaction.guards.InteractiveGuard;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.PdpAPI;
import rinde.sim.core.model.pdp.PdpModel;
import rinde.sim.core.model.pdp.apis.ContainerAPI;
import rinde.sim.core.model.pdp.receivers.DeliveryReceiver;
import rinde.sim.core.model.pdp.receivers.DeliverySpecificReceiver;
import rinde.sim.core.model.pdp.receivers.PickupReceiver;
import rinde.sim.core.model.pdp.twpolicy.TimeWindowPolicy;
import rinde.sim.core.model.pdp.users.Container;
import rinde.sim.core.model.pdp.visitors.PickupSpecificVisitor;
import rinde.sim.core.model.pdp.visitors.PickupVisitor;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.model.road.guards.RoadGuard;
import rinde.sim.core.simulation.TimeLapse;

public class ContainerGuard<P extends Parcel> implements ContainerAPI<P>, PdpAPI{
    
    private PdpModel pdpModel;
    private RoadGuard roadGuard;
    private InteractiveGuard interactiveGuard;
    private Container<P> container;
    
    private ContainerState state = ContainerState.AVAILABLE;
    private long actionTime = 0;
    private double capacity;
    private List<P> load = new ArrayList<P>();
    
    private Class<P> parcelType;
    
    public ContainerGuard(Container<P> container, RoadAPI roadAPI,
            InteractiveAPI interactiveAPI, PdpModel model) {
        this.container = container;
    }
    
    public void tick(TimeLapse lapse){
        //TODO
        
        doAction(lapse);
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

    @Override
    public TimeWindowPolicy getPolicy() {
        return pdpModel.getPolicy();
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
        
        PickupVisitor<P> visitor = new PickupVisitor<P>(parcelType,
                roadGuard.getLocation(), getCapacityLeft());
        P result = interactiveGuard.visit(lapse, visitor);
        if(result != null) load.add(result);
        return result;
    }

    @Override
    public boolean tryPickupOf(TimeLapse lapse, P parcel) {
        if(state != ContainerState.AVAILABLE) return false;
        
        PickupVisitor<P> visitor = new PickupSpecificVisitor<P>(parcel,
                roadGuard.getLocation(), getCapacityLeft());
        P result = interactiveGuard.visit(lapse, visitor);
        if(result != null) load.add(result);
        return result != null;
    }

    @Override
    public void acceptAll(TimeLapse lapse){
        DeliveryReceiver rec = new DeliveryReceiver(roadGuard.getLocation(),
                parcelType, pdpModel.getPolicy());
        accept(lapse, rec);
    }
    
    @Override
    public void accept(TimeLapse lapse, List<P> parcels){
        DeliveryReceiver rec = new DeliverySpecificReceiver(
                roadGuard.getLocation(), parcels, parcelType,
                pdpModel.getPolicy());
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

        PickupReceiver rec = new PickupReceiver(roadGuard.getLocation(),
                parcels, pdpModel.getPolicy());
        interactiveGuard.advertise(rec);
        doAction(lapse);
    }

    @Override
    public ContainerState getState() {
        return state;
    }
}
