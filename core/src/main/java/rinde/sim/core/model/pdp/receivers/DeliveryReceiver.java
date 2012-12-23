package rinde.sim.core.model.pdp.receivers;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.interaction.ExtendedReceiver;
import rinde.sim.core.model.interaction.Receiver;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.receivers.ContainerNotification.NotificationType;
import rinde.sim.core.model.pdp.twpolicy.TimeWindowPolicy;
import rinde.sim.core.model.pdp.visitors.DeliveryVisitor;
import rinde.sim.core.simulation.TimeLapse;

/**
 * This class represents a {@link Receiver} publishing himself as a point that
 * {@link DeliveryVisitor}s can approach to deliver certain {@link Parcel}s.
 * 
 * As soon as a visitor delivers a parcel to this receiver it will send a
 * notification and terminate. This makes it impossible to deliver multiple
 * parcels at the same time.
 * 
 * @author dmerckx
 */
public class DeliveryReceiver extends ExtendedReceiver {

    private final Class<? extends Parcel> target;
    private final TimeWindowPolicy policy;

    /**
     * @param location The location of this receiver.
     * @param target The target class that can be delivered to this receiver.
     * @param policy The policy to apply on deliveries.
     * @param guard The guard from which this receiver originates.
     */
    @SuppressWarnings("hiding")
    public DeliveryReceiver(Point location, Class<? extends Parcel> target,
            TimeWindowPolicy policy) {
        super(location);
        this.target = target;
        this.policy = policy;
    }

    /**
     * @param time The time at which the action happens.
     * @param parcel The parcel that the approaching visitor wants to deliver.
     * @param visitor The approaching visitor.
     * @return Whether or not the visitor is allowed to deliver the given
     *         parcel.
     */
    public boolean canAccept(TimeLapse time, Parcel parcel, DeliveryVisitor visitor) {
       return parcel.getClass().isAssignableFrom(target)
                && policy.canDeliver(parcel.deliveryTimeWindow, time.getCurrentTime(), parcel.deliveryDuration);
    }

    /**
     * @param parcel Deliver the specified parcel.
     */
    public void deliver(Parcel parcel) {
        sendNotification(new ContainerNotification(NotificationType.DELIVERY, parcel));
        terminate();
    }
}
