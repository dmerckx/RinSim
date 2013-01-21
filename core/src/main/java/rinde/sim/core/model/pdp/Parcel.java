package rinde.sim.core.model.pdp;

import java.io.Serializable;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.interaction.Result;
import rinde.sim.util.TimeWindow;

public class Parcel extends Result implements Serializable{
    
    public final Point location;
    public final Point destination;
    
    public final double magnitude;
    
    public final long pickupDuration;
    public final long deliveryDuration;
    
    public final TimeWindow pickupTimeWindow;
    public final TimeWindow deliveryTimeWindow;
    
    /**
     * Create a new parcel.
     * @param pDestination The position where this parcel needs to be delivered.
     * @param pPickupDuration The time needed for pickup.
     * @param pDeliveryDuration The time needed for delivery.
     * @param pMagnitude The weight/volume/count of this parcel.
     */
    public Parcel(Point from, Point to, long pPickupDuration,
            TimeWindow pickupTW, long pDeliveryDuration, TimeWindow deliveryTW,
            double pMagnitude) {
        location = from;
        destination = to;
        pickupDuration = pPickupDuration;
        pickupTimeWindow = pickupTW;
        deliveryDuration = pDeliveryDuration;
        deliveryTimeWindow = deliveryTW;
        magnitude = pMagnitude;
    }
    
    @Override
    public Parcel clone(){
        return new Parcel(location, destination, pickupDuration, pickupTimeWindow, 
                        deliveryDuration, deliveryTimeWindow, magnitude);
    }
}
