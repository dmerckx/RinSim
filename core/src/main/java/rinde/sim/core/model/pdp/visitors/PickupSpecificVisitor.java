package rinde.sim.core.model.pdp.visitors;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.supported.Parcel;

/**
 * This class extends a {@link PickupVisitor} by only attempting to pick up
 * one specific parcel.
 * 
 * @author dmerckx
 *
 * @param <P> The type of parcels in which the visitor is interested.
 */
public class PickupSpecificVisitor<P extends Parcel> extends PickupVisitor<P>{

    private final P parcel;
    
    /**
     * @param parcel The specific parcel that should be picked up.
     * @param location The location of this visitor.
     * @param capacity The available capacity of this visitor.
     */
    @SuppressWarnings({ "unchecked", "hiding" })
    public PickupSpecificVisitor(P parcel, Point location, double capacity) {
        super((Class<? extends P>) parcel.getClass(), location, capacity);
        this.parcel = parcel; 
    }
    
    @Override
    public boolean canPickup(Parcel p) {
        return p == parcel && super.canPickup(p);
    }

}
