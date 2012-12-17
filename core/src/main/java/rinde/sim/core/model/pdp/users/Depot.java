package rinde.sim.core.model.pdp.users;

import rinde.sim.core.model.pdp.apis.ContainerAPI;
import rinde.sim.core.model.pdp.apis.ContainerState;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.model.road.apis.RoadState;
import rinde.sim.core.model.road.users.FixedRoadUser;

public class Depot<D extends ContainerData> implements Container<D>, FixedRoadUser<D>{

    protected RoadAPI roadAPI;
    protected ContainerAPI containerAPI;
    
    @Override
    public final void setRoadAPI(RoadAPI api){
        this.roadAPI = api;
    }

    @Override
    public final RoadState getRoadState() {
        return roadAPI.getState();
    }
    
    @Override
    public final void setContainerAPI(ContainerAPI api){
        api.init(roadAPI);
        this.containerAPI = api;
    }

    @Override
    public final ContainerState getContainerState() {
        return containerAPI.getState();
    }
}
