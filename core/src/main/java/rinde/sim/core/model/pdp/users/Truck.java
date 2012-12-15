package rinde.sim.core.model.pdp.users;

import rinde.sim.core.model.pdp.apis.ContainerAPI;
import rinde.sim.core.model.pdp.apis.TruckAPI;
import rinde.sim.core.model.road.apis.MovingRoadAPI;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.model.road.users.MovingRoadUser;

public abstract class Truck
            implements Container<TruckData>, MovingRoadUser<TruckData>, PdpUser<TruckData>{
   
    protected MovingRoadAPI roadAPI;
    protected ContainerAPI containerAPI;
    protected TruckAPI truckAPI;
    
    @Override
    public void setRoadAPI(RoadAPI roadAPI) {
        this.roadAPI = (MovingRoadAPI) roadAPI;
    }
    
    @Override
    public void setContainerAPI(ContainerAPI api) {
        this.containerAPI = api;
    }
    
    public void setTruckAPI(TruckAPI api){
        this.truckAPI = api;
    }
}
