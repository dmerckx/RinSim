package rinde.sim.core.model.pdp.guards;

import java.util.List;

import rinde.sim.core.model.Guard;
import rinde.sim.core.model.pdp.PdpModel;
import rinde.sim.core.model.pdp.apis.TruckAPI;
import rinde.sim.core.model.pdp.receivers.Delivery;
import rinde.sim.core.model.pdp.supported.Parcel;
import rinde.sim.core.model.pdp.users.Truck;
import rinde.sim.core.model.pdp.visitors.PickupVisitor;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.core.simulation.types.Agent;

public class TruckPort implements Guard, TruckAPI{

    private final RoadAPI roadAPI;
    private final PdpModel pdpModel;
    private final Truck truck;
    
    private double capacity;
    private List<Parcel> load;
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
    public List<Parcel> getLoad() {
        return load;
    }

    @Override
    public Parcel tryPickup(TimeLapse lapse) {
        Parcel result = roadAPI.visitNode(new PickupVisitor());
        
        if( result == null )
            return null;
        
        state = TruckState.PICKING_UP;
        actionTime = result.pickupDuration;
        
        doAction(lapse);
        
        return result;
    }
    
    @Override
    public boolean tryDelivery(TimeLapse lapse) {
        Parcel parcel = null;
        
        for(Parcel p:load){
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
    public List<Parcel> scanLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void tick(TimeLapse lapse) {
        doAction(lapse);
    }
}
