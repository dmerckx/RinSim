package rinde.sim.core.model.pdp.users;

import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.users.RoadData;

public interface ContainerData extends RoadData{

    double getCapacity();
    
    Class<? extends Parcel> getParcelType();
}
