package rinde.sim.core.model.pdp.receivers;

import rinde.sim.core.model.pdp.supported.Parcel;

/**
 * A simple message class carrying the reference to a parcel that was
 * picked up.
 * 
 * @author dmerckx
 */
public class PickupNotification implements ContainerNotification{

    /**
     * Contains a reference to the parcel that was picked up.
     */
    public final Parcel parcel;
    
    /**
     * @param parcel The parcel that is picked up.
     */
    @SuppressWarnings("hiding")
    public PickupNotification(Parcel parcel) {
        this.parcel = parcel;
    }
}
