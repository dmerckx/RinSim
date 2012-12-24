package rinde.sim.core.model.pdp.apis;

import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.simulation.TimeInterval;

public interface DeliveryAPI {

    public boolean isDelivered();
    
    public Parcel getParcel();
    
    public DeliveryState getDeliveryState();
    
    public boolean canBeDelivered(TimeInterval time);
    
    public DeliveryPointState getState();
    
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
         * State that indicates that a {@link Container2} is currently delivering
         * the {@link Parcel} into this {@link DeliveryPoint}.
         */
        BEING_DELIVERED,
        /**
         * Indicates that the {@link Parcel} has been delivered into this {@link DeliveryPoint}.
         */
        DELIVERED
    }
}
