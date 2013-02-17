package rinde.sim.core.model.pdp.apis;

import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.users.DeliveryPoint;
import rinde.sim.core.simulation.TimeInterval;

/**
 * The API provided to each individual {@link DeliveryPoint}.
 * This API allows to retrieve the current state of this user.
 * 
 * Note that there is not a single method in this API that does
 * not return the same result within a single turn.
 * 
 * When a parcel from a delivery point is actually delivered, it
 * is not reflected until the start of the next turn! 
 * It is impossible to retrieve this during the actual turn, since
 * that could break deterministic executing of the simulator.
 * 
 * @author dmerckx
 */
public interface DeliveryAPI {
    
    /**
     * Returns a presentation of the state of the user of this API. 
     * @return The state of this user.
     */
    public DeliveryPointState getState();

    /**
     * Returns true iff the package contained by this pickup point is
     * either being delivered or delivered.
     * @return Whether the contained package is delivered yet.
     */
    public boolean isDelivered();
    
    /**
     * Data of the parcel that should be delivered to this point.
     * @return The parcel associated with this point.
     */
    public Parcel getParcel();
    
    /**
     * Returns the current state of this pickup point as an enum.
     * @return The current state.
     */
    public DeliveryState getDeliveryState();

    /**
     * Check whether the time constraints on this {@link DeliveryPoint} still
     * allow it to be delivered at the given time (if it was not already delivered then).
     * @param time The time at which we would like to check.
     * @return Whether delivery is still allowed at the given time. 
     */
    public boolean canBeDelivered(TimeInterval time);
    
    /**
     * The possible states a {@link DeliveryPoint} can be in.
     */
    public enum DeliveryState {
        /**
         * The initial state, indicating that a {@link DeliveryPoint} is not yet
         * ready to receive a {@link Parcel} but will be at some point in the future.
         */
        SETTING_UP,
        /**
         * The 'normal' state, indicating that a {@link DeliveryPoint} is waiting for
         * the {@link Parcel} to arrive.
         */
        AVAILABLE,
        /**
         * Indicates that the {@link Parcel} is not yet delivered, and it is late.
         */
        LATE,
        /**
         * State that indicates that someone is still currently delivering
         * the {@link Parcel} into this {@link DeliveryPoint}.
         */
        BEING_DELIVERED,
        /**
         * Indicates that the {@link Parcel} has been delivered into this {@link DeliveryPoint}.
         */
        DELIVERED
    }
}
