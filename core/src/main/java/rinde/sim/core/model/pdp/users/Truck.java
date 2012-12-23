package rinde.sim.core.model.pdp.users;

import rinde.sim.core.model.pdp.apis.ContainerAPI;
import rinde.sim.core.model.pdp.apis.ContainerState;
import rinde.sim.core.model.pdp.apis.TruckAPI;
import rinde.sim.core.model.road.apis.MovingRoadAPI;
import rinde.sim.core.model.road.apis.RoadState;
import rinde.sim.core.model.road.users.MovingRoadUser;

public class Truck<D extends TruckData> implements Container<D>, MovingRoadUser<D>{
   
    protected MovingRoadAPI roadAPI;
    protected ContainerAPI containerAPI;
    protected TruckAPI truckAPI;
    
    @Override
    public final void setRoadAPI(MovingRoadAPI api) {
        this.roadAPI = api;
    }
    
    @Override
    public RoadState getRoadState() {
        return roadAPI.getState();
    }
    
    @Override
    public void setContainerAPI(ContainerAPI api) {
        api.init(roadAPI);
        this.containerAPI = api;
    }
    
    @Override
    public ContainerState getContainerState() {
        return containerAPI.getState();
    }
    
    public final void setTruckAPI(TruckAPI api){
        api.init(roadAPI, containerAPI);
        this.truckAPI = api;
    }
}
