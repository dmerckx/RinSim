package rinde.sim.core.model.pdp.apis;

import java.awt.Container;

import rinde.sim.core.model.communication.Address;
import rinde.sim.core.model.communication.users.CommUser;
import rinde.sim.core.model.pdp.users.Parcel;
import rinde.sim.core.simulation.TimeInterval;

public interface PickupAPI {

    public boolean isPickedUp();
    
    /**
     * 
     * @return
     */
    public Parcel getParcel();
    
    /**
     * @return Returns the state of this point.
     */
    public PickupState getState();

    /**
     * Check whether the constraints on this {@link PickupPoint} still
     * allow it to be picked up.
     * Returns true iff the parcel is not gone yet and 
     * 
     * @return
     */
    public boolean canBePickedUp(TimeInterval time);
    
    
    /**
     * The possible states a {@link Container} can be in.
     */
    public enum PickupState {
        /**
         * The initial state, indicating that a {@link PickupPoint} is not yet
         * ready to have a {@link Parcel} picked up but will be at some point in the future.
         */
        SETTING_UP,
        /**
         * The 'normal' state, indicating that a {@link PickupPoint} has a {@link Parcel}
         * ready to be picked up.
         */
        AVAILABLE,
        /**
         * Indicates that the {@link Parcel} is not yet picked up, and it is late.
         */
        LATE,
        /**
         * State that indicates that a {@link Container2} is currently picking up
         * the {@link Parcel} from this {@link PickupPoint}.
         */
        BEING_PICKED_UP,
        /**
         * Indicates that the {@link Parcel} in this {@link PickupPoint} has been picked
         * up.
         */
        PICKED_UP
    }
}
