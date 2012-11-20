package rinde.sim.core.model.pdp.visitors;

import java.util.List;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.interaction.Visitor;
import rinde.sim.core.model.pdp.receivers.DeliveryReceiver;
import rinde.sim.core.model.pdp.supported.Parcel;
import rinde.sim.core.simulation.TimeLapse;

/**
 * This class represents a {@link Visitor} interested in delivering a certain type of
 * {@link Parcel}s. This visitor will approach {@link DeliveryReceiver}s and check 
 * whether they can accept one of his parcels.
 * 
 * If any such receiver can be found it is given the parcel, this parcel is then returned
 * as a result of the visit.
 * 
 * @author dmerckx
 *
 * @param <P> The type of parcels in that this visitor is trying to deliver.
 */
public class DeliveryVisitor<P extends Parcel> extends Visitor<DeliveryReceiver, P> {

    private final List<P> parcels;
    
    /**
     * @param location The location of this visitor.
     * @param parcels The parcels of which this visitor will attempt to deliver one.
     */
    @SuppressWarnings("hiding")
    public DeliveryVisitor(Point location, List<P> parcels) {
        super(DeliveryReceiver.class, location);
        this.parcels = parcels;
    }
     
    @Override
    public P visit(TimeLapse lapse, List<DeliveryReceiver> targets) {
        for(DeliveryReceiver r:targets){
            for(P parcel:parcels){
                if(tryDelivery(lapse, parcel, r)) return parcel;
            }
        }
        
        return null;
    }
    
    /**
     * @param lapse The time at which the action happens.
     * @param parcel The parcel that the approaching visitor wants to deliver.
     * @param receiver The receiver that is approached by this visitor.
     * @return Returns whether or not the given receiver accepted this delivery.
     */
    protected boolean tryDelivery(TimeLapse lapse, P parcel, DeliveryReceiver receiver){
        if(!receiver.canAccept(lapse, parcel, this))
            return false;
        
        receiver.deliver(parcel);
        return true;
    }
}
