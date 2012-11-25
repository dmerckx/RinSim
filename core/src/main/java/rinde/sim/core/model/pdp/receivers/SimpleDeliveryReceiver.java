package rinde.sim.core.model.pdp.receivers;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.interaction.Receiver;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.twpolicy.TimeWindowPolicy;
import rinde.sim.core.model.pdp.visitors.DeliveryVisitor;
import rinde.sim.core.simulation.TimeLapse;

public class SimpleDeliveryReceiver<P extends Parcel> extends Receiver{
    
    private final P parcel;
    private final TimeWindowPolicy policy;

    /**
     * @param location The location of this receiver.
     * @param target The target class that can be delivered to this receiver.
     * @param policy The policy to apply on deliveries.
     * @param guard The guard from which this receiver originates.
     */
    @SuppressWarnings("hiding")
    public SimpleDeliveryReceiver(Point location, P parcel, TimeWindowPolicy policy) {
        super(location);
        this.parcel = parcel;
        this.policy = policy;
    }

    /**
     * @param time The time at which the action happens.
     * @param parcel The parcel that the approaching visitor wants to deliver.
     * @param visitor The approaching visitor.
     * @return Whether or not the visitor is allowed to deliver the given
     *         parcel.
     */
    public boolean canAccept(TimeLapse time, Parcel parcel, DeliveryVisitor<?> visitor) {
        return this.parcel == parcel
                && policy.canDeliver(parcel.deliveryTimeWindow, time.getCurrentTime(), parcel.deliveryDuration);
    }

    /**
     * @param parcel Deliver the specified parcel.
     */
    public void deliver(Parcel parcel) {
        terminate();
    }
}
