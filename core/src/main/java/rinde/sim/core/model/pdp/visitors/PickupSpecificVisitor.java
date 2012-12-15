package rinde.sim.core.model.pdp.visitors;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.users.Parcel;

/**
 * This class extends a {@link PickupVisitor} by only attempting to pick up
 * one specific parcel.
 * 
 * @author dmerckx
 *
 * @param <P> The type of parcels in which the visitor is interested.
 */
public class PickupSpecificVisitor extends PickupVisitor{

    private final Parcel parcel;
    
    /**
     * @param parcel The specific parcel that should be picked up.
     * @param location The location of this visitor.
     * @param capacity The available capacity of this visitor.
     */
    @SuppressWarnings({ "unchecked", "hiding" })
    public PickupSpecificVisitor(Parcel parcel, Point location, double capacity) {
        super((Class<Parcel>) parcel.getClass(), location, capacity);
        this.parcel = parcel; 
    }
    
    @Override
    public boolean canPickup(Parcel p) {
        return p == parcel && super.canPickup(p);
    }

}
