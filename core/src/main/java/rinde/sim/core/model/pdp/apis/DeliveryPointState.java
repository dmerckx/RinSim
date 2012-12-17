package rinde.sim.core.model.pdp.apis;

import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.apis.DeliveryAPI.DeliveryState;

public abstract class DeliveryPointState {

    DeliveryPointState() {}
    
    public abstract Parcel getParcel();
    
    public abstract DeliveryState getDeliveryState();
}
