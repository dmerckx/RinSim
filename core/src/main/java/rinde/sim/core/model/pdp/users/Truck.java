package rinde.sim.core.model.pdp.users;

import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.apis.ContainerAPI;
import rinde.sim.core.model.pdp.apis.TruckAPI;
import rinde.sim.core.model.pdp.supported.TruckUnit;
import rinde.sim.core.model.road.apis.MovingRoadAPI;
import rinde.sim.core.model.road.users.MovingRoadUser;

public abstract class Truck<P extends Parcel> implements Container<P>, MovingRoadUser{
   
    protected MovingRoadAPI roadAPI;
    protected ContainerAPI<P> containerAPI;
    protected TruckAPI<P> truckAPI;
    
    public void setContainerAPI(ContainerAPI<P> api){
        this.containerAPI = api;
    }
    
    public void setTruckAPI(TruckAPI api){
        this.truckAPI = api;
    }
    
    public void setMovingRoadAPI(MovingRoadAPI api){
        this.roadAPI = api;
    }
    
    @Override
    public abstract TruckUnit<P> buildUnit();
}
