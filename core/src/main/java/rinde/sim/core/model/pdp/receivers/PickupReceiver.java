package rinde.sim.core.model.pdp.receivers;

import java.util.List;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.interaction.Receiver;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.twpolicy.TimeWindowPolicy;
import rinde.sim.core.model.pdp.visitors.PickupVisitor;
import rinde.sim.core.simulation.TimeLapse;

/**
 * This class represents a {@link Receiver} advertising a number of {@link Parcel}s.
 * Visitors can approach this receiver to check whether or not it contains parcels
 * that they require and to pick up one of these parcels.
 * 
 * As soon as a visitor picks up one of the parcels this receiver will send a 
 * notification and terminate. This makes it impossible to pick up multiple parcels at
 * the same time.
 * 
 * @author dmerckx
 */
public class PickupReceiver extends Receiver {

    private final List<? extends Parcel> parcels;
    private final TimeWindowPolicy policy;
    
    /**
     * @param location The location of this receiver.
     * @param parcels The parcels available to be picked up by visitors.
     * @param policy The policy to apply on pickups.
     */
    @SuppressWarnings("hiding")
    public PickupReceiver(Point location, List<? extends Parcel> parcels, TimeWindowPolicy policy) {
        super(location);
        this.parcels = parcels;
        this.policy = policy;
    }
    
    /**
     * @return The parcels available to be picked up by visitors.
     */
    public List<? extends Parcel> getParcels(){
        return parcels;
    }
    
    /**
     * @param lapse The time at which the action happens.
     * @param parcel The parcel that the approaching visitor wants to pick up.
     * @param visitor The approaching visitor.
     * @return Whether or not the visitor is allowed to pick up the given parcel.
     */
    public boolean canBePickedUp(TimeLapse lapse, Parcel parcel, PickupVisitor visitor){
        if(terminated) return false;
        
        return parcels.contains(parcel) &&
                policy.canPickup(parcel.pickupTimeWindow, lapse.getCurrentTime(), parcel.pickupDuration);
    }
    
    /**
     * @param parcel Pickup the specified parcel.
     */
    public void pickup(TimeLapse lapse, Parcel parcel){
        assert !terminated;
        
        //The pickup point will have to stay unavailable until the pickup duration is over
        long timeCost = parcel.pickupDuration - lapse.getTimeLeft();
        terminate(timeCost > 0 ? timeCost : 0);
    }
}
