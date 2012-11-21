package rinde.sim.core.model.pdp.visitors;

import java.util.List;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.interaction.Visitor;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.receivers.PickupReceiver;
import rinde.sim.core.simulation.TimeLapse;

/**
 * This class represents a {@link Visitor} interested in a certain type of {@link Parcel}s.
 * This visitor will approach {@link PickupReceiver}s and check whether they have any 
 * available parcels of the correct type.
 * 
 * If any such parcel can be found it is picked up and the parcel is return as result
 * of the visit.
 * 
 * @author dmerckx
 *
 * @param <P> The type of parcels in which the visitor is interested.
 */
public class PickupVisitor<P extends Parcel> extends Visitor<PickupReceiver, P>{

    private final double capacity;
    private final Class<? extends P> parcelType;
    
    /**
     * @param parcelType The type of parcel that this visitor will attempt to pick up.
     * @param location The location of this visitor.
     * @param capacity The available capacity of this visitor.
     */
    @SuppressWarnings("hiding")
    public PickupVisitor(Class<? extends P> parcelType, Point location, double capacity) {
        super(PickupReceiver.class, location);
        this.capacity = capacity;
        this.parcelType = parcelType;
    }
     
    @Override
    public P visit(TimeLapse lapse, List<PickupReceiver> targets) {
        for(PickupReceiver r:targets){
            P p = tryPickup(lapse, r);
            if( p != null) return p;
        }
        
        return null;
    }
    
    /**
     * @param lapse The time at which this action occurs.
     * @param receiver The receiver that is approached by this visitor.
     * @return Returns the picked up parcel if successful, <code>null</code> otherwise.
     */
    @SuppressWarnings("unchecked")
    protected P tryPickup(TimeLapse lapse, PickupReceiver receiver){
        for(Parcel p:receiver.getParcels()){
            if(canPickup(p) && receiver.canBePickedUp(lapse, p, this)){
                receiver.pickup(p);
                return (P) p;
            }
        }
        
        return null;
    }

    /**
     * @param parcel The parcels that should be checked.
     * @return Returns whether or not the given parcel can be picked up.
     */
    protected boolean canPickup(Parcel parcel) {
        return parcel.getClass().isAssignableFrom(parcelType)
                && capacity >= parcel.magnitude;
    }
}
