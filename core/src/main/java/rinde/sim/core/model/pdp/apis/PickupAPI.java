package rinde.sim.core.model.pdp.apis;

import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.simulation.TimeInterval;

/**
 * The API provided to each individual {@link PickupPoint}.
 * This API allows to retrieve the current state of this user.
 * 
 * Note that there is not a single method in this API that does
 * not return the same result within a single turn.
 * 
 * When a parcel from a pickup point is actually picked up, it
 * is not reflected until the start of the next turn! 
 * It is impossible to retrieve this during the actual turn, since
 * that could break deterministic executing of the simulator.
 * 
 * @author dmerckx
 */
public interface PickupAPI {
    
    /**
     * Returns a presentation of the state of the user of this API. 
     * @return The state of this user.
     */
    public PickupPointState getState();
    
    /**
     * Returns true iff the package contained by this pickup point is
     * either being picked up or picked up.
     * @return Whether the contained package is picked up yet.
     */
    public boolean isPickedUp();
    
    /**
     * Data of the parcel that was originally contained by this pickup point.
     * @return The parcel associated with this point.
     */
    public Parcel getParcel();
    
    /**
     * Returns the current state of this pickup point as an enum.
     * @return The current state.
     */
    public PickupState getPickupState();

    /**
     * Check whether the time constraints on this {@link PickupPoint} still
     * allow it to be picked up at the given time (if it were still present then).
     * @param time The time at which we would like to check.
     * @return Whether pickup is still allowed at the given time. 
     */
    public boolean canBePickedUp(TimeInterval time);
    
    /**
     * The possible states a {@link PickupPoint} can be in.
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
         * State that indicates that someone is currently picking up
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
