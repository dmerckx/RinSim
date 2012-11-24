package rinde.sim.core.model.pdp.users;

import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.apis.ContainerAPI;
import rinde.sim.core.model.road.apis.RoadAPI;

public abstract class Depot<P extends Parcel> implements Container<P>{

    protected RoadAPI roadAPI;
    protected ContainerAPI containerAPI;
    
    public void setRoadAPI(RoadAPI api){
        this.roadAPI = api;
    }
    
    public void setContainerAPI(ContainerAPI<P> api){
        this.containerAPI = api;
    }
}
