package rinde.sim.core.model.pdp2.ports;

import java.util.List;

import rinde.sim.core.model.pdp2.PdpModel;
import rinde.sim.core.model.pdp2.actions.Delivery;
import rinde.sim.core.model.pdp2.actions.Pickup;
import rinde.sim.core.model.pdp2.apis.TruckAPI.TruckState;
import rinde.sim.core.model.pdp2.objects.Parcel2;
import rinde.sim.core.model.pdp2.users.Truck;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.core.simulation.types.Agent;

public abstract class ContainerPort implements AgentPort, ContainerAPI{
    
    private final RoadAPI roadAPI;
    private final PdpModel pdpModel;
    private final Truck truck;
    
    private double capacity;
    private List<Parcel2> load;
    private TruckState state;
    
    private boolean initialised = false;
    
    private long actionTime = 0;
    
    public TruckPort(Truck truck, PdpModel pdpModel, RoadAPI roadAPI) {
        this.pdpModel = pdpModel;
        this.roadAPI = roadAPI;
        
        this.truck = truck;
        this.state = TruckState.IDLE;
    }

    @Override
    public Agent getAgent() {
        return truck;
    }
    
    @Override
    public void init(double capacity) {
        if(initialised) throw new IllegalStateException("Already initialised");
        
        this.capacity = capacity;
        
        initialised = true;
    }

    @Override
    public TruckState getState() {
        return state;
    }

    @Override
    public List<Parcel2> getLoad() {
        return load;
    }

    @Override
    public Parcel2 tryPickup(TimeLapse lapse) {
        Parcel2 result = roadAPI.visitNode(new Pickup());
        
        if( result == null )
            return null;
        
        state = TruckState.PICKING_UP;
        actionTime = result.pickupDuration;
        
        doAction(lapse);
        
        return result;
    }
    
    @Override
    public boolean tryDelivery(TimeLapse lapse) {
        Parcel2 parcel = null;
        
        for(Parcel2 p:load){
            if( roadAPI.visitNode(new Delivery(p)) ){
                parcel = p;
                break;
            }
        }
        
        if(parcel == null) return false;
        
        state = TruckState.DELIVERING;
        actionTime = parcel.deliveryDuration;
        
        doAction(lapse);
        return true;
    }
    
    private void doAction(TimeLapse lapse){
        if(actionTime == 0) return;
        
        if( lapse.getTimeLeft() >= actionTime){
            lapse.consume(actionTime);
            actionTime = 0;
            state = TruckState.IDLE;
        }
        else {
            actionTime -= lapse.getTimeLeft();
            lapse.consumeAll();
        }
    }

    @Override
    public List<Parcel2> scanLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void tick(TimeLapse lapse) {
        doAction(lapse);
    }
}
