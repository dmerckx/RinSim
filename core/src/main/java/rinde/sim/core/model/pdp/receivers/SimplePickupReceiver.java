package rinde.sim.core.model.pdp.receivers;

import java.util.ArrayList;
import java.util.List;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.interaction.Receiver;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.twpolicy.TimeWindowPolicy;
import rinde.sim.core.model.pdp.visitors.PickupVisitor;
import rinde.sim.core.simulation.TimeLapse;

public class SimplePickupReceiver<P extends Parcel> extends Receiver{

    private final P parcel;
    private final TimeWindowPolicy policy;
    
    /**
     * @param location The location of this receiver.
     * @param parcels The parcels available to be picked up by visitors.
     * @param policy The policy to apply on pickups.
     * @param guard The guard from which this receiver originates.
     */
    @SuppressWarnings("hiding")
    public SimplePickupReceiver(Point location, P parcel, TimeWindowPolicy policy) {
        super(location);
        this.parcel = parcel;
        this.policy = policy;
    }
    
    /**
     * @return The parcels available to be picked up by visitors.
     */
    public List<? extends Parcel> getParcels(){
        List<P> result = new ArrayList<P>();
        result.add(parcel);
        return result;
    }
    
    /**
     * @param lapse The time at which the action happens.
     * @param parcel The parcel that the approaching visitor wants to pick up.
     * @param visitor The approaching visitor.
     * @return Whether or not the visitor is allowed to pick up the given parcel.
     */
    public boolean canBePickedUp(TimeLapse lapse, Parcel parcel, PickupVisitor<?> visitor){
        return this.parcel == parcel &&
                policy.canPickup(parcel.pickupTimeWindow, lapse.getCurrentTime(), parcel.pickupDuration);
    }
    
    /**
     * @param parcel Pickup the specified parcel.
     */
    public void pickup(Parcel parcel){
        terminate();
    }
}
