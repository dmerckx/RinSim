package rinde.sim.core.model.pdp.apis;

import java.awt.Container;

import rinde.sim.core.model.communication.Address;
import rinde.sim.core.model.communication.users.CommUser;
import rinde.sim.core.model.pdp.Parcel;

public interface PickupAPI {

    public void init(Parcel parcelToDeliver);
    
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
    public boolean canBePickedUp();
    
    /**
     * @return Returns whether or not this point is locked for having
     * parcels delivered.
     */
    public boolean isLocked();
    
    /**
     * Lock this point from having its parcel being picked up.
     * Note that this only goes into effect <b>the following tick</b>.
     */
    public void lock();
    
    /**
     * Unlocks this point, any can pickup its parcel.
     * Note that this only goes into effect <b>the following tick</b>. 
     */
    public void unlock();
    
    /**
     * Reserve using this point for the specified {@link Address},
     * only the {@link CommUser} with this specific
     * address will be able to pickup this {@link PickupPoint}.
     * Note that this only goes into effect <b>the following tick</b>.
     */
    public void lockForAddress(Address address);
    
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
