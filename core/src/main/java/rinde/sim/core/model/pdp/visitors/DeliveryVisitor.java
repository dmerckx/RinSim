package rinde.sim.core.model.pdp.visitors;

import java.util.List;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.interaction.Visitor;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.receivers.DeliveryReceiver;
import rinde.sim.core.simulation.TimeLapse;

/**
 * This class represents a {@link Visitor} interested in delivering {@link Parcel}s.
 * This visitor will approach {@link DeliveryReceiver}s and check 
 * whether they can accept one of his parcels.
 * 
 * If any such receiver can be found it is given the parcel, this parcel is then returned
 * as a result of the visit.
 * 
 * @author dmerckx
 */
public class DeliveryVisitor extends Visitor<DeliveryReceiver, Parcel> {

    private final List<Parcel> parcels;
    
    /**
     * @param location The location of this visitor.
     * @param parcels The parcels of which this visitor will attempt to deliver one.
     */
    @SuppressWarnings("hiding")
    public DeliveryVisitor(Point location, List<Parcel> parcels) {
        super(DeliveryReceiver.class, location);
        this.parcels = parcels;
    }
     
    @Override
    public Parcel visit(TimeLapse lapse, List<DeliveryReceiver> targets) {
        for(DeliveryReceiver r:targets){
            for(Parcel parcel:parcels){
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
    protected boolean tryDelivery(TimeLapse lapse, Parcel parcel, DeliveryReceiver receiver){
        if(!receiver.canAccept(lapse, parcel, this))
            return false;
        
        receiver.deliver(lapse, parcel);
        return true;
    }
}
