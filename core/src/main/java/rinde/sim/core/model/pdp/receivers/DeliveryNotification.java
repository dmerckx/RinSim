package rinde.sim.core.model.pdp.receivers;

import rinde.sim.core.model.pdp.supported.Parcel;

/**
 * A simple message class carrying the reference to a parcel that was
 * delivered.
 * 
 * @author dmerckx
 */
public class DeliveryNotification implements ContainerNotification{

    /**
     * Contains a reference to the parcel that was delivered.
     */
    public final Parcel parcel;
    
    /**
     * @param parcel The parcel that is delivered.
     */
    @SuppressWarnings("hiding")
    public DeliveryNotification(Parcel parcel) {
        this.parcel = parcel;
    }
}