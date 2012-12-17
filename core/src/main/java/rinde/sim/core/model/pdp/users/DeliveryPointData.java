package rinde.sim.core.model.pdp.users;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.users.RoadData;

public abstract class DeliveryPointData implements RoadData{

    public abstract Parcel getParcel();
    
    @Override
    public Point getStartPosition() {
        return getParcel().destination;
    }
}
