package rinde.sim.core.model.pdp.apis;

import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.apis.PickupAPI.PickupState;

public abstract class PickupPointState {

    PickupPointState() {}
    
    public abstract Parcel getParcel();
    
    public abstract PickupState getPickupState();
}
