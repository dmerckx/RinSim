package rinde.sim.core.model.pdp.users;

import rinde.sim.core.model.pdp.apis.TruckAPI;
import rinde.sim.core.model.pdp.supported.Parcel;
import rinde.sim.core.model.road.users.MovingRoadUser;

public interface Truck<P extends Parcel> extends Container<P>, MovingRoadUser{
   
    public void setTruckAPI(TruckAPI api);
}
