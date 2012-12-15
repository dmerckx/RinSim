package rinde.sim.core.model.pdp.receivers;

import java.util.List;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.twpolicy.TimeWindowPolicy;
import rinde.sim.core.model.pdp.users.Parcel;
import rinde.sim.core.model.pdp.visitors.DeliveryVisitor;
import rinde.sim.core.simulation.TimeLapse;

/**
 * This class extends a {@link DeliveryReceiver} by only allowing parcels from
 * the given list of targets to be delivered here.
 * 
 * @author dmerckx
 */
public class DeliverySpecificReceiver extends DeliveryReceiver {

    private final List<? extends Parcel> targets;
    
    /**
     * @param location The location of this receiver.
     * @param targets The specific targets that can be delivered to this receiver.
     * @param target The target class that can be delivered to this receiver.
     * @param policy The policy to apply on deliveries.
     * @param guard The guard from which this receiver originates.
     */
    @SuppressWarnings( "hiding" )
    public DeliverySpecificReceiver(Point location,
            List<? extends Parcel> targets, TimeWindowPolicy policy) {
        super(location, Parcel.class, policy);
        this.targets = targets;
    }

    @Override
    public boolean canAccept(TimeLapse time, Parcel parcel, DeliveryVisitor visitor){
        return targets.contains(parcel) &&
                 super.canAccept(time, parcel, visitor);
    }
}
