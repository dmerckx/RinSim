package rinde.sim.core.model.pdp.receivers;

import rinde.sim.core.model.interaction.Notification;
import rinde.sim.core.model.pdp.users.Parcel;

/**
 * A simple superclass for {@link DeliveryNotification} and
 * {@link PickupNotification}.
 * 
 * @author dmerckx
 *
 */
public class ContainerNotification implements Notification{
    
    private final NotificationType type;
    private final Parcel parcel;
    
    public ContainerNotification(NotificationType type, Parcel parcel) {
        this.type = type;
        this.parcel = parcel;
    }
    
    public enum NotificationType{
        DELIVERY,
        PICKUP
    }

    public NotificationType getType(){
        return type;
    }
    
    public Parcel getParcel(){
        return parcel;
    }
}
