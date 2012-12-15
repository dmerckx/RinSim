package rinde.sim.core.model.pdp.users;

import rinde.sim.core.model.pdp.apis.ContainerAPI;
import rinde.sim.core.model.road.apis.RoadAPI;

public abstract class Depot implements Container<ContainerData>{

    protected RoadAPI roadAPI;
    protected ContainerAPI containerAPI;
    
    @Override
    public void setRoadAPI(RoadAPI api){
        this.roadAPI = api;
    }
    
    @Override
    public void setContainerAPI(ContainerAPI api){
        this.containerAPI = api;
    }
}
